package dev.alien.client.hud;

import dev.alien.client.config.ClientConfig;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class HudModulesTest {
    @Test public void masterSwitchesSuppressConfiguredButInactiveVisuals() {
        ClientConfig config = new ClientConfig();
        config.cosmetics.enabled = false;
        config.cosmetics.self.aura = true;
        config.effects.enabled = false;
        config.effects.movement = true;
        config.effects.ambient = true;
        config.world.enabled = false;
        config.world.fogEnabled = false;
        config.world.weather = "Server";
        config.performance.diagnostics = false;
        assertTrue(HudModules.active(config).isEmpty());
    }

    @Test public void overlayListsOnlyActualEnabledVisualFeatures() {
        ClientConfig config = new ClientConfig();
        config.cosmetics.enabled = false;
        config.effects.enabled = true;
        config.effects.movement = true;
        config.effects.landing = false;
        config.effects.ambient = true;
        config.effects.shape = "Petal";
        config.world.enabled = true;
        config.world.theme = "Sakura";
        config.world.weather = "Snow";
        config.performance.diagnostics = false;
        List<String> modules = HudModules.active(config);
        assertTrue(modules.contains("Sakura sky"));
        assertTrue(modules.contains("Weather / Snow"));
        assertTrue(modules.contains("Motion particles"));
        assertTrue(modules.contains("Ambient / Petal"));
        assertFalse(modules.contains("Landing particles"));
        config.effects.enabled = false;
        assertFalse(HudModules.active(config).contains("Ambient / Petal"));
    }

    @Test public void disabledSelfCosmeticsDoNotAppearAsActive() {
        ClientConfig config = new ClientConfig();
        config.cosmetics.enabled = true;
        config.cosmetics.self.enabled = false;
        config.cosmetics.self.back = "CAPE";
        assertFalse(HudModules.active(config).contains("Cape"));
        config.cosmetics.self.enabled = true;
        assertTrue(HudModules.active(config).contains("Cape"));
    }
}
