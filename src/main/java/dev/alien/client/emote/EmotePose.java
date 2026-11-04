package dev.alien.client.emote;

import java.util.Arrays;

/** Additive radians for the six vanilla body parts; root offsets are visual only. */
public final class EmotePose {
    public static final int HEAD=0,BODY=1,RIGHT_ARM=2,LEFT_ARM=3,RIGHT_LEG=4,LEFT_LEG=5;
    public final float[] angles=new float[18];
    public float lift,sway,yaw;
    public void clear(){Arrays.fill(angles,0);lift=sway=yaw=0;}
    public void set(int part,double x,double y,double z){int at=part*3;angles[at]=(float)x;angles[at+1]=(float)y;angles[at+2]=(float)z;}
    public void copy(EmotePose p){System.arraycopy(p.angles,0,angles,0,18);lift=p.lift;sway=p.sway;yaw=p.yaw;}
    public void mix(EmotePose a,EmotePose b,float weight){for(int i=0;i<18;i++)angles[i]=a.angles[i]+(b.angles[i]-a.angles[i])*weight;lift=a.lift+(b.lift-a.lift)*weight;sway=a.sway+(b.sway-a.sway)*weight;yaw=a.yaw+(b.yaw-a.yaw)*weight;}
    public void scale(float value){for(int i=0;i<18;i++)angles[i]*=value;lift*=value;sway*=value;yaw*=value;}
    public boolean visible(){if(Math.abs(lift)+Math.abs(sway)+Math.abs(yaw)>.0001f)return true;for(float v:angles)if(Math.abs(v)>.0001f)return true;return false;}
    public boolean finite(){if(!Float.isFinite(lift)||!Float.isFinite(sway)||!Float.isFinite(yaw))return false;for(float v:angles)if(!Float.isFinite(v))return false;return true;}
}
