package dev.alien.client.world;

import dev.alien.client.AlienClient;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

/** Local-only PNG/3x2 cubemap browser; never silently changes the active sky. */
public final class SkyboxGalleryScreen extends GuiScreen {
    private final GuiScreen parent;
    private List<String> files = Collections.emptyList();
    private String selected = "";
    private String message = "Choose a local PNG. Preview first, then Apply.";
    private ResourceLocation preview;
    private int page, rows, left, panelWidth, listWidth, top = 52;

    public SkyboxGalleryScreen(GuiScreen parent) { this.parent = parent; }
    private WorldConfig world() { return AlienClient.get().config().world; }
    private File folder() { return new File(AlienClient.get().store().directory(), "skyboxes"); }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(false);
        if (files.isEmpty()) refreshFiles();
        panelWidth = Math.min(660, width - 22);
        left = (width - panelWidth) / 2;
        listWidth = Math.max(125, panelWidth / 2 - 8);
        rows = Math.min(6, Math.max(2, (height - 126) / 23));
        if (page * rows >= files.size()) page = Math.max(0, (files.size() - 1) / rows);
        buttonList.clear();
        for (int i = 0; i < rows; i++) {
            int index = page * rows + i;
            if (index >= files.size()) break;
            String file = files.get(index);
            boolean favorite = SkyboxGallery.isFavorite(world(), file);
            String display = (favorite ? "* " : "  ") + file;
            int available = listWidth - 13;
            display = fontRendererObj.trimStringToWidth(display, available);
            if (file.equals(selected)) display = "> " + display.trim();
            buttonList.add(new GuiButton(100 + i, left + 4, top + 23 * i, listWidth - 8, 20, display));
        }
        int bottom = height - 56;
        int btn = Math.max(50, (panelWidth - 24) / 4);
        buttonList.add(new GuiButton(20, left + 4, bottom, btn, 20, "< Prev"));
        buttonList.add(new GuiButton(21, left + 8 + btn, bottom, btn, 20, "Next >"));
        buttonList.add(new GuiButton(30, left + 12 + btn * 2, bottom, btn, 20, "Favorite"));
        buttonList.add(new GuiButton(33, left + 16 + btn * 3, bottom, btn, 20, "Refresh"));
        int width3 = Math.max(65, (panelWidth - 16) / 3);
        buttonList.add(new GuiButton(31, left + 4, height - 30, width3, 20, "Apply selected"));
        buttonList.add(new GuiButton(32, left + 8 + width3, height - 30, width3, 20, "Procedural"));
        buttonList.add(new GuiButton(0, left + 12 + width3 * 2, height - 30, width3, 20, "Done"));
    }

    private void refreshFiles() {
        files = new ArrayList<String>(SkyboxGallery.list(folder()));
        Collections.sort(files, new Comparator<String>() {
            @Override public int compare(String a, String b) {
                boolean fa=SkyboxGallery.isFavorite(world(),a), fb=SkyboxGallery.isFavorite(world(),b);
                if (fa != fb) return fa ? -1 : 1;
                int cmp=a.compareToIgnoreCase(b);
                return cmp==0?a.compareTo(b):cmp;
            }
        });
        if (files.isEmpty()) {
            selected = "";
            releasePreview();
            message = "No PNG files. Place PNG / .cube.png in config/alien-client/skyboxes.";
        } else {
            if (!files.contains(selected)) {
                selected = files.contains(world().skyboxFile) ? world().skyboxFile : files.get(0);
            }
            preparePreview();
        }
    }

    private void releasePreview() {
        if (preview != null && mc != null) mc.getTextureManager().deleteTexture(preview);
        preview = null;
    }

    private void preparePreview() {
        releasePreview();
        if (selected.isEmpty()) return;
        try {
            File file = SkyboxLoader.resolve(folder(), selected);
            if (file == null) throw new IOException("No PNG selected");
            BufferedImage image = SkyboxLoader.read(file);
            preview = mc.getTextureManager().getDynamicTextureLocation(
                    "alien-sky-gallery", new DynamicTexture(image));
            message = (SkyboxAtlas.isCubeFile(selected) ? "Six-face atlas" : "Equirectangular panorama")
                    + " / " + image.getWidth() + "x" + image.getHeight() + " / " + selected;
        } catch (IOException | RuntimeException ex) {
            message = "Cannot preview " + selected + ": " + ex.getMessage();
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        int id = button.id;
        if (id == 0) { mc.displayGuiScreen(parent); return; }
        if (id >= 100 && id < 100 + rows) {
            int index = page * rows + (id - 100);
            if (index < files.size()) { selected = files.get(index); preparePreview(); }
        } else if (id == 20) {
            if (page > 0) page--;
        } else if (id == 21) {
            if ((page + 1) * rows < files.size()) page++;
        } else if (id == 30) {
            SkyboxGallery.toggleFavorite(world(), selected);
            AlienClient.get().save();
            refreshFiles();
        } else if (id == 33) {
            refreshFiles();
        } else if (id == 31) {
            if (preview != null && !selected.isEmpty()) {
                world().skyboxFile = selected;
                world().enabled = true;
                AlienClient.get().save();
                message = "Applied " + selected + ". Return to the world to see the sky.";
            } else message = "Choose a valid PNG before applying.";
        } else if (id == 32) {
            world().skyboxFile = "";
            AlienClient.get().save();
            message = "Procedural sky selected.";
        }
        initGui();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "ALIEN / SKYBOX GALLERY", width / 2, 12, 0xffd7b9ff);
        drawCenteredString(fontRendererObj, "Local PNGs / favorites / preview / apply", width / 2, 26, 0xffa9a6b7);
        drawRect(left, top - 4, left + panelWidth, height - 60, 0xb31a172b);
        int previewX = left + listWidth + 10;
        int previewW = Math.max(24, panelWidth - listWidth - 16);
        int previewY = top + 8;
        int previewH = Math.max(30, height - top - 100);
        drawRect(previewX, previewY, previewX + previewW, previewY + previewH, 0xff1c1c2e);
        if (preview != null) {
            mc.getTextureManager().bindTexture(preview);
            GlStateManager.color(1f,1f,1f,1f);
            drawModalRectWithCustomSizedTexture(previewX+4, previewY+4, 0, 0,
                    previewW-8, Math.max(16,previewH-8),previewW-8,Math.max(16,previewH-8));
        } else {
            drawCenteredString(fontRendererObj, "No valid preview", previewX + previewW/2,
                    previewY + previewH/2,0xffc8b6d0);
        }
        super.drawScreen(mouseX,mouseY,partialTicks);
        String active = world().skyboxFile.isEmpty() ? "procedural" : world().skyboxFile;
        drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth(
                "Active: " + active + "   [" + files.size() + " skyboxes, page " + (page+1) + "]",
                width - 12), width/2, height-77, 0xffcbc3da);
        drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth(message,width-16),
                width/2,height-67,0xffb3a9c9);
    }

    @Override protected void keyTyped(char character, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(character,key);
    }
    @Override public void onGuiClosed() {
        releasePreview();
        AlienClient.get().save();
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
