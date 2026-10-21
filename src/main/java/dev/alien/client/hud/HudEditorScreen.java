package dev.alien.client.hud;

import dev.alien.client.AlienClient;
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
public final class HudEditorScreen extends GuiScreen {
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

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        ids.clear();
        for (String id : cfg().widgets.keySet()) if (HudManager.known(id)) ids.add(id);
        rows = Math.max(1, Math.min(10, (height - 196) / 18));
        scroll = Math.max(0, Math.min(scroll, Math.max(0, ids.size() - rows)));
        rebuildButtons();
        custom = new GuiTextField(50, fontRendererObj, 12, height - 40, 146, 16);
        custom.setMaxStringLength(100);
        custom.setText(cfg().customText);
    }

    private void rebuildButtons() {
        buttonList.clear();
        for (int i = 0; i < rows && scroll + i < ids.size(); i++) {
            String id = ids.get(scroll + i);
            buttonList.add(new GuiButton(100 + scroll + i, 10, 56 + i * 18, 150, 17,
                (cfg().widgets.get(id).enabled ? "[+] " : "[-] ") + HudConfig.label(id)));
        }
        buttonList.add(new GuiButton(20, 10, 35, 42, 17, "Up"));
        buttonList.add(new GuiButton(21, 54, 35, 42, 17, "Down"));
        buttonList.add(new GuiButton(10, 100, 35, 60, 17, "Reset"));
        buttonList.add(new GuiButton(0, 10, height - 132, 95, 18, "Toggle selected"));
        buttonList.add(new GuiButton(1, 109, height - 132, 24, 18, "-"));
        buttonList.add(new GuiButton(2, 136, height - 132, 24, 18, "+"));
        buttonList.add(new GuiButton(3, 10, height - 110, 73, 18, "Snap: " + (cfg().snapToGrid ? "ON" : "OFF")));
        buttonList.add(new GuiButton(13, 87, height - 110, 73, 18, "Backdrop"));
        buttonList.add(new GuiButton(4, 10, height - 88, 46, 18, "Left"));
        buttonList.add(new GuiButton(5, 60, height - 88, 46, 18, "Center"));
        buttonList.add(new GuiButton(6, 110, height - 88, 50, 18, "Right"));
        buttonList.add(new GuiButton(7, 10, height - 66, 46, 18, "Top"));
        buttonList.add(new GuiButton(8, 60, height - 66, 46, 18, "Middle"));
        buttonList.add(new GuiButton(9, 110, height - 66, 50, 18, "Bottom"));
        buttonList.add(new GuiButton(11, 10, height - 20, 73, 18, "Done"));
        buttonList.add(new GuiButton(12, 87, height - 20, 73, 18, "HUD: " + (cfg().enabled ? "ON" : "OFF")));
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x60101420);
        if (cfg().snapToGrid) {
            for (int x = 0; x < width; x += cfg().gridSize) drawRect(x, 28, x + 1, height, 0x112e3655);
            for (int y = 28; y < height; y += cfg().gridSize) drawRect(0, y, width, y + 1, 0x112e3655);
        }
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
        drawRect(0, 0, width, 27, 0xf0121422);
        drawString(fontRendererObj, "ALIEN / HUD EDITOR", 10, 8, 0xffd3b5ff);
        if (width > 480) drawString(fontRendererObj, "Drag widgets | Corner / wheel: scale | Right click: toggle", 175, 8, 0xffc4c7da);
        drawRect(6, 30, 165, height, 0xed121522);
        HudConfig.Widget widget = cfg().widgets.get(selected);
        String label = HudConfig.label(selected) + (widget == null ? "" : String.format(" %.2fx", widget.scale));
        drawString(fontRendererObj, label, 10, height - 145, 0xffd3b5ff);
        super.drawScreen(mouseX, mouseY, partialTicks);
        custom.drawTextBox();
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
            case 10: cfg().widgets.clear(); cfg().ensureDefaults(); break;
            case 11: mc.displayGuiScreen(parent); return;
            case 12: cfg().enabled = !cfg().enabled; break;
            case 13: cfg().background = !cfg().background; break;
            case 20: scroll = Math.max(0, scroll - 1); break;
            case 21: scroll = Math.min(Math.max(0, ids.size() - rows), scroll + 1); break;
            default: break;
        }
        AlienClient.get().save();
        rebuildButtons();
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        custom.mouseClicked(x, y, button);
        if (x <= 170 || y < 28) {
            super.mouseClicked(x, y, button);
            // A sidebar entry doubles as a drag handle, including for widgets under the panel.
            if (button == 0 && x >= 10 && x <= 160 && y >= 56 && y < 56 + rows * 18) {
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
        if (x <= 170) {
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
