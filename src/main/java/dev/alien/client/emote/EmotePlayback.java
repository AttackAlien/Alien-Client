package dev.alien.client.emote;

/** Tick-driven, bounded phase and smooth start/switch/stop; independent of rendering FPS. */
public final class EmotePlayback {
    private final EmotePose previous=new EmotePose(),hold=new EmotePose(),raw=new EmotePose();
    private String id="";private double phase,age,fadeAge;private float speed=1;private boolean loop,fading;
    public boolean active(){return !id.isEmpty();}
    public boolean playing(){return active()&&!fading;}
    public String id(){return id;}
    public double phase(){return phase;}
    public boolean play(String selected,float rate,boolean repeat){
        if(!EmoteLibrary.valid(selected))return false;
        sample(0,hold);previous.copy(hold);id=selected;speed=Float.isFinite(rate)?Math.max(.5f,Math.min(1.5f,rate)):1;
        loop=repeat&&EmoteLibrary.loopable(selected);phase=age=fadeAge=0;fading=false;return true;
    }
    public void update(double dt){
        if(!active()||!Double.isFinite(dt)||dt<=0)return;dt=Math.min(.25,dt);
        if(fading){fadeAge+=dt;if(fadeAge>=.2)clear();return;}
        age+=dt;phase+=dt*speed;
        double duration=EmoteLibrary.duration(id);
        if(phase>=duration){if(loop)phase%=duration;else{phase=duration-.000001;stop();}}
    }
    public void stop(){if(!active()||fading)return;sample(0,hold);fading=true;fadeAge=0;}
    public void clear(){id="";phase=age=fadeAge=0;fading=false;previous.clear();hold.clear();}
    public void sample(double partialSeconds,EmotePose out){
        if(!active()){out.clear();return;}double extra=Double.isFinite(partialSeconds)?Math.max(0,Math.min(.05,partialSeconds)):0;
        if(fading){out.copy(hold);out.scale(1-smooth((fadeAge+extra)/.2));return;}
        EmoteLibrary.sample(id,loop?(phase+extra*speed)%EmoteLibrary.duration(id):Math.min(EmoteLibrary.duration(id)-.000001,phase+extra*speed),raw);
        out.mix(previous,raw,smooth((age+extra)/.25));
    }
    private static float smooth(double t){float f=(float)Math.max(0,Math.min(1,t));return f*f*f*(f*(f*6-15)+10);}
}
