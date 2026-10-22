package dev.alien.client.gui;

import com.lukflug.panelstudio.base.SettingsAnimation;
import dev.alien.client.AlienClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

/** Lightweight Alien render helpers and PanelStudio-based time interpolation; still uses Minecraft's bundled font. */
public final class Ui extends Gui {
    public static final int TEXT = 0xfff3f1ff;
    public static final int MUTED = 0xffa3a0b5;
    public static final int PANEL = 0xff151520;
    private Ui() { }

    public static int accent() {
        if (AlienClient.get().config().rainbowAccent) {
            return 0xff000000 | java.awt.Color.HSBtoRGB((System.currentTimeMillis() % 12000L) / 12000f, .55f, 1f);
        }
        return AlienClient.get().config().accentColor | 0xff000000;
    }

    public static int alpha(int color, float amount) {
        return (Math.max(0, Math.min(255, (int) (255 * amount))) << 24) | (color & 0xffffff);
    }

    public static int mix(int a, int b, float fraction) {
        float f = Math.max(0f, Math.min(1f, fraction));
        int r = (int) (((a >> 16) & 255) * (1 - f) + ((b >> 16) & 255) * f);
        int g = (int) (((a >> 8) & 255) * (1 - f) + ((b >> 8) & 255) * f);
        int bl = (int) ((a & 255) * (1 - f) + (b & 255) * f);
        return 0xff000000 | (r << 16) | (g << 8) | bl;
    }

    public static void panel(int x, int y, int width, int height, int color) {
        drawRect(x + 2, y, x + width - 2, y + height, color);
        drawRect(x, y + 2, x + width, y + height - 2, color);
    }

    /**
     * Compact, no-stencil rounded rectangle. Called only for interactive UI surfaces,
     * not the in-world renderer. Every strip is opaque to avoid alpha accumulation.
     */
    public static void roundedPanel(int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) return;
        int radius = Math.min(4, Math.min(width, height) / 2);
        if (radius < 2) { drawRect(x, y, x + width, y + height, color); return; }
        drawRect(x + radius, y, x + width - radius, y + height, color);
        drawRect(x, y + radius, x + width, y + height - radius, color);
        for (int i = 1; i < radius; i++) {
            int inset = (int) Math.ceil(radius - Math.sqrt(radius * radius - (radius - i) * (radius - i)));
            drawRect(x + inset, y + i, x + width - inset, y + i + 1, color);
            drawRect(x + inset, y + height - i - 1, x + width - inset, y + height - i, color);
        }
    }

    public static final class Button extends GuiButton {
        private boolean selected;
        // PanelStudio MIT animation: elapsed-time easing instead of per-frame increments.
        private final SettingsAnimation hoverAnimation = new SettingsAnimation(
                () -> Math.max(1, Math.round(150f / Math.max(.25f, AlienClient.get().config().animationSpeed))),
                System::currentTimeMillis);

        public Button(int id, int x, int y, int width, int height, String text) {
            super(id, x, y, width, height, text);
            hoverAnimation.initValue(0);
        }

        public Button selected(boolean value) { selected = value; return this; }

        @Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            double target = hovered && enabled ? 1 : 0;
            if (hoverAnimation.getTarget() != target) hoverAnimation.setValue(target);
            float hover = (float) (AlienClient.get().config().animations ? hoverAnimation.getValue() : target);
            int accentColor = accent();
            int base = selected ? mix(0xff242132, accentColor, .28f) : 0xff22222e;
            int border = mix(0xff3a3549, accentColor, selected ? .36f : hover * .28f);
            roundedPanel(xPosition, yPosition, width, height, border);
            roundedPanel(xPosition + 1, yPosition + 1, width - 2, height - 2, mix(base, accentColor, hover * .17f));
            if (selected && height >= 10) drawRect(xPosition + 2, yPosition + 5, xPosition + 4, yPosition + height - 5, accentColor);
            String label = mc.fontRendererObj.trimStringToWidth(displayString, Math.max(0, width - 18));
            mc.fontRendererObj.drawStringWithShadow(label, xPosition + 9, yPosition + (height - 8) / 2, enabled ? TEXT : MUTED);
            mouseDragged(mc, mouseX, mouseY);
        }
    }
}
