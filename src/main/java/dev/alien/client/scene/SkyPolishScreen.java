package dev.alien.client.scene;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import java.io.IOException;
import net.minecraft.client.gui.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.GL11;

/** Live controls for soft procedural clouds, horizon haze and stable antialiased stars. */
public final class SkyPolishScreen extends PremiumScreen {
    private final GuiScreen parent;private int left,right,column,top,bottom,scroll;
    public SkyPolishScreen(GuiScreen parent){this.parent=parent;}
    private SceneConfig c(){return AlienClient.get().config().scenes;}
    private void button(int id,int row,String label){buttonList.add(new Ui.Button(id,left+10,top+26+row*28-scroll,column-20,23,label));}
    private void slider(int id,int row,String label,float min,float max,float value,java.util.function.Consumer<Float> set){buttonList.add(new Ui.Slider(id,left+10,top+26+row*28-scroll,column-20,23,label,min,max,.05f,value,v->{set.accept(v);AlienClient.get().save();}));}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(850,width-24);left=(width-w)/2;column=(w-12)/2;right=left+column+12;top=46;bottom=height-38;
        buttonList.add(new Ui.Button(0,left+w-64,height-28,64,21,"Done"));
        button(1,0,"Earlier skies smooth: "+(c().skyPolish?"ON":"OFF"));buttonList.get(buttonList.size()-1).enabled=!"Realistic".equals(c().sky);button(2,1,"Use Realistic sky");
        slider(3,2,"Cloud coverage",0,1,c().cloudCoverage,v->c().cloudCoverage=v);slider(4,3,"Cloud softness",0,1,c().cloudSoftness,v->c().cloudSoftness=v);
        slider(5,4,"Horizon haze",0,1,c().horizonHaze,v->c().horizonHaze=v);slider(6,5,"Star density",0,1,c().starDensity,v->c().starDensity=v);
        slider(7,6,"Cloud motion",0,3,c().skySpeed,v->c().skySpeed=v);button(8,7,"Reset atmosphere controls");
    }
    @Override public void drawContents(int mx,int my,float partial){Ui.background(width,height);Ui.text("ALIEN / SKY ATMOSPHERE",left,15,Ui.theme().text);Ui.card(left,top,column,bottom-top);Ui.card(right,top,column,bottom-top);Ui.text("APPEARANCE",left+10,top+9,Ui.theme().muted);
        for(GuiButton b:buttonList)if(b.id==0)b.drawButton(mc,mx,my);float scale=framebufferScale();GL11.glPushAttrib(GL11.GL_SCISSOR_BIT|GL11.GL_ENABLE_BIT);
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(left*scale),mc.displayHeight-Math.round(bottom*scale),Math.round(column*scale),Math.round((bottom-top-23)*scale));for(GuiButton b:buttonList)if(b.id!=0)b.drawButton(mc,mx,my);}finally{GL11.glPopAttrib();}
        Ui.text("LIVE SKY / "+c().sky,right+10,top+10,Ui.accent());
        SceneShaders.skyPreview(c(),c().skyIndex(),right+8,top+30,column-16,Math.max(20,bottom-top-61),width,height);
        Ui.centered("World Studio / server time",right+column/2,bottom-19,Ui.theme().muted);
    }
    @Override protected void actionPerformed(GuiButton b){if(b.id==0){mc.displayGuiScreen(parent);return;}if(b.id==1)c().skyPolish=!c().skyPolish;if(b.id==2){c().sky="Realistic";c().enabled=true;}if(b.id==8){c().cloudCoverage=.55f;c().cloudSoftness=.7f;c().horizonHaze=.55f;c().starDensity=.6f;c().skyPolish=true;c().skySpeed=1;}AlienClient.get().save();initGui();}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button!=0)return;for(GuiButton b:buttonList)if((b.id==0||y>=top+23&&y<bottom)&&b.mousePressed(mc,x,y)){if(!(b instanceof Ui.Slider))actionPerformed(b);return;}}
    @Override protected void mouseReleased(int x,int y,int state){for(GuiButton b:buttonList)b.mouseReleased(x,y);}
    @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int delta=Mouse.getEventDWheel();if(delta!=0){scroll=Math.max(0,Math.min(Math.max(0,8*28-(bottom-top-26)),scroll+(delta<0?28:-28)));initGui();}}
    @Override protected void keyTyped(char ch,int key)throws IOException{if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
