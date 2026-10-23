package dev.alien.client.combat;

import dev.alien.client.config.ConfigStore;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class CombatSessionGuardTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void savedFullPresetNeedsExplicitMultiplayerConsent() {
        CombatConfig c = new CombatConfig();
        c.applyPreset("FULL");
        CombatSessionGuard guard = new CombatSessionGuard();
        Object world = new Object();
        assertTrue(c.enabled && c.aimAssist && c.clickAssist);
        assertFalse(guard.permits(world, c, false));
        assertTrue(guard.approve(world, c, false));
        assertTrue(guard.permits(world, c, false));
    }

    @Test public void disconnectReconnectAndProfileSwapRevoke() {
        CombatConfig first = new CombatConfig(), second = new CombatConfig();
        CombatSessionGuard guard = new CombatSessionGuard();
        Object a = new Object(), b = new Object();
        assertTrue(guard.approve(a, first, false));
        assertFalse(guard.permits(null, first, false));
        assertFalse(guard.permits(b, first, false));
        assertTrue(guard.approve(b, first, false));
        assertFalse(guard.permits(b, second, false));
        assertFalse(guard.isApproved(b, second));
    }

    @Test public void persistedServerProfileIsNeverConsent() throws IOException {
        ConfigStore store = new ConfigStore(temp.newFolder());
        store.load();
        store.config().combat.applyPreset("FULL");
        store.saveProfile("full");
        store.config().combat.applyPreset("OFF");
        store.save();
        store.bindServer("example.net", "full");
        assertTrue(store.loadServerProfile("example.net"));
        assertTrue(store.config().combat.enabled);
        CombatSessionGuard guard = new CombatSessionGuard();
        Object world = new Object();
        assertFalse(guard.permits(world, store.config().combat, false));
        assertTrue(guard.approve(world, store.config().combat, false));
        store.leaveServerProfile();
        assertFalse(guard.permits(world, store.config().combat, false));
        assertTrue(store.loadServerProfile("example.net"));
        assertFalse(guard.permits(world, store.config().combat, false));
    }

    @Test public void revokeAndLocalWorldRules() {
        CombatSessionGuard guard = new CombatSessionGuard();
        Object w = new Object(), c = new Object();
        assertFalse(guard.approve(null, c, false));
        assertFalse(guard.approve(w, c, true));
        assertTrue(guard.permits(w, c, true));
        assertFalse(guard.permits(w, c, false));
        assertTrue(guard.approve(w, c, false));
        guard.revoke();
        assertFalse(guard.permits(w, c, false));
        assertFalse(guard.permits(null, c, true));
    }
}
