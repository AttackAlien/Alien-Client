package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import dev.alien.client.combat.CombatConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/**
 * Both combat styles coexist inside the same Unified JAR.
 * Presets are starting points; each switch/slider is independently editable.
 */
public final class CombatStudioScreen extends PremiumScreen {
    private final GuiScreen parent;
    private int left,panelWidth;
    private boolean tuning;
    public CombatStudioScreen(GuiScreen parent){this.parent=parent;}
    private CombatConfig config(){return AlienClient.get().config().combat;}

    @Override public void initGui() {
        fontRendererObj=Ui.font();
        Keyboard.enableRepeatEvents(false);
        panelWidth=Math.min(450,width-30);
        left=(width-panelWidth)/2;
        buttonList.clear();
        int third=(panelWidth-12)/3;
        String[] presets={"OFF","LEGIT","FULL"};
        for(int i=0;i<3;i++)buttonList.add(new Ui.Button(10+i,left+i*(third+6),45,third,23,
                presets[i]+(presets[i].equals(config().preset)?" *":"")));
        buttonList.add(new Ui.Button(20,left,77,(panelWidth-8)/2,22,"Presets / switches"));
        buttonList.add(new Ui.Button(21,left+(panelWidth-8)/2+8,77,(panelWidth-8)/2,22,"Tune aim and CPS"));
        if(!tuning) {
            CombatConfig c=config();
            toggle(30,left,110,panelWidth,"Combat master",c.enabled);
            int half=(panelWidth-8)/2;
            toggle(31,left,140,half,"Aim assist",c.aimAssist);
            toggle(32,left+half+8,140,half,"Click assist",c.clickAssist);
            toggle(33,left,170,half,"Hold LMB to aim",c.requireAttackHeld);
            toggle(34,left+half+8,170,half,"Visible targets",c.visibleTargetsOnly);
        } else {
            CombatConfig c=config();
            int stride=Math.max(24,Math.min(37,(height-148)/3));
            slider(40,109,"Aim strength",.01F,1F,.01F,c.aimStrength,value->c.aimStrength=value);
            slider(41,109+stride,"Aim field of view",5F,180F,1F,c.aimFov,value->c.aimFov=value);
            slider(42,109+stride*2,"Target select range",2F,6F,.1F,c.targetRange,value->c.targetRange=value);
            slider(43,109+stride*3,"Click assist CPS",1F,12F,1F,c.clicksPerSecond,value->c.clicksPerSecond=value);
        }
        boolean multiplayer = mc.theWorld != null && !mc.isSingleplayer();
        if(multiplayer) buttonList.add(new Ui.Button(50,left,height-28,panelWidth/2-5,21,
                AlienClient.get().combatSession().isApproved(mc.theWorld,config())?"Revoke access":"Allow on server"));
        buttonList.add(new Ui.Button(0,multiplayer?left+panelWidth/2+5:left+panelWidth/2-62,
                height-28,multiplayer?panelWidth/2-5:124,21,"Done"));
    }

    private void toggle(int id,int x,int y,int w,String name,boolean enabled) {
        buttonList.add(new Ui.Button(id,x,y,w,22,name+": "+(enabled?"ON":"OFF")));
    }

    private void slider(int id,int y,String name,float min,float max,float step,float value,
                        java.util.function.Consumer<Float> setter) {
        buttonList.add(new ValueSlider(id,left,y,panelWidth,name,min,max,step,value,v->{
            setter.accept(v);
            config().preset="CUSTOM";
        },()->{config().normalize();AlienClient.get().save();}));
    }

    @Override protected void actionPerformed(GuiButton button)throws IOException {
        int id=button.id;
        if(id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(id==50) {
            if(mc.theWorld!=null && !mc.isSingleplayer()) {
                if(AlienClient.get().combatSession().isApproved(mc.theWorld,config()))
                    AlienClient.get().combatSession().revoke();
                else
                    AlienClient.get().combatSession().approve(mc.theWorld,config(),false);
            }
            initGui();return;
        }
        CombatConfig c=config();
        if(id>=10&&id<=12)c.applyPreset(new String[]{"OFF","LEGIT","FULL"}[id-10]);
        else if(id==20||id==21)tuning=id==21;
        else if(id>=30&&id<=34) {
            c.preset="CUSTOM";
            if(id==30)c.enabled=!c.enabled;
            else if(id==31)c.aimAssist=!c.aimAssist;
            else if(id==32)c.clickAssist=!c.clickAssist;
            else if(id==33)c.requireAttackHeld=!c.requireAttackHeld;
            else c.visibleTargetsOnly=!c.visibleTargetsOnly;
        } else return;
        c.normalize();AlienClient.get().save();initGui();
    }

    @Override public void drawContents(int mouseX,int mouseY,float partialTicks) {
        Ui.background(width,height);
        Ui.card(left-10,7,panelWidth+20,height-14);
        Ui.icon("combat",left+1,15,13,Ui.accent());
        Ui.text("COMBAT STUDIO",left+22,16,Ui.theme().text);
        String status="Profile: "+config().preset+" / "+(config().enabled?"MASTER ON":"MASTER OFF");
        Ui.text(status,left+panelWidth-fontRendererObj.getStringWidth(status),17,Ui.theme().muted);
        Ui.text("Presets and controls save with your active profile",left,32,Ui.theme().muted);
        if(mc.theWorld!=null&&!mc.isSingleplayer()) Ui.text(
                AlienClient.get().combatSession().isApproved(mc.theWorld,config())
                        ?"This server session: ALLOWED":"This server session: BLOCKED",
                left,62,Ui.theme().muted);
        super.drawContents(mouseX,mouseY,partialTicks);
        if(height>330) {
            Ui.centered("Aim tracks players; click assist requires holding left mouse.",width/2f,height-58,Ui.theme().muted);
            Ui.centered("Combat changes gameplay. Check the rules of your server.",width/2f,height-45,Ui.theme().muted);
        }
    }
    @Override protected void keyTyped(char character,int key)throws IOException {
        if(key==Keyboard.KEY_ESCAPE){AlienClient.get().save();mc.displayGuiScreen(parent);}
        else super.keyTyped(character,key);
    }
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
