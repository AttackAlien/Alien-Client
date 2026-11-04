package dev.alien.client.emote;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Hold the configured key, choose a sector and release; click/number selection also works. */
public final class EmoteWheelScreen extends PremiumScreen {
    private final GuiScreen parent;private final boolean hold;
    private int cx,cy,radius,hover=-1;
    public EmoteWheelScreen(GuiScreen parent,boolean hold){this.parent=parent;this.hold=hold;}
    private EmoteConfig c(){return AlienClient.get().config().emotes;}
    @Override public void initGui(){fontRendererObj=Ui.font();cx=width/2;cy=height/2;radius=Math.max(65,Math.min(220,(height-82)/2));}
    public int[] slotCenter(int slot){double a=-Math.PI/2+slot*Math.PI/4;return new int[]{cx+(int)(Math.cos(a)*radius*.72),cy+(int)(Math.sin(a)*radius*.72)};}
    public int hoveredSlot(){return hover;}
    private int at(int x,int y){double dx=x-cx,dy=y-cy,d=Math.hypot(dx,dy);if(d<radius*.3||d>radius+8)return -1;double angle=Math.atan2(dy,dx)+Math.PI/2+Math.PI/8;return (int)Math.floor((angle+Math.PI*2)%(Math.PI*2)/(Math.PI/4));}
    @Override public void drawContents(int mx,int my,float partial){
        Ui.background(width,height);Ui.centered("ALIEN / EMOTES",cx,14,Ui.theme().text);hover=at(mx,my);
        for(int i=0;i<8;i++){
            sector(i,i==hover?Ui.alpha(Ui.accent(),.76f):Ui.alpha(Ui.theme().surfaceRaised,.94f));
            int[] center=slotCenter(i);EmoteIcons.draw(c().slots[i],center[0],center[1]-4,Math.max(20,radius*.23f),i==hover?0xffffffff:Ui.theme().text);
            String name=fontRendererObj.trimStringToWidth(EmoteLibrary.name(c().slots[i]),Math.max(36,radius/2));Ui.centered(name,center[0],center[1]+radius*.13f,Ui.theme().text);
        }
        Ui.rounded(cx-radius*.29f,cy-radius*.29f,radius*.58f,radius*.58f,radius*.29f,Ui.alpha(Ui.theme().surface,.98f));
        Ui.centered(hover<0?"CHOOSE":Integer.toString(hover+1),cx,cy-5,Ui.accent());
        Ui.centered(hover<0?"Move to cancel / local visuals":EmoteLibrary.name(c().slots[hover]),cx,height-48,Ui.theme().text);
        Ui.centered(hold?"Release the wheel key to play / Esc cancels":"Click or press 1-8 / Esc returns",cx,height-22,Ui.theme().muted);
    }
    private void sector(int slot,int color){
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        try{GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glColor4f((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,(color>>>24)/255f);
            GL11.glBegin(GL11.GL_QUAD_STRIP);for(int j=0;j<=18;j++){double a=-Math.PI/2+slot*Math.PI/4-Math.PI/8+.035+j*(Math.PI/4-.07)/18;GL11.glVertex2d(cx+Math.cos(a)*radius*.31,cy+Math.sin(a)*radius*.31);GL11.glVertex2d(cx+Math.cos(a)*radius,cy+Math.sin(a)*radius);}GL11.glEnd();
        }finally{GL11.glPopAttrib();}
    }
    private void choose(int slot){if(slot>=0&&slot<8){String id=c().slots[slot];if(AlienClient.get().emotes().request(id)){c().selected=id;AlienClient.get().save();mc.displayGuiScreen(null);return;}}mc.displayGuiScreen(parent);}
    @Override public void updateScreen(){if(hold&&!AlienClient.get().emotes().keyHeld())choose(hover);}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button==0)choose(at(x,y));else if(button==1)mc.displayGuiScreen(parent);}
    @Override protected void keyTyped(char ch,int key){if(key==Keyboard.KEY_ESCAPE){mc.displayGuiScreen(parent);return;}if(key>=Keyboard.KEY_1&&key<=Keyboard.KEY_8)choose(key-Keyboard.KEY_1);}
    @Override public boolean doesGuiPauseGame(){return false;}
}
