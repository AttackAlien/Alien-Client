package dev.alien.client.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClientConfigTest {
    @Test public void missingSectionsRestored() {
        ClientConfig c = new ClientConfig();
        c.hud = null; c.cosmetics = null; c.world = null; c.effects = null; c.performance = null;
        c.normalize(); assertNotNull(c.hud); assertNotNull(c.cosmetics); assertNotNull(c.world); assertNotNull(c.effects); assertNotNull(c.performance);
    }
    @Test public void invalidNumericSettingsCannotBreakRenderState() {
        ClientConfig c = new ClientConfig(); c.opacity = Float.NaN; c.animationSpeed = Float.POSITIVE_INFINITY;
        c.menuKey = -9; c.accentColor = 0x12abcdef; c.performance.maxParticles = Integer.MAX_VALUE;
        c.normalize(); assertEquals(.3f, c.opacity, 0); assertEquals(.1f, c.animationSpeed, 0);
        assertEquals(54, c.menuKey); assertEquals(0xffabcdef, c.accentColor); assertEquals(512, c.performance.maxParticles);
    }
    @Test(expected=IllegalArgumentException.class) public void unsupportedVersionsAreNotSilentlyDowngraded() {
        ClientConfig c = new ClientConfig(); c.version = 2; c.normalize();
    }
}
