package dev.alien.client.world;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Locale;

public final class EffectsScreen extends dev.alien.client.gui.PremiumScreen {
    private final GuiScreen parent;
    private GuiTextField color;
    private String message="Effects are local, optional and limited by the performance profile";

    public EffectsScreen(GuiScreen parent) { this.parent=parent; }
    private EffectsConfig settings() { return AlienClient.get().config().effects; }

    @Override public void initGui() {
        fontRendererObj=Ui.font();
        Keyboard.enableRepeatEvents(true); buttonList.clear();
        int w=Math.min(300,width-30), x=(width-w)/2, y=34;
        final EffectsConfig c=settings();
        buttonList.add(new Ui.Button(1,x,y,w,20,"Visual effects: "+on(c.enabled))); y+=22;
        buttonList.add(new Ui.Button(2,x,y,w/2-3,20,"Movement: "+on(c.movement)));
        buttonList.add(new Ui.Button(3,x+w/2+3,y,w/2-3,20,"Landing: "+on(c.landing))); y+=22;
        buttonList.add(new Ui.Button(4,x,y,w/2-3,20,"Trails: "+on(c.trails)));
        buttonList.add(new Ui.Button(5,x+w/2+3,y,w/2-3,20,"Quality: "+c.quality)); y+=22;
        buttonList.add(new StudioSlider(20,x,y,w,"Effect distance",4,64,c.distance,new StudioSlider.Change(){
            @Override public void set(float value){c.distance=value;}
        })); y+=22;
        buttonList.add(new StudioSlider(21,x,y,w,"Particle budget",0,256,c.budget,new StudioSlider.Change(){
            @Override public void set(float value){c.budget=Math.round(value);}
        })); y+=22;
        buttonList.add(new StudioSlider(22,x,y,w,"Particle size",1,8,c.size,new StudioSlider.Change(){
            @Override public void set(float value){c.size=value;}
        })); y+=22;
        color=new dev.alien.client.gui.ui.ModernTextField(30,fontRendererObj,x,y,w/2-3,20); color.setMaxStringLength(6);
        color.setText(String.format(Locale.ROOT,"%06X",c.color&0xFFFFFF));
        buttonList.add(new Ui.Button(6,x+w/2+3,y,w/2-3,20,"Apply RGB hex")); y+=22;
        buttonList.add(new Ui.Button(7,x,y,w/2-3,20,"Reset effects"));
        buttonList.add(new Ui.Button(0,x+w/2+3,y,w/2-3,20,"Done"));
        int third=(w-12)/3;
        buttonList.add(new Ui.Button(8,x,y+22,third,20,"Camera"));
        buttonList.add(new Ui.Button(9,x+third+6,y+22,third,20,"Presets"));
        buttonList.add(new Ui.Button(10,x+(third+6)*2,y+22,third,20,"Particles"));
    }

    private static String on(boolean v){return v?"ON":"OFF";}

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        EffectsConfig c=settings();
        switch(button.id){
            case 0: applyColor(); mc.displayGuiScreen(parent);return;
            case 1:c.enabled=!c.enabled;break;
            case 2:c.movement=!c.movement;break;
            case 3:c.landing=!c.landing;break;
            case 4:c.trails=!c.trails;break;
            case 5:c.quality="Low".equals(c.quality)?"Balanced":"Balanced".equals(c.quality)?"High":"Low";break;
            case 6:applyColor();break;
            case 7:AlienClient.get().config().effects=new EffectsConfig();break;
            case 8:applyColor();mc.displayGuiScreen(new VisualStyleScreen(this));return;
            case 10:applyColor();mc.displayGuiScreen(new ParticleStudioScreen(this));return;
            case 9:applyColor();mc.displayGuiScreen(new AtmospherePresetScreen(this));return;
            default:return;
        }
        AlienClient.get().save();initGui();
    }

    private void applyColor(){
        if(color.getText().matches("[0-9a-fA-F]{6}")){
            settings().color=Integer.parseInt(color.getText(),16);AlienClient.get().save();message="Color saved";
        }else message="Use exactly six hexadecimal digits";
    }

    @Override public void drawContents(int mx,int my,float partialTicks){
        Ui.background(width,height);Ui.card(Math.max(8,width/2-174),6,Math.min(348,width-16),height-12);drawCenteredString(fontRendererObj,"ALIEN / VISUAL EFFECTS",width/2,12,Ui.accent());
        color.drawTextBox();super.drawContents(mx,my,partialTicks);
        if(height>270)drawCenteredString(fontRendererObj,message,width/2,height-24,0xB7ADC9);
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{color.mouseClicked(x,y,button);super.mouseClicked(x,y,button);}
    @Override protected void keyTyped(char c,int code)throws IOException{
        if(code==Keyboard.KEY_ESCAPE){applyColor();mc.displayGuiScreen(parent);return;}color.textboxKeyTyped(c,code);
    }
    @Override public void updateScreen(){color.updateCursorCounter();}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
