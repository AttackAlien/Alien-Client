package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import dev.alien.client.cosmetic.pet.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/** Cosmetic followers only; never creates entities, changes owner input, or sends packets. */
public final class PetManager {
    private final Map<UUID,Entry> entries=new LinkedHashMap<UUID,Entry>();private Object world,profile;private final Entry[] ordered=new Entry[4];
    private static int active,drawn,previewDrawn;private static float movement,sleeping,dancing;private static double selfX,selfY,selfZ;
    private final PetPose previewPose=new PetPose();private static final PetPose studioPose=new PetPose();
    public static int activeCount(){return active;}public static int drawnCount(){return drawn;}public static int previewCount(){return previewDrawn;}
    public static float selfMovement(){return movement;}public static float selfSleep(){return sleeping;}public static float selfDance(){return dancing;}
    public static int sourceModelVertices(){return PetRenderer.vertices();}public static int sourceModelTriangles(){return PetRenderer.triangles();}public static int sourceModelClips(){return PetRenderer.clips();}
    public static int tungModelVertices(){return PetRenderer.tungVertices();}public static int tungModelTriangles(){return PetRenderer.tungTriangles();}public static String lastRenderedModel(){return PetRenderer.lastModel();}
    public static double selfX(){return selfX;}public static double selfY(){return selfY;}public static double selfZ(){return selfZ;}
    public void beginFrame(){drawn=previewDrawn=0;}
    private void clear(){entries.clear();Arrays.fill(ordered,null);active=drawn=previewDrawn=0;movement=sleeping=dancing=0;selfX=selfY=selfZ=0;}
    public void tick(Minecraft m,CosmeticConfig c){
        if(world!=m.theWorld||profile!=c){clear();world=m.theWorld;profile=c;}
        int budget=Math.min(4,Math.max(0,AlienClient.get().performance().cosmeticBudget()));
        if(m.thePlayer==null||m.theWorld==null||!c.enabled||budget==0){clear();return;}if(m.isGamePaused())return;
        for(Entry e:entries.values())e.present=false;float distance=Math.min(c.maxDistance,AlienClient.get().performance().effectDistance());
        for(int i=-1;i<m.theWorld.playerEntities.size();i++){
            EntityPlayer p=i<0?m.thePlayer:m.theWorld.playerEntities.get(i);if(i>=0&&p==m.thePlayer)continue;
            CosmeticConfig.Accessory a=p==m.thePlayer?c.self:c.players.get(p.getUniqueID().toString());
            if(a==null||!a.enabled||!a.pet.enabled||p.isDead||p.isSpectator()||p.isInvisibleToPlayer(m.thePlayer)||p.getDistanceSqToEntity(m.thePlayer)>distance*distance)continue;
            Entry e=entries.get(p.getUniqueID());if(e==null){if(entries.size()>=budget){if(p!=m.thePlayer)continue;Iterator<UUID> keys=entries.keySet().iterator();UUID last=null;while(keys.hasNext())last=keys.next();entries.remove(last);}e=new Entry();entries.put(p.getUniqueID(),e);}
            if(e.accessory!=a){e.motion=new PetMotion();e.accessory=a;}e.present=true;e.owner=p;
            e.motion.update(a.pet,.05,p.posX,p.posY,p.posZ,p.renderYawOffset,Math.hypot(p.posX-p.prevPosX,p.posZ-p.prevPosZ)*20,p==m.thePlayer&&AlienClient.get().emotes().active(),animated(a));
        }
        Iterator<Entry> it=entries.values().iterator();while(it.hasNext())if(!it.next().present)it.remove();
        while(entries.size()>budget){Iterator<UUID> keys=entries.keySet().iterator();UUID last=null;while(keys.hasNext()){UUID key=keys.next();if(!key.equals(m.thePlayer.getUniqueID()))last=key;}if(last==null)break;entries.remove(last);}
        active=entries.size();Entry e=entries.get(m.thePlayer.getUniqueID());movement=e==null?0:e.motion.movement(1);sleeping=e==null?0:e.motion.sleeping(1);dancing=e==null?0:e.motion.dancing(1);
        Arrays.fill(ordered,null);int index=0;if(e!=null)ordered[index++]=e;for(Entry entry:entries.values())if(entry!=e)ordered[index++]=entry;
        selfX=e==null?0:e.motion.x(1);selfY=e==null?0:e.motion.y(1);selfZ=e==null?0:e.motion.z(1);
    }
    private boolean animated(CosmeticConfig.Accessory a){return a.pet.animate&&a.animations&&AlienClient.get().config().animations;}
    public void render(RenderWorldLastEvent event){
        Minecraft m=Minecraft.getMinecraft();Entity camera=m.getRenderViewEntity();drawn=0;if(camera==null||m.theWorld!=world||entries.isEmpty()||!AlienClient.get().config().cosmetics.enabled)return;
        float partial=m.isGamePaused()?1:event.partialTicks;double cx=camera.lastTickPosX+(camera.posX-camera.lastTickPosX)*partial,cy=camera.lastTickPosY+(camera.posY-camera.lastTickPosY)*partial,cz=camera.lastTickPosZ+(camera.posZ-camera.lastTickPosZ)*partial;
        int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try{setup();GL11.glTranslated(-cx,-cy,-cz);int left=Math.min(4,Math.max(0,AlienClient.get().performance().cosmeticBudget()));
            for(int i=0;i<active;i++){Entry e=ordered[i];if(left--<=0)break;if(!e.accessory.enabled||!e.accessory.pet.enabled||e.owner.isInvisibleToPlayer(m.thePlayer)||e.owner.isSpectator())continue;
                e.motion.pose(partial,animated(e.accessory),e.pose);GL11.glPushMatrix();try{GL11.glTranslated(e.motion.x(partial),e.motion.y(partial),e.motion.z(partial));GL11.glRotated(-e.motion.yaw(partial),0,1,0);PetRenderer.draw(e.accessory.pet,e.pose,e.accessory.neon);drawn++;}finally{GL11.glPopMatrix();}}
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();GlStateManager.bindTexture(texture);}
    }
    public void previewLayer(EntityPlayer p,CosmeticConfig.Accessory a,float partial){if(!a.pet.enabled)return;boolean animation=animated(a);Minecraft m=Minecraft.getMinecraft();
        double time=animation?(p.ticksExisted+partial)*.05*a.pet.speed:0;
        // The real owner's events feed this fitting-room view; no artificial movement/emote trigger.
        previewPose.sample(time,Math.min(1,(float)Math.hypot(p.posX-p.prevPosX,p.posZ-p.prevPosZ)*5),sleeping,p==m.thePlayer?dancing:0,animation);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();try{setup();GL11.glTranslated(0,1.5,0);GL11.glScaled(1,-1,1);double radius="Shoulder".equals(a.pet.mode)?.42:a.pet.distance,angle="Orbit".equals(a.pet.mode)?time*.65:0;GL11.glTranslated((a.pet.right?-1:1)*Math.cos(angle)*radius,"Shoulder".equals(a.pet.mode)?1.6:a.pet.height,Math.sin(angle)*radius);GL11.glRotated(180,0,1,0);PetRenderer.draw(a.pet,previewPose,a.neon);previewDrawn++;}finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
    public static void renderStudio(CosmeticConfig.Accessory a,float partial){Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||!AlienClient.get().config().cosmetics.enabled||!a.enabled||!a.pet.enabled)return;
        boolean animation=a.pet.animate&&a.animations&&AlienClient.get().config().animations;studioPose.sample((m.thePlayer.ticksExisted+partial)*.05*a.pet.speed,movement,sleeping,dancing,animation);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{setup();PetRenderer.draw(a.pet,studioPose,a.neon);previewDrawn++;}finally{GL11.glPopAttrib();}
    }
    private static void setup(){GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(true);GL11.glDisable(GL11.GL_BLEND);GL11.glShadeModel(GL11.GL_SMOOTH);}
    private static final class Entry{PetMotion motion=new PetMotion();final PetPose pose=new PetPose();CosmeticConfig.Accessory accessory;EntityPlayer owner;boolean present;}
}
