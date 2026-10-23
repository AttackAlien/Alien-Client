package dev.alien.client.gui.theme;

/** Shared, persisted design tokens. No game or OpenGL dependency. */
public final class ThemeConfig {
    public static final String[] PRESETS = {"Alien Purple", "Midnight Black", "Cyber Cyan", "Crimson Red", "Minimal White", "Custom RGB"};
    public String preset = "Alien Purple";
    public int primary = 0xffa98bff, secondary = 0xff43d9ed;
    public int surface = 0xff12141d, surfaceRaised = 0xff202330;
    public int text = 0xfff5f3fc, muted = 0xff989daf;
    public float opacity = .96f, radius = 7f, shadow = .65f;
    public float animationSpeed = 1f, menuScale = 1f, blur = 0f;
    public String font = "Modern";
    public boolean reducedMotion, hoverEffects = true;

    public void normalize() {
        boolean known = false;
        for (String candidate : PRESETS) if (candidate.equals(preset)) known = true;
        if (!known) preset = "Custom RGB";
        opacity = clamp(opacity, .3f, 1f, .96f);
        radius = clamp(radius, 0, 14, 7);
        shadow = clamp(shadow, 0, 1, .65f);
        animationSpeed = clamp(animationSpeed, .25f, 3f, 1);
        menuScale = clamp(menuScale, .75f, 1.25f, 1);
        blur = clamp(blur, 0, 1, 0);
        if (!"Modern".equals(font) && !"Vanilla".equals(font)) font = "Modern";
    }

    public void applyPreset(String name) {
        if ("Midnight Black".equals(name)) {
            primary=0xffb4b9cb;secondary=0xff68748b;surface=0xff0d1016;surfaceRaised=0xff1b202a;text=0xfff1f4fa;muted=0xff949daf;
        } else if ("Cyber Cyan".equals(name)) {
            primary=0xff43d9ed;secondary=0xff7c91ff;surface=0xff10191f;surfaceRaised=0xff1b2a35;text=0xffeefdff;muted=0xff8caab6;
        } else if ("Crimson Red".equals(name)) {
            primary=0xffff697f;secondary=0xffffb38b;surface=0xff1a121a;surfaceRaised=0xff2c202a;text=0xfffff3f6;muted=0xffb89ca9;
        } else if ("Minimal White".equals(name)) {
            primary=0xff7455c3;secondary=0xff198baa;surface=0xffedf0f6;surfaceRaised=0xffdfe4ee;text=0xff202636;muted=0xff616b7d;
        } else if ("Alien Purple".equals(name)) {
            primary=0xffa98bff;secondary=0xff43d9ed;surface=0xff12141d;surfaceRaised=0xff202330;text=0xfff5f3fc;muted=0xff989daf;
        } else if (!"Custom RGB".equals(name)) throw new IllegalArgumentException("Unknown theme preset: " + name);
        preset=name;
        normalize();
    }

    public ThemeConfig copy() {
        ThemeConfig result = new ThemeConfig();
        result.preset=preset;result.primary=primary;result.secondary=secondary;result.surface=surface;
        result.surfaceRaised=surfaceRaised;result.text=text;result.muted=muted;result.opacity=opacity;
        result.radius=radius;result.shadow=shadow;result.animationSpeed=animationSpeed;result.menuScale=menuScale;
        result.blur=blur;result.font=font;result.reducedMotion=reducedMotion;result.hoverEffects=hoverEffects;
        return result;
    }

    public float effectiveAnimationSpeed(boolean enabled) { return enabled && !reducedMotion ? animationSpeed : 0; }

    private static float clamp(float value, float min, float max, float fallback) {
        return !Float.isFinite(value) ? fallback : Math.max(min, Math.min(max, value));
    }
}
