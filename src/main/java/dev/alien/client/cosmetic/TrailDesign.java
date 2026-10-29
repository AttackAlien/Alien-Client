package dev.alien.client.cosmetic;

/** Three editable stops from the oldest tail (0) to the newest head (1). */
public final class TrailDesign {
    public static final String[] TEXTURES = {"Soft", "Silk", "Dashes", "Weave"};
    public static final String[] PRESETS = {"Nebula", "Ember", "Glacier"};
    /** Opt-in: old profiles retain their original untextured ribbon. */
    public boolean enabled;
    public String texture = "Soft", customTexture = "", preset = "Custom";
    public float tiling = 1, scroll = .25f;
    public int tailColor = 0x775AB1, middleColor = 0xB79AFF, headColor = 0x36DCC9;
    public float tailWidth = 0, middleWidth = .7f, headWidth = 1;
    public float tailOpacity = 0, middleOpacity = .65f, headOpacity = 1;

    public void normalize() {
        if (!isBuiltin(texture) && !"PNG".equals(texture)) texture = "Soft";
        customTexture = CosmeticConfig.safeName(customTexture, ".png");
        if (!"Custom".equals(preset) && !contains(PRESETS, preset)) preset = "Custom";
        tiling = CosmeticConfig.clamp(tiling, .25f, 8);
        scroll = CosmeticConfig.clamp(scroll, -2, 2);
        tailColor &= 0xffffff; middleColor &= 0xffffff; headColor &= 0xffffff;
        tailWidth = CosmeticConfig.clamp(tailWidth, 0, 1.5f);
        middleWidth = CosmeticConfig.clamp(middleWidth, 0, 1.5f);
        headWidth = CosmeticConfig.clamp(headWidth, 0, 1.5f);
        tailOpacity = CosmeticConfig.clamp(tailOpacity, 0, 1);
        middleOpacity = CosmeticConfig.clamp(middleOpacity, 0, 1);
        headOpacity = CosmeticConfig.clamp(headOpacity, 0, 1);
    }
    public static boolean isBuiltin(String name) { return contains(TEXTURES, name); }
    private static boolean contains(String[] values, String name) {
        for (String value : values) if (value.equals(name)) return true;
        return false;
    }
    private static float unit(float value) { return Float.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0; }
    private static float interpolate(float t, float a, float b, float c) {
        t = unit(t); return t <= .5f ? a + (b-a)*t*2 : b + (c-b)*(t-.5f)*2;
    }
    public float widthAt(float position) { return interpolate(position, tailWidth, middleWidth, headWidth); }
    public float opacityAt(float position) { return interpolate(position, tailOpacity, middleOpacity, headOpacity); }
    public int colorAt(float position) {
        float t = unit(position); int a = t <= .5f ? tailColor : middleColor, b = t <= .5f ? middleColor : headColor;
        float mix = t <= .5f ? t*2 : (t-.5f)*2;
        return (Math.round((a>>16&255)*(1-mix)+(b>>16&255)*mix)<<16)
                | (Math.round((a>>8&255)*(1-mix)+(b>>8&255)*mix)<<8)
                | Math.round((a&255)*(1-mix)+(b&255)*mix);
    }
    public int color(int stop) { return stop == 0 ? tailColor : stop == 1 ? middleColor : headColor; }
    public void color(int stop, int rgb) {
        if (stop == 0) tailColor = rgb & 0xffffff; else if (stop == 1) middleColor = rgb & 0xffffff; else headColor = rgb & 0xffffff;
        preset = "Custom";
    }
    public void point(int stop, boolean opacity, float value) {
        value = CosmeticConfig.clamp(value, 0, opacity ? 1 : 1.5f);
        if (opacity) { if (stop == 0) tailOpacity=value; else if (stop == 1) middleOpacity=value; else headOpacity=value; }
        else { if (stop == 0) tailWidth=value; else if (stop == 1) middleWidth=value; else headWidth=value; }
        preset = "Custom";
    }
    public boolean applyPreset(String name) {
        if (!contains(PRESETS, name)) return false;
        enabled=true; preset=name; customTexture=""; tailWidth=0; middleWidth=.8f; headWidth=.6f;
        tailOpacity=0; middleOpacity=.8f; headOpacity=1; tiling=1.3f; scroll=.3f;
        if ("Nebula".equals(name)) { texture="Silk"; tailColor=0x6438B8; middleColor=0xD18CFF; headColor=0x70F0E0; }
        else if ("Ember".equals(name)) { texture="Dashes"; tailColor=0x8C224C; middleColor=0xFF713D; headColor=0xFFE1A0; middleWidth=1.2f; tiling=2; }
        else { texture="Weave"; tailColor=0x296DB7; middleColor=0x65D9FF; headColor=0xE3FFFF; middleOpacity=.55f; scroll=-.2f; }
        return true;
    }
    public TrailDesign copy() {
        TrailDesign d=new TrailDesign(); d.enabled=enabled; d.texture=texture; d.customTexture=customTexture; d.preset=preset;
        d.tiling=tiling; d.scroll=scroll; d.tailColor=tailColor; d.middleColor=middleColor; d.headColor=headColor;
        d.tailWidth=tailWidth; d.middleWidth=middleWidth; d.headWidth=headWidth;
        d.tailOpacity=tailOpacity; d.middleOpacity=middleOpacity; d.headOpacity=headOpacity; return d;
    }
}
