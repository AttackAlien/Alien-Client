package dev.alien.client.emote;

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
        double a=t*Math.PI*2,s=Math.sin(a),c=Math.cos(a),slow=Math.sin(a*.5),bounce=Math.sin(a*2);
        switch(id){
        case "floss":
            p.set(RIGHT_ARM,.38*slow,.25*c,.95*s-.08);p.set(LEFT_ARM,.38*slow,-.25*c,.95*s+.08);
            p.set(BODY,0,-.12*s,-.08*s);p.set(HEAD,0,.12*s,.05*s);
            p.set(RIGHT_LEG,-.14*s,0,-.05);p.set(LEFT_LEG,.14*s,0,.05);p.sway=(float)(.07*s);p.yaw=(float)(.08*slow);break;
        case "orange_justice":
            p.set(RIGHT_ARM,-1.25+.8*s,.45*c,-.65-.45*c);p.set(LEFT_ARM,-1.25-.8*s,-.45*c,.65-.45*c);
            p.set(BODY,.1+.08*bounce,.12*s,.09*s);p.set(HEAD,.1*bounce,0,-.08*s);
            p.set(RIGHT_LEG,.55*s,0,-.22-.1*c);p.set(LEFT_LEG,-.55*s,0,.22-.1*c);p.lift=(float)(.045*(1-Math.cos(a*2)));break;
        case "take_the_l":
            p.set(RIGHT_ARM,-2.35,-.65,-.15);p.set(LEFT_ARM,-.3+.3*s,0,.45);
            p.set(RIGHT_LEG,Math.max(0,s)*-1.05,0,-.2);p.set(LEFT_LEG,Math.max(0,-s)*-1.05,0,.2);
            p.set(HEAD,.05,0,-.1);p.set(BODY,.07,0,.07*s);p.lift=(float)(.065*Math.abs(s));p.sway=(float)(.035*s);break;
        case "best_mates":
            p.set(RIGHT_ARM,-.8-.6*s,0,-.32);p.set(LEFT_ARM,-.8+.6*s,0,.32);
            p.set(RIGHT_LEG,-.95*s,0,-.1);p.set(LEFT_LEG,.95*s,0,.1);p.set(BODY,.06,0,.09*s);
            p.lift=(float)(.07*Math.abs(s));break;
        case "dab":{
            double envelope=Math.min(1,t/.35)*Math.min(1,Math.max(0,(2.4-t)/.5));
            p.set(HEAD,.48*envelope,-.6*envelope,-.25*envelope);
            p.set(RIGHT_ARM,-1.7*envelope,-.7*envelope,-1.0*envelope);p.set(LEFT_ARM,-.05*envelope,.1*envelope,-2.25*envelope);
            p.set(BODY,.05*envelope,0,-.08*envelope);break;}
        case "jubilation":
            p.set(RIGHT_ARM,-2.8+.22*s,.2*c,-.25-.15*s);p.set(LEFT_ARM,-2.8-.22*s,-.2*c,.25-.15*s);
            p.set(BODY,0,0,.08*s);p.set(HEAD,-.12,0,0);p.set(RIGHT_LEG,.15*s,0,-.08);p.set(LEFT_LEG,-.15*s,0,.08);p.lift=(float)(.045*Math.abs(bounce));break;
        case "robot":{
            double step=Math.floor(t*4)%4,turn=step<2?-.35:.35,arm=step%2==0?-.35:-1.65;
            p.set(HEAD,0,turn,0);p.set(BODY,0,turn*.35,0);p.set(RIGHT_ARM,arm,.1,-.25);p.set(LEFT_ARM,-2-arm,-.1,.25);
            p.set(RIGHT_LEG,step==0?-.25:0,0,0);p.set(LEFT_LEG,step==2?-.25:0,0,0);break;}
        case "wave":{
            double envelope=Math.min(1,t/.4)*Math.min(1,Math.max(0,(3.2-t)/.5));
            p.set(RIGHT_ARM,-.2*envelope,0,(-2.65+.25*Math.sin(a*1.5))*envelope);p.set(HEAD,0,0,-.08*envelope);break;}
        default:break;
        }
    }
}
