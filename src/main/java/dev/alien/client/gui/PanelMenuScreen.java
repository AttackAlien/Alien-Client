package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.cosmetic.CosmeticStudioScreen;
import dev.alien.client.hud.HudEditorScreen;
import dev.alien.client.world.EffectsScreen;
import dev.alien.client.world.AtmospherePresetScreen;
import dev.alien.client.world.WorldStudioScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** Draggable, independently scrollable visual-module columns. No gameplay modules. */
public final class PanelMenuScreen extends AlienMenuScreen {
    private final GuiScreen parent;
    private GuiTextField search;
    private final List<ColumnView> views = new ArrayList<ColumnView>();
    private final Map<String, Integer> scrolls = new HashMap<String, Integer>();
    private String filter = "", dragging;
    private int page, pageCount = 1, dragX, dragY;
    private GuiButton activeButton;

    public PanelMenuScreen(GuiScreen parent) { super(parent); this.parent = parent; }
    private ClientConfig config() { return AlienClient.get().config(); }
    private PanelLayoutConfig layout() { return config().panels; }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        layout().normalize();
        search = new GuiTextField(1, fontRendererObj, Math.max(100, width - 166), 12, Math.min(154, width - 110), 18);
        search.setMaxStringLength(48); search.setText(filter);
        buttonList.clear();
        buttonList.add(new Ui.Button(900, 12, height - 29, 68, 19, "Settings"));
        buttonList.add(new Ui.Button(901, 84, height - 29, 48, 19, "Reset"));
        buttonList.add(new Ui.Button(902, width - 68, height - 29, 56, 19, "Done"));
        buttonList.add(new Ui.Button(903, width / 2 - 27, height - 29, 22, 19, "<"));
        buttonList.add(new Ui.Button(904, width / 2 + 6, height - 29, 22, 19, ">"));
        rebuildViews();
    }

    private List<Row> rows(String category) {
        final ClientConfig c = config();
        List<Row> rows = new ArrayList<Row>();
        if ("HUD".equals(category)) {
            rows.add(moduleToggle("HUD", "hud"));
            rows.add(toggle("Alien wordmark", () -> c.hud.widgets.get("brand").enabled, () -> c.hud.widgets.get("brand").enabled = !c.hud.widgets.get("brand").enabled));
            rows.add(toggle("Active visuals", () -> c.hud.widgets.get("modules").enabled, () -> c.hud.widgets.get("modules").enabled = !c.hud.widgets.get("modules").enabled));
            rows.add(toggle("Backdrop", () -> c.hud.background, () -> c.hud.background = !c.hud.background));
            rows.add(action("Compact preset", () -> c.hud.applyReferencePreset()));
            rows.add(action("Edit HUD", () -> openEditor(category)));
        } else if ("Cosmetics".equals(category)) {
            rows.add(moduleToggle("Accessories", "cosmetics"));
            rows.add(toggle("Animation", () -> c.cosmetics.self.animations, () -> c.cosmetics.self.animations = !c.cosmetics.self.animations));
            rows.add(toggle("Aura", () -> c.cosmetics.self.aura, () -> c.cosmetics.self.aura = !c.cosmetics.self.aura));
            rows.add(toggle("Trail", () -> c.cosmetics.self.trail, () -> c.cosmetics.self.trail = !c.cosmetics.self.trail));
            rows.add(action("Cosmetic Studio", () -> openEditor(category)));
        } else if ("World".equals(category)) {
            rows.add(moduleToggle("Custom sky", "sky"));
            rows.add(moduleToggle("Visual time", "visual_time"));
            rows.add(moduleToggle("Fog tint", "fog"));
            rows.add(moduleToggle("Weather preview", "weather"));
            rows.add(action("World Studio", () -> openEditor(category)));
            rows.add(action("Atmosphere presets", () -> mc.displayGuiScreen(new AtmospherePresetScreen(this))));
        } else if ("Effects".equals(category)) {
            rows.add(moduleToggle("Visual effects", "effects"));
            rows.add(toggle("Movement", () -> c.effects.movement, () -> c.effects.movement = !c.effects.movement));
            rows.add(toggle("Landing", () -> c.effects.landing, () -> c.effects.landing = !c.effects.landing));
            rows.add(toggle("Trails", () -> c.effects.trails, () -> c.effects.trails = !c.effects.trails));
            rows.add(toggle("Ambient", () -> c.effects.ambient, () -> c.effects.ambient = !c.effects.ambient));
            rows.add(moduleToggle("Camera sway", "camera_sway"));
            rows.add(moduleToggle("Color filter", "color_filter"));
            rows.add(action("Effects Studio", () -> openEditor(category)));
        } else if ("Combat".equals(category)) {
            if (!"unified".equals(BuildEdition.ID)) return rows;
            rows.add(toggle("Combat enabled", () -> c.combat.enabled,
                    () -> { c.combat.enabled = !c.combat.enabled; c.combat.preset = "CUSTOM"; }));
            rows.add(moduleToggle("Aim assist", "aim_assist"));
            rows.add(moduleToggle("Click assist", "click_assist"));
            rows.add(action("Legit preset", () -> c.combat.applyPreset("LEGIT")));
            rows.add(action("Full preset", () -> c.combat.applyPreset("FULL")));
            rows.add(action("Combat Studio", () -> mc.displayGuiScreen(new CombatStudioScreen(this))));
        } else {
            rows.add(toggle("Diagnostics", () -> c.performance.diagnostics, () -> c.performance.diagnostics = !c.performance.diagnostics));
            rows.add(toggle("RGB accent", () -> c.rainbowAccent, () -> c.rainbowAccent = !c.rainbowAccent));
            rows.add(toggle("Alien title", () -> c.menuReplacement, () -> c.menuReplacement = !c.menuReplacement));
            rows.add(action("Profiles / settings", () -> mc.displayGuiScreen(new AlienMenuScreen(this))));
            rows.add(action("Use Studio menu", () -> { c.menuLayout = "Studio"; AlienClient.get().save(); mc.displayGuiScreen(new AlienMenuScreen(parent)); }));
        }
        String query = filter.trim().toLowerCase(Locale.ROOT);
        if (!query.isEmpty() && !category.toLowerCase(Locale.ROOT).contains(query)) {
            List<Row> selected = new ArrayList<Row>();
            for (Row row : rows) if (row.label.toLowerCase(Locale.ROOT).contains(query)) selected.add(row);
            return selected;
        }
        return rows;
    }

    private void rebuildViews() {
        views.clear();
        config().hud.ensureDefaults();
        List<String> categories = new ArrayList<String>();
        for (String category : PanelLayoutConfig.CATEGORIES) if (!rows(category).isEmpty()) categories.add(category);
        int columns = Math.max(1, Math.min(5, (width - 16) / 114));
        pageCount = Math.max(1, (categories.size() + columns - 1) / columns);
        page = Math.max(0, Math.min(pageCount - 1, page));
        int panelWidth = Math.min(156, (width - 24 - (columns - 1) * 8) / columns);
        int start = page * columns;
        for (int i = start; i < Math.min(categories.size(), start + columns); i++) {
            String id = categories.get(i);
            List<Row> rows = rows(id);
            PanelLayoutConfig.Column position = layout().columns.get(id);
            int panelHeight = position.open ? Math.min(height - 88, 25 + rows.size() * 20 + 5) : 24;
            int x = position.customPosition ? 12 + Math.round(position.x * Math.max(0, width - panelWidth - 24)) : 12 + (i - start) * (panelWidth + 8);
            int y = position.customPosition ? 46 + Math.round(position.y * Math.max(0, height - panelHeight - 86)) : 46;
            x = Math.max(6, Math.min(width - panelWidth - 6, x));
            y = Math.max(40, Math.min(height - panelHeight - 36, y));
            int maxScroll = Math.max(0, rows.size() * 20 - (panelHeight - 29));
            int scroll = Math.max(0, Math.min(maxScroll, scrolls.containsKey(id) ? scrolls.get(id) : 0));
            scrolls.put(id, scroll);
            views.add(new ColumnView(id, x, y, panelWidth, panelHeight, rows, scroll));
        }
        for (GuiButton button : buttonList) if (button.id == 903 || button.id == 904) button.enabled = pageCount > 1;
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x4910131c);
        drawRect(12, 13, 15, 30, Ui.accent());
        fontRendererObj.drawStringWithShadow("ALIEN", 22, 13, Ui.TEXT);
        fontRendererObj.drawString("unified".equals(BuildEdition.ID) ? "UNIFIED / COMBAT PROFILES" :
                BuildEdition.ALLOW_FULL ? "FULL / ARCHITECTURE" : "LEGIT / VISUALS", 22, 25, Ui.MUTED);
        search.drawTextBox();
        if (!search.isFocused() && search.getText().isEmpty()) fontRendererObj.drawString("Search modules...", search.xPosition + 4, search.yPosition + 5, Ui.MUTED);
        rebuildViews();
        ColumnView dragView = null;
        for (ColumnView view : views) {
            if (view.id.equals(dragging)) dragView = view;
            else drawColumn(view, mouseX, mouseY);
        }
        if (dragView != null) drawColumn(dragView, mouseX, mouseY);
        for (GuiButton button : buttonList) button.drawButton(mc, mouseX, mouseY);
        if (pageCount > 1) drawCenteredString(fontRendererObj, (page + 1) + " / " + pageCount, width / 2, height - 39, Ui.MUTED);
        if (views.isEmpty()) drawCenteredString(fontRendererObj, "No matching visual modules", width / 2, height / 2, Ui.MUTED);
        if (height > 270) drawCenteredString(fontRendererObj, "Drag headers  /  Scroll a panel  /  Right click for studio", width / 2, height - 47, Ui.MUTED);
    }

    private void drawColumn(ColumnView view, int mouseX, int mouseY) {
        int x = view.x, y = view.y, w = view.width, h = view.height;
        drawRect(x + 2, y + 3, x + w + 2, y + h + 3, 0x60000000);
        Ui.panel(x, y, w, h, Ui.alpha(0xff0c0d15, config().opacity));
        drawGradientRect(x + 1, y + 1, x + w - 1, y + 24, 0xf0241c36, 0xf012101c);
        drawRect(x + 8, y + 9, x + 10, y + 15, Ui.accent());
        fontRendererObj.drawStringWithShadow(view.id, x + 16, y + 8, Ui.TEXT);
        fontRendererObj.drawString("+", x + w - 30, y + 8, Ui.MUTED);
        fontRendererObj.drawString(layout().columns.get(view.id).open ? "-" : "+", x + w - 13, y + 8, Ui.MUTED);
        if (!layout().columns.get(view.id).open) return;
        ScaledResolution resolution = new ScaledResolution(mc);
        int factor = resolution.getScaleFactor();
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT | GL11.GL_ENABLE_BIT);
        try {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(x * factor, (height - y - h + 3) * factor, w * factor, Math.max(0, h - 28) * factor);
            int rowY = y + 26 - view.scroll;
            for (Row row : view.rows) {
                boolean hovered = mouseX >= x + 3 && mouseX < x + w - 3 && mouseY >= Math.max(rowY, y + 25) && mouseY < Math.min(rowY + 20, y + h - 3);
                boolean enabled = row.enabled != null && row.enabled.getAsBoolean();
                if (enabled) drawGradientRect(x + 3, rowY, x + w - 3, rowY + 19, 0xb54e2f78, 0x76352a50);
                else if (hovered) drawRect(x + 3, rowY, x + w - 3, rowY + 19, 0x9c292533);
                if (enabled) drawRect(x + 3, rowY + 3, x + 5, rowY + 16, Ui.accent());
                fontRendererObj.drawStringWithShadow(fontRendererObj.trimStringToWidth(row.label, w - 27), x + 9, rowY + 6, enabled ? Ui.TEXT : 0xffc1bdcc);
                fontRendererObj.drawString(row.enabled == null ? ">" : enabled ? "\u2022" : "\u00b7", x + w - 14, rowY + 6, enabled ? Ui.accent() : Ui.MUTED);
                rowY += 20;
            }
        } finally { GL11.glPopAttrib(); }
        int maxScroll = Math.max(0, view.rows.size() * 20 - (h - 29));
        if (maxScroll > 0) {
            int barHeight = Math.max(12, (h - 29) * (h - 29) / (view.rows.size() * 20));
            int barY = y + 26 + view.scroll * Math.max(0, h - 29 - barHeight) / maxScroll;
            drawRect(x + w - 3, barY, x + w - 1, barY + barHeight, Ui.accent());
        }
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        search.mouseClicked(x, y, button);
        if (button == 0) for (GuiButton b : buttonList) if (b.mousePressed(mc, x, y)) {
            activeButton = b; b.playPressSound(mc.getSoundHandler()); actionPerformed(b); return;
        }
        for (int i = views.size() - 1; i >= 0; i--) {
            ColumnView view = views.get(i);
            if (x < view.x || x >= view.x + view.width || y < view.y || y >= view.y + view.height) continue;
            if (button == 1) { openEditor(view.id); return; }
            if (button != 0) return;
            if (y < view.y + 24) {
                if (x >= view.x + view.width - 20) { layout().columns.get(view.id).open = !layout().columns.get(view.id).open; AlienClient.get().save(); }
                else if (x >= view.x + view.width - 38) openEditor(view.id);
                else { dragging = view.id; dragX = x - view.x; dragY = y - view.y; }
            } else if (layout().columns.get(view.id).open) {
                int index = (y - view.y - 26 + view.scroll) / 20;
                if (y >= view.y + 26 && index >= 0 && index < view.rows.size()) {
                    view.rows.get(index).action.run(); AlienClient.get().save();
                }
            }
            return;
        }
    }

    @Override protected void mouseClickMove(int x, int y, int button, long elapsed) {
        if (dragging == null || button != 0) return;
        for (ColumnView view : views) if (view.id.equals(dragging)) {
            PanelLayoutConfig.Column position = layout().columns.get(dragging);
            position.customPosition = true;
            position.x = Math.max(0f, Math.min(1f, (x - dragX - 12f) / Math.max(1, width - view.width - 24)));
            position.y = Math.max(0f, Math.min(1f, (y - dragY - 46f) / Math.max(1, height - view.height - 86)));
            return;
        }
    }
    @Override protected void mouseReleased(int x, int y, int state) {
        if (activeButton != null && state == 0) { activeButton.mouseReleased(x, y); activeButton = null; }
        if (dragging != null) { dragging = null; AlienClient.get().save(); }
    }
    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int delta = Mouse.getEventDWheel();
        if (delta == 0) return;
        int x = Mouse.getEventX() * width / mc.displayWidth, y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        for (ColumnView view : views) if (x >= view.x && x < view.x + view.width && y >= view.y + 24 && y < view.y + view.height) {
            int maximum = Math.max(0, view.rows.size() * 20 - (view.height - 29));
            scrolls.put(view.id, Math.max(0, Math.min(maximum, view.scroll + (delta > 0 ? -20 : 20)))); return;
        }
        page = Math.max(0, Math.min(pageCount - 1, page + (delta > 0 ? -1 : 1)));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 900) mc.displayGuiScreen(new AlienMenuScreen(this));
        if (button.id == 901) { layout().reset(); scrolls.clear(); AlienClient.get().save(); }
        if (button.id == 902) mc.displayGuiScreen(parent);
        if (button.id == 903) page = Math.max(0, page - 1);
        if (button.id == 904) page = Math.min(pageCount - 1, page + 1);
    }
    private void openEditor(String category) {
        if ("HUD".equals(category)) mc.displayGuiScreen(new HudEditorScreen(this));
        else if ("Cosmetics".equals(category)) mc.displayGuiScreen(new CosmeticStudioScreen(this));
        else if ("World".equals(category)) mc.displayGuiScreen(new WorldStudioScreen(this));
        else if ("Effects".equals(category)) mc.displayGuiScreen(new EffectsScreen(this));
        else if ("Combat".equals(category) && "unified".equals(BuildEdition.ID))
            mc.displayGuiScreen(new CombatStudioScreen(this));
        else mc.displayGuiScreen(new AlienMenuScreen(this));
    }
    @Override protected void keyTyped(char typedChar, int code) throws IOException {
        if (search.textboxKeyTyped(typedChar, code)) { filter = search.getText(); page = 0; return; }
        if (code == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
    }
    @Override public void updateScreen() { search.updateCursorCounter(); }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); AlienClient.get().save(); }
    @Override public boolean doesGuiPauseGame() { return false; }
    private Row moduleToggle(String label, String id) {
        return toggle(label, () -> AlienClient.get().modules().find(id).enabled(),
                () -> AlienClient.get().modules().toggle(id));
    }
    private static Row toggle(String label, BooleanSupplier enabled, Runnable action) { return new Row(label, enabled, action); }
    private static Row action(String label, Runnable action) { return new Row(label, null, action); }
    private static final class Row {
        final String label; final BooleanSupplier enabled; final Runnable action;
        Row(String label, BooleanSupplier enabled, Runnable action) { this.label = label; this.enabled = enabled; this.action = action; }
    }
    private static final class ColumnView {
        final String id; final int x, y, width, height, scroll; final List<Row> rows;
        ColumnView(String id, int x, int y, int width, int height, List<Row> rows, int scroll) {
            this.id = id; this.x = x; this.y = y; this.width = width; this.height = height; this.rows = rows; this.scroll = scroll;
        }
    }
}
