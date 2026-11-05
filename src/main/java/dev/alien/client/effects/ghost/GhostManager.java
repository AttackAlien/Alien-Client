package dev.alien.client.effects.ghost;

import dev.alien.client.AlienClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/** Render-only confirmed death effects. No attack hooks, outgoing packets, damage or world entities. */
public final class GhostManager {
    private static GhostManager instance;
    private static final class Context {
        final Object world,profile,server;final UUID player;final int dimension;final boolean players,mobs;
        Context(Minecraft m,GhostConfig c){world=m.theWorld;profile=c;server=m.getIntegratedServer();player=m.thePlayer.getUniqueID();dimension=m.thePlayer.dimension;players=c.players;mobs=c.mobs;}
    }
    private static final class Snapshot {
        final UUID id;final Class<?> type;final int entityId;final double x,y,z;final boolean player;final String name;final long tick;
        Snapshot(EntityLivingBase e,long t){id=e.getUniqueID();type=e.getClass();entityId=e.getEntityId();x=e.posX;y=e.posY;z=e.posZ;player=e instanceof EntityPlayer;name=e.getDisplayName().getUnformattedText();tick=t;}
    }
    private static final class Pending {final Snapshot at;final Context context;final String source;final long born;Pending(Snapshot s,Context c,String source,long t){at=s;context=c;this.source=source;born=t;}}
    private static final class Spirit {final Snapshot at;final GhostConfig design;final long start;final boolean preview;Spirit(Snapshot s,GhostConfig c,long t,boolean preview){at=s;design=c.copy();start=t;this.preview=preview;}}
    private volatile Context context;private Object world,profile;private long ticks;
    private final ArrayBlockingQueue<Pending> inbox=new ArrayBlockingQueue<Pending>(16);
    private final LinkedHashMap<UUID,Snapshot> known=new LinkedHashMap<UUID,Snapshot>();private final Map<UUID,Pending> pending=new LinkedHashMap<UUID,Pending>();private final List<Spirit> spirits=new ArrayList<Spirit>();private final GhostLedger ledger=new GhostLedger();
    private static long confirmed,previews,rendered;private static int active;private static String lastSource="Waiting for a confirmed elimination";
    public GhostManager(){instance=this;}
    public static long confirmed(){return confirmed;}public static long previews(){return previews;}public static long rendered(){return rendered;}public static int active(){return active;}public static float oldestAge(){return instance==null||instance.spirits.isEmpty()?-1:(instance.ticks-instance.spirits.get(0).start)/20f;}public static String lastSource(){return lastSource;}
    private GhostConfig cfg(){return AlienClient.get().config().killEffects;}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;Minecraft m=Minecraft.getMinecraft();GhostConfig c=cfg();
        if(world!=m.theWorld||profile!=c){clear();world=m.theWorld;profile=c;ticks=0;}
        if(m.theWorld==null||m.thePlayer==null||!c.enabled){context=null;clear();return;}
        context=new Context(m,c);if(m.isGamePaused())return;ticks++;
        // Only player identities are needed for remote vanilla death translations. Keep a bounded pose cache.
        known.entrySet().removeIf(e->ticks-e.getValue().tick>80);
        int scanned=0;for(Object value:m.theWorld.playerEntities){if(scanned++>=128)break;EntityPlayer p=(EntityPlayer)value;if(p==m.thePlayer||p.getDistanceSqToEntity(m.thePlayer)>c.distance*c.distance)continue;known.put(p.getUniqueID(),new Snapshot(p,ticks));}
        while(known.size()>128)known.remove(known.keySet().iterator().next());
        Pending next;while((next=inbox.poll())!=null){if(same(next.context)&&pending.size()<32)pending.put(next.at.id,new Pending(next.at,next.context,next.source,ticks));}
        Iterator<Pending> wait=pending.values().iterator();while(wait.hasNext()){
            Pending p=wait.next();if(!same(p.context)||ticks-p.born>35){wait.remove();continue;}
            Entity entity=m.theWorld.getEntityByID(p.at.entityId);
            if(entity!=null&&!GhostLedger.trackedIdentity(p.at.player,p.at.id,entity.getUniqueID(),p.at.type,entity.getClass())){wait.remove();continue;}
            // An attributed server death must also reach the client as actual death/removal.
            boolean dead=entity==null||entity instanceof EntityLivingBase&&(((EntityLivingBase)entity).deathTime>0||((EntityLivingBase)entity).getHealth()<=0);
            
            if(dead){spawn(p.at,c,p.source,false,m);wait.remove();}
        }
        spirits.removeIf(s->ticks-s.start>Math.ceil(s.design.duration*20));active=spirits.size();
    }
    private boolean same(Context c){Context now=context;return c!=null&&now!=null&&c.world==now.world&&c.profile==now.profile&&c.player.equals(now.player);}
    private void clear(){inbox.clear();known.clear();pending.clear();spirits.clear();ledger.clear();active=0;}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void death(LivingDeathEvent event){
        Context c=context;if(c==null||event.isCanceled()||event.entityLiving.worldObj.isRemote||event.entityLiving.dimension!=c.dimension)return;
        // Only our own integrated server exposes authoritative killer attribution to this client.
        if(!(event.entityLiving.worldObj instanceof net.minecraft.world.WorldServer)||((net.minecraft.world.WorldServer)event.entityLiving.worldObj).getMinecraftServer()!=c.server)return;
        Entity killer=event.entityLiving.getAttackingEntity();if(killer==null)killer=event.source.getEntity();if(killer==null||!GhostLedger.localKill(c.player,killer.getUniqueID(),event.entityLiving.getUniqueID()))return;
        if(event.entityLiving instanceof EntityPlayer?!c.players:!c.mobs)return;
        
        inbox.offer(new Pending(new Snapshot(event.entityLiving,0),c,"Integrated server",0));
    }
    @SubscribeEvent public void chat(ClientChatReceivedEvent event){
        Minecraft m=Minecraft.getMinecraft();Context c=context;if(c==null||!c.players||!(event.message instanceof ChatComponentTranslation)||m.thePlayer==null)return;
        ChatComponentTranslation msg=(ChatComponentTranslation)event.message;Object[] a=msg.getFormatArgs();if(a.length<2)return;
        String killer=text(a[1]);if(!GhostLedger.deathTranslation(msg.getKey(),killer,m.thePlayer.getName(),m.thePlayer.getDisplayName().getUnformattedText()))return;
        String victim=GhostLedger.clean(text(a[0]));Snapshot found=null;
        for(Snapshot p:known.values())if(GhostLedger.clean(p.name).equals(victim)){if(found!=null)return;found=p;}
        if(found==null||found.id.equals(c.player)||pending.size()>=32)return;
        pending.put(found.id,new Pending(found,c,"Vanilla PvP death message",ticks));
    }
    private static String text(Object a){return a instanceof IChatComponent?((IChatComponent)a).getUnformattedText():a instanceof String?(String)a:"";}
    private void spawn(Snapshot at,GhostConfig c,String source,boolean preview,Minecraft m){
        if(!Double.isFinite(at.x)||!Double.isFinite(at.y)||!Double.isFinite(at.z)||m.thePlayer.getDistanceSq(at.x,at.y,at.z)>c.distance*c.distance||!preview&&!ledger.accept(at.id,ticks))return;
        
        int max=Math.min(c.maxGhosts,Math.max(0,AlienClient.get().performance().cosmeticBudget()/4));if(max<=0)return;
        while(spirits.size()>=max)spirits.remove(0);spirits.add(new Spirit(at,c,ticks,preview));if(preview)previews++;else{confirmed++;lastSource=source;}active=spirits.size();
    }
    public static boolean preview(){
        Minecraft m=Minecraft.getMinecraft();if(instance==null||m.thePlayer==null||m.theWorld==null)return false;
        GhostConfig c=instance.cfg();Snapshot at=new Snapshot(m.thePlayer,instance.ticks);instance.spawn(at,c,"Preview",true,m);return true;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public void render(RenderWorldLastEvent event){
        Minecraft m=Minecraft.getMinecraft();if(m.theWorld==null||m.thePlayer==null||!cfg().enabled||spirits.isEmpty())return;
        double cx=m.getRenderManager().viewerPosX,cy=m.getRenderManager().viewerPosY,cz=m.getRenderManager().viewerPosZ;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try{GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glTranslated(-cx,-cy,-cz);int budget=Math.max(0,AlienClient.get().performance().particleBudget());
            int shown=0,max=Math.min(cfg().maxGhosts,Math.max(0,AlienClient.get().performance().cosmeticBudget()/4));
            for(Spirit spirit:spirits){if(shown++>=max)break;if(m.thePlayer.getDistanceSq(spirit.at.x,spirit.at.y,spirit.at.z)>cfg().distance*cfg().distance)continue;float age=(ticks+event.partialTicks-spirit.start)/20f;
                GL11.glPushMatrix();try{GL11.glTranslated(spirit.at.x,spirit.at.y+.08,spirit.at.z);GL11.glRotated(Math.toDegrees(Math.atan2(spirit.at.x-cx,spirit.at.z-cz)),0,1,0);GhostRenderer.draw(age,spirit.design,AlienClient.get().config().animations,budget/Math.max(1,spirits.size()));rendered++;}finally{GL11.glPopMatrix();}
            }
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
}
