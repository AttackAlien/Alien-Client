package dev.alien.client.smoke;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.world.SkyboxOptionsScreen;
import dev.alien.client.world.WorldManager;
import dev.alien.client.world.WorldConfig;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
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

/**
 * Test-only Forge mod. Real OpenGL/Minecraft framebuffer checks in a disposable
 * superflat world; this source set is deliberately absent from release JARs.
 */
@Mod(modid="alienclient_skysmoke",name="Sky Studio integration smoke",version="1",
        clientSideOnly=true,dependencies="required-after:alienclient")
public final class AlienSkySmoke {
    private static final int[] COLORS = {
        0xC93232, 0x34C953, 0x3678E5, 0xE7CB34, 0xC43BC9, 0x35C5CE
    };
    private final List<String> checks = new ArrayList<String>();
    private Minecraft mc;
    private File output;
    private long started, since;
    private int stage;
    private boolean finished;
    private int northBeforeYaw, northAfterYaw;
    private String edition;

    @Mod.EventHandler public void init(FMLInitializationEvent event) throws Exception {
        if (!Boolean.getBoolean("alien.sky.smoke")) return;
        mc = Minecraft.getMinecraft();
        output = new File(System.getProperty("alien.sky.smoke.output")).getCanonicalFile();
        Files.createDirectories(output.toPath());
        Files.deleteIfExists(new File(output,"result.txt").toPath());
        edition = System.getProperty("alien.sky.smoke.edition");
        AlienClient.get().store().resetDefaults();
        started = since = System.nanoTime();
        FMLCommonHandler.instance().bus().register(this);
        System.out.println("ALIEN_SKY_SMOKE_START edition=" + edition);
    }

    @SubscribeEvent public void onRender(TickEvent.RenderTickEvent event) {
        if (finished || event.phase != TickEvent.Phase.END) return;
        try {
            if (elapsed(started)>160000L) throw new AssertionError("Sky smoke deadline at stage "+stage);
            if (elapsed(since)<(stage==1?1000L:stage>=2?1800L:350L)) return;
            step();
        } catch(Throwable ex) {
            finished=true;
            try(PrintWriter writer=new PrintWriter(new File(output,"result.txt"),"UTF-8")) {
                writer.println("FAIL at stage "+stage);
                for(String check:checks) writer.println("PASS "+check);
                ex.printStackTrace(writer);
            } catch(Exception writing){writing.printStackTrace();}
            ex.printStackTrace();
            System.err.println("ALIEN_SKY_SMOKE_FAIL stage="+stage);
            System.exit(1);
        }
    }

    private void step() throws Exception {
        switch(stage) {
            case 0:
                if (mc.currentScreen == null || !Loader.isModLoaded("alienclient")) return;
                require("legit".equals(edition)||"full".equals(edition),"Valid edition was selected");
                require("alienclient".equals(AlienClient.MOD_ID),"Production mod has expected ID");
                mc.gameSettings.limitFramerate = 30;
                mc.gameSettings.pauseOnLostFocus = false;
                mc.launchIntegratedServer("AlienSkySmoke-"+System.currentTimeMillis(),
                        "Disposable Sky Studio smoke",
                        new WorldSettings(45892L,WorldSettings.GameType.CREATIVE,
                                false,false,WorldType.FLAT));
                advance(); return;
            case 1:
                if(mc.theWorld==null||mc.thePlayer==null||mc.thePlayer.ticksExisted<40) return;
                require(mc.theWorld.provider.getDimensionId()==0,"Overworld loaded in actual Minecraft/Forge");
                File skyboxes=new File(AlienClient.get().store().directory(),"skyboxes");
                Files.createDirectories(skyboxes.toPath());
                File atlas=new File(skyboxes,"sky-smoke.cube.png");
                writeAtlas(atlas);
                WorldConfig c=world();
                c.enabled=true;c.applyTheme("Crystal");c.sunMoon=false;c.stars=0;
                c.fogEnabled=false;c.weather="Server";c.skyboxFile=atlas.getName();
                c.skyboxYaw=0;c.skyboxDrift=0;c.skyboxBrightness=1;
                c.twinklingStars=false;
                mc.gameSettings.hideGUI=true;
                mc.gameSettings.thirdPersonView=0;
                mc.gameSettings.renderDistanceChunks=4;
                face(0F,-90F);
                AlienClient.get().save();
                advance();return;
            case 2:
                require(WorldManager.lastImportMessage.startsWith("Loaded cubemap sky-smoke.cube.png"),
                        "Skybox atlas decoded and uploaded through the real texture manager");
                captureColor("sky-cube-up",COLORS[2],85);
                require(GL11.glGetError()==GL11.GL_NO_ERROR,"OpenGL error-free cubemap render");
                face(0F,-35F);
                advance();return;
            case 3:
                captureColor("sky-cube-south",COLORS[4],95);
                face(180F,-35F);
                advance();return;
            case 4:
                northBeforeYaw=captureColor("sky-cube-north",COLORS[5],95);
                world().skyboxYaw=180F;
                advance();return;
            case 5:
                northAfterYaw=captureColor("sky-cube-yaw180",COLORS[4],95);
                require(distance(northBeforeYaw,northAfterYaw)>100,
                        "180 degree skybox yaw visibly changes the actual framebuffer");
                world().skyboxBrightness=.45F;
                advance();return;
            case 6:
                int dim=capture("sky-cube-dimmed");
                require(luminance(dim)<luminance(northAfterYaw)*.75D,
                        "Skybox brightness darkens the real PNG framebuffer");
                world().skyboxDrift=1.5F;
                world().twinklingStars=true;
                AlienClient.get().save();
                AlienClient.get().store().load();
                require(Math.abs(world().skyboxYaw-180F)<.001F
                            && Math.abs(world().skyboxBrightness-.45F)<.001F
                            && Math.abs(world().skyboxDrift-1.5F)<.001F
                            && world().twinklingStars,
                        "New Sky Studio options persist after the actual config reload");
                mc.gameSettings.hideGUI=false;
                mc.displayGuiScreen(new SkyboxOptionsScreen(null));
                advance();return;
            case 7:
                require(mc.currentScreen instanceof SkyboxOptionsScreen,
                        "Skybox Options screen opens and renders in the live world");
                capture("sky-options-live-world");
                boolean prior=world().twinklingStars;
                click(mc.currentScreen,4);
                require(prior!=world().twinklingStars,"Twinkling stars button changes live setting");
                click(mc.currentScreen,5);
                require(world().skyboxYaw==0F&&world().skyboxDrift==0F
                            && world().skyboxBrightness==1F&&!world().twinklingStars,
                        "Reset skybox options restores defaults without clearing selected PNG");
                require("sky-smoke.cube.png".equals(world().skyboxFile),
                        "Skybox remains selected after options reset");
                mc.displayGuiScreen(null);
                mc.gameSettings.hideGUI=true;
                world().skyboxFile="";
                advance();return;
            case 8:
                require("Procedural sky active".equals(WorldManager.lastImportMessage),
                        "Removing filename releases atlas and restores procedural sky");
                capture("sky-procedural-restored");
                world().enabled=false;
                world().weather="Server";
                advance();return;
            case 9:
                require(mc.theWorld.provider.getSkyRenderer()==null,
                        "Disabling custom sky restores vanilla renderer");
                require(GL11.glGetError()==GL11.GL_NO_ERROR,
                        "Final game render has no OpenGL errors");
                require(AlienClient.get().lastError().isEmpty(),
                        "No config persistence errors during graphical smoke");
                capture("sky-vanilla-restored");
                finished=true;
                try(PrintWriter writer=new PrintWriter(new File(output,"result.txt"),"UTF-8")) {
                    writer.println("PASS "+checks.size()+" checks");
                    writer.println("Actual Forge 1.8.9 "+edition+"; six cubemap faces tested using live framebuffer");
                    for(String check:checks) writer.println("PASS "+check);
                }
                System.out.println("ALIEN_SKY_SMOKE_PASS checks="+checks.size());
                // Deliberately discard the smoke world: this is an isolated test runtime.
                System.exit(0);
                return;
            default: throw new AssertionError("Unknown sky smoke stage "+stage);
        }
    }
    private void advance(){stage++;since=System.nanoTime();System.out.println("ALIEN_SKY_SMOKE_STAGE "+stage);}
    private WorldConfig world(){return AlienClient.get().config().world;}
    private void face(float yaw,float pitch) {
        mc.thePlayer.rotationYaw=yaw;mc.thePlayer.prevRotationYaw=yaw;
        mc.thePlayer.rotationPitch=pitch;mc.thePlayer.prevRotationPitch=pitch;
    }
    private static long elapsed(long base){return (System.nanoTime()-base)/1000000L;}
    private void require(boolean ok,String name){
        if(!ok)throw new AssertionError(name);
        checks.add(name);
        System.out.println("ALIEN_SKY_SMOKE_CHECK "+name);
    }
    private void writeAtlas(File file)throws Exception {
        int cell=256;
        BufferedImage im=new BufferedImage(cell*3,cell*2,BufferedImage.TYPE_INT_RGB);
        for(int f=0;f<6;f++){
            int x0=(f%3)*cell,y0=(f/3)*cell;
            for(int y=0;y<cell;y++)for(int x=0;x<cell;x++)
                im.setRGB(x0+x,y0+y,COLORS[f]);
        }
        if(!ImageIO.write(im,"png",file))throw new AssertionError("PNG writer unavailable");
        require(file.length()>1024,"Disposable six-face 3x2 atlas generated");
    }
    private int capture(String name)throws Exception {
        ScreenShotHelper.saveScreenshot(output,name+".png",
                mc.displayWidth,mc.displayHeight,mc.getFramebuffer());
        File file=new File(output,"screenshots/"+name+".png");
        if(!file.isFile()||file.length()<1000)
            throw new AssertionError("Missing framebuffer image: "+name);
        BufferedImage screenshot=ImageIO.read(file);
        require(screenshot!=null && screenshot.getWidth()==mc.displayWidth
                  && screenshot.getHeight()==mc.displayHeight,
                  "Real framebuffer screenshot "+name);
        int pixel=screenshot.getRGB(screenshot.getWidth()/2,screenshot.getHeight()/2)&0xFFFFFF;
        System.out.println("ALIEN_SKY_SMOKE_PIXEL "+name+" #"+Integer.toHexString(pixel));
        return pixel;
    }
    private int captureColor(String name,int expected,int tolerance)throws Exception {
        int actual=capture(name);
        require(distance(actual,expected)<tolerance,
                name+" matches atlas face: actual #"+Integer.toHexString(actual)
                        +" expected #"+Integer.toHexString(expected));
        return actual;
    }
    private static int distance(int a,int b){
        return Math.abs((a>>16&255)-(b>>16&255))
                +Math.abs((a>>8&255)-(b>>8&255))
                +Math.abs((a&255)-(b&255));
    }
    private static double luminance(int color){
        return (color>>16&255)*.2126D+(color>>8&255)*.7152D+(color&255)*.0722D;
    }
    @SuppressWarnings("unchecked")
    private void click(GuiScreen screen,int id)throws Exception {
        Field field=GuiScreen.class.getDeclaredField("buttonList");
        field.setAccessible(true);
        GuiButton target=null;
        for(GuiButton b:(List<GuiButton>)field.get(screen))
            if(b.id==id){target=b;break;}
        if(target==null)throw new AssertionError("Missing Skybox Options button "+id);
        Method method=screen.getClass().getDeclaredMethod("actionPerformed",GuiButton.class);
        method.setAccessible(true);
        method.invoke(screen,target);
    }
}
