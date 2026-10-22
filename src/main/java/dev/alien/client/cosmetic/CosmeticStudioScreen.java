package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** A live fitting room: every control edits the selected local cosmetic preset. */
public final class CosmeticStudioScreen extends GuiScreen {
    static boolean drawingPreview;
    private final GuiScreen parent;
    private int page;
    private String target;
    private int left, top, panelWidth, controlX, controlWidth, bodyTop, bodyBottom;
    private int previewX, previewWidth, rowStep, buttonHeight;
    private String status = "Drag the preview to turn. Scroll to zoom.";
    private long statusUntil;
    private GuiButton activeButton;
    private boolean draggingPreview, turntable;
    private int dragX, dragY;
    private float viewYaw = -24, viewPitch = -7, zoom = 1;
    private long previousFrame = System.nanoTime();
    // LWJGL 2's glGetInteger wrapper requires room for 16 ints even for the
    // four-component GL_SCISSOR_BOX query. Only elements 0..3 are restored.
    private final IntBuffer savedScissor = BufferUtils.createIntBuffer(16);

    public CosmeticStudioScreen(GuiScreen parent) { this.parent = parent; }
    /** View-only controls for accessibility and automated real-render checks. */
    public void setPreviewYaw(float degrees) {
        if (!Float.isNaN(degrees) && !Float.isInfinite(degrees)) viewYaw = degrees % 360f;
        turntable = false;
    }
    public void setPreviewZoom(float value) {
        if (!Float.isNaN(value) && !Float.isInfinite(value)) zoom = Math.max(.65f, Math.min(1.65f, value));
    }
    public float previewYaw() { return viewYaw; }
    public float previewZoom() { return zoom; }
    public int[] previewCanvasBounds() { return new int[] {previewX, bodyTop + 20, previewWidth, bodyBottom - bodyTop - 40}; }
    private CosmeticConfig config() { return AlienClient.get().config().cosmetics; }

    private CosmeticConfig.Accessory current() {
        if (target == null) return config().self;
        CosmeticConfig.Accessory accessory = config().players.get(target);
        if (accessory == null) {
            if (config().players.size() >= 128) { target = null; return config().self; }
            accessory = config().self.copy(); config().players.put(target, accessory);
        }
        return accessory;
    }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear(); activeButton = null; draggingPreview = false;
        panelWidth = Math.min(680, width - 16); left = (width - panelWidth) / 2; top = 6;
        controlX = left + 10;
        controlWidth = Math.max(145, (panelWidth - 34) / 2);
        previewX = controlX + controlWidth + 12;
        previewWidth = left + panelWidth - 10 - previewX;
        int tabY = top + 33;
        String[] tabs = {"Style", "Fit", "Players", "Imports", "Motion", "Looks"};
        int tabSpace = (panelWidth - 20) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            buttonList.add(new StudioButton(2 + i, controlX + i * tabSpace, tabY,
                    tabSpace - 3, 18, tabs[i]).selected(page == i));
        }
        buttonList.add(new StudioButton(0, left + panelWidth - 59, top + 7, 49, 18, "Done"));
        bodyTop = tabY + 24; bodyBottom = height - 31;
        rowStep = Math.min(24, Math.max(18, (bodyBottom - bodyTop - 1) / 7)); buttonHeight = rowStep - 3;
        CosmeticConfig.Accessory a = current();
        if (page == 0) {
            row(10, 0, "Hat: " + title(a.hat));
            row(11, 1, "Face: " + title(a.face));
            row(12, 2, "Back: " + title(a.back));
            row(13, 3, "Hand charm: " + on(a.charm));
            pair(14, "Aura: " + on(a.aura), 15, "Trail: " + on(a.trail), 4);
            pair(70, "Main color", 71, "Trim color", 5);
            pair(16, "Ronin", 17, "Celestial", 6);
        } else if (page == 1) {
            int step = Math.min(22, Math.max(15, (bodyBottom - bodyTop - 1) / 8));
            String[] labels = {"Position X", "Position Y", "Position Z", "Rotation X", "Rotation Y", "Rotation Z", "Scale"};
            for (int i = 0; i < labels.length; i++) buttonList.add(new StudioSlider(50 + i, controlX,
                    bodyTop + i * step, controlWidth, step - 3, labels[i], i));
            buttonList.add(new StudioButton(18, controlX, bodyTop + 7 * step, controlWidth, step - 3, "Reset fit"));
        } else if (page == 2) {
            row(40, 0, "Choose player  >"); row(41, 1, "Visible: " + on(a.enabled));
            row(42, 2, "Reset selected player"); row(43, 3, "Reset player overrides");
            row(44, 4, "All cosmetics: " + on(config().enabled)); row(45, 5, "Copy your look here");
        } else if (page == 3) {
            row(60, 0, "Model: " + display(a.modelFile)); row(61, 1, "Texture: " + display(a.textureFile));
            row(62, 2, "Reload local assets"); row(63, 3, "Create model example"); row(64, 4, "Clear imported model");
        } else if (page == 4) {
            row(80, 0, "Animation: " + on(a.animations));
            buttonList.add(new StudioSlider(81, controlX, bodyTop + rowStep, controlWidth, buttonHeight, "Cape flutter", 7));
            buttonList.add(new StudioSlider(82, controlX, bodyTop + rowStep * 2, controlWidth, buttonHeight, "Cape length", 8));
            buttonList.add(new StudioSlider(83, controlX, bodyTop + rowStep * 3, controlWidth, buttonHeight, "Hat size", 9));
            row(84, 4, "Detail: " + a.detailLevel); row(86, 5, "Emblem: " + title(a.capePattern));
            row(85, 6, "Reset motion & size");
        } else {
            pair(110, "Ronin", 111, "Celestial", 0);
            pair(112, "Sakura", 113, "Phantom", 1);
            for (int slot = 1; slot <= 3; slot++) {
                pair(120 + (slot - 1) * 2, "Save look " + slot,
                        121 + (slot - 1) * 2,
                        (config().savedLooks != null && config().savedLooks.containsKey(Integer.toString(slot))
                         ? "Load " : "Empty ") + slot, slot + 1);
            }
        }
        buttonList.add(new StudioButton(90, previewX + previewWidth - 25, bodyTop + 6, 19, 16, "R"));
        buttonList.add(new StudioButton(91, previewX + 7, bodyBottom - 17, previewWidth - 14, 14,
                "Turntable: " + on(turntable)));
    }

    private void row(int id, int row, String text) {
        buttonList.add(new StudioButton(id, controlX, bodyTop + rowStep * row, controlWidth, buttonHeight, text));
    }
    private void pair(int id, String label, int otherId, String otherLabel, int row) {
        int half = (controlWidth - 5) / 2;
        buttonList.add(new StudioButton(id, controlX, bodyTop + rowStep * row, half, buttonHeight, label));
        buttonList.add(new StudioButton(otherId, controlX + half + 5, bodyTop + rowStep * row,
                controlWidth - half - 5, buttonHeight, otherLabel));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        CosmeticConfig.Accessory a = current();
        if (button.id == 0) { AlienClient.get().save(); mc.displayGuiScreen(parent); return; }
        if (button.id >= 2 && button.id <= 7) { page = button.id - 2; initGui(); return; }
        switch (button.id) {
            case 10: a.hat = cycle(a.hat, "NONE", "JAPANESE", "SAMURAI", "CROWN"); break;
            case 11: a.face = cycle(a.face, "NONE", "KITSUNE", "FUTURE", "GLASSES"); break;
            case 12: a.back = cycle(a.back, "NONE", "CAPE", "WINGS", "BACKPACK"); break;
            case 13: a.charm = !a.charm; break;
            case 14: a.aura = !a.aura; break;
            case 15: a.trail = !a.trail; break;
            case 16: case 110: CosmeticLooks.apply(a, "Ronin"); break;
            case 17: case 111: CosmeticLooks.apply(a, "Celestial"); break;
            case 112: CosmeticLooks.apply(a, "Sakura"); break;
            case 113: CosmeticLooks.apply(a, "Phantom"); break;
            case 18: a.offsetX = a.offsetY = a.offsetZ = a.rotationX = a.rotationY = a.rotationZ = 0; a.scale = 1; break;
            case 40: nextPlayer(); break;
            case 41: a.enabled = !a.enabled; break;
            case 42: if (target == null) config().self = new CosmeticConfig.Accessory(); else { config().players.remove(target); target = null; } break;
            case 43: config().players.clear(); target = null; break;
            case 44: config().enabled = !config().enabled; break;
            case 45: if (target != null) config().players.put(target, config().self.copy()); break;
            case 60: a.modelFile = next(a.modelFile, CosmeticAssets.models()); break;
            case 61: a.textureFile = next(a.textureFile, CosmeticAssets.textures()); break;
            case 62: CosmeticAssets.reload(); message("Local models and textures reloaded."); break;
            case 63:
                try { CosmeticAssets.writeExample(); message("Created example.json in your cosmetics folder."); }
                catch (IOException e) { message("Unable to create example: " + e.getMessage()); }
                break;
            case 64: a.modelFile = ""; a.textureFile = ""; break;
            case 70: mc.displayGuiScreen(new ColorPicker(this, a, false)); return;
            case 71: mc.displayGuiScreen(new ColorPicker(this, a, true)); return;
            case 80: a.animations = !a.animations; break;
            case 84: a.detailLevel = cycle(a.detailLevel, "Low", "Balanced", "High"); break;
            case 85: a.capeFlutter = a.capeLength = a.hatSize = 1; a.animations = true; a.detailLevel = "Balanced"; break;
            case 86: a.capePattern = cycle(a.capePattern, "ALIEN", "MOON", "STARS", "PLAIN"); break;
            case 90: viewYaw = -24; viewPitch = -7; zoom = 1; message("Preview reset. Drag to turn; scroll to zoom."); break;
            case 91: turntable = !turntable; break;
            case 120: case 122: case 124: {
                int slot = (button.id - 120) / 2 + 1;
                config().saveLook(slot, a);
                message("Saved current look in slot " + slot + " (active profile).");
                break;
            }
            case 121: case 123: case 125: {
                int slot = (button.id - 121) / 2 + 1;
                CosmeticConfig.Accessory look = config().loadLook(slot);
                if (look == null) { message("Look slot " + slot + " is empty."); break; }
                if (target == null) config().self = look;
                else config().players.put(target, look);
                message("Loaded saved look " + slot + ".");
                break;
            }
            default: return;
        }
        AlienClient.get().save(); initGui();
    }

    private void nextPlayer() {
        List<String> ids = new ArrayList<String>(); ids.add(null);
        if (mc.theWorld != null) for (EntityPlayer player : mc.theWorld.playerEntities)
            if (player != mc.thePlayer) ids.add(player.getUniqueID().toString());
        for (String id : config().players.keySet()) if (!ids.contains(id)) ids.add(id);
        int index = ids.indexOf(target) + 1; target = ids.get(index >= ids.size() ? 0 : index);
    }
    private EntityPlayer previewPlayer() {
        if (target != null && mc.theWorld != null) for (EntityPlayer player : mc.theWorld.playerEntities)
            if (target.equals(player.getUniqueID().toString())) return player;
        return target == null ? mc.thePlayer : null;
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawGradientRect(left, top, left + panelWidth, height - 6, 0xf21a1728, 0xf20e111b);
        drawRect(left, top, left + panelWidth, top + 2, Ui.accent());
        drawRect(left + 10, top + 10, left + 12, top + 27, Ui.accent());
        fontRendererObj.drawStringWithShadow("ALIEN / COSMETIC STUDIO", left + 19, top + 10, Ui.TEXT);
        EntityPlayer player = previewPlayer();
        String name = player != null ? player.getName() : target == null ? "Join a world for your live preview" : "Offline player";
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth("Editing: " + name, panelWidth - 90), left + 19, top + 22, Ui.MUTED);
        drawGradientRect(previewX, bodyTop, previewX + previewWidth, bodyBottom, 0xff252037, 0xff121724);
        drawRect(previewX, bodyTop, previewX + previewWidth, bodyTop + 1, 0xff54416c);
        drawRect(previewX, bodyTop, previewX + 1, bodyBottom, 0xff3b304f);
        fontRendererObj.drawString("LIVE PREVIEW", previewX + 8, bodyTop + 9, 0xffb2a4d0);
        int floorY = bodyBottom - 26;
        drawRect(previewX + 12, floorY, previewX + previewWidth - 12, floorY + 1, 0xff42375c);
        drawRect(previewX + previewWidth / 2 - 20, floorY + 2, previewX + previewWidth / 2 + 20, floorY + 3, 0xff776291);
        long now = System.nanoTime();
        float elapsed = Math.min(.1f, (now - previousFrame) / 1_000_000_000f); previousFrame = now;
        if (turntable && !draggingPreview) viewYaw = (viewYaw + elapsed * 18f) % 360f;
        if (player != null) drawPlayer(player);
        else {
            drawCenteredString(fontRendererObj, "No player loaded", previewX + previewWidth / 2, bodyTop + (bodyBottom - bodyTop) / 2, Ui.MUTED);
        }
        if (page == 3) {
            fontRendererObj.drawString(fontRendererObj.trimStringToWidth("Local folder: config/" + dev.alien.client.edition.BuildEdition.CONFIG_DIRECTORY + "/cosmetics", controlWidth), controlX, bodyTop + rowStep * 5 + 2, Ui.MUTED);
            String issue = CosmeticAssets.lastError;
            if (!issue.isEmpty()) fontRendererObj.drawString(fontRendererObj.trimStringToWidth(issue, controlWidth), controlX, bodyTop + rowStep * 6, 0xffffaaad);
        }
        for (GuiButton button : buttonList) button.drawButton(mc, mouseX, mouseY);
        if (page == 0) {
            swatch(70, current().color); swatch(71, current().secondaryColor);
        }
        String footer = System.currentTimeMillis() < statusUntil ? status : "Drag preview to rotate  /  Scroll to zoom";
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(footer, panelWidth - 94), left + 10, height - 20, Ui.MUTED);
        fontRendererObj.drawString("LOCAL ONLY", left + panelWidth - 71, height - 20, 0xff8d7fa5);
    }

    private void swatch(int id, int color) {
        for (GuiButton button : buttonList) if (button.id == id) {
            drawRect(button.xPosition + button.width - 13, button.yPosition + 4,
                    button.xPosition + button.width - 5, button.yPosition + button.height - 4, color | 0xff000000);
        }
    }

    /** Render a real entity, restoring every temporary orientation and render-manager value. */
    private void drawPlayer(EntityPlayer player) {
        float body = player.renderYawOffset, yaw = player.rotationYaw, pitch = player.rotationPitch;
        float head = player.rotationYawHead, previousHead = player.prevRotationYawHead;
        RenderManager manager = mc.getRenderManager();
        float oldViewY = manager.playerViewY, oldViewX = manager.playerViewX;
        boolean shadow = manager.isRenderShadow(), debug = manager.isDebugBoundingBox();
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        savedScissor.clear(); GL11.glGetInteger(GL11.GL_SCISSOR_BOX, savedScissor);
        ScaledResolution resolution = new ScaledResolution(mc); int factor = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(previewX * factor, (height - bodyBottom + 20) * factor,
                previewWidth * factor, Math.max(1, bodyBottom - bodyTop - 40) * factor);
        drawingPreview = true;
        GlStateManager.pushMatrix();
        try {
            float scale = Math.min(100, (bodyBottom - bodyTop - 51) / 2.4f) * zoom;
            GlStateManager.translate(previewX + previewWidth / 2f, bodyBottom - 24f, 100);
            GlStateManager.scale(-scale, scale, scale);
            GlStateManager.rotate(180, 0, 0, 1);
            GlStateManager.rotate(viewPitch, 1, 0, 0);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.rotate(viewYaw, 0, 1, 0);
            GlStateManager.enableDepth(); GlStateManager.enableColorMaterial();
            player.renderYawOffset = player.rotationYaw = player.rotationYawHead = player.prevRotationYawHead = 0;
            player.rotationPitch = 0;
            manager.setPlayerViewY(180); manager.playerViewX = 0;
            manager.setRenderShadow(false); manager.setDebugBoundingBox(false);
            manager.renderEntityWithPosYaw(player, 0, 0, 0, 0, 1);
        } finally {
            player.renderYawOffset = body; player.rotationYaw = yaw; player.rotationPitch = pitch;
            player.rotationYawHead = head; player.prevRotationYawHead = previousHead;
            manager.setPlayerViewY(oldViewY); manager.playerViewX = oldViewX;
            manager.setRenderShadow(shadow); manager.setDebugBoundingBox(debug);
            GlStateManager.popMatrix(); drawingPreview = false;
            RenderHelper.disableStandardItemLighting(); GlStateManager.disableLighting(); GlStateManager.disableColorMaterial();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit); GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit); GlStateManager.enableTexture2D();
            GlStateManager.color(1, 1, 1, 1); GlStateManager.enableBlend();
            if (depth) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
            GL11.glScissor(savedScissor.get(0), savedScissor.get(1), savedScissor.get(2), savedScissor.get(3));
            if (!scissor) GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        if (button != 0) return;
        for (GuiButton control : buttonList) if (control.mousePressed(mc, x, y)) {
            activeButton = control; control.playPressSound(mc.getSoundHandler());
            if (!(control instanceof StudioSlider)) actionPerformed(control);
            return;
        }
        if (inPreview(x, y)) { draggingPreview = true; dragX = x; dragY = y; turntable = false; }
    }
    @Override protected void mouseClickMove(int x, int y, int button, long elapsed) {
        if (button == 0 && draggingPreview) {
            viewYaw += (x - dragX) * 1.5f; viewPitch = Math.max(-28, Math.min(28, viewPitch + (y - dragY) * .55f));
            dragX = x; dragY = y;
        }
    }
    @Override protected void mouseReleased(int x, int y, int button) {
        draggingPreview = false;
        if (activeButton != null) { activeButton.mouseReleased(x, y); activeButton = null; }
    }
    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput(); int wheel = Mouse.getEventDWheel();
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (wheel != 0 && inPreview(x, y)) zoom = Math.max(.65f, Math.min(1.65f, zoom + (wheel > 0 ? .08f : -.08f)));
    }
    private boolean inPreview(int x, int y) { return x >= previewX && x < previewX + previewWidth && y >= bodyTop && y < bodyBottom; }
    @Override protected void keyTyped(char character, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) { AlienClient.get().save(); mc.displayGuiScreen(parent); return; }
        super.keyTyped(character, key);
    }
    @Override public void onGuiClosed() { draggingPreview = false; Keyboard.enableRepeatEvents(false); AlienClient.get().save(); }
    @Override public boolean doesGuiPauseGame() { return false; }
    private void message(String text) { status = text; statusUntil = System.currentTimeMillis() + 5000; }
    private static String on(boolean value) { return value ? "ON" : "OFF"; }
    private static String display(String value) { return value == null || value.isEmpty() ? "None" : value; }
    private static String title(String value) {
        if ("JAPANESE".equals(value)) return "Conical kasa";
        if ("SAMURAI".equals(value)) return "Kabuto";
        if ("KITSUNE".equals(value)) return "Kitsune";
        if ("FUTURE".equals(value)) return "Visor";
        return value == null || value.isEmpty() ? "None" : value.substring(0, 1) + value.substring(1).toLowerCase(Locale.ROOT);
    }
    private static String cycle(String value, String... values) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(value)) return values[(i + 1) % values.length];
        return values[0];
    }
    private static String next(String value, List<String> values) {
        int index = values.indexOf(value) + 1; return values.get(index >= values.size() ? 0 : index);
    }

    private static final class StudioButton extends GuiButton {
        private boolean selected;
        StudioButton(int id, int x, int y, int w, int h, String text) { super(id, x, y, w, h, text); }
        StudioButton selected(boolean selected) { this.selected = selected; return this; }
        @Override public void drawButton(Minecraft mc, int x, int y) {
            if (!visible) return;
            hovered = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
            int fill = selected ? 0xff49375f : hovered && enabled ? 0xff373146 : 0xff272633;
            Ui.panel(xPosition, yPosition, width, height, fill);
            if (selected) drawRect(xPosition, yPosition + 3, xPosition + 2, yPosition + height - 3, Ui.accent());
            String text = mc.fontRendererObj.trimStringToWidth(displayString, width - ((id == 70 || id == 71) ? 22 : 12));
            mc.fontRendererObj.drawStringWithShadow(text, xPosition + 6, yPosition + (height - 8) / 2f, enabled ? Ui.TEXT : Ui.MUTED);
            mouseDragged(mc, x, y);
        }
        @Override protected int getHoverState(boolean hovered) { return 0; }
    }

    private final class StudioSlider extends GuiButton {
        private final String label;
        private final int field;
        private boolean dragging;
        StudioSlider(int id, int x, int y, int width, int height, String label, int field) {
            super(id, x, y, width, height, ""); this.label = label; this.field = field;
        }
        private float min() { return field == 6 ? .25f : field == 7 ? 0 : field == 8 ? .65f : field == 9 ? .75f : field < 3 ? -16 : -180; }
        private float max() { return field == 6 ? 3 : field == 7 ? 2 : field == 8 || field == 9 ? 1.35f : field < 3 ? 16 : 180; }
        private float value() {
            CosmeticConfig.Accessory a = current();
            switch (field) {
                case 0: return a.offsetX; case 1: return a.offsetY; case 2: return a.offsetZ;
                case 3: return a.rotationX; case 4: return a.rotationY; case 5: return a.rotationZ;
                case 6: return a.scale; case 7: return a.capeFlutter; case 8: return a.capeLength; default: return a.hatSize;
            }
        }
        private void update(int x) {
            float value = min() + Math.max(0, Math.min(1, (x - xPosition - 4f) / Math.max(1, width - 8f))) * (max() - min());
            CosmeticConfig.Accessory a = current();
            switch (field) {
                case 0: a.offsetX = value; break; case 1: a.offsetY = value; break; case 2: a.offsetZ = value; break;
                case 3: a.rotationX = value; break; case 4: a.rotationY = value; break; case 5: a.rotationZ = value; break;
                case 6: a.scale = value; break; case 7: a.capeFlutter = value; break; case 8: a.capeLength = value; break; default: a.hatSize = value;
            }
        }
        @Override public void drawButton(Minecraft mc, int x, int y) {
            if (dragging) update(x);
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0xff252431);
            int progress = Math.round((value() - min()) / (max() - min()) * width);
            drawRect(xPosition, yPosition + height - 2, xPosition + progress, yPosition + height, Ui.accent());
            String right = String.format(Locale.ROOT, "%.2f", value());
            mc.fontRendererObj.drawStringWithShadow(label, xPosition + 5, yPosition + (height - 8) / 2f, Ui.MUTED);
            mc.fontRendererObj.drawStringWithShadow(right, xPosition + width - mc.fontRendererObj.getStringWidth(right) - 5,
                    yPosition + (height - 8) / 2f, Ui.TEXT);
        }
        @Override public boolean mousePressed(Minecraft mc, int x, int y) {
            if (!super.mousePressed(mc, x, y)) return false; dragging = true; update(x); return true;
        }
        @Override public void mouseReleased(int x, int y) { if (dragging) AlienClient.get().save(); dragging = false; }
    }

    /** Color control with a live RGB preview, swatches and a portable hex value. */
    private static final class ColorPicker extends GuiScreen {
        private final CosmeticStudioScreen parent;
        private final CosmeticConfig.Accessory accessory;
        private final boolean trim;
        private int x, y, w;
        private GuiTextField hex;
        private GuiButton active;
        ColorPicker(CosmeticStudioScreen parent, CosmeticConfig.Accessory accessory, boolean trim) {
            this.parent = parent; this.accessory = accessory; this.trim = trim;
        }
        private int color() { return (trim ? accessory.secondaryColor : accessory.color) & 0xffffff; }
        private void color(int rgb) { if (trim) accessory.secondaryColor = rgb; else accessory.color = rgb; }
        @Override public void initGui() {
            Keyboard.enableRepeatEvents(true); buttonList.clear();
            w = Math.min(280, width - 24); x = (width - w) / 2; y = Math.max(8, (height - 194) / 2);
            for (int channel = 0; channel < 3; channel++) buttonList.add(new ColorSlider(10 + channel, x + 12, y + 52 + channel * 25, w - 24, channel));
            int[] palette = {0xB79AFF, 0x74E0D4, 0xE5B979, 0xF5E9DC, 0xC94C69, 0x352738, 0x4F91B5, 0xE8EDF7};
            int cell = (w - 24) / palette.length;
            for (int i = 0; i < palette.length; i++) buttonList.add(new ColorSwatch(30 + i, x + 12 + i * cell, y + 132, cell - 3, palette[i]));
            hex = new GuiTextField(2, fontRendererObj, x + 12, y + 159, w - 111, 20); hex.setMaxStringLength(6); syncHex();
            buttonList.add(new StudioButton(0, x + w - 89, y + 159, 77, 20, "Done"));
        }
        private void syncHex() { if (hex != null) hex.setText(String.format(Locale.ROOT, "%06X", color())); }
        @Override public void drawScreen(int mx, int my, float ticks) {
            parent.drawScreen(-100, -100, ticks); drawRect(0, 0, width, height, 0xb8000000);
            drawGradientRect(x, y, x + w, y + 194, 0xff242032, 0xff141722);
            drawRect(x, y, x + w, y + 2, Ui.accent());
            fontRendererObj.drawStringWithShadow(trim ? "TRIM / RGB COLOR" : "MAIN / RGB COLOR", x + 12, y + 13, Ui.TEXT);
            drawRect(x + 12, y + 31, x + w - 12, y + 43, color() | 0xff000000);
            hex.drawTextBox(); for (GuiButton button : buttonList) button.drawButton(mc, mx, my);
        }
        @Override protected void actionPerformed(GuiButton button) {
            if (button.id == 0) { applyHex(); AlienClient.get().save(); mc.displayGuiScreen(parent); }
            else if (button instanceof ColorSwatch) { color(((ColorSwatch)button).rgb); syncHex(); AlienClient.get().save(); }
        }
        private void applyHex() { if (hex.getText().matches("[0-9A-Fa-f]{6}")) color(Integer.parseInt(hex.getText(), 16)); }
        @Override protected void mouseClicked(int mx, int my, int button) throws IOException {
            hex.mouseClicked(mx, my, button);
            if (button == 0) for (GuiButton control : buttonList) if (control.mousePressed(mc, mx, my)) {
                active = control; control.playPressSound(mc.getSoundHandler());
                if (!(control instanceof ColorSlider)) actionPerformed(control); return;
            }
        }
        @Override protected void mouseReleased(int mx, int my, int button) { if (active != null) { active.mouseReleased(mx, my); active = null; } }
        @Override protected void keyTyped(char character, int key) throws IOException {
            if (key == Keyboard.KEY_ESCAPE) { applyHex(); AlienClient.get().save(); mc.displayGuiScreen(parent); return; }
            if (hex.textboxKeyTyped(character, key)) { applyHex(); return; }
            super.keyTyped(character, key);
        }
        @Override public void updateScreen() { hex.updateCursorCounter(); }
        @Override public void onGuiClosed() { applyHex(); AlienClient.get().save(); Keyboard.enableRepeatEvents(false); }
        @Override public boolean doesGuiPauseGame() { return false; }
        private final class ColorSlider extends GuiButton {
            final int channel; boolean dragging;
            ColorSlider(int id, int x, int y, int width, int channel) { super(id, x, y, width, 20, ""); this.channel = channel; }
            int shift() { return 16 - channel * 8; }
            void update(int mx) {
                int next = Math.max(0, Math.min(255, Math.round((mx - xPosition) / (float)width * 255)));
                color((color() & ~(255 << shift())) | next << shift()); syncHex();
            }
            @Override public void drawButton(Minecraft mc, int mx, int my) {
                if (dragging) update(mx);
                int value = (color() >> shift()) & 255;
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0xff302c40);
                drawRect(xPosition, yPosition + height - 3, xPosition + width * value / 255, yPosition + height, (255 << shift()) | 0xff000000);
                mc.fontRendererObj.drawStringWithShadow(new String[]{"Red", "Green", "Blue"}[channel], xPosition + 6, yPosition + 6, Ui.MUTED);
                String text = Integer.toString(value); mc.fontRendererObj.drawStringWithShadow(text, xPosition + width - 6 - mc.fontRendererObj.getStringWidth(text), yPosition + 6, Ui.TEXT);
            }
            @Override public boolean mousePressed(Minecraft mc, int mx, int my) { if (!super.mousePressed(mc, mx, my)) return false; dragging = true; update(mx); return true; }
            @Override public void mouseReleased(int mx, int my) { dragging = false; AlienClient.get().save(); }
        }
        private static final class ColorSwatch extends GuiButton {
            final int rgb;
            ColorSwatch(int id, int x, int y, int width, int rgb) { super(id, x, y, width, 17, ""); this.rgb = rgb; }
            @Override public void drawButton(Minecraft mc, int x, int y) {
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0xff62596f);
                drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, rgb | 0xff000000);
            }
        }
    }
}
