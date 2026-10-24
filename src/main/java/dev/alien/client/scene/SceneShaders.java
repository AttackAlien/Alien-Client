package dev.alien.client.scene;
import dev.alien.client.AlienClient;
import java.io.*;import java.nio.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;import org.lwjgl.opengl.*;import org.lwjgl.util.vector.Matrix4f;
/** Original GLSL 1.20 effects. Snapshots the bound framebuffer; never samples the active color attachment. */
public final class SceneShaders {
    private static int post,glass,color,depth,width,height,depthWidth,depthHeight;
    private static final java.util.Map<String,Integer> locations=new java.util.HashMap<String,Integer>();
    private static final FloatBuffer waveData=BufferUtils.createFloatBuffer(16),pathData=BufferUtils.createFloatBuffer(128);private static boolean failed;
    private static String status="Not initialized";private static long frames,glassDraws;
    private static final FloatBuffer matrix=BufferUtils.createFloatBuffer(16),inverse=BufferUtils.createFloatBuffer(16);
    private static final Matrix4f mat=new Matrix4f();
    public static String status(){return status;}public static long frames(){return frames;}public static long glassDraws(){return glassDraws;}
    private static String read(String name)throws IOException{try(InputStream s=SceneShaders.class.getResourceAsStream("/assets/alienclient/shaders/"+name)){if(s==null)throw new IOException("Missing shader "+name);ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while((n=s.read(buf))!=-1)b.write(buf,0,n);return new String(b.toByteArray(),"UTF-8");}}
    private static int shader(int type,String file)throws IOException{int s=GL20.glCreateShader(type);GL20.glShaderSource(s,read(file));GL20.glCompileShader(s);if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0){String e=GL20.glGetShaderInfoLog(s,4096);GL20.glDeleteShader(s);throw new IOException(e);}return s;}
    private static int program(String v,String f)throws IOException{int a=shader(GL20.GL_VERTEX_SHADER,v),b=shader(GL20.GL_FRAGMENT_SHADER,f),p=GL20.glCreateProgram();GL20.glAttachShader(p,a);GL20.glAttachShader(p,b);GL20.glLinkProgram(p);GL20.glDeleteShader(a);GL20.glDeleteShader(b);if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0){String e=GL20.glGetProgramInfoLog(p,4096);GL20.glDeleteProgram(p);throw new IOException(e);}return p;}
    private static boolean prepare(){
        if(failed)return false;if(post!=0)return true;
        if(!OpenGlHelper.shadersSupported||!OpenGlHelper.isFramebufferEnabled()){status="Requires shaders and framebuffer support";return false;}
        try{post=program("scene.vert","scene.frag");glass=program("glass.vert","glass.frag");color=GL11.glGenTextures();depth=GL11.glGenTextures();status="Ready / GLSL 1.20";return true;}
        catch(Exception e){failed=true;status="Shader unavailable: "+e.getMessage();AlienClient.LOG.error("Scene shader compilation failed",e);return false;}
    }
    private static void capture(boolean withDepth){Minecraft m=Minecraft.getMinecraft();
        boolean resize=width!=m.displayWidth||height!=m.displayHeight;width=m.displayWidth;height=m.displayHeight;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,color);if(resize)GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGB,0,0,width,height,0);else GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,0,0,width,height);
        if(resize)parameters();if(withDepth){GL11.glBindTexture(GL11.GL_TEXTURE_2D,depth);
            if(depthWidth!=width||depthHeight!=height){GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_DEPTH_COMPONENT,0,0,width,height,0);parameters();depthWidth=width;depthHeight=height;}
            else GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,0,0,width,height);
        }
    }
    private static void parameters(){GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);}
    private static int loc(int p,String s){String key=p+":"+s;Integer location=locations.get(key);if(location==null){location=GL20.glGetUniformLocation(p,s);locations.put(key,location);}return location;}
    private static void f(int p,String s,float v){GL20.glUniform1f(loc(p,s),v);}
    private static void accent(int p,SceneConfig c){GL20.glUniform3f(loc(p,"Accent"),(c.color>>16&255)/255f,(c.color>>8&255)/255f,(c.color&255)/255f);f(p,"Strength",c.strength);}
    private static void invert(int kind,int p,String name){matrix.clear();GL11.glGetFloat(kind,matrix);matrix.position(0);mat.load(matrix);if(Matrix4f.invert(mat,mat)==null)mat.setIdentity();inverse.clear();mat.store(inverse);inverse.flip();GL20.glUniformMatrix4(loc(p,name),false,inverse);}
    public static void render(SceneConfig c,SceneHistory waves,SceneHistory path,double now,float partial){render(c,waves,path,now,partial,false);}
    public static void render(SceneConfig c,SceneHistory waves,SceneHistory path,double now,float partial,boolean afterHand){
        if(!prepare())return;Minecraft m=Minecraft.getMinecraft();int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),previousProgram=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);int binding0=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL13.glActiveTexture(GL13.GL_TEXTURE1);int binding1=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{
            capture(true);GL20.glUseProgram(post);GL20.glUniform1i(loc(post,"Preview"),0);invert(GL11.GL_PROJECTION_MATRIX,post,"InvProjection");invert(GL11.GL_MODELVIEW_MATRIX,post,"InvView");
            GL13.glActiveTexture(GL13.GL_TEXTURE0);GL11.glBindTexture(GL11.GL_TEXTURE_2D,color);GL20.glUniform1i(loc(post,"Scene"),0);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glBindTexture(GL11.GL_TEXTURE_2D,depth);GL20.glUniform1i(loc(post,"Depth"),1);GL13.glActiveTexture(GL13.GL_TEXTURE0);
            net.minecraft.entity.Entity e=m.getRenderViewEntity();GL20.glUniform3f(loc(post,"Camera"),(float)(e.lastTickPosX+(e.posX-e.lastTickPosX)*partial),(float)(e.lastTickPosY+(e.posY-e.lastTickPosY)*partial),(float)(e.lastTickPosZ+(e.posZ-e.lastTickPosZ)*partial));
            accent(post,c);f(post,"Time",(float)now);f(post,"SkySpeed",c.skySpeed);f(post,"SkyLiveliness",c.skyLiveliness);f(post,"SkyBreathing",c.skyBreathing);f(post,"MistDensity",c.mistDensity);f(post,"BokehStrength",c.bokehStrength);GL20.glUniform2f(loc(post,"Size"),width,height);
            GL20.glUniform1i(loc(post,"Mist"),c.mist?1:0);GL20.glUniform1i(loc(post,"Bokeh"),c.bokeh?1:0);GL20.glUniform1i(loc(post,"AfterHand"),afterHand?1:0);int palette=0;for(int i=0;i<SceneConfig.PALETTES.length;i++)if(SceneConfig.PALETTES[i].equals(c.palette))palette=i;GL20.glUniform1i(loc(post,"Palette"),palette);f(post,"Radius",c.radius);f(post,"WaveLife",c.lifetime);f(post,"ChromaRadius",c.chromaRadius);f(post,"ChromaMemory",c.chromaMemory);
            GL20.glUniform1i(loc(post,"Mode"),c.modeIndex());GL20.glUniform1i(loc(post,"Sky"),c.skyIndex());GL20.glUniform1i(loc(post,"WaveCount"),c.shockwave?waves.size():0);GL20.glUniform1i(loc(post,"PathCount"),path.size());
            upload(waves,waveData,post,"Waves[0]",now);upload(path,pathData,post,"Path[0]",now);
            GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glViewport(0,0,width,height);
            GL11.glBegin(GL11.GL_QUADS);GL11.glTexCoord2f(0,0);GL11.glVertex2f(-1,-1);GL11.glTexCoord2f(1,0);GL11.glVertex2f(1,-1);GL11.glTexCoord2f(1,1);GL11.glVertex2f(1,1);GL11.glTexCoord2f(0,1);GL11.glVertex2f(-1,1);GL11.glEnd();frames++;
        }finally{GL20.glUseProgram(previousProgram);GL11.glPopAttrib();GL13.glActiveTexture(GL13.GL_TEXTURE0);GL11.glBindTexture(GL11.GL_TEXTURE_2D,binding0);GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glBindTexture(GL11.GL_TEXTURE_2D,binding1);GL13.glActiveTexture(active);}
    }
    public static void skyPreview(SceneConfig c,int sky,int x,int y,int w,int h,int guiWidth,int guiHeight){
        if(!prepare())return;Minecraft m=Minecraft.getMinecraft();int previous=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{float sx=m.displayWidth/(float)guiWidth,sy=m.displayHeight/(float)guiHeight;GL11.glViewport(Math.round(x*sx),m.displayHeight-Math.round((y+h)*sy),Math.round(w*sx),Math.round(h*sy));GL20.glUseProgram(post);GL20.glUniform1i(loc(post,"Preview"),1);GL20.glUniform1i(loc(post,"Sky"),sky);f(post,"Time",(float)(System.nanoTime()/1e9%10000));f(post,"SkySpeed",c.skySpeed);f(post,"SkyLiveliness",c.skyLiveliness);f(post,"SkyBreathing",c.skyBreathing);int palette=0;for(int i=0;i<SceneConfig.PALETTES.length;i++)if(SceneConfig.PALETTES[i].equals(c.palette))palette=i;GL20.glUniform1i(loc(post,"Palette"),palette);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glBegin(GL11.GL_QUADS);GL11.glTexCoord2f(0,0);GL11.glVertex2f(-1,-1);GL11.glTexCoord2f(1,0);GL11.glVertex2f(1,-1);GL11.glTexCoord2f(1,1);GL11.glVertex2f(1,1);GL11.glTexCoord2f(0,1);GL11.glVertex2f(-1,1);GL11.glEnd();}
        finally{GL20.glUniform1i(loc(post,"Preview"),0);GL20.glUseProgram(previous);GL11.glPopAttrib();}
    }
    private static void upload(SceneHistory h,FloatBuffer b,int program,String name,double now){if(h.size()==0)return;b.clear();for(int i=0;i<h.size();i++)b.put((float)h.x(i)).put((float)h.y(i)).put((float)h.z(i)).put((float)h.age(i,now));b.flip();GL20.glUniform4(loc(program,name),b);}
    public static void glass(Runnable draw){
        if(!prepare()){draw.run();return;}int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),previous=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);int binding0=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL13.glActiveTexture(GL13.GL_TEXTURE1);int binding=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{capture(false);GL11.glBindTexture(GL11.GL_TEXTURE_2D,color);GL20.glUseProgram(glass);GL20.glUniform1i(loc(glass,"Scene"),1);GL20.glUniform2f(loc(glass,"Size"),width,height);accent(glass,AlienClient.get().config().scenes);GL13.glActiveTexture(GL13.GL_TEXTURE0);draw.run();glassDraws++;}
        finally{GL20.glUseProgram(previous);GL11.glPopAttrib();GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glBindTexture(GL11.GL_TEXTURE_2D,binding);GL13.glActiveTexture(GL13.GL_TEXTURE0);net.minecraft.client.renderer.GlStateManager.bindTexture(binding0);GL13.glActiveTexture(active);}
    }
}
