package dev.alien.client.hud;
import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import dev.alien.client.gui.ui.ColorPickerScreen;
import net.minecraft.client.gui.*;
import java.io.IOException;
import java.util.function.Consumer;
public final class HudWidgetSettingsScreen extends PremiumScreen {
    private final GuiScreen parent;private final String widget;private boolean style;
    private int x,w,half;
    public HudWidgetSettingsScreen(GuiScreen parent,String widget){this.parent=parent;this.widget=widget;}
    private HudConfig c(){return AlienClient.get().config().hud;}
    private HudConfig.Widget item(){return c().widgets.get(widget);}
    private void button(int id,int col,int row,String text){buttonList.add(new Ui.Button(id,x+col*(half+8),76+row*28,half,21,text));}
    private void slider(int id,int row,String label,float min,float max,float step,float v,Consumer<Float> setter){buttonList.add(new Ui.Slider(id,x,76+row*28,half,23,label,min,max,step,v,setter));}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();w=Math.min(460,width-28);x=(width-w)/2;half=(w-8)/2;
        buttonList.add(new Ui.Button(20,x,42,half,21,"Widget controls").selected(!style));buttonList.add(new Ui.Button(21,x+half+8,42,half,21,"Style / scale").selected(style));
        if(style){slider(1,0,"Opacity",.15f,1,.01f,item().opacity,v->item().opacity=v);slider(2,1,"Scale",.5f,3,.05f,item().scale,v->item().scale=v);
            button(3,1,0,"Choose accent");button(4,1,1,"Custom color: "+on(item().customColor));
        }else if("crosshair".equals(widget)){
            CrosshairConfig h=c().crosshair;
            slider(100,0,"Gap",0,12,.5f,h.gap,v->h.gap=v);slider(101,1,"Length",1,16,.5f,h.length,v->h.length=v);
            slider(102,2,"Thickness",.5f,4,.25f,h.thickness,v->h.thickness=v);slider(103,3,"Dynamic spread",0,12,.5f,h.spread,v->h.spread=v);
            button(110,1,0,"Dot: "+on(h.dot));button(111,1,1,"Outline: "+on(h.outline));button(112,1,2,"Centered: "+on(h.centered));button(113,1,3,"Dynamic: "+on(h.dynamic));
            button(114,0,4,"Shape / "+h.shape);
        }else if("target".equals(widget)){
            slider(120,0,"Hold seconds",.5f,8,.25f,c().targetHold,v->c().targetHold=v);
            slider(121,1,"Health smoothing",.1f,30,.5f,c().targetSmoothing,v->c().targetSmoothing=v);
            slider(122,2,"Display range",3,64,1,c().targetRange,v->c().targetRange=v);
            button(123,1,0,"Avatar: "+on(c().targetAvatar));
        }else if("keys".equals(widget)){button(130,0,0,"Mouse keys: "+on(c().mouseKeys));button(131,1,0,"Sprint state: "+on(c().sprintKey));}
        else if("cps".equals(widget)){button(140,0,0,"Left CPS: "+on(c().cpsLeft));button(141,1,0,"Right CPS: "+on(c().cpsRight));}
        else if("armor".equals(widget)){button(150,0,0,"Percent: "+on(c().armorPercent));}
        else if("indicators".equals(widget)){button(160,0,0,"Health: "+on(c().indicatorHealth));button(161,1,0,"Speed: "+on(c().indicatorSpeed));button(162,0,1,"Effects: "+on(c().indicatorEffects));button(163,1,1,"Facing: "+on(c().indicatorFacing));}
        buttonList.add(new Ui.Button(0,width/2-50,height-26,100,20,"Done"));
    }
    private static String on(boolean b){return b?"ON":"OFF";}
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        int id=b.id;HudConfig c=c();CrosshairConfig h=c.crosshair;
        if(id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(id==20||id==21)style=id==21;
        if(id==3){mc.displayGuiScreen(new ColorPickerScreen(this,"HUD accent",item().color,false,v->{item().color=v;item().customColor=true;}));return;}
        if(id==4)item().customColor=!item().customColor;
        if(id==110)h.dot=!h.dot;if(id==111)h.outline=!h.outline;if(id==112)h.centered=!h.centered;if(id==113)h.dynamic=!h.dynamic;
        if(id==114){String[] modes={"Plus","T","Circle","Dot"};for(int i=0;i<modes.length;i++)if(modes[i].equals(h.shape)){h.shape=modes[(i+1)%4];break;}}
        if(id==123)c.targetAvatar=!c.targetAvatar;if(id==130)c.mouseKeys=!c.mouseKeys;if(id==131)c.sprintKey=!c.sprintKey;
        if(id==140)c.cpsLeft=!c.cpsLeft;if(id==141)c.cpsRight=!c.cpsRight;if(id==150)c.armorPercent=!c.armorPercent;
        if(id==160)c.indicatorHealth=!c.indicatorHealth;if(id==161)c.indicatorSpeed=!c.indicatorSpeed;if(id==162)c.indicatorEffects=!c.indicatorEffects;if(id==163)c.indicatorFacing=!c.indicatorFacing;
        AlienClient.get().save();initGui();
    }
    @Override public void drawContents(int x,int y,float p){Ui.background(width,height);Ui.centered(HudConfig.label(widget)+" / OPTIONS",width/2,17,Ui.theme().text);super.drawContents(x,y,p);}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
