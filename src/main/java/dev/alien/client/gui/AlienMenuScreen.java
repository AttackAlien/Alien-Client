package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.cosmetic.CosmeticStudioScreen;
import dev.alien.client.hud.HudEditorScreen;
import dev.alien.client.performance.PerformanceEngine;
import dev.alien.client.world.EffectsScreen;
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
import java.util.List;
import java.util.Locale;

/** Live client control center. Studio buttons open the actual in-game editors. */
public class AlienMenuScreen extends dev.alien.client.gui.PremiumScreen {
    private static final String[] PAGES = {"Dashboard", "Appearance", "Performance", "HUD Editor", "Cosmetic Studio", "World Studio", "Effects Studio", "Profiles", "Combat Studio"};
    private final GuiScreen parent;
    private final List<Label> labels = new ArrayList<Label>();
    private GuiTextField search, name;
    private int panelX, panelY, panelW, panelH, sideW, contentX, contentW, contentTop, contentBottom;
    private int page, scroll, contentHeight;
    private boolean capturingKey;
    private GuiButton activeButton;
    private int pendingConfirmation = -1;
    private String confirmationName = "";
    private long confirmationUntil;
    private String searchValue = "", nameValue = "default", message = "";
    private long messageUntil;
    private long opened = System.nanoTime();

    public AlienMenuScreen(GuiScreen parent) { this.parent = parent; }

    /** Keep the classic studio available while allowing the in-world panel layout. */
    public static AlienMenuScreen create(GuiScreen parent) {
        if("Gallery".equals(AlienClient.get().config().menuLayout))return new dev.alien.client.studio.ModuleGalleryScreen(parent);
        return "Panels".equals(AlienClient.get().config().menuLayout)
                ? new PanelMenuScreen(parent) : new AlienMenuScreen(parent);
    }

    public AlienMenuScreen openPage(int selected) { page = selected == 1 || selected == 2 || selected == 7 ? selected : 0; return this; }

    @Override public void initGui() {
        fontRendererObj = Ui.font();
        Keyboard.enableRepeatEvents(true);
        panelW = Math.min(860, width - 20); panelH = Math.min(height - 24, 520);
        panelX = (width - panelW) / 2; panelY = 12;
        sideW = panelW >= 620 ? 144 : 112;
        contentX = panelX + sideW + 16; contentW = panelW - sideW - 32;
        contentTop = panelY + 69; contentBottom = panelY + panelH - 31;
        search = new GuiTextField(1, fontRendererObj, contentX + Math.max(0, contentW - 154), panelY + 17, Math.min(154, contentW), 18);
        search.setMaxStringLength(40); search.setText(searchValue); search.setEnableBackgroundDrawing(false); search.setTextColor(config().ui.text);
        name = new GuiTextField(2, fontRendererObj, contentX, contentTop - scroll + 20, contentW, 19);
        name.setMaxStringLength(40); name.setText(nameValue); name.setEnableBackgroundDrawing(false); name.setTextColor(config().ui.text);
        rebuild();
    }

    private ClientConfig config() { return AlienClient.get().config(); }

    private void rebuild() {
        buttonList.clear(); labels.clear();
        int navHeight = Math.max(14, Math.min(25, (panelH - 65) / ("unified".equals(BuildEdition.ID) ? 9 : 8) - 2));
        int navY = panelY + 52;
        String filter = searchValue.trim().toLowerCase(Locale.ROOT);
        for (int i = 0; i < PAGES.length; i++) {
            if (i == 8 && !"unified".equals(BuildEdition.ID)) continue;
            if (!filter.isEmpty() && !PAGES[i].toLowerCase(Locale.ROOT).contains(filter)) continue;
            buttonList.add(new NavigationButton(10 + i, panelX + 9, navY, sideW - 18, navHeight, PAGES[i], i, page == i));
            navY += navHeight + 2;
        }
        buttonList.add(new Ui.Button(90, panelX + panelW - 64, panelY + panelH - 25, 55, 18, "Back"));
        int y = contentTop - scroll;
        if (page == 0) dashboard(y);
        else if (page == 1) appearance(y);
        else if (page == 2) performance(y);
        else if (page == 7) profiles(y);
    }

    private void dashboard(int y) {
        label("YOUR CLIENT. YOUR SPACE.", y, Ui.accent());
        label("Client-side visuals and quality of life for Forge 1.8.9.", y + 17, config().ui.muted);
        label("Open a studio to edit your live world and character.", y + 31, config().ui.muted);
        int row = y + 56;
        studio(13, "HUD Editor", "Drag, resize and align your widgets.", row);
        studio(14, "Cosmetic Studio", "3D accessories and local player overrides.", row + 52);
        studio(15, "World Studio", "Visual sky, atmosphere and weather.", row + 104);
        studio(16, "Effects Studio", "Motion particles and cosmetic effects.", row + 156);
        int details = row + 218;
        if ("unified".equals(BuildEdition.ID)) {
            studio(18, "Combat Studio", "Off / Legit / Full / Custom; tune aim and click assist.", row + 208);
            details += 52;
        }
        label("Performance preset: " + config().performance.preset, details, config().ui.muted);
        label("Menu key: " + keyName(config().menuKey) + "  |  All changes save automatically.", details + 16, config().ui.muted);
        contentHeight = details - y + 82;
    }

    private void studio(int id, String title, String description, int y) {
        buttonList.add(new Ui.Button(400 + id, contentX, y, contentW, 25, title));
        label(description, y + 31, config().ui.muted);
    }

    private void appearance(int y) {
        ClientConfig c = config();
        int half = (contentW - 8) / 2;
        add(100, contentX, y, half, "Home menu: " + on(c.menuReplacement));
        add(101, contentX + half + 8, y, half, "Animations: " + on(c.animations));
        add(102, contentX, y + 30, half, capturingKey ? "Press a key..." : "Menu key: " + keyName(c.menuKey));
        add(103, contentX + half + 8, y + 30, half, "RGB accent: " + on(c.rainbowAccent));
        add(109, contentX, y + 60, contentW, "Open Theme Studio");
        slider(150, y + 99, "Panel opacity", .3f, 1, .01f, c.ui.opacity, v -> { c.opacity = v; c.ui.opacity = v; });
        slider(151, y + 137, "Animation speed", .25f, 3f, .05f, c.ui.animationSpeed, v -> { c.animationSpeed = v; c.ui.animationSpeed = v; });
        slider(152, y + 175, "Accent red", 0, 255, 1, (c.ui.primary >> 16) & 255, v -> { c.ui.primary = (c.ui.primary & 0xff00ffff) | (Math.round(v) << 16); c.accentColor = c.ui.primary; });
        slider(153, y + 213, "Accent green", 0, 255, 1, (c.ui.primary >> 8) & 255, v -> { c.ui.primary = (c.ui.primary & 0xffff00ff) | (Math.round(v) << 8); c.accentColor = c.ui.primary; });
        slider(154, y + 251, "Accent blue", 0, 255, 1, c.ui.primary & 255, v -> { c.ui.primary = (c.ui.primary & 0xffffff00) | Math.round(v); c.accentColor = c.ui.primary; });
        label("THEME LIBRARY", y + 291, Ui.accent());
        int quarter = (contentW - 18) / 4;
        String[] themes = {"Nebula", "Sakura", "Ocean", "Ember"};
        for (int i = 0; i < themes.length; i++) add(110 + i, contentX + i * (quarter + 6), y + 309, quarter, themes[i]);
        label("Theme name (letters, numbers, _ and -)", y + 343, config().ui.muted);
        name.yPosition = y + 358;
        add(120, contentX, y + 387, half, "Save theme");
        add(121, contentX + half + 8, y + 387, half, "Load theme");
        add(122, contentX, y + 417, half, "Export theme");
        add(123, contentX + half + 8, y + 417, half, "Import theme");
        label("Import: themes/imports/<name>.json", y + 453, config().ui.muted);
        label("Export: themes/exports/<name>.json", y + 468, config().ui.muted);
        add(104, contentX, y + 491, contentW, "Module menu: " + c.menuLayout + " / switch");
        contentHeight = 527;
    }

    private void performance(int y) {
        ClientConfig c = config();
        label("Choose an effects budget. No FPS claims are assumed.", y, config().ui.muted);
        int third = (contentW - 12) / 3;
        String[] presets = {"Competitive", "Balanced", "Cinematic"};
        for (int i = 0; i < presets.length; i++) {
            buttonList.add(new Ui.Button(200 + i, contentX + i * (third + 6), y + 22, third, 25, presets[i]).selected(presets[i].equals(c.performance.preset)));
        }
        slider(250, y + 64, "Particle budget / tick", 0, 500, 1, c.performance.maxParticles, v -> c.performance.maxParticles = Math.round(v));
        slider(251, y + 104, "Cosmetic render budget", 0, 128, 1, c.performance.maxCosmetics, v -> c.performance.maxCosmetics = Math.round(v));
        slider(252, y + 144, "Effect distance (blocks)", 8, 96, 1, c.performance.effectDistance, v -> c.performance.effectDistance = v);
        add(203, contentX, y + 187, contentW, "Performance diagnostics: " + on(c.performance.diagnostics));
        add(204, contentX, y + 223, contentW, "Open live Performance Dashboard / benchmark");
        label("Adaptive mode is optional and never rewrites your saved budgets.", y + 257, config().ui.muted);
        label("Samples must run in-world (not in the menu).", y + 272, config().ui.muted);
        contentHeight = 300;
    }

    private void profiles(int y) {
        label("Profile name", y, config().ui.muted); name.yPosition = y + 17;
        int half = (contentW - 8) / 2;
        add(300, contentX, y + 46, half, "Save current");
        add(301, contentX + half + 8, y + 46, half, "Load profile");
        add(302, contentX, y + 76, half, "Delete profile");
        add(303, contentX + half + 8, y + 76, half, "Server profiles: " + on(config().perServerProfiles));
        add(304, contentX, y + 106, half, "Export profile");
        add(305, contentX + half + 8, y + 106, half, "Import profile");
        add(306, contentX, y + 136, half, "Bind current server");
        add(307, contentX + half + 8, y + 136, half, "Restore defaults");
        label("Available profiles", y + 179, Ui.accent());
        List<String> profiles = AlienClient.get().store().listProfiles();
        int profileY = y + 198;
        if (profiles.isEmpty()) label("No saved profiles yet.", profileY, config().ui.muted);
        int index = 0;
        for (String profile : profiles) {
            if (index >= 30) { label("More profiles can be selected by name above.", profileY, config().ui.muted); break; }
            add(500 + index++, contentX, profileY, contentW, profile);
            profileY += 29;
        }
        label("Import: imports/<name>.json; export: exports/<name>.json", profileY + 27, config().ui.muted);
        label("Server associations load only local client settings.", profileY + 43, config().ui.muted);
        contentHeight = profileY - y + 68;
    }

    private void add(int id, int x, int y, int width, String text) { buttonList.add(new Ui.Button(id, x, y, width, 24, text)); }
    private void label(String text, int y, int color) { labels.add(new Label(text, y, color)); }
    private void slider(int id, int y, String text, float min, float max, float step, float value, java.util.function.Consumer<Float> change) {
        buttonList.add(new ValueSlider(id, contentX, y, contentW, text, min, max, step, value, change, () -> AlienClient.get().save()));
    }

    @Override public void drawContents(int mouseX, int mouseY, float partialTicks) {
        Ui.background(width,height);
        Ui.shadow(panelX, panelY, panelW, panelH, config().ui.radius, config().ui.shadow);
        Ui.rounded(panelX, panelY, panelW, panelH, config().ui.radius, Ui.alpha(config().ui.surface, config().ui.opacity));
        Ui.rounded(panelX + 4, panelY + 4, sideW - 4, panelH - 8, Math.max(3, config().ui.radius - 2), config().ui.surfaceRaised);
        Ui.icon("alien", panelX + 11, panelY + 12, 24, Ui.accent());
        fontRendererObj.drawStringWithShadow("ALIEN", panelX + 41, panelY + 13, config().ui.text);
        fontRendererObj.drawString("SETTINGS", panelX + 41, panelY + 27, config().ui.muted);
        fontRendererObj.drawStringWithShadow(PAGES[page], contentX, panelY + 45, config().ui.text);
        field(search);
        if (!search.isFocused() && search.getText().isEmpty()) fontRendererObj.drawString("Search categories...", search.xPosition + 4, search.yPosition + 5, config().ui.muted);
        for (GuiButton b : buttonList) if (b.id < 100) b.drawButton(mc, mouseX, mouseY);
        ScaledResolution resolution = new ScaledResolution(mc);
        float factor = framebufferScale();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(Math.round(contentX*factor),mc.displayHeight-Math.round(contentBottom*factor),Math.round(contentW*factor),Math.round(Math.max(0,contentBottom-contentTop)*factor));
        for (Label label : labels) fontRendererObj.drawString(fontRendererObj.trimStringToWidth(label.text, contentW), contentX, label.y, label.color);
        for (GuiButton b : buttonList) if (b.id >= 100) b.drawButton(mc, mouseX, mouseY);
        if (page == 1 || page == 7) field(name);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        if (contentHeight > contentBottom - contentTop) {
            int available = contentBottom - contentTop;
            int thumb = Math.max(12, available * available / contentHeight);
            int thumbY = contentTop + scroll * (available - thumb) / Math.max(1, contentHeight - available);
            drawRect(contentX + contentW + 6, contentTop, contentX + contentW + 8, contentBottom, 0xff292634);
            drawRect(contentX + contentW + 6, thumbY, contentX + contentW + 8, thumbY + thumb, Ui.accent());
        }
        String saveError = AlienClient.get().lastError();
        String footer = !saveError.isEmpty() ? saveError : System.currentTimeMillis() < messageUntil ? message : ("unified".equals(BuildEdition.ID) ? "UNIFIED  /  COMBAT " + (config().combat.enabled ? "ON" : "OFF") : "LOCAL VISUALS  /  NO GAMEPLAY CHANGES");
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(footer, panelW - sideW - 100), contentX, panelY + panelH - 19, config().ui.muted);
        if (config().animations && !config().ui.reducedMotion) {
            float progress = Math.min(1f, (System.nanoTime() - opened) / (240000000f / config().animationSpeed));
            if (progress < 1) Ui.rounded(panelX, panelY, panelW, panelH, config().ui.radius, Ui.alpha(config().ui.surface, (1f - progress) * .7f));
        }
        if (pendingConfirmation >= 0 && System.currentTimeMillis() < confirmationUntil) drawConfirmation();
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        int id = button.id;
        if (id == 90) { mc.displayGuiScreen(parent); return; }
        if (id >= 10 && id < 19) { navigate(id - 10); return; }
        if (id >= 413 && id <= 418) { navigate(id - 410); return; }
        ClientConfig c = config();
        if (id == 100) c.menuReplacement = !c.menuReplacement;
        else if (id == 101) c.animations = !c.animations;
        else if (id == 102) capturingKey = !capturingKey;
        else if (id == 103) c.rainbowAccent = !c.rainbowAccent;
        else if (id == 109) { mc.displayGuiScreen(new ThemeEditorScreen(this)); return; }
        else if (id == 104) c.menuLayout = "Panels".equals(c.menuLayout) ? "Studio" : "Panels";
        else if (id >= 110 && id <= 113) {
            String[] names = {"Nebula", "Sakura", "Ocean", "Ember"};
            int[] colors = {0xffa882ff, 0xffff8ebc, 0xff5ed3e6, 0xffffad62};
            c.theme = names[id - 110]; c.accentColor = colors[id - 110]; c.ui.primary = c.accentColor; c.rainbowAccent = false;
        } else if (id >= 120 && id <= 123) themeAction(id);
        else if (id >= 200 && id <= 202) PerformanceEngine.applyPreset(c, new String[] {"Competitive", "Balanced", "Cinematic"}[id - 200]);
        else if (id == 203) c.performance.diagnostics = !c.performance.diagnostics;
        else if (id == 204) { mc.displayGuiScreen(new dev.alien.client.performance.PerformanceDashboardScreen(this)); return; }
        else if (id == 303) c.perServerProfiles = !c.perServerProfiles;
        else if (id >= 300 && id <= 307) profileAction(id);
        else if (id >= 500) {
            List<String> profiles = AlienClient.get().store().listProfiles();
            int index = id - 500;
            if (index < profiles.size()) { nameValue = profiles.get(index); name.setText(nameValue); }
        }
        AlienClient.get().save(); rebuild();
    }

    private void navigate(int next) {
        capturingKey = false;
        if (next == 3) mc.displayGuiScreen(new HudEditorScreen(this));
        else if (next == 4) mc.displayGuiScreen(new CosmeticStudioScreen(this));
        else if (next == 5) mc.displayGuiScreen(new WorldStudioScreen(this));
        else if (next == 6) mc.displayGuiScreen(new EffectsScreen(this));
        else if (next == 8 && "unified".equals(BuildEdition.ID)) mc.displayGuiScreen(new CombatStudioScreen(this));
        else { page = next; scroll = 0; rebuild(); }
    }

    private void themeAction(int id) {
        ThemeLibrary library = new ThemeLibrary(AlienClient.get().store().directory());
        try {
            if (id == 120) library.save(nameValue, config());
            if (id == 121) library.load(nameValue, config());
            if (id == 122) library.exportTheme(nameValue, config());
            if (id == 123) library.importTheme(nameValue, config());
            status(id == 122 ? "Theme exported to themes/exports/." : "Theme updated: " + nameValue);
        } catch (IOException ex) { status(ex.getMessage()); }
    }

    private void profileAction(int id) {
        try {
            if (id == 300) { AlienClient.get().store().saveProfile(nameValue); status("Profile saved: " + nameValue); }
            if (id == 301) status(AlienClient.get().store().loadProfile(nameValue) ? "Profile loaded: " + nameValue : "Profile not found.");
            if (id == 302 && confirm(id)) { AlienClient.get().store().deleteProfile(nameValue); status("Profile deleted: " + nameValue); }
            if (id == 304) { AlienClient.get().store().exportProfile(nameValue); status("Profile exported to exports/."); }
            if (id == 305) status(AlienClient.get().store().importProfile(nameValue) ? "Profile imported: " + nameValue : "Profile import failed.");
            if (id == 306) {
                String server = AlienClient.get().currentServerKey();
                if (server == null) status("Join a server before binding a profile.");
                else { AlienClient.get().store().bindServer(server, nameValue); status("Profile bound to the current server."); }
            }
            if (id == 307 && confirm(id)) { AlienClient.get().store().resetDefaults(); status("Client defaults restored."); }
        } catch (IOException ex) { status(ex.getMessage()); }
    }

    private boolean confirm(int id) {
        pendingConfirmation = id; confirmationName = nameValue; confirmationUntil = System.currentTimeMillis() + 30000;
        return false;
    }

    private void status(String text) { message = text == null ? "Operation failed." : text; messageUntil = System.currentTimeMillis() + 6500; }
    private static String on(boolean value) { return value ? "ON" : "OFF"; }
    private static String keyName(int code) { String result = Keyboard.getKeyName(code); return result == null ? Integer.toString(code) : result; }

    @Override public void updateScreen() { search.updateCursorCounter(); name.updateCursorCounter(); }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (pendingConfirmation >= 0 && System.currentTimeMillis() < confirmationUntil) { if (keyCode == Keyboard.KEY_ESCAPE) pendingConfirmation = -1; return; }
        if (capturingKey) {
            if (keyCode != Keyboard.KEY_ESCAPE && keyCode != Keyboard.KEY_NONE) config().menuKey = keyCode;
            capturingKey = false; AlienClient.get().save(); rebuild(); return;
        }
        if (search.textboxKeyTyped(typedChar, keyCode)) { searchValue = search.getText(); rebuild(); return; }
        if ((page == 1 || page == 7) && name.textboxKeyTyped(typedChar, keyCode)) { nameValue = name.getText(); return; }
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        if (pendingConfirmation >= 0 && System.currentTimeMillis() < confirmationUntil) {
            int dx = (width - 270) / 2, dy = (height - 116) / 2;
            if (button == 0 && x >= dx + 142 && x < dx + 256 && y >= dy + 78 && y < dy + 102) completeConfirmation();
            else if (button == 0 && x >= dx + 14 && x < dx + 128 && y >= dy + 78 && y < dy + 102) pendingConfirmation = -1;
            return;
        }
        search.mouseClicked(x, y, button);
        if ((page == 1 || page == 7) && y >= contentTop && y < contentBottom) name.mouseClicked(x, y, button);
        else name.setFocused(false);
        if (button != 0) return;
        for (GuiButton b : buttonList) {
            if (b.id >= 100 && (y < contentTop || y >= contentBottom)) continue;
            if (b.mousePressed(mc, x, y)) {
                b.playPressSound(mc.getSoundHandler()); activeButton = b;
                if (!(b instanceof ValueSlider)) actionPerformed(b);
                break;
            }
        }
    }

    @Override protected void mouseReleased(int x, int y, int state) {
        if (activeButton != null && state == 0) { activeButton.mouseReleased(x, y); activeButton = null; }
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (name == null) return; // The floating-panel subclass has its own controls.
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            nameValue = name.getText();
            int maximum = Math.max(0, contentHeight - (contentBottom - contentTop));
            scroll = Math.max(0, Math.min(maximum, scroll + (wheel > 0 ? -30 : 30)));
            rebuild();
        }
    }

    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); AlienClient.get().save(); }
    @Override public boolean doesGuiPauseGame() { return false; }
    private void field(GuiTextField field) {
        Ui.rounded(field.xPosition - 4, field.yPosition - 3, field.width + 8, field.height + 6, 5, config().ui.surfaceRaised);
        field.yPosition += 4; field.drawTextBox(); field.yPosition -= 4;
    }
    private void drawConfirmation() {
        int x = (width - 270) / 2, y = (height - 116) / 2;
        drawRect(0, 0, width, height, 0xaa000208);
        Ui.shadow(x, y, 270, 116, 11, .8f); Ui.rounded(x, y, 270, 116, 11, config().ui.surface);
        fontRendererObj.drawString(pendingConfirmation == 302 ? "Delete this profile?" : "Restore client defaults?", x + 15, y + 15, config().ui.text);
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(pendingConfirmation == 302 ? confirmationName : "Your current preferences will be replaced.", 240), x + 15, y + 38, config().ui.muted);
        Ui.rounded(x + 14, y + 78, 114, 24, 6, config().ui.surfaceRaised);
        Ui.rounded(x + 142, y + 78, 114, 24, 6, Ui.mix(config().ui.surfaceRaised, 0xffef6f7d, .24f));
        drawCenteredString(fontRendererObj, "Cancel", x + 71, y + 85, config().ui.text);
        drawCenteredString(fontRendererObj, "Confirm", x + 199, y + 85, config().ui.text);
    }
    private void completeConfirmation() {
        int action = pendingConfirmation; pendingConfirmation = -1;
        try {
            if (action == 302) { AlienClient.get().store().deleteProfile(confirmationName); status("Profile deleted: " + confirmationName); }
            else if (action == 307) { AlienClient.get().store().resetDefaults(); status("Defaults restored"); }
            AlienClient.get().save(); initGui();
        } catch (IOException failure) { status(failure.getMessage()); }
    }
    private final class NavigationButton extends Ui.Button {
        private final int pageIndex; private final boolean selected;
        NavigationButton(int id, int x, int y, int width, int height, String text, int pageIndex, boolean selected) {
            super(id, x, y, width, height, text); this.pageIndex = pageIndex; this.selected = selected;
        }
        @Override public void drawButton(net.minecraft.client.Minecraft minecraft, int mx, int my) {
            hovered = mx >= xPosition && my >= yPosition && mx < xPosition + width && my < yPosition + height;
            int color = selected ? Ui.mix(config().ui.surfaceRaised, Ui.accent(), .18f) : hovered ? Ui.mix(config().ui.surfaceRaised, config().ui.text, .035f) : config().ui.surfaceRaised;
            Ui.rounded(xPosition, yPosition, width, height, 5, color);
            String[] icons = {"grid", "palette", "chart", "grid", "spark", "world", "eye", "folder", "combat"};
            Ui.icon(icons[pageIndex], xPosition + 6, yPosition + (height - 10) / 2f, 10, selected ? Ui.accent() : config().ui.muted);
            fontRendererObj.drawString(fontRendererObj.trimStringToWidth(displayString, width - 29), xPosition + 23, yPosition + (height - 10) / 2, selected ? config().ui.text : config().ui.muted);
        }
    }
    private static final class Label {
        final String text; final int y, color;
        Label(String text, int y, int color) { this.text = text; this.y = y; this.color = color; }
    }
}
