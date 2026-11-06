package dev.alien.client.animation;

/** Shared finite, elapsed-time curves. These functions never change game simulation. */
public final class Motion {
    private Motion() {}
    public static double unit(double t) { return Double.isFinite(t) ? Math.max(0, Math.min(1, t)) : 0; }
    public static double smoother(double t) { t=unit(t); return t*t*t*(t*(t*6-15)+10); }
    public static double window(double from,double to,double value) { return smoother((value-from)/(to-from)); }
    public static double lerp(double a,double b,double t) { return a+(b-a)*t; }
    public static double hermite(double a,double b,double va,double vb,double t) {
        t=unit(t);double t2=t*t,t3=t2*t;
        return (2*t3-3*t2+1)*a+(t3-2*t2+t)*va+(-2*t3+3*t2)*b+(t3-t2)*vb;
    }
    public static double bezier(double a,double b,double c,double d,double t) {
        t=unit(t);double u=1-t;return u*u*u*a+3*u*u*t*b+3*u*t*t*c+t*t*t*d;
    }
    public static double shortestAngle(double a,double b,double t) {
        double d=(b-a)%(Math.PI*2);if(d>Math.PI)d-=Math.PI*2;if(d< -Math.PI)d+=Math.PI*2;
        return a+d*t;
    }
    /** Smoothly rounded absolute value with exact zero and unit endpoints. */
    public static double softAbs(double v) { return (Math.sqrt(v*v+.012)-Math.sqrt(.012))/(Math.sqrt(1.012)-Math.sqrt(.012)); }
}
