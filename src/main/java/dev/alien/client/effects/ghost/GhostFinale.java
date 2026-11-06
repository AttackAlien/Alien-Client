package dev.alien.client.effects.ghost;

import dev.alien.client.animation.Motion;

/** Body-to-star choreography in local coordinates, shared by world and Studio preview. */
public final class GhostFinale {
    public static final int STARS=10;
    private GhostFinale(){}
    public static double formation(float age,GhostConfig c){return c.constellation?Motion.window(c.duration*.64,c.duration+c.finaleDuration*.40,age):0;}
    public static double opacity(float age,GhostConfig c){return c.constellation?Motion.window(c.duration*.64,c.duration*.93,age)*(1-Motion.window(c.duration+c.finaleDuration*.48,c.totalDuration(),age)):0;}
    public static void star(int i,double age,boolean calm,double[] out){
        double a=-Math.PI*.5+(i%STARS)*Math.PI/5,r=(i%2==0?.58:.275);
        out[0]=Math.cos(a)*r;out[1]=1.0-Math.sin(a)*r;
        out[2]=Math.sin(a*2+age*.4)*(calm?.025:.085)-.05;
    }
    public static void mote(int i,double age,GhostConfig c,double[] out){
        double start=c.duration*(.60+(i%7)*.018),t=Motion.window(start,c.duration+c.finaleDuration*.32,age);
        double angle=i*2.3999632297,bodyT=.09+(i%19)/20.0;
        double[] startPoint=new double[3]; // Only used by tests; renderer uses the allocation-free overload.
        mote(i,age,c,t,angle,bodyT,startPoint,out);
    }
    public static void mote(int i,double age,GhostConfig c,double t,double angle,double bodyT,double[] scratch,double[] out){
        GhostMesh.point(angle,bodyT,age,c.calm,scratch);star(i%STARS,age,c.calm,out);
        double sx=scratch[0],sy=scratch[1],sz=scratch[2],ex=out[0],ey=out[1],ez=out[2],side=(i%2==0?1:-1);
        out[0]=Motion.bezier(sx,sx+side*.32,ex-side*.13,ex,t);
        out[1]=Motion.bezier(sy,sy+.28,ey+.20,ey,t);
        out[2]=Motion.bezier(sz,sz+.18,ez-.18,ez,t);
    }
}
