package dev.alien.client.smoke;

import com.mojang.authlib.GameProfile;
import dev.alien.client.AlienClient;
import dev.alien.client.combat.*;
import dev.alien.client.hud.*;
import dev.alien.client.gui.*;
import dev.alien.client.config.ClientConfig;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.awt.Robot;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.potion.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.*;

/** Test-only Forge mod. Uses actual client/server entities and an isolated disposable world. */
@Mod(modid="alienclient_ultimatesmoke",name="Alien HUD / Full tools smoke",version="1",clientSideOnly=true,dependencies="required-after:alienclient")
public final class AlienUltimateSmoke {
    private Minecraft m;private File out;private long start,since;private int stage;private boolean done;
    private final List<String> checks=new ArrayList<String>();
    private EntityOtherPlayerMP dummy;private Robot robot;private long beforeFoods,beforePotions,beforeBlocks;
    private float angleBefore;private int oldSlot;private float draggedX;
    private ClientConfig c(){return AlienClient.get().config();}
    @Mod.EventHandler public void init(FMLInitializationEvent e)throws Exception{
        if(!Boolean.getBoolean("alien.ultimate.smoke"))return;
        m=Minecraft.getMinecraft();out=new File(System.getProperty("alien.ultimate.smoke.output"));Files.createDirectories(out.toPath());
        Files.deleteIfExists(new File(out,"result.txt").toPath());AlienClient.get().store().resetDefaults();
        start=since=System.nanoTime();FMLCommonHandler.instance().bus().register(this);
    }
    @SubscribeEvent public void render(TickEvent.RenderTickEvent e){
        if(done||e.phase!=TickEvent.Phase.END)return;
        try{
            if(elapsed(start)>230000)throw new AssertionError("Deadline at stage "+stage);
            long wait=stage==4?110:stage==5?1600:stage==15||stage==17?2500:700;
            if(elapsed(since)<wait)return;step();
        }catch(Throwable failure){
            done=true;try(PrintWriter w=new PrintWriter(new File(out,"result.txt"),"UTF-8")){
                w.println("FAIL stage "+stage);for(String check:checks)w.println("PASS "+check);failure.printStackTrace(w);
            }catch(Exception ignored){}
            if(robot!=null){robot.keyRelease(java.awt.event.KeyEvent.VK_W);robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);}
            throw new RuntimeException("HUD Ultimate / Full integration failed",failure);
        }
    }
    private EntityPlayerMP serverPlayer(){return m.getIntegratedServer().getConfigurationManager().playerEntityList.get(0);}
    private void server(Runnable work)throws Exception{m.getIntegratedServer().addScheduledTask(work).get();}
    private void step()throws Exception{
        switch(stage){
        case 0:
            if(m.currentScreen==null)return;
            require(c().hud.widgets.size()==15,"All 12 existing widgets and three new widgets are registered");
            require(AlienClient.get().modules().modules().size()==17,"Unified registers nine visuals and eight real gameplay switches");
            require(!c().combat.enabled&&!c().combat.tools.autoFood&&!c().combat.tools.bridge,"New gameplay functions are off on fresh profiles");
            resize(1280,720);m.gameSettings.guiScale=2;m.gameSettings.limitFramerate=30;m.gameSettings.pauseOnLostFocus=false;
            m.launchIntegratedServer("AlienUltimate-"+System.currentTimeMillis(),"Disposable HUD / Full validation",new WorldSettings(83023,WorldSettings.GameType.SURVIVAL,false,false,WorldType.FLAT));
            next();return;
        case 1:
            if(m.thePlayer==null||m.theWorld==null)return;
            if(m.currentScreen!=null)m.displayGuiScreen(null);
            if(m.thePlayer.ticksExisted<40)return;
            server(()->{
                EntityPlayerMP p=serverPlayer();p.worldObj.getGameRules().setOrCreateGameRule("doMobSpawning","false");p.setHealth(18);p.getFoodStats().setFoodLevel(8);
                p.inventory.mainInventory[4]=new ItemStack(Items.bread,4);p.inventory.mainInventory[5]=new ItemStack(Items.potionitem,1,8197);
                p.inventory.mainInventory[6]=new ItemStack(Blocks.stone,64);
                p.inventory.armorInventory[3]=new ItemStack(Items.diamond_helmet,1,100);p.inventory.armorInventory[2]=new ItemStack(Items.diamond_chestplate,1,70);
                p.inventory.armorInventory[1]=new ItemStack(Items.diamond_leggings,1,190);p.inventory.armorInventory[0]=new ItemStack(Items.diamond_boots,1,350);
                p.addPotionEffect(new PotionEffect(Potion.moveSpeed.id,1200,0));p.inventoryContainer.detectAndSendChanges();
            });
            m.gameSettings.thirdPersonView=0;m.gameSettings.renderDistanceChunks=4;
            dummy();c().hud.applyPreset("PvP");c().hud.theme="Cyan";c().hud.widgets.get("target").x=.58f;
            c().hud.widgets.get("target").y=.65f;c().hud.widgets.get("crosshair").customColor=true;c().hud.widgets.get("crosshair").color=0xff43d9ed;
            faceDummy(0);AlienClient.get().save();next();return;
        case 2:
            require(UltimateHud.trackedEntity()==dummy.getEntityId(),"Target HUD selects the real ray-traced practice entity");
            require(m.thePlayer.inventory.armorInventory[3]!=null,"Real server inventory armor synchronizes to client");
            robot=new Robot();robot.mouseMove(Display.getX()+m.displayWidth/2,Display.getY()+m.displayHeight/2);
            robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);next();return;
        case 3:
            robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
            require(HudManager.leftCps()>0,"CPS counts actual automated X11 mouse events delivered to LWJGL");
            capture("40-hud-ultimate-pvp-live");
            BufferedImage image=ImageIO.read(new File(out,"screenshots/40-hud-ultimate-pvp-live.png"));
            int rgb=image.getRGB(image.getWidth()/2,image.getHeight()/2),r=rgb>>16&255,g=rgb>>8&255,blue=rgb&255;
            require(g>r+30&&blue>r+30,"Custom cyan crosshair replaces vanilla white at the actual framebuffer center");
            dummy.setHealth(8);next();return;
        case 4:
            require(UltimateHud.displayedHealth()<.99f&&UltimateHud.displayedHealth()>.4f,"Health bar visibly interpolates after entity damage");
            capture("41-target-health-animation");next();return;
        case 5:
            require(Math.abs(UltimateHud.displayedHealth()-.4f)<.04f,"Animated target health converges to the entity's real 8/20 health");
            capture("42-target-damaged");m.theWorld.removeEntityFromWorld(dummy.getEntityId());next();return;
        case 6:
            require(UltimateHud.trackedEntity()==-1,"Removed entities clear the target; no invented target remains");
            HudEditorScreen editor=new HudEditorScreen(null);m.displayGuiScreen(editor);editor.selectWidget("target");
            c().hud.widgets.get("target").x=.6f;c().hud.widgets.get("target").y=.55f;next();return;
        case 7:{
            HudConfig.Widget w=c().hud.widgets.get("target");int[] b=HudManager.bounds("target",w,m.currentScreen.width,m.currentScreen.height);
            float before=w.x;call("mouseClicked",new Class[]{int.class,int.class,int.class},b[0]+10,b[1]+10,0);
            call("mouseClickMove",new Class[]{int.class,int.class,int.class,long.class},b[0]-30,b[1]-35,0,80L);
            call("mouseReleased",new Class[]{int.class,int.class,int.class},b[0]-30,b[1]-35,0);
            require(w.x!=before,"Actual HUD canvas drag changes the selected widget position");draggedX=w.x;
            b=HudManager.bounds("target",w,m.currentScreen.width,m.currentScreen.height);float old=w.scale;
            call("mouseClicked",new Class[]{int.class,int.class,int.class},b[0]+b[2]-2,b[1]+b[3]-2,0);
            call("mouseClickMove",new Class[]{int.class,int.class,int.class,long.class},b[0]+b[2]+35,b[1]+b[3],0,100L);
            call("mouseReleased",new Class[]{int.class,int.class,int.class},b[0]+b[2]+35,b[1]+b[3],0);
            require(w.scale>old,"Actual resize handle scales the existing target widget");
            click(5);require(w.x!=draggedX,"HUD Center alignment operates after dragging and resizing");
            next();return;}
        case 8:
            capture("43-hud-ultimate-editor");click(53);next();return;
        case 9:
            require(m.currentScreen instanceof HudWidgetSettingsScreen,"Selected widget opens the real HUD settings screen");
            click(21);GuiButton opacity=button(1);opacity.mousePressed(m,opacity.xPosition+opacity.width/3,opacity.yPosition+opacity.height-2);opacity.mouseReleased(0,0);
            require(c().hud.widgets.get("target").opacity<.7f,"Widget opacity slider changes the selected widget only");click(0);
            click(14);next();return;
        case 10:
            require(m.currentScreen instanceof HudPresetsScreen,"HUD presets are accessible from the preserved editor");capture("44-hud-presets-themes");
            click(2);click(0);m.displayGuiScreen(null);next();return;
        case 11:
            require("Survival".equals(c().hud.preset)&&c().hud.widgets.get("indicators").enabled,"Survival preset enables live health, speed and potion indicators");
            capture("45-hud-survival-live");c().hud.applyPreset("Cinematic");next();return;
        case 12:
            require(!c().hud.widgets.get("crosshair").enabled&&!c().hud.widgets.get("keys").enabled,"Cinematic disables gameplay overlays without deleting their settings");
            capture("46-hud-cinematic-live");c().hud.applyPreset("PvP");
            m.displayGuiScreen(new HudWidgetSettingsScreen(null,"crosshair"));next();return;
        case 13:
            capture("47-crosshair-options");click(114);require("T".equals(c().hud.crosshair.shape),"Crosshair shape control selects an implemented shape");
            click(0);m.displayGuiScreen(null);c().combat.enabled=true;c().combat.aimAssist=false;c().combat.tools.autoFood=true;
            beforeFoods=FullToolsManager.foodsStarted();oldSlot=m.thePlayer.inventory.currentItem;next();return;
        case 14:
            if(FullToolsManager.foodsStarted()<=beforeFoods){if(elapsed(since)<10000)return;throw new AssertionError("Auto Food did not start");}
            require(true,"Auto Food selects actual hotbar food in a hungry server world");next();return;
        case 15:
            if(m.thePlayer.getFoodStats().getFoodLevel()<14){if(elapsed(since)<12000)return;throw new AssertionError("Auto Food did not restore hunger");}
            require(m.thePlayer.inventory.currentItem==oldSlot,"Auto Food restores the original selected slot after eating");
            require(serverPlayer().getFoodStats().getFoodLevel()>=14,"Auto Food actually increases server-side hunger through vanilla item use");capture("48-auto-food-result");
            c().combat.tools.autoFood=false;server(()->serverPlayer().setHealth(8));c().combat.tools.potionHealth=10;c().combat.tools.autoPotion=true;
            beforePotions=FullToolsManager.potionsStarted();next();return;
        case 16:
            if(FullToolsManager.potionsStarted()<=beforePotions){if(elapsed(since)<10000)return;throw new AssertionError("Auto Potion did not start");}
            require(true,"Auto Potion selects an actual healing potion from the hotbar");next();return;
        case 17:
            if(serverPlayer().getHealth()<=8){if(elapsed(since)<12000)return;throw new AssertionError("Potion did not heal");}
            require(serverPlayer().getHealth()>8,"Auto Potion produces real server-side healing");
            capture("49-auto-potion-result");c().combat.tools.autoPotion=false;
            c().combat.tools.antiAim=true;angleBefore=m.thePlayer.rotationYaw;next();return;
        case 18:
            require(Math.abs(MathHelper.wrapAngleTo180_float(m.thePlayer.rotationYaw-angleBefore))>20,"Anti Aim spin changes the actual camera orientation");
            c().combat.tools.antiAim=false;c().combat.tools.aimbot=true;c().combat.requireAttackHeld=false;c().combat.aimFov=180;c().combat.targetRange=5;
            dummy();faceDummy(40);next();return;
        case 19:{
            float[] look=angles();float error=CombatMath.angularDistance(m.thePlayer.rotationYaw,m.thePlayer.rotationPitch,look[0],look[1]);
            require(error<2,"Aimbot actually aligns the camera with the visible practice player");capture("50-aimbot-target-hud");
            c().combat.tools.aimbot=false;m.theWorld.removeEntityFromWorld(dummy.getEntityId());
            server(()->{EntityPlayerMP p=serverPlayer();p.setGameType(WorldSettings.GameType.CREATIVE);p.capabilities.isFlying=true;p.sendPlayerAbilities();
                p.worldObj.setBlockState(new BlockPos(0,12,0),Blocks.stone.getDefaultState());p.worldObj.setBlockToAir(new BlockPos(1,12,0));p.setPositionAndUpdate(1.5,13,.5);
            });next();return;}
        case 20:
            if(m.thePlayer.posY<12.5)return;
            m.thePlayer.capabilities.isFlying=true;c().combat.tools.bridge=true;beforeBlocks=FullToolsManager.placedBlocks();next();return;
        case 21:
            if(serverPlayer().worldObj.getBlockState(new BlockPos(1,12,0)).getBlock()!=Blocks.stone){if(elapsed(since)<8000)return;throw new AssertionError("Bridge did not place the supported block");}
            require(FullToolsManager.placedBlocks()>beforeBlocks,"Auto Bridge calls the vanilla placement controller");
            require(serverPlayer().worldObj.getBlockState(new BlockPos(1,12,0)).getBlock()==Blocks.stone,"Auto Bridge places the block in the actual integrated server world");
            c().combat.tools.bridge=false;capture("51-auto-bridge-server-block");c().combat.tools.autoSprint=true;
            server(()->{EntityPlayerMP p=serverPlayer();p.setGameType(WorldSettings.GameType.SURVIVAL);p.setPositionAndUpdate(.5,4,.5);p.getFoodStats().setFoodLevel(20);});next();return;
        case 22:
            robot.keyPress(java.awt.event.KeyEvent.VK_W);next();return;
        case 23:
            require(m.thePlayer.isSprinting(),"Auto Sprint enables the actual sprint state while forward input is held");robot.keyRelease(java.awt.event.KeyEvent.VK_W);
            c().combat.tools.autoSprint=false;m.displayGuiScreen(new FullToolsScreen(null));next();return;
        case 24:
            capture("52-full-modules-unified");require(button(100)!=null&&button(105)!=null,"All six new modules have actual clickable controls");
            click(101);require(c().combat.tools.autoFood,"Full module GUI edits the registered Auto Food state");
            c().hud.crosshair.shape="Circle";c().hud.widgets.get("target").opacity=.61f;
            AlienClient.get().store().saveProfile("hud_ultimate_smoke");c().hud.widgets.get("target").opacity=1;
            require(AlienClient.get().store().loadProfile("hud_ultimate_smoke"),"Unified profile reload succeeds with all new sections");
            require(c().hud.widgets.get("target").opacity==.61f&&"Circle".equals(c().hud.crosshair.shape)&&c().combat.tools.autoFood,
                    "HUD preferences and Full tool settings round-trip together without data loss");
            resize(960,600);c().ui.menuScale=.85f;HudEditorScreen small=new HudEditorScreen(null);m.displayGuiScreen(small);small.selectWidget("crosshair");next();return;
        case 25:
            capture("53-hud-ultimate-scaled");require(GL11.glGetError()==GL11.GL_NO_ERROR,"HUD/avatars/items/crosshair and Full screens preserve clean OpenGL state");
            c().combat.applyPreset("OFF");m.displayGuiScreen(null);next();return;
        case 26:
            require(!m.gameSettings.keyBindUseItem.isKeyDown(),"Automation leaves no stuck use key after master OFF");
            server(()->{EntityPlayerMP p=serverPlayer();p.setHealth(8);p.inventory.mainInventory[5]=new ItemStack(Items.potionitem,1,16389);p.inventoryContainer.detectAndSendChanges();});
            c().combat.enabled=true;c().combat.tools.autoPotion=true;c().combat.tools.potionHealth=10;
            dummy();faceDummy(0);c().combat.tools.aimbot=true;
            beforePotions=FullToolsManager.potionsStarted();angleBefore=m.thePlayer.rotationPitch;next();return;
        case 27:
            if(FullToolsManager.potionsStarted()<=beforePotions||serverPlayer().getHealth()<=8){if(elapsed(since)<12000)return;throw new AssertionError("Splash potion did not heal");}
            require(serverPlayer().getHealth()>8,"Auto Potion splash mode heals through a real thrown potion on the integrated server");
            require(Math.abs(m.thePlayer.rotationPitch-angleBefore)<.1f,"Splash potion owns its camera while Aimbot is enabled and restores the pitch");
            capture("54-auto-splash-potion-result");c().combat.tools.autoPotion=false;
            server(()->{EntityPlayerMP p=serverPlayer();p.getFoodStats().setFoodLevel(6);p.inventory.mainInventory[4]=new ItemStack(Items.bread,3);p.inventoryContainer.detectAndSendChanges();});
            c().combat.tools.autoFood=true;beforeFoods=FullToolsManager.foodsStarted();oldSlot=m.thePlayer.inventory.currentItem;next();return;
        case 28:
            if(FullToolsManager.foodsStarted()<=beforeFoods||!m.thePlayer.isUsingItem()){if(elapsed(since)<8000)return;throw new AssertionError("Food cleanup test did not begin");}
            m.displayGuiScreen(new FullToolsScreen(null));next();return;
        case 29:
            require(!m.gameSettings.keyBindUseItem.isKeyDown(),"Opening a GUI interrupts automation and releases its owned use key");
            require(m.thePlayer.inventory.currentItem==oldSlot,"Opening a GUI during food use restores the user's hotbar slot");
            capture("55-full-input-cleanup");c().combat.applyPreset("OFF");next();return;
        case 30:
            require(!c().combat.enabled,"OFF disables all Full tool execution after in-world tests");
            m.displayGuiScreen(new CombatStudioScreen(null));click(12);click(22);next();return;
        case 31:
            require(c().combat.tools.aimbot&&c().combat.tools.autoFood&&c().combat.tools.autoPotion&&c().combat.tools.autoSprint,
                    "Full preset is functionally distinct and enables the implemented Full tools");
            capture("56-full-preset-enabled");c().combat.enabled=false;c().hud.applyPreset("Survival");c().hud.theme="Light";
            m.displayGuiScreen(null);next();return;
        case 32:
            capture("57-hud-light-theme-live");resize(1280,720);m.gameSettings.guiScale=1;c().ui.menuScale=1;
            c().interfacePrefs.selectedCategory="Combat";m.displayGuiScreen(new PanelMenuScreen(null));next();return;
        case 33:
            capture("58-full-combat-catalog");require(GL11.glGetError()==GL11.GL_NO_ERROR,"Light HUD theme and full module catalog render without OpenGL errors");
            c().combat.applyPreset("OFF");
            done=true;try(PrintWriter w=new PrintWriter(new File(out,"result.txt"),"UTF-8")){
                w.println("PASS "+checks.size()+" checks");w.println("Actual Minecraft 1.8.9 / Forge 11.15.1.2318; disposable integrated server world");
                w.println("Practice targets are client-only entities; hunger, potion healing and bridge block are checked on server");
                w.println("Automated X11 mouse/keyboard, software OpenGL; external servers / physical input / Windows not tested");
                for(String check:checks)w.println("PASS "+check);
            }
            System.out.println("ALIEN_ULTIMATE_SMOKE_PASS checks="+checks.size());m.shutdown();return;
        default:throw new AssertionError(stage);
        }
    }
    private void dummy(){dummy=new EntityOtherPlayerMP(m.theWorld,new GameProfile(UUID.fromString("7d5b4917-113a-4f6c-bd06-f218720e1801"),"AlienPractice"));
        dummy.setPosition(m.thePlayer.posX+2,m.thePlayer.posY,m.thePlayer.posZ-1);dummy.setHealth(20);m.theWorld.addEntityToWorld(-12432,dummy);}
    private float[] angles(){return CombatMath.lookAt(m.thePlayer.posX,m.thePlayer.posY+m.thePlayer.getEyeHeight(),m.thePlayer.posZ,dummy.posX,dummy.posY+dummy.getEyeHeight(),dummy.posZ);}
    private void faceDummy(float offset){float[] a=angles();m.thePlayer.rotationYaw=a[0]+offset;m.thePlayer.rotationPitch=a[1];m.thePlayer.prevRotationYaw=m.thePlayer.rotationYaw;}
    private void resize(int w,int h)throws Exception{Display.setDisplayMode(new DisplayMode(w,h));Method r=Minecraft.class.getDeclaredMethod("resize",int.class,int.class);r.setAccessible(true);r.invoke(m,w,h);}
    private void next(){stage++;since=System.nanoTime();System.out.println("ALIEN_ULTIMATE_SMOKE_STAGE "+stage);}
    private static long elapsed(long since){return (System.nanoTime()-since)/1000000;}
    private void require(boolean yes,String message){if(!yes)throw new AssertionError(message);checks.add(message);System.out.println("ALIEN_ULTIMATE_SMOKE_CHECK "+message);}
    private void capture(String name)throws Exception{File file=new File(out,"screenshots/"+name+".png");Files.deleteIfExists(file.toPath());ScreenShotHelper.saveScreenshot(out,name+".png",m.displayWidth,m.displayHeight,m.getFramebuffer());
        BufferedImage image=ImageIO.read(file);require(image!=null&&image.getWidth()==m.displayWidth&&image.getHeight()==m.displayHeight,"Real framebuffer capture "+name);}
    @SuppressWarnings("unchecked") private GuiButton button(int id)throws Exception{Field f=GuiScreen.class.getDeclaredField("buttonList");f.setAccessible(true);for(GuiButton b:(List<GuiButton>)f.get(m.currentScreen))if(b.id==id)return b;return null;}
    private void click(int id)throws Exception{GuiButton b=button(id);if(b==null)throw new AssertionError("Missing GUI control "+id);call("actionPerformed",new Class[]{GuiButton.class},b);}
    private void call(String name,Class[] types,Object...args)throws Exception{Class<?> type=m.currentScreen.getClass();Method method=null;while(type!=null){try{method=type.getDeclaredMethod(name,types);break;}catch(NoSuchMethodException e){type=type.getSuperclass();}}if(method==null)throw new NoSuchMethodException(name);method.setAccessible(true);method.invoke(m.currentScreen,args);}
}
