package dev.alien.client.cosmetic.aura;

/** Tick-driven reaction channels; rendering samples them without advancing simulation. */
public final class AuraResponse {
    private float movement,dance,darkness,oldMovement,oldDance,oldDarkness;
    private double phase,oldPhase;
    public void update(AuraDesign d,double dt,double metersPerSecond,boolean playing,double visualHour){
        if(!Double.isFinite(dt)||dt<=0)return;dt=Math.min(.25,dt);
        oldMovement=movement;oldDance=dance;oldDarkness=darkness;oldPhase=phase;
        float target=d.motion&&Double.isFinite(metersPerSecond)?clamp((float)(metersPerSecond/5.6)):0;
        movement=approach(movement,target,dt,d.response);
        dance=approach(dance,d.emotes&&playing?1:0,dt,d.response);
        darkness=approach(darkness,d.night?nightAt(visualHour):0,dt,d.response);
        phase+=dt*d.speed;if(phase>=120){phase-=120;oldPhase-=120;}
    }
    public static float nightAt(double hour){if(!Double.isFinite(hour))return 0;hour=(hour%24+24)%24;return clamp((float)((-.15-Math.cos((hour-12)*Math.PI/12))*2));}
    private static float approach(float now,float target,double dt,float response){double tau=Math.max(.05,response)*(target>now?1:2);return now+(target-now)*(float)(1-Math.exp(-dt/tau));}
    public float movement(float partial){return mix(oldMovement,movement,partial);}
    public float dance(float partial){return mix(oldDance,dance,partial);}
    public float night(float partial){return mix(oldDarkness,darkness,partial);}
    public double phase(float partial){return oldPhase+(phase-oldPhase)*clamp(partial);}
    public float energy(AuraDesign d,float partial){return Math.min(2.2f,Math.max(0,(d.idle+d.motionGain*movement(partial)+d.emoteGain*dance(partial)+d.nightGain*night(partial))*d.intensity));}
    public void clear(){movement=dance=darkness=oldMovement=oldDance=oldDarkness=0;phase=oldPhase=0;}
    private static float mix(float a,float b,float partial){return a+(b-a)*clamp(partial);}
    private static float clamp(float v){return Float.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
