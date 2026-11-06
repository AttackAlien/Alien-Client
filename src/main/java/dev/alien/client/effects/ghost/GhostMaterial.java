package dev.alien.client.effects.ghost;

import dev.alien.client.AlienClient;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL20;
import java.io.*;

/** GLSL 1.20 pearl/cloth lighting. Failure is sticky and leaves the CPU-lit path available. */
public final class GhostMaterial {
    private static int program,time,cut,opacity,glow,mask;private static boolean failed;
    private static String status="Not initialized";
    private GhostMaterial(){}
    public static String status(){return status;}
    public static boolean ready(){return program!=0&&!failed;}
    public static boolean begin(float age,float dissolve,float alpha,float bloom){
        if(Boolean.getBoolean("alien.ghost.fallback")||!OpenGlHelper.shadersSupported||failed)return false;
        try{
            if(program==0){int v=compile(GL20.GL_VERTEX_SHADER,"ghost.vert"),f=0;
                try{f=compile(GL20.GL_FRAGMENT_SHADER,"ghost.frag");program=GL20.glCreateProgram();GL20.glAttachShader(program,v);GL20.glAttachShader(program,f);GL20.glLinkProgram(program);
                    if(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)==0)throw new IOException(GL20.glGetProgramInfoLog(program,2048));
                }finally{GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}
                mask=GL20.glGetUniformLocation(program,"Mask");time=GL20.glGetUniformLocation(program,"Time");cut=GL20.glGetUniformLocation(program,"Cut");opacity=GL20.glGetUniformLocation(program,"Opacity");glow=GL20.glGetUniformLocation(program,"Glow");status="Ready / GLSL 1.20";
            }
            GL20.glUseProgram(program);GL20.glUniform1i(mask,dev.alien.client.cosmetic.neon.NeonPipeline.maskPass()?1:0);GL20.glUniform1f(time,age);GL20.glUniform1f(cut,dissolve);GL20.glUniform1f(opacity,alpha);GL20.glUniform1f(glow,bloom);return true;
        }catch(Exception e){failed=true;status="CPU lighting / shader unavailable";if(program!=0){GL20.glDeleteProgram(program);program=0;}AlienClient.LOG.warn("Ghost material uses CPU fallback",e);return false;}
    }
    private static int compile(int type,String name)throws IOException{
        int shader=GL20.glCreateShader(type);
        try(InputStream in=GhostMaterial.class.getResourceAsStream("/assets/alienclient/shaders/"+name)){
            if(in==null)throw new IOException("Missing "+name);ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] bytes=new byte[2048];int count;while((count=in.read(bytes))!=-1)out.write(bytes,0,count);
            GL20.glShaderSource(shader,new String(out.toByteArray(),"UTF-8"));GL20.glCompileShader(shader);
            if(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS)==0)throw new IOException(GL20.glGetShaderInfoLog(shader,2048));return shader;
        }catch(IOException e){GL20.glDeleteShader(shader);throw e;}
    }
}
