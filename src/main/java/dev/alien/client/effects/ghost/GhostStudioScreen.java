package dev.alien.client.effects.ghost;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import dev.alien.client.gui.ui.ColorPickerScreen;
import net.minecraft.client.gui.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.function.Consumer;

/** The preview uses exactly the world renderer and choreography, explicitly labeled as a preview. */
public final class GhostStudioScreen extends PremiumScreen {
    private final GuiScreen parent;private int left,right,column,top,bottom,scroll;private long began=System.nanoTime();
    public GhostStudioScreen(GuiScreen p){parent=p;}
    private GhostConfig c(){return AlienClient.get().config().killEffects;}
    private void button(int id,int row,String text){buttonList.add(new Ui.Button(id,left+10,top+27+row*28-scroll,column-20,22,text));}
    private void slider(int id,int row,String label,float min,float max,float step,float value,Consumer<Float> set){buttonList.add(new Ui.Slider(id,left+10,top+27+row*28-scroll,column-20,23,label,min,max,step,value,v->{set.accept(v);AlienClient.get().save();}));}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(860,width-24);left=(width-w)/2;column=(w-12)/2;right=left+column+12;top=46;bottom=height-42;
        buttonList.add(new Ui.Button(0,left+w-54,9,54,22,"Done"));buttonList.add(new Ui.Button(1,right+10,height-30,column-20,22,"Replay preview"));buttonList.add(new Ui.Button(2,left+10,height-30,column-20,22,"Back to world"));
        button(10,0,"Ghost effect: "+on(c().enabled));button(11,1,"Palette / "+c().preset);button(12,2,"Primary color");button(13,3,"Accent color");
        slider(20,4,"Duration",1.5f,4.5f,.1f,c().duration,v->c().duration=v);slider(21,5,"Rise height",.6f,4,.1f,c().height,v->c().height=v);slider(22,6,"Size",.5f,1.5f,.05f,c().size,v->c().size=v);slider(23,7,"Opacity",.15f,1,.05f,c().opacity,v->c().opacity=v);slider(24,8,"Glow",0,1,.05f,c().glow,v->c().glow=v);
        button(14,9,"Particles: "+on(c().particles));button(15,10,"Soft echoes: "+on(c().echoes));button(16,11,"Calm motion: "+on(c().calm));button(17,12,"Players: "+on(c().players));button(18,13,"Mobs / local world: "+on(c().mobs));slider(25,14,"Distance",8,48,1,c().distance,v->c().distance=v);slider(26,15,"Simultaneous",1,6,1,c().maxGhosts,v->c().maxGhosts=Math.round(v));
    }
    private static String on(boolean b){return b?"ON":"OFF";}
    private boolean content(GuiButton b){return b.id>=10;}
    @Override public void drawContents(int mx,int my,float partial){
        Ui.background(width,height);Ui.text("ALIEN / GHOST ELIMINATION",left,17,Ui.theme().text);Ui.card(left,top,column,bottom-top);Ui.card(right,top,column,bottom-top);
        Ui.text("GHOST DESIGN",left+10,top+9,Ui.theme().muted);Ui.centered("LIVE PREVIEW",right+column/2,top+10,Ui.theme().muted);
        float factor=framebufferScale();GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(left*factor),mc.displayHeight-Math.round(bottom*factor),Math.round(column*factor),Math.round((bottom-top-24)*factor));for(GuiButton b:buttonList)if(content(b))b.drawButton(mc,mx,my);}finally{GL11.glPopAttrib();}
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(right*factor),mc.displayHeight-Math.round((bottom-24)*factor),Math.round(column*factor),Math.round((bottom-top-48)*factor));GL11.glDisable(GL11.GL_DEPTH_TEST);
            float size=Math.max(16,Math.min(column/1.5f,(bottom-top-60)/(1.85f*c().size+c().height+.2f)));GL11.glTranslatef(right+column/2,bottom-31,100);GL11.glScalef(size,-size,size);GL11.glRotatef(-15,1,0,0);GL11.glRotatef(-14,0,1,0);
            float age=(float)(((System.nanoTime()-began)/1e9)%(c().duration+.4));GhostRenderer.draw(age,c(),AlienClient.get().config().animations,48);
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();}
        Ui.centered("Preview only / no elimination",right+column/2,bottom-16,Ui.theme().muted);
        for(GuiButton b:buttonList)if(!content(b))b.drawButton(mc,mx,my);
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b.id==0){mc.displayGuiScreen(parent);return;}if(b.id==1){began=System.nanoTime();return;}if(b.id==2){mc.displayGuiScreen(null);return;}
        if(b.id==10)c().enabled=!c().enabled;
        if(b.id==11){c().preset("Neon".equals(c().preset)?"Spectral":"Spectral".equals(c().preset)?"Rose":"Neon");}
        if(b.id==12||b.id==13){boolean first=b.id==12;mc.displayGuiScreen(new ColorPickerScreen(this,first?"Ghost primary":"Ghost accent",0xff000000|(first?c().color:c().secondaryColor),false,v->{if(first)c().color=v&0xffffff;else c().secondaryColor=v&0xffffff;c().preset="Custom";}));return;}
        if(b.id==14)c().particles=!c().particles;if(b.id==15)c().echoes=!c().echoes;if(b.id==16)c().calm=!c().calm;if(b.id==17)c().players=!c().players;if(b.id==18)c().mobs=!c().mobs;
        AlienClient.get().save();initGui();
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button!=0)return;for(GuiButton b:buttonList)if((!content(b)||y>=top+24&&y<bottom)&&b.mousePressed(mc,x,y)){if(!(b instanceof Ui.Slider))actionPerformed(b);return;}}
    @Override protected void mouseReleased(int x,int y,int state){for(GuiButton b:buttonList)b.mouseReleased(x,y);}
    @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int d=Mouse.getEventDWheel();if(d!=0){scroll=Math.max(0,Math.min(Math.max(0,16*28-(bottom-top-27)),scroll+(d<0?28:-28)));initGui();}}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
