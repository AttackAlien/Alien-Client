package dev.alien.client.gui.ui;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.function.Consumer;

/** Typed numeric entry shared by sliders; errors leave the original value intact. */
public final class ValueInputScreen extends dev.alien.client.gui.PremiumScreen {
    private final GuiScreen parent;private final String title;private final float minimum,maximum,value;private final Consumer<Float> setter;
    private ModernTextField input;private String error="";
    public ValueInputScreen(GuiScreen parent,String title,float minimum,float maximum,float value,Consumer<Float> setter){this.parent=parent;this.title=title;this.minimum=minimum;this.maximum=maximum;this.value=value;this.setter=setter;}
    @Override public void initGui(){fontRendererObj=Ui.font();Keyboard.enableRepeatEvents(true);int x=width/2-120,y=height/2-54;input=new ModernTextField(0,fontRendererObj,x+15,y+37,210,24);input.setMaxStringLength(24);input.setText(Float.toString(value));input.setFocused(true);buttonList.clear();buttonList.add(new Ui.Button(1,x+15,y+72,100,23,"Apply value"));buttonList.add(new Ui.Button(2,x+125,y+72,100,23,"Cancel"));}
    @Override public void drawContents(int mx,int my,float pt){Ui.background(width,height);int x=width/2-120,y=height/2-54;Ui.card(x,y,240,111);Ui.text(title,x+15,y+13,Ui.theme().text);input.drawTextBox();for(GuiButton b:buttonList)b.drawButton(mc,mx,my);if(!error.isEmpty())Ui.centered(error,width/2f,y+100,0xffff8799);}
    @Override protected void actionPerformed(GuiButton button){if(button.id==2){mc.displayGuiScreen(parent);return;}try{float next=Float.parseFloat(input.getText());if(!Float.isFinite(next)||next<minimum||next>maximum)throw new NumberFormatException();setter.accept(next);AlienClient.get().save();mc.displayGuiScreen(parent);}catch(NumberFormatException invalid){error="Use a value from "+minimum+" to "+maximum;}}
    @Override protected void keyTyped(char c,int key)throws IOException{if(key==Keyboard.KEY_ESCAPE){mc.displayGuiScreen(parent);return;}if(key==Keyboard.KEY_RETURN||key==Keyboard.KEY_NUMPADENTER){actionPerformed(buttonList.get(0));return;}input.textboxKeyTyped(c,key);}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{input.mouseClicked(x,y,button);super.mouseClicked(x,y,button);}
    @Override public void updateScreen(){input.updateCursorCounter();}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);}
    @Override public boolean doesGuiPauseGame(){return false;}
}
