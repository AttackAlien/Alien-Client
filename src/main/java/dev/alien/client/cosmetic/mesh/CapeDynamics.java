package dev.alien.client.cosmetic.mesh;

import dev.alien.client.animation.Spring;

/** Per-player critically damped cloth inertia, with an exact rather than Euler spring step. */
public final class CapeDynamics {
    private float age=Float.NaN;private final Spring bend=new Spring(),sway=new Spring();
    public void update(float now,float speed,float lateral,float vertical,float yawDelta){
        if(!Float.isFinite(now)){reset();return;}
        if(!Float.isFinite(age)||now<age||now-age>10){age=now;bend.snap(0);sway.snap(0);return;}
        double dt=(now-age)/20d;age=now;if(dt<=0)return;
        double b=clamp(speed*1.15f+vertical*.32f,0,4.5f),s=clamp(lateral*.4f-yawDelta*.055f,-1.8f,1.8f);
        bend.advance(b,7.4,dt);sway.advance(s,6.6,dt);
    }
    private static float clamp(float n,float min,float max){return Float.isFinite(n)?Math.max(min,Math.min(max,n)):0;}
    public void reset(){age=Float.NaN;bend.snap(0);sway.snap(0);}
    public float bend(){return clamp((float)bend.value(),0,5);}public float sway(){return clamp((float)sway.value(),-2,2);}
}
