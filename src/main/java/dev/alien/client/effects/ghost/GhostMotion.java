package dev.alien.client.effects.ghost;

/** Continuous elapsed-time choreography; no accumulation tied to rendered frame count. */
public final class GhostMotion {
    private GhostMotion(){}
    public static float smooth(float a,float b,float v){float t=Math.max(0,Math.min(1,(v-a)/(b-a)));return t*t*t*(t*(t*6-15)+10);}
    public static float phase(float seconds,float duration){return Math.max(0,Math.min(1,seconds/duration));}
    public static float lift(float p,float height){return height*smooth(.08f,.97f,p);}
    public static float dissolve(float p){return smooth(.60f,1,p);}
    public static float alpha(float p){return smooth(0,.13f,p)*(1-smooth(.84f,1,p));}
    public static float sway(float age,float p,boolean calm){return (float)(Math.sin(age*2.25)*Math.sin(Math.PI*p)*(calm?.025:.11));}
    public static float roll(float age,float p,boolean calm){return (float)(Math.sin(age*1.9-.2)*Math.sin(Math.PI*p)*(calm?1.1:5));}
    public static float cloth(float angle,float t,float age,boolean calm){return (float)((Math.sin(angle*3-age*2.5)+.35*Math.sin(angle*5+age*1.65))*(1-t)*(1-t)*(calm?.016:.045));}
}
