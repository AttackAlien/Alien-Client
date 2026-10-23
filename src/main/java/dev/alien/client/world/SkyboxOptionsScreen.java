package dev.alien.client.world;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** Optional local skybox controls; neither time nor world/gameplay state changes. */
public final class SkyboxOptionsScreen extends dev.alien.client.gui.PremiumScreen {
    private final GuiScreen parent;
    private int left, panelWidth;

    public SkyboxOptionsScreen(GuiScreen parent) { this.parent = parent; }
    private WorldConfig settings() { return AlienClient.get().config().world; }

    @Override public void initGui() {
        fontRendererObj = Ui.font();
        buttonList.clear();
        panelWidth = Math.min(440, width - 28);
        left = (width - panelWidth) / 2;
        WorldConfig c = settings();
        buttonList.add(new StudioSlider(1, left, 53, panelWidth, "Skybox yaw", 0F, 360F, c.skyboxYaw,
                value -> c.skyboxYaw = value));
        buttonList.add(new StudioSlider(2, left, 81, panelWidth, "Drift deg/sec", 0F, 2F, c.skyboxDrift,
                value -> c.skyboxDrift = value));
        buttonList.add(new StudioSlider(3, left, 109, panelWidth, "PNG brightness", .2F, 1F, c.skyboxBrightness,
                value -> c.skyboxBrightness = value));
        buttonList.add(new Ui.Button(4, left, 143, panelWidth, 23,
                "Twinkling stars: " + (c.twinklingStars ? "ON" : "OFF")));
        buttonList.add(new Ui.Button(5, left, 173, panelWidth, 22, "Reset skybox options"));
        buttonList.add(new Ui.Button(0, width / 2 - 64, height - 27, 128, 22, "Done"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) { AlienClient.get().save(); mc.displayGuiScreen(parent); return; }
        if (button.id == 4) settings().twinklingStars = !settings().twinklingStars;
        else if (button.id == 5) {
            WorldConfig c = settings();
            c.skyboxYaw = 0F;
            c.skyboxDrift = 0F;
            c.skyboxBrightness = 1F;
            c.twinklingStars = false;
        } else return;
        AlienClient.get().save();
        initGui();
    }

    @Override public void drawContents(int mouseX, int mouseY, float partialTicks) {
        Ui.background(width, height);
        Ui.card(left - 8, 8, panelWidth + 16, height - 15);
        Ui.rounded(left + 3, 10, panelWidth - 6, 2, 1, Ui.accent());
        drawCenteredString(fontRendererObj, "ALIEN / SKYBOX OPTIONS", width / 2, 18, Ui.theme().text);
        drawCenteredString(fontRendererObj, "Panorama PNG or six-face 3x2 .cube.png atlas", width / 2, 32, Ui.theme().muted);
        super.drawContents(mouseX, mouseY, partialTicks);
        if (height >= 245) drawCenteredString(fontRendererObj,
                "Local visuals only - world time and server weather stay unchanged",
                width / 2, height - 41, Ui.theme().muted);
    }

    @Override protected void keyTyped(char c, int code) throws IOException {
        if (code == Keyboard.KEY_ESCAPE) { AlienClient.get().save(); mc.displayGuiScreen(parent); }
        else super.keyTyped(c, code);
    }

    @Override public void onGuiClosed() { AlienClient.get().save(); }
    @Override public boolean doesGuiPauseGame() { return false; }
}
