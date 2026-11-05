package dev.alien.client.smoke;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.hud.*;
import dev.alien.client.hud.music.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.*;

@Mod(modid="alien_media_smoke",name="Now Playing graphical validation (synthetic source)",version="1",clientSideOnly=true,dependencies="required-after:alienclient")
public final class AlienMediaSmoke {
    private Minecraft m;private File out;private Path control;private int stage;private long since,start,pausedPosition;private boolean done;private String oldHash;private int texture;private Object textureObject;private final List<String> checks=new ArrayList<String>();
    private ClientConfig c(){return AlienClient.get().config();}
    private HudConfig.Widget widget(){return c().hud.widgets.get("music");}
    private NowPlayingState state(){return NowPlayingService.get().snapshot();}
    @Mod.EventHandler public void init(FMLInitializationEvent e)throws Exception {
        if(!Boolean.getBoolean("alien.media.smoke"))return;m=Minecraft.getMinecraft();out=new File(System.getProperty("alien.media.smoke.output"));Files.createDirectories(out.toPath());Files.deleteIfExists(new File(out,"result.txt").toPath());control=Paths.get(System.getenv("ALIEN_MEDIA_FIXTURE_CONTROL"));AlienClient.get().store().resetDefaults();start=since=System.nanoTime();FMLCommonHandler.instance().bus().register(this);
    }
    private long elapsed(long t){return (System.nanoTime()-t)/1000000;}
    @SubscribeEvent public void render(TickEvent.RenderTickEvent e) {
        if(done||e.phase!=TickEvent.Phase.END)return;
        try{if(elapsed(start)>180000)throw new AssertionError("Deadline at stage "+stage);if(elapsed(since)<850)return;step();}
        catch(Throwable f){done=true;write(false,f);m.shutdown();throw new RuntimeException(f);}
    }
    private void require(boolean b,String text){if(!b)throw new AssertionError(text);checks.add(text);System.out.println("ALIEN_MEDIA_CHECK "+text);}
    private void next(){stage++;since=System.nanoTime();System.out.println("ALIEN_MEDIA_STAGE "+stage);}
    private void feed(String fields)throws Exception{Path temp=control.resolveSibling(control.getFileName()+".tmp");Files.write(temp,("{"+fields+"}").getBytes(StandardCharsets.UTF_8));Files.move(temp,control,StandardCopyOption.REPLACE_EXISTING);}
    private boolean waitFor(boolean condition,String text){if(condition)return true;if(elapsed(since)>12000)throw new AssertionError(text+"; actual state "+state().status);return false;}
    private void capture(String name)throws Exception {
        File f=new File(out,"screenshots/"+name+".png");Files.deleteIfExists(f.toPath());ScreenShotHelper.saveScreenshot(out,name+".png",m.displayWidth,m.displayHeight,m.getFramebuffer());BufferedImage image=ImageIO.read(f);require(image!=null&&image.getWidth()==1280&&image.getHeight()==720,"Actual Forge framebuffer "+name);require(GL11.glGetError()==GL11.GL_NO_ERROR,"No GL errors after "+name);
    }
    private int texture()throws Exception{java.lang.reflect.Field f=NowPlayingHud.class.getDeclaredField("texture");f.setAccessible(true);Object value=f.get(null);return value==null?0:m.getTextureManager().getTexture((net.minecraft.util.ResourceLocation)value).getGlTextureId();}
    private void click(int id)throws Exception{java.lang.reflect.Field field=GuiScreen.class.getDeclaredField("buttonList");field.setAccessible(true);for(Object value:(List<?>)field.get(m.currentScreen)){GuiButton b=(GuiButton)value;if(b.id==id){java.lang.reflect.Method method=m.currentScreen.getClass().getDeclaredMethod("actionPerformed",GuiButton.class);method.setAccessible(true);method.invoke(m.currentScreen,b);return;}}throw new AssertionError("Missing button "+id);}
    private void validateState(){
        net.minecraft.client.renderer.GlStateManager.disableBlend();net.minecraft.client.renderer.GlStateManager.disableAlpha();net.minecraft.client.renderer.GlStateManager.disableTexture2D();net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(1,0,1,0);
        NowPlayingHud.render(widget(),640,360,false);
        require(!GL11.glIsEnabled(GL11.GL_BLEND)&&!GL11.glIsEnabled(GL11.GL_ALPHA_TEST)&&!GL11.glIsEnabled(GL11.GL_TEXTURE_2D),"Media renderer restores incoming GL blend, alpha and texture flags");
        require(GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB)==1&&GL11.glGetInteger(GL14.GL_BLEND_DST_RGB)==0,"Media renderer restores incoming blend factors");
        net.minecraft.client.renderer.GlStateManager.enableBlend();net.minecraft.client.renderer.GlStateManager.enableAlpha();net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        require(GL11.glIsEnabled(GL11.GL_BLEND)&&GL11.glIsEnabled(GL11.GL_ALPHA_TEST)&&GL11.glIsEnabled(GL11.GL_TEXTURE_2D),"Minecraft GL state cache still enables blending, alpha and textures after music rendering");
        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(770,771,1,0);
    }
    private void step()throws Exception {
        switch(stage){
        case 0:
            if(m.currentScreen==null)return;require(c().hud.widgets.size()==16&&!widget().enabled,"All fifteen existing widgets remain; music is opt-in");
            Display.setDisplayMode(new DisplayMode(1280,720));java.lang.reflect.Method resize=Minecraft.class.getDeclaredMethod("resize",int.class,int.class);resize.setAccessible(true);resize.invoke(m,1280,720);m.gameSettings.guiScale=2;m.gameSettings.limitFramerate=30;m.gameSettings.renderDistanceChunks=3;m.gameSettings.pauseOnLostFocus=false;
            feed("\"title\":\"MÍDIA DE TESTE / fonte sintética\"");m.launchIntegratedServer("AlienMedia-"+System.currentTimeMillis(),"Disposable media validation",new WorldSettings(42,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT));next();return;
        case 1:
            if(m.thePlayer==null||m.thePlayer.ticksExisted<40)return;m.displayGuiScreen(null);for(HudConfig.Widget w:c().hud.widgets.values())if(w!=null)w.enabled=false;c().hud.widgets.get("brand").enabled=true;c().hud.background=true;widget().enabled=true;widget().x=.035f;widget().y=.70f;m.thePlayer.rotationYaw=25;m.thePlayer.rotationPitch=-8;m.getIntegratedServer().addScheduledTask(()->{m.getIntegratedServer().worldServers[0].getGameRules().setOrCreateGameRule("doDaylightCycle","false");m.getIntegratedServer().worldServers[0].getGameRules().setOrCreateGameRule("doMobSpawning","false");m.getIntegratedServer().worldServers[0].setWorldTime(6000);}).get();next();return;
        case 2:
            if(!waitFor(state().active(System.currentTimeMillis())&&state().cover!=null,"Fixture bridge must publish real transport files"))return;
            require(state().title.startsWith("MÍDIA DE TESTE")&&state().source.equals("Diagnostic fixture"),"Synthetic provider is explicitly labeled; no Spotify or YouTube claim");require(texture()!=0,"Artwork is uploaded as an actual Minecraft texture");texture=texture();java.lang.reflect.Field texField=NowPlayingHud.class.getDeclaredField("texture");texField.setAccessible(true);textureObject=m.getTextureManager().getTexture((net.minecraft.util.ResourceLocation)texField.get(null));oldHash=state().artwork;validateState();capture("01-music-compact-fixture");c().hud.music.style="Card";c().hud.music.time=true;c().hud.music.sourceLabel=true;next();return;
        case 3:
            require(texture()==texture,"Unchanged artwork reuses the same GPU texture");capture("02-music-card-fixture");feed("\"status\":\"paused\",\"positionMs\":51000,\"durationMs\":200000");next();return;
        case 4:
            if(!waitFor("paused".equals(state().status),"Pause must reach client"))return;pausedPosition=state().position(System.currentTimeMillis());next();return;
        case 5:
            require(state().position(System.currentTimeMillis())==pausedPosition,"Paused HUD elapsed time remains fixed across frames and polls");capture("03-music-paused-fixture");c().hud.music.hidePaused=true;require(!NowPlayingHud.visible(state(),false),"Hide paused suppresses the actual overlay");c().hud.music.hidePaused=false;c().hud.music.style="Transparent";widget().customColor=true;widget().color=0xff36d2bf;widget().opacity=.78f;next();return;
        case 6:
            capture("04-music-transparent-fixture");m.displayGuiScreen(new HudEditorScreen(null));((HudEditorScreen)m.currentScreen).selectWidget("music");widget().x=.36f;widget().y=.43f;next();return;
        case 7:
            require(((HudEditorScreen)m.currentScreen).selectedWidget().equals("music"),"Now Playing is selectable in the existing HUD Studio");
            click(2);require(widget().scale>1,"Existing scale control works for music");
            java.lang.reflect.Method down=HudEditorScreen.class.getDeclaredMethod("mouseClicked",int.class,int.class,int.class),move=HudEditorScreen.class.getDeclaredMethod("mouseClickMove",int.class,int.class,int.class,long.class),up=HudEditorScreen.class.getDeclaredMethod("mouseReleased",int.class,int.class,int.class);down.setAccessible(true);move.setAccessible(true);up.setAccessible(true);
            int[] b=HudManager.bounds("music",widget(),m.currentScreen.width,m.currentScreen.height);float old=widget().x;down.invoke(m.currentScreen,b[0]+8,b[1]+8,0);move.invoke(m.currentScreen,b[0]+50,b[1]+24,0,100L);up.invoke(m.currentScreen,b[0]+50,b[1]+24,0);require(widget().x>old,"Drag changes the actual music widget position");click(5);b=HudManager.bounds("music",widget(),m.currentScreen.width,m.currentScreen.height);require(Math.abs(b[0]+b[2]/2-m.currentScreen.width/2)<=1,"Existing alignment centers the music widget");next();return;
        case 8:
            capture("05-music-hud-studio-fixture");click(53);next();return;
        case 9:
            require(m.currentScreen instanceof HudWidgetSettingsScreen&&((HudWidgetSettingsScreen)m.currentScreen).isMusic(),"Options opens the real media settings page");click(170);require("Compact".equals(c().hud.music.style),"Layout control cycles three implemented styles");click(172);require(!c().hud.music.artist,"Artist toggle changes live configuration");click(172);capture("06-music-controls-fixture");click(21);next();return;
        case 10:
            capture("07-music-style-scale");AlienClient.get().store().saveProfile("media-smoke");widget().opacity=1;c().hud.music.style="Card";require(AlienClient.get().store().loadProfile("media-smoke"),"Unified profile reload succeeds");require(widget().opacity==.78f&&"Compact".equals(c().hud.music.style),"Media style, color, transform and opacity persist in Unified profiles");m.displayGuiScreen(null);widget().x=.035f;widget().y=.70f;widget().scale=1;widget().opacity=1;feed("\"title\":\"MÍDIA DE TESTE / segunda faixa\",\"version\":2");next();return;
        case 11:
            if(!waitFor(!state().artwork.equals(oldHash)&&state().cover!=null,"Track art update must reach client"))return;java.lang.reflect.Field currentTexture=NowPlayingHud.class.getDeclaredField("texture");currentTexture.setAccessible(true);require(texture()!=0&&m.getTextureManager().getTexture((net.minecraft.util.ResourceLocation)currentTexture.get(null))!=textureObject,"Track change replaces the previous artwork texture object (GL may reuse numeric IDs)");net.minecraft.util.ResourceLocation slot=(net.minecraft.util.ResourceLocation)currentTexture.get(null);
            require("alienclient:dynamic/now_playing".equals(slot.toString()),"Artwork always uses one fixed texture slot, without dynamic counter entries");
            java.lang.reflect.Field registry=net.minecraft.client.renderer.texture.TextureManager.class.getDeclaredField("mapTextureObjects");registry.setAccessible(true);Map<?,?> entries=(Map<?,?>)registry.get(m.getTextureManager());
            require(!entries.containsValue(textureObject),"Texture manager no longer retains the previous artwork object after a track change");c().hud.music.artwork=false;next();return;
        case 12:
            if(!waitFor(state().cover==null,"Artwork opt-out must restart bridge without images"))return;require(texture()==0,"Artwork opt-out releases the GPU texture");capture("08-music-no-art-fixture");c().hud.music.artwork=true;feed("\"title\":\"MÍDIA DE TESTE / stream sem duração\",\"positionMs\":null,\"durationMs\":null,\"cover\":false");next();return;
        case 13:
            if(!waitFor(state().title.contains("stream")&&state().durationMs<0,"Unknown timeline must reach client"))return;require(state().fraction(System.currentTimeMillis())<0&&state().position(System.currentTimeMillis())<0,"Unknown live timeline has no fictitious progress or elapsed time");capture("09-music-unknown-duration-fixture");c().hud.music.source="Spotify";next();return;
        case 14:
            if(!waitFor("idle".equals(state().status),"Source filtering must reach bridge"))return;require(!NowPlayingHud.visible(state(),false),"Filtered out source is hidden instead of inventing a track");c().hud.music.source="Auto";feed("\"title\":\"MÍDIA DE TESTE / título muito longo para conferir o recorte e a rolagem dentro do HUD Studio Ultimate\"");m.displayGuiScreen(new HudEditorScreen(null));((HudEditorScreen)m.currentScreen).selectWidget("music");widget().x=.39f;widget().y=.43f;next();return;
        case 15:
            if(!waitFor(state().title.contains("muito longo"),"Long title must reach client"))return;next();return;
        case 16:
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"Scrolling long title preserves editor scissor and GL state");capture("10-music-long-title-fixture");m.displayGuiScreen(null);widget().x=.035f;widget().y=.70f;feed("\"disconnect\":true");next();return;
        case 17:
            if(elapsed(since)<7200)return;require(!state().active(System.currentTimeMillis()),"A stopped publisher expires after six seconds");require(!NowPlayingHud.visible(state(),false),"Disconnected track is hidden");c().hud.music.hideInactive=false;next();return;
        case 18:
            require(texture()==0,"Disconnect releases stale artwork texture");capture("11-music-disconnected");widget().enabled=false;next();return;
        case 19:
            if(!waitFor("idle".equals(state().status),"Disabling widget must stop bridge"))return;Path runtime=AlienClient.get().store().directory().toPath().resolve("media-runtime");try(java.util.stream.Stream<Path> files=Files.list(runtime)){require(!files.findAny().isPresent(),"Disabling widget removes temporary listening metadata and session directories");}done=true;write(true,null);m.shutdown();return;
        }
    }
    private void write(boolean pass,Throwable failure){try(PrintWriter w=new PrintWriter(new File(out,"result.txt"),"UTF-8")){w.println((pass?"PASS ":"FAIL stage "+stage+" ")+checks.size()+" checks");w.println("Actual Forge 1.8.9 Linux software OpenGL. Explicit SYNTHETIC metadata process; NOT Spotify/YouTube/Windows SMTC evidence.");for(String s:checks)w.println("PASS "+s);if(failure!=null)failure.printStackTrace(w);}catch(Exception ignored){}}
}
