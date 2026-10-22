package dev.alien.client.gui.ui;

import dev.alien.client.gui.Ui;
import java.util.ArrayDeque;
import java.util.Deque;

/** A bounded local toast queue. No threads, network or persistent history. */
public final class Notifications {
    private static final Deque<Toast> ACTIVE=new ArrayDeque<Toast>();
    private Notifications(){}
    public static void push(String message){if(message==null||message.isEmpty())return;while(ACTIVE.size()>=3)ACTIVE.removeFirst();ACTIVE.addLast(new Toast(message,System.nanoTime()));}
    public static void render(int width,int height){long now=System.nanoTime();while(!ACTIVE.isEmpty()&&(now-ACTIVE.peekFirst().since)/1_000_000>3500)ACTIVE.removeFirst();int row=0;for(Toast toast:ACTIVE){float age=(now-toast.since)/1_000_000f;float fade=Math.max(0,Math.min(1,Math.min(age/150,(3500-age)/250)));int w=Math.min(width-24,Ui.font().getStringWidth(toast.text)+34);float x=width-w-12+(1-fade)*18,y=height-42-row++*34;Ui.shadow(x,y,w,28,6,.6f*fade);Ui.rounded(x,y,w,28,6,Ui.alpha(Ui.theme().surfaceRaised,.97f*fade));Ui.rounded(x+6,y+7,2,14,1,Ui.alpha(Ui.accent(),fade));Ui.text(Ui.font().trimStringToWidth(toast.text,w-25),x+15,y+9,Ui.alpha(Ui.theme().text,fade));}}
    private static final class Toast{final String text;final long since;Toast(String text,long since){this.text=text;this.since=since;}}
}
