package dev.alien.client.smoke;

import com.mojang.authlib.GameProfile;
import dev.alien.client.AlienClient;
import dev.alien.client.combat.CombatConfig;
import dev.alien.client.combat.CombatMath;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.gui.CombatStudioScreen;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

/** Test-only Forge mod, excluded from every distributable JAR. */
@Mod(modid="alienclient_combatsmoke",name="Alien Unified combat smoke",version="1",
        clientSideOnly=true,dependencies="required-after:alienclient")
public final class AlienCombatSmoke {
    private final List<String> checks=new ArrayList<String>();
    private Minecraft mc;
    private File output;
    private long started,since;
    private int stage;
    private boolean finished;
    private float initialAngle;
    private EntityOtherPlayerMP target;

    @Mod.EventHandler public void init(FMLInitializationEvent event)throws Exception {
        if(!Boolean.getBoolean("alien.combat.smoke"))return;
        mc=Minecraft.getMinecraft();
        output=new File(System.getProperty("alien.combat.smoke.output")).getCanonicalFile();
        Files.createDirectories(output.toPath());
        Files.deleteIfExists(new File(output,"result.txt").toPath());
        AlienClient.get().store().resetDefaults();
        started=since=System.nanoTime();
        FMLCommonHandler.instance().bus().register(this);
        System.out.println("ALIEN_COMBAT_SMOKE_START");
    }

    @SubscribeEvent public void onRender(TickEvent.RenderTickEvent event) {
        if(finished||event.phase!=TickEvent.Phase.END)return;
        try {
            if(elapsed(started)>160000L)throw new AssertionError("Combat smoke exceeded deadline at "+stage);
            if(elapsed(since)<(stage==1?1000L:stage>=4?1900L:650L))return;
            step();
        }catch(Throwable failure){
            finished=true;
            try(PrintWriter writer=new PrintWriter(new File(output,"result.txt"),"UTF-8")){
                writer.println("FAIL at stage "+stage);
                for(String check:checks)writer.println("PASS "+check);
                failure.printStackTrace(writer);
            }catch(Exception ex){ex.printStackTrace();}
            failure.printStackTrace();
            System.err.println("ALIEN_COMBAT_SMOKE_FAIL stage="+stage);
            // Forge 1.8.9 traps calls to System.exit from mod code.
            throw new RuntimeException("Combat Studio integration smoke failed",failure);
        }
    }

    private void step()throws Exception {
        switch(stage) {
            case 0:
                if(mc.currentScreen==null)return;
                require(Loader.isModLoaded("alienclient"),"Production Alien Client is loaded by Forge");
                require("unified".equals(BuildEdition.ID),"One Unified edition is loaded");
                require(AlienClient.get().config().combat!=null,"Old profiles gain safe combat defaults");
                require(!combat().enabled&&!combat().aimAssist&&!combat().clickAssist,
                        "Combat remains fully disabled by default");
                mc.gameSettings.limitFramerate=30;
                mc.launchIntegratedServer("AlienCombat-"+System.currentTimeMillis(),
                        "Disposable Combat Studio smoke",
                        new WorldSettings(41822L,WorldSettings.GameType.CREATIVE,
                                false,false,WorldType.FLAT));
                advance();return;
            case 1:
                if(mc.theWorld==null||mc.thePlayer==null||mc.thePlayer.ticksExisted<40)return;
                require(mc.theWorld.provider.getDimensionId()==0,
                        "Minecraft integrated Overworld loaded in Forge 1.8.9");
                mc.gameSettings.clouds=0;
                mc.gameSettings.renderDistanceChunks=4;
                mc.displayGuiScreen(new CombatStudioScreen(null));
                advance();return;
            case 2:
                require(mc.currentScreen instanceof CombatStudioScreen,
                        "Actual Combat Studio GUI is open");
                require(button(10)!=null&&button(11)!=null&&button(12)!=null
                        &&button(30)!=null&&button(31)!=null&&button(32)!=null,
                        "Presets, master switch, and real combat modules have clickable controls");
                capture("combat-studio-off");
                click(12);
                require("FULL".equals(combat().preset)&&combat().enabled
                        &&combat().aimAssist&&combat().clickAssist,
                        "Full preset enables real aim and attack-held click assistance");
                click(20);
                click(32);
                require("CUSTOM".equals(combat().preset)&&!combat().clickAssist,
                        "Changing a Full preset switch creates an independent Custom profile");
                advance();return;
            case 3:
                click(11);
                require("LEGIT".equals(combat().preset)&&combat().aimAssist
                        &&!combat().clickAssist&&combat().requireAttackHeld,
                        "Legit preset restores subtle manual-attack aim configuration");
                click(21);
                require(button(40)!=null&&button(41)!=null&&button(42)!=null&&button(43)!=null,
                        "All four advanced combat sliders render in the actual GUI");
                capture("combat-studio-tuning");
                click(12);
                AlienClient.get().save();
                AlienClient.get().store().load();
                require("FULL".equals(combat().preset)&&combat().clickAssist
                        &&!combat().requireAttackHeld,
                        "Unified combat configuration persists after real config reload");
                mc.displayGuiScreen(null);
                advance();return;
            case 4:
                // Create a local rendering-only dummy: no external player/server.
                target=new EntityOtherPlayerMP(mc.theWorld,
                        new GameProfile(UUID.fromString("7d5b4917-113a-4f6c-bd06-f218720e1801"),"CombatSmokeDummy"));
                target.setPosition(mc.thePlayer.posX+2D,mc.thePlayer.posY,mc.thePlayer.posZ-1D);
                mc.theWorld.addEntityToWorld(-12721,target);
                require(mc.theWorld.playerEntities.contains(target),"Disposable target is present in world player list");
                CombatConfig c=combat();
                c.applyPreset("FULL");
                c.visibleTargetsOnly=false;
                c.aimStrength=.2F;
                c.aimFov=150F;
                c.targetRange=5F;
                float[] angles=look();
                mc.thePlayer.rotationYaw=angles[0]+45F;
                mc.thePlayer.prevRotationYaw=mc.thePlayer.rotationYaw;
                mc.thePlayer.rotationPitch=angles[1];
                mc.thePlayer.prevRotationPitch=mc.thePlayer.rotationPitch;
                initialAngle=CombatMath.angularDistance(mc.thePlayer.rotationYaw,mc.thePlayer.rotationPitch,
                        angles[0],angles[1]);
                require(initialAngle>30F,"Aim controller starts visibly outside target direction");
                advance();return;
            case 5:
                float[] point=look();
                float remaining=CombatMath.angularDistance(mc.thePlayer.rotationYaw,mc.thePlayer.rotationPitch,
                        point[0],point[1]);
                require(remaining<initialAngle*.5F,
                        "Live combat tick gradually aims towards the visible local dummy");
                require(GL11.glGetError()==GL11.GL_NO_ERROR,"Combat Studio and in-world render have no OpenGL errors");
                capture("combat-target-tracking");
                combat().enabled=false;
                advance();return;
            case 6:
                require(!combat().enabled,"Combat master can be disabled during a world session");
                if(target!=null)mc.theWorld.removeEntityFromWorld(-12721);
                finished=true;
                try(PrintWriter writer=new PrintWriter(new File(output,"result.txt"),"UTF-8")){
                    writer.println("PASS "+checks.size()+" checks");
                    writer.println("Forge 1.8.9 GUI/presets/JSON and real aiming against a disposable local dummy");
                    writer.println("Attack timing with physical mouse input, anti-cheat and multiplayer NOT tested");
                    for(String check:checks)writer.println("PASS "+check);
                }
                System.out.println("ALIEN_COMBAT_SMOKE_PASS checks="+checks.size());
                mc.shutdown();
                return;
            default:throw new AssertionError("Unknown smoke stage "+stage);
        }
    }

    private CombatConfig combat(){return AlienClient.get().config().combat;}
    private float[] look(){
        return CombatMath.lookAt(mc.thePlayer.posX,mc.thePlayer.posY+mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ,target.posX,target.posY+target.getEyeHeight(),target.posZ);
    }
    private void advance(){stage++;since=System.nanoTime();System.out.println("ALIEN_COMBAT_SMOKE_STAGE "+stage);}
    private static long elapsed(long base){return (System.nanoTime()-base)/1000000L;}
    private void require(boolean yes,String label){
        if(!yes)throw new AssertionError(label);
        checks.add(label);
        System.out.println("ALIEN_COMBAT_SMOKE_CHECK "+label);
    }
    @SuppressWarnings("unchecked")
    private GuiButton button(int id)throws Exception {
        Field field=GuiScreen.class.getDeclaredField("buttonList");
        field.setAccessible(true);
        for(GuiButton button:(List<GuiButton>)field.get(mc.currentScreen))
            if(button.id==id)return button;
        return null;
    }
    private void click(int id)throws Exception {
        GuiScreen screen=mc.currentScreen;
        GuiButton selected=button(id);
        if(selected==null)throw new AssertionError("Missing button "+id);
        Method action=screen.getClass().getDeclaredMethod("actionPerformed",GuiButton.class);
        action.setAccessible(true);
        action.invoke(screen,selected);
    }
    private void capture(String name)throws Exception {
        ScreenShotHelper.saveScreenshot(output,name+".png",
                mc.displayWidth,mc.displayHeight,mc.getFramebuffer());
        File file=new File(output,"screenshots/"+name+".png");
        require(file.isFile()&&file.length()>1024,"Screenshot "+name+" recorded in Forge");
        BufferedImage image=ImageIO.read(file);
        require(image!=null&&image.getWidth()==mc.displayWidth
                &&image.getHeight()==mc.displayHeight,"Screenshot "+name+" has valid framebuffer dimensions");
    }
}
