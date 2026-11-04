package dev.alien.client.hud;
import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import net.minecraft.client.gui.*;
import java.io.IOException;
/** Bounded HUD presets and palette; no gameplay settings are touched. */
public final class HudPresetsScreen extends PremiumScreen {
    private final GuiScreen parent;
    public HudPresetsScreen(GuiScreen parent){this.parent=parent;}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(380,width-32),x=(width-w)/2;
        for(int i=0;i<4;i++)buttonList.add(new Ui.Button(1+i,x,52+i*28,w,23,new String[]{"PvP / target, keys, CPS, armor","Survival / health, effects, coordinates","Cinematic / minimal overlays","Compact / original Alien layout"}[i]));
        for(int i=0;i<4;i++)buttonList.add(new Ui.Button(10+i,x+i*(w/4),174,w/4-4,20,new String[]{"Inherit","Midnight","Cyan","Light"}[i]));
        buttonList.add(new Ui.Button(0,width/2-50,height-28,100,20,"Done"));
    }
    @Override public void drawContents(int x,int y,float p){Ui.background(width,height);Ui.centered("HUD PRESETS / THEMES",width/2,16,Ui.theme().text);super.drawContents(x,y,p);}
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        HudConfig c=AlienClient.get().config().hud;
        if(b.id==0){mc.displayGuiScreen(parent);return;}
        if(b.id>=1&&b.id<=3)c.applyPreset(new String[]{"PvP","Survival","Cinematic"}[b.id-1]);
        if(b.id==4)c.applyReferencePreset();
        if(b.id>=10&&b.id<=13)c.theme=new String[]{"Inherit","Midnight","Cyan","Light"}[b.id-10];
        AlienClient.get().save();
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
