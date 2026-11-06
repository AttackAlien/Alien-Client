package dev.alien.client.smoke;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.effects.ghost.*;
import dev.alien.client.gui.Ui;
import java.awt.Robot;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.util.*;
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

/** Test-only real screen/input and GL-state regression checks, excluded from production artifacts. */
@Mod(modid="alien_motion_smoke",name="Animation polish regression",version="1",clientSideOnly=true,dependencies="required-after:alienclient")
public final class AlienMotionSmoke {
    private Minecraft m;private File out;private Robot robot;private int stage,x,y;private long began,since;private boolean done;private final List<String> checks=new ArrayList<String>();
    private ClientConfig c(){return AlienClient.get().config();}
    @Mod.EventHandler public void init(FMLInitializationEvent e)throws Exception{if(!Boolean.getBoolean("alien.motion.smoke"))return;m=Minecraft.getMinecraft();out=new File(System.getProperty("alien.motion.smoke.output"));Files.createDirectories(out.toPath());Files.deleteIfExists(new File(out,"result.txt").toPath());AlienClient.get().store().resetDefaults();began=since=System.nanoTime();FMLCommonHandler.instance().bus().register(this);}
    private void check(boolean yes,String why){if(!yes)throw new AssertionError(why);checks.add(why);System.out.println("ALIEN_MOTION_CHECK "+why);}
    private void next(){stage++;since=System.nanoTime();System.out.println("ALIEN_MOTION_STAGE "+stage);}
    @SubscribeEvent public void frame(TickEvent.RenderTickEvent e){if(done||e.phase!=TickEvent.Phase.END)return;try{if((System.nanoTime()-began)/1e9>180)throw new AssertionError("Deadline stage "+stage);if(stage!=4&&stage!=6&&(System.nanoTime()-since)/1e6<800)return;step();}catch(Throwable t){done=true;write(t);m.shutdown();throw new RuntimeException(t);}}
    private GuiButton button(int id)throws Exception{Field f=GuiScreen.class.getDeclaredField("buttonList");f.setAccessible(true);for(Object o:(List<?>)f.get(m.currentScreen))if(((GuiButton)o).id==id)return (GuiButton)o;throw new AssertionError("Missing control "+id);}
    private float toggle(int id)throws Exception{Field f=Ui.Button.class.getDeclaredField("toggleProgress");f.setAccessible(true);return f.getFloat(button(id));}
    private void click(int id)throws Exception{GuiButton b=button(id);Method method=GhostStudioScreen.class.getDeclaredMethod("mouseClicked",int.class,int.class,int.class);method.setAccessible(true);method.invoke(m.currentScreen,b.xPosition+b.width/2,b.yPosition+b.height/2,0);}
    private void capture(String name)throws Exception{ScreenShotHelper.saveScreenshot(out,name+".png",m.displayWidth,m.displayHeight,m.getFramebuffer());File f=new File(out,"screenshots/"+name+".png");check(ImageIO.read(f).getWidth()==1280,"Actual Forge framebuffer "+name);check(GL11.glGetError()==0,"Clean OpenGL after "+name);}
    private void step()throws Exception{switch(stage){
        case 0:if(m.currentScreen==null)return;Display.setDisplayMode(new DisplayMode(1280,720));Display.setLocation(0,0);Method resize=Minecraft.class.getDeclaredMethod("resize",int.class,int.class);resize.setAccessible(true);resize.invoke(m,1280,720);m.gameSettings.guiScale=2;m.gameSettings.renderDistanceChunks=2;m.gameSettings.limitFramerate=60;m.gameSettings.pauseOnLostFocus=false;robot=new Robot();m.launchIntegratedServer("AlienMotion-"+System.currentTimeMillis(),"Disposable polish regression",new WorldSettings(42,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT));next();return;
        case 1:if(m.thePlayer==null||m.thePlayer.ticksExisted<40)return;c().killEffects.enabled=true;c().ui.blur=0;c().cosmetics.self.hat="SAMURAI";c().emotes.slots[0]="take_the_l";m.displayGuiScreen(new GhostStudioScreen(null));robot.mouseMove(Display.getX()+5,Display.getY()+5);next();return;
        case 2:check(toggle(10)==1,"First paint of a saved ON preference shows the knob ON");capture("01-ghost-interface-saved-state");next();return;
        case 3:x=button(10).xPosition;y=button(10).yPosition;click(10);check(!c().killEffects.enabled,"Production mouse input toggles the actual saved setting");next();return;
        case 4:check(toggle(10)>0&&toggle(10)<1,"Rebuilding production controls retains an in-progress switch transition");check(button(10).xPosition==x&&button(10).yPosition==y,"Animated feedback leaves the actual input rectangle fixed");click(10);next();return;
        case 5:check(c().killEffects.enabled&&toggle(10)>.99,"Rapid switch reversal settles to the new preference");c().ui.reducedMotion=true;click(10);next();return;
        case 6:check(toggle(10)==0&&!c().killEffects.enabled,"Reduced motion immediately reflects the real OFF preference");c().ui.reducedMotion=false;robot.mouseMove(Display.getX()+100,Display.getY()+200);for(int i=0;i<28;i++){robot.mouseWheel(1);robot.delay(8);}next();return;
        case 7:GuiButton last=button(27);check(last.yPosition>=70&&last.yPosition+last.height<=m.currentScreen.height-42,"Real native mouse-wheel scrolling reaches the final constellation slider");capture("02-constellation-controls-scrolled");click(19);check(!c().killEffects.constellation,"Actual visible constellation toggle changes the effect design");check(last.mousePressed(m,last.xPosition+(int)(last.width*.85),last.yPosition+last.height-3),"Finale slider receives actual Minecraft button input");last.mouseReleased(0,0);check(c().killEffects.finaleDuration>1.6,"Finale slider changes real bounded duration");AlienClient.get().store().saveProfile("motion-polish");c().killEffects.constellation=true;c().killEffects.finaleDuration=.7f;check(AlienClient.get().store().loadProfile("motion-polish"),"Unified profile reload succeeds");check(!c().killEffects.constellation&&c().killEffects.finaleDuration>1.6&&"SAMURAI".equals(c().cosmetics.self.hat)&&"take_the_l".equals(c().emotes.slots[0])&&c().hud.widgets.size()==16,"Polished preferences round trip with existing emotes, cosmetics and sixteen widgets");next();return;
        case 8:checkState(false);checkState(true);System.clearProperty("alien.ghost.fallback");c().killEffects.enabled=true;c().killEffects.constellation=true;c().killEffects.finaleDuration=1.2f;m.displayGuiScreen(new GhostStudioScreen(null));next();return;
        case 9:capture("03-ghost-polished-preview");c().animations=false;next();return;
        case 10:checkState(true);capture("04-ghost-global-animation-off");c().animations=true;System.clearProperty("alien.ghost.fallback");done=true;write(null);m.shutdown();return;
    }}
    private void checkState(boolean fallback)throws Exception{
        System.setProperty("alien.ghost.fallback",Boolean.toString(fallback));
        int oldArray=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING),oldElement=GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING),oldActive=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),oldClient=GL11.glGetInteger(GL13.GL_CLIENT_ACTIVE_TEXTURE),oldProgram=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int array=GL15.glGenBuffers(),element=GL15.glGenBuffers(),program=GL20.glCreateProgram(),v=shader(GL20.GL_VERTEX_SHADER,"void main(){gl_Position=ftransform();gl_FrontColor=gl_Color;}"),f=shader(GL20.GL_FRAGMENT_SHADER,"void main(){gl_FragColor=gl_Color;}");
        GL20.glAttachShader(program,v);GL20.glAttachShader(program,f);GL20.glLinkProgram(program);check(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)!=0,"Test sentinel GL program links");
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);
        try{GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,array);GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,element);GL13.glActiveTexture(GL13.GL_TEXTURE1);GL13.glClientActiveTexture(GL13.GL_TEXTURE1);GL20.glUseProgram(program);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glEnable(GL11.GL_CULL_FACE);GL11.glDepthMask(true);GL11.glBlendFunc(GL11.GL_ONE,GL11.GL_ZERO);
            GhostRenderer.draw(1.1f,c().killEffects,c().animations,48);
            check(GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING)==array&&GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING)==element,"Ghost restores both VBO and index-buffer bindings / fallback "+fallback);
            check(GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)==program&&GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)==GL13.GL_TEXTURE1&&GL11.glGetInteger(GL13.GL_CLIENT_ACTIVE_TEXTURE)==GL13.GL_TEXTURE1,"Ghost restores shader and both texture units / fallback "+fallback);
            check(!GL11.glIsEnabled(GL11.GL_DEPTH_TEST)&&GL11.glIsEnabled(GL11.GL_CULL_FACE)&&GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)&&GL11.glGetInteger(GL11.GL_BLEND_SRC)==GL11.GL_ONE&&GL11.glGetInteger(GL11.GL_BLEND_DST)==GL11.GL_ZERO,"Ghost restores depth, culling and blend state / fallback "+fallback);
            check(GL11.glGetError()==0,"Shader or CPU fallback renders without a GL error / fallback "+fallback);
        }finally{GL20.glUseProgram(oldProgram);GL11.glPopClientAttrib();GL11.glPopAttrib();GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,oldArray);GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,oldElement);GL13.glActiveTexture(oldActive);GL13.glClientActiveTexture(oldClient);GL15.glDeleteBuffers(array);GL15.glDeleteBuffers(element);GL20.glDeleteShader(v);GL20.glDeleteShader(f);GL20.glDeleteProgram(program);}
    }
    private int shader(int type,String text){int s=GL20.glCreateShader(type);GL20.glShaderSource(s,text);GL20.glCompileShader(s);if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0)throw new AssertionError(GL20.glGetShaderInfoLog(s,2048));return s;}
    private void write(Throwable failure){try(PrintWriter w=new PrintWriter(new File(out,"result.txt"),"UTF-8")){w.println((failure==null?"PASS ":"FAIL stage "+stage+" ")+checks.size()+" checks");w.println("Actual Forge 1.8.9 / Linux software OpenGL / production UI and native wheel input / isolated world.");for(String check:checks)w.println("PASS "+check);if(failure!=null)failure.printStackTrace(w);}catch(IOException ignored){}}
}
