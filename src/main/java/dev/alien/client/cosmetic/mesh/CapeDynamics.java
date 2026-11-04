package dev.alien.client.cosmetic.mesh;

/** Original damped spring response to real movement; separate state for each player. */
public final class CapeDynamics {
    private float age=Float.NaN,bend,sway,bendVelocity,swayVelocity;
    public void update(float now,float speed,float lateral,float vertical,float yawDelta){
        if(!Float.isFinite(now)){reset();return;}
        if(!Float.isFinite(age)||now<age||now-age>10){age=now;bend=sway=bendVelocity=swayVelocity=0;return;}
        float dt=Math.min(.1f,(now-age)/20f);age=now;if(dt<=0)return;
        float bendTarget=clamp(speed*1.15f+vertical*.32f,0,4.5f),swayTarget=clamp(lateral*.4f-yawDelta*.055f,-1.8f,1.8f);
        int steps=Math.max(1,(int)Math.ceil(dt/.016f));float step=dt/steps;
        for(int i=0;i<steps;i++){
            bendVelocity+=(55*(bendTarget-bend)-13*bendVelocity)*step;bend=clamp(bend+bendVelocity*step,0,5);
            swayVelocity+=(44*(swayTarget-sway)-12*swayVelocity)*step;sway=clamp(sway+swayVelocity*step,-2,2);
        }
    }
    private static float clamp(float n,float min,float max){return Float.isFinite(n)?Math.max(min,Math.min(max,n)):0;}
    public void reset(){age=Float.NaN;bend=sway=bendVelocity=swayVelocity=0;}
    public float bend(){return bend;}public float sway(){return sway;}
}
