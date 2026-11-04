package dev.alien.client.emote;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.*;
import org.lwjgl.input.Keyboard;

/** Local render-only playback: never changes player coordinates, rotations, input or packets. */
public final class EmoteManager {
    private final EmotePlayback playback=new EmotePlayback();
    private final EmotePose rootPose=new EmotePose();
    private final KeyBinding wheel;private int previousKey;
    private Object world,profile;private Entity player;
    private float previousHealth=Float.NaN,renderPartial;
    private String pending="",status="Choose an emote";private int pendingTicks,oldCamera;private boolean ownsCamera;
    private String preview="";private double previewTime;private int previewDepth,depth;
    private final Object[] renderers=new Object[8],entities=new Object[8];
    private long posedFrames,armorFrames;
    public EmoteManager(){previousKey=c().wheelKey;wheel=new KeyBinding("Alien / Emote wheel",previousKey,"key.categories.alienclient");ClientRegistry.registerKeyBinding(wheel);}
    private EmoteConfig c(){return AlienClient.get().config().emotes;}
    public String status(){return status;}public boolean active(){return playback.active();}public String activeId(){return playback.id();}
    public long posedFrames(){return posedFrames;}public long armorFrames(){return armorFrames;}public int renderDepth(){return depth;}
    public static long articulatedDraws(){return ArticulatedLimb.drawn;}
    public static float jointBend(net.minecraft.client.model.ModelRenderer limb){return EmoteModels.bend(limb);}
    public static void handBend(net.minecraft.client.model.ModelBiped model,float scale){EmoteModels.hand(model,scale);}
    public boolean keyHeld(){return wheel.getKeyCode()>0&&Keyboard.isKeyDown(wheel.getKeyCode());}
    private boolean conflict(){if(wheel.getKeyCode()==0)return false;for(KeyBinding key:Minecraft.getMinecraft().gameSettings.keyBindings)if(key!=wheel&&key.getKeyCode()==wheel.getKeyCode())return true;return false;}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void key(InputEvent.KeyInputEvent event){
        boolean pressed=Keyboard.getEventKeyState()&&Keyboard.getEventKey()==wheel.getKeyCode();while(wheel.isPressed())pressed=true;Minecraft m=Minecraft.getMinecraft();
        if(!pressed||wheel.getKeyCode()==0||m.currentScreen!=null||m.thePlayer==null||!c().enabled)return;
        if(conflict()){Ui.notify("Emote key conflicts; change it in Controls");return;}
        m.displayGuiScreen(new EmoteWheelScreen(null,true));
    }
    public boolean request(String id){
        Minecraft m=Minecraft.getMinecraft();
        if(!EmoteLibrary.valid(id)||!c().enabled||!AlienClient.get().config().animations||m.theWorld==null||m.thePlayer==null){status="Emotes need a world and enabled animations";return false;}
        RenderPlayer renderer=m.getRenderManager().getSkinMap().get(m.thePlayer.getSkinType());
        if(renderer==null||!EmoteModels.prepare(renderer,m.thePlayer)){status="Emote animation unavailable with this renderer";return false;}
        pending=id;pendingTicks=0;status="Release controls to begin";return true;
    }
    public void stop(){pending="";playback.stop();status="Stopped";}
    private void clear(String reason){pending="";playback.clear();restoreCamera();status=reason;}
    private void restoreCamera(){Minecraft m=Minecraft.getMinecraft();if(ownsCamera&&m.gameSettings.thirdPersonView==2)m.gameSettings.thirdPersonView=oldCamera;ownsCamera=false;}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;Minecraft m=Minecraft.getMinecraft();EmoteConfig settings=c();
        if(settings.wheelKey!=previousKey){wheel.setKeyCode(settings.wheelKey);previousKey=settings.wheelKey;KeyBinding.resetKeyBindingArrayAndHash();}
        else if(wheel.getKeyCode()!=previousKey){int key=wheel.getKeyCode();settings.wheelKey=key<0||key>255?0:key;wheel.setKeyCode(settings.wheelKey);previousKey=settings.wheelKey;KeyBinding.resetKeyBindingArrayAndHash();AlienClient.get().save();}
        if(world!=m.theWorld||profile!=settings||player!=m.thePlayer){clear("Context reset");world=m.theWorld;profile=settings;player=m.thePlayer;previousHealth=Float.NaN;}
        if(m.thePlayer==null||m.theWorld==null)return;
        float health=m.thePlayer.getHealth();boolean damaged=Float.isFinite(previousHealth)&&health<previousHealth-.001f;previousHealth=health;
        if(!settings.enabled||!AlienClient.get().config().animations){clear("Emotes disabled");return;}
        if(m.thePlayer.isDead||health<=0||damaged||m.thePlayer.hurtTime>0||m.thePlayer.isRiding()){if(active()||!pending.isEmpty())clear("Stopped for player action");return;}
        if(m.isGamePaused())return;
        boolean moving=m.thePlayer.movementInput.moveForward!=0||m.thePlayer.movementInput.moveStrafe!=0||m.gameSettings.keyBindJump.isKeyDown()||m.thePlayer.isSneaking()||!m.thePlayer.onGround||m.thePlayer.isInWater()
                ||Math.hypot(m.thePlayer.posX-m.thePlayer.prevPosX,m.thePlayer.posZ-m.thePlayer.prevPosZ)>.025;
        boolean action=m.gameSettings.keyBindAttack.isKeyDown()||m.gameSettings.keyBindUseItem.isKeyDown()||m.thePlayer.isSwingInProgress||m.thePlayer.isUsingItem();
        if(!pending.isEmpty()){
            if(++pendingTicks>20){pending="";status="Stand still on the ground to emote";}
            else if(m.currentScreen==null&&!moving&&!action){String id=pending;pending="";playback.play(id,settings.speed,settings.loop);status="Playing / "+EmoteLibrary.name(id);if(settings.autoThirdPerson&&m.gameSettings.thirdPersonView==0){oldCamera=0;ownsCamera=true;m.gameSettings.thirdPersonView=2;}}
            else status=moving?"Stand still on the ground":action?"Release attack and item use":"Close the menu to begin";
        }
        if(playback.playing()){if(action)clear("Stopped for player action");else if(moving)stop();}
        playback.update(.05);if(!playback.active())restoreCamera();
    }
    @SubscribeEvent public void gui(GuiOpenEvent event){if(event.gui!=null&&!(event.gui instanceof EmoteWheelScreen))clear("Stopped for menu");}
    @SubscribeEvent public void frame(TickEvent.RenderTickEvent event){if(event.phase==TickEvent.Phase.START)renderPartial=event.renderTickTime;}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void prepare(RenderPlayerEvent.Pre event){
        if(event.entityPlayer!=Minecraft.getMinecraft().thePlayer||(!active()&&previewDepth==0))return;
        if(!EmoteModels.prepare(event.renderer,(AbstractClientPlayer)event.entityPlayer))clear("Emote animation unavailable with this renderer");
    }
    private boolean pose(Entity entity,double partial,EmotePose out){
        if(entity!=Minecraft.getMinecraft().thePlayer||!c().enabled){out.clear();return false;}
        if(previewDepth>0){EmoteLibrary.sample(preview,previewTime,out);return out.visible();}
        playback.sample(partial,out);return out.visible();
    }
    boolean modelPose(Entity entity,float age,EmotePose out){if(depth==0){out.clear();return false;}float partial=age-(float)Math.floor(age);return pose(entity,partial*.05,out);}
    void modelApplied(boolean armor){if(armor)armorFrames++;else posedFrames++;}
    boolean hideItem(Entity entity){return entity==Minecraft.getMinecraft().thePlayer&&c().enabled&&c().hideHeldItem&&(active()||previewDepth>0);}
    public void preview(String id,double seconds,Runnable draw){String old=preview;double time=previewTime;preview=id;previewTime=seconds;previewDepth++;try{draw.run();}finally{previewDepth--;preview=old;previewTime=time;}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void root(RenderLivingEvent.Pre event){
        if(!(event.renderer instanceof RenderPlayer)||event.entity!=Minecraft.getMinecraft().thePlayer||depth>=8||(!active()&&previewDepth==0))return;
        if(!pose(event.entity,renderPartial*.05,rootPose))return;
        renderers[depth]=event.renderer;entities[depth]=event.entity;depth++;
        double yaw=Math.toRadians(event.entity.renderYawOffset);
        GlStateManager.pushMatrix();GlStateManager.translate(event.x+Math.cos(yaw)*rootPose.sway,event.y+rootPose.lift,event.z+Math.sin(yaw)*rootPose.sway);
        GlStateManager.rotate((float)Math.toDegrees(rootPose.yaw),0,1,0);GlStateManager.translate(-event.x,-event.y,-event.z);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void rootEnd(RenderLivingEvent.Post event){
        if(depth==0||renderers[depth-1]!=event.renderer||entities[depth-1]!=event.entity)return;
        depth--;renderers[depth]=entities[depth]=null;GlStateManager.popMatrix();
    }
}
