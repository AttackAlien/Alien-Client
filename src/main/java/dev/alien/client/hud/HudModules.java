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
        if(config.scenes.enabled){if(config.scenes.skyIndex()>0)labels.add("Living sky / "+config.scenes.sky);if(!"Natural".equals(config.scenes.mode))labels.add("Scene / "+config.scenes.mode);if(config.scenes.shockwave)labels.add("Shockwave");if(config.scenes.cute)labels.add("Cute World");if(config.scenes.mist)labels.add("Mist");if(config.scenes.afterglow)labels.add("Afterglow");if(config.scenes.radiance)labels.add("Radiance");if(config.scenes.targetEsp)labels.add("Target ESP");if(config.scenes.bokeh)labels.add("Bokeh");}
        if(config.studio.handMovement)labels.add("Hand Movement");if(config.studio.nameTags)labels.add("Name Tags");if(config.studio.zoom)labels.add("Zoom / hold C");
        if (config.performance.diagnostics) labels.add("Performance metrics");
        if(config.combat.enabled){
            if(config.combat.aimAssist)labels.add("Aim assist");if(config.combat.clickAssist)labels.add("Click assist");
            if(config.combat.tools.aimbot)labels.add("Aimbot");if(config.combat.tools.autoFood)labels.add("Auto Food");
            if(config.combat.tools.autoPotion)labels.add("Auto Potion");if(config.combat.tools.antiAim)labels.add("Anti Aim / spin");
            if(config.combat.tools.bridge)labels.add("Auto Bridge");if(config.combat.tools.autoSprint)labels.add("Auto Sprint");
        }
        return labels;
    }

    private static String title(String text) {
        return text.isEmpty() ? text : text.substring(0, 1) + text.substring(1).toLowerCase(java.util.Locale.ROOT);
    }
}
