package dev.alien.client.scene;
import dev.alien.client.AlienClient;import dev.alien.client.cosmetic.CosmeticManager;
import net.minecraft.client.Minecraft;import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;import net.minecraft.entity.item.EntityItem;import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;import net.minecraftforge.fml.common.eventhandler.*;import net.minecraftforge.fml.common.gameevent.TickEvent;
/** Independent local scene effects. Existing sky, cosmetics and gameplay configurations remain intact. */
public final class SceneManager {
    private final SceneHistory waves=new SceneHistory(),path=new SceneHistory();
    private Object world,profile;private boolean grounded,replacing;private double clock;private int rendered;
    private ScenePlayerRenderer classic,slim;
    private static long landings,droppedGlass;
    public static long glassBodyDraws(){return ScenePlayerRenderer.glassBodies;}public static long glassHeldDraws(){return ScenePlayerRenderer.glassHeld;}public static long glassDroppedDraws(){return droppedGlass;}private static int waveCount,pathCount;
    public static long landings(){return landings;}public static int waveCount(){return waveCount;}public static int pathCount(){return pathCount;}
    private SceneConfig c(){return AlienClient.get().config().scenes;}
    public void install(CosmeticManager cosmetics){Minecraft m=Minecraft.getMinecraft();for(RenderPlayer r:m.getRenderManager().getSkinMap().values())r.addLayer(new ScenePlayerLayer(r));
        classic=new ScenePlayerRenderer(m.getRenderManager(),false);slim=new ScenePlayerRenderer(m.getRenderManager(),true);cosmetics.attach(classic);cosmetics.attach(slim);
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;Minecraft m=Minecraft.getMinecraft();SceneConfig c=c();
        if(world!=m.theWorld||profile!=c){waves.clear();path.clear();grounded=true;clock=0;world=m.theWorld;profile=c;}
        if(m.thePlayer==null||m.theWorld==null||!c.enabled){waves.clear();path.clear();waveCount=pathCount=0;return;}
        if(m.isGamePaused())return;clock+=.05;waves.expire(clock,c.lifetime);path.expire(clock,c.chromaMemory);
        if(c.shockwave&&m.thePlayer.onGround&&!grounded&&m.thePlayer.ticksExisted>10){waves.add(m.thePlayer.posX,m.thePlayer.posY,m.thePlayer.posZ,clock,4);landings++;}
        grounded=m.thePlayer.onGround;if(!c.shockwave)waves.clear();
        if("Chroma".equals(c.mode)){double x=m.thePlayer.posX,y=m.thePlayer.posY,z=m.thePlayer.posZ;if(path.lastDistanceSq(x,y,z)>64)path.clear();if(path.lastDistanceSq(x,y,z)>.6||path.size()==0)path.add(x,y,z,clock,32);}else path.clear();
        waveCount=waves.size();pathCount=path.size();
        RenderManager rm=m.getRenderManager();Render<EntityItem> original=rm.getEntityClassRenderObject(EntityItem.class);
        if(!(original instanceof GlassItemRenderer))rm.entityRenderMap.put(EntityItem.class,new GlassItemRenderer(rm,original));
    }
    @SubscribeEvent public void frame(TickEvent.RenderTickEvent e){if(e.phase==TickEvent.Phase.START){rendered=0;ScenePlayerLayer.beginFrame();}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void world(RenderWorldLastEvent e){SceneConfig c=c();if(!c.enabled||Minecraft.getMinecraft().theWorld==null)return;
        if(c.skyIndex()>0||c.modeIndex()==1||c.modeIndex()==4||c.mist||c.bokeh||(c.shockwave&&waves.size()>0))SceneShaders.render(c,waves,path,clock+e.partialTicks*.05,e.partialTicks);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void afterHand(RenderGameOverlayEvent.Pre e){if(e.type==RenderGameOverlayEvent.ElementType.ALL&&c().enabled&&c().radiance&&Minecraft.getMinecraft().theWorld!=null)SceneShaders.render(c(),waves,path,clock+e.partialTicks*.05,e.partialTicks,true);}
    @SubscribeEvent(priority=EventPriority.LOW) public void player(RenderPlayerEvent.Pre e){
        if(replacing||!c().enabled||!(e.entityPlayer instanceof AbstractClientPlayer)||!("Glass".equals(c().mode)||"Skeleton".equals(c().mode)))return;
        Minecraft m=Minecraft.getMinecraft();if(e.entityPlayer.isSpectator()||e.entityPlayer.isInvisibleToPlayer(m.thePlayer)||e.entityPlayer.getDistanceSqToEntity(m.thePlayer)>c().distance*c().distance||rendered++>=Math.min(16,AlienClient.get().performance().cosmeticBudget()))return;
        AbstractClientPlayer p=(AbstractClientPlayer)e.entityPlayer;e.setCanceled(true);replacing=true;
        try{("slim".equals(p.getSkinType())?slim:classic).doRender(p,e.x,e.y,e.z,p.prevRotationYaw+(p.rotationYaw-p.prevRotationYaw)*e.partialRenderTick,e.partialRenderTick);}finally{replacing=false;}
    }
    private static final class GlassItemRenderer extends Render<EntityItem>{
        private final Render<EntityItem> original;GlassItemRenderer(RenderManager rm,Render<EntityItem> original){super(rm);this.original=original;}
        @Override public void doRender(EntityItem e,double x,double y,double z,float yaw,float partial){SceneConfig c=AlienClient.get().config().scenes;if(c.enabled&&"Glass".equals(c.mode)&&e.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer)<c.distance*c.distance)SceneShaders.glass(()->{original.doRender(e,x,y,z,yaw,partial);droppedGlass++;});else original.doRender(e,x,y,z,yaw,partial);}
        @Override public void doRenderShadowAndFire(net.minecraft.entity.Entity e,double x,double y,double z,float yaw,float partial){original.doRenderShadowAndFire(e,x,y,z,yaw,partial);}
        @Override protected ResourceLocation getEntityTexture(EntityItem e){return net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture;}
    }
}
