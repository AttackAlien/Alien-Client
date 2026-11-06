package dev.alien.client.gui.ui;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import dev.alien.client.animation.Motion;
import dev.alien.client.animation.Spring;
import java.util.ArrayDeque;
import java.util.Deque;

/** Bounded toasts with gentle entrances and inertial reflow when a preceding toast expires. */
public final class Notifications {
    private static final Deque<Toast> ACTIVE=new ArrayDeque<Toast>();
    private Notifications(){}
    public static void push(String message){if(message==null||message.isEmpty())return;while(ACTIVE.size()>=3)ACTIVE.removeFirst();ACTIVE.addLast(new Toast(message,System.nanoTime()));}
    public static void render(int width,int height){
        long now=System.nanoTime();while(!ACTIVE.isEmpty()&&(now-ACTIVE.peekFirst().since)/1_000_000>3500)ACTIVE.removeFirst();int row=0;
        boolean animated=AlienClient.get()!=null&&AlienClient.get().config().animations&&!Ui.theme().reducedMotion;
        for(Toast toast:ACTIVE){
            double age=(now-toast.since)/1e9,dt=Math.max(0,Math.min(.2,(now-toast.last)/1e9));toast.last=now;
            float fade=animated?(float)(Motion.window(0,.25,age)*(1-Motion.window(3.13,3.5,age))):1;
            double target=height-42-row++*34;if(!toast.placed||!animated){toast.y.snap(target);toast.placed=true;}else toast.y.advance(target,20,dt);
            int w=Math.max(24,Math.min(width-24,Ui.font().getStringWidth(toast.text)+34));float x=width-w-12+(1-fade)*24,y=(float)toast.y.value();
            Ui.shadow(x,y,w,28,6,.6f*fade);Ui.rounded(x,y,w,28,6,Ui.alpha(Ui.theme().surfaceRaised,.97f*fade));Ui.rounded(x+6,y+7,2,14,1,Ui.alpha(Ui.accent(),fade));Ui.text(Ui.font().trimStringToWidth(toast.text,w-25),x+15,y+9,Ui.alpha(Ui.theme().text,fade));
        }
    }
    private static final class Toast{final String text;final long since;long last;boolean placed;final Spring y=new Spring();Toast(String text,long time){this.text=text;since=last=time;}}
}
