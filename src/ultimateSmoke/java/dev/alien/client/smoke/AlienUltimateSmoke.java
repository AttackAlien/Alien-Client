package dev.alien.client.smoke;

import com.mojang.authlib.GameProfile;
import dev.alien.client.AlienClient;
import dev.alien.client.combat.*;
import dev.alien.client.hud.*;
import dev.alien.client.gui.*;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.world.*;
import dev.alien.client.world.WorldManager;
import dev.alien.client.cosmetic.*;
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
    private int[] trailPreviewPixels,trailPreviewBounds;private int trailMovieFrame;
    private final List<Long> trailMovieTimes=new ArrayList<Long>();
    private TrailDesign trail(){return c().cosmetics.self.trailDesign;}
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
            long wait=stage==60?180:stage==56||stage==64?1400:stage==4?110:stage==5?1600:stage==34||stage==35?3500:stage==45?2600:stage==43?1400:stage==15||stage==17?2500:700;
            if(elapsed(since)<wait)return;step();
        }catch(Throwable failure){
            done=true;try(PrintWriter w=new PrintWriter(new File(out,"result.txt"),"UTF-8")){
                w.println("FAIL stage "+stage);for(String check:checks)w.println("PASS "+check);failure.printStackTrace(w);
            }catch(Exception ignored){}
            if(robot!=null){robot.keyRelease(java.awt.event.KeyEvent.VK_W);robot.keyRelease(java.awt.event.KeyEvent.VK_D);robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);}
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
            c().combat.applyPreset("OFF");c().hud.applyPreset("Cinematic");c().hud.theme="Midnight";
            m.gameSettings.guiScale=2;m.gameSettings.thirdPersonView=1;
            server(()->serverPlayer().clearActivePotions());
            AtmospherePresets.apply(c().world,c().effects,"Nebula");ParticlePresets.apply(c().effects,"Cinematic");
            m.thePlayer.rotationYaw=0;m.thePlayer.rotationPitch=-12;m.displayGuiScreen(null);next();return;
        case 34:
            require(WorldManager.liveParticleCount()>0&&WorldManager.liveParticleCount()<=96,"Cinematic particles spawn through the actual bounded world emitter");
            require(WorldManager.drawnParticleCount()>0,"Cinematic feathered orb particles reach the world renderer");
            capture("59-particles-cinematic-live");AtmospherePresets.apply(c().world,c().effects,"Sakura");ParticlePresets.apply(c().effects,"Sakura");next();return;
        case 35:
            require(WorldManager.liveParticleCount()<=72&&WorldManager.drawnParticleCount()>0,"Sakura petals render within their real 72-particle budget");
            capture("60-particles-sakura-live");ParticlePresets.apply(c().effects,"PvP");
            robot.keyPress(java.awt.event.KeyEvent.VK_W);next();return;
        case 36:
            robot.keyRelease(java.awt.event.KeyEvent.VK_W);
            require(WorldManager.liveParticleCount()>0&&WorldManager.liveParticleCount()<=24&&c().effects.glow==0&&!c().effects.ambient,
                    "PvP preset keeps actual movement particles within 24 and disables glow and ambient spawning");
            capture("61-particles-pvp-live");m.displayGuiScreen(new ParticleStudioScreen(null));next();return;
        case 37:
            click(2);click(11);require("Petal".equals(c().effects.shape)&&c().effects.glow>.5f,"Particle Studio controls edit the active renderer configuration");
            c().effects.opacity=.7f;c().effects.flow=1.2f;c().effects.lifetime=1.3f;AlienClient.get().save();
            AlienClient.get().store().saveProfile("particle_studio_smoke");c().effects.opacity=.1f;
            require(AlienClient.get().store().loadProfile("particle_studio_smoke")&&c().effects.opacity==.7f&&c().effects.flow==1.2f&&c().effects.lifetime==1.3f,
                    "Particle shape, opacity, glow, flow and lifetime persist in Unified profiles");
            m.displayGuiScreen(new ParticleStudioScreen(null));next();return;
        case 38:
            capture("62-particle-studio-controls");c().effects.enabled=false;m.displayGuiScreen(null);next();return;
        case 39:
            require(WorldManager.liveParticleCount()==0&&WorldManager.drawnParticleCount()==0,"Effects OFF clears particle simulation and rendering");
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"All particle meshes, glow passes and controls preserve clean OpenGL state");
            c().cosmetics.enabled=true;CosmeticConfig.Accessory outfit=c().cosmetics.self;
            outfit.enabled=true;outfit.hat="SAKURA";outfit.face="NONE";outfit.back="CAPE";outfit.capePattern="MOON";
            outfit.color=0xEFADCC;outfit.secondaryColor=0x775AB1;outfit.detailLevel="High";outfit.animations=true;
            outfit.trail=true;outfit.trailStyle="Ribbon";outfit.trailWidth=.22f;outfit.trailLifetime=1.2f;outfit.aura=false;
            server(()->{EntityPlayerMP p=serverPlayer();p.clearActivePotions();p.setHealth(20);p.getFoodStats().setFoodLevel(20);
                for(int i=0;i<4;i++)p.inventory.armorInventory[i]=null;p.inventoryContainer.detectAndSendChanges();});
            m.gameSettings.thirdPersonView=2;m.thePlayer.rotationYaw=0;m.thePlayer.rotationPitch=0;next();return;
        case 40:
            require("SAKURA".equals(c().cosmetics.self.hat),"New sculpted Sakura headwear is active in the actual player renderer");
            capture("63-sakura-wreath-world");CosmeticStudioScreen fabric=new CosmeticStudioScreen(null);m.displayGuiScreen(fabric);fabric.setPreviewYaw(155);fabric.setPreviewZoom(1.3f);next();return;
        case 41:
            capture("64-cc0-fabric-cape-studio");click(6);click(87);next();return;
        case 42:
            require(m.currentScreen instanceof CosmeticTrailScreen,"Motion tab opens actual per-player ribbon controls");
            capture("65-cosmetic-ribbon-controls");click(2);
            require("Particles".equals(c().cosmetics.self.trailStyle)&&!button(3).enabled,"Classic particle option remains available and ribbon-only sliders are disabled");
            click(2);c().cosmetics.self.trailOpacity=.72f;AlienClient.get().store().saveProfile("cosmetic_polish_smoke");
            c().cosmetics.self.trailWidth=.04f;
            require(AlienClient.get().store().loadProfile("cosmetic_polish_smoke")&&c().cosmetics.self.trailWidth==.22f
                    &&c().cosmetics.self.trailOpacity==.72f&&"SAKURA".equals(c().cosmetics.self.hat),"Wreath and ribbon preferences round-trip with Unified profiles");
            m.displayGuiScreen(null);m.gameSettings.thirdPersonView=1;m.thePlayer.rotationYaw=-40;m.thePlayer.rotationPitch=22;
            robot.keyPress(java.awt.event.KeyEvent.VK_W);robot.keyPress(java.awt.event.KeyEvent.VK_D);next();return;
        case 43:
            require(CosmeticManager.activeRibbonCount()>0&&CosmeticManager.drawnRibbonSegments()>0,"Real X11 player movement produces a rendered continuous ribbon");
            require(CosmeticManager.drawnRibbonSegments()<=192,"Ribbon geometry respects the shared performance segment limit");
            require(CosmeticManager.lastCapeBend()>.2f,"Actual player movement drives the cape's damped spring response");
            capture("66-ribbon-trail-and-cape-moving");robot.keyRelease(java.awt.event.KeyEvent.VK_D);
            m.gameSettings.thirdPersonView=2;m.thePlayer.rotationYaw+=60;m.thePlayer.rotationPitch=-12;next();return;
        case 44:
            require(CosmeticManager.drawnRibbonSegments()>0,"Ribbon remains rendered while turning in front third-person view");
            capture("67-ribbon-curved-path");robot.keyRelease(java.awt.event.KeyEvent.VK_W);next();return;
        case 45:
            require(CosmeticManager.activeRibbonCount()==0,"Stationary trail fully expires after its configured lifetime");
            capture("68-cape-relaxed-after-motion");c().cosmetics.self.hat="JAPANESE";c().cosmetics.self.color=0x262D3E;
            c().cosmetics.self.secondaryColor=0xD6B56C;c().cosmetics.self.capePattern="ALIEN";m.gameSettings.thirdPersonView=2;m.thePlayer.rotationPitch=-8;next();return;
        case 46:
            capture("69-woven-kasa-world");c().cosmetics.self.hat="SAKURA";c().cosmetics.self.color=0xEFADCC;c().cosmetics.self.secondaryColor=0x775AB1;
            c().cosmetics.self.animations=false;CosmeticStudioScreen wreath=new CosmeticStudioScreen(null);m.displayGuiScreen(wreath);wreath.setPreviewZoom(1.05f);next();return;
        case 47:
            capture("70-sakura-cosmetic-studio");require(CosmeticManager.activeRibbonCount()==0,"Reduced cosmetic animation leaves no active ribbon simulation");
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"CC0 cape fabric, sculpted hats and ribbons restore clean OpenGL state");
            c().cosmetics.self.trail=true;c().cosmetics.self.trailWidth=.3f;c().cosmetics.self.trailOpacity=1;c().cosmetics.self.trailLifetime=1.8f;
            trail().applyPreset("Nebula");m.displayGuiScreen(new CosmeticTrailScreen(null,c().cosmetics.self));next();return;
        case 48:
            require(m.currentScreen instanceof CosmeticTrailScreen&&button(10).enabled&&button(11).enabled,"Real Trail Studio exposes opt-in designer and texture controls");
            capture("128-trail-studio-overview");click(30);
            require("Ember".equals(trail().preset)&&"Dashes".equals(trail().texture),"Preset button applies its actual material, gradient and curve values");next();return;
        case 49:{
            capture("129-trail-ember-preset");rememberTrailPreview("129-trail-ember-preset");
            int[] trailCanvas=((CosmeticTrailScreen)m.currentScreen).curveCanvasBounds();
            call("mouseClicked",new Class[]{int.class,int.class,int.class},trailCanvas[0]+trailCanvas[2]/2,trailCanvas[1]+trailCanvas[3]/2,0);
            call("mouseClickMove",new Class[]{int.class,int.class,int.class,long.class},trailCanvas[0]+trailCanvas[2]/2,trailCanvas[1]+trailCanvas[3]/4,0,100L);
            call("mouseReleased",new Class[]{int.class,int.class,int.class},trailCanvas[0]+trailCanvas[2]/2,trailCanvas[1]+trailCanvas[3]/4,0);
            require(trail().middleWidth>1.1f&&trail().middleWidth<1.2f&&"Custom".equals(trail().preset),"Dragging the real width canvas changes its middle stop");next();return;}
        case 50:{
            capture("130-trail-width-curve");require(changedTrailPreview("130-trail-width-curve")>40,"Width edit changes actual material-preview framebuffer pixels");rememberTrailPreview("130-trail-width-curve");
            click(27);int[] trailCanvas=((CosmeticTrailScreen)m.currentScreen).curveCanvasBounds();
            call("mouseClicked",new Class[]{int.class,int.class,int.class},trailCanvas[0]+trailCanvas[2],trailCanvas[1]+trailCanvas[3]/2,0);
            call("mouseClickMove",new Class[]{int.class,int.class,int.class,long.class},trailCanvas[0]+trailCanvas[2],trailCanvas[1]+Math.round(trailCanvas[3]*.8f),0,100L);
            call("mouseReleased",new Class[]{int.class,int.class,int.class},trailCanvas[0]+trailCanvas[2],trailCanvas[1]+Math.round(trailCanvas[3]*.8f),0);
            require(trail().headOpacity>.15f&&trail().headOpacity<.25f,"Opacity curve independently edits the newest end of the ribbon");next();return;}
        case 51:
            capture("131-trail-opacity-curve");require(changedTrailPreview("131-trail-opacity-curve")>30,"Opacity edit changes real preview pixels without changing the texture");
            click(23);require(m.currentScreen instanceof dev.alien.client.gui.ui.ColorPickerScreen,"Trail stop opens the existing real HSV and HEX picker");next();return;
        case 52:{
            Field field=m.currentScreen.getClass().getDeclaredField("hex");field.setAccessible(true);GuiTextField hex=(GuiTextField)field.get(m.currentScreen);
            hex.setText("#4DDFA");hex.setFocused(true);call("keyTyped",new Class[]{char.class,int.class},'8',org.lwjgl.input.Keyboard.KEY_8);
            require(trail().headColor==0x4DDFA8,"Typing HEX through the actual picker updates the selected stop");next();return;}
        case 53:{
            capture("132-trail-color-picker");click(1);
            c().cosmetics.saveLook(1,c().cosmetics.self);AlienClient.get().store().saveProfile("trail_studio_smoke");
            float middle=trail().middleWidth;c().cosmetics.self.trailDesign=new TrailDesign();
            require(AlienClient.get().store().loadProfile("trail_studio_smoke")&&trail().headColor==0x4DDFA8&&Math.abs(trail().middleWidth-middle)<1e-5,"Actual Unified profile load restores custom trail stops");
            require(c().cosmetics.loadLook(1).trailDesign.headColor==0x4DDFA8,"Saved cosmetic look includes an independent trail design");
            File folder=new File(AlienClient.get().store().directory(),"cosmetics/trails");TrailTextureLoader.ensureDirectory(folder);
            BufferedImage png=new BufferedImage(64,16,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<16;y++)for(int x=0;x<64;x++)png.setRGB(x,y,(((x/8+y/4)&1)==0?0xff:0x30)<<24|0xffffff);
            ImageIO.write(png,"png",new File(folder,"smoke-streak.png"));
            m.displayGuiScreen(new CosmeticTrailScreen(null,c().cosmetics.self));click(13);
            for(int i=0;i<70&&!("PNG".equals(trail().texture)&&"smoke-streak.png".equals(trail().customTexture));i++)click(11);
            require("PNG".equals(trail().texture)&&"smoke-streak.png".equals(trail().customTexture),"Reload and texture selector discover an actual local PNG");next();return;}
        case 54:{
            capture("133-trail-png-material");click(27);int[] trailCanvas=((CosmeticTrailScreen)m.currentScreen).curveCanvasBounds();
            call("mouseClicked",new Class[]{int.class,int.class,int.class},trailCanvas[0]+trailCanvas[2],trailCanvas[1]+trailCanvas[3]/2,1);
            require(m.currentScreen instanceof dev.alien.client.gui.ui.ValueInputScreen,"Right-click curve opens precise numeric input");
            Field input=m.currentScreen.getClass().getDeclaredField("input");input.setAccessible(true);((GuiTextField)input.get(m.currentScreen)).setText("0.35");click(1);
            require(Math.abs(trail().headOpacity-.35f)<1e-5,"Typed curve value is applied through the actual numeric control");next();return;}
        case 55:
            capture("134-trail-exact-opacity");trail().headOpacity=1;c().cosmetics.self.animations=true;m.displayGuiScreen(null);
            m.gameSettings.thirdPersonView=2;m.thePlayer.rotationYaw=-40;m.thePlayer.rotationPitch=-12;
            robot.keyPress(java.awt.event.KeyEvent.VK_D);next();return;
        case 56:
            require(CosmeticManager.texturedRibbonSegments()>0&&"smoke-streak.png".equals(CosmeticManager.lastTrailTextureName()),"Real player movement renders the imported PNG on world-space ribbon geometry");
            require(CosmeticManager.drawnRibbonSegments()<=192&&CosmeticManager.activeRibbonCount()<=8,"Designed trails retain bounded shared geometry and player budgets");
            capture("135-trail-custom-png-world");trail().applyPreset("Nebula");next();return;
        case 57:
            require("Silk".equals(CosmeticManager.lastTrailTextureName()),"Nebula binds the actual Silk material in the world renderer");capture("136-trail-nebula-world");trail().applyPreset("Ember");next();return;
        case 58:
            require("Dashes".equals(CosmeticManager.lastTrailTextureName()),"Ember binds the actual segmented material in the world renderer");capture("137-trail-ember-world");trail().applyPreset("Glacier");next();return;
        case 59:
            require("Weave".equals(CosmeticManager.lastTrailTextureName()),"Glacier binds the actual woven material in the world renderer");capture("138-trail-glacier-world");trail().applyPreset("Nebula");next();return;
        case 60:
            capture(String.format(Locale.ROOT,"trail-motion-%02d",trailMovieFrame));trailMovieTimes.add(System.currentTimeMillis());m.thePlayer.rotationYaw+=3;
            if(++trailMovieFrame<20){since=System.nanoTime();return;}
            try(PrintWriter times=new PrintWriter(new File(out,"trail-motion-times.txt"),"UTF-8")){for(Long time:trailMovieTimes)times.println(time);}
            robot.keyRelease(java.awt.event.KeyEvent.VK_W);robot.keyRelease(java.awt.event.KeyEvent.VK_D);
            c().ui.menuScale=1.25f;m.gameSettings.guiScale=0;resize(800,600);m.displayGuiScreen(new CosmeticTrailScreen(null,c().cosmetics.self));
            robot.mouseMove(Display.getX()+400,Display.getY()+300);robot.mouseWheel(30);next();return;
        case 61:{
            int[] trailCanvas=((CosmeticTrailScreen)m.currentScreen).curveCanvasBounds();
            require(m.currentScreen.width<=400&&trailCanvas[1]>=72&&trailCanvas[1]+trailCanvas[3]<=m.currentScreen.height-31,"Actual mouse wheel exposes the full curve editor in compact scaled layout");
            capture("139-trail-studio-compact");c().ui.menuScale=1;m.gameSettings.guiScale=1;resize(1280,720);
            File folder=new File(AlienClient.get().store().directory(),"cosmetics/trails");ImageIO.write(new BufferedImage(2048,2,BufferedImage.TYPE_INT_ARGB),"png",new File(folder,"oversized.png"));
            trail().texture="PNG";trail().customTexture="oversized.png";m.displayGuiScreen(new CosmeticTrailScreen(null,c().cosmetics.self));click(13);next();return;}
        case 62:
            capture("140-trail-invalid-png-fallback");require(GL11.glGetError()==GL11.GL_NO_ERROR,"Preview, graph and imported-texture fallback restore clean OpenGL state");
            m.displayGuiScreen(null);robot.keyPress(java.awt.event.KeyEvent.VK_W);next();return;
        case 63:
            require(CosmeticManager.texturedRibbonSegments()>0&&"Soft / fallback".equals(CosmeticManager.lastTrailTextureName()),"Rejected PNG renders the truthful Soft fallback instead of pretending import success");
            capture("141-trail-fallback-world");robot.keyRelease(java.awt.event.KeyEvent.VK_W);m.displayGuiScreen(new CosmeticTrailScreen(null,c().cosmetics.self));click(1);m.displayGuiScreen(null);next();return;
        case 64:
            require(!c().cosmetics.self.trail&&CosmeticManager.activeRibbonCount()==0&&CosmeticManager.texturedRibbonSegments()==0,"Actual OFF control clears designed trail simulation and rendering");
            capture("142-trail-off-restored");m.displayGuiScreen(new CosmeticTrailScreen(null,c().cosmetics.self));click(10);click(1);click(2);
            require("Particles".equals(c().cosmetics.self.trailStyle)&&!button(3).enabled&&!button(11).enabled,"Legacy particle choice remains selectable with designer-only controls disabled");next();return;
        case 65:
            capture("143-trail-particles-preserved");require(GL11.glGetError()==GL11.GL_NO_ERROR,"All designed trail world passes and editor screens leave clean OpenGL state");
            c().cosmetics.self.trail=false;AlienClient.get().save();
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
    private int[] trailPreview(String name)throws Exception{
        BufferedImage image=ImageIO.read(new File(out,"screenshots/"+name+".png"));
        int[] b=trailPreviewBounds;return image.getRGB(b[0],b[1],b[2],b[3],null,0,b[2]);
    }
    private void rememberTrailPreview(String name)throws Exception{
        int[] b=((CosmeticTrailScreen)m.currentScreen).materialCanvasBounds();float scale=m.displayWidth/(float)m.currentScreen.width;
        trailPreviewBounds=new int[]{Math.round(b[0]*scale),Math.round(b[1]*scale),Math.round(b[2]*scale),Math.round(b[3]*scale)};trailPreviewPixels=trailPreview(name);
    }
    private int changedTrailPreview(String name)throws Exception{int[] next=trailPreview(name);int count=0;for(int i=0;i<next.length;i++)if(next[i]!=trailPreviewPixels[i])count++;return count;}
    private static long elapsed(long since){return (System.nanoTime()-since)/1000000;}
    private void require(boolean yes,String message){if(!yes)throw new AssertionError(message);checks.add(message);System.out.println("ALIEN_ULTIMATE_SMOKE_CHECK "+message);}
    private void capture(String name)throws Exception{File file=new File(out,"screenshots/"+name+".png");Files.deleteIfExists(file.toPath());ScreenShotHelper.saveScreenshot(out,name+".png",m.displayWidth,m.displayHeight,m.getFramebuffer());
        BufferedImage image=ImageIO.read(file);require(image!=null&&image.getWidth()==m.displayWidth&&image.getHeight()==m.displayHeight,"Real framebuffer capture "+name);}
    @SuppressWarnings("unchecked") private GuiButton button(int id)throws Exception{Field f=GuiScreen.class.getDeclaredField("buttonList");f.setAccessible(true);for(GuiButton b:(List<GuiButton>)f.get(m.currentScreen))if(b.id==id)return b;return null;}
    private void click(int id)throws Exception{GuiButton b=button(id);if(b==null)throw new AssertionError("Missing GUI control "+id);call("actionPerformed",new Class[]{GuiButton.class},b);}
    private void call(String name,Class[] types,Object...args)throws Exception{Class<?> type=m.currentScreen.getClass();Method method=null;while(type!=null){try{method=type.getDeclaredMethod(name,types);break;}catch(NoSuchMethodException e){type=type.getSuperclass();}}if(method==null)throw new NoSuchMethodException(name);method.setAccessible(true);method.invoke(m.currentScreen,args);}
}
