package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import dev.alien.client.cosmetic.mesh.*;
import dev.alien.client.cosmetic.neon.NeonPipeline;
import dev.alien.client.cosmetic.pet.*;
import dev.alien.client.emote.EmotePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.*;

/** Optional local body renderer. Vanilla entities, inventory, targeting and collision are untouched. */
public final class CharacterRenderer {
    private final EmotePose pose=new EmotePose();private final PetPose companionPose=new PetPose();private final PetDesign design=new PetDesign();
    private static EmotePose attachmentPose;
    static EmotePose attachmentPose(){return attachmentPose;}
    static void charmBend(net.minecraft.client.model.ModelBiped model,float scale){if(attachmentPose==null){dev.alien.client.emote.EmoteManager.handBend(model,scale);return;}GlStateManager.translate(0,4*scale,0);GlStateManager.rotate((float)Math.toDegrees(attachmentPose.bends[0]),1,0,0);GlStateManager.translate(0,-4*scale,0);}
    private int inFrame;private static long frames,posedFrames;private static String last="Vanilla";
    public static long frames(){return frames;}public static long posedFrames(){return posedFrames;}public static String lastModel(){return last;}
    @SubscribeEvent public void frame(TickEvent.RenderTickEvent e){if(e.phase==TickEvent.Phase.START)inFrame=0;}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void render(RenderPlayerEvent.Pre event){
        Minecraft m=Minecraft.getMinecraft();if(!(event.entityPlayer instanceof AbstractClientPlayer)||m.thePlayer==null||!AlienClient.get().config().cosmetics.enabled)return;
        AbstractClientPlayer player=(AbstractClientPlayer)event.entityPlayer;CosmeticConfig.Accessory a=CosmeticManager.appearanceFor(player);
        if(a==null||!a.enabled||a.character==null||!a.character.custom()||player.isSpectator()||player.isInvisible())return;
        boolean preview=CosmeticStudioScreen.drawingPreview||PetStudioScreen.drawingPreview||dev.alien.client.cosmetic.aura.AuraStudioScreen.drawingPreview;
        int budget=Math.max(0,AlienClient.get().performance().cosmeticBudget());if(!preview&&(inFrame>=Math.min(8,budget)||player.getDistanceSqToEntity(m.thePlayer)>Math.pow(AlienClient.get().performance().effectDistance(),2)))return;
        event.setCanceled(true);if(!preview)inFrame++;
        float partial=event.partialRenderTick,bodyYaw=player.prevRenderYawOffset+MathHelper.wrapAngleTo180_float(player.renderYawOffset-player.prevRenderYawOffset)*partial;
        boolean animate=a.character.animate&&AlienClient.get().config().animations;
        boolean emote=animate&&a.character.emotes&&AlienClient.get().emotes()!=null&&AlienClient.get().emotes().visualPose(player,partial,pose);if(!emote)walk(player,partial,animate,pose);
        int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),array=OpenGlHelper.vboSupported?GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING):0,element=OpenGlHelper.vboSupported?GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING):0;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);GlStateManager.pushMatrix();
        try{
            GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(true);GL11.glShadeModel(GL11.GL_SMOOTH);
            GlStateManager.translate(event.x,event.y-(player.isSneaking()?.125:0),event.z);GlStateManager.scale(a.character.size,a.character.size,a.character.size);
            if(emote){GlStateManager.translate(pose.sway,pose.lift,0);GlStateManager.rotate((float)Math.toDegrees(pose.yaw),0,1,0);}
            if(player.deathTime>0)GlStateManager.rotate(Math.min(1,(float)Math.sqrt(Math.max(0,(player.deathTime+partial-1)/20f*1.6f)))*90,0,0,1);
            GlStateManager.pushMatrix();try{
                GlStateManager.rotate(-bodyYaw,0,1,0);
                if("Kenny".equals(a.character.model))NeonPipeline.render(a.neon,()->kenny(player,bodyYaw,partial,pose));
                else{
                    boolean tung="Tung Tung Sahur".equals(a.character.model);design.model=tung?"Tung Tung Sahur":"Alien";design.preset="Original";design.mode="Follow";design.size=1;design.color=0x467876;design.accent=0x303030;
                    double movement=animate?Math.min(1,Math.hypot(player.posX-player.prevPosX,player.posZ-player.prevPosZ)*5):0;
                    companionPose.sample(animate?(player.ticksExisted+partial)*.05:0,(float)movement,0,emote?1:0,animate);
                    double scale=1.8/PetRenderer.modelHeight(tung);GlStateManager.scale(scale,scale,scale);GlStateManager.translate(0,-PetRenderer.modelFloor(tung),0);PetRenderer.draw(design,companionPose,a.neon);
                }
            }finally{GlStateManager.popMatrix();}
            // Attach the existing wardrobe using vanilla's known attachment skeleton.
            GlStateManager.pushMatrix();try{GlStateManager.rotate(180-bodyYaw,0,1,0);GlStateManager.scale(-.9375f,-.9375f,.9375f);GlStateManager.translate(0,-1.5078125,0);attachmentPose=emote?pose:null;CosmeticManager.characterAccessories(event.renderer,player,partial);}finally{attachmentPose=null;GlStateManager.popMatrix();}
            frames++;last=a.character.model;if(emote)posedFrames++;
        }finally{
            GlStateManager.popMatrix();GL11.glPopClientAttrib();if(OpenGlHelper.vboSupported){OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER,array);OpenGlHelper.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,element);}GL11.glPopAttrib();GlStateManager.bindTexture(texture);
        }
        if(!preview)event.renderer.renderName(player,event.x,event.y,event.z);
    }
    private static void walk(AbstractClientPlayer player,float partial,boolean animated,EmotePose p){
        p.clear();if(!animated)return;float amount=player.prevLimbSwingAmount+(player.limbSwingAmount-player.prevLimbSwingAmount)*partial,phase=player.limbSwing-player.limbSwingAmount*(1-partial);
        float step=(float)Math.cos(phase*.6662)*Math.min(1,amount)*1.1f;
        p.set(EmotePose.RIGHT_ARM,-step*.8,0,-.045);p.set(EmotePose.LEFT_ARM,step*.8,0,.045);p.set(EmotePose.RIGHT_LEG,step,0,0);p.set(EmotePose.LEFT_LEG,-step,0,0);
        p.bends[2]=Math.max(0,-step)*.35f;p.bends[3]=Math.max(0,step)*.35f;p.bends[0]=p.bends[1]=-.08f;
    }
    // Vanilla model coordinates flip Y/Z relative to this rig: preserve X, reverse Y/Z rotations.
    private static void rotate(EmotePose p,int part){int i=part*3;GlStateManager.rotate((float)Math.toDegrees(-p.angles[i+2]),0,0,1);GlStateManager.rotate((float)Math.toDegrees(-p.angles[i+1]),0,1,0);GlStateManager.rotate((float)Math.toDegrees(p.angles[i]),1,0,0);}
    private static void kenny(AbstractClientPlayer player,float yaw,float partial,EmotePose p){
        GlStateManager.pushMatrix();try{
            rotate(p,EmotePose.BODY);CosmeticMeshRenderer.draw(CharacterMeshes.BODY,0xED7B20,0xEBA75C);
            GlStateManager.pushMatrix();try{GlStateManager.translate(0,22/16f,0);rotate(p,EmotePose.HEAD);float head=player.prevRotationYawHead+MathHelper.wrapAngleTo180_float(player.rotationYawHead-player.prevRotationYawHead)*partial,pitch=player.prevRotationPitch+(player.rotationPitch-player.prevRotationPitch)*partial;GlStateManager.rotate(-MathHelper.wrapAngleTo180_float(head-yaw),0,1,0);GlStateManager.rotate(pitch,1,0,0);CosmeticMeshRenderer.draw(CharacterMeshes.HOOD,0xF08020,0xE8A257);CosmeticMeshRenderer.draw(CharacterMeshes.FACE,0x503024,0xF5CA9A);CosmeticMeshRenderer.draw(CharacterMeshes.EYES,0xF8FFFF,0x151921);}finally{GlStateManager.popMatrix();}
            for(int side=0;side<2;side++){
                GlStateManager.pushMatrix();try{GlStateManager.translate((side==0?-5.10:5.10)/16,13.6/16,0);rotate(p,side==0?EmotePose.RIGHT_ARM:EmotePose.LEFT_ARM);CosmeticMeshRenderer.draw(CharacterMeshes.UPPER,0xED7B20,0x99591B);GlStateManager.translate(0,-3.5/16,0);GlStateManager.rotate((float)Math.toDegrees(p.bends[side]),1,0,0);CosmeticMeshRenderer.draw(CharacterMeshes.LOWER,0xED7B20,0x99591B);CosmeticMeshRenderer.draw(CharacterMeshes.MITTEN,0xED7B20,0x986329);}finally{GlStateManager.popMatrix();}
                GlStateManager.pushMatrix();try{GlStateManager.translate((side==0?-2.55:2.55)/16,4.8/16,0);rotate(p,side==0?EmotePose.RIGHT_LEG:EmotePose.LEFT_LEG);CosmeticMeshRenderer.draw(CharacterMeshes.LEG,0xDA711B,0xD88D39);GlStateManager.translate(0,-.4/16,0);GlStateManager.rotate((float)Math.toDegrees(p.bends[side+2]),1,0,0);CosmeticMeshRenderer.draw(CharacterMeshes.BOOT,0x6B4329,0xAA794E);}finally{GlStateManager.popMatrix();}
            }
        }finally{GlStateManager.popMatrix();}
    }
}
