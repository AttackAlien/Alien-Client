package dev.alien.client.effects.ghost;

import dev.alien.client.animation.Motion;

/** Reusable original cloth surface. Derivatives supply smooth outward normals, including the cap seam. */
public final class GhostMesh {
    public static final int SIDES=48,RINGS=36,STRIDE=6;
    public final float[] vertices=new float[(SIDES+1)*(RINGS+1)*STRIDE];
    private final double[] point=new double[3];
    public void update(double age,boolean calm){
        for(int ring=0;ring<=RINGS;ring++)for(int side=0;side<=SIDES;side++){
            int n=(ring*(SIDES+1)+side)*STRIDE;point(side*Math.PI*2/SIDES,ring/(double)RINGS,age,calm,point);
            vertices[n]=(float)point[0];vertices[n+1]=(float)point[1];vertices[n+2]=(float)point[2];
        }
        // Differentiate the already sampled grid: five times fewer cloth evaluations per frame.
        for(int ring=0;ring<=RINGS;ring++)for(int side=0;side<=SIDES;side++){
            int n=(ring*(SIDES+1)+side)*STRIDE,left=(ring*(SIDES+1)+(side+SIDES-1)%SIDES)*STRIDE,right=(ring*(SIDES+1)+(side+1)%SIDES)*STRIDE;
            int lower=(Math.max(0,ring-1)*(SIDES+1)+side)*STRIDE,upper=(Math.min(RINGS,ring+1)*(SIDES+1)+side)*STRIDE;
            double ux=vertices[right]-vertices[left],uy=vertices[right+1]-vertices[left+1],uz=vertices[right+2]-vertices[left+2];
            double vx=vertices[upper]-vertices[lower],vy=vertices[upper+1]-vertices[lower+1],vz=vertices[upper+2]-vertices[lower+2];
            double nx=vy*uz-vz*uy,ny=vz*ux-vx*uz,nz=vx*uy-vy*ux,len=Math.sqrt(nx*nx+ny*ny+nz*nz);
            if(ring==RINGS||len<1e-10){nx=0;ny=1;nz=0;len=1;}
            vertices[n+3]=(float)(nx/len);vertices[n+4]=(float)(ny/len);vertices[n+5]=(float)(nz/len);
        }
    }
    public static void point(double angle,double t,double age,boolean calm,double[] out){
        double cap=Math.max(0,(t-.64)/.36)*Math.PI*.5;
        double radius=t>.64?.395*Math.cos(cap):.29+.105*Math.sin(t/.64*Math.PI*.5);
        double y=t>.64?1.08+.72*Math.sin(cap):1.08*t/.64;
        double lower=1-Motion.window(.05,.70,t),motion=calm?.25:1;
        double wave=(Math.sin(angle*3-age*2.1-t*3.8)*.037+Math.sin(angle*5+age*1.27-t*2)*.012)*lower*lower*motion;
        double tail=Math.pow(1-t,7)*(.105*Math.cos(angle*3-age*1.2)+.03*Math.sin(angle*5+age*1.7))*motion;
        out[0]=(radius+wave)*Math.cos(angle)+leanX(t,age,motion);
        out[1]=y+tail;
        out[2]=(radius+wave)*Math.sin(angle)*.88+Math.sin(age*1.7-t*3)*.028*lower*motion;
    }
    public static double leanX(double t,double age,double strength){return Math.sin(age*1.75-t*2.7)*.028*strength;}
    public static void arm(double t,int side,double age,boolean calm,double[] out){
        double m=calm?.25:1,lag=Math.sin(age*1.85-t*2.8-side*.24)*m;
        out[0]=side*Motion.bezier(.32,.51,.65,.57,t);
        out[1]=Motion.bezier(1.03,.99,.64,.73,t)+lag*.075*t;
        out[2]=-.01+Math.sin(age*1.5-t*2.1)*.07*t*m;
    }
}
