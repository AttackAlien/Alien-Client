package dev.alien.client.world;
import dev.alien.client.animation.Motion;
/** Time-continuous fade, independent of render FPS; no allocation in the render loop. */
public final class ParticleAppearance {
    private ParticleAppearance() { }
    private static float smooth(float t){t=Math.max(0,Math.min(1,t));return (float)Motion.smoother(t);}
    public static float fade(int initial,int remaining,float partial){
        if(initial<=0||remaining<=0||!Float.isFinite(partial))return 0;
        partial=Math.max(0,Math.min(1,partial));
        return smooth((initial-remaining+partial)/6f)*smooth((remaining-partial)/Math.max(8f,initial*.3f));
    }
    public static double curve(double previous,double current,double beforeVelocity,double velocity,float partial){return Motion.hermite(previous,current,beforeVelocity,velocity,partial);}
    public static double size(int initial,int remaining,float partial){double age=initial-remaining+Motion.unit(partial);return (.58+.42*Motion.window(0,7,age))*(.72+.28*Motion.window(0,12,remaining-partial));}
    public static double interpolate(double previous,double current,float partial){
        return previous+(current-previous)*Math.max(0,Math.min(1,partial));
    }
}
