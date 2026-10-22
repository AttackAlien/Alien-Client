package dev.alien.client.combat;

/** Pure angle calculations for the real-world aiming controller. */
public final class CombatMath {
    private CombatMath() {}
    public static float wrap(float angle) {
        return (angle % 360F + 540F) % 360F - 180F;
    }
    public static float interpolate(float current,float target,float strength) {
        return current + wrap(target-current)*Math.max(0F,Math.min(1F,strength));
    }
    public static float[] lookAt(double fromX,double fromY,double fromZ,
                                 double toX,double toY,double toZ) {
        double dx=toX-fromX,dy=toY-fromY,dz=toZ-fromZ;
        float yaw=(float)(Math.atan2(dz,dx)*180D/Math.PI-90D);
        float pitch=(float)(-Math.atan2(dy,Math.sqrt(dx*dx+dz*dz))*180D/Math.PI);
        return new float[]{yaw,pitch};
    }
    public static float angularDistance(float yaw,float pitch,float targetYaw,float targetPitch) {
        return (float)Math.sqrt(Math.pow(wrap(yaw-targetYaw),2)+Math.pow(pitch-targetPitch,2));
    }
    public static long intervalNanos(float clicksPerSecond) {
        float bounded=Math.max(1F,Math.min(12F,clicksPerSecond));
        return (long)(1000000000D/bounded);
    }
}
