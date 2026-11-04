package dev.alien.client.showcase;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import dev.alien.client.AlienClient;
import dev.alien.client.emote.EmoteManager;
import dev.alien.client.combat.*;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.cosmetic.*;
import dev.alien.client.cosmetic.aura.*;
import dev.alien.client.gui.Ui;
import dev.alien.client.hud.*;
import dev.alien.client.scene.SceneShaders;
import dev.alien.client.world.*;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.*;

/** Test-only continuous actual-game film. Never included in the shipping client JAR. */
@Mod(modid="alien_showcase",name="Alien local showcase fixture",version="1",clientSideOnly=true,dependencies="required-after:alienclient")
public final class AlienShowcase {
    private static final String[] TITLES={"01 / Capa animada","02 / Chapeus e acessorios","03 / Trilhas desenhadas","04 / Particulas Sakura","05 / Ceu realista e nuvens suaves","06 / HUD Ultimate","07 / Floss, Orange Justice, Take the L","08 / Auras reativas","09 / Modo FULL","10 / Aimbot - alvo de treino","11 / Auto Food","12 / Auto Potion","13 / AutoBridge"};
    private static final double[] LENGTHS={8,8,8,8,8,8,12,12,4,6,7,7,9};
    private Minecraft m;private File out;private Robot robot;private long started,warm,filmStart,chapterStart,lastFrame;private int chapter=-2,change=-1;private boolean launched,prepared,ready,finished,previewPrepared;
    private CosmeticStudioScreen studio;private EntityOtherPlayerMP target;
    private final List<Double> frameTimes=new ArrayList<Double>();private final List<String> checks=new ArrayList<String>();private final List<Map<String,Object>> chapters=new ArrayList<Map<String,Object>>();
    private long foods,potions,blocks;private boolean movementSeen,emoteSeen,nightSeen,aimSeen;private float aimStart;private int foodStart;private float healthStart;
    private ClientConfig c(){return AlienClient.get().config();}private CosmeticConfig.Accessory a(){return c().cosmetics.self;}
    @Mod.EventHandler public void init(FMLInitializationEvent event)throws Exception{
        if(!Boolean.getBoolean("alien.showcase"))return;m=Minecraft.getMinecraft();out=new File(System.getProperty("alien.showcase.output"));out.mkdirs();
        for(String f:new String[]{"ready.json","recording.start","capture.done","recording.stopped","result.json","failure.txt"})Files.deleteIfExists(new File(out,f).toPath());
        AlienClient.get().store().resetDefaults();robot=new Robot();started=System.nanoTime();FMLCommonHandler.instance().bus().register(this);net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }
    private double seconds(long since){return (System.nanoTime()-since)/1e9;}
    private EntityPlayerMP player(){return m.getIntegratedServer().getConfigurationManager().playerEntityList.get(0);}
    private void server(Runnable work)throws Exception{m.getIntegratedServer().addScheduledTask(work).get();}
    private void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add(text);}
    @SubscribeEvent public void frame(TickEvent.RenderTickEvent event){
        if(m==null||event.phase!=TickEvent.Phase.END||finished)return;
        try{
            if(seconds(started)>360)throw new AssertionError("Showcase deadline at "+chapter);
            if(!launched){if(m.currentScreen==null)return;resize();m.gameSettings.pauseOnLostFocus=false;m.launchIntegratedServer("AlienShowcase-"+System.currentTimeMillis(),"Disposable local film",new WorldSettings(42,WorldSettings.GameType.SURVIVAL,false,false,WorldType.FLAT));launched=true;return;}
            if(m.thePlayer==null||m.thePlayer.ticksExisted<45)return;
            if(!prepared){prepare();prepared=true;warm=System.nanoTime();return;}
            if(!ready){
                if(seconds(warm)<3)return;
                check(SceneShaders.status().startsWith("Ready"),"Scene shader compiled before recording");c().scenes.enabled=false;c().effects.enabled=false;a().trail=false;a().aura=false;
                if(!previewPrepared){enter(0);chapter=-2;previewPrepared=true;return;}
                json("ready.json",map("x",Display.getX(),"y",Display.getY(),"width",m.displayWidth,"height",m.displayHeight));ready=true;return;
            }
            if(chapter==-2){if(!new File(out,"recording.start").isFile())return;filmStart=System.nanoTime();enter(0);lastFrame=System.nanoTime();return;}
            if(chapter==TITLES.length){if(new File(out,"recording.stopped").isFile()){finished=true;m.shutdown();}return;}
            drawCaption();long now=System.nanoTime();frameTimes.add((now-lastFrame)/1e6);lastFrame=now;double t=seconds(chapterStart);animate(t);
            if(t>=LENGTHS[chapter]){validateChapter();if(chapter+1<TITLES.length)enter(chapter+1);else finish();}
        }catch(Throwable failure){try{release();try(PrintWriter w=new PrintWriter(new File(out,"failure.txt"),"UTF-8")){failure.printStackTrace(w);}}catch(Exception ignored){}finished=true;throw new RuntimeException(failure);}
    }
    private void resize()throws Exception{Display.setDisplayMode(new DisplayMode(960,540));java.lang.reflect.Method method=Minecraft.class.getDeclaredMethod("resize",int.class,int.class);method.setAccessible(true);method.invoke(m,960,540);}
    private void prepare()throws Exception{
        m.displayGuiScreen(null);release();m.gameSettings.guiScale=2;m.gameSettings.limitFramerate=60;m.gameSettings.enableVsync=false;m.gameSettings.renderDistanceChunks=2;m.gameSettings.fancyGraphics=false;m.gameSettings.ambientOcclusion=0;m.gameSettings.clouds=0;m.gameSettings.useVbo=true;m.gameSettings.showInventoryAchievementHint=false;m.guiAchievement.clearAchievements();m.renderGlobal.loadRenderers();
        c().ui.blur=0;c().ui.menuScale=1;c().hud.applyPreset("Cinematic");c().performance.adaptiveEnabled=false;c().performance.maxParticles=128;c().performance.maxCosmetics=4;c().performance.preset="Balanced";
        c().world.enabled=false;c().cosmetics.enabled=true;a().enabled=true;a().hat="SAKURA";a().face="NONE";a().back="CAPE";a().color=0xAF8BEB;a().secondaryColor=0x60DCD1;a().capePattern="MOON";a().detailLevel="Balanced";a().animations=true;
        a().aura=true;a().auraDesign.applyPreset("Arcane");a().trail=true;a().trailDesign.applyPreset("Nebula");c().scenes.enabled=true;c().scenes.sky="Realistic";
        server(()->{EntityPlayerMP p=player();p.worldObj.getGameRules().setOrCreateGameRule("doMobSpawning","false");p.worldObj.getGameRules().setOrCreateGameRule("doDaylightCycle","false");p.worldObj.setWorldTime(6000);p.setPositionAndUpdate(.5,4,.5);p.setHealth(20);p.getFoodStats().setFoodLevel(20);p.inventory.mainInventory[4]=new ItemStack(Items.bread,8);p.inventory.mainInventory[5]=new ItemStack(Items.potionitem,2,8197);p.inventory.mainInventory[6]=new ItemStack(Blocks.stone,64);p.inventoryContainer.detectAndSendChanges();for(Object e:new ArrayList<Object>(p.worldObj.loadedEntityList))if(e instanceof net.minecraft.entity.EntityLiving)((net.minecraft.entity.EntityLiving)e).setDead();});
        m.gameSettings.thirdPersonView=2;yaw(0);m.thePlayer.rotationPitch=5;
    }
    private void release(){robot.keyRelease(KeyEvent.VK_W);robot.keyRelease(KeyEvent.VK_D);robot.keyRelease(KeyEvent.VK_SHIFT);robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);KeyBinding.unPressAllKeys();}
    private void yaw(float value){m.thePlayer.rotationYaw=m.thePlayer.prevRotationYaw=m.thePlayer.rotationYawHead=m.thePlayer.prevRotationYawHead=m.thePlayer.renderYawOffset=m.thePlayer.prevRenderYawOffset=value;}
    private void enter(int next)throws Exception{
        release();AlienClient.get().emotes().stop();c().combat.applyPreset("OFF");m.displayGuiScreen(null);a().aura=a().trail=false;c().effects.enabled=false;c().scenes.enabled=false;c().world.enabled=false;c().hud.enabled=false;m.gameSettings.thirdPersonView=2;m.thePlayer.rotationPitch=5;
        chapter=next;change=-1;chapterStart=System.nanoTime();if(filmStart!=0)chapters.add(map("title",TITLES[next],"startSeconds",seconds(filmStart),"plannedSeconds",LENGTHS[next]));System.out.println("ALIEN_SHOWCASE "+TITLES[next]);
        switch(next){
        case 0:a().hat="NONE";a().back="CAPE";studio=new CosmeticStudioScreen(null);m.displayGuiScreen(studio);studio.setPreviewYaw(160);studio.setPreviewZoom(1.12f);break;
        case 1:a().back="NONE";a().hat="SAKURA";studio=new CosmeticStudioScreen(null);m.displayGuiScreen(studio);studio.setPreviewZoom(1.2f);break;
        case 2:a().back="CAPE";a().hat="SAKURA";a().trail=true;a().trailStyle="Ribbon";a().trailDesign.applyPreset("Nebula");yaw(0);robot.keyPress(KeyEvent.VK_W);break;
        case 3:a().back="NONE";ParticlePresets.apply(c().effects,"Sakura");c().effects.budget=40;break;
        case 4:c().world.enabled=true;c().world.timeOverride=true;c().world.visualHour=12;c().scenes.enabled=true;c().scenes.sky="Realistic";m.thePlayer.rotationPitch=-32;m.gameSettings.thirdPersonView=1;break;
        case 5:
            c().hud.applyPreset("PvP");c().hud.theme="Cyan";m.gameSettings.thirdPersonView=0;yaw(0);m.thePlayer.rotationPitch=0;dummy();
            server(()->{EntityPlayerMP p=player();p.inventory.armorInventory[3]=new ItemStack(Items.diamond_helmet,1,70);p.inventory.armorInventory[2]=new ItemStack(Items.diamond_chestplate,1,80);p.inventory.armorInventory[1]=new ItemStack(Items.diamond_leggings,1,150);p.inventory.armorInventory[0]=new ItemStack(Items.diamond_boots,1,100);p.inventoryContainer.detectAndSendChanges();});break;
        case 6:
            if(target!=null)m.theWorld.removeEntityFromWorld(target.getEntityId());a().back="CAPE";yaw(0);m.thePlayer.rotationPitch=0;server(()->{for(int i=0;i<4;i++)player().inventory.armorInventory[i]=null;player().inventoryContainer.detectAndSendChanges();});AlienClient.get().emotes().request("floss");break;
        case 7:a().aura=true;a().back="NONE";a().auraDesign.applyPreset("Neon");yaw(0);robot.keyPress(KeyEvent.VK_W);break;
        case 8:c().combat.applyPreset("FULL");m.displayGuiScreen(new dev.alien.client.gui.CombatStudioScreen(null));server(()->player().worldObj.setWorldTime(6000));break;
        case 9:c().combat.applyPreset("FULL");c().combat.enabled=false;c().hud.applyPreset("PvP");m.gameSettings.thirdPersonView=0;m.thePlayer.rotationPitch=0;dummy();yaw(38);aimStart=aimError();break;
        case 10:
            if(target!=null)m.theWorld.removeEntityFromWorld(target.getEntityId());m.gameSettings.thirdPersonView=0;c().hud.applyPreset("Survival");c().combat.applyPreset("FULL");foods=FullToolsManager.foodsStarted();
            server(()->{player().getFoodStats().setFoodLevel(6);player().setHealth(20);});foodStart=6;break;
        case 11:
            m.gameSettings.thirdPersonView=0;c().hud.applyPreset("Survival");c().combat.applyPreset("FULL");potions=FullToolsManager.potionsStarted();
            server(()->{player().getFoodStats().setFoodLevel(20);player().setHealth(8);player().inventory.mainInventory[5]=new ItemStack(Items.potionitem,2,8197);player().inventoryContainer.detectAndSendChanges();});healthStart=8;break;
        case 12:
            m.gameSettings.thirdPersonView=0;c().hud.applyPreset("Survival");blocks=FullToolsManager.placedBlocks();
            server(()->{EntityPlayerMP p=player();p.setHealth(20);p.getFoodStats().setFoodLevel(20);p.worldObj.setBlockState(new BlockPos(0,12,0),Blocks.stone.getDefaultState());p.setPositionAndUpdate(.5,13,.5);p.inventory.mainInventory[6]=new ItemStack(Blocks.stone,64);p.inventoryContainer.detectAndSendChanges();});
            c().combat.applyPreset("FULL");c().combat.tools.bridge=true;yaw(0);m.thePlayer.rotationPitch=77;break;
        default:throw new AssertionError(next);
        }
    }
    private void animate(double t)throws Exception{
        if(chapter==0)studio.setPreviewYaw((float)(145+t*5));
        else if(chapter==1){studio.setPreviewYaw((float)(-35+t*8));int part=Math.min(2,(int)(t/2.7));if(part!=change){a().hat=new String[]{"SAKURA","CROWN","SAMURAI"}[part];change=part;}}
        else if(chapter==2)yaw((float)(t*32));
        else if(chapter==3)yaw((float)(-20+t*5));
        else if(chapter==4){yaw((float)(t*8));m.thePlayer.rotationPitch=(float)(-28-t*3);}
        else if(chapter==5){if(t>2&&change<0){target.setHealth(8);change=0;}UltimateHud.observe(target);}
        else if(chapter==6){int part=Math.min(2,(int)(t/4));if(part!=change){AlienClient.get().emotes().request(new String[]{"floss","orange_justice","take_the_l"}[part]);change=part;}}
        else if(chapter==7){int part=Math.min(2,(int)(t/4));if(part!=change){if(part==1){release();a().auraDesign.applyPreset("Celestial");AlienClient.get().emotes().request("floss");}else if(part==2){AlienClient.get().emotes().stop();a().auraDesign.applyPreset("Arcane");server(()->player().worldObj.setWorldTime(18000));}change=part;}
            if(part==0){yaw((float)(t*25));movementSeen|=ReactiveAuras.selfMovement()>.5f;}if(part==1)emoteSeen|=ReactiveAuras.selfEmote()>.8f;if(part==2)nightSeen|=ReactiveAuras.selfNight()>.8f;
        }else if(chapter==9){if(t>1.5)c().combat.enabled=true;if(t>3)aimSeen|=aimError()<2;}
        else if(chapter==12){m.thePlayer.rotationPitch=m.thePlayer.prevRotationPitch=77;if(t>1&&change<0){robot.keyPress(KeyEvent.VK_SHIFT);robot.keyPress(KeyEvent.VK_W);change=0;}}
    }
    private void dummy(){target=new EntityOtherPlayerMP(m.theWorld,new GameProfile(UUID.fromString("72e00e09-3bf8-4c80-8d20-076b62f2a239"),"AlienTreino"));target.setPosition(m.thePlayer.posX,m.thePlayer.posY,m.thePlayer.posZ+3);target.setHealth(20);m.theWorld.addEntityToWorld(-7723,target);}
    private float aimError(){float[] targetAngles=CombatMath.lookAt(m.thePlayer.posX,m.thePlayer.posY+m.thePlayer.getEyeHeight(),m.thePlayer.posZ,target.posX,target.posY+target.getEyeHeight(),target.posZ);return CombatMath.angularDistance(m.thePlayer.rotationYaw,m.thePlayer.rotationPitch,targetAngles[0],targetAngles[1]);}
    private void validateChapter(){
        if(chapter==2)check(CosmeticManager.texturedRibbonSegments()>0,"Actual native movement renders designed ribbon geometry");
        if(chapter==6)check(AlienClient.get().emotes().posedFrames()>0&&EmoteManager.articulatedDraws()>0,"Emotes reach actual articulated body model rendering");
        if(chapter==7){check(movementSeen&&emoteSeen&&nightSeen,"Aura film records actual movement, emote and midnight responses");check(ReactiveAuras.drawnUnits()>0,"Aura geometry reaches the actual world framebuffer");}
        if(chapter==9)check(aimStart>20&&aimSeen,"Aimbot actually moves the camera from an offset to the visible practice target");
        if(chapter==10)check(FullToolsManager.foodsStarted()>foods&&player().getFoodStats().getFoodLevel()>foodStart,"Auto Food uses real bread and increases integrated-server hunger");
        if(chapter==11)check(FullToolsManager.potionsStarted()>potions&&player().getHealth()>healthStart,"Auto Potion actually heals the integrated-server player");
        if(chapter==12)check(FullToolsManager.placedBlocks()>blocks+2,"AutoBridge places multiple real blocks while walking in Survival");
        check(GL11.glGetError()==GL11.GL_NO_ERROR,"Clean OpenGL after "+TITLES[chapter]);
    }
    private void drawCaption(){
        if(chapter<0||chapter>=TITLES.length)return;m.entityRenderer.setupOverlayRendering();
        ScaledResolution sr=new ScaledResolution(m);String text=TITLES[chapter];Ui.rounded(sr.getScaledWidth()/2f-137,5,274,32,5,0xe8161822);Ui.centered(text,sr.getScaledWidth()/2f,10,0xfff1efff);Ui.centered("Demonstracao automatizada / mundo local",sr.getScaledWidth()/2f,23,0xffaaa8bf);Ui.finishFrame();
    }
    private void finish()throws Exception{
        release();c().combat.applyPreset("OFF");AlienClient.get().emotes().stop();chapter=TITLES.length;
        List<Double> sorted=new ArrayList<Double>(frameTimes);Collections.sort(sorted);double median=sorted.get(sorted.size()/2),p95=sorted.get((int)(sorted.size()*.95));
        json("result.json",map("status","PASS","sourceCommit",System.getProperty("alien.showcase.source","unknown"),"minecraft","1.8.9","forge","11.15.1.2318","width",m.displayWidth,"height",m.displayHeight,"seconds",seconds(filmStart),"renderedFrames",frameTimes.size(),"medianFrameMs",median,"p95FrameMs",p95,"medianRenderedFps",1000/median,"chapters",chapters,"checks",checks,"capture","Continuous native X11 recording; no generated or interpolated frames","environment","Linux / software OpenGL / disposable integrated-server world","microsoftAuthenticationTested",false));
        Files.write(new File(out,"capture.done").toPath(),new byte[]{1});
    }
    private Map<String,Object> map(Object...kv){Map<String,Object> m=new LinkedHashMap<String,Object>();for(int i=0;i<kv.length;i+=2)m.put((String)kv[i],kv[i+1]);return m;}
    private void json(String name,Object value)throws Exception{try(Writer writer=new OutputStreamWriter(new FileOutputStream(new File(out,name)),"UTF-8")){new GsonBuilder().setPrettyPrinting().create().toJson(value,writer);}}
}
