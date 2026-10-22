package dev.alien.client.config;

import dev.alien.client.cosmetic.CosmeticConfig;
import dev.alien.client.hud.HudConfig;
import dev.alien.client.performance.PerformanceConfig;
import dev.alien.client.gui.PanelLayoutConfig;
import dev.alien.client.world.EffectsConfig;
import dev.alien.client.world.WorldConfig;
import java.util.LinkedHashMap;
import java.util.Map;

/** Versioned, client-only preferences. No server state belongs here. */
public final class ClientConfig {
    public int version = 1;
    public boolean menuReplacement = true;
    public boolean animations = true;
    public boolean rainbowAccent = false;
    public boolean perServerProfiles = true;
    public float animationSpeed = 1f;
    public float opacity = .94f;
    public int accentColor = 0xffa882ff;
    public int menuKey = 54;
    public Map<String, Integer> moduleKeys = new LinkedHashMap<String, Integer>();
    public String theme = "Nebula";
    public String menuLayout = "Panels";
    public PanelLayoutConfig panels = new PanelLayoutConfig();
    public HudConfig hud = new HudConfig();
    public CosmeticConfig cosmetics = new CosmeticConfig();
    public WorldConfig world = new WorldConfig();
    public EffectsConfig effects = new EffectsConfig();
    public PerformanceConfig performance = new PerformanceConfig();

    public void normalize() {
        if (version != 1) throw new IllegalArgumentException("Unsupported config version: " + version);
        animationSpeed = clamp(animationSpeed, .1f, 3f);
        opacity = clamp(opacity, .3f, 1f);
        if (menuKey < 0 || menuKey > 255) menuKey = 54;
        Map<String, Integer> validKeys = new LinkedHashMap<String, Integer>();
        if (moduleKeys != null) for (Map.Entry<String, Integer> entry : moduleKeys.entrySet()) {
            if (validKeys.size() == 64) break;
            if (entry.getKey() == null || !entry.getKey().matches("[a-z][a-z0-9_]{0,47}") || entry.getValue() == null) continue;
            int key = entry.getValue();
            validKeys.put(entry.getKey(), key >= 0 && key <= 255 ? key : 0);
        }
        moduleKeys = validKeys;
        accentColor |= 0xff000000;
        if (theme == null || theme.length() > 64) theme = "Nebula";
        if (!"Panels".equals(menuLayout) && !"Studio".equals(menuLayout)) menuLayout = "Panels";
        if (panels == null) panels = new PanelLayoutConfig();
        panels.normalize();
        if (hud == null) hud = new HudConfig();
        if (cosmetics == null) cosmetics = new CosmeticConfig();
        if (world == null) world = new WorldConfig();
        if (effects == null) effects = new EffectsConfig();
        if (performance == null) performance = new PerformanceConfig();
        hud.ensureDefaults();
        cosmetics.normalize();
        world.normalize();
        effects.normalize();
        performance.normalize();
    }

    public static float clamp(float value, float min, float max) {
        return Float.isNaN(value) || Float.isInfinite(value) ? min : Math.max(min, Math.min(max, value));
    }
}
