package dev.alien.client.gui.ui;

import dev.alien.client.gui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Compact popup list; draw after surrounding controls so its popup stays above them. */
public final class Dropdown extends GuiButton {
    private final List<String> items;private final Consumer<Integer> selected;private int index,offset;private boolean expanded;
    public Dropdown(int id,int x,int y,int w,int h,List<String> items,int index,Consumer<Integer> selected){super(id,x,y,w,h,"");if(items==null||items.isEmpty())throw new IllegalArgumentException("Dropdown requires choices");this.items=new ArrayList<String>(items);this.index=Math.max(0,Math.min(items.size()-1,index));this.selected=selected;}
    public boolean expanded(){return expanded;}
    public void wheel(int delta){if(expanded)offset=Math.max(0,Math.min(Math.max(0,items.size()-6),offset+(delta>0?-1:1)));}
    @Override public void drawButton(Minecraft mc,int mx,int my){if(!visible)return;Ui.rounded(xPosition,yPosition,width,height,5,Ui.theme().surfaceRaised);Ui.text(Ui.font().trimStringToWidth(items.get(index),width-27),xPosition+8,yPosition+(height-10)/2f,Ui.theme().text);Ui.text(expanded?"−":"+",xPosition+width-16,yPosition+(height-10)/2f,Ui.accent());if(expanded){int count=Math.min(6,items.size());Ui.shadow(xPosition,yPosition+height+4,width,count*20+6,5,.7f);Ui.rounded(xPosition,yPosition+height+4,width,count*20+6,5,Ui.theme().surface);for(int i=0;i<count;i++){int yy=yPosition+height+7+i*20;if(mx>=xPosition&&mx<xPosition+width&&my>=yy&&my<yy+20)Ui.rounded(xPosition+3,yy,width-6,19,3,Ui.mix(Ui.theme().surfaceRaised,Ui.accent(),.17f));Ui.text(Ui.font().trimStringToWidth(items.get(offset+i),width-16),xPosition+8,yy+5,offset+i==index?Ui.accent():Ui.theme().text);}}}
    @Override public boolean mousePressed(Minecraft mc,int x,int y){if(!visible||!enabled)return false;if(x>=xPosition&&x<xPosition+width&&y>=yPosition&&y<yPosition+height){expanded=!expanded;return true;}if(expanded&&x>=xPosition&&x<xPosition+width&&y>=yPosition+height+7&&y<yPosition+height+7+Math.min(6,items.size())*20){index=offset+(y-yPosition-height-7)/20;expanded=false;selected.accept(index);return true;}expanded=false;return false;}
}
