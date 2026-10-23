package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/** Camera-facing feathered ribbons through real player history, with depth and fixed budgets. */
final class RibbonTrails {
    private final Map<UUID,Entry> paths=new LinkedHashMap<UUID,Entry>();
    private Object world;private long tick;private final double[] samples=new double[14];
    private double rightX,rightZ,upX,upY,upZ;
    private static int active,drawn;
    static int activeCount(){return active;}static int drawnSegments(){return drawn;}
    void tick(Minecraft m,CosmeticConfig config){
        tick++;
        if(world!=m.theWorld||m.thePlayer==null||!config.enabled){paths.clear();active=drawn=0;world=m.theWorld;}
        if(m.thePlayer==null||m.theWorld==null||!config.enabled)return;
        for(Entry e:paths.values())e.present=false;
        int allowed=Math.min(8,Math.max(0,AlienClient.get().performance().cosmeticBudget()));
        float distance=Math.min(config.maxDistance,AlienClient.get().performance().effectDistance());
        for(EntityPlayer player:m.theWorld.playerEntities){
            CosmeticConfig.Accessory a=player==m.thePlayer?config.self:config.players.get(player.getUniqueID().toString());
            if(a==null||!a.enabled||!a.trail||!"Ribbon".equals(a.trailStyle)||!a.animations||!AlienClient.get().config().animations
                    ||player.isDead||player.isSpectator()||player.isInvisibleToPlayer(m.thePlayer)||player.getDistanceSqToEntity(m.thePlayer)>distance*distance)continue;
            Entry e=paths.get(player.getUniqueID());
            if(e==null){if(paths.size()>=allowed)continue;e=new Entry();paths.put(player.getUniqueID(),e);}
            e.present=true;e.a=a;e.player=player;e.path.expire(tick,Math.round(a.trailLifetime*20));
            double dx=player.posX-player.prevPosX,dz=player.posZ-player.prevPosZ;
            if(dx*dx+dz*dz>.0004){
                if(e.path.size()==0)e.path.add(player.prevPosX,player.prevPosY+.12,player.prevPosZ,tick-1);
                e.path.add(player.posX,player.posY+.12,player.posZ,tick);
            }
        }
        Iterator<Entry> iterator=paths.values().iterator();while(iterator.hasNext())if(!iterator.next().present)iterator.remove();
        active=0;for(Entry e:paths.values())if(e.path.size()>1)active++;
    }
    void render(RenderWorldLastEvent event){
        drawn=0;Minecraft m=Minecraft.getMinecraft();Entity camera=m.getRenderViewEntity();
        if(paths.isEmpty()||camera==null||m.theWorld!=world)return;
        double cx=camera.lastTickPosX+(camera.posX-camera.lastTickPosX)*event.partialTicks;
        double cy=camera.lastTickPosY+(camera.posY-camera.lastTickPosY)*event.partialTicks;
        double cz=camera.lastTickPosZ+(camera.posZ-camera.lastTickPosZ)*event.partialTicks;
        int budget=Math.min(192,Math.max(0,AlienClient.get().performance().particleBudget())*2);
        boolean glow=AlienClient.get().performance().qualityScale()>=.65f&&!"Competitive".equals(AlienClient.get().config().performance.preset);
        double yaw=Math.toRadians(m.getRenderManager().playerViewY),pitch=Math.toRadians(m.getRenderManager().playerViewX);
        rightX=Math.cos(yaw);rightZ=Math.sin(yaw);upX=-Math.sin(yaw)*Math.sin(pitch);upY=Math.cos(pitch);upZ=Math.cos(yaw)*Math.sin(pitch);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_LIGHTING_BIT);
        GL11.glPushMatrix();
        try{
            GL11.glTranslated(-cx,-cy,-cz);GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);GL11.glEnable(GL11.GL_BLEND);GL11.glShadeModel(GL11.GL_SMOOTH);
            for(int pass=glow?0:1;pass<2;pass++){
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA,pass==0?GL11.GL_ONE:GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glBegin(GL11.GL_TRIANGLES);
                int emitted=0;
                for(Entry e:paths.values()){
                    if(e.path.size()<2)continue;
                    double distance=AlienClient.get().performance().effectDistance();
                    if(e.player.getDistanceSq(cx,cy,cz)>distance*distance)continue;
                    for(int segment=0;segment<e.path.size()-1&&emitted<budget;segment++)for(int sub=0;sub<2&&emitted<budget;sub++){
                        sample(e,segment,sub*.5,event.partialTicks,cx,cy+camera.getEyeHeight(),cz,0);
                        sample(e,segment,(sub+1)*.5,event.partialTicks,cx,cy+camera.getEyeHeight(),cz,7);
                        if(pass==1)drawn++;emitted++;
                        float w=e.a.trailWidth*(pass==0?2.8f:1),opacity=e.a.trailOpacity*(pass==0?.13f:1);
                        for(int band=0;band<4;band++){
                            float[] offsets=EDGES;float left=offsets[band],right=offsets[band+1];
                            vertex(e,0,left,w,opacity,pass);vertex(e,7,left,w,opacity,pass);vertex(e,7,right,w,opacity,pass);
                            vertex(e,0,left,w,opacity,pass);vertex(e,7,right,w,opacity,pass);vertex(e,0,right,w,opacity,pass);
                        }
                    }
                }
                GL11.glEnd();
            }
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
    private static final float[] EDGES={-1,-.38f,0,.38f,1};
    private void sample(Entry e,int segment,double t,float partial,double cx,double cy,double cz,int at){
        RibbonPath p=e.path;
        double x=p.sample(0,segment,t),y=p.sample(1,segment,t),z=p.sample(2,segment,t);
        if(segment==p.size()-2){double blend=t*t; x+=(e.player.prevPosX+(e.player.posX-e.player.prevPosX)*partial-p.coordinate(0,p.size()-1))*blend;
            y+=(e.player.prevPosY+(e.player.posY-e.player.prevPosY)*partial+.12-p.coordinate(1,p.size()-1))*blend;
            z+=(e.player.prevPosZ+(e.player.posZ-e.player.prevPosZ)*partial-p.coordinate(2,p.size()-1))*blend;}
        double tx=p.sample(0,segment,t+.01)-p.sample(0,segment,t-.01),ty=p.sample(1,segment,t+.01)-p.sample(1,segment,t-.01),tz=p.sample(2,segment,t+.01)-p.sample(2,segment,t-.01);
        // Rotate the projected tangent in the actual camera plane, including front third person.
        double screenX=tx*rightX+tz*rightZ,screenY=tx*upX+ty*upY+tz*upZ;
        double nx=-screenY*rightX+screenX*upX,ny=screenX*upY,nz=-screenY*rightZ+screenX*upZ,n=Math.sqrt(nx*nx+ny*ny+nz*nz);
        if(n<1e-8){nx=rightX;ny=0;nz=rightZ;n=1;}
        int life=Math.round(e.a.trailLifetime*20);double alpha=(p.alpha(segment,tick+partial,life)*(1-t)+p.alpha(segment+1,tick+partial,life)*t)*Math.min(1,(segment+t)/2);
        samples[at]=x;samples[at+1]=y;samples[at+2]=z;samples[at+3]=nx/n;samples[at+4]=ny/n;samples[at+5]=nz/n;samples[at+6]=alpha;
    }
    private void vertex(Entry e,int at,float edge,float width,float opacity,int pass){
        double alpha=samples[at+6],radius=width*(.15+.85*alpha)*edge;
        float blend=(float)(.2+.7*alpha),shade=pass==1&&edge==0?.4f:0;
        int a=e.a.color,b=e.a.secondaryColor;
        float r=((a>>16&255)*(1-blend)+(b>>16&255)*blend)/255f,g=((a>>8&255)*(1-blend)+(b>>8&255)*blend)/255f,blue=((a&255)*(1-blend)+(b&255)*blend)/255f;
        GL11.glColor4f(r+(1-r)*shade,g+(1-g)*shade,blue+(1-blue)*shade,(float)(alpha*opacity*(Math.abs(edge)==1?0:edge==0?1:.75)));
        GL11.glVertex3d(samples[at]+samples[at+3]*radius,samples[at+1]+samples[at+4]*radius,samples[at+2]+samples[at+5]*radius);
    }
    private static final class Entry{final RibbonPath path=new RibbonPath();CosmeticConfig.Accessory a;EntityPlayer player;boolean present;}
}
