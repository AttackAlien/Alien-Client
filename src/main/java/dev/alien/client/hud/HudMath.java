package dev.alien.client.hud;
/** Pure bounded math shared by the HUD and its tests. */
public final class HudMath {
    private HudMath() { }
    public static float clamp(float v,float low,float high,float fallback) {
        return Float.isFinite(v)?Math.max(low,Math.min(high,v)):fallback;
    }
    public static float ratio(float value,float maximum) {
        return !Float.isFinite(value)||!Float.isFinite(maximum)||maximum<=0?0:Math.max(0,Math.min(1,value/maximum));
    }
    public static float smooth(float current,float target,float seconds,float speed) {
        target=clamp(target,0,1,0);current=clamp(current,0,1,target);
        return current+(target-current)*(1-(float)Math.exp(-clamp(seconds,0,.25f,0)*clamp(speed,.1f,30,10)));
    }
    public static float speed(double dx,double dz) { return (float)Math.sqrt(dx*dx+dz*dz)*20; }
}
