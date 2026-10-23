package dev.alien.client.world;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** Combined palettes and individual particle controls, all local and reversible. */
public final class AtmospherePresetScreen extends dev.alien.client.gui.PremiumScreen {
    private final GuiScreen parent;
    private int page,left,panelWidth;
    private String message="Choose a complete visual palette or tune your particles";
    public AtmospherePresetScreen(GuiScreen parent){this.parent=parent;}

    @Override public void initGui(){
        fontRendererObj=Ui.font();
        buttonList.clear();panelWidth=Math.min(420,width-28);left=(width-panelWidth)/2;
        buttonList.add(new Ui.Button(1,left,34,panelWidth/2-3,22,"Palettes").selected(page==0));
        buttonList.add(new Ui.Button(2,left+panelWidth/2+3,34,panelWidth/2-3,22,"Particles").selected(page==1));
        if(page==0){
            buttonList.add(new Ui.Button(10,left,69,panelWidth,27,"CRYSTAL  /  cyan sky + mint diamonds"));
            buttonList.add(new Ui.Button(11,left,109,panelWidth,27,"SAKURA  /  rose dusk + drifting petals"));
            buttonList.add(new Ui.Button(12,left,149,panelWidth,27,"NEBULA  /  violet night + star sparks"));
        }else{
            EffectsConfig c=AlienClient.get().config().effects;
            buttonList.add(new Ui.Button(20,left,69,panelWidth,23,"Shape: "+c.shape));
            buttonList.add(new Ui.Button(21,left,98,panelWidth,23,"Ambient particles: "+(c.ambient?"ON":"OFF")));
            buttonList.add(new Ui.Button(22,left,127,panelWidth,23,"Particles: "+(c.enabled?"ON":"OFF")));
            buttonList.add(new Ui.Button(23,left,156,panelWidth,23,"More particle settings"));
        }
        buttonList.add(new Ui.Button(0,width/2-65,height-27,130,21,"Done"));
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b.id==0){mc.displayGuiScreen(parent);return;}
        if(b.id==1||b.id==2){page=b.id-1;initGui();return;}
        EffectsConfig c=AlienClient.get().config().effects;
        if(b.id>=10&&b.id<=12){
            String preset=new String[]{"Crystal","Sakura","Nebula"}[b.id-10];
            AtmospherePresets.apply(AlienClient.get().config().world,c,preset);
            message=preset+" palette applied to your sky and local effects";
        }
        if(b.id==20)c.shape="Diamond".equals(c.shape)?"Petal":"Petal".equals(c.shape)?"Spark":"Diamond";
        if(b.id==21)c.ambient=!c.ambient;
        if(b.id==22)c.enabled=!c.enabled;
        if(b.id==23){mc.displayGuiScreen(new EffectsScreen(this));return;}
        AlienClient.get().save();initGui();
    }
    @Override public void drawContents(int x,int y,float partial){
        Ui.background(width,height);Ui.card(Math.max(8,width/2-174),6,Math.min(348,width-16),height-12);
        Ui.card(left-10,8,panelWidth+20,height-15);
        drawGradientRect(left-10,8,left+panelWidth+10,10,Ui.accent(),0xff61d5dc);
        drawCenteredString(fontRendererObj,"ALIEN / ATMOSPHERE",width/2,18,Ui.theme().text);
        super.drawContents(x,y,partial);
        if(height>=240)drawCenteredString(fontRendererObj,fontRendererObj.trimStringToWidth(message,panelWidth),width/2,height-42,Ui.theme().muted);
    }
    @Override protected void keyTyped(char c,int code)throws IOException{if(code==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
