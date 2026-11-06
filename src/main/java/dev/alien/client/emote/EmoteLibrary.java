package dev.alien.client.emote;

import dev.alien.client.animation.Motion;

import static dev.alien.client.emote.EmotePose.*;

/** Original procedural adaptations for Minecraft's block rig, with no extracted game assets. */
public final class EmoteLibrary {
    public static final String[] IDS={"floss","orange_justice","take_the_l","best_mates","dab","jubilation","wave","robot"};
    private static final String[] NAMES={"Floss","Orange Justice","Take the L","Best Mates","Dab","Jubilation","Wave","Robot"};
    private EmoteLibrary(){}
    public static boolean valid(String id){return index(id)>=0;}
    private static int index(String id){for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;return -1;}
    public static String name(String id){int i=index(id);return i<0?"None":NAMES[i];}
    public static boolean loopable(String id){return valid(id)&&!"dab".equals(id)&&!"wave".equals(id);}
    public static double duration(String id){return "dab".equals(id)?2.4:"wave".equals(id)?3.2:4;}
    public static void sample(String id,double seconds,EmotePose p){
        p.clear();if(!valid(id))return;
        double t=Double.isFinite(seconds)?Math.max(0,seconds):0;t%=duration(id);
        double a=t*Math.PI*2-.10*Math.sin(t*Math.PI*2),s=Math.sin(a-.08*Math.sin(a*2)),c=Math.cos(a),slow=Math.sin(a*.5),bounce=Math.sin(a*2);
        switch(id){
        case "floss":
            p.set(RIGHT_ARM,.26*slow+.08*Math.sin(a-.2),.20*c,.88*s-.08);p.set(LEFT_ARM,.26*slow+.08*Math.sin(a-.2),-.20*c,.88*s+.08);
            p.bends[0]=(float)(-.12-.22*(.5+.5*Math.sin(a-.25)));p.bends[1]=(float)(-.12-.22*(.5+.5*Math.sin(a+.25)));
            p.bends[2]=(float)(.06+.08*(.5+.5*bounce));p.bends[3]=(float)(.06+.08*(.5-.5*bounce));
            p.set(BODY,.025*Math.sin(a*2-.4),-.16*Math.sin(a-.14),-.10*Math.sin(a-.20));p.set(HEAD,-.025*Math.sin(a*2-.7),.12*Math.sin(a-.28),.06*Math.sin(a-.32));
            p.set(RIGHT_LEG,-.14*s,0,-.05);p.set(LEFT_LEG,.14*s,0,.05);p.sway=(float)(.07*s);p.yaw=(float)(.08*slow);break;
        case "orange_justice":
            p.set(RIGHT_ARM,-.68+.72*s,.38*c,-.58-.38*c);p.set(LEFT_ARM,-.68-.72*s,-.38*c,.58-.38*c);
            p.bends[0]=(float)(-.82-.48*Math.sin(a+.45));p.bends[1]=(float)(-.82+.48*Math.sin(a-.45));
            p.bends[2]=(float)(.24+.24*(.5+.5*Math.sin(a+.3)));p.bends[3]=(float)(.24+.24*(.5-.5*Math.sin(a-.3)));
            p.set(BODY,.10+.065*Math.sin(a*2-.20),.16*Math.sin(a-.12),.11*Math.sin(a-.18));p.set(HEAD,.065*Math.sin(a*2-.42),-.06*Math.sin(a-.31),-.08*Math.sin(a-.37));p.sway=(float)(.035*Math.sin(a-.15));
            p.set(RIGHT_LEG,.55*s,0,-.22-.1*c);p.set(LEFT_LEG,-.55*s,0,.22-.1*c);p.lift=(float)(.045*(1-Math.cos(a*2)));break;
        case "take_the_l":
            p.set(RIGHT_ARM,-1.75,-.55,-.22);p.set(LEFT_ARM,-.22+.24*s,0,.4);
            p.bends[0]=-1.0f;p.bends[1]=(float)(-.28-.15*Math.sin(a-.2));
            p.bends[2]=(float)(.18+.7*positive(-c));p.bends[3]=(float)(.18+.7*positive(c));
            p.set(RIGHT_LEG,positive(s)*-.78,0,-.15);p.set(LEFT_LEG,positive(-s)*-.78,0,.15);
            p.set(HEAD,.05,0,-.1);p.set(BODY,.07,0,.07*s);p.lift=(float)(.04*(1-Math.cos(a*2)));p.sway=(float)(.035*s);break;
        case "best_mates":
            p.set(RIGHT_ARM,-.5-.55*s,0,-.32);p.set(LEFT_ARM,-.5+.55*s,0,.32);
            p.bends[0]=(float)(-.65-.2*s);p.bends[1]=(float)(-.65+.2*s);p.bends[2]=(float)(.25+.45*positive(-s));p.bends[3]=(float)(.25+.45*positive(s));
            p.set(RIGHT_LEG,-.95*s,0,-.1);p.set(LEFT_LEG,.95*s,0,.1);p.set(BODY,.06,0,.09*s);
            p.lift=(float)(.07*Motion.softAbs(s));break;
        case "dab":{
            double envelope=Motion.window(0,.40,t)*(1-Motion.window(1.85,2.4,t));
            p.set(HEAD,.48*envelope,-.6*envelope,-.25*envelope);
            p.bends[0]=(float)(-.7*envelope);p.bends[1]=(float)(-.12*envelope);
            p.set(RIGHT_ARM,-1.1*envelope,-.7*envelope,-1.0*envelope);p.set(LEFT_ARM,-.05*envelope,.1*envelope,-2.25*envelope);
            p.set(BODY,.05*envelope,0,-.08*envelope);break;}
        case "jubilation":
            p.bends[0]=(float)(-.35-.15*s);p.bends[1]=(float)(-.35+.15*s);
            p.set(RIGHT_ARM,-2.45+.22*s,.2*c,-.25-.15*s);p.set(LEFT_ARM,-2.45-.22*s,-.2*c,.25-.15*s);
            p.set(BODY,0,0,.08*s);p.set(HEAD,-.12,0,0);p.set(RIGHT_LEG,.15*s,0,-.08);p.set(LEFT_LEG,-.15*s,0,.08);p.lift=(float)(.045*Motion.softAbs(bounce));break;
        case "robot":{
            int step=(int)Math.floor(t*4)%4,previous=(step+3)%4;
            double weight=Motion.window(0,.14,t% .25),turn=Motion.lerp(previous<2?-.35:.35,step<2?-.35:.35,weight);
            double arm=Motion.lerp(previous%2==0?-.35:-1.65,step%2==0?-.35:-1.65,weight);
            p.set(HEAD,0,turn,0);p.set(BODY,.018*Math.sin(a*2),turn*.35,0);p.set(RIGHT_ARM,arm,.1,-.25);p.set(LEFT_ARM,-2-arm,-.1,.25);
            p.bends[0]=(float)(-.25-.18*(1-Math.cos(a)));p.bends[1]=(float)(-.25-.18*(1+Math.cos(a)));
            p.set(RIGHT_LEG,Motion.lerp(previous==0?-.25:0,step==0?-.25:0,weight),0,0);p.set(LEFT_LEG,Motion.lerp(previous==2?-.25:0,step==2?-.25:0,weight),0,0);break;}
        case "wave":{
            double envelope=Motion.window(0,.45,t)*(1-Motion.window(2.60,3.2,t));
            p.bends[0]=(float)((-.42+.2*Math.sin(a*1.5))*envelope);
            p.set(RIGHT_ARM,-.2*envelope,0,(-2.65+.25*Math.sin(a*1.5))*envelope);p.set(HEAD,0,0,-.08*envelope);break;}
        default:break;
        }
    }
    private static double positive(double v){v=Math.max(0,v);return v*v*(3-2*v);}
}
