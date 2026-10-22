package dev.alien.client.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClientConfigTest {
    @Test public void missingSectionsRestored() {
        ClientConfig c = new ClientConfig();
        c.hud = null; c.cosmetics = null; c.world = null; c.effects = null; c.performance = null; c.panels = null;
        c.normalize(); assertNotNull(c.hud); assertNotNull(c.cosmetics); assertNotNull(c.world); assertNotNull(c.effects); assertNotNull(c.performance);
        assertNotNull(c.panels); assertEquals(6, c.panels.columns.size());
    }
    @Test public void invalidNumericSettingsCannotBreakRenderState() {
        ClientConfig c = new ClientConfig(); c.opacity = Float.NaN; c.animationSpeed = Float.POSITIVE_INFINITY;
        c.menuKey = -9; c.accentColor = 0x12abcdef; c.performance.maxParticles = Integer.MAX_VALUE; c.menuLayout = "invalid";
        c.normalize(); assertEquals(.3f, c.opacity, 0); assertEquals(.1f, c.animationSpeed, 0);
        assertEquals(54, c.menuKey); assertEquals(0xffabcdef, c.accentColor); assertEquals(512, c.performance.maxParticles);
        assertEquals("Panels", c.menuLayout);
    }
    @Test(expected=IllegalArgumentException.class) public void unsupportedVersionsAreNotSilentlyDowngraded() {
        ClientConfig c = new ClientConfig(); c.version = 2; c.normalize();
    }
    @Test public void moduleKeysRemainBoundedAndOldProfilesHaveNoAutomaticShortcuts() {
        ClientConfig old = new com.google.gson.Gson().fromJson("{\"version\":1}", ClientConfig.class);
        old.normalize(); assertTrue(old.moduleKeys.isEmpty());
        old.moduleKeys.put("sky", 34); old.moduleKeys.put("effects", -100);
        old.moduleKeys.put("bad/path", 35); old.moduleKeys.put("missing", null);
        old.normalize();
        assertEquals(Integer.valueOf(34), old.moduleKeys.get("sky"));
        assertEquals(Integer.valueOf(0), old.moduleKeys.get("effects"));
        assertFalse(old.moduleKeys.containsKey("bad/path")); assertFalse(old.moduleKeys.containsKey("missing"));
        for (int i = 0; i < 100; i++) old.moduleKeys.put("module_" + i, 20);
        old.normalize(); assertEquals(64, old.moduleKeys.size());
        old.moduleKeys = null; old.normalize(); assertTrue(old.moduleKeys.isEmpty());
    }
}
