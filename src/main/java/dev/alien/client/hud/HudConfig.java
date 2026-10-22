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
        background = false;
    }

    private void place(String id, float x, float y) {
        Widget widget = widgets.get(id);
        widget.enabled = true; widget.x = x; widget.y = y; widget.scale = 1f;
    }

    private void add(String key, boolean enabled, float x, float y) {
        if (widgets.get(key) == null) widgets.put(key, new Widget(enabled, x, y, 1f));
    }

    public static String label(String id) {
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
        public Widget() { }
        public Widget(boolean enabled, float x, float y, float scale) {
            this.enabled = enabled; this.x = x; this.y = y; this.scale = scale;
        }
        public void sanitize() {
            x = finite(x) ? Math.max(0, Math.min(1, x)) : .02f;
            y = finite(y) ? Math.max(0, Math.min(1, y)) : .04f;
            scale = finite(scale) ? Math.max(.5f, Math.min(3f, scale)) : 1f;
        }
        private static boolean finite(float n) { return !Float.isNaN(n) && !Float.isInfinite(n); }
    }
}
