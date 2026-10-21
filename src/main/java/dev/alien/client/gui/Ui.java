package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

/** Small original immediate-mode UI toolkit; uses Minecraft's bundled font. */
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

    public static final class Button extends GuiButton {
        private boolean selected;
        private float hover;

        public Button(int id, int x, int y, int width, int height, String text) {
            super(id, x, y, width, height, text);
        }

        public Button selected(boolean value) { selected = value; return this; }

        @Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            hover += ((hovered && enabled ? 1f : 0f) - hover) * (AlienClient.get().config().animations ? .25f : 1f);
            int base = selected ? mix(0xff242132, accent(), .28f) : 0xff22222e;
            panel(xPosition, yPosition, width, height, mix(base, accent(), hover * .18f));
            if (selected) drawRect(xPosition, yPosition + 4, xPosition + 2, yPosition + height - 4, accent());
            mc.fontRendererObj.drawStringWithShadow(displayString, xPosition + 9, yPosition + (height - 8) / 2, enabled ? TEXT : MUTED);
            mouseDragged(mc, mouseX, mouseY);
        }
    }
}
