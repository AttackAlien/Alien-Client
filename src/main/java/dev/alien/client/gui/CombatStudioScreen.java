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
public final class CombatStudioScreen extends GuiScreen {
    private final GuiScreen parent;
    private int left,panelWidth;
    private boolean tuning;
    public CombatStudioScreen(GuiScreen parent){this.parent=parent;}
    private CombatConfig config(){return AlienClient.get().config().combat;}

    @Override public void initGui() {
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
            toggle(30,110,"Combat master",c.enabled);
            toggle(31,140,"Aim assist",c.aimAssist);
            toggle(32,170,"Click assist (LMB only)",c.clickAssist);
            toggle(33,200,"Require LMB for aim",c.requireAttackHeld);
            toggle(34,230,"Visible players only",c.visibleTargetsOnly);
        } else {
            CombatConfig c=config();
            slider(40,112,"Aim strength",.01F,1F,.01F,c.aimStrength,value->c.aimStrength=value);
            slider(41,149,"Aim field of view",5F,180F,1F,c.aimFov,value->c.aimFov=value);
            slider(42,186,"Target select range",2F,6F,.1F,c.targetRange,value->c.targetRange=value);
            slider(43,223,"Click assist CPS",1F,12F,1F,c.clicksPerSecond,value->c.clicksPerSecond=value);
        }
        buttonList.add(new Ui.Button(0,left+panelWidth/2-62,height-28,124,21,"Done"));
    }

    private void toggle(int id,int y,String name,boolean enabled) {
        buttonList.add(new Ui.Button(id,left,y,panelWidth,22,name+": "+(enabled?"ON":"OFF")));
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

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawDefaultBackground();
        drawRect(left-9,7,left+panelWidth+9,height-7,0xea10121f);
        drawGradientRect(left-9,7,left+panelWidth+9,10,Ui.accent(),0xff45b4d2);
        drawCenteredString(fontRendererObj,"ALIEN COMBAT STUDIO / ONE CLIENT",width/2,15,Ui.TEXT);
        drawCenteredString(fontRendererObj,"Profile: "+config().preset+" | Settings saved in current profile",width/2,31,Ui.MUTED);
        super.drawScreen(mouseX,mouseY,partialTicks);
        if(height>330) {
            drawCenteredString(fontRendererObj,"Aim tracks players; click assist requires holding left mouse.",
                    width/2,height-58,Ui.MUTED);
            drawCenteredString(fontRendererObj,"Server reach/anti-cheat are unchanged. Fake anti-aim not implemented.",
                    width/2,height-45,Ui.MUTED);
        }
    }
    @Override protected void keyTyped(char character,int key)throws IOException {
        if(key==Keyboard.KEY_ESCAPE){AlienClient.get().save();mc.displayGuiScreen(parent);}
        else super.keyTyped(character,key);
    }
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
