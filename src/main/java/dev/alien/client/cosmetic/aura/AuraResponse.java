package dev.alien.client.cosmetic.aura;
import dev.alien.client.animation.Motion;
import dev.alien.client.animation.Spring;

/** Tick-driven reaction channels; rendering samples them without advancing simulation. */
public final class AuraResponse {
    private final Spring move=new Spring(),dancing=new Spring(),dark=new Spring();
    private double mv,dv,nv,pmv,pdv,pnv,step=.05;
    private float movement,dance,darkness,oldMovement,oldDance,oldDarkness;
    private double phase,oldPhase;
    public void update(AuraDesign d,double dt,double metersPerSecond,boolean playing,double visualHour){
        if(!Double.isFinite(dt)||dt<=0)return;dt=Math.min(.25,dt);
        step=dt;pmv=mv;pdv=dv;pnv=nv;oldMovement=movement;oldDance=dance;oldDarkness=darkness;oldPhase=phase;
        float target=d.motion&&Double.isFinite(metersPerSecond)?clamp((float)(metersPerSecond/5.6)):0;
        double omega=4/Math.max(.1,d.response);
        movement=(float)move.advance(target,omega,dt);mv=move.velocity();
        dance=(float)dancing.advance(d.emotes&&playing?1:0,omega,dt);dv=dancing.velocity();
        darkness=(float)dark.advance(d.night?nightAt(visualHour):0,omega,dt);nv=dark.velocity();
        phase+=dt*d.speed;if(phase>=120){phase-=120;oldPhase-=120;}
    }
    public static float nightAt(double hour){if(!Double.isFinite(hour))return 0;hour=(hour%24+24)%24;return clamp((float)((-.15-Math.cos((hour-12)*Math.PI/12))*2));}
    public float movement(float partial){return clamp((float)Motion.hermite(oldMovement,movement,pmv*step,mv*step,partial));}
    public float dance(float partial){return clamp((float)Motion.hermite(oldDance,dance,pdv*step,dv*step,partial));}
    public float night(float partial){return clamp((float)Motion.hermite(oldDarkness,darkness,pnv*step,nv*step,partial));}
    public double phase(float partial){return oldPhase+(phase-oldPhase)*clamp(partial);}
    public float energy(AuraDesign d,float partial){return Math.min(2.2f,Math.max(0,(d.idle+d.motionGain*movement(partial)+d.emoteGain*dance(partial)+d.nightGain*night(partial))*d.intensity));}
    public void clear(){move.snap(0);dancing.snap(0);dark.snap(0);mv=dv=nv=pmv=pdv=pnv=0;movement=dance=darkness=oldMovement=oldDance=oldDarkness=0;phase=oldPhase=0;}
    private static float clamp(float v){return Float.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
