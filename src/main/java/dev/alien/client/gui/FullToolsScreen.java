package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import dev.alien.client.combat.*;
import net.minecraft.client.gui.*;
import java.io.IOException;

/**
 * Categorized Combat Studio for the Unified client.
 * Combat automation remains opt-in and protected by CombatSessionGuard.
 */
public final class FullToolsScreen extends PremiumScreen {
    private final GuiScreen parent;
    private int category;
    private static final String[] IDS={"aimbot","auto_food","auto_potion","anti_aim","bridge","auto_sprint"};
    private static final String[] NAMES={"Aimbot","Auto Food","Auto Potion","Anti Aim / spin","Auto Bridge","Auto Sprint"};
    private static final int[][] GROUPS={{0},{3,4,5},{1,2},{},{}};

    public FullToolsScreen(GuiScreen parent){this.parent=parent;}
    private CombatConfig c(){return AlienClient.get().config().combat;}
    private FullToolsConfig f(){return c().tools;}

    @Override public void initGui(){
        fontRendererObj=Ui.font();
        buttonList.clear();
        int w=Math.min(500,width-28),x=(width-w)/2;
        int gap=5,tabW=(w-gap*4)/5;
        String[] tabs={"Combat","Movement","Utility","Visual","Custom"};
        for(int i=0;i<tabs.length;i++)
            buttonList.add(new Ui.Button(110+i,x+i*(tabW+gap),47,tabW,22,tabs[i]).selected(category==i));

        if(category<=2){
            int[] modules=GROUPS[category];
            int cols=modules.length==1?1:2;
            int cell=(w-(cols-1)*8)/cols;
            for(int i=0;i<modules.length;i++){
                int index=modules[i];
                dev.alien.client.module.ModuleDescriptor m=AlienClient.get().modules().find(IDS[index]);
                String name=m==null?NAMES[index]:m.name();
                boolean enabled=m!=null&&m.enabled();
                buttonList.add(new Ui.Button(100+index,x+(i%cols)*(cell+8),88+(i/cols)*31,cell,24,
                        name+": "+(enabled?"ON":"OFF")));
            }
            if(category==0){
                Ui.text("Aim Assist and Click Assist are in the Combat tab.",x,151,Ui.theme().muted);
                Ui.text("Aimbot uses the normal camera and visible targets.",x,164,Ui.theme().muted);
            }else if(category==1){
                Ui.text("Movement helpers use vanilla movement and block placement.",x,151,Ui.theme().muted);
            }else{
                Ui.text("Utility actions use items available in your hotbar.",x,151,Ui.theme().muted);
            }
        }else if(category==3){
            buttonList.add(new Ui.Button(116,x,91,w,25,"Open HUD / Target HUD editor"));
        }else{
            buttonList.add(new Ui.Slider(150,x,91,w,22,"Food threshold",1,19,1,f().foodThreshold,v->f().foodThreshold=Math.round(v)));
            buttonList.add(new Ui.Slider(151,x,123,w,22,"Potion health threshold",1,20,.5f,f().potionHealth,v->f().potionHealth=v));
            buttonList.add(new Ui.Slider(152,x,155,w,22,"Anti-Aim spin speed",15,720,15,f().spinSpeed,v->f().spinSpeed=v));
            buttonList.add(new Ui.Button(120,x,187,w,22,"Splash potions: "+(f().splashPotions?"ON":"OFF")));
        }

        buttonList.add(new Ui.Button(7,x,height-29,w/2-4,21,"Combat master: "+(c().enabled?"ON":"OFF")));
        buttonList.add(new Ui.Button(0,x+w/2+4,height-29,w/2-4,21,"Done"));
        buttonList.add(new Ui.Button(60,width-118,12,104,20,"Session / aim"));
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b.id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(b.id==60){mc.displayGuiScreen(new CombatStudioScreen(this));return;}
        if(b.id>=110&&b.id<=114){category=b.id-110;initGui();return;}
        if(b.id==116){mc.displayGuiScreen(new dev.alien.client.hud.HudEditorScreen(this));return;}
        if(b.id>=100&&b.id<=105)AlienClient.get().modules().toggle(IDS[b.id-100]);
        else if(b.id==120)f().splashPotions=!f().splashPotions;
        else if(b.id==7)c().enabled=!c().enabled;
        else return;
        c().preset="CUSTOM";
        c().normalize();
        AlienClient.get().save();
        initGui();
    }

    @Override public void drawContents(int x,int y,float pt){
        Ui.background(width,height);
        int w=Math.min(500,width-28),left=(width-w)/2;
        Ui.card(left-10,7,w+20,height-14);
        Ui.text("COMBAT STUDIO / MODULE CATEGORIES",left,16,Ui.theme().text);
        String session=mc.theWorld==null?"No world":mc.isSingleplayer()?"Local world":
                AlienClient.get().combatSession().isApproved(mc.theWorld,c())?"Session allowed":"Session blocked";
        Ui.text("Master "+(c().enabled?"ON":"OFF")+"  /  "+session,left,30,Ui.theme().muted);
        super.drawContents(x,y,pt);
        String note=category==3
                ?"Visual modules stay in HUD Editor; combat does not change visual settings."
                :category==4
                ?"Custom values are saved with the active client profile."
                :"Gameplay assistance is opt-in; follow the rules of the server.";
        Ui.centered(note,width/2f,height-49,Ui.theme().muted);
    }

    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
