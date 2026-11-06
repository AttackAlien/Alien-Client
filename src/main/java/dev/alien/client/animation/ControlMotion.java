package dev.alien.client.animation;

/** Reversible response; target changes retain velocity and first paint reflects the saved preference. */
public final class ControlMotion {
    private final Spring hover=new Spring(),press=new Spring(),toggle=new Spring();
    private boolean initialized;
    public void update(boolean hovered,boolean pressed,boolean on,double dt,boolean animated,double speed){
        if(!initialized){toggle.snap(on?1:0);initialized=true;}
        if(!animated){hover.snap(hovered?1:0);press.snap(pressed?1:0);toggle.snap(on?1:0);return;}
        double rate=Math.max(.25,Math.min(3,speed));hover.advance(hovered?1:0,22*rate,dt);press.advance(pressed?1:0,32*rate,dt);toggle.advance(on?1:0,24*rate,dt);
    }
    public float hover(){return (float)hover.value();}public float press(){return (float)press.value();}public float toggle(){return (float)toggle.value();}
}
