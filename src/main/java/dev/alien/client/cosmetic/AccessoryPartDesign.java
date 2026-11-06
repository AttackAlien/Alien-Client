package dev.alien.client.cosmetic;

/** Per-look, local fitting values for one of the six body accessory groups. */
public final class AccessoryPartDesign {
    public boolean enabled;
    public String style = "Classic";
    /** Offsets are model pixels and are converted at the biped attachment point. */
    public float x, y, z, scale = 1;

    public void normalize(String slot) {
        String[] styles = "HORNS".equals(slot) ? new String[] {"Classic", "Crescent"}
                : "MASK".equals(slot) ? new String[] {"Oni", "Tech"}
                : "SHOULDERS".equals(slot) ? new String[] {"Dragon", "Knight"}
                : "TAIL".equals(slot) ? new String[] {"Spade", "Crystal"}
                : "ARMS".equals(slot) ? new String[] {"Bracers", "Gauntlets"}
                : new String[] {"Greaves", "Boots"};
        boolean valid = false;
        for (String option : styles) if (option.equals(style)) valid = true;
        if (!valid) style = styles[0];
        x = clamp(x, -8, 8); y = clamp(y, -8, 8); z = clamp(z, -8, 8);
        scale = clamp(scale, .5f, 2f);
    }

    public AccessoryPartDesign copy() {
        AccessoryPartDesign result = new AccessoryPartDesign();
        result.enabled = enabled; result.style = style; result.x = x; result.y = y;
        result.z = z; result.scale = scale;
        return result;
    }

    private static float clamp(float value, float min, float max) {
        return Float.isNaN(value) || Float.isInfinite(value) ? min : Math.max(min, Math.min(max, value));
    }
}
