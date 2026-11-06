package dev.alien.client.emote;

/** Tick-driven, bounded phase and smooth start/switch/stop; independent of rendering FPS. */
public final class EmotePlayback {
    private final EmotePose previous=new EmotePose(),hold=new EmotePose(),raw=new EmotePose(),velocity=new EmotePose(),ahead=new EmotePose(),captured=new EmotePose();
    private String id="";private double phase,age,fadeAge;private float speed=1;private boolean loop,fading;
    public boolean active(){return !id.isEmpty();}
    public boolean playing(){return active()&&!fading;}
    public String id(){return id;}
    public double phase(){return phase;}
    public boolean play(String selected,float rate,boolean repeat){
        if(!EmoteLibrary.valid(selected))return false;
        capture();previous.copy(hold);id=selected;speed=Float.isFinite(rate)?Math.max(.5f,Math.min(1.5f,rate)):1;
        loop=repeat&&EmoteLibrary.loopable(selected);phase=age=fadeAge=0;fading=false;return true;
    }
    public void update(double dt){
        if(!active()||!Double.isFinite(dt)||dt<=0)return;dt=Math.min(.25,dt);
        if(fading){fadeAge+=dt;if(fadeAge>=.2)clear();return;}
        age+=dt;phase+=dt*speed;
        double duration=EmoteLibrary.duration(id);
        if(phase>=duration){if(loop)phase%=duration;else{phase=duration-.000001;stop();}}
    }
    public void stop(){if(!active()||fading)return;capture();fading=true;fadeAge=0;}
    private void capture(){sample(0,captured);sample(.001,ahead);velocity.velocity(captured,ahead,.001f);hold.copy(captured);}
    public void clear(){id="";phase=age=fadeAge=0;fading=false;previous.clear();hold.clear();velocity.clear();}
    public void sample(double partialSeconds,EmotePose out){
        if(!active()){out.clear();return;}double extra=Double.isFinite(partialSeconds)?Math.max(0,Math.min(.05,partialSeconds)):0;
        if(fading){out.copy(hold);float remain=1-smooth((fadeAge+extra)/.2);out.scale(remain);out.add(velocity,(float)((fadeAge+extra)*remain));return;}
        EmoteLibrary.sample(id,loop?(phase+extra*speed)%EmoteLibrary.duration(id):Math.min(EmoteLibrary.duration(id)-.000001,phase+extra*speed),raw);
        float weight=smooth((age+extra)/.25);out.mix(previous,raw,weight);out.add(velocity,(float)((age+extra)*(1-weight)));
    }
    private static float smooth(double t){float f=(float)Math.max(0,Math.min(1,t));return f*f*f*(f*(f*6-15)+10);}
}
