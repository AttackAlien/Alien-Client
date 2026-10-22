package dev.alien.client.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

final class StudioSlider extends GuiButton {
    interface Change { void set(float value); }
    private final float min, max;
    private final String label;
    private final Change change;
    private float value;
    private boolean dragging;

    StudioSlider(int id, int x, int y, int width, String label, float min, float max,
                 float value, Change change) {
        super(id, x, y, width, 20, "");
        this.label = label; this.min = min; this.max = max;
        this.value = Math.max(min, Math.min(max, value)); this.change = change;
        updateLabel();
    }

    private void updateLabel() {
        displayString = label + ": " + ((max <= 1 || "Drift deg/sec".equals(label)) ? String.format(java.util.Locale.ROOT, "%.2f", value)
                : String.format(java.util.Locale.ROOT, "%.0f", value));
    }

    private void set(int mouseX) {
        float fraction = Math.max(0F, Math.min(1F, (mouseX - xPosition - 4F) / (width - 8F)));
        value = min + fraction * (max - min);
        change.set(value); updateLabel();
    }

    @Override public boolean mousePressed(Minecraft mc, int x, int y) {
        if (!super.mousePressed(mc, x, y)) return false;
        dragging = true; set(x); return true;
    }

    @Override protected void mouseDragged(Minecraft mc, int x, int y) {
        if (dragging) set(x);
        int marker = xPosition + 3 + (int)((value - min) / (max - min) * (width - 8));
        drawRect(marker, yPosition + height - 4, marker + 3, yPosition + height - 1, 0xFFC2A0FF);
    }

    @Override public void mouseReleased(int x, int y) { dragging = false; }
}
