package dev.alien.client.hud;

import dev.alien.client.config.ClientConfig;
import dev.alien.client.cosmetic.CosmeticConfig;
import java.util.ArrayList;
import java.util.List;

/** The overlay reflects real local visual settings, including their master switches. */
public final class HudModules {
    private HudModules() { }

    public static List<String> active(ClientConfig config) {
        List<String> labels = new ArrayList<String>();
        if (config.cosmetics.enabled && config.cosmetics.self.enabled) {
            CosmeticConfig.Accessory self = config.cosmetics.self;
            if (!"NONE".equals(self.hat)) labels.add("Hat / " + title(self.hat));
            if (!"NONE".equals(self.face)) labels.add("Mask / " + title(self.face));
            if (!"NONE".equals(self.back)) labels.add(title(self.back));
            if (self.aura) labels.add("Cosmetic aura");
            if (self.trail) labels.add("Cosmetic trail");
        }
        if (config.world.enabled) labels.add(config.world.theme + " sky");
        if (config.world.fogEnabled) labels.add("Atmospheric fog");
        if (!"Server".equals(config.world.weather)) labels.add("Weather / " + config.world.weather);
        if (config.effects.enabled) {
            if (config.effects.movement) labels.add("Motion particles");
            if (config.effects.landing) labels.add("Landing particles");
            if (config.effects.trails) labels.add("Particle trails");
            if (config.effects.ambient) labels.add("Ambient / " + config.effects.shape);
            if (config.effects.colorFilter) labels.add("Color grading");
            if (config.effects.cameraSway) labels.add("Camera sway");
        }
        if (config.performance.diagnostics) labels.add("Performance metrics");
        return labels;
    }

    private static String title(String text) {
        return text.isEmpty() ? text : text.substring(0, 1) + text.substring(1).toLowerCase(java.util.Locale.ROOT);
    }
}
