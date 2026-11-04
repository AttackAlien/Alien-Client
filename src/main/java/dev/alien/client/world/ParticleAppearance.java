package dev.alien.client.world;
/** Time-continuous fade, independent of render FPS; no allocation in the render loop. */
public final class ParticleAppearance {
    private ParticleAppearance() { }
    private static float smooth(float t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    public static float fade(int initial,int remaining,float partial){
        if(initial<=0||remaining<=0||!Float.isFinite(partial))return 0;
        partial=Math.max(0,Math.min(1,partial));
        return smooth((initial-remaining+partial)/6f)*smooth((remaining-partial)/Math.max(8f,initial*.3f));
    }
    public static double interpolate(double previous,double current,float partial){
        return previous+(current-previous)*Math.max(0,Math.min(1,partial));
    }
}
