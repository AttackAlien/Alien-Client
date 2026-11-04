package dev.alien.client.emote;

import org.lwjgl.opengl.GL11;

/** Small original pose silhouettes, sampled from the same procedural motion library. */
final class EmoteIcons {
    private static final EmotePose pose=new EmotePose();
    static void draw(String id,float x,float y,float size,int color){
        EmoteLibrary.sample(id,.8,pose);float s=size/26;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_LINE_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        try{
            GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glLineWidth(Math.max(1,size/12));
            GL11.glColor4f((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,(color>>>24)/255f);
            GL11.glBegin(GL11.GL_LINES);
            line(x,y-6*s,x,y+3*s);line(x-3*s,y-8*s,x+3*s,y-8*s);line(x-3*s,y-8*s,x-3*s,y-12*s);line(x+3*s,y-8*s,x+3*s,y-12*s);line(x-3*s,y-12*s,x+3*s,y-12*s);
            bone(x-3*s,y-5*s,8*s,pose.angles[EmotePose.RIGHT_ARM*3+2],pose.angles[EmotePose.RIGHT_ARM*3]);
            bone(x+3*s,y-5*s,8*s,pose.angles[EmotePose.LEFT_ARM*3+2],pose.angles[EmotePose.LEFT_ARM*3]);
            bone(x-2*s,y+3*s,9*s,pose.angles[EmotePose.RIGHT_LEG*3+2]-.15f,pose.angles[EmotePose.RIGHT_LEG*3]);
            bone(x+2*s,y+3*s,9*s,pose.angles[EmotePose.LEFT_LEG*3+2]+.15f,pose.angles[EmotePose.LEFT_LEG*3]);
            GL11.glEnd();
        }finally{GL11.glPopAttrib();}
    }
    private static void bone(float x,float y,float length,float roll,float pitch){line(x,y,x-(float)Math.sin(roll)*length,y+(float)(Math.cos(roll)*(.5+.5*Math.cos(pitch)))*length);}
    private static void line(float x,float y,float x2,float y2){GL11.glVertex2f(x,y);GL11.glVertex2f(x2,y2);}
}
