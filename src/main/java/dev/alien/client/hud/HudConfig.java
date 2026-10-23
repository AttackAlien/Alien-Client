package dev.alien.client.hud;

import java.util.LinkedHashMap;
import java.util.Map;

/** Resolution-independent HUD layout. All coordinates are fractions of the screen. */
public final class HudConfig {
    public boolean enabled = true;
    public boolean background = true;
    public boolean hideWithDebug = true;
    public boolean snapToGrid = true;
    public int gridSize = 8;
    public boolean showGrid = true;
    public float opacity = .88f;
    public boolean modernFont = true;
    public boolean gradient = true;
    public boolean borders = true;
    public boolean shadows = true;
    public String preset="Custom",theme="Inherit";
    public CrosshairConfig crosshair=new CrosshairConfig();
    public float targetHold=3,targetSmoothing=10,targetRange=32;
    public boolean targetAvatar=true,mouseKeys=true,sprintKey=true;
    public boolean cpsLeft=true,cpsRight=true,armorPercent=true;
    public boolean indicatorHealth=true,indicatorSpeed=true,indicatorEffects=true,indicatorFacing=true;
    public String customText = "Alien Client";
    public Map<String, Widget> widgets = new LinkedHashMap<String, Widget>();

    public HudConfig() { ensureDefaults(); }

    public void ensureDefaults() {
        boolean fresh = widgets == null || widgets.isEmpty();
        if (widgets == null) widgets = new LinkedHashMap<String, Widget>();
        // Existing layouts keep their widgets and opt into the new overlays in the editor.
        add("brand", fresh, .02f, .04f);
        add("modules", fresh, .02f, .14f);
        add("fps", !fresh, .02f, .04f);
        add("cps", !fresh, .02f, .14f);
        add("keys", !fresh, .02f, .25f);
        add("coordinates", true, .02f, fresh ? .80f : .61f);
        add("ping", true, .80f, .04f);
        add("clock", true, .80f, .14f);
        add("inventory", false, .35f, .75f);
        add("armor", true, .80f, .26f);
        add("performance", false, .35f, .04f);
        add("text", false, .45f, .15f);
        add("target", false, .62f, .58f);
        add("crosshair", false, .5f, .5f);
        add("indicators", false, .02f, .70f);
        if(crosshair==null)crosshair=new CrosshairConfig();crosshair.normalize();
        targetHold=HudMath.clamp(targetHold,.5f,8,3);
        targetSmoothing=HudMath.clamp(targetSmoothing,.1f,30,10);
        targetRange=HudMath.clamp(targetRange,3,64,32);
        if(!cpsLeft&&!cpsRight)cpsLeft=true;
        if(!"Inherit".equals(theme)&&!"Midnight".equals(theme)&&!"Cyan".equals(theme)&&!"Light".equals(theme))theme="Inherit";
        if(!"Custom".equals(preset)&&!"PvP".equals(preset)&&!"Survival".equals(preset)&&!"Cinematic".equals(preset))preset="Custom";
        gridSize = Math.max(1, Math.min(32, gridSize));
        opacity = Float.isFinite(opacity) ? Math.max(.15f, Math.min(1, opacity)) : .88f;
        if (customText == null) customText = "Alien Client";
        if (customText.length() > 100) customText = customText.substring(0, 100);
        for (Widget widget : widgets.values()) if (widget != null) widget.sanitize();
    }

    /** Compact layout with live FPS and only enabled visual features. Old widgets remain editable. */
    public void applyReferencePreset() {
        ensureDefaults();
        for (Widget widget : widgets.values()) if (widget != null) widget.enabled = false;
        place("brand", .02f, .04f);
        place("modules", .02f, .14f);
        place("coordinates", .02f, .80f);
        enabled = true;
        preset = "Custom";
        background = false;
    }

    /** HUD-only presets: never change combat, performance or world settings. */
    public void applyPreset(String name) {
        if(!"PvP".equals(name)&&!"Survival".equals(name)&&!"Cinematic".equals(name))throw new IllegalArgumentException("Unknown HUD preset");
        ensureDefaults();
        for(Widget widget:widgets.values())if(widget!=null)widget.enabled=false;
        enabled=true;background=true;opacity=.88f;modernFont=true;
        place("brand",.02f,.03f);
        if("PvP".equals(name)) {
            place("target",.61f,.57f);place("keys",.02f,.30f);place("cps",.02f,.15f);
            place("armor",.82f,.19f);place("ping",.82f,.03f);place("crosshair",.5f,.5f);
            widgets.get("keys").scale=.85f;widgets.get("cps").scale=.85f;widgets.get("armor").scale=.85f;
            crosshair.shape="Plus";crosshair.centered=true;
        } else if("Survival".equals(name)) {
            place("coordinates",.02f,.88f);place("armor",.80f,.25f);
            place("indicators",.02f,.58f);place("clock",.80f,.03f);place("target",.60f,.62f);
        } else {place("coordinates",.02f,.88f);background=false;widgets.get("brand").opacity=.65f;}
        preset=name;
    }

    private void place(String id, float x, float y) {
        Widget widget = widgets.get(id);
        widget.enabled = true; widget.x = x; widget.y = y; widget.scale = 1f;
    }

    private void add(String key, boolean enabled, float x, float y) {
        if (widgets.get(key) == null) widgets.put(key, new Widget(enabled, x, y, 1f));
    }

    public static String label(String id) {
        if ("target".equals(id)) return "Target HUD";
        if ("crosshair".equals(id)) return "Custom Crosshair";
        if ("indicators".equals(id)) return "Indicators";
        if ("brand".equals(id)) return "Alien / live FPS";
        if ("modules".equals(id)) return "Active visuals";
        if ("fps".equals(id)) return "FPS";
        if ("cps".equals(id)) return "CPS (left / right)";
        if ("keys".equals(id)) return "Keystrokes";
        if ("coordinates".equals(id)) return "Coordinates";
        if ("ping".equals(id)) return "Ping";
        if ("clock".equals(id)) return "Clock";
        if ("inventory".equals(id)) return "Inventory";
        if ("armor".equals(id)) return "Armor";
        if ("performance".equals(id)) return "Performance";
        return "Custom text";
    }

    public static final class Widget {
        public boolean enabled = true;
        public float x;
        public float y;
        public float scale = 1f;
        public float opacity=1f;
        public boolean customColor;
        public int color=0xffa98bff;
        public Widget() { }
        public Widget(boolean enabled, float x, float y, float scale) {
            this.enabled = enabled; this.x = x; this.y = y; this.scale = scale;
        }
        public void sanitize() {
            opacity=HudMath.clamp(opacity,.15f,1,1);
            x = finite(x) ? Math.max(0, Math.min(1, x)) : .02f;
            y = finite(y) ? Math.max(0, Math.min(1, y)) : .04f;
            scale = finite(scale) ? Math.max(.5f, Math.min(3f, scale)) : 1f;
        }
        private static boolean finite(float n) { return !Float.isNaN(n) && !Float.isInfinite(n); }
    }
}
