package dev.alien.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** One uniform coordinate system for rendering, hit testing and persistent menu scale. */
public class PremiumScreen extends GuiScreen {
    private int nativeWidth,nativeHeight;
    private float appliedScale=1;
    @Override public void setWorldAndResolution(Minecraft mc,int width,int height) {
        nativeWidth=width;nativeHeight=height;
        appliedScale=effectiveScale(width,height);
        super.setWorldAndResolution(mc,Math.max(256,Math.round(width/appliedScale)),Math.max(192,Math.round(height/appliedScale)));
    }
    private float effectiveScale(int w,int h) {
        return Math.min(Math.max(.75f,Math.min(1.25f,Ui.theme().menuScale)),Math.min(w/320f,h/240f));
    }
    @Override public final void drawScreen(int mouseX,int mouseY,float partialTicks) {
        float desired=effectiveScale(nativeWidth,nativeHeight);
        if(nativeWidth>0 && desired!=appliedScale && !Mouse.isButtonDown(0))setWorldAndResolution(mc,nativeWidth,nativeHeight);
        GL11.glPushMatrix();
        try {
            GL11.glScalef(appliedScale,appliedScale,1);
            drawContents(Math.round(mouseX/appliedScale),Math.round(mouseY/appliedScale),partialTicks);
        } finally {GL11.glPopMatrix();}
    }
    public void drawContents(int x,int y,float partialTicks) {super.drawScreen(x,y,partialTicks);}
    /** Includes both vanilla GUI scale and this screen's custom scale. */
    protected float framebufferScale() { return mc.displayWidth/(float)width; }
}
