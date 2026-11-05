package dev.alien.client.effects.ghost;

import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.OpenGlHelper;

/** Original rounded spirit geometry. No entity, skin, texture registry or display-list allocation. */
public final class GhostRenderer {
    private static final int SIDES=40,RINGS=32;
    private static final double[] SIN=new double[SIDES+1],COS=new double[SIDES+1];
    private static final FloatBuffer SURFACE=BufferUtils.createFloatBuffer(RINGS*(SIDES+1)*2*7);
    private static final float[] VERTICES=new float[(RINGS+1)*(SIDES+1)*3];
    static {for(int i=0;i<=SIDES;i++){SIN[i]=Math.sin(i*Math.PI*2/SIDES);COS[i]=Math.cos(i*Math.PI*2/SIDES);}}
    private GhostRenderer(){}
    public static void draw(float age,GhostConfig c,boolean animate,int particleBudget){
        float p=GhostMotion.phase(age,c.duration);if(p<=0||p>=1)return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);GL11.glPushMatrix();
        int oldArray=OpenGlHelper.vboSupported?GL11.glGetInteger(34964):0;
        if(OpenGlHelper.vboSupported)OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER,0);
        try{
            GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glDepthMask(false);GL11.glShadeModel(GL11.GL_SMOOTH);
            float lift=animate?GhostMotion.lift(p,c.height):0,sway=animate?GhostMotion.sway(age,p,c.calm):0;
            GL11.glTranslatef(sway,lift,0);GL11.glRotatef(animate?GhostMotion.roll(age,p,c.calm):0,0,0,1);GL11.glRotatef(animate?(float)Math.sin(age*1.6)*8*(c.calm?.3f:1):0,0,1,0);GL11.glScalef(c.size,c.size,c.size);
            fill(animate?age:0,c.calm);
            float dissolve=GhostMotion.dissolve(p),alpha=GhostMotion.alpha(p)*c.opacity;
            // Back faces first, then front faces, with depth testing retained from the world pass.
            if(c.glow>0){GL11.glPushMatrix();GL11.glScalef(1.075f,1.02f,1.075f);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE);surface(c,dissolve,alpha*c.glow*.11f);GL11.glPopMatrix();GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);}
            surface(c,dissolve,alpha*.60f);
            if(c.echoes&&animate&&!c.calm){for(int echo=2;echo>=1;echo--){GL11.glPushMatrix();GL11.glTranslatef((float)Math.sin(age*2-echo*.35)*.025f,-echo*.105f,echo*.055f);GL11.glScalef(1+echo*.018f,1,1+echo*.018f);surface(c,Math.min(1,dissolve+.05f),alpha*.065f/echo);GL11.glPopMatrix();}}
            face(c,alpha*(1-GhostMotion.smooth(.66f,.86f,dissolve)),animate?age:0);
            arms(c,animate?age:0,dissolve,alpha);
            if(c.particles&&animate&&particleBudget>0)motes(c,age,p,Math.min(c.calm?12:48,particleBudget));
        }finally{GL11.glPopMatrix();GL11.glPopClientAttrib();if(OpenGlHelper.vboSupported)OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER,oldArray);GL11.glPopAttrib();}
    }
    private static void fill(float age,boolean calm){int n=0;for(int r=0;r<=RINGS;r++){float t=r/(float)RINGS;double cap=t>.66?(t-.66)/.34*Math.PI/2:0,bodyY=t>.66?(.66+.34*Math.sin(cap))*1.72:t*1.72;double radius=t>.66?.345*Math.cos(cap):.27+.075*Math.sin(t/.66*Math.PI/2);for(int i=0;i<=SIDES;i++){double angle=i*Math.PI*2/SIDES,w=GhostMotion.cloth((float)angle,t,age,calm);VERTICES[n++]=(float)((radius+w)*COS[i]+Math.sin(age*2-t*4)*.025*(1-t));VERTICES[n++]=(float)(bodyY+Math.sin(angle*3+age*2.1)*.095*Math.pow(1-t,6));VERTICES[n++]=(float)((radius+w)*SIN[i]+Math.sin(age*1.7-t*3)*.018*(1-t));}}}
    private static void surface(GhostConfig c,float cut,float alpha){
        SURFACE.clear();
        for(int r=0;r<RINGS;r++)for(int i=0;i<=SIDES;i++)for(int k=1;k>=0;k--){
            float t=(r+k)/(float)RINGS,a=GhostMotion.smooth(cut-.025f,cut+.13f,t)*alpha;int color=mix(c.color,c.secondaryColor,.18f+t*.52f),n=((r+k)*(SIDES+1)+i)*3;
            SURFACE.put((color>>16&255)/255f).put((color>>8&255)/255f).put((color&255)/255f).put(a).put(VERTICES[n]).put(VERTICES[n+1]).put(VERTICES[n+2]);
        }
        SURFACE.flip();SURFACE.position(4);GL11.glVertexPointer(3,28,SURFACE);SURFACE.position(0);GL11.glColorPointer(4,28,SURFACE);GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);GL11.glEnable(GL11.GL_CULL_FACE);
        for(int pass=0;pass<2;pass++){GL11.glCullFace(pass==0?GL11.GL_FRONT:GL11.GL_BACK);for(int r=0;r<RINGS;r++)GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP,r*(SIDES+1)*2,(SIDES+1)*2);}
        GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }
    private static void face(GhostConfig c,float alpha,float age){if(alpha<=0)return;float blink=1-.88f*GhostMotion.smooth(.0f,.055f,(age%2.4f)-1.15f)*(1-GhostMotion.smooth(.07f,.13f,(age%2.4f)-1.15f));for(int s:new int[]{-1,1}){oval(s*.125f,1.35f,-.333f,.072f,.112f*blink,c.secondaryColor,alpha*.22f);oval(s*.125f,1.35f,-.34f,.049f,.086f*blink,0x101425,alpha*.93f);oval(s*.125f-.014f,1.38f,-.346f,.013f,.022f*blink,0xe1fff7,alpha*.86f);}oval(0,1.16f,-.335f,.027f,.038f,0x14182B,alpha*.65f);}
    private static void oval(float x,float y,float z,float w,float h,int color,float alpha){rgba(color,alpha);GL11.glBegin(GL11.GL_TRIANGLE_FAN);GL11.glVertex3f(x,y,z);for(int i=0;i<=SIDES;i++)GL11.glVertex3d(x+COS[i]*w,y+SIN[i]*h,z);GL11.glEnd();}
    private static void arms(GhostConfig c,float age,float cut,float alpha){if(cut>.72)return;for(int side:new int[]{-1,1}){GL11.glBegin(GL11.GL_TRIANGLE_STRIP);for(int j=0;j<=16;j++){float t=j/16f;double x=side*(.29+t*.22),y=.92-t*.42+Math.sin(age*2.1-t*2.4-side*.35)*.055,z=-.015+Math.sin(age*1.8-t*2)*.055,width=.09*Math.sin(Math.PI*(.15+t*.85))*(1-t*.8);rgba(mix(c.color,c.secondaryColor,t*.8f),alpha*.65f*(1-t*.3f)*(1-GhostMotion.smooth(.38f,.72f,cut)));GL11.glVertex3d(x,y+width,z-.035);GL11.glVertex3d(x,y-width,z+.035);}GL11.glEnd();}}
    private static void motes(GhostConfig c,float age,float p,int count){GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE);for(int i=0;i<count;i++){float start=.12f+(i%17)/17f*.73f,q=(p-start)/.27f;if(q<=0||q>=1)continue;double a=i*2.399963,rad=.24+q*.28,x=Math.cos(a+q*.45)*rad,y=(i%13)/13f*1.55+q*.42,z=Math.sin(a+q*.45)*rad;float size=(.012f+(i%3)*.004f)*(float)Math.sin(Math.PI*q);rgba(mix(c.color,c.secondaryColor,q),c.opacity*.6f*(float)Math.sin(Math.PI*q));GL11.glBegin(GL11.GL_TRIANGLE_FAN);GL11.glVertex3d(x,y,z);for(int j=0;j<=8;j++){GL11.glColor4f(0,0,0,0);double b=j*Math.PI/4;GL11.glVertex3d(x+Math.cos(b)*size,y+Math.sin(b)*size,z);}GL11.glEnd();}GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);}
    private static int mix(int a,int b,float t){return (Math.round((a>>16&255)*(1-t)+(b>>16&255)*t)<<16)|(Math.round((a>>8&255)*(1-t)+(b>>8&255)*t)<<8)|Math.round((a&255)*(1-t)+(b&255)*t);}
    private static void rgba(int rgb,float a){GL11.glColor4f((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,Math.max(0,Math.min(1,a)));}
}
