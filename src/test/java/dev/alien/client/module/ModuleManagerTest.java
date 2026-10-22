package dev.alien.client.module;

import dev.alien.client.config.ClientConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModuleManagerTest {
    @Test public void builtInsDescribeOnlyExistingLocalSwitchesInStableOrder() {
        ClientConfig config = new ClientConfig();
        ModuleManager manager = ModuleManager.visuals(() -> config, false);
        assertEquals(Arrays.asList("hud", "cosmetics", "sky", "visual_time", "fog", "weather", "effects", "camera_sway", "color_filter"), ids(manager.modules()));
        for (ModuleDescriptor module : manager.modules()) {
            assertEquals(AccessLevel.SHARED, module.accessLevel());
            assertEquals(GameplayImpact.LOCAL_VISUAL, module.gameplayImpact());
            assertFalse(module.name().isEmpty());
            assertFalse(module.description().isEmpty());
        }
        assertTrue(manager.find("hud").enabled());
        assertFalse(manager.find("sky").enabled());
        assertFalse(manager.find("effects").enabled());
    }

    @Test public void legitRejectsFullBeforeReadingOrChangingModuleState() {
        ModuleManager manager = new ModuleManager(false);
        AtomicInteger calls = new AtomicInteger();
        ModuleDescriptor full = new ModuleDescriptor("full_test", "Full test", ModuleCategory.EFFECTS,
                "Registration boundary test.", GameplayImpact.LOCAL_VISUAL, AccessLevel.FULL,
                () -> { calls.incrementAndGet(); return false; }, value -> calls.incrementAndGet());
        try { manager.register(full); fail("Full module admitted to Legit"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("Full-only")); }
        assertTrue(manager.modules().isEmpty());
        assertNull(manager.find("full_test"));
        assertFalse(manager.toggleKey(30, Collections.singletonMap("full_test", 30)));
        assertEquals(0, calls.get());
    }

    @Test public void fullEditionMayRegisterExplicitlyFullDescriptors() {
        ModuleManager manager = new ModuleManager(true);
        AtomicBoolean value = new AtomicBoolean();
        manager.register(module("full_test", AccessLevel.FULL, value, null));
        assertEquals(AccessLevel.FULL, manager.find("full_test").accessLevel());
        assertTrue(manager.toggle("full_test"));
        assertTrue(value.get());
    }

    @Test public void legitFailsClosedForNonVisualImpactEvenWhenMarkedShared() {
        ModuleManager manager = new ModuleManager(false);
        AtomicInteger calls = new AtomicInteger();
        for (GameplayImpact impact : new GameplayImpact[]{GameplayImpact.GAMEPLAY_CHANGING, GameplayImpact.REVEALS_HIDDEN_INFORMATION}) {
            ModuleDescriptor misclassified = new ModuleDescriptor("invalid_impact", "Invalid impact", ModuleCategory.EFFECTS,
                    "Edition metadata test.", impact, AccessLevel.SHARED,
                    () -> { calls.incrementAndGet(); return false; }, value -> calls.incrementAndGet());
            try { manager.register(misclassified); fail("Non-visual impact admitted to Legit"); }
            catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("local visual")); }
        }
        assertTrue(manager.modules().isEmpty());
        assertEquals(0, calls.get());
    }

    @Test public void fullRegistryCanRepresentAdditionalImpactWithoutImplementingNewFeatures() {
        ModuleManager manager = new ModuleManager(true);
        AtomicBoolean enabled = new AtomicBoolean();
        ModuleDescriptor descriptor = new ModuleDescriptor("future_test", "Future metadata", ModuleCategory.EFFECTS,
                "Metadata boundary test only.", GameplayImpact.REVEALS_HIDDEN_INFORMATION, AccessLevel.FULL,
                enabled::get, enabled::set);
        manager.register(descriptor);
        assertSame(descriptor, manager.find("future_test"));
        assertFalse(enabled.get());
    }

    @Test public void duplicateRegistrationCannotReplaceTheOriginalOrRunActions() {
        ModuleManager manager = new ModuleManager(false);
        AtomicBoolean first = new AtomicBoolean(), duplicate = new AtomicBoolean();
        ModuleDescriptor original = module("same_id", AccessLevel.SHARED, first, null);
        manager.register(original);
        try { manager.register(module("same_id", AccessLevel.SHARED, duplicate, null)); fail(); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("Duplicate")); }
        assertSame(original, manager.find("same_id"));
        assertEquals(1, manager.modules().size());
        assertFalse(first.get()); assertFalse(duplicate.get());
        manager.toggle("same_id");
        assertTrue(first.get()); assertFalse(duplicate.get());
    }

    @Test public void profileReplacementIsSeenByEveryGetterAndSetter() {
        ClientConfig original = new ClientConfig();
        AtomicReference<ClientConfig> active = new AtomicReference<ClientConfig>(original);
        ModuleManager manager = ModuleManager.visuals(active::get, false);
        for (ModuleDescriptor module : manager.modules()) module.setEnabled(false);
        ClientConfig replacement = new ClientConfig();
        replacement.world.weather = "Rain";
        active.set(replacement);
        assertTrue(manager.find("hud").enabled());
        assertTrue(manager.find("cosmetics").enabled());
        assertTrue(manager.find("weather").enabled());
        assertFalse(manager.find("sky").enabled());
        for (ModuleDescriptor module : manager.modules()) {
            module.setEnabled(true);
            assertTrue(module.enabled());
        }
        assertFalse(original.hud.enabled); assertFalse(original.cosmetics.enabled);
        assertFalse(original.world.enabled); assertFalse(original.world.timeOverride); assertFalse(original.world.fogEnabled);
        assertEquals("Server", original.world.weather);
        assertFalse(original.effects.enabled); assertFalse(original.effects.cameraSway); assertFalse(original.effects.colorFilter);
        assertTrue(replacement.hud.enabled); assertTrue(replacement.cosmetics.enabled);
        assertTrue(replacement.world.enabled); assertTrue(replacement.world.timeOverride); assertTrue(replacement.world.fogEnabled);
        assertEquals("Rain", replacement.world.weather);
        assertTrue(replacement.effects.enabled); assertTrue(replacement.effects.cameraSway); assertTrue(replacement.effects.colorFilter);
    }

    @Test public void keyDispatchIgnoresUnboundInvalidUnknownAndNullEntries() {
        ClientConfig config = new ClientConfig();
        ModuleManager manager = ModuleManager.visuals(() -> config, false);
        Map<String, Integer> bindings = new LinkedHashMap<String, Integer>();
        bindings.put("hud", 0); bindings.put("cosmetics", -1); bindings.put("sky", 256);
        bindings.put("effects", null); bindings.put("unregistered", 30); bindings.put(null, 30);
        assertFalse(manager.toggleKey(30, bindings));
        assertFalse(manager.toggleKey(0, bindings));
        assertFalse(manager.toggleKey(-1, bindings));
        assertFalse(manager.toggleKey(256, bindings));
        assertFalse(manager.toggleKey(Integer.MAX_VALUE, bindings));
        assertFalse(manager.toggleKey(30, null));
        assertFalse(manager.toggle("unknown")); assertFalse(manager.toggle(null));
        assertTrue(config.hud.enabled); assertTrue(config.cosmetics.enabled); assertFalse(config.world.enabled);
        assertFalse(config.effects.enabled);
    }

    @Test public void sharedKeyTogglesExactlyOnceEachInRegistrationOrder() {
        ModuleManager manager = new ModuleManager(false);
        AtomicBoolean first = new AtomicBoolean(), second = new AtomicBoolean();
        StringBuilder calls = new StringBuilder();
        manager.register(module("first", AccessLevel.SHARED, first, calls));
        manager.register(module("second", AccessLevel.SHARED, second, calls));
        Map<String, Integer> bindings = new LinkedHashMap<String, Integer>();
        bindings.put("unregistered", 30); bindings.put("second", 30); bindings.put("first", 30);
        assertTrue(manager.toggleKey(30, bindings));
        assertTrue(first.get()); assertTrue(second.get()); assertEquals("first;second;", calls.toString());
        calls.setLength(0);
        assertTrue(manager.toggleKey(30, bindings));
        assertFalse(first.get()); assertFalse(second.get()); assertEquals("first;second;", calls.toString());
    }

    @Test public void boundaryKeysAreValidAndDisabledSwitchesCanBeEnabled() {
        ClientConfig config = new ClientConfig();
        ModuleManager manager = ModuleManager.visuals(() -> config, false);
        assertTrue(manager.toggleKey(1, Collections.singletonMap("sky", 1)));
        assertTrue(config.world.enabled);
        assertTrue(manager.toggleKey(255, Collections.singletonMap("effects", 255)));
        assertTrue(config.effects.enabled);
        assertFalse(config.effects.ambient); assertFalse(config.effects.movement); assertFalse(config.effects.trails);
    }

    @Test public void childSwitchesDoNotEnableTheirMasterOrAnyOtherFeature() {
        ClientConfig config = new ClientConfig();
        ModuleManager manager = ModuleManager.visuals(() -> config, false);
        assertTrue(manager.toggle("camera_sway"));
        assertTrue(config.effects.cameraSway); assertFalse(config.effects.enabled); assertFalse(config.effects.colorFilter);
        assertTrue(manager.toggle("color_filter"));
        assertTrue(config.effects.colorFilter); assertFalse(config.effects.enabled);
        assertFalse(config.effects.ambient); assertFalse(config.effects.movement); assertFalse(config.effects.landing);
        assertFalse(config.world.enabled); assertFalse(config.world.timeOverride); assertFalse(config.world.fogEnabled);
        assertTrue(config.menuReplacement); assertTrue(config.hud.enabled); assertTrue(config.cosmetics.enabled);
        assertEquals("Server", config.world.weather);
    }

    @Test public void weatherUsesExistingRendererChoicesWithoutOtherConfigurationChanges() {
        ClientConfig config = new ClientConfig();
        ModuleManager manager = ModuleManager.visuals(() -> config, false);
        manager.find("weather").setEnabled(true);
        assertEquals("Snow", config.world.weather);
        config.world.weather = "Clear";
        manager.find("weather").setEnabled(true);
        assertEquals("Clear", config.world.weather);
        manager.toggle("weather");
        assertEquals("Server", config.world.weather);
        assertFalse(config.world.enabled); assertFalse(config.world.timeOverride); assertFalse(config.effects.enabled);
    }

    @Test public void queriesAreImmutableOrderedSnapshotsAndSearchIsCaseInsensitive() {
        ClientConfig config = new ClientConfig();
        ModuleManager manager = ModuleManager.visuals(() -> config, false);
        List<ModuleDescriptor> snapshot = manager.modules();
        assertEquals(Arrays.asList("sky", "visual_time", "fog", "weather"), ids(manager.category(ModuleCategory.WORLD)));
        assertEquals(Arrays.asList("camera_sway"), ids(manager.search("  CAMERA SWAY  ")));
        assertEquals(Arrays.asList("visual_time"), ids(manager.search("VISUAL_TIME")));
        assertEquals(ids(snapshot), ids(manager.search(null)));
        assertTrue(manager.search("missing-module").isEmpty());
        assertImmutable(snapshot); assertImmutable(manager.category(ModuleCategory.HUD)); assertImmutable(manager.search("HUD"));
        manager.register(module("later", AccessLevel.SHARED, new AtomicBoolean(), null));
        assertEquals(9, snapshot.size()); assertEquals(10, manager.modules().size());
        assertFalse(config.world.enabled);
    }

    @Test public void descriptorRejectsUnstableIdentifiers() {
        for (String id : new String[]{"", "NotStable", "with space", "../id", null}) {
            try { module(id, AccessLevel.SHARED, new AtomicBoolean(), null); fail("Invalid identifier accepted"); }
            catch (IllegalArgumentException expected) { }
        }
    }

    private static ModuleDescriptor module(String id, AccessLevel access, AtomicBoolean value, StringBuilder calls) {
        return new ModuleDescriptor(id, "Test module", ModuleCategory.EFFECTS, "A test switch.",
                GameplayImpact.LOCAL_VISUAL, access, value::get, enabled -> {
                    value.set(enabled);
                    if (calls != null) calls.append(id).append(';');
                });
    }

    private static List<String> ids(List<ModuleDescriptor> modules) {
        List<String> result = new java.util.ArrayList<String>();
        for (ModuleDescriptor module : modules) result.add(module.id());
        return result;
    }

    private static void assertImmutable(List<ModuleDescriptor> modules) {
        try { modules.clear(); fail("Mutable query result"); }
        catch (UnsupportedOperationException expected) { }
    }
}
