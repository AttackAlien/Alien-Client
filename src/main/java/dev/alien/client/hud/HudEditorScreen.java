package dev.alien.client.hud;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Live layout editor; Escape and Done both persist the current layout. */
public final class HudEditorScreen extends dev.alien.client.gui.PremiumScreen {
    private final GuiScreen parent;
    private final List<String> ids = new ArrayList<String>();
    private String selected = "fps";
    private String dragging;
    private int dragX, dragY, scroll;
    private int rows;
    private GuiTextField custom;
    private boolean resizing;

    public HudEditorScreen(GuiScreen parent) { this.parent = parent; }

    private HudConfig cfg() { return HudManager.config(); }

    private int tab;
    private String status = "Drag widgets on the canvas / Shift bypasses snap";
    private int rail = 142;
    @Override public void initGui() {
        fontRendererObj=Ui.font(); Keyboard.enableRepeatEvents(true);
        ids.clear(); for(String id:cfg().widgets.keySet()) if(HudManager.known(id))ids.add(id);
        rail=Math.min(142,width/3+4);rows=Math.max(1,(height-164)/22);
        scroll=Math.max(0,Math.min(scroll,Math.max(0,ids.size()-rows)));
        rebuildButtons();
        custom=new dev.alien.client.gui.ui.ModernTextField(50,fontRendererObj,rail+20,height-66,Math.max(70,width-rail-34),20);
        custom.setMaxStringLength(100);custom.setText(cfg().customText);
    }
    private void button(int id,int x,int y,int w,String label){buttonList.add(new Ui.Button(id,x,y,w,20,label));}
    private void rebuildButtons() {
        buttonList.clear();
        button(11,width-64,12,52,"Done");button(14,width-164,12,94,"Compact preset");
        button(40,12,53,(rail-18)/2,"Widgets");button(41,16+(rail-18)/2,53,(rail-18)/2,"Style");
        if(tab==0){
            for(int i=0;i<rows && scroll+i<ids.size();i++){
                String id=ids.get(scroll+i);
                buttonList.add(new Ui.Button(100+scroll+i,12,81+i*22,rail-12,20,HudConfig.label(id)).selected(id.equals(selected)));
            }
            button(20,12,height-78,(rail-18)/2,"Previous");button(21,16+(rail-18)/2,height-78,(rail-18)/2,"Next");
        }else{
            int x=12,y=81,w=rail-12;
            button(13,x,y,w,"Backdrop / "+(cfg().background?"On":"Off"));y+=23;
            button(42,x,y,w,"Font / "+(cfg().modernFont?"Modern":"Minecraft"));y+=23;
            button(43,x,y,w,"Gradient / "+(cfg().gradient?"On":"Off"));y+=23;
            if(height>260){button(44,x,y,w,"Shadow / "+(cfg().shadows?"On":"Off"));y+=23;}
            button(45,x,y,w,"Opacity / "+Math.round(cfg().opacity*100)+"%");
        }
        int x=12,y=height-38;int gap=4;
        button(0,x,y,46,"Toggle");x+=50;button(1,x,y,22,"-");x+=26;button(2,x,y,22,"+");x+=26;
        button(4,x,y,34,"Left");x+=38;button(5,x,y,42,"Center");x+=46;button(6,x,y,36,"Right");x+=40;
        if(width>=390)button(3,x,y,60,"Snap / "+(cfg().snapToGrid?"On":"Off"));
        else button(3,12,height-55,rail-12,"Snap / "+(cfg().snapToGrid?"On":"Off"));
        if(width>=520){button(7,x+64,y,34,"Top");button(9,x+102,y,44,"Bottom");}
        button(51,rail+20,53,54,"Export");button(52,rail+78,53,54,"Import");
        if(width>400)button(46,rail+136,53,64,"Grid / "+(cfg().showGrid?"On":"Off"));
    }

    @Override public void drawContents(int mouseX, int mouseY, float partialTicks) {
        Ui.background(width,height);
        if (cfg().showGrid) {
            for(int x=rail+12;x<width;x+=cfg().gridSize*2)for(int y=82;y<height-78;y+=cfg().gridSize*2)
                drawRect(x,y,x+1,y+1,0x283f4260);
        }
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_SCISSOR_BIT|org.lwjgl.opengl.GL11.GL_ENABLE_BIT);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        float pixelScale=framebufferScale();
        org.lwjgl.opengl.GL11.glScissor(Math.round((rail+14)*pixelScale),Math.round(76*pixelScale),Math.round((width-rail-20)*pixelScale),Math.round((height-158)*pixelScale));
        for (Map.Entry<String, HudConfig.Widget> entry : cfg().widgets.entrySet()) {
            if (!HudManager.known(entry.getKey()) || entry.getValue() == null) continue;
            HudConfig.Widget widget = entry.getValue();
            if (widget.enabled || entry.getKey().equals(selected)) HudManager.render(entry.getKey(), widget, width, height, true);
            if (entry.getKey().equals(selected)) {
                int[] b = HudManager.bounds(entry.getKey(), widget, width, height);
                outline(b[0] - 1, b[1] - 1, b[0] + b[2] + 1, b[1] + b[3] + 1, 0xff000000 | AlienClient.get().config().accentColor);
                drawRect(b[0] + b[2] - 5, b[1] + b[3] - 5, b[0] + b[2] + 2, b[1] + b[3] + 2, 0xffeeeeff);
            }
        }
        org.lwjgl.opengl.GL11.glPopAttrib();
        Ui.card(6,44,rail+6,height-91);
        Ui.rounded(6,6,width-12,34,8,AlienClient.get().config().ui.surface);
        Ui.icon("hud",16,17,13,Ui.accent());
        fontRendererObj.drawString("HUD Studio",35,16,AlienClient.get().config().ui.text);
        fontRendererObj.drawString("LIVE LAYOUT",rail+20,85,AlienClient.get().config().ui.muted);
        String label=HudConfig.label(selected)+" / "+String.format(java.util.Locale.ROOT,"%.1fx",cfg().widgets.get(selected).scale);
        fontRendererObj.drawString(label,rail+20,height-82,AlienClient.get().config().ui.text);
        Ui.rounded(6,height-45,width-12,39,7,AlienClient.get().config().ui.surface);
        super.drawContents(mouseX,mouseY,partialTicks);custom.drawTextBox();
        dev.alien.client.gui.ui.Notifications.render(width,height);
        if(height>280)fontRendererObj.drawString(fontRendererObj.trimStringToWidth(status,width-rail-28),rail+20,100,AlienClient.get().config().ui.muted);
    }

    private static void outline(int x, int y, int right, int bottom, int color) {
        drawRect(x, y, right, y + 1, color); drawRect(x, bottom - 1, right, bottom, color);
        drawRect(x, y, x + 1, bottom, color); drawRect(right - 1, y, right, bottom, color);
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        HudConfig.Widget w = cfg().widgets.get(selected);
        if (button.id >= 100) { selected = ids.get(button.id - 100); return; }
        switch (button.id) {
            case 0: w.enabled = !w.enabled; break;
            case 1: w.scale = Math.max(.5f, w.scale - .1f); break;
            case 2: w.scale = Math.min(3f, w.scale + .1f); break;
            case 3: cfg().snapToGrid = !cfg().snapToGrid; break;
            case 4: w.x = 0f; break;
            case 5: w.x = Math.max(0, (width - HudManager.bounds(selected, w, width, height)[2]) / 2f) / width; break;
            case 6: w.x = Math.max(0, width - HudManager.bounds(selected, w, width, height)[2]) / (float) width; break;
            case 7: w.y = 0f; break;
            case 8: w.y = Math.max(0, (height - HudManager.bounds(selected, w, width, height)[3]) / 2f) / height; break;
            case 9: w.y = Math.max(0, height - HudManager.bounds(selected, w, width, height)[3]) / (float) height; break;
            case 10: mc.displayGuiScreen(new dev.alien.client.gui.ui.ConfirmScreen(this,"Reset HUD","Restore the default widget positions?",()->{cfg().widgets.clear();cfg().ensureDefaults();AlienClient.get().save();}));return;
            case 11: mc.displayGuiScreen(parent); return;
            case 12: cfg().enabled = !cfg().enabled; break;
            case 13: cfg().background = !cfg().background; break;
            case 14: cfg().applyReferencePreset(); selected = "brand"; break;
            case 40: tab=0;break;
            case 41: tab=1;break;
            case 42: cfg().modernFont=!cfg().modernFont;break;
            case 43: cfg().gradient=!cfg().gradient;break;
            case 44: cfg().shadows=!cfg().shadows;break;
            case 45: cfg().opacity=cfg().opacity>.95f?.3f:Math.min(1,cfg().opacity+.1f);break;
            case 46: cfg().showGrid=!cfg().showGrid;break;
            case 51: HudLayouts.exportLayout(AlienClient.get().store().directory(),cfg());status="Exported hud/exports/layout.json";break;
            case 52:
                try{AlienClient.get().config().hud=HudLayouts.importLayout(AlienClient.get().store().directory());status="HUD layout imported";}
                catch(IOException failure){status=failure.getMessage();}break;
            case 20: scroll = Math.max(0, scroll - 1); break;
            case 21: scroll = Math.min(Math.max(0, ids.size() - rows), scroll + 1); break;
            default: break;
        }
        AlienClient.get().save();
        if(button.id==51||button.id==52)dev.alien.client.gui.ui.Notifications.push(status);
        rebuildButtons();
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        custom.mouseClicked(x, y, button);
        if (x <= rail+12 || y < 80 || y>height-78) {
            super.mouseClicked(x, y, button);
            // A sidebar entry doubles as a drag handle, including for widgets under the panel.
            if (button == 0 && tab==0 && x >= 12 && x <= rail && y >= 81 && y < 81 + rows * 22) {
                dragging = selected;
                int[] b = HudManager.bounds(selected, cfg().widgets.get(selected), width, height);
                dragX = b[2] / 2; dragY = b[3] / 2; resizing = false;
            }
            return;
        }
        List<String> reverse = new ArrayList<String>(ids);
        java.util.Collections.reverse(reverse);
        for (String id : reverse) {
            HudConfig.Widget w = cfg().widgets.get(id);
            if (!w.enabled && !id.equals(selected)) continue;
            int[] b = HudManager.bounds(id, w, width, height);
            if (x >= b[0] && y >= b[1] && x <= b[0] + b[2] + 3 && y <= b[1] + b[3] + 3) {
                selected = id;
                if (button == 1) { w.enabled = !w.enabled; AlienClient.get().save(); return; }
                if (button == 0) {
                    dragging = id; resizing = x >= b[0] + b[2] - 6 && y >= b[1] + b[3] - 6;
                    dragX = x - b[0]; dragY = y - b[1];
                }
                return;
            }
        }
    }

    @Override protected void mouseClickMove(int x, int y, int button, long elapsed) {
        if (dragging == null || button != 0) return;
        HudConfig.Widget w = cfg().widgets.get(dragging);
        if (resizing) {
            int[] b = HudManager.bounds(dragging, w, width, height);
            w.scale = Math.max(.5f, Math.min(3f, (x - b[0]) / (float) b[4]));
        } else {
            int nx = x - dragX, ny = y - dragY;
            if (cfg().snapToGrid && !isShiftKeyDown()) {
                nx = Math.round(nx / (float) cfg().gridSize) * cfg().gridSize;
                ny = Math.round(ny / (float) cfg().gridSize) * cfg().gridSize;
            }
            w.x = Math.max(0, Math.min(1, nx / (float) width));
            w.y = Math.max(0, Math.min(1, ny / (float) height));
        }
    }

    @Override protected void mouseReleased(int x, int y, int button) {
        if (dragging != null) { dragging = null; resizing = false; AlienClient.get().save(); }
        super.mouseReleased(x, y, button);
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int delta = Mouse.getEventDWheel();
        if (delta == 0) return;
        int x = Mouse.getEventX() * width / mc.displayWidth;
        if (x <= rail+12) {
            scroll = Math.max(0, Math.min(Math.max(0, ids.size() - rows), scroll + (delta > 0 ? -1 : 1)));
            rebuildButtons();
        } else {
            HudConfig.Widget w = cfg().widgets.get(selected);
            w.scale = Math.max(.5f, Math.min(3f, w.scale + (delta > 0 ? .1f : -.1f)));
            AlienClient.get().save();
        }
    }

    @Override protected void keyTyped(char typed, int code) throws IOException {
        if (custom.textboxKeyTyped(typed, code)) {
            cfg().customText = custom.getText();
            return;
        }
        if (code == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
    }
    @Override public void updateScreen() { custom.updateCursorCounter(); }
    @Override public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        AlienClient.get().save();
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
