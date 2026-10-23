package dev.alien.client.gui;

import dev.alien.client.gui.ui.ValueInputScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;
import java.util.Locale;
import java.util.function.Consumer;

/** Live custom slider with typed entry on its value; commits once on release. */
public final class ValueSlider extends GuiButton {
    private final String label;private final float minimum,maximum,step;private final Consumer<Float> change;private final Runnable commit;
    private float value;private boolean dragging;
    public ValueSlider(int id,int x,int y,int width,String label,float min,float max,float step,float value,Consumer<Float> change,Runnable commit){super(id,x,y,width,25,label);this.label=label;minimum=min;maximum=max;this.step=step;this.value=Math.max(min,Math.min(max,value));this.change=change;this.commit=commit;}
    @Override public void drawButton(Minecraft mc,int x,int y){if(!visible)return;mouseDragged(mc,x,y);String formatted=step>=1?Integer.toString(Math.round(value)):String.format(Locale.ROOT,"%.2f",value);Ui.slider(xPosition,yPosition,width,height,label,formatted,(value-minimum)/(maximum-minimum),x>=xPosition&&x<xPosition+width&&y>=yPosition&&y<yPosition+height);}
    @Override public boolean mousePressed(Minecraft mc,int x,int y){if(!super.mousePressed(mc,x,y))return false;if(y<yPosition+12&&x>xPosition+width-46){mc.displayGuiScreen(new ValueInputScreen(mc.currentScreen,label,minimum,maximum,value,next->{value=next;change.accept(next);commit.run();}));return true;}dragging=true;update(x);return true;}
    @Override protected void mouseDragged(Minecraft mc,int x,int y){if(dragging)update(x);}
    @Override public void mouseReleased(int x,int y){if(dragging){dragging=false;commit.run();}}
    private void update(int x){float next=minimum+Math.max(0,Math.min(1,(x-xPosition)/(float)width))*(maximum-minimum);next=Math.max(minimum,Math.min(maximum,Math.round(next/step)*step));if(next!=value){value=next;change.accept(next);}}
}
