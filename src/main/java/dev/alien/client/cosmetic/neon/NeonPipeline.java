package dev.alien.client.cosmetic.neon;

import dev.alien.client.AlienClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.*;
import java.io.*;
import java.util.*;

/** Shared depth-tested emissive mask, one reduced-resolution separable bloom per world/GUI pass. */
public final class NeonPipeline {
    private static int surface,blur,level;
    private static boolean failed,maskPass,dirty,cleared,textured;
    private static String status="Not initialized";
    private static NeonDesign current;private static final NeonDesign GHOST=new NeonDesign();
    private static final Target MASK=new Target(),PING=new Target(),PONG=new Target();
    private static final Map<String,Integer> LOCATIONS=new HashMap<String,Integer>();
    private static int maskDraws;private static float radius;private static long composites,surfaces;
    public static boolean maskPass(){return maskPass;}public static boolean active(){return level>0&&surface!=0&&!failed;}
    public static String status(){return status;}public static long composites(){return composites;}public static long surfaces(){return surfaces;}
    @SubscribeEvent public void frame(TickEvent.RenderTickEvent e){if(e.phase==TickEvent.Phase.START){cleared=dirty=false;maskDraws=0;radius=0;}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void world(RenderWorldLastEvent e){flush();}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void gui(GuiScreenEvent.DrawScreenEvent.Post e){flush();}
    public static void render(NeonDesign d,Runnable geometry){render(d,geometry,false);}
    public static void ghost(float strength,Runnable geometry){GHOST.intensity=strength;GHOST.bloom=true;render(GHOST,geometry,true);}
    private static void render(NeonDesign d,Runnable geometry,boolean foreign){
        if(d==null||!d.enabled||d.intensity<=0){geometry.run();return;}
        if(level>0){NeonDesign before=current;current=d;if(!foreign&&surface!=0)parameters(d,maskPass);try{geometry.run();}finally{current=before;if(before!=null&&!foreign)parameters(before,maskPass);}return;}
        int oldProgram=OpenGlHelper.shadersSupported?GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM):0;
        float lx=OpenGlHelper.lastBrightnessX,ly=OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);level++;current=d;
        try{
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240,240);
            boolean ready=prepare();if(ready&&!foreign)parameters(d,false);
            geometry.run();surfaces++;
            if(ready&&d.bloom&&bloomAllowed()&&maskDraws<8&&GL11.glGetInteger(36006)==Minecraft.getMinecraft().getFramebuffer().framebufferObject){
                int oldFramebuffer=GL11.glGetInteger(36006),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                try{
                    boolean targetReady=true;
                    try{Minecraft m=Minecraft.getMinecraft();GL13.glActiveTexture(GL13.GL_TEXTURE0);MASK.ensure(m.displayWidth,m.displayHeight);MASK.bind();
                    // Borrow, never clear/delete/write the game's existing depth attachment.
                    OpenGlHelper.glFramebufferRenderbuffer(OpenGlHelper.GL_FRAMEBUFFER,OpenGlHelper.GL_DEPTH_ATTACHMENT,36161,m.getFramebuffer().depthBuffer);
                    if(OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER)!=OpenGlHelper.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Neon depth attachment unavailable");
                    if(!cleared){GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glColorMask(true,true,true,true);GL11.glClearColor(0,0,0,0);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);cleared=true;}}catch(RuntimeException problem){failed=true;targetReady=false;status="Full-bright fallback / bloom unavailable";AlienClient.LOG.warn("Neon mask unavailable",problem);}
                    // Restore the caller's clipping rectangle after the full mask clear.
                    GL11.glPopAttrib();GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                    GL11.glDepthMask(false);GL11.glDisable(GL11.GL_ALPHA_TEST);if(targetReady){maskPass=true;GL13.glActiveTexture(GL13.GL_TEXTURE0);if(!foreign)parameters(d,true);geometry.run();dirty=true;maskDraws++;radius=Math.max(radius,d.radius);}
                }finally{maskPass=false;OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER,oldFramebuffer);GL11.glPopAttrib();GL13.glActiveTexture(active);}
            }
        }finally{maskPass=false;level--;current=null;if(OpenGlHelper.shadersSupported)GL20.glUseProgram(oldProgram);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,lx,ly);GL11.glPopAttrib();}
    }
    public static void texture(boolean value){if(active()&&textured!=value){GL20.glUseProgram(surface);GL20.glUniform1i(loc(surface,"Textured"),value?1:0);textured=value;}}
    private static boolean bloomAllowed(){AlienClient c=AlienClient.get();return OpenGlHelper.isFramebufferEnabled()&&c!=null&&c.performance()!=null&&c.performance().qualityScale()>=.65f&&!"Competitive".equals(c.config().performance.preset);}
    private static void parameters(NeonDesign d,boolean mask){textured=false;GL20.glUseProgram(surface);GL20.glUniform3f(loc(surface,"GlowColor"),(d.color>>16&255)/255f,(d.color>>8&255)/255f,(d.color&255)/255f);GL20.glUniform1f(loc(surface,"Intensity"),d.intensity);GL20.glUniform1i(loc(surface,"Mask"),mask?1:0);GL20.glUniform1i(loc(surface,"Textured"),0);GL20.glUniform1i(loc(surface,"Fabric"),0);}
    private static void flush(){
        if(!dirty||failed)return;dirty=false;
        int previous=GL11.glGetInteger(36006),program=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);GL13.glActiveTexture(GL13.GL_TEXTURE0);int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{
            Minecraft m=Minecraft.getMinecraft();PING.ensure(Math.max(1,m.displayWidth/4),Math.max(1,m.displayHeight/4));PONG.ensure(PING.w,PING.h);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_BLEND);GL11.glColorMask(true,true,true,true);GL20.glUseProgram(blur);GL20.glUniform1i(loc(blur,"Image"),0);
            PING.bind();GL11.glViewport(0,0,PING.w,PING.h);GL11.glBindTexture(GL11.GL_TEXTURE_2D,MASK.texture);GL20.glUniform2f(loc(blur,"Direction"),radius/(3.230769f*m.displayWidth),0);quad();
            PONG.bind();GL11.glBindTexture(GL11.GL_TEXTURE_2D,PING.texture);GL20.glUniform2f(loc(blur,"Direction"),0,radius/(3.230769f*m.displayHeight));quad();
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER,previous);GL11.glViewport(0,0,m.displayWidth,m.displayHeight);GL11.glBindTexture(GL11.GL_TEXTURE_2D,PONG.texture);GL20.glUniform2f(loc(blur,"Direction"),0,0);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_ONE,GL11.GL_ONE);quad();composites++;cleared=false;radius=0;maskDraws=0;
        }catch(RuntimeException e){failed=true;status="Emissive surface / bloom unavailable";AlienClient.LOG.warn("Neon bloom disabled",e);}
        finally{OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER,previous);GL20.glUseProgram(program);GL11.glPopAttrib();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL13.glActiveTexture(active);}
    }
    private static void quad(){GL11.glBegin(GL11.GL_QUADS);GL11.glTexCoord2f(0,0);GL11.glVertex2f(-1,-1);GL11.glTexCoord2f(1,0);GL11.glVertex2f(1,-1);GL11.glTexCoord2f(1,1);GL11.glVertex2f(1,1);GL11.glTexCoord2f(0,1);GL11.glVertex2f(-1,1);GL11.glEnd();}
    private static int loc(int program,String name){String key=program+"/"+name;Integer value=LOCATIONS.get(key);if(value==null){value=GL20.glGetUniformLocation(program,name);LOCATIONS.put(key,value);}return value;}
    private static boolean prepare(){if(failed)return false;if(surface!=0)return true;if(!OpenGlHelper.shadersSupported||!OpenGlHelper.isFramebufferEnabled())return false;try{surface=program("neon.vert","neon.frag");blur=program("neon-post.vert","neon-blur.frag");status="Ready / emissive material and depth-tested bloom";return true;}catch(IOException e){failed=true;status="Full-bright fallback";AlienClient.LOG.warn("Neon uses full-bright fallback",e);return false;}}
    private static int program(String vs,String fs)throws IOException{int v=shader(GL20.GL_VERTEX_SHADER,vs),f=0,p=0;try{f=shader(GL20.GL_FRAGMENT_SHADER,fs);p=GL20.glCreateProgram();GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL20.glLinkProgram(p);if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IOException(GL20.glGetProgramInfoLog(p,2048));return p;}catch(IOException e){if(p!=0)GL20.glDeleteProgram(p);throw e;}finally{GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}}
    private static int shader(int type,String name)throws IOException{int s=GL20.glCreateShader(type);try(InputStream in=NeonPipeline.class.getResourceAsStream("/assets/alienclient/shaders/"+name)){if(in==null)throw new IOException(name);ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] bytes=new byte[2048];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);GL20.glShaderSource(s,new String(out.toByteArray(),"UTF-8"));GL20.glCompileShader(s);if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0)throw new IOException(GL20.glGetShaderInfoLog(s,2048));return s;}catch(IOException e){GL20.glDeleteShader(s);throw e;}}
    private static final class Target {
        int framebuffer,texture,w,h;
        void bind(){OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER,framebuffer);}
        void ensure(int width,int height){if(framebuffer!=0&&w==width&&h==height)return;
            if(framebuffer!=0){OpenGlHelper.glDeleteFramebuffers(framebuffer);GL11.glDeleteTextures(texture);}w=width;h=height;framebuffer=OpenGlHelper.glGenFramebuffers();texture=GL11.glGenTextures();bind();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,w,h,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,(java.nio.ByteBuffer)null);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER,OpenGlHelper.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture,0);if(OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER)!=OpenGlHelper.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Incomplete neon framebuffer");
        }
    }
}
