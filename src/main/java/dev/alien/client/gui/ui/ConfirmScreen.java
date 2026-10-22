package dev.alien.client.gui.ui;

import dev.alien.client.gui.Ui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** Explicit confirmation before profile deletion or resetting preferences. */
public final class ConfirmScreen extends dev.alien.client.gui.PremiumScreen {
    private final GuiScreen parent;private final String title,message;private final Runnable confirmed;
    public ConfirmScreen(GuiScreen parent,String title,String message,Runnable confirmed){this.parent=parent;this.title=title;this.message=message;this.confirmed=confirmed;}
    @Override public void initGui(){fontRendererObj=Ui.font();int w=Math.min(340,width-32),x=(width-w)/2,y=height/2-60;buttonList.clear();buttonList.add(new Ui.Button(1,x+16,y+83,(w-40)/2,24,"Confirm"));buttonList.add(new Ui.Button(2,x+w/2+4,y+83,(w-40)/2,24,"Cancel"));}
    @Override public void drawContents(int mx,int my,float pt){Ui.background(width,height);int w=Math.min(340,width-32),x=(width-w)/2,y=height/2-60;Ui.card(x,y,w,123);Ui.icon("settings",x+16,y+17,15,Ui.accent());Ui.text(title,x+40,y+18,Ui.theme().text);fontRendererObj.drawSplitString(message,x+16,y+43,w-32,Ui.theme().muted);for(GuiButton b:buttonList)b.drawButton(mc,mx,my);}
    @Override protected void actionPerformed(GuiButton b){if(b.id==1)confirmed.run();mc.displayGuiScreen(parent);}
    @Override protected void keyTyped(char c,int key)throws IOException{if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);}
    @Override public boolean doesGuiPauseGame(){return false;}
}
