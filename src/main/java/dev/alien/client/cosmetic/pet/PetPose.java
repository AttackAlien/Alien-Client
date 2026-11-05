package dev.alien.client.cosmetic.pet;

/** Continuous local joint animation, independent of rendering frame count. Angles are degrees. */
public final class PetPose {
    public float bob,tilt,headYaw,headTilt,headPitch,rightArm,leftArm,rightElbow,leftElbow,wrist,feet,antenna,gazeX,gazeY;
    public float eyes=1,breath=1;
    public void sample(double time,float movement,float sleeping,float dancing,boolean animated){
        bob=tilt=headYaw=headTilt=headPitch=rightArm=leftArm=rightElbow=leftElbow=wrist=feet=antenna=gazeX=gazeY=0;
        eyes=breath=1;if(!animated)return;
        double t=Double.isFinite(time)?time:0;
        float m=unit(movement),s=unit(sleeping),d=unit(dancing),awake=1-s;
        double idle=Math.sin(t*2.1),wave=Math.sin(t*5.2);
        bob=(float)(idle*.026+Math.sin(t*4.6)*.009*m)*(1-s*.7f);
        breath=1+(float)Math.sin(t*2.1-.5)*.018f*(1-m*.5f);
        tilt=-m*9+(float)Math.sin(t*1.2)*.8f*awake;
        headYaw=(float)Math.sin(t*.65)*5*awake;
        headTilt=s*17+(float)Math.sin(t*1.3)*2*awake;
        headPitch=s*13-m*3+(float)Math.sin(t*1.1)*1.5f*awake;
        rightArm=-m*19*(1-d)-d*(64+(float)wave*10)+(float)idle*2*awake*(1-d);
        leftArm=m*12*(1-d)-d*12+(float)Math.sin(t*2.1+.7)*2*awake;
        rightElbow=4+m*5+d*(25+(float)Math.sin(t*5.2+.7)*12);
        leftElbow=5+(float)idle*2*awake;
        wrist=(float)Math.sin(t*6.2)*16*d;
        feet=(float)Math.sin(t*4.6)*8*m+(float)Math.sin(t*2.1+.8)*1.5f*awake;
        antenna=(float)Math.sin(t*2.1-.5)*4*awake+m*9+d*4;
        gazeX=(float)Math.sin(t*.55)*.09f*awake;gazeY=(float)Math.sin(t*.8+.4)*.045f*awake;
        double blink=t%4.8,blinkPhase=(blink-4.5)/.12;
        float closing=Math.abs(blinkPhase)<1?(float)(.5+.5*Math.cos(blinkPhase*Math.PI)):0;
        eyes=Math.max(.055f,(1-closing*.94f)*(1-s*.94f));
    }
    private static float unit(float v){return Float.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
