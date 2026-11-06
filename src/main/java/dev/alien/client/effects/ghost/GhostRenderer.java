package dev.alien.client.effects.ghost;

import dev.alien.client.animation.Motion;
import org.lwjgl.opengl.*;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.OpenGlHelper;

/** One reusable indexed cloth mesh, tubular arms and a continuous body-to-constellation finale. */
public final class GhostRenderer {
    private static final GhostMesh MESH=new GhostMesh();
    private static final int VERTEX_COUNT=(GhostMesh.RINGS+1)*(GhostMesh.SIDES+1),STRIDE=12*4;
    private static final FloatBuffer SURFACE=BufferUtils.createFloatBuffer(VERTEX_COUNT*12);
    private static final IntBuffer INDICES=BufferUtils.createIntBuffer(GhostMesh.RINGS*GhostMesh.SIDES*6);
    private static final double[] P=new double[3],Q=new double[3],A=new double[3],B=new double[3];
    private static long surfaces,finales;
    static{for(int r=0;r<GhostMesh.RINGS;r++)for(int i=0;i<GhostMesh.SIDES;i++){int a=r*(GhostMesh.SIDES+1)+i,b=a+GhostMesh.SIDES+1;INDICES.put(a).put(b).put(a+1).put(a+1).put(b).put(b+1);}INDICES.flip();}
    private GhostRenderer(){}
    public static long surfaces(){return surfaces;}public static long finales(){return finales;}
    public static void draw(float age,GhostConfig c,boolean animate,int particleBudget){
        if(!Float.isFinite(age)||age<=0||age>=c.totalDuration())return;
        dev.alien.client.cosmetic.neon.NeonPipeline.ghost(c.glow,()->drawInternal(age,c,animate,particleBudget));
    }
    private static void drawInternal(float age,GhostConfig c,boolean animate,int particleBudget){
        if(!Float.isFinite(age)||age<=0||age>=c.totalDuration())return;
        float p=GhostMotion.phase(age,c.duration),cut=GhostMotion.dissolve(p),alpha=GhostMotion.alpha(p)*c.opacity;
        int activeTexture=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),clientTexture=GL11.glGetInteger(GL13.GL_CLIENT_ACTIVE_TEXTURE);GL13.glActiveTexture(GL13.GL_TEXTURE0);GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        int program=OpenGlHelper.shadersSupported?GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM):0;
        int array=OpenGlHelper.vboSupported?GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING):0,element=OpenGlHelper.vboSupported?GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING):0;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);GL11.glPushMatrix();
        if(OpenGlHelper.vboSupported){OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER,0);OpenGlHelper.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,0);}
        try{
            if(OpenGlHelper.shadersSupported)GL20.glUseProgram(0);
            GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glDepthMask(false);GL11.glShadeModel(GL11.GL_SMOOTH);
            float time=animate?age:0;float lift=animate?GhostMotion.lift(p,c.height):0;
            GL11.glTranslatef(animate?GhostMotion.sway(age,p,c.calm):0,lift,0);
            GL11.glRotatef(animate?GhostMotion.roll(age,p,c.calm):0,0,0,1);
            GL11.glRotatef(animate?(float)Math.sin(age*1.25)*5*(c.calm?.25f:1):0,0,1,0);GL11.glScalef(c.size,c.size,c.size);
            float release=animate?(float)Math.sin(Motion.window(0,.24,p)*Math.PI)*.065f:0;
            GL11.glScalef(1-release*.5f,1+release,1-release*.5f);
            if(alpha>.001){MESH.update(time,c.calm);surface(c,time,cut,alpha);
                arms(c,time,cut,alpha);face(c,alpha*(1-GhostMotion.smooth(.58f,.9f,cut)),time);
                if(c.echoes&&animate)ribbons(c,time,p,alpha);
                surfaces++;
            }
            if(c.constellation){constellation(c,age,animate,Math.max(0,particleBudget));if(GhostFinale.opacity(age,c)>.01)finales++;}
            else if(c.particles&&animate&&particleBudget>0)looseMotes(c,age,p,Math.min(24,particleBudget));
        }finally{
            if(OpenGlHelper.shadersSupported)GL20.glUseProgram(program);
            GL11.glPopMatrix();GL11.glPopClientAttrib();
            if(OpenGlHelper.vboSupported){OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER,array);OpenGlHelper.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,element);}
            GL11.glPopAttrib();GL13.glActiveTexture(activeTexture);GL13.glClientActiveTexture(clientTexture);
        }
    }
    private static void surface(GhostConfig c,float time,float cut,float alpha){
        boolean shader=GhostMaterial.begin(time,cut,alpha,c.glow);SURFACE.clear();
        for(int r=0;r<=GhostMesh.RINGS;r++)for(int i=0;i<=GhostMesh.SIDES;i++){
            float t=r/(float)GhostMesh.RINGS;int n=(r*(GhostMesh.SIDES+1)+i)*6,color=mix(c.color,c.secondaryColor,.12f+t*.56f);
            float nx=MESH.vertices[n+3],ny=MESH.vertices[n+4],nz=MESH.vertices[n+5];
            float light=shader?1:.52f+.37f*Math.max(0,nx*-.5f+ny*.65f-nz*.55f)+.18f*(1-Math.abs(nz));
            float mask=shader?1:GhostMotion.smooth(cut-.025f,cut+.085f,t)*alpha;
            SURFACE.put(MESH.vertices,n,6).put(((color>>16&255)/255f*(shader?1:.8f)+(shader?0:.2f))*light).put(((color>>8&255)/255f*(shader?1:.8f)+(shader?0:.2f))*light).put(((color&255)/255f*(shader?1:.8f)+(shader?0:.2f))*light).put(mask).put(t).put(i/(float)GhostMesh.SIDES);
        }
        SURFACE.flip();SURFACE.position(0);GL11.glVertexPointer(3,STRIDE,SURFACE);SURFACE.position(3);GL11.glNormalPointer(STRIDE,SURFACE);SURFACE.position(6);GL11.glColorPointer(4,STRIDE,SURFACE);SURFACE.position(10);GL11.glTexCoordPointer(2,STRIDE,SURFACE);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);GL11.glEnable(GL11.GL_CULL_FACE);
        for(int pass=0;pass<2;pass++){GL11.glCullFace(pass==0?GL11.GL_FRONT:GL11.GL_BACK);INDICES.position(0);GL11.glDrawElements(GL11.GL_TRIANGLES,INDICES);}
        GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);if(shader)GL20.glUseProgram(0);
    }
    private static void arms(GhostConfig c,float age,float cut,float alpha){
        float opacity=alpha*(1-GhostMotion.smooth(.29f,.73f,cut));if(opacity<.001)return;
        boolean shader=GhostMaterial.begin(age,cut,opacity*.9f,c.glow);
        for(int side=-1;side<=1;side+=2)for(int j=0;j<24;j++){
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            for(int i=0;i<=12;i++)for(int k=1;k>=0;k--){double t=(j+k)/24.0,a=i*Math.PI/6,r=.094*Math.pow(1-t,.45)+.005;
                GhostMesh.arm(t,side,age,c.calm,P);GhostMesh.arm(Math.max(0,t-.001),side,age,c.calm,A);GhostMesh.arm(Math.min(1,t+.001),side,age,c.calm,B);
                double tx=B[0]-A[0],ty=B[1]-A[1],len=Math.sqrt(tx*tx+ty*ty),nx=-ty/len,ny=tx/len;
                int color=mix(c.color,c.secondaryColor,(float)(.12+P[1]/1.8*.56));float lighting=shader?1:(float)(.52+.37*Math.max(0,Math.cos(a)*ny*.65-Math.sin(a)*.55));
                GL11.glColor4f(((color>>16&255)/255f*(shader?1:.8f)+(shader?0:.2f))*lighting,((color>>8&255)/255f*(shader?1:.8f)+(shader?0:.2f))*lighting,((color&255)/255f*(shader?1:.8f)+(shader?0:.2f))*lighting,shader?1:opacity*.75f);GL11.glNormal3d(nx*Math.cos(a),ny*Math.cos(a),Math.sin(a));GL11.glTexCoord2d(P[1]/1.8,t);
                GL11.glVertex3d(P[0]+nx*Math.cos(a)*r,P[1]+ny*Math.cos(a)*r,P[2]+Math.sin(a)*r);
            }GL11.glEnd();
        }
        if(shader)GL20.glUseProgram(0);
    }
    private static void face(GhostConfig c,float alpha,float age){if(alpha<.001)return;
        float blink=1-.90f*GhostMotion.smooth(1.13f,1.20f,age%3.1f)*(1-GhostMotion.smooth(1.23f,1.33f,age%3.1f));
        float x=(float)GhostMesh.leanX(.76,age,c.calm?.25:1);
        for(int side=-1;side<=1;side+=2){oval(x+side*.127f,1.43f,-.325f,.062f,.088f*blink,0x101528,alpha*.95f);oval(x+side*.127f-.013f,1.456f,-.329f,.012f,.021f*blink,0xebfffc,alpha*.92f);}
        oval(x,1.24f,-.354f,.022f,.031f,0x1c2039,alpha*.6f);
    }
    private static void ribbons(GhostConfig c,float age,float p,float alpha){
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE);for(int side=-1;side<=1;side+=2){GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            for(int i=0;i<=30;i++){double t=i/30.0,y=.1+t*.92,a=t*4.2-age*1.45,x=Math.sin(a)*(.31+.08*(1-t))*side,z=Math.cos(a)*.29;
                float width=(float)(Math.sin(t*Math.PI)*.012),opacity=(float)(Math.sin(t*Math.PI)*alpha*c.glow*.28*(1-GhostMotion.dissolve(p)));
                rgba(c.secondaryColor,opacity);GL11.glVertex3d(x-width,y,z);GL11.glVertex3d(x+width,y,z);
            }GL11.glEnd();}GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    private static void constellation(GhostConfig c,float age,boolean animated,int budget){
        float opacity=(float)GhostFinale.opacity(age,c)*c.opacity;if(opacity<=.0001)return;
        double formation=GhostFinale.formation(age,c);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE);
        // Growing, feathered strokes use geometry, independent of unsupported wide GL lines.
        for(int i=0;i<GhostFinale.STARS;i++){
            double progress=Motion.window(i*.038,.62+i*.038,formation);if(progress<=0)continue;
            GhostFinale.star(i,animated?age:0,c.calm,P);GhostFinale.star((i+1)%GhostFinale.STARS,animated?age:0,c.calm,Q);
            Q[0]=Motion.lerp(P[0],Q[0],progress);Q[1]=Motion.lerp(P[1],Q[1],progress);Q[2]=Motion.lerp(P[2],Q[2],progress);
            stroke(P,Q,.006f,c.secondaryColor,opacity*.72f);
            double twinkle=animated?.85+.15*Math.sin(age*3.4+i*2.4):1;
            spark(P[0],P[1],P[2]-.006,(float)(.040*Motion.window(0,.65,formation)*twinkle),c.secondaryColor,opacity);
        }
        if(c.particles&&animated){int count=Math.min(c.calm?20:60,budget);for(int i=0;i<count;i++){
            double t=Motion.window(c.duration*(.60+(i%7)*.018),c.duration+c.finaleDuration*.32,age);
            if(t<=0||t>=1)continue;GhostFinale.mote(i,age,c,t,i*2.3999632297,.09+(i%19)/20.0,A,P);
            spark(P[0],P[1],P[2],.020f,c.secondaryColor,opacity*(float)Math.sin(t*Math.PI));
        }}GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    private static void looseMotes(GhostConfig c,float age,float p,int count){GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE);for(int i=0;i<count;i++){double q=(p-(.12+(i%17)/17.0*.73))/.27;if(q<=0||q>=1)continue;double a=i*2.399963;float fade=(float)Math.sin(q*Math.PI)*GhostMotion.alpha(p)*c.opacity;spark(Math.cos(a+q*.45)*(.24+q*.28),(i%13)/13.0*1.55+q*.42,Math.sin(a+q*.45)*(.24+q*.28),.022f,c.secondaryColor,fade);}GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);}
    private static void stroke(double[] a,double[] b,float width,int color,float alpha){double dx=b[0]-a[0],dy=b[1]-a[1],len=Math.sqrt(dx*dx+dy*dy);if(len<1e-6)return;double nx=-dy/len*width,ny=dx/len*width;for(int band=-1;band<1;band++){GL11.glBegin(GL11.GL_TRIANGLE_STRIP);rgba(color,band==0?alpha:0);GL11.glVertex3d(a[0]+nx*band,a[1]+ny*band,a[2]);GL11.glVertex3d(b[0]+nx*band,b[1]+ny*band,b[2]);rgba(color,band==0?0:alpha);GL11.glVertex3d(a[0]+nx*(band+1),a[1]+ny*(band+1),a[2]);GL11.glVertex3d(b[0]+nx*(band+1),b[1]+ny*(band+1),b[2]);GL11.glEnd();}}
    private static void spark(double x,double y,double z,float size,int color,float alpha){oval((float)x,(float)y,(float)z,size,size,color,alpha*.45f);rgba(color,0);GL11.glBegin(GL11.GL_TRIANGLE_FAN);rgba(color,alpha);GL11.glVertex3d(x,y,z-.002);for(int i=0;i<=16;i++){rgba(color,0);double a=i*Math.PI/8,r=i%4==0?size*2.6:size*.45;GL11.glVertex3d(x+Math.cos(a)*r,y+Math.sin(a)*r,z-.002);}GL11.glEnd();}
    private static void oval(float x,float y,float z,float w,float h,int color,float alpha){GL11.glBegin(GL11.GL_TRIANGLE_FAN);rgba(color,alpha);GL11.glVertex3f(x,y,z);for(int i=0;i<=48;i++){double a=i*Math.PI/24;rgba(color,alpha*.90f);GL11.glVertex3d(x+Math.cos(a)*w,y+Math.sin(a)*h,z);}GL11.glEnd();}
    private static int mix(int a,int b,float t){return (Math.round((a>>16&255)*(1-t)+(b>>16&255)*t)<<16)|(Math.round((a>>8&255)*(1-t)+(b>>8&255)*t)<<8)|Math.round((a&255)*(1-t)+(b&255)*t);}
    private static void rgba(int rgb,float a){GL11.glColor4f((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,Math.max(0,Math.min(1,a)));}
}
