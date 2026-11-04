package dev.alien.client.cosmetic.aura;

import dev.alien.client.AlienClient;
import dev.alien.client.cosmetic.CosmeticConfig;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/** Bounded local aura states fed by real movement, actual emotes and the displayed world hour. */
public final class ReactiveAuras {
    private final Map<UUID,Entry> entries=new LinkedHashMap<UUID,Entry>();
    private final Entry[] ordered=new Entry[8];
    private final AuraResponse preview=new AuraResponse();private Object world,profile,previewDesign;private int previewTick=-1;
    private static int active,drawn,previewDrawn;private static float movement,emote,night,energy;
    private static final AuraGeometry.Sink GL_SINK=(x,y,z,color,alpha)->{GL11.glColor4f((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,alpha);GL11.glVertex3d(x,y,z);};
    public static int activeCount(){return active;}public static int drawnUnits(){return drawn;}public static int previewUnits(){return previewDrawn;}
    public static float selfMovement(){return movement;}public static float selfEmote(){return emote;}public static float selfNight(){return night;}public static float selfEnergy(){return energy;}
    public void beginFrame(){drawn=previewDrawn=0;}
    private void clear(){entries.clear();Arrays.fill(ordered,null);preview.clear();previewTick=-1;active=drawn=previewDrawn=0;movement=emote=night=energy=0;}
    public void tick(Minecraft m,CosmeticConfig c){
        if(world!=m.theWorld||profile!=c){clear();world=m.theWorld;profile=c;}
        if(m.theWorld==null||m.thePlayer==null||!c.enabled||!AlienClient.get().config().animations){clear();return;}
        int allowed=Math.min(8,Math.max(0,AlienClient.get().performance().cosmeticBudget()));
        if(allowed==0||AlienClient.get().performance().particleBudget()<=0){clear();return;}
        if(m.isGamePaused())return;
        for(Entry e:entries.values())e.present=false;
        float distance=Math.min(c.maxDistance,AlienClient.get().performance().effectDistance());
        // The owner's aura gets the first budget slot, even in a crowded player list.
        for(int i=-1;i<m.theWorld.playerEntities.size();i++){
            EntityPlayer p=i<0?m.thePlayer:m.theWorld.playerEntities.get(i);if(i>=0&&p==m.thePlayer)continue;
            CosmeticConfig.Accessory a=p==m.thePlayer?c.self:c.players.get(p.getUniqueID().toString());
            if(a==null||!a.enabled||!a.aura||!a.auraDesign.enabled||!a.animations||p.isDead||p.isSpectator()||p.isInvisibleToPlayer(m.thePlayer)||p.getDistanceSqToEntity(m.thePlayer)>distance*distance)continue;
            Entry e=entries.get(p.getUniqueID());if(e==null){if(entries.size()>=allowed){if(p!=m.thePlayer)continue;Iterator<UUID> keys=entries.keySet().iterator();UUID remove=null;while(keys.hasNext())remove=keys.next();entries.remove(remove);}e=new Entry();entries.put(p.getUniqueID(),e);}
            if(e.a!=a)e.state.clear();e.a=a;e.player=p;e.present=true;
            double speed=Math.hypot(p.posX-p.prevPosX,p.posZ-p.prevPosZ)*20;
            e.state.update(a.auraDesign,.05,speed,p==m.thePlayer&&AlienClient.get().emotes().active(),hour(m));
        }
        Iterator<Entry> it=entries.values().iterator();while(it.hasNext())if(!it.next().present)it.remove();
        // A lower adaptive limit also evicts existing states immediately.
        while(entries.size()>allowed){Iterator<UUID> keys=entries.keySet().iterator();UUID last=null;while(keys.hasNext())last=keys.next();entries.remove(last);}
        active=entries.size();Entry self=entries.get(m.thePlayer.getUniqueID());
        Arrays.fill(ordered,null);int index=0;if(self!=null)ordered[index++]=self;for(Entry entry:entries.values())if(entry!=self)ordered[index++]=entry;
        movement=self==null?0:self.state.movement(1);emote=self==null?0:self.state.dance(1);night=self==null?0:self.state.night(1);energy=self==null?0:self.state.energy(self.a.auraDesign,1);
    }
    private double hour(Minecraft m){dev.alien.client.world.WorldConfig s=AlienClient.get().config().world;return s.enabled&&s.timeOverride?s.visualHour:((m.theWorld.getWorldTime()%24000L+24000L)%24000L/1000d+6)%24;}
    public void render(RenderWorldLastEvent event){
        Minecraft m=Minecraft.getMinecraft();Entity camera=m.getRenderViewEntity();drawn=0;
        if(camera==null||m.theWorld!=world||entries.isEmpty())return;
        float partial=m.isGamePaused()?0:event.partialTicks;
        double cx=camera.lastTickPosX+(camera.posX-camera.lastTickPosX)*partial,cy=camera.lastTickPosY+(camera.posY-camera.lastTickPosY)*partial,cz=camera.lastTickPosZ+(camera.posZ-camera.lastTickPosZ)*partial;
        int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try{setup();GL11.glTranslated(-cx,-cy,-cz);
            for(int pass=glow()?0:1;pass<2;pass++){
                int left=Math.min(192,Math.max(0,AlienClient.get().performance().particleBudget()));GL11.glBlendFunc(GL11.GL_SRC_ALPHA,pass==0?GL11.GL_ONE:GL11.GL_ONE_MINUS_SRC_ALPHA);
                for(int i=0;i<active;i++){Entry e=ordered[i];
                    if(left<12)break;if(e.player.getDistanceSq(cx,cy,cz)>Math.pow(AlienClient.get().performance().effectDistance(),2))continue;
                    GL11.glPushMatrix();try{GL11.glTranslated(e.player.prevPosX+(e.player.posX-e.player.prevPosX)*partial,e.player.prevPosY+(e.player.posY-e.player.prevPosY)*partial,e.player.prevPosZ+(e.player.posZ-e.player.prevPosZ)*partial);
                        GL11.glBegin(GL11.GL_TRIANGLES);int units;try{units=AuraGeometry.emit(e.a.auraDesign,e.state,partial,Math.min(104,left),pass==0,GL_SINK);}finally{GL11.glEnd();}
                        left-=units;if(pass==1)drawn+=units;
                    }finally{GL11.glPopMatrix();}
                }
            }
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();GlStateManager.bindTexture(texture);}
    }
    /** Called only inside an actual player preview, at the vanilla model's foot origin. */
    public void previewLayer(EntityPlayer p,CosmeticConfig.Accessory a){
        Minecraft m=Minecraft.getMinecraft();if(!a.aura||!a.auraDesign.enabled||!a.animations||!AlienClient.get().config().animations||m.theWorld==null)return;
        if(previewDesign!=a.auraDesign){preview.clear();previewDesign=a.auraDesign;previewTick=-1;}
        if(previewTick!=p.ticksExisted){preview.update(a.auraDesign,.05*(previewTick<0?1:Math.max(1,Math.min(5,p.ticksExisted-previewTick))),Math.hypot(p.posX-p.prevPosX,p.posZ-p.prevPosZ)*20,p==m.thePlayer&&AlienClient.get().emotes().active(),hour(m));previewTick=p.ticksExisted;}
        int budget=Math.min(104,Math.max(0,AlienClient.get().performance().particleBudget()));if(budget<12)return;
        int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try{setup();GL11.glTranslated(0,1.5,0);GL11.glScaled(1,-1,1);
            for(int pass=glow()?0:1;pass<2;pass++){
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA,pass==0?GL11.GL_ONE:GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glBegin(GL11.GL_TRIANGLES);
                try{int units=AuraGeometry.emit(a.auraDesign,preview,1,budget,pass==0,GL_SINK);if(pass==1)previewDrawn+=units;}finally{GL11.glEnd();}
            }
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();GlStateManager.bindTexture(texture);}
    }
    private boolean glow(){return AlienClient.get().performance().qualityScale()>=.65f&&!"Competitive".equals(AlienClient.get().config().performance.preset);}
    private void setup(){GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);GL11.glEnable(GL11.GL_BLEND);GL11.glShadeModel(GL11.GL_SMOOTH);}
    private static final class Entry{final AuraResponse state=new AuraResponse();CosmeticConfig.Accessory a;EntityPlayer player;boolean present;}
}
