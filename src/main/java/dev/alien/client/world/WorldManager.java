package dev.alien.client.world;

import dev.alien.client.AlienClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Installs Forge render hooks only; never writes world time, weather or player state. */
public final class WorldManager {
    public static String lastImportMessage = "PNG equirectangular, at most 4096 x 2048 / 16 MiB";
    private final Minecraft mc = Minecraft.getMinecraft();
    private final IRenderHandler sky = new Sky();
    private final IRenderHandler weather = new Weather();
    private final IRenderHandler hiddenClouds = new IRenderHandler() {
        @Override public void render(float partialTicks, WorldClient world, Minecraft minecraft) { }
    };
    public static ResourceLocation importedPreview;
    private final List<Particle> particles = new ArrayList<Particle>();
    private final Random random = new Random(0xA11E);
    private final double[][] starGeometry = createStars();
    private final double[][] skyLatitude = angles(25, -Math.PI / 2, Math.PI / 24);
    private final double[][] skyAzimuth = angles(65, 0, Math.PI * 2 / 64);
    private WorldProvider provider;
    private IRenderHandler previousSky, previousWeather, previousClouds;
    private ResourceLocation importedSky;
    private SkyboxAtlas importedAtlas;
    private String importedKey = "";
    private boolean wasGrounded;
    private long ticks;

    private static double[][] angles(int count,double start,double step) {
        double[][] result=new double[count][2];
        for(int i=0;i<count;i++) { result[i][0]=Math.sin(start+i*step);result[i][1]=Math.cos(start+i*step); }
        return result;
    }

    private static double[][] createStars() {
        Random starRandom = new Random(73);
        double[][] stars = new double[1000][4];
        for (int i = 0; i < stars.length; i++) {
            double x = starRandom.nextDouble() * 2D - 1D, y = starRandom.nextDouble(), z = starRandom.nextDouble() * 2D - 1D;
            double norm = Math.max(.001D, Math.sqrt(x*x+y*y+z*z));
            stars[i][0] = x / norm * 98D; stars[i][1] = y / norm * 98D; stars[i][2] = z / norm * 98D;
            stars[i][3] = .8D + starRandom.nextDouble() * .2D;
        }
        return stars;
    }

    private WorldConfig world() { return AlienClient.get().config().world; }
    private EffectsConfig effects() { return AlienClient.get().config().effects; }

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ticks++;
        WorldProvider next = mc.theWorld == null ? null : mc.theWorld.provider;
        if (provider != next) {
            restore(); provider = next;
            if (provider != null) { previousSky = provider.getSkyRenderer(); previousWeather = provider.getWeatherRenderer(); previousClouds = provider.getCloudRenderer(); }
            particles.clear(); wasGrounded = false;
        }
        if (provider == null) return;
        WorldConfig settings = world();
        // Leave End/Nether and other dimension renderers entirely under their owner's control.
        boolean surface = provider.getDimensionId() == 0;
        if (settings.enabled && surface) {
            if (provider.getSkyRenderer() != sky) {
                previousSky = provider.getSkyRenderer(); provider.setSkyRenderer(sky);
            }
            if (ticks % 40 == 0) loadSkybox(settings.skyboxFile);
        } else if (provider.getSkyRenderer() == sky) provider.setSkyRenderer(previousSky);
        boolean replaceWeather = surface && !"Server".equals(settings.weather);
        if (replaceWeather && provider.getWeatherRenderer() != weather) {
            previousWeather = provider.getWeatherRenderer(); provider.setWeatherRenderer(weather);
        } else if (!replaceWeather && provider.getWeatherRenderer() == weather) provider.setWeatherRenderer(previousWeather);
        boolean hideClouds = surface && settings.enabled && "Hidden".equals(settings.cloudMode);
        if (hideClouds && provider.getCloudRenderer() != hiddenClouds) {
            previousClouds = provider.getCloudRenderer(); provider.setCloudRenderer(hiddenClouds);
        } else if (!hideClouds && provider.getCloudRenderer() == hiddenClouds) provider.setCloudRenderer(previousClouds);
        tickParticles();
    }

    @SubscribeEvent public void unload(WorldEvent.Unload event) {
        if (mc.theWorld == event.world) { restore(); provider = null; particles.clear(); }
    }

    private void restore() {
        if (provider == null) return;
        if (provider.getSkyRenderer() == sky) provider.setSkyRenderer(previousSky);
        if (provider.getWeatherRenderer() == weather) provider.setWeatherRenderer(previousWeather);
        if (provider.getCloudRenderer() == hiddenClouds) provider.setCloudRenderer(previousClouds);
    }

    private void loadSkybox(String name) {
        String key = name == null ? "" : name;
        File file = null;
        try {
            File directory = new File(AlienClient.get().store().directory(), "skyboxes");
            file=SkyboxLoader.resolve(directory,key);
            if (file != null) {
                key += ":" + file.lastModified() + ":" + file.length();
            }
            if (key.equals(importedKey)) return;
            importedKey = key;
            importedPreview = null;
            if (importedSky != null) { mc.getTextureManager().deleteTexture(importedSky); importedSky = null; }
            importedAtlas = null;
            if (file == null) { lastImportMessage = "Procedural sky active"; return; }
            BufferedImage image=SkyboxLoader.read(file);
            if (SkyboxAtlas.isCubeFile(file.getName())) importedAtlas = SkyboxAtlas.validate(image.getWidth(), image.getHeight());
            importedSky = mc.getTextureManager().getDynamicTextureLocation("alien_sky", new DynamicTexture(image));
            importedPreview = importedSky;
            lastImportMessage = "Loaded " + (importedAtlas == null ? "panorama " : "cubemap ") + file.getName();
        } catch (IOException | RuntimeException failure) {
            importedKey = key;
            lastImportMessage = "Skybox: " + failure.getMessage();
        }
    }

    @SubscribeEvent public void fog(EntityViewRenderEvent.FogColors event) {
        if (!world().fogEnabled || mc.theWorld == null || mc.theWorld.provider.getDimensionId() != 0) return;
        int color = world().fogColor;
        event.red = ((color >> 16) & 255) / 255F;
        event.green = ((color >> 8) & 255) / 255F;
        event.blue = (color & 255) / 255F;
    }

    @SubscribeEvent public void density(EntityViewRenderEvent.FogDensity event) {
        // Preserve water/lava blindness and similar gameplay visibility constraints.
        if (!world().fogEnabled || mc.thePlayer == null || mc.thePlayer.isInsideOfMaterial(net.minecraft.block.material.Material.water)
                || mc.thePlayer.isInsideOfMaterial(net.minecraft.block.material.Material.lava)
                || mc.thePlayer.isPotionActive(net.minecraft.potion.Potion.blindness)) return;
        if (mc.theWorld.provider.getDimensionId() != 0) return;
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP2);
        event.density = Math.max(0.0001F, world().fogDensity);
        event.setCanceled(true);
    }

    @SubscribeEvent public void camera(EntityViewRenderEvent.CameraSetup event) {
        EffectsConfig settings = effects();
        if (!settings.enabled || !settings.cameraSway || mc.thePlayer == null || mc.gameSettings.thirdPersonView != 0) return;
        double movement = Math.abs(mc.thePlayer.posX - mc.thePlayer.prevPosX) + Math.abs(mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
        float amount = (float)Math.min(1D, movement * 6D);
        // Roll affects the camera matrix only, never yaw/pitch, movement or targeting.
        event.roll += (float)Math.sin((ticks + event.renderPartialTicks) * .3D) * settings.swayStrength * amount;
    }

    @SubscribeEvent public void colorFilter(RenderGameOverlayEvent.Post event) {
        EffectsConfig settings = effects();
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !settings.enabled || !settings.colorFilter || mc.theWorld == null) return;
        int alpha = Math.round(settings.filterStrength * 255F);
        net.minecraft.client.gui.Gui.drawRect(0, 0, event.resolution.getScaledWidth(), event.resolution.getScaledHeight(),
                (alpha << 24) | (settings.color & 0xFFFFFF));
    }

    private void tickParticles() {
        EffectsConfig settings = effects();
        if (mc.thePlayer == null || !settings.enabled) { particles.clear(); return; }
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            if (--particle.life <= 0) iterator.remove();
            else { particle.x += particle.vx; particle.y += particle.vy; particle.z += particle.vz; particle.vy -= 0.001D; }
        }
        int budget = Math.min(settings.effectiveBudget(), Math.max(0, AlienClient.get().performance().particleBudget()));
        while (particles.size() > budget) particles.remove(0);
        boolean ground = mc.thePlayer.onGround;
        double movement = Math.abs(mc.thePlayer.posX - mc.thePlayer.prevPosX) + Math.abs(mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
        if (settings.landing && ground && !wasGrounded && mc.thePlayer.ticksExisted > 10) {
            for (int i = 0; i < 12 && particles.size() < budget; i++) {
                double angle = i * Math.PI / 6;
                add(mc.thePlayer.posX, mc.thePlayer.posY + 0.06D, mc.thePlayer.posZ,
                        Math.cos(angle) * 0.05D, 0.015D, Math.sin(angle) * 0.05D, 22);
            }
        }
        if ((settings.movement || settings.trails) && movement > 0.025D && particles.size() < budget) {
            int count = "Low".equals(settings.quality) ? 1 : 2;
            for (int i = 0; i < count && particles.size() < budget; i++) {
                add(mc.thePlayer.posX + (random.nextDouble() - .5D) * .4D, mc.thePlayer.posY + .15D,
                        mc.thePlayer.posZ + (random.nextDouble() - .5D) * .4D,
                        (random.nextDouble() - .5D) * .015D, .015D, (random.nextDouble() - .5D) * .015D,
                        settings.trails ? 28 : 12);
            }
        }
        if (settings.ambient && (ticks & 1) == 0 && particles.size() < budget) {
            int count="Low".equals(settings.quality)?1:2;
            for(int i=0;i<count && particles.size()<budget;i++) {
                double angle=random.nextDouble()*Math.PI*2, radius=1.1+random.nextDouble()*2.8;
                boolean petal="Petal".equals(settings.shape);
                add(mc.thePlayer.posX+Math.cos(angle)*radius,
                        mc.thePlayer.posY+.5+random.nextDouble()*2.6,
                        mc.thePlayer.posZ+Math.sin(angle)*radius,
                        petal?.005D:-.002D,petal?-.012D:.007D,petal?.003D:.001D,52+random.nextInt(24));
            }
        }
        wasGrounded = ground;
    }

    private void add(double x, double y, double z, double vx, double vy, double vz, int life) {
        particles.add(new Particle(x, y, z, vx, vy, vz, life,random.nextDouble()*Math.PI*2));
    }

    /**
     * Vanilla 1.8.9 skips RenderGlobal.renderSky entirely below four chunks. Fill
     * only untouched far-depth background in that case, preserving all terrain
     * depth and the player's selected render distance.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void lowDistanceSky(RenderWorldLastEvent event) {
        if (!world().enabled || mc.theWorld == null || mc.theWorld.provider.getDimensionId() != 0
                || mc.gameSettings.renderDistanceChunks >= 4 || mc.theWorld.provider.getSkyRenderer() != sky) return;
        // GL_DEPTH_RANGE belongs to GL_VIEWPORT_BIT, rather than GL_DEPTH_BUFFER_BIT.
        GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_ENABLE_BIT | GL11.GL_VIEWPORT_BIT);
        try {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(false);
            GL11.glDepthRange(1D, 1D);
            sky.render(event.partialTicks, mc.theWorld, mc);
        } finally {
            GL11.glPopAttrib();
        }
    }

    @SubscribeEvent public void renderParticles(RenderWorldLastEvent event) {
        if (particles.isEmpty() || !effects().enabled || mc.getRenderViewEntity() == null) return;
        Entity camera = mc.getRenderViewEntity();
        double cx = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.partialTicks;
        double cy = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.partialTicks;
        double cz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.partialTicks;
        double distance = Math.min(effects().distance, AlienClient.get().performance().effectDistance());
        double maxDistance = distance * distance;
        net.minecraft.util.Vec3 look = camera.getLook(event.partialTicks);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_POINT_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(-cx, -cy, -cz);
            GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_DEPTH_TEST); GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            double yaw=Math.toRadians(mc.getRenderManager().playerViewY),pitch=Math.toRadians(mc.getRenderManager().playerViewX);
            double rx=Math.cos(yaw),rz=Math.sin(yaw),ux=-Math.sin(yaw)*Math.sin(pitch),uy=Math.cos(pitch),uz=Math.cos(yaw)*Math.sin(pitch);
            EffectsConfig settings=effects();
            ParticleShapes.Shape shape=ParticleShapes.get(settings.shape);
            GL11.glBegin(GL11.GL_TRIANGLES);
            for (Particle particle : particles) {
                double dx = particle.x - cx, dy = particle.y - cy, dz = particle.z - cz;
                if (dx * dx + dy * dy + dz * dz > maxDistance || dx * look.xCoord + dy * look.yCoord + dz * look.zCoord < -2D) continue;
                int color=(particle.initialLife&1)==0?settings.color:settings.secondaryColor;
                float fade=Math.min(1F,Math.min((particle.initialLife-particle.life)/6F,particle.life/12F));
                double size=.028D*settings.size,rotation=particle.rotation+(ticks+event.partialTicks)*.025D;
                double cs=Math.cos(rotation),sn=Math.sin(rotation);
                for(int i=0;i<shape.vertexCount();i++) {
                    double u=shape.u(i),v=shape.v(i),px=(u*cs-v*sn)*size,py=(u*sn+v*cs)*size;
                    rgb(i%3==0?mix(color,0xFFFFFF,.38F):color,fade*(i%3==0?.9F:.72F));
                    GL11.glVertex3d(particle.x+rx*px+ux*py,particle.y+uy*py,particle.z+rz*px+uz*py);
                }
            }
            GL11.glEnd();
        } finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
    }

    private static void rgb(int color, float alpha) {
        GL11.glColor4f(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F, alpha);
    }

    private static int mix(int first, int second, float ratio) {
        float inverse = 1F - ratio;
        int r = (int)(((first >> 16) & 255) * inverse + ((second >> 16) & 255) * ratio);
        int g = (int)(((first >> 8) & 255) * inverse + ((second >> 8) & 255) * ratio);
        int b = (int)((first & 255) * inverse + (second & 255) * ratio);
        return (r << 16) | (g << 8) | b;
    }

    private final class Sky extends IRenderHandler {
        @Override public void render(float partialTicks, WorldClient clientWorld, Minecraft minecraft) {
            WorldConfig settings = world();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_POINT_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_LIGHTING_BIT);
            GL11.glPushMatrix();
            try {
                // Keep the sky inside the far plane even at the vanilla minimum view distance.
                GL11.glScalef(.32F,.32F,.32F);
                GL11.glDisable(GL11.GL_FOG); GL11.glDisable(GL11.GL_ALPHA_TEST); GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_CULL_FACE); GL11.glDepthMask(false);
                GL11.glShadeModel(GL11.GL_SMOOTH);
                GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                if (importedSky != null) {
                    // Raw bind stays inside glPushAttrib, leaving GlStateManager's cached binding unchanged.
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, minecraft.getTextureManager().getTexture(importedSky).getGlTextureId());
                    float brightness = Math.min(1F, settings.brightness * settings.skyboxBrightness);
                    GL11.glColor4f(brightness, brightness, brightness, 1F);
                    GL11.glPushMatrix();
                    try {
                        GL11.glRotated(settings.skyboxYaw + (ticks + partialTicks) * settings.skyboxDrift / 20D, 0D, 1D, 0D);
                        if (importedAtlas != null) renderCube(importedAtlas);
                        else renderSphere(settings, true);
                    } finally {
                        GL11.glPopMatrix();
                    }
                } else {
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    renderSphere(settings, false);
                }
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                double hour = settings.timeOverride ? settings.visualHour : ((clientWorld.getWorldTime() % 24000L) / 1000D + 6) % 24;
                double angle = (hour - 6D) / 24D * Math.PI * 2D;
                float night = (float)Math.max(.12D, Math.min(1D, .5D - Math.sin(angle)));
                GL11.glPointSize(1.5F);
                GL11.glBegin(GL11.GL_POINTS);
                for (int i = 0; i < Math.min(settings.stars,starGeometry.length); i++) {
                    double[] star = starGeometry[i];
                    float shimmer = settings.twinklingStars
                            ? (float)(.7D + .3D * Math.sin((ticks + partialTicks) * .065D + i * 1.17D))
                            : 1F;
                    GL11.glColor4f((float)star[3], .8F, 1F, night * shimmer);
                    GL11.glVertex3d(star[0], star[1], star[2]);
                }
                GL11.glEnd();
                if ("Aurora".equals(settings.theme)) aurora(partialTicks);
                if ("Nebula".equals(settings.theme) || "Galaxy".equals(settings.theme)) nebula(partialTicks);
                if (settings.sunMoon) {
                    GL11.glPushMatrix();
                    GL11.glRotated(Math.toDegrees(angle), 0D, 0D, 1D);
                    disk(90D, 5D * settings.sunSize, 0xFFE2AE); disk(-90D, 3.5D * settings.moonSize, 0xDAD9F9);
                    GL11.glPopMatrix();
                }
            } finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
        }

        private void renderSphere(WorldConfig settings, boolean textured) {
            for (int band = 0; band < 24; band++) {
                GL11.glBegin(GL11.GL_QUAD_STRIP);
                for (int segment = 0; segment <= 64; segment++) {
                    for (int j = 0; j < 2; j++) {
                        double[] latitude = skyLatitude[band + j], azimuth = skyAzimuth[segment];
                        if (!textured) rgb(settings.skyColor(mix(settings.skyBottom, settings.skyTop,
                                (float)Math.max(0, latitude[0]))), 1);
                        GL11.glTexCoord2f(segment / 64F, 1F - (band + j) / 24F);
                        GL11.glVertex3d(azimuth[1] * latitude[1] * 100D,
                                latitude[0] * 100D, azimuth[0] * latitude[1] * 100D);
                    }
                }
                GL11.glEnd();
            }
        }

        private void renderCube(SkyboxAtlas atlas) {
            GL11.glBegin(GL11.GL_QUADS);
            for (int face = 0; face < 6; face++) {
                for (int corner = 0; corner < 4; corner++) {
                    GL11.glTexCoord2f(atlas.u(face, corner), atlas.v(face, corner));
                    GL11.glVertex3d(atlas.coordinate(face, corner, 0),
                            atlas.coordinate(face, corner, 1), atlas.coordinate(face, corner, 2));
                }
            }
            GL11.glEnd();
        }

        private void disk(double x, double radius, int color) {
            rgb(color, 1);
            GL11.glBegin(GL11.GL_TRIANGLE_FAN); GL11.glVertex3d(x, 0, 0);
            for (int i = 0; i <= 32; i++) {
                double theta = i * Math.PI / 16;
                GL11.glVertex3d(x, Math.cos(theta) * radius, Math.sin(theta) * radius);
            }
            GL11.glEnd();
        }

        private void aurora(float partialTicks) {
            double time = (ticks + partialTicks) * .007D;
            for (int ribbon = 0; ribbon < 3; ribbon++) {
                GL11.glBegin(GL11.GL_QUAD_STRIP);
                for (int i = -32; i <= 32; i++) {
                    double x = i * 2D, z = -65D + ribbon * 15D + Math.sin(i * .15D + time) * 8D;
                    double y = 40D + Math.sin(i * .12D + time + ribbon) * 10D;
                    GL11.glColor4f(.18F, .85F, .7F, .24F); GL11.glVertex3d(x, y, z);
                    GL11.glColor4f(.4F, .3F, 1F, 0F); GL11.glVertex3d(x, y + 18D, z);
                }
                GL11.glEnd();
            }
        }

        private void nebula(float partialTicks) {
            GL11.glPushMatrix(); GL11.glRotatef(28, 0, 0, 1);
            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (int i = -32; i <= 32; i++) {
                double x = i * 3D, y = 48D + Math.sin(i * .16D + (ticks + partialTicks) * .0005D) * 5D;
                GL11.glColor4f(.42F, .24F, .72F, .15F); GL11.glVertex3d(x, y, -58);
                GL11.glColor4f(.6F, .3F, .7F, 0F); GL11.glVertex3d(x, y + 22D, -58);
            }
            GL11.glEnd(); GL11.glPopMatrix();
        }
    }

    private final class Weather extends IRenderHandler {
        @Override public void render(float partialTicks, WorldClient clientWorld, Minecraft minecraft) {
            WorldConfig settings = world();
            if ("Clear".equals(settings.weather) || "Server".equals(settings.weather) || settings.weatherIntensity <= 0 || mc.getRenderViewEntity() == null) return;
            Entity camera = mc.getRenderViewEntity();
            double cx = camera.lastTickPosX + (camera.posX-camera.lastTickPosX)*partialTicks;
            double cy = camera.lastTickPosY + (camera.posY-camera.lastTickPosY)*partialTicks;
            double cz = camera.lastTickPosZ + (camera.posZ-camera.lastTickPosZ)*partialTicks;
            boolean snow = "Snow".equals(settings.weather);
            int budget = Math.min(240, Math.max(0, AlienClient.get().performance().particleBudget()));
            int count = (int)(budget * settings.weatherIntensity);
            Random weatherRandom = new Random(91);
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_POINT_BIT | GL11.GL_LINE_BIT);
            GL11.glPushMatrix();
            try {
                GL11.glTranslated(-cx, -cy, -cz);
                GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_DEPTH_TEST); GL11.glDepthMask(false);
                GL11.glPointSize(3F); GL11.glLineWidth(1F);
                GL11.glBegin(snow ? GL11.GL_POINTS : GL11.GL_LINES);
                for (int i = 0; i < count; i++) {
                    double x = Math.floor(cx / 16D) * 16D + weatherRandom.nextDouble() * 36D - 10D;
                    double z = Math.floor(cz / 16D) * 16D + weatherRandom.nextDouble() * 36D - 10D;
                    double seed = weatherRandom.nextDouble() * 18D;
                    double speed = snow ? .035D : .32D;
                    double y = cy + 10D - ((seed + (ticks + partialTicks) * speed) % 18D);
                    // Stay above solid blocks, even when the player is below a roof.
                    net.minecraft.util.BlockPos column = new net.minecraft.util.BlockPos(x, 0, z);
                    if (y < clientWorld.getPrecipitationHeight(column).getY()) continue;
                    if (snow) { x += Math.sin(ticks * .03D + seed) * .5D; GL11.glColor4f(.9F, .94F, 1F, .65F); }
                    else GL11.glColor4f(.58F, .69F, .9F, .4F);
                    GL11.glVertex3d(x, y, z);
                    if (!snow) GL11.glVertex3d(x - .05D, y - ("Storm".equals(settings.weather) ? 1.6D : .9D), z);
                }
                GL11.glEnd();
            } finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
        }
    }

    private static final class Particle {
        double x, y, z, vx, vy, vz;
        int life;
        final int initialLife;
        final double rotation;
        Particle(double x, double y, double z, double vx, double vy, double vz, int life,double rotation) {
            this.x=x; this.y=y; this.z=z; this.vx=vx; this.vy=vy; this.vz=vz; this.life=life; initialLife=life;this.rotation=rotation;
        }
    }
}
