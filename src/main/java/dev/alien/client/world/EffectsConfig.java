package dev.alien.client.world;

/** Every effect is independently optional and has a bounded rendering budget. */
public final class EffectsConfig {
    public boolean enabled = false;
    public boolean movement = false;
    public boolean landing = false;
    public boolean trails = false;
    public boolean ambient = false;
    public String shape = "Diamond";
    public String quality = "Balanced";
    public int color = 0xAF83FF;
    public int secondaryColor = 0xE8C7FF;
    public float size = 3F;
    public float distance = 24F;
    public int budget = 96;
    public boolean colorFilter = false;
    public float filterStrength = 0.08F;
    public boolean cameraSway = false;
    public float swayStrength = 0.4F;

    public void normalize() {
        if (!("Low".equals(quality) || "Balanced".equals(quality) || "High".equals(quality))) quality = "Balanced";
        if (!("Diamond".equals(shape) || "Petal".equals(shape) || "Spark".equals(shape))) shape = "Diamond";
        color &= 0xFFFFFF; secondaryColor &= 0xFFFFFF;
        size = Float.isNaN(size) ? 3F : Math.max(1F, Math.min(8F, size));
        distance = Float.isNaN(distance) ? 24F : Math.max(4F, Math.min(64F, distance));
        budget = Math.max(0, Math.min(256, budget));
        filterStrength = Float.isNaN(filterStrength) ? 0.08F : Math.max(0F, Math.min(0.25F, filterStrength));
        swayStrength = Float.isNaN(swayStrength) ? 0.4F : Math.max(0F, Math.min(1.5F, swayStrength));
    }

    public int effectiveBudget() {
        int maximum = "Low".equals(quality) ? 32 : "High".equals(quality) ? 256 : 96;
        return Math.max(0, Math.min(Math.min(budget, maximum), 256));
    }
}
