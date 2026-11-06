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
import dev.alien.client.cosmetic.mesh.CapeDynamics;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.lwjgl.opengl.GL11;

/** Original procedural accessories rendered only by the local client. */
public final class CosmeticManager {
    private static CosmeticManager instance;
    private final Map<RenderPlayer,AccessoryLayer> attached=new java.util.IdentityHashMap<RenderPlayer,AccessoryLayer>();
    public CosmeticManager(){instance=this;}
    private boolean installed;
    private int rendered;
    private int tick;
    private final RibbonTrails ribbons=new RibbonTrails();
    private final PetManager pets=new PetManager();
    private final dev.alien.client.cosmetic.aura.ReactiveAuras auras=new dev.alien.client.cosmetic.aura.ReactiveAuras();
    private static long legacyAuraEmissions;
    public static long legacyAuraEmissions(){return legacyAuraEmissions;}
    private static float lastCapeBend;
    public static float lastCapeBend(){return lastCapeBend;}
    public static int activeRibbonCount(){return RibbonTrails.activeCount();}
    public static int drawnRibbonSegments(){return RibbonTrails.drawnSegments();}
    public static int texturedRibbonSegments(){return RibbonTrails.texturedSegments();}
    public static String lastTrailTextureName(){return RibbonTrails.lastTexture();}
    @SubscribeEvent public void renderRibbons(RenderWorldLastEvent event){ribbons.render(event);}
    @SubscribeEvent public void renderAuras(RenderWorldLastEvent event){auras.render(event);}
    @SubscribeEvent public void renderPets(RenderWorldLastEvent event){pets.render(event);}

    public void attach(RenderPlayer renderer){AccessoryLayer layer=new AccessoryLayer(renderer);attached.put(renderer,layer);renderer.addLayer(layer);}

    static CosmeticConfig.Accessory appearanceFor(EntityPlayer p){
        if(dev.alien.client.cosmetic.pet.PetStudioScreen.drawingPreview)return dev.alien.client.cosmetic.pet.PetStudioScreen.previewAccessory;
        if(dev.alien.client.cosmetic.aura.AuraStudioScreen.drawingPreview)return dev.alien.client.cosmetic.aura.AuraStudioScreen.previewAccessory;
        return instance==null?null:instance.forPlayer(p);
    }
    static void characterAccessories(RenderPlayer renderer,AbstractClientPlayer p,float partial){
        if(instance==null)return;AccessoryLayer layer=instance.attached.get(renderer);if(layer==null)return;
        float amount=p.prevLimbSwingAmount+(p.limbSwingAmount-p.prevLimbSwingAmount)*partial,phase=p.limbSwing-p.limbSwingAmount*(1-partial);
        net.minecraft.client.model.ModelPlayer model=renderer.getMainModel();model.isSneak=p.isSneaking();model.isRiding=p.isRiding();model.isChild=p.isChild();
        float bodyYaw=p.prevRenderYawOffset+net.minecraft.util.MathHelper.wrapAngleTo180_float(p.renderYawOffset-p.prevRenderYawOffset)*partial,headYaw=p.prevRotationYawHead+net.minecraft.util.MathHelper.wrapAngleTo180_float(p.rotationYawHead-p.prevRotationYawHead)*partial,headPitch=p.prevRotationPitch+(p.rotationPitch-p.prevRotationPitch)*partial;
        model.setLivingAnimations(p,phase,amount,partial);model.setRotationAngles(phase,amount,p.ticksExisted+partial,net.minecraft.util.MathHelper.wrapAngleTo180_float(headYaw-bodyYaw),headPitch,.0625f,p);
        dev.alien.client.emote.EmotePose pose=CharacterRenderer.attachmentPose();
        net.minecraft.client.model.ModelRenderer[] parts=layer.characterParts;parts[0]=model.bipedHead;parts[1]=model.bipedBody;parts[2]=model.bipedRightArm;parts[3]=model.bipedLeftArm;parts[4]=model.bipedRightLeg;parts[5]=model.bipedLeftLeg;
        float[] old=layer.characterSnapshot;for(int i=0;i<parts.length;i++){net.minecraft.client.model.ModelRenderer part=parts[i];int at=i*6;old[at]=part.rotateAngleX;old[at+1]=part.rotateAngleY;old[at+2]=part.rotateAngleZ;old[at+3]=part.rotationPointX;old[at+4]=part.rotationPointY;old[at+5]=part.rotationPointZ;if(pose!=null){part.rotateAngleX+=pose.angles[i*3];part.rotateAngleY+=pose.angles[i*3+1];part.rotateAngleZ+=pose.angles[i*3+2];}}
        CosmeticConfig.Accessory look=appearanceFor(p);boolean kenny=look!=null&&"Kenny".equals(look.character.model);
        if(kenny){model.bipedHead.rotationPointY+=.67f;model.bipedBody.rotationPointY+=7.2f;model.bipedRightArm.rotationPointY+=7.63f;model.bipedLeftArm.rotationPointY+=7.63f;model.bipedRightArm.rotationPointX-=.44f;model.bipedLeftArm.rotationPointX+=.44f;}
        try{layer.doRenderLayer(p,phase,amount,partial,p.ticksExisted+partial,0,p.rotationPitch,.0625f);}
        finally{for(int i=0;i<parts.length;i++){net.minecraft.client.model.ModelRenderer part=parts[i];int at=i*6;part.rotateAngleX=old[at];part.rotateAngleY=old[at+1];part.rotateAngleZ=old[at+2];part.rotationPointX=old[at+3];part.rotationPointY=old[at+4];part.rotationPointZ=old[at+5];}}
    }
    public void install() {
        if (installed) return;
        Minecraft mc = Minecraft.getMinecraft();
        for (RenderPlayer renderer : mc.getRenderManager().getSkinMap().values()) attach(renderer);
        installed = true;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {rendered = 0;auras.beginFrame();pets.beginFrame();}
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        ribbons.tick(mc,config());
        auras.tick(mc,config());pets.tick(mc,config());
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
            boolean legacyAura=accessory.aura&&!accessory.auraDesign.enabled;
            if (legacyAura || (accessory.trail && "Particles".equals(accessory.trailStyle) && moving)) {
                double angle = tick * .09 + emitted;
                double radius = legacyAura ? .5 : .15;
                double y = legacyAura ? player.posY + .3 + .4 * Math.sin(angle) : player.posY + .1;
                int color = accessory.color;
                mc.theWorld.spawnParticle(EnumParticleTypes.REDSTONE,
                        player.posX + Math.cos(angle) * radius, y, player.posZ + Math.sin(angle) * radius,
                        Math.max(.001, ((color >> 16) & 255) / 255d), ((color >> 8) & 255) / 255d, (color & 255) / 255d);
                emitted++;
                if(legacyAura)legacyAuraEmissions++;
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
        private final float[] characterSnapshot=new float[36];private final net.minecraft.client.model.ModelRenderer[] characterParts=new net.minecraft.client.model.ModelRenderer[6];
        private final CapeCloth lowCape = new CapeCloth(8, 4);
        private final CapeCloth balancedCape = new CapeCloth(20, 10);
        private final CapeCloth highCape = new CapeCloth(32, 16);
        private final Map<UUID,CapeDynamics> dynamics=new LinkedHashMap<UUID,CapeDynamics>();
        private final CapeDynamics previewDynamics=new CapeDynamics();
        private Object capeWorld;

        AccessoryLayer(RenderPlayer renderer) { this.renderer = renderer; }

        @Override public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbAmount,
                float partialTicks, float age, float yaw, float pitch, float scale) {
            Minecraft mc = Minecraft.getMinecraft();
            boolean preview = dev.alien.client.cosmetic.pet.PetStudioScreen.drawingPreview||CosmeticStudioScreen.drawingPreview||dev.alien.client.cosmetic.aura.AuraStudioScreen.drawingPreview;
            if (!config().enabled || mc.thePlayer == null || player.isSpectator()
                    || player.isInvisibleToPlayer(mc.thePlayer) || (!preview && !inRange(player))) return;
            CosmeticConfig.Accessory a = dev.alien.client.cosmetic.pet.PetStudioScreen.drawingPreview?dev.alien.client.cosmetic.pet.PetStudioScreen.previewAccessory:dev.alien.client.cosmetic.aura.AuraStudioScreen.drawingPreview?dev.alien.client.cosmetic.aura.AuraStudioScreen.previewAccessory:forPlayer(player);
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
                dev.alien.client.cosmetic.neon.NeonPipeline.render(a.neon,()->drawAppearance(player,a,phase,movement,animated,preview,partialTicks));
                if(preview){auras.previewLayer(player,a);pets.previewLayer(player,a,partialTicks);}
            } finally {
                GlStateManager.popMatrix(); GL11.glPopAttrib();
                // Binding dynamic/imported textures changes MC's binding cache;
                // restore it to the actual texture reinstated by glPopAttrib.
                GlStateManager.bindTexture(previousTexture);
            }
        }

        private void drawAppearance(AbstractClientPlayer player,CosmeticConfig.Accessory a,float phase,float movement,boolean animated,boolean preview,float partialTicks){
            Minecraft mc=Minecraft.getMinecraft();
                drawHead(player, a);
                GlStateManager.pushMatrix();
                try {
                    renderer.getMainModel().bipedBody.postRender(.0625f);
                    transform(a);
                    if ("CAPE".equals(a.back)) {
                        float limit = AlienClient.get().performance().effectDistance();
                        boolean distant = !preview && player.getDistanceSqToEntity(mc.thePlayer) > (limit * .55f) * (limit * .55f);
                        if(capeWorld!=mc.theWorld){dynamics.clear();capeWorld=mc.theWorld;}
                        CapeDynamics response=preview?previewDynamics:dynamics.get(player.getUniqueID());
                        if(response==null){if(dynamics.size()>=64)dynamics.remove(dynamics.keySet().iterator().next());response=new CapeDynamics();dynamics.put(player.getUniqueID(),response);}
                        if(animated&&preview)response.update(phase,0,0,0,0);
                        else if(animated){
                            double bodyYaw=Math.toRadians(player.renderYawOffset),vx=(player.posX-player.prevPosX)*20,vz=(player.posZ-player.prevPosZ)*20;
                            response.update(phase,(float)Math.hypot(vx,vz),(float)(vx*Math.cos(bodyYaw)+vz*Math.sin(bodyYaw)),
                                    (float)((player.posY-player.prevPosY)*20),net.minecraft.util.MathHelper.wrapAngleTo180_float(player.renderYawOffset-player.prevRenderYawOffset));
                        }else response.reset();
                        if(!preview&&player==mc.thePlayer)lastCapeBend=response.bend();
                        drawCape(a, phase, movement, animated, distant,response);
                    }
                    else if("DRAGON_WINGS".equals(a.back)){
                        float flap=animated?(float)(Math.sin(phase*.065)*13+Math.sin(phase*.13-.5)*2)+movement*3:0;
                        for(int side=-1;side<=1;side+=2){GlStateManager.pushMatrix();try{
                            GlStateManager.rotate(side*(12+flap),0,1,0);CosmeticMeshRenderer.draw(dev.alien.client.cosmetic.mesh.NeonMeshes.dragon(side==1,false),a.color,a.secondaryColor);
                            GlStateManager.translate(side*7.5f/16,-2.8f/16,3.8f/16);GlStateManager.rotate(side*(animated?(float)(Math.sin(phase*.065-.45)*9):0),0,1,0);GlStateManager.translate(-side*7.5f/16,2.8f/16,-3.8f/16);
                            CosmeticMeshRenderer.draw(dev.alien.client.cosmetic.mesh.NeonMeshes.dragon(side==1,true),a.color,a.secondaryColor);
                        }finally{GlStateManager.popMatrix();}}
                    }
                    else if ("WINGS".equals(a.back)) {
                        float flap = animated ? 12f + (float)(Math.sin(phase*.085f)+.14*Math.sin(phase*.17f-.6))*11f + movement*3 : 12;
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
                        renderer.getMainModel().bipedRightArm.postRender(.0625f); CharacterRenderer.charmBend(renderer.getMainModel(),.0625f); transform(a);
                        if("Kenny".equals(a.character.model))GlStateManager.translate(0,-.25f,0);
                        if (animated) GlStateManager.rotate((float)(Math.sin(phase*.065f)*5+Math.sin(phase*.10f-.6)*1.5+Math.sin(phase*.035f)*movement*2),0,0,1);
                        CosmeticMeshRenderer.draw(CosmeticMeshes.charm(),a.secondaryColor,a.color);
                    } finally { GlStateManager.popMatrix(); }
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
                        dev.alien.client.cosmetic.neon.NeonPipeline.texture(true);
                        GL11.glEnable(GL11.GL_TEXTURE_2D); Minecraft.getMinecraft().getTextureManager().bindTexture(imported.texture);
                        GL11.glColor4f(1,1,1,1);
                    } else {dev.alien.client.cosmetic.neon.NeonPipeline.texture(false);GL11.glColor4f(((a.color>>16)&255)/255f,((a.color>>8)&255)/255f,(a.color&255)/255f,1);}
                    imported.model.render(.0625f); GL11.glDisable(GL11.GL_TEXTURE_2D);
                }
            } finally { GlStateManager.popMatrix(); }
        }

        private void drawCape(CosmeticConfig.Accessory a, float phase, float movement, boolean animated, boolean distant,CapeDynamics response) {
            dev.alien.client.cosmetic.neon.NeonPipeline.texture(true);
            CapeCloth cloth = "Low".equals(a.detailLevel) ? lowCape : "High".equals(a.detailLevel) ? highCape : balancedCape;
            if ("Competitive".equals(AlienClient.get().config().performance.preset) || distant || AlienClient.get().performance().qualityScale() < .65f) cloth=lowCape;
            cloth.update(phase,movement,animated ? a.capeFlutter : 0,response.bend(),response.sway());
            GlStateManager.pushMatrix();
            try {
                GlStateManager.scale(1,a.capeLength,1);
                GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_CULL_FACE);
                Minecraft.getMinecraft().getTextureManager().bindTexture(CapeTextures.get(a));
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
                GL11.glBegin(GL11.GL_TRIANGLES);
                for(int i=0;i<cloth.vertexCount();i++) {
                    float shine=a.capeShimmer&&animated?(float)Math.pow(Math.max(0,Math.sin(cloth.u(i)*5+cloth.v(i)*7-phase*.08)),12)*.23f:0;
                    float shade=Math.min(1.18f,cloth.shade(i)+.13f+shine);
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
