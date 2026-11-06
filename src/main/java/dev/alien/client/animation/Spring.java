package dev.alien.client.animation;

/** Exact critically damped spring. Reversing a target preserves velocity, unlike restarting an easing. */
public final class Spring {
    private double value,velocity;
    public Spring() {} public Spring(double initial){snap(initial);}
    public void snap(double v){value=Double.isFinite(v)?v:0;velocity=0;}
    public double value(){return value;}public double velocity(){return velocity;}
    public double advance(double target,double omega,double dt){
        if(!Double.isFinite(target)||!Double.isFinite(omega)||!Double.isFinite(dt)||dt<=0)return value;
        omega=Math.max(.01,Math.min(100,omega));dt=Math.min(2,dt);
        double offset=value-target,c=velocity+omega*offset,e=Math.exp(-omega*dt);
        value=target+(offset+c*dt)*e;velocity=(velocity-omega*c*dt)*e;
        if(Math.abs(value-target)<1e-7&&Math.abs(velocity)<1e-6){value=target;velocity=0;}
        return value;
    }
}
