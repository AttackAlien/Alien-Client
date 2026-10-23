package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import dev.alien.client.cosmetic.mesh.CapeCloth;
import dev.alien.client.cosmetic.mesh.CosmeticMeshes;
import org.lwjgl.opengl.GL11;

/** Original procedural accessories rendered only by the local client. */
public final class CosmeticManager {
    private boolean installed;
    private int rendered;
    private int tick;

    public void install() {
        if (installed) return;
        Minecraft mc = Minecraft.getMinecraft();
        for (RenderPlayer renderer : mc.getRenderManager().getSkinMap().values()) renderer.addLayer(new AccessoryLayer(renderer));
        installed = true;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) rendered = 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || mc.isGamePaused()) return;
        tick++;
        if ((tick & 3) != 0 || !config().enabled) return;
        int emitted = 0;
        int budget = Math.min(32, Math.min(Math.max(0, AlienClient.get().performance().cosmeticBudget()),
                AlienClient.get().performance().particleBudget()));
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (emitted >= budget) break;
            CosmeticConfig.Accessory accessory = forPlayer(player);
            if (accessory == null || !accessory.enabled || player.isInvisibleToPlayer(mc.thePlayer) || !inRange(player)) continue;
            boolean moving = player.motionX * player.motionX + player.motionZ * player.motionZ > .0025;
            if (accessory.aura || (accessory.trail && moving)) {
                double angle = tick * .09 + emitted;
                double radius = accessory.aura ? .5 : .15;
                double y = accessory.aura ? player.posY + .3 + .4 * Math.sin(angle) : player.posY + .1;
                int color = accessory.color;
                mc.theWorld.spawnParticle(EnumParticleTypes.REDSTONE,
                        player.posX + Math.cos(angle) * radius, y, player.posZ + Math.sin(angle) * radius,
                        Math.max(.001, ((color >> 16) & 255) / 255d), ((color >> 8) & 255) / 255d, (color & 255) / 255d);
                emitted++;
            }
        }
    }

    private CosmeticConfig config() { return AlienClient.get().config().cosmetics; }

    private CosmeticConfig.Accessory forPlayer(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && player.getUniqueID().equals(mc.thePlayer.getUniqueID())) return config().self;
        return config().players.get(player.getUniqueID().toString());
    }

    private boolean inRange(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        float distance = Math.min(config().maxDistance, AlienClient.get().performance().effectDistance());
        return mc.thePlayer != null && player.getDistanceSqToEntity(mc.thePlayer) <= distance * distance;
    }

    private final class AccessoryLayer implements LayerRenderer<AbstractClientPlayer> {
        private final RenderPlayer renderer;
        private final CapeCloth lowCape = new CapeCloth(8, 4);
        private final CapeCloth balancedCape = new CapeCloth(14, 6);
        private final CapeCloth highCape = new CapeCloth(24, 12);

        AccessoryLayer(RenderPlayer renderer) { this.renderer = renderer; }

        @Override public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbAmount,
                float partialTicks, float age, float yaw, float pitch, float scale) {
            Minecraft mc = Minecraft.getMinecraft();
            boolean preview = CosmeticStudioScreen.drawingPreview;
            if (!config().enabled || mc.thePlayer == null || player.isSpectator()
                    || player.isInvisibleToPlayer(mc.thePlayer) || (!preview && !inRange(player))) return;
            CosmeticConfig.Accessory a = forPlayer(player);
            if (a == null || !a.enabled || (!preview && rendered >= AlienClient.get().performance().cosmeticBudget())) return;
            if (!preview) rendered++;
            boolean animated = a.animations && AlienClient.get().config().animations;
            float phase = animated ? player.ticksExisted + partialTicks : 0;
            float movement = animated ? (float)Math.min(2, Math.hypot(player.posX-player.prevPosX,player.posZ-player.prevPosZ)*10) : 0;
            int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            // Use raw attribute changes here: push/pop also restores the states
            // tracked by GlStateManager without leaving its caches out of sync.
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_LIGHTING_BIT
                    | GL11.GL_TEXTURE_BIT | GL11.GL_POLYGON_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GlStateManager.pushMatrix();
            try {
                GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_CULL_FACE); GL11.glShadeModel(GL11.GL_SMOOTH);
                GL11.glEnable(GL11.GL_DEPTH_TEST); GL11.glDepthMask(true);
                if (player.isSneaking()) GlStateManager.translate(0, .2f, 0);
                drawHead(player, a);
                GlStateManager.pushMatrix();
                try {
                    renderer.getMainModel().bipedBody.postRender(.0625f);
                    transform(a);
                    if ("CAPE".equals(a.back)) {
                        float limit = AlienClient.get().performance().effectDistance();
                        boolean distant = !preview && player.getDistanceSqToEntity(mc.thePlayer) > (limit * .55f) * (limit * .55f);
                        drawCape(a, phase, movement, animated, distant);
                    }
                    else if ("WINGS".equals(a.back)) {
                        float flap = animated ? 11f + (float)Math.sin(phase*.11f)*13f + movement*4 : 12;
                        GlStateManager.pushMatrix();
                        try { GlStateManager.rotate(flap,0,1,0); CosmeticMeshRenderer.draw(CosmeticMeshes.back("WINGS",true),a.color,a.secondaryColor); }
                        finally { GlStateManager.popMatrix(); }
                        GlStateManager.pushMatrix();
                        try { GlStateManager.rotate(-flap,0,1,0); CosmeticMeshRenderer.draw(CosmeticMeshes.back("WINGS",false),a.color,a.secondaryColor); }
                        finally { GlStateManager.popMatrix(); }
                    } else CosmeticMeshRenderer.draw(CosmeticMeshes.back(a.back,true),a.color,a.secondaryColor);
                } finally { GlStateManager.popMatrix(); }
                if (a.charm) {
                    GlStateManager.pushMatrix();
                    try {
                        renderer.getMainModel().bipedRightArm.postRender(.0625f); transform(a);
                        if (animated) GlStateManager.rotate((float)Math.sin(phase*.08f)*9,0,0,1);
                        CosmeticMeshRenderer.draw(CosmeticMeshes.charm(),a.secondaryColor,a.color);
                    } finally { GlStateManager.popMatrix(); }
                }
            } finally {
                GlStateManager.popMatrix(); GL11.glPopAttrib();
                // Binding dynamic/imported textures changes MC's binding cache;
                // restore it to the actual texture reinstated by glPopAttrib.
                GlStateManager.bindTexture(previousTexture);
            }
        }

        private void drawHead(AbstractClientPlayer player, CosmeticConfig.Accessory a) {
            GlStateManager.pushMatrix();
            try {
                renderer.getMainModel().bipedHead.postRender(.0625f); transform(a);
                GlStateManager.pushMatrix();
                try { GlStateManager.scale(a.hatSize,a.hatSize,a.hatSize); CosmeticMeshRenderer.draw(CosmeticMeshes.hat(a.hat),a.color,a.secondaryColor); }
                finally { GlStateManager.popMatrix(); }
                CosmeticMeshRenderer.draw(CosmeticMeshes.face(a.face),a.color,a.secondaryColor);
                CosmeticAssets.Imported imported = CosmeticAssets.get(a.modelFile,a.textureFile);
                if (imported != null) {
                    if (imported.texture != null) {
                        GL11.glEnable(GL11.GL_TEXTURE_2D); Minecraft.getMinecraft().getTextureManager().bindTexture(imported.texture);
                        GL11.glColor4f(1,1,1,1);
                    } else GL11.glColor4f(((a.color>>16)&255)/255f,((a.color>>8)&255)/255f,(a.color&255)/255f,1);
                    imported.model.render(.0625f); GL11.glDisable(GL11.GL_TEXTURE_2D);
                }
            } finally { GlStateManager.popMatrix(); }
        }

        private void drawCape(CosmeticConfig.Accessory a, float phase, float movement, boolean animated, boolean distant) {
            CapeCloth cloth = "Low".equals(a.detailLevel) ? lowCape : "High".equals(a.detailLevel) ? highCape : balancedCape;
            if ("Competitive".equals(AlienClient.get().config().performance.preset) || distant || AlienClient.get().performance().qualityScale() < .65f) cloth=lowCape;
            cloth.update(phase,movement,animated ? a.capeFlutter : 0);
            GlStateManager.pushMatrix();
            try {
                GlStateManager.scale(1,a.capeLength,1);
                GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_CULL_FACE);
                Minecraft.getMinecraft().getTextureManager().bindTexture(CapeTextures.get(a));
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
                GL11.glBegin(GL11.GL_TRIANGLES);
                for(int i=0;i<cloth.vertexCount();i++) {
                    float shade=Math.min(1,cloth.shade(i)+.13f);
                    GL11.glColor4f(shade,shade,shade,1);
                    GL11.glNormal3f(cloth.nx(i),cloth.ny(i),cloth.nz(i));
                    GL11.glTexCoord2f(cloth.u(i),cloth.v(i));
                    GL11.glVertex3f(cloth.x(i)/16f,cloth.y(i)/16f,cloth.z(i)/16f);
                }
                GL11.glEnd();
            } finally {
                GL11.glDisable(GL11.GL_CULL_FACE); GL11.glDisable(GL11.GL_TEXTURE_2D); GlStateManager.popMatrix();
            }
        }

        private void transform(CosmeticConfig.Accessory a) {
            GlStateManager.translate(a.offsetX/16f,a.offsetY/16f,a.offsetZ/16f);
            GlStateManager.rotate(a.rotationX,1,0,0); GlStateManager.rotate(a.rotationY,0,1,0); GlStateManager.rotate(a.rotationZ,0,0,1);
            GlStateManager.scale(a.scale,a.scale,a.scale);
        }
        @Override public boolean shouldCombineTextures() { return false; }
    }
}
