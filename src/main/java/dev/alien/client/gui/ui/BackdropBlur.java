package dev.alien.client.gui.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

/** Optional bounded quarter-resolution two-pass Gaussian blur of the actual world framebuffer. */
public final class BackdropBlur {
    private static Framebuffer horizontal, vertical;
    private static int program;
    private static boolean failed;
    private static long frames;
    private BackdropBlur() { }
    public static boolean supported() {
        return !failed && GLContext.getCapabilities().OpenGL20
                && net.minecraft.client.renderer.OpenGlHelper.isFramebufferEnabled();
    }
    public static long renderedFrames() { return frames; }
    public static boolean render(float amount) {
        Minecraft mc=Minecraft.getMinecraft();
        if (amount<=.005f || mc.theWorld==null || !supported()) return false;
        int oldProgram=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int oldActive=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int previousTexture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int oldMatrixMode=GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();
        try {
            if(program==0) program=makeProgram();
            int w=Math.max(1,mc.displayWidth/4),h=Math.max(1,mc.displayHeight/4);
            if(horizontal==null || horizontal.framebufferWidth!=w || horizontal.framebufferHeight!=h) {
                if(horizontal!=null){horizontal.deleteFramebuffer();vertical.deleteFramebuffer();}
                horizontal=new Framebuffer(w,h,false);vertical=new Framebuffer(w,h,false);
                horizontal.setFramebufferFilter(GL11.GL_LINEAR);vertical.setFramebufferFilter(GL11.GL_LINEAR);
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glColor4f(1,1,1,1);
            GL20.glUseProgram(program);GL20.glUniform1i(GL20.glGetUniformLocation(program,"image"),0);
            float radius=.8f+Math.min(1,amount)*2.6f;
            pass(horizontal,mc.getFramebuffer().framebufferTexture,w,h,radius/w,0);
            pass(vertical,horizontal.framebufferTexture,w,h,0,radius/h);
            mc.getFramebuffer().bindFramebuffer(false);
            GL11.glViewport(0,0,mc.displayWidth,mc.displayHeight);
            GL20.glUseProgram(0);projection(mc.displayWidth,mc.displayHeight);
            quad(vertical.framebufferTexture,mc.displayWidth,mc.displayHeight);
            frames++;return true;
        } catch(RuntimeException failure) {
            failed=true;dev.alien.client.AlienClient.LOG.warn("Optional backdrop blur unavailable",failure);return false;
        } finally {
            mc.getFramebuffer().bindFramebuffer(false);
            GL20.glUseProgram(oldProgram);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopAttrib();GL13.glActiveTexture(oldActive);
            // Raw shader drawing keeps MC's texture cache intact; restore the actual binding too.
            GL11.glBindTexture(GL11.GL_TEXTURE_2D,previousTexture);
            GlStateManager.bindTexture(previousTexture);
            GL11.glMatrixMode(oldMatrixMode);
        }
    }
    private static void pass(Framebuffer target,int texture,int w,int h,float dx,float dy) {
        target.bindFramebuffer(false);GL11.glViewport(0,0,w,h);projection(w,h);
        GL20.glUniform2f(GL20.glGetUniformLocation(program,"stepSize"),dx,dy);quad(texture,w,h);
    }
    private static void projection(int w,int h) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(0,w,h,0,-1,1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
    }
    private static void quad(int texture,int w,int h) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0,1);GL11.glVertex2f(0,0);GL11.glTexCoord2f(1,1);GL11.glVertex2f(w,0);
        GL11.glTexCoord2f(1,0);GL11.glVertex2f(w,h);GL11.glTexCoord2f(0,0);GL11.glVertex2f(0,h);GL11.glEnd();
    }
    private static int makeProgram() {
        int vertex=compile(GL20.GL_VERTEX_SHADER,"#version 120\nvarying vec2 uv; void main(){ gl_Position=gl_ModelViewProjectionMatrix*gl_Vertex; uv=gl_MultiTexCoord0.xy; }");
        int fragment=compile(GL20.GL_FRAGMENT_SHADER,"#version 120\nuniform sampler2D image; uniform vec2 stepSize; varying vec2 uv; void main(){vec4 c=texture2D(image,uv)*0.227027; c+=texture2D(image,uv+stepSize*1.384615)*0.316216; c+=texture2D(image,uv-stepSize*1.384615)*0.316216; c+=texture2D(image,uv+stepSize*3.230769)*0.070270; c+=texture2D(image,uv-stepSize*3.230769)*0.070270; gl_FragColor=vec4(c.rgb,1.0);}");
        int p=GL20.glCreateProgram();GL20.glAttachShader(p,vertex);GL20.glAttachShader(p,fragment);GL20.glLinkProgram(p);
        String log=GL20.glGetProgramInfoLog(p,2048);boolean ok=GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)!=0;
        GL20.glDeleteShader(vertex);GL20.glDeleteShader(fragment);
        if(!ok){GL20.glDeleteProgram(p);throw new IllegalStateException(log);}return p;
    }
    private static int compile(int type,String source) {
        int shader=GL20.glCreateShader(type);GL20.glShaderSource(shader,source);GL20.glCompileShader(shader);
        if(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS)==0){String log=GL20.glGetShaderInfoLog(shader,2048);GL20.glDeleteShader(shader);throw new IllegalStateException(log);}return shader;
    }
}
