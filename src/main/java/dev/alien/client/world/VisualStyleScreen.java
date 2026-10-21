package dev.alien.client.world;

import dev.alien.client.AlienClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** Optional camera and post-HUD tint, with small bounded values for comfort. */
public final class VisualStyleScreen extends GuiScreen {
    private final GuiScreen parent;
    public VisualStyleScreen(GuiScreen parent) { this.parent=parent; }

    @Override public void initGui() {
        buttonList.clear(); final EffectsConfig c=AlienClient.get().config().effects;
        int w=Math.min(width-30,300), x=(width-w)/2, y=45;
        buttonList.add(new GuiButton(1,x,y,w,20,"Camera sway: "+(c.cameraSway?"ON":"OFF")));y+=24;
        buttonList.add(new StudioSlider(10,x,y,w,"Sway degrees",0,1.5F,c.swayStrength,new StudioSlider.Change(){
            @Override public void set(float value){c.swayStrength=value;}
        }));y+=24;
        buttonList.add(new GuiButton(2,x,y,w,20,"Screen color filter: "+(c.colorFilter?"ON":"OFF")));y+=24;
        buttonList.add(new StudioSlider(11,x,y,w,"Tint strength",0,.25F,c.filterStrength,new StudioSlider.Change(){
            @Override public void set(float value){c.filterStrength=value;}
        }));y+=29;
        buttonList.add(new GuiButton(0,x,y,w,20,"Done"));
    }
    @Override protected void actionPerformed(GuiButton button)throws IOException{
        EffectsConfig c=AlienClient.get().config().effects;
        if(button.id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(button.id==1)c.cameraSway=!c.cameraSway;
        if(button.id==2)c.colorFilter=!c.colorFilter;
        if(button.id<10){AlienClient.get().save();initGui();}
    }
    @Override public void drawScreen(int x,int y,float ticks){
        drawDefaultBackground();drawCenteredString(fontRendererObj,"ALIEN / CAMERA & COLOR",width/2,15,AlienClient.get().config().accentColor);
        super.drawScreen(x,y,ticks);
        if(height>250){
            drawCenteredString(fontRendererObj,"Uses the particle RGB color. Master visual-effects switch must be ON.",width/2,height-37,0xB4A9C5);
            drawCenteredString(fontRendererObj,"Roll and tint are purely visual; player aim stays unchanged.",width/2,height-24,0x958CA5);
        }
    }
    @Override protected void keyTyped(char c,int key)throws IOException{
        if(key==Keyboard.KEY_ESCAPE){AlienClient.get().save();mc.displayGuiScreen(parent);}
    }
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
