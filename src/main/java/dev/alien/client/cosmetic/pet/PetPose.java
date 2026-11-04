package dev.alien.client.cosmetic.pet;

/** Continuous local joint animation, independent of rendering frame count. Angles are degrees. */
public final class PetPose {
    public float bob,tilt,headYaw,headTilt,rightArm,leftArm,feet,antenna,eyes=1;
    public void sample(double time,float movement,float sleeping,float dancing,boolean animated){
        bob=tilt=headYaw=headTilt=rightArm=leftArm=feet=antenna=0;eyes=1;if(!animated)return;
        double t=Double.isFinite(time)?time:0;float m=unit(movement),s=unit(sleeping),d=unit(dancing);
        bob=(float)(Math.sin(t*2.2)*.035+Math.sin(t*5)*.012*m)*(1-s*.7f);
        tilt=-m*12;headYaw=(float)Math.sin(t*.7)*7*(1-s);headTilt=s*18+(float)Math.sin(t*1.5)*3;
        rightArm=-m*24*(1-d)-d*(75+(float)Math.sin(t*9)*24);leftArm=m*15*(1-d)-d*16;
        feet=(float)Math.sin(t*5)*12*m;antenna=(float)Math.sin(t*3.1)*8+m*12;
        double blink=t%4.8;double blinkPhase=(blink-4.5)/.12;float closing=Math.abs(blinkPhase)<1?(float)(.5+.5*Math.cos(blinkPhase*Math.PI)):0;
        eyes=Math.max(.055f,(1-closing*.94f)*(1-s*.94f));
    }
    private static float unit(float v){return Float.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
