package dev.alien.client.gui;
import dev.alien.client.AlienClient;
import dev.alien.client.combat.*;
import net.minecraft.client.gui.*;
import java.io.IOException;
public final class FullToolsScreen extends PremiumScreen {
    private final GuiScreen parent;
    public FullToolsScreen(GuiScreen parent){this.parent=parent;}
    private CombatConfig c(){return AlienClient.get().config().combat;}
    private static final String[] IDS={"aimbot","auto_food","auto_potion","anti_aim","bridge","auto_sprint"};
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(470,width-28),x=(width-w)/2,half=(w-8)/2;
        for(int i=0;i<6;i++){
            dev.alien.client.module.ModuleDescriptor m=AlienClient.get().modules().find(IDS[i]);
            if(m!=null)buttonList.add(new Ui.Button(100+i,x+(i%2)*(half+8),54+(i/2)*28,half,22,m.name()+": "+(m.enabled()?"ON":"OFF")));
        }
        FullToolsConfig f=c().tools;
        buttonList.add(new Ui.Slider(150,x,143,half,22,"Food threshold",1,19,1,f.foodThreshold,v->f.foodThreshold=Math.round(v)));
        buttonList.add(new Ui.Slider(151,x,173,half,22,"Potion HP",1,20,.5f,f.potionHealth,v->f.potionHealth=v));
        buttonList.add(new Ui.Slider(152,x+half+8,143,half,22,"Spin deg/sec",15,720,15,f.spinSpeed,v->f.spinSpeed=v));
        buttonList.add(new Ui.Button(106,x+half+8,173,half,22,"Splash potions: "+(f.splashPotions?"ON":"OFF")));
        buttonList.add(new Ui.Button(7,x,height-27,half,21,"Master: "+(c().enabled?"ON":"OFF")));
        buttonList.add(new Ui.Button(0,x+half+8,height-27,half,21,"Done"));
        buttonList.add(new Ui.Button(60,width-118,12,104,20,"Session / aim"));
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b.id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(b.id==60){mc.displayGuiScreen(new CombatStudioScreen(this));return;}
        if(b.id>=100&&b.id<=105)AlienClient.get().modules().toggle(IDS[b.id-100]);
        if(b.id==106)c().tools.splashPotions=!c().tools.splashPotions;
        if(b.id==7)c().enabled=!c().enabled;
        c().preset="CUSTOM";AlienClient.get().save();initGui();
    }
    @Override public void drawContents(int x,int y,float pt){Ui.background(width,height);Ui.text("UNIFIED / FULL MODULES",15,17,Ui.theme().text);
        Ui.centered("Master "+(c().enabled?"ON":"OFF")+" / "+(mc.theWorld==null?"No world":mc.isSingleplayer()?"Local world":AlienClient.get().combatSession().isApproved(mc.theWorld,c())?"Session allowed":"Session blocked"),width/2,36,Ui.theme().muted);
        super.drawContents(x,y,pt);
    }
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
