package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.theme.ColorValue;
import dev.alien.client.gui.theme.ThemeConfig;
import dev.alien.client.gui.ui.BackdropBlur;
import dev.alien.client.gui.ui.ModernTextField;
import java.awt.Color;
import java.io.IOException;
import java.util.Locale;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** A live design-token studio. Colors are edited as RGBA, HSV or hexadecimal. */
public final class ThemeEditorScreen extends dev.alien.client.gui.PremiumScreen {
    private final GuiScreen parent;
    private int left, top, panelWidth, navWidth, contentX, contentWidth;
    private int tab, selectedColor, drag, pickerX, pickerY, pickerSize;
    private boolean hsv;
    private ModernTextField hex;
    private final ModernTextField[] channels = new ModernTextField[4];
    private ColorValue color;
    private String status = "Changes are shared by every Alien screen";
    private static final String[] COLOR_NAMES = {"Main", "Glow", "Base", "Card", "Text", "Hint"};

    public ThemeEditorScreen(GuiScreen parent) { this.parent = parent; }
    private ThemeConfig theme() { return AlienClient.get().config().ui; }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        fontRendererObj = Ui.font(); buttonList.clear();
        panelWidth = Math.min(width - 20, 820); left = (width - panelWidth) / 2; top = 10;
        navWidth = Math.max(90, Math.min(135, panelWidth / 4));
        contentX = left + navWidth + 20; contentWidth = panelWidth - navWidth - 32;
        buttonList.add(new Ui.Button(0, left + panelWidth - 65, top + 12, 53, 20, "Done"));
        int y = top + 50;
        for (int i = 0; i < ThemeConfig.PRESETS.length; i++) {
            buttonList.add(new Ui.Button(100 + i, left + 10, y, navWidth - 8, 20, ThemeConfig.PRESETS[i])
                    .selected(ThemeConfig.PRESETS[i].equals(theme().preset)));
            y += 23;
        }
        int half = (contentWidth - 5) / 2;
        buttonList.add(new Ui.Button(20, contentX, top + 50, half, 20, "Colors").selected(tab == 0));
        buttonList.add(new Ui.Button(21, contentX + half + 5, top + 50, contentWidth - half - 5, 20, "Style & motion").selected(tab == 1));
        if (tab == 0) colorControls(); else styleControls();
    }

    private void colorControls() {
        color = new ColorValue(selectedArgb());
        int perRow = contentWidth < 220 ? 3 : 6;
        int cell = (contentWidth - (perRow - 1) * 3) / perRow;
        for (int i = 0; i < COLOR_NAMES.length; i++) buttonList.add(new Ui.Button(10 + i,
                contentX + i % perRow * (cell + 3), top + 77 + i / perRow * 19, cell, 16, COLOR_NAMES[i]).selected(selectedColor == i));
        pickerX = contentX; pickerY = top + 101 + (perRow == 3 ? 19 : 0);
        pickerSize = Math.min(78, Math.min(Math.round(contentWidth * .42f), height - pickerY - 40));
        pickerSize = Math.max(40, pickerSize);
        int labelX = pickerX + pickerSize + 28;
        int inputWidth = Math.max(32, contentX + contentWidth - labelX - 15);
        for (int i = 0; i < 4; i++) {
            channels[i] = new ModernTextField(200 + i, fontRendererObj, labelX + 13, pickerY + i * 18, inputWidth, 16);
            channels[i].setMaxStringLength(3);
        }
        int hexY = pickerY + pickerSize + 8;
        buttonList.add(new Ui.Button(22, contentX, hexY, 43, 17, hsv ? "HSV" : "RGBA"));
        hex = new ModernTextField(210, fontRendererObj, contentX + 49, hexY, contentWidth - 49, 17).placeholder("RRGGBBAA");
        hex.setMaxStringLength(9); fillFields();
    }

    private void styleControls() {
        int half = (contentWidth - 8) / 2, y = top + 77;
        slider(40, contentX, y, half, "Opacity", .3f, 1, .01f, theme().opacity, v -> theme().opacity = v);
        slider(41, contentX + half + 8, y, half, "Corners", 0, 14, 1, theme().radius, v -> theme().radius = v);
        slider(42, contentX, y + 33, half, "Shadows", 0, 1, .01f, theme().shadow, v -> theme().shadow = v);
        slider(43, contentX + half + 8, y + 33, half, "Motion", .25f, 3, .05f, theme().animationSpeed, v -> theme().animationSpeed = v);
        slider(44, contentX, y + 66, half, "Menu scale", .75f, 1.25f, .05f, theme().menuScale, v -> theme().menuScale = v);
        Ui.Slider blur = slider(45, contentX + half + 8, y + 66, half, "Blur", 0, 1, .05f, theme().blur, v -> theme().blur = v);
        blur.enabled = BackdropBlur.supported();
        buttonList.add(new Ui.Button(30, contentX, y + 101, half, 18, "Font / " + theme().font));
        buttonList.add(new Ui.Button(31, contentX + half + 8, y + 101, half, 18, "Hover / " + (theme().hoverEffects ? "On" : "Off")));
        buttonList.add(new Ui.Button(32, contentX, y + 123, contentWidth, 17, "Reduced motion / " + (theme().reducedMotion ? "On" : "Off")));
    }

    private Ui.Slider slider(int id,int x,int y,int w,String label,float min,float max,float step,float value,java.util.function.Consumer<Float> change) {
        Ui.Slider result = new Ui.Slider(id,x,y,w,28,label,min,max,step,value,v -> {
            change.accept(v); syncLegacy();
        });
        buttonList.add(result); return result;
    }

    private int selectedArgb() {
        ThemeConfig t = theme();
        switch (selectedColor) {
            case 1:return t.secondary;case 2:return t.surface;case 3:return t.surfaceRaised;
            case 4:return t.text;case 5:return t.muted;default:return t.primary;
        }
    }

    private void setColor(int argb, boolean fields) {
        ThemeConfig t = theme();
        switch (selectedColor) {
            case 1:t.secondary=argb;break;case 2:t.surface=argb;break;case 3:t.surfaceRaised=argb;break;
            case 4:t.text=argb;break;case 5:t.muted=argb;break;default:t.primary=argb;
        }
        t.preset = "Custom RGB"; color.set(argb); syncLegacy();
        if (fields) fillFields();
    }

    private void fillFields() {
        if (hex == null) return;
        int argb = color.argb();
        int[] values = hsv ? new int[]{Math.round(color.hue*360),Math.round(color.saturation*100),Math.round(color.brightness*100),argb>>>24}
                :new int[]{argb>>16&255,argb>>8&255,argb&255,argb>>>24};
        for(int i=0;i<4;i++)channels[i].setText(Integer.toString(values[i]));
        hex.setText(ColorValue.hex(argb));
    }

    private void syncLegacy() {
        theme().normalize();
        AlienClient.get().config().accentColor = theme().primary;
        AlienClient.get().config().opacity = theme().opacity;
        AlienClient.get().config().animationSpeed = theme().animationSpeed;
        AlienClient.get().config().theme = theme().preset;
    }

    private void commitFields() {
        if (tab != 0) return;
        try {
            if (hex.isFocused()) setColor(ColorValue.parseHex(hex.getText()), false);
            else {
                int[] n = new int[4];
                for(int i=0;i<4;i++)n[i]=Integer.parseInt(channels[i].getText());
                if(hsv) {
                    color.hue=Math.max(0,Math.min(360,n[0]))/360f;
                    color.saturation=Math.max(0,Math.min(100,n[1]))/100f;
                    color.brightness=Math.max(0,Math.min(100,n[2]))/100f;
                    color.alpha=Math.max(0,Math.min(255,n[3]))/255f;
                    setColor(color.argb(),false);
                } else setColor(ColorValue.rgba(n[0],n[1],n[2],n[3]),false);
            }
            fillFields(); status = "Color saved / " + ColorValue.hex(color.argb()); AlienClient.get().save();
        } catch (IllegalArgumentException invalid) { status = "Use numeric channels or RRGGBB / RRGGBBAA"; }
    }

    @Override public void drawContents(int mx,int my,float partialTicks) {
        Ui.background(width,height);
        Ui.shadow(left,top,panelWidth,height-20,theme().radius,theme().shadow);
        Ui.rounded(left,top,panelWidth,height-20,theme().radius,Ui.alpha(theme().surface,theme().opacity));
        Ui.rounded(left+10,top+12,5,21,2,theme().primary);
        fontRendererObj.drawString("Theme studio",left+23,top+13,theme().text);
        fontRendererObj.drawString("LIVE PREVIEW / ENTER APPLIES CHANNELS",left+23,top+27,theme().muted);
        fontRendererObj.drawString("PALETTES",left+13,top+40,theme().muted);
        Ui.rounded(left+navWidth+8,top+49,1,height-80,0,Ui.alpha(theme().muted,.15f));
        for(GuiButton button:buttonList)button.drawButton(mc,mx,my);
        if(tab==0)drawColorEditor();
        else if(!BackdropBlur.supported())status="Blur unavailable on this OpenGL device";
        int swatchX=left+13,swatchY=height-29;
        for(int i=0;i<5;i++)Ui.rounded(swatchX+i*15,swatchY,11,7,2,new int[]{theme().primary,theme().secondary,theme().surfaceRaised,theme().text,theme().muted}[i]);
        if(height>280)fontRendererObj.drawString(fontRendererObj.trimStringToWidth(status,contentWidth),contentX,height-24,theme().muted);
        for(GuiButton button:buttonList) if(mx>=button.xPosition&&mx<button.xPosition+button.width
                &&my>=button.yPosition&&my<button.yPosition+button.height) {
            if(button.id==45)Ui.tooltip(BackdropBlur.supported()?"Blur previews over the loaded world":"Blur unavailable on this OpenGL device",mx,my,width,height);
            if(button.id==22)Ui.tooltip("Switch RGBA / HSV. Enter applies typed channels.",mx,my,width,height);
        }
    }

    private void drawColorEditor() {
        drawPicker();
        String[] labels = hsv ? new String[]{"H","S","V","A"}:new String[]{"R","G","B","A"};
        for(int i=0;i<4;i++) {
            fontRendererObj.drawString(labels[i],channels[i].xPosition-12,channels[i].yPosition+4,theme().muted);
            channels[i].drawTextBox();
        }
        hex.drawTextBox();
    }

    private void drawPicker() {
        int hueColor = Color.HSBtoRGB(color.hue,1,1);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_LIGHTING_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDepthMask(false);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glShadeModel(GL11.GL_SMOOTH);GL11.glBegin(GL11.GL_QUADS);
            vertex(pickerX,pickerY,0xffffffff);vertex(pickerX+pickerSize,pickerY,hueColor);
            vertex(pickerX+pickerSize,pickerY+pickerSize,0xff000000);vertex(pickerX,pickerY+pickerSize,0xff000000);
            GL11.glEnd();
            for(int band=0;band<6;band++) {
                float y0=pickerY+pickerSize*band/6f,y1=pickerY+pickerSize*(band+1)/6f;
                int c0=Color.HSBtoRGB(band/6f,1,1),c1=Color.HSBtoRGB((band+1)/6f,1,1);
                GL11.glBegin(GL11.GL_QUADS);vertex(pickerX+pickerSize+4,y0,c0);vertex(pickerX+pickerSize+11,y0,c0);
                vertex(pickerX+pickerSize+11,y1,c1);vertex(pickerX+pickerSize+4,y1,c1);GL11.glEnd();
            }
            int ax=pickerX+pickerSize+16;
            GL11.glBegin(GL11.GL_QUADS);
            for(int y=0;y<pickerSize;y+=5)for(int x=0;x<7;x+=4) {
                int c=((y/5+x/4)&1)==0?0xffe5e7ed:0xff707582;
                vertex(ax+x,pickerY+y,c);vertex(ax+Math.min(7,x+4),pickerY+y,c);
                vertex(ax+Math.min(7,x+4),pickerY+Math.min(pickerSize,y+5),c);vertex(ax+x,pickerY+Math.min(pickerSize,y+5),c);
            }
            int opaque=0xff000000|(color.argb()&0xffffff),transparent=color.argb()&0xffffff;
            vertex(ax,pickerY,opaque);vertex(ax+7,pickerY,opaque);vertex(ax+7,pickerY+pickerSize,transparent);vertex(ax,pickerY+pickerSize,transparent);
            GL11.glEnd();
        } finally { GL11.glPopAttrib(); }
        int sx=Math.round(pickerX+color.saturation*pickerSize),sy=Math.round(pickerY+(1-color.brightness)*pickerSize);
        Ui.rounded(sx-3,sy-3,6,6,3,0xff11131b);Ui.rounded(sx-2,sy-2,4,4,2,0xffffffff);
        int hy=Math.round(pickerY+color.hue*pickerSize),ay=Math.round(pickerY+(1-color.alpha)*pickerSize);
        drawRect(pickerX+pickerSize+3,hy-1,pickerX+pickerSize+12,hy+1,0xffffffff);
        drawRect(pickerX+pickerSize+15,ay-1,pickerX+pickerSize+24,ay+1,0xffffffff);
    }

    private static void vertex(float x,float y,int c) {
        GL11.glColor4f((c>>16&255)/255f,(c>>8&255)/255f,(c&255)/255f,(c>>>24)/255f);GL11.glVertex2f(x,y);
    }

    @Override protected void actionPerformed(GuiButton button) {
        if(button.id==0){syncLegacy();AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(button.id>=100&&button.id<106){theme().applyPreset(ThemeConfig.PRESETS[button.id-100]);syncLegacy();AlienClient.get().save();initGui();return;}
        if(button.id>=10&&button.id<16){selectedColor=button.id-10;initGui();return;}
        if(button.id==20||button.id==21){tab=button.id-20;initGui();return;}
        if(button.id==22){hsv=!hsv;initGui();return;}
        if(button.id==30)theme().font="Modern".equals(theme().font)?"Vanilla":"Modern";
        else if(button.id==31)theme().hoverEffects=!theme().hoverEffects;
        else if(button.id==32)theme().reducedMotion=!theme().reducedMotion;
        else return;
        syncLegacy();AlienClient.get().save();initGui();
    }

    @Override protected void mouseClicked(int x,int y,int button)throws IOException {
        if(tab==0) {
            for(ModernTextField field:channels)field.mouseClicked(x,y,button);
            hex.mouseClicked(x,y,button);
            if(button==0&&y>=pickerY&&y<=pickerY+pickerSize) {
                if(x>=pickerX&&x<=pickerX+pickerSize)drag=1;
                else if(x>=pickerX+pickerSize+3&&x<=pickerX+pickerSize+12)drag=2;
                else if(x>=pickerX+pickerSize+15&&x<=pickerX+pickerSize+24)drag=3;
                if(drag>0){editPicker(x,y);return;}
            }
        }
        super.mouseClicked(x,y,button);
    }
    private void editPicker(int x,int y) {
        if(drag==1){color.saturation=ColorValue.clamp((x-pickerX)/(float)pickerSize);color.brightness=1-ColorValue.clamp((y-pickerY)/(float)pickerSize);}
        else if(drag==2)color.hue=ColorValue.clamp((y-pickerY)/(float)pickerSize);
        else if(drag==3)color.alpha=1-ColorValue.clamp((y-pickerY)/(float)pickerSize);
        // Keep hue even at zero saturation, where an RGB roundtrip otherwise loses it.
        float hue=color.hue;setColor(color.argb(),true);color.hue=hue;
    }
    @Override protected void mouseClickMove(int x,int y,int button,long time){if(drag>0)editPicker(x,y);else super.mouseClickMove(x,y,button,time);}
    @Override protected void mouseReleased(int x,int y,int button){drag=0;super.mouseReleased(x,y,button);syncLegacy();AlienClient.get().save();}
    @Override protected void keyTyped(char c,int code)throws IOException {
        if(code==Keyboard.KEY_ESCAPE){syncLegacy();AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(tab!=0)return;
        if(code==Keyboard.KEY_RETURN||code==Keyboard.KEY_NUMPADENTER){commitFields();return;}
        for(ModernTextField field:channels)if(field.textboxKeyTyped(c,code))return;
        hex.textboxKeyTyped(c,code);
    }
    @Override public void updateScreen(){if(tab==0){for(ModernTextField field:channels)field.updateCursorCounter();hex.updateCursorCounter();}}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);syncLegacy();AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
