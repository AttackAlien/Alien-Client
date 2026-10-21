package dev.alien.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.Locale;
import java.util.function.Consumer;

/** Clamped slider that updates the live preview and saves once on release. */
public final class ValueSlider extends GuiButton {
    private final String label;
    private final float minimum, maximum, step;
    private final Consumer<Float> change;
    private final Runnable commit;
    private float value;
    private boolean dragging;

    public ValueSlider(int id, int x, int y, int width, String label, float minimum, float maximum,
                       float step, float value, Consumer<Float> change, Runnable commit) {
        super(id, x, y, width, 25, label);
        this.label = label;
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
        this.value = Math.max(minimum, Math.min(maximum, value));
        this.change = change;
        this.commit = commit;
    }

    @Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        mouseDragged(mc, mouseX, mouseY);
        String formatted = step >= 1 ? Integer.toString(Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
        mc.fontRendererObj.drawStringWithShadow(label, xPosition, yPosition, Ui.MUTED);
        mc.fontRendererObj.drawStringWithShadow(formatted, xPosition + width - mc.fontRendererObj.getStringWidth(formatted), yPosition, Ui.TEXT);
        drawRect(xPosition, yPosition + 16, xPosition + width, yPosition + 20, 0xff30303d);
        int thumb = xPosition + Math.round((value - minimum) / (maximum - minimum) * width);
        drawRect(xPosition, yPosition + 16, thumb, yPosition + 20, Ui.accent());
        Ui.panel(thumb - 3, yPosition + 13, 6, 10, Ui.TEXT);
    }

    @Override public boolean mousePressed(Minecraft mc, int x, int y) {
        if (!super.mousePressed(mc, x, y)) return false;
        dragging = true;
        update(x);
        return true;
    }

    @Override protected void mouseDragged(Minecraft mc, int x, int y) { if (dragging) update(x); }
    @Override public void mouseReleased(int x, int y) { if (dragging) { dragging = false; commit.run(); } }

    private void update(int x) {
        float next = minimum + Math.max(0f, Math.min(1f, (x - xPosition) / (float) width)) * (maximum - minimum);
        next = Math.max(minimum, Math.min(maximum, Math.round(next / step) * step));
        if (Float.compare(next, value) != 0) { value = next; change.accept(value); }
    }
}
