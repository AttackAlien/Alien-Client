package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import dev.alien.client.gui.ui.ColorPickerScreen;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;
import dev.alien.client.cosmetic.mesh.Mesh;
import dev.alien.client.cosmetic.mesh.MeshView;
import dev.alien.client.cosmetic.mesh.CosmeticMeshes;
import dev.alien.client.cosmetic.mesh.CapeCloth;
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
public final class CosmeticStudioScreen extends dev.alien.client.gui.PremiumScreen {
    static boolean drawingPreview;
    private final GuiScreen parent;
    private int page;
    private String target;
    private int left, top, panelWidth, controlX, controlWidth, bodyTop, bodyBottom;
    private int previewX, previewWidth, rowStep, buttonHeight, controlTop, railX, railWidth;
    private String status = "Drag the preview to turn. Scroll to zoom.";
    private long statusUntil;
    private GuiButton activeButton;
    private boolean draggingPreview, turntable;
    private int dragX, dragY;
    private float viewYaw = -24, viewPitch = -7, zoom = 1;
    private float shownYaw = -24, shownPitch = -7, shownZoom = 1;
    private final FloatBuffer previewLight = BufferUtils.createFloatBuffer(16);
    private final CapeCloth thumbnailCape = new CapeCloth(8, 4);
    private long previousFrame = System.nanoTime();
    // LWJGL 2's glGetInteger wrapper requires room for 16 ints even for the
    // four-component GL_SCISSOR_BOX query. Only elements 0..3 are restored.
    private final IntBuffer savedScissor = BufferUtils.createIntBuffer(16);

    public CosmeticStudioScreen(GuiScreen parent) { this.parent = parent; }
    /** View-only controls for accessibility and automated real-render checks. */
    public void setPreviewYaw(float degrees) {
        if (!Float.isNaN(degrees) && !Float.isInfinite(degrees)) shownYaw = viewYaw = degrees % 360f;
        turntable = false;
    }
    public void setPreviewZoom(float value) {
        if (!Float.isNaN(value) && !Float.isInfinite(value)) shownZoom = zoom = Math.max(.65f, Math.min(1.65f, value));
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
        fontRendererObj = Ui.font();
        Keyboard.enableRepeatEvents(true);
        buttonList.clear(); activeButton = null; draggingPreview = false;
        panelWidth = Math.min(980, width - 16); left = (width - panelWidth) / 2; top = 8;
        railX = left + 8; railWidth = width < 370 ? 35 : 42;
        controlX = railX + railWidth + 8;
        controlWidth = Math.max(104, Math.min(232, Math.round((panelWidth - railWidth - 42) * .355f)));
        previewX = controlX + controlWidth + 10;
        previewWidth = left + panelWidth - 8 - previewX;
        bodyTop = top + 35; bodyBottom = height - 27; controlTop = bodyTop + 13;
        rowStep = Math.min(24, Math.max(18, (bodyBottom - controlTop - 2) / 7)); buttonHeight = rowStep - 3;
        String[] tabs = {"Style", "Fit", "Players", "Assets", "Motion"};
        int navStep = Math.min(34, (bodyBottom - bodyTop - 15) / 5);
        for (int i = 0; i < tabs.length; i++) buttonList.add(new StudioButton(2 + i,
                railX, bodyTop + 8 + i * navStep, railWidth, navStep - 4, tabs[i]).selected(page == i));
        buttonList.add(new StudioButton(0, left + panelWidth - 56, top + 8, 45, 18, "Done"));
        CosmeticConfig.Accessory a = current();
        if (page == 0) {
            int cardStep = rowStep + 5;
            buttonList.add(new AccessoryCard(10, controlTop, cardStep - 3, "HEADWEAR", title(a.hat)));
            buttonList.add(new AccessoryCard(11, controlTop + cardStep, cardStep - 3, "FACE", title(a.face)));
            buttonList.add(new AccessoryCard(12, controlTop + cardStep * 2, cardStep - 3, "BACK", title(a.back)));
            int y = controlTop + cardStep * 3, step = rowStep - 4;
            addAt(13, controlX, y, controlWidth, step - 1, "Charm: " + on(a.charm));
            pairAt(14, "Aura: " + on(a.aura), 15, "Trail: " + on(a.trail), y + step, step - 1);
            pairAt(70, "Fabric", 71, "Accent", y + step * 2, step - 1);
            pairAt(16, "Ronin", 17, "Celestial", y + step * 3, step - 1);
        } else if (page == 1) {
            int step = Math.min(25, Math.max(16, (bodyBottom - controlTop - 2) / 8));
            String[] labels = {"Position X", "Position Y", "Position Z", "Rotation X", "Rotation Y", "Rotation Z", "Scale"};
            for (int i = 0; i < labels.length; i++) buttonList.add(new StudioSlider(50 + i, controlX,
                    controlTop + i * step, controlWidth, step - 3, labels[i], i));
            addAt(18, controlX, controlTop + 7 * step, controlWidth, step - 3, "Reset transform");
        } else if (page == 2) {
            row(40, 0, "Select player"); row(41, 1, "Visible: " + on(a.enabled));
            row(42, 2, "Reset selected"); row(43, 3, "Reset overrides");
            row(44, 4, "Global: " + on(config().enabled)); row(45, 5, "Copy your look");
        } else if (page == 3) {
            row(60, 0, "Model: " + display(a.modelFile)); row(61, 1, "Texture: " + display(a.textureFile));
            row(62, 2, "Reload library"); row(63, 3, "Create example"); row(64, 4, "Clear imported model");
        } else {
            row(80, 0, "Animate: " + on(a.animations));
            buttonList.add(new StudioSlider(81, controlX, controlTop + rowStep, controlWidth, buttonHeight, "Cape flutter", 7));
            buttonList.add(new StudioSlider(82, controlX, controlTop + rowStep * 2, controlWidth, buttonHeight, "Cape length", 8));
            buttonList.add(new StudioSlider(83, controlX, controlTop + rowStep * 3, controlWidth, buttonHeight, "Hat size", 9));
            row(84, 4, "Mesh / " + a.detailLevel); row(86, 5, "Emblem / " + title(a.capePattern));
            row(85, 6, "Reset motion");
        }
        buttonList.add(new StudioButton(90, previewX + previewWidth - 24, bodyTop + 5, 18, 15, "R"));
        buttonList.add(new StudioButton(91, previewX + 7, bodyBottom - 20, Math.max(37, previewWidth - 64), 15,
                "Orbit: " + on(turntable)));
        buttonList.add(new StudioButton(102, previewX + previewWidth - 50, bodyBottom - 20, 20, 15, "L"));
        buttonList.add(new StudioButton(103, previewX + previewWidth - 27, bodyBottom - 20, 20, 15, "B"));
    }

    private void addAt(int id, int x, int y, int w, int h, String text) {
        buttonList.add(new StudioButton(id, x, y, w, h, text));
    }
    private void row(int id, int row, String text) {
        addAt(id, controlX, controlTop + rowStep * row, controlWidth, buttonHeight, text);
    }
    private void pairAt(int id, String label, int otherId, String otherLabel, int y, int h) {
        int half = (controlWidth - 4) / 2;
        addAt(id, controlX, y, half, h, label);
        addAt(otherId, controlX + half + 4, y, controlWidth - half - 4, h, otherLabel);
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        CosmeticConfig.Accessory a = current();
        if (button.id == 0) { AlienClient.get().save(); mc.displayGuiScreen(parent); return; }
        if (button.id >= 2 && button.id <= 6) { page = button.id - 2; initGui(); return; }
        switch (button.id) {
            case 10: a.hat = cycle(a.hat, "NONE", "JAPANESE", "SAMURAI", "CROWN"); break;
            case 11: a.face = cycle(a.face, "NONE", "KITSUNE", "FUTURE", "GLASSES"); break;
            case 12: a.back = cycle(a.back, "NONE", "CAPE", "WINGS", "BACKPACK"); break;
            case 13: a.charm = !a.charm; break;
            case 14: a.aura = !a.aura; break;
            case 15: a.trail = !a.trail; break;
            case 16:
                a.hat = "SAMURAI"; a.face = "KITSUNE"; a.back = "CAPE"; a.color = 0x252333;
                a.secondaryColor = 0xD6B56C; a.animations = true; a.capeFlutter = 1.15f; a.capeLength = 1.05f; break;
            case 17:
                a.hat = "CROWN"; a.face = "NONE"; a.back = "WINGS"; a.aura = true;
                a.color = 0xAAB4ED; a.secondaryColor = 0x74E0D4; a.animations = true; break;
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
            case 70: mc.displayGuiScreen(new ColorPickerScreen(this, "Fabric color", a.color | 0xff000000, false, rgb -> a.color = rgb & 0xffffff)); return;
            case 71: mc.displayGuiScreen(new ColorPickerScreen(this, "Accent color", a.secondaryColor | 0xff000000, false, rgb -> a.secondaryColor = rgb & 0xffffff)); return;
            case 80: a.animations = !a.animations; break;
            case 84: a.detailLevel = cycle(a.detailLevel, "Low", "Balanced", "High"); break;
            case 85: a.capeFlutter = a.capeLength = a.hatSize = 1; a.animations = true; a.detailLevel = "Balanced"; break;
            case 86: a.capePattern = cycle(a.capePattern, "ALIEN", "MOON", "STARS", "PLAIN"); break;
            case 90: shownYaw = viewYaw = -24; shownPitch = viewPitch = -7; shownZoom = zoom = 1; message("Preview reset. Drag to turn; scroll to zoom."); break;
            case 91: turntable = !turntable; break;
            case 102:
                config().previewLight = config().previewLight < .55f ? .8f : config().previewLight < .95f ? 1 : .35f;
                message("Studio illumination / " + Math.round(config().previewLight * 100) + "%"); break;
            case 103:
                config().previewBackground = cycle(config().previewBackground, "Violet", "Graphite", "Slate");
                message("Studio background / " + config().previewBackground); break;
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

    @Override public void drawContents(int mouseX, int mouseY, float partialTicks) {
        Ui.background(width,height);
        Ui.shadow(left, top, panelWidth, height - top - 8, Ui.theme().radius + 2, Ui.theme().shadow);
        Ui.gradient(left, top, panelWidth, height - top - 8, Ui.theme().radius,
                Ui.alpha(Ui.theme().surface, Ui.theme().opacity), Ui.alpha(Ui.mix(Ui.theme().surface, 0xff060911, .38f), Ui.theme().opacity));
        Ui.rounded(left + 9, top + 9, 18, 18, 6, Ui.mix(Ui.theme().surfaceRaised, Ui.accent(), .2f));
        Ui.icon("cosmetics", left + 13, top + 13, 10, Ui.accent());
        fontRendererObj.drawString("COSMETIC STUDIO", left + 33, top + 8, Ui.theme().text);
        EntityPlayer player = previewPlayer();
        String name = player != null ? player.getName() : target == null ? "Join a world for a live model" : "Offline player";
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(name + " / local wardrobe", panelWidth - 110), left + 33, top + 21, Ui.theme().muted);
        Ui.rounded(railX - 2, bodyTop, railWidth + 4, bodyBottom - bodyTop, 7, Ui.alpha(Ui.theme().surfaceRaised, .64f));
        String[] sections = {"COLLECTION", "TRANSFORM", "PLAYER LOOKS", "LOCAL ASSETS", "ANIMATION"};
        fontRendererObj.drawString(sections[page], controlX + 2, bodyTop, Ui.theme().muted);
        drawStudioBackdrop();
        long now = System.nanoTime();
        float elapsed = Math.min(.1f, (now - previousFrame) / 1_000_000_000f); previousFrame = now;
        boolean animated = AlienClient.get().config().animations && !Ui.theme().reducedMotion;
        float response = animated ? 1 - (float)Math.exp(-elapsed * 16) : 1;
        if (turntable && !draggingPreview && animated) viewYaw += elapsed * 18f;
        shownYaw += (viewYaw - shownYaw) * response; shownPitch += (viewPitch - shownPitch) * response;
        shownZoom += (zoom - shownZoom) * response;
        if (player != null) drawPlayer(player);
        else drawCenteredString(fontRendererObj, "No player loaded", previewX + previewWidth / 2,
                    bodyTop + (bodyBottom - bodyTop) / 2, Ui.theme().muted);
        if (page == 3) {
            fontRendererObj.drawString(fontRendererObj.trimStringToWidth("config/" + dev.alien.client.edition.BuildEdition.CONFIG_DIRECTORY + "/cosmetics", controlWidth),
                    controlX + 1, controlTop + rowStep * 5 + 2, Ui.theme().muted);
            if (!CosmeticAssets.lastError.isEmpty()) fontRendererObj.drawString(fontRendererObj.trimStringToWidth(CosmeticAssets.lastError, controlWidth),
                    controlX + 1, controlTop + rowStep * 6, 0xffed8d9b);
        }
        for (GuiButton button : buttonList) button.drawButton(mc, mouseX, mouseY);
        if (page == 0) { swatch(70, current().color); swatch(71, current().secondaryColor); }
        String footer = System.currentTimeMillis() < statusUntil ? status : "Drag to orbit  /  Scroll to zoom";
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(footer, panelWidth - 86), left + 10, height - 18, Ui.theme().muted);
        fontRendererObj.drawString("LOCAL ONLY", left + panelWidth - 65, height - 18, Ui.accent());
        if (hover(102, mouseX, mouseY)) studioTooltip("Illumination / " + Math.round(config().previewLight * 100) + "%", mouseX, mouseY);
        if (hover(103, mouseX, mouseY)) studioTooltip("Backdrop / " + config().previewBackground, mouseX, mouseY);
        if (hover(90, mouseX, mouseY)) studioTooltip("Reset camera", mouseX, mouseY);
    }

    private void drawStudioBackdrop() {
        int upper = "Graphite".equals(config().previewBackground) ? 0xff181c24
                : "Slate".equals(config().previewBackground) ? 0xff234451 : 0xff302646;
        int lower = "Graphite".equals(config().previewBackground) ? 0xff0c1018
                : "Slate".equals(config().previewBackground) ? 0xff13212e : 0xff111522;
        Ui.gradient(previewX, bodyTop, previewWidth, bodyBottom - bodyTop, 8, upper, lower);
        Ui.rounded(previewX + 7, bodyTop + 6, 3, 3, 1.5f, 0xff5de3c1);
        fontRendererObj.drawString("LIVE MODEL", previewX + 15, bodyTop + 3, 0xffc2c9d9);
        int floor = bodyBottom - 25, center = previewX + previewWidth / 2;
        for (int i = 0; i < 5; i++) {
            int half = Math.max(12, previewWidth / 2 - 15 - i * 8);
            drawRect(center - half, floor - i * 3, center + half, floor - i * 3 + 1, 0x092ecdeb);
        }
        Ui.rounded(center - Math.min(38, previewWidth / 3), floor - 1,
                Math.min(76, previewWidth * 2 / 3), 3, 1.5f, Ui.alpha(Ui.accent(), .26f));
    }
    private boolean hover(int id, int x, int y) {
        for (GuiButton button : buttonList) if (button.id == id)
            return x >= button.xPosition && x < button.xPosition + button.width && y >= button.yPosition && y < button.yPosition + button.height;
        return false;
    }
    private void studioTooltip(String label, int x, int y) {
        int w = fontRendererObj.getStringWidth(label) + 16;
        int px = Math.max(4, Math.min(width - w - 4, x - w)), py = Math.max(4, y - 23);
        Ui.shadow(px, py, w, 20, 5, .5f); Ui.rounded(px, py, w, 20, 5, Ui.theme().surfaceRaised);
        fontRendererObj.drawString(label, px + 8, py + 5, Ui.theme().text);
    }
    private void swatch(int id, int color) {
        for (GuiButton button : buttonList) if (button.id == id) Ui.rounded(button.xPosition + button.width - 12,
                button.yPosition + 4, 7, button.height - 8, 2, color | 0xff000000);
    }

    /** Render a real entity, restoring every temporary orientation and render-manager value. */
    private void drawPlayer(EntityPlayer player) {
        float body = player.renderYawOffset, yaw = player.rotationYaw, pitch = player.rotationPitch;
        float head = player.rotationYawHead, previousHead = player.prevRotationYawHead;
        RenderManager manager = mc.getRenderManager();
        float oldViewY = manager.playerViewY, oldViewX = manager.playerViewX;
        boolean shadow = manager.isRenderShadow(), debug = manager.isDebugBoundingBox();
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean lit = GL11.glIsEnabled(GL11.GL_LIGHTING), light0 = GL11.glIsEnabled(GL11.GL_LIGHT0), light1 = GL11.glIsEnabled(GL11.GL_LIGHT1);
        boolean material = GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL);
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        savedScissor.clear(); GL11.glGetInteger(GL11.GL_SCISSOR_BOX, savedScissor);
        ScaledResolution resolution = new ScaledResolution(mc); float factor = framebufferScale();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(Math.round(previewX*factor),mc.displayHeight-Math.round((bodyBottom-20)*factor),
                Math.round(previewWidth*factor),Math.round(Math.max(1,bodyBottom-bodyTop-40)*factor));
        drawingPreview = true;
        GlStateManager.pushMatrix();
        try {
            float scale = Math.min(100, (bodyBottom - bodyTop - 51) / 2.4f) * shownZoom;
            GlStateManager.translate(previewX + previewWidth / 2f, bodyBottom - 24f, 100);
            GlStateManager.scale(-scale, scale, scale);
            GlStateManager.rotate(180, 0, 0, 1);
            GlStateManager.rotate(shownPitch, 1, 0, 0);
            GlStateManager.color(1,1,1,1);
            RenderHelper.enableStandardItemLighting();
            float illumination = Math.max(.25f, Math.min(1f, config().previewLight));
            lightVector(GL11.GL_LIGHT_MODEL_AMBIENT, .18f + illumination * .42f, false, 0);
            lightVector(GL11.GL_DIFFUSE, .18f + illumination * .52f, true, GL11.GL_LIGHT0);
            lightVector(GL11.GL_DIFFUSE, .12f + illumination * .42f, true, GL11.GL_LIGHT1);
            GlStateManager.rotate(shownYaw, 0, 1, 0);
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
            GL11.glPopAttrib();
            if (lit) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
            if (light0) GlStateManager.enableLight(0); else GlStateManager.disableLight(0);
            if (light1) GlStateManager.enableLight(1); else GlStateManager.disableLight(1);
            if (material) GlStateManager.enableColorMaterial(); else GlStateManager.disableColorMaterial();
        }
    }

    private void lightVector(int property, float value, boolean source, int light) {
        previewLight.clear(); previewLight.put(value).put(value).put(value).put(1).rewind();
        if (source) GL11.glLight(light, property, previewLight); else GL11.glLightModel(property, previewLight);
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
        if (activeButton != null) {
            activeButton.mouseReleased(x, y);
            if (activeButton instanceof Ui.Slider) AlienClient.get().save();
            activeButton = null;
        }
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

    private static class StudioButton extends GuiButton {
        protected boolean selected;
        private float hoverAmount;
        StudioButton(int id, int x, int y, int w, int h, String text) { super(id, x, y, w, h, text); }
        StudioButton selected(boolean selected) { this.selected = selected; return this; }
        @Override public void drawButton(Minecraft mc, int x, int y) {
            if (!visible) return;
            hovered = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
            boolean animated = AlienClient.get().config().animations && !Ui.theme().reducedMotion;
            hoverAmount += ((hovered && enabled ? 1 : 0) - hoverAmount) * (animated ? .22f : 1);
            if (id >= 2 && id <= 6) {
                int fill = selected ? Ui.mix(Ui.theme().surfaceRaised, Ui.accent(), .28f)
                        : Ui.mix(Ui.theme().surfaceRaised, Ui.accent(), hoverAmount * .1f);
                Ui.rounded(xPosition + 1, yPosition, width - 2, height, 5, fill);
                String icon = new String[]{"cosmetics", "settings", "player", "import", "effects"}[id - 2];
                Ui.icon(icon, xPosition + width / 2f - 4, yPosition + 4, 8, selected ? Ui.accent() : Ui.theme().muted);
                String label = Ui.font().trimStringToWidth(displayString, width - 3);
                Ui.font().drawString(label, xPosition + (width - Ui.font().getStringWidth(label)) / 2f,
                        yPosition + height - 12, selected ? Ui.theme().text : Ui.theme().muted, false);
                return;
            }
            if (displayString.endsWith(": ON") || displayString.endsWith(": OFF")) {
                String label = displayString.substring(0, displayString.lastIndexOf(':'));
                Ui.switchControl(xPosition, yPosition, width, height, label, displayString.endsWith(": ON"), hoverAmount);
            } else {
                Ui.rounded(xPosition, yPosition, width, height, Math.min(5, height / 3f),
                        Ui.mix(Ui.theme().surfaceRaised, Ui.accent(), hoverAmount * .15f));
                if (id == 90 || id == 102 || id == 103) {
                    Ui.icon(id == 90 ? "reset" : id == 102 ? "sun" : "world", xPosition + width / 2f - 4,
                            yPosition + height / 2f - 4, 8, Ui.theme().muted);
                } else {
                    String label = Ui.font().trimStringToWidth(displayString, width - ((id == 70 || id == 71) ? 17 : 10));
                    Ui.font().drawString(label, xPosition + 5, yPosition + (height - Ui.font().FONT_HEIGHT) / 2f,
                            enabled ? Ui.theme().text : Ui.theme().muted, false);
                }
            }
            mouseDragged(mc, x, y);
        }
        @Override protected int getHoverState(boolean hovered) { return 0; }
    }

    private final class AccessoryCard extends StudioButton {
        private final String category, value;
        AccessoryCard(int id, int y, int h, String category, String value) {
            super(id, controlX, y, controlWidth, h, value); this.category = category; this.value = value;
        }
        @Override public void drawButton(Minecraft mc, int mx, int my) {
            hovered = mx >= xPosition && mx < xPosition + width && my >= yPosition && my < yPosition + height;
            Ui.rounded(xPosition, yPosition, width, height, 5,
                    Ui.mix(Ui.theme().surfaceRaised, Ui.accent(), hovered ? .15f : .04f));
            Ui.rounded(xPosition + 2, yPosition + 2, 27, height - 4, 4, Ui.alpha(Ui.accent(), .10f));
            drawAccessoryThumbnail(id, xPosition + 15, yPosition + height / 2f, 24, height - 5);
            Ui.font().drawString(category, xPosition + 34, yPosition + 2, Ui.theme().muted);
            Ui.font().drawString(Ui.font().trimStringToWidth(value, width - 39), xPosition + 34,
                    yPosition + height - 12, Ui.theme().text);
        }
    }

    /** Real cached accessory geometry, not a picture or a generated mockup. */
    private void drawAccessoryThumbnail(int id, float centerX, float centerY, float w, float h) {
        CosmeticConfig.Accessory a = current();
        MeshView mesh;
        if (id == 10) mesh = CosmeticMeshes.hat(a.hat);
        else if (id == 11) mesh = CosmeticMeshes.face(a.face);
        else if ("CAPE".equals(a.back)) { thumbnailCape.update(0, 0, 0); mesh = thumbnailCape; }
        else mesh = CosmeticMeshes.back(a.back, true);
        if (mesh.vertexCount() == 0) {
            Ui.rounded(centerX - 4, centerY - 1, 8, 2, 1, Ui.theme().muted); return;
        }
        float minX=Float.MAX_VALUE,minY=Float.MAX_VALUE,minZ=Float.MAX_VALUE,maxX=-Float.MAX_VALUE,maxY=-Float.MAX_VALUE,maxZ=-Float.MAX_VALUE;
        for(int i=0;i<mesh.vertexCount();i++) {
            minX=Math.min(minX,mesh.x(i)); maxX=Math.max(maxX,mesh.x(i));
            minY=Math.min(minY,mesh.y(i)); maxY=Math.max(maxY,mesh.y(i));
            minZ=Math.min(minZ,mesh.z(i)); maxZ=Math.max(maxZ,mesh.z(i));
        }
        float scale = Math.min(w / Math.max(1,maxX-minX),h / Math.max(1,maxY-minY)) * 13;
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_POLYGON_BIT);
        GlStateManager.pushMatrix();
        try {
            GL11.glDisable(GL11.GL_LIGHTING); GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_DEPTH_TEST);
            GlStateManager.translate(centerX, centerY, 70);
            GlStateManager.scale(scale, scale, scale); GlStateManager.rotate(-12,1,0,0); GlStateManager.rotate(-24,0,1,0);
            GlStateManager.translate(-(minX+maxX)/32f,-(minY+maxY)/32f,-(minZ+maxZ)/32f);
            if (mesh instanceof Mesh) CosmeticMeshRenderer.draw((Mesh)mesh,a.color,a.secondaryColor);
            else {
                GL11.glEnable(GL11.GL_TEXTURE_2D); mc.getTextureManager().bindTexture(CapeTextures.get(a));
                GL11.glBegin(GL11.GL_TRIANGLES);
                for(int i=0;i<mesh.vertexCount();i++) {
                    GL11.glColor4f(.9f,.9f,.9f,1); GL11.glTexCoord2f(thumbnailCape.u(i),thumbnailCape.v(i));
                    GL11.glVertex3f(mesh.x(i)/16f,mesh.y(i)/16f,mesh.z(i)/16f);
                }
                GL11.glEnd();
            }
        } finally {
            GlStateManager.popMatrix(); GL11.glPopAttrib(); GlStateManager.bindTexture(previousTexture);
        }
    }

    private float sliderMin(int field) {
        return field == 6 ? .25f : field == 7 ? 0 : field == 8 ? .65f : field == 9 ? .75f : field < 3 ? -16 : -180;
    }
    private float sliderMax(int field) {
        return field == 6 ? 3 : field == 7 ? 2 : field == 8 || field == 9 ? 1.35f : field < 3 ? 16 : 180;
    }
    private float sliderValue(int field) {
        CosmeticConfig.Accessory a = current();
        switch (field) {
            case 0: return a.offsetX; case 1: return a.offsetY; case 2: return a.offsetZ;
            case 3: return a.rotationX; case 4: return a.rotationY; case 5: return a.rotationZ;
            case 6: return a.scale; case 7: return a.capeFlutter; case 8: return a.capeLength; default: return a.hatSize;
        }
    }
    private void sliderValue(int field, float value) {
        CosmeticConfig.Accessory a = current();
        switch (field) {
            case 0: a.offsetX=value; break; case 1: a.offsetY=value; break; case 2: a.offsetZ=value; break;
            case 3: a.rotationX=value; break; case 4: a.rotationY=value; break; case 5: a.rotationZ=value; break;
            case 6: a.scale=value; break; case 7: a.capeFlutter=value; break; case 8: a.capeLength=value; break; default:a.hatSize=value;
        }
    }
    private final class StudioSlider extends Ui.Slider {
        StudioSlider(int id,int x,int y,int width,int height,String label,int field) {
            super(id,x,y,width,height,label,CosmeticStudioScreen.this.sliderMin(field),CosmeticStudioScreen.this.sliderMax(field),field<6?.1f:.01f,
                    CosmeticStudioScreen.this.sliderValue(field),value -> CosmeticStudioScreen.this.sliderValue(field,value));
        }
    }
}
