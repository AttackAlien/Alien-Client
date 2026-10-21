package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Live local-player studio, including UUID-scoped overrides for other players. */
public final class CosmeticStudioScreen extends GuiScreen {
    static boolean drawingPreview;
    private final GuiScreen parent;
    private int page;
    private String target;
    private GuiTextField primary, secondary;
    private int left, top, panelWidth, rowStep, controlY, controlHeight;
    private String status = "";

    public CosmeticStudioScreen(GuiScreen parent) { this.parent = parent; }

    private CosmeticConfig config() { return AlienClient.get().config().cosmetics; }
    private CosmeticConfig.Accessory current() {
        if (target == null) return config().self;
        CosmeticConfig.Accessory accessory = config().players.get(target);
        if (accessory == null) {
            if (config().players.size() >= 128) { target = null; return config().self; }
            accessory = config().self.copy();
            config().players.put(target, accessory);
        }
        return accessory;
    }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear(); primary = secondary = null;
        panelWidth = Math.min(464, width - 16);
        left = (width - panelWidth) / 2; top = Math.max(6, (height - 298) / 2);
        int tabsY = top + 29;
        String[] tabs = {"Accessories", "Transform", "Inspector", "Imports"};
        for (int i = 0; i < tabs.length; i++) buttonList.add(new GuiButton(2 + i, left + 7 + i * (panelWidth - 14) / 4,
                tabsY, (panelWidth - 14) / 4 - 2, 20, tabs[i]));
        buttonList.add(new GuiButton(0, left + panelWidth - 73, top + 5, 66, 20, "Done"));
        controlHeight = height < 300 ? 16 : 20; rowStep = controlHeight + 3; controlY = top + 64;
        int x = left + 10, y = controlY, w = Math.min(212, panelWidth - 20);
        CosmeticConfig.Accessory a = current();
        if (page == 0) {
            buttonList.add(new GuiButton(10, x, y, w, controlHeight, "Hat: " + a.hat));
            buttonList.add(new GuiButton(11, x, y + rowStep, w, controlHeight, "Face: " + a.face));
            buttonList.add(new GuiButton(12, x, y + 2 * rowStep, w, controlHeight, "Back: " + a.back));
            buttonList.add(new GuiButton(13, x, y + 3 * rowStep, w, controlHeight, "Hand charm: " + on(a.charm)));
            buttonList.add(new GuiButton(14, x, y + 4 * rowStep, w / 2 - 2, controlHeight, "Aura: " + on(a.aura)));
            buttonList.add(new GuiButton(15, x + w / 2, y + 4 * rowStep, w / 2, controlHeight, "Trail: " + on(a.trail)));
            primary = new GuiTextField(30, fontRendererObj, x, y + 5 * rowStep + 13, w / 2 - 4, 18);
            secondary = new GuiTextField(31, fontRendererObj, x + w / 2, y + 5 * rowStep + 13, w / 2, 18);
            primary.setMaxStringLength(6); secondary.setMaxStringLength(6);
            primary.setText(String.format("%06X", a.color & 0xffffff));
            secondary.setText(String.format("%06X", a.secondaryColor & 0xffffff));
            buttonList.add(new GuiButton(16, x, y + 6 * rowStep + 18, w / 2 - 2, controlHeight, "Samurai preset"));
            buttonList.add(new GuiButton(17, x + w / 2, y + 6 * rowStep + 18, w / 2, controlHeight, "Celestial preset"));
        } else if (page == 1) {
            String[] labels = {"Position X", "Position Y", "Position Z", "Rotation X", "Rotation Y", "Rotation Z", "Scale"};
            for (int i = 0; i < 7; i++) buttonList.add(new Slider(50 + i, x, y + i * rowStep, w, labels[i], i));
            buttonList.add(new GuiButton(18, x, y + 7 * rowStep + 2, w, controlHeight, "Reset transform"));
        } else if (page == 2) {
            buttonList.add(new GuiButton(40, x, y, w, controlHeight, "Select next player"));
            buttonList.add(new GuiButton(41, x, y + rowStep + 3, w, controlHeight, "Accessory visibility: " + on(a.enabled)));
            buttonList.add(new GuiButton(42, x, y + 2 * (rowStep + 3), w, controlHeight, "Reset selected player"));
            buttonList.add(new GuiButton(43, x, y + 3 * (rowStep + 3), w, controlHeight, "Reset all UUID overrides"));
            buttonList.add(new GuiButton(44, x, y + 4 * (rowStep + 3), w, controlHeight, "Global cosmetics: " + on(config().enabled)));
            buttonList.add(new GuiButton(45, x, y + 5 * (rowStep + 3), w, controlHeight, "Copy self preset to selected"));
        } else {
            buttonList.add(new GuiButton(60, x, y, w, controlHeight, "Model: " + display(a.modelFile)));
            buttonList.add(new GuiButton(61, x, y + rowStep + 3, w, controlHeight, "Texture: " + display(a.textureFile)));
            buttonList.add(new GuiButton(62, x, y + 2 * (rowStep + 3), w, controlHeight, "Reload local assets"));
            buttonList.add(new GuiButton(63, x, y + 3 * (rowStep + 3), w, controlHeight, "Create example box model"));
            buttonList.add(new GuiButton(64, x, y + 4 * (rowStep + 3), w, controlHeight, "Clear imported model"));
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        applyColors();
        CosmeticConfig.Accessory a = current();
        if (button.id == 0) { AlienClient.get().save(); mc.displayGuiScreen(parent); return; }
        if (button.id >= 2 && button.id <= 5) page = button.id - 2;
        switch (button.id) {
            case 10: a.hat = cycle(a.hat, "NONE", "JAPANESE", "SAMURAI", "CROWN"); break;
            case 11: a.face = cycle(a.face, "NONE", "KITSUNE", "FUTURE", "GLASSES"); break;
            case 12: a.back = cycle(a.back, "NONE", "CAPE", "WINGS", "BACKPACK"); break;
            case 13: a.charm = !a.charm; break;
            case 14: a.aura = !a.aura; break;
            case 15: a.trail = !a.trail; break;
            case 16: a.hat = "SAMURAI"; a.face = "KITSUNE"; a.back = "CAPE"; a.color = 0x251F38; a.secondaryColor = 0xEF6487; break;
            case 17: a.hat = "CROWN"; a.face = "NONE"; a.back = "WINGS"; a.aura = true; a.color = 0xB79AFF; a.secondaryColor = 0x36DCC9; break;
            case 18: a.offsetX = a.offsetY = a.offsetZ = a.rotationX = a.rotationY = a.rotationZ = 0; a.scale = 1; break;
            case 40: nextPlayer(); break;
            case 41: a.enabled = !a.enabled; break;
            case 42: if (target == null) config().self = new CosmeticConfig.Accessory(); else { config().players.remove(target); target = null; } break;
            case 43: config().players.clear(); target = null; break;
            case 44: config().enabled = !config().enabled; break;
            case 45: if (target != null) config().players.put(target, config().self.copy()); break;
            case 60: a.modelFile = next(a.modelFile, CosmeticAssets.models()); break;
            case 61: a.textureFile = next(a.textureFile, CosmeticAssets.textures()); break;
            case 62: CosmeticAssets.reload(); status = "Local resource cache cleared."; break;
            case 63:
                try { CosmeticAssets.writeExample(); status = "Created example.json (head anchor)."; }
                catch (IOException e) { status = "Unable to create example: " + e.getMessage(); }
                break;
            case 64: a.modelFile = ""; a.textureFile = ""; break;
            default: if (button.id >= 50 && button.id <= 56) return;
        }
        AlienClient.get().save();
        initGui();
    }

    private void nextPlayer() {
        List<String> ids = new ArrayList<String>();
        ids.add(null);
        if (mc.theWorld != null) for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (p != mc.thePlayer) ids.add(p.getUniqueID().toString());
        }
        for (String id : config().players.keySet()) if (!ids.contains(id)) ids.add(id);
        int next = ids.indexOf(target) + 1;
        target = ids.get(next >= ids.size() ? 0 : next);
    }

    private EntityPlayer previewPlayer() {
        if (target != null && mc.theWorld != null) for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (target.equals(p.getUniqueID().toString())) return p;
        }
        return target == null ? mc.thePlayer : null;
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(left, top, left + panelWidth, Math.min(height - 4, top + 298), 0xEE11121D);
        drawRect(left, top, left + panelWidth, top + 2, 0xFF000000 | AlienClient.get().config().accentColor);
        fontRendererObj.drawStringWithShadow("ALIEN / Cosmetic Studio", left + 10, top + 11, 0xEEEAF9);
        String name = target == null ? "Your player" : target;
        EntityPlayer player = previewPlayer();
        if (player != null) name = player.getName();
        fontRendererObj.drawString("Editing: " + fontRendererObj.trimStringToWidth(name, panelWidth - 74), left + 10, top + 56, 0xB9B4CC);
        int previewX = left + panelWidth - Math.max(39, (panelWidth - 240) / 2);
        if (panelWidth > 320) {
            int previewY = Math.min(top + 230, height - 38);
            drawRect(left + 230, top + 72, left + panelWidth - 10, Math.min(top + 252, height - 24), 0x99202234);
            if (player != null) {
                int scale = Math.min(64, Math.max(28, (panelWidth - 240) / 3));
                drawingPreview = true;
                try {
                    GuiInventory.drawEntityOnScreen(previewX, previewY, scale,
                            previewX - mouseX, previewY - 90 - mouseY, player);
                } finally { drawingPreview = false; }
            } else {
                drawCenteredString(fontRendererObj, "Player offline", previewX, previewY - 72, 0xAAA5BD);
            }
            drawCenteredString(fontRendererObj, "Live local preview", previewX, previewY + 10, 0x8E89A6);
        }
        if (page == 0 && primary != null) {
            fontRendererObj.drawString("Primary / secondary RGB", left + 10, controlY + 5 * rowStep + 2, 0xA29AB8);
            primary.drawTextBox(); secondary.drawTextBox();
        }
        if (page == 2) {
            fontRendererObj.drawString("Overrides saved by UUID; local visuals only.", left + 10, Math.min(top + 267, height - 18), 0xA29AB8);
        }
        if (page == 3) {
            fontRendererObj.drawString("config/alien-client/cosmetics/", left + 10, controlY + 5 * (rowStep + 3) + 2, 0xA29AB8);
            fontRendererObj.drawString("JSON: 1..128 boxes. PNG: max 1024px / 1MiB.", left + 10, controlY + 5 * (rowStep + 3) + 15, 0xA29AB8);
            String message = CosmeticAssets.lastError.isEmpty() ? status : CosmeticAssets.lastError;
            fontRendererObj.drawString(fontRendererObj.trimStringToWidth(message, panelWidth - 20), left + 10, Math.min(top + 263, height - 18), 0xC2A9E9);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        if (primary != null) { primary.mouseClicked(x, y, button); secondary.mouseClicked(x, y, button); }
        super.mouseClicked(x, y, button);
    }

    @Override protected void keyTyped(char character, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) { applyColors(); AlienClient.get().save(); mc.displayGuiScreen(parent); return; }
        if (primary != null && (primary.isFocused() || secondary.isFocused())) {
            primary.textboxKeyTyped(character, key); secondary.textboxKeyTyped(character, key);
            applyColors(); return;
        }
        super.keyTyped(character, key);
    }

    private void applyColors() {
        if (primary == null) return;
        try { if (primary.getText().matches("[0-9a-fA-F]{6}")) current().color = Integer.parseInt(primary.getText(), 16); }
        catch (NumberFormatException ignored) { }
        try { if (secondary.getText().matches("[0-9a-fA-F]{6}")) current().secondaryColor = Integer.parseInt(secondary.getText(), 16); }
        catch (NumberFormatException ignored) { }
    }

    @Override public void onGuiClosed() { applyColors(); AlienClient.get().save(); Keyboard.enableRepeatEvents(false); }
    @Override public boolean doesGuiPauseGame() { return false; }
    private static String on(boolean value) { return value ? "ON" : "OFF"; }
    private static String display(String value) { return value == null || value.isEmpty() ? "none" : value; }
    private static String cycle(String value, String... values) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(value)) return values[(i + 1) % values.length];
        return values[0];
    }
    private static String next(String value, List<String> values) {
        int index = values.indexOf(value) + 1;
        return values.get(index >= values.size() ? 0 : index);
    }

    private final class Slider extends GuiButton {
        private final int field;
        private final String label;
        private boolean dragging;
        Slider(int id, int x, int y, int width, String label, int field) {
            super(id, x, y, width, controlHeight, ""); this.label = label; this.field = field; updateLabel();
        }
        private float min() { return field == 6 ? .25f : field < 3 ? -16 : -180; }
        private float max() { return field == 6 ? 3 : field < 3 ? 16 : 180; }
        private float value() {
            CosmeticConfig.Accessory a = current();
            switch (field) {
                case 0: return a.offsetX; case 1: return a.offsetY; case 2: return a.offsetZ;
                case 3: return a.rotationX; case 4: return a.rotationY; case 5: return a.rotationZ; default: return a.scale;
            }
        }
        private void updateLabel() { displayString = label + ": " + String.format(java.util.Locale.ROOT, "%.2f", value()); }
        private void set(int mouseX) {
            float value = min() + CosmeticConfig.clamp((mouseX - xPosition - 4f) / (width - 8f), 0, 1) * (max() - min());
            CosmeticConfig.Accessory a = current();
            switch (field) {
                case 0: a.offsetX = value; break; case 1: a.offsetY = value; break; case 2: a.offsetZ = value; break;
                case 3: a.rotationX = value; break; case 4: a.rotationY = value; break; case 5: a.rotationZ = value; break; default: a.scale = value;
            }
            updateLabel();
        }
        @Override public boolean mousePressed(net.minecraft.client.Minecraft mc, int mouseX, int mouseY) {
            if (!super.mousePressed(mc, mouseX, mouseY)) return false;
            dragging = true; set(mouseX); return true;
        }
        @Override protected void mouseDragged(net.minecraft.client.Minecraft mc, int mouseX, int mouseY) {
            if (dragging) set(mouseX);
            int handle = xPosition + 4 + (int)((value() - min()) / (max() - min()) * (width - 8));
            drawRect(handle - 2, yPosition + height - 3, handle + 2, yPosition + height - 1, 0xFFB79AFF);
        }
        @Override public void mouseReleased(int x, int y) { if (dragging) AlienClient.get().save(); dragging = false; }
        @Override protected int getHoverState(boolean hovered) { return 0; }
    }
}
