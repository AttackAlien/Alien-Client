package dev.alien.client.gui.ui;

import dev.alien.client.gui.Ui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

/** Native editing/selection semantics with an entirely custom field surface. */
public final class ModernTextField extends GuiTextField {
    private String placeholder="";
    public ModernTextField(int id,FontRenderer font,int x,int y,int w,int h){super(id,font,x,y,w,h);setEnableBackgroundDrawing(false);}
    public ModernTextField placeholder(String value){placeholder=value==null?"":value;return this;}
    @Override public void drawTextBox(){
        if(!getVisible())return;
        Ui.rounded(xPosition,yPosition,width,height,Math.min(5,Ui.theme().radius),Ui.mix(Ui.theme().surface,Ui.theme().surfaceRaised,.65f));
        if(isFocused())Ui.rounded(xPosition,yPosition+height-1,width,1,0,Ui.alpha(Ui.accent(),.8f));
        int ox=xPosition,oy=yPosition,ow=width;
        xPosition+=7;yPosition+=(height-Ui.font().FONT_HEIGHT)/2;width-=14;
        setTextColor(Ui.theme().text);setDisabledTextColour(Ui.theme().muted);
        try{super.drawTextBox();if(getText().isEmpty()&&!isFocused())Ui.text(Ui.font().trimStringToWidth(placeholder,width),xPosition,yPosition,Ui.theme().muted);}
        finally{xPosition=ox;yPosition=oy;width=ow;}
    }
    @Override public void mouseClicked(int x,int y,int button){
        boolean inside=x>=xPosition&&x<xPosition+width&&y>=yPosition&&y<yPosition+height;
        int ox=xPosition,ow=width;xPosition+=7;width-=14;
        try{super.mouseClicked(inside?Math.max(xPosition,Math.min(xPosition+width-1,x)):x,y,button);}
        finally{xPosition=ox;width=ow;}
    }
}
