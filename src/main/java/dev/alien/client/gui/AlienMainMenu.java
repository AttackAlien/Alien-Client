package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraftforge.fml.client.GuiModList;

import java.io.IOException;

/** Optional title replacement using original procedural artwork only. */
public final class AlienMainMenu extends GuiMainMenu {
    @Override public void initGui() {
        buttonList.clear();
        int x = width / 2 - 105, y = height / 2 - 8;
        buttonList.add(new Ui.Button(1, x, y, 210, 24, "Singleplayer"));
        buttonList.add(new Ui.Button(2, x, y + 30, 210, 24, "Multiplayer"));
        buttonList.add(new Ui.Button(3, x, y + 60, 210, 24, "Alien Client / Studios"));
        buttonList.add(new Ui.Button(4, x, y + 90, 101, 22, "Options"));
        buttonList.add(new Ui.Button(5, x + 109, y + 90, 101, 22, "Quit"));
        buttonList.add(new Ui.Button(6, width - 66, 10, 56, 20, "Mods"));
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xff181324, 0xff080c16);
        long now = AlienClient.get().config().animations ? System.currentTimeMillis() : 0;
        for (int i = 0; i < 74; i++) {
            int x = Math.floorMod(i * 137 + 43, Math.max(1, width));
            int y = Math.floorMod(i * 73 + 11, Math.max(1, height));
            float brightness = .18f + .22f * (float) (Math.sin(now / 1800d + i) * .5 + .5);
            drawRect(x, y, x + (i % 7 == 0 ? 2 : 1), y + 1, Ui.alpha(Ui.accent(), brightness));
        }
        int center = width / 2, titleY = Math.max(22, height / 2 - 72);
        drawRect(center - 19, titleY - 14, center + 19, titleY - 12, Ui.accent());
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(center, titleY, 0);
        net.minecraft.client.renderer.GlStateManager.scale(3f, 3f, 1f);
        drawCenteredString(fontRendererObj, "ALIEN", 0, 0, Ui.TEXT);
        net.minecraft.client.renderer.GlStateManager.popMatrix();
        drawCenteredString(fontRendererObj, "C L I E N T   /   U L T I M A T E", center, titleY + 32, Ui.MUTED);
        for (GuiButton button : buttonList) button.drawButton(mc, mouseX, mouseY);
        fontRendererObj.drawString("Minecraft 1.8.9 / Forge", 9, height - 14, Ui.MUTED);
        String credit = "Alien Client | Local visuals";
        fontRendererObj.drawString(credit, width - fontRendererObj.getStringWidth(credit) - 9, height - 14, Ui.MUTED);
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) mc.displayGuiScreen(new GuiSelectWorld(this));
        if (button.id == 2) mc.displayGuiScreen(new GuiMultiplayer(this));
        if (button.id == 3) mc.displayGuiScreen(new AlienMenuScreen(this));
        if (button.id == 4) mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        if (button.id == 5) mc.shutdown();
        if (button.id == 6) mc.displayGuiScreen(new GuiModList(this));
    }

    @Override public void updateScreen() { }
    @Override protected void keyTyped(char typedChar, int keyCode) { }
}
