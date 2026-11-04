package dev.alien.client.hud;

import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class HudConfigTest {
    private final Gson gson = new Gson();

    @Test public void defaultLayoutContainsEveryInformationalWidget() {
        HudConfig config = new HudConfig();
        assertEquals(15, config.widgets.size());
        for (String key : new String[] {"brand", "modules", "fps", "cps", "keys", "coordinates", "ping", "clock", "inventory", "armor", "performance", "text"}) {
            assertNotNull(key, config.widgets.get(key));
        }
        assertTrue(config.widgets.get("brand").enabled);
        assertTrue(config.widgets.get("modules").enabled);
        assertFalse(config.widgets.get("fps").enabled);
        assertFalse(config.widgets.get("inventory").enabled);
    }

    @Test public void missingMapInImportedProfileRestoresUsableDefaults() {
        HudConfig config = gson.fromJson("{\"widgets\":null}", HudConfig.class);
        config.ensureDefaults();
        assertEquals(15, config.widgets.size());
        assertNotNull(config.widgets.get("fps"));
    }

    @Test public void partialMapAndNullKnownWidgetsAreRepairedWithoutDiscardingEdits() {
        HudConfig config = gson.fromJson("{\"widgets\":{\"fps\":null,\"clock\":{\"enabled\":false,\"x\":0.25,\"y\":0.75,\"scale\":1.5}}}", HudConfig.class);
        config.ensureDefaults();
        assertNotNull(config.widgets.get("fps"));
        assertFalse(config.widgets.get("clock").enabled);
        assertEquals(.25f, config.widgets.get("clock").x, .0001f);
        assertEquals(1.5f, config.widgets.get("clock").scale, .0001f);
        assertEquals(15, config.widgets.size());
    }

    @Test public void nonFiniteTransformsCannotPoisonRenderCoordinates() {
        HudConfig config = new HudConfig();
        HudConfig.Widget widget = config.widgets.get("fps");
        widget.x = Float.NaN;
        widget.y = Float.POSITIVE_INFINITY;
        widget.scale = Float.NEGATIVE_INFINITY;
        config.ensureDefaults();
        assertEquals(.02f, widget.x, 0f);
        assertEquals(.04f, widget.y, 0f);
        assertEquals(1f, widget.scale, 0f);
    }

    @Test public void finiteTransformsAreClampedToSafeEditorRanges() {
        HudConfig config = new HudConfig();
        HudConfig.Widget widget = config.widgets.get("fps");
        widget.x = 42; widget.y = -3; widget.scale = 50;
        config.ensureDefaults();
        assertEquals(1f, widget.x, 0f);
        assertEquals(0f, widget.y, 0f);
        assertEquals(3f, widget.scale, 0f);
        widget.scale = -1; config.ensureDefaults();
        assertEquals(.5f, widget.scale, 0f);
    }

    @Test public void importedTextAndGridValuesHaveBoundedDefaults() {
        HudConfig config = new HudConfig();
        config.customText = null; config.gridSize = 0;
        config.ensureDefaults();
        assertEquals("Alien Client", config.customText);
        assertEquals(1, config.gridSize);
        config.customText = new String(new char[120]).replace('\0', 'a');
        config.gridSize = 100; config.ensureDefaults();
        assertEquals(100, config.customText.length());
        assertEquals(32, config.gridSize);
    }

    @Test public void unknownFutureWidgetEntriesDoNotBreakKnownDefaults() {
        HudConfig config = gson.fromJson("{\"widgets\":{\"future_widget\":null,\"future_widget_2\":{\"enabled\":true,\"x\":99,\"scale\":-2}}}", HudConfig.class);
        config.ensureDefaults();
        assertEquals(17, config.widgets.size());
        assertNotNull(config.widgets.get("fps"));
        assertNull(config.widgets.get("future_widget"));
        assertEquals(1f, config.widgets.get("future_widget_2").x, 0f);
        assertEquals(.5f, config.widgets.get("future_widget_2").scale, 0f);
    }

    @Test public void serializationRoundTripPreservesLayoutAndPreferences() {
        HudConfig config = new HudConfig();
        config.enabled = false; config.background = false; config.snapToGrid = false;
        config.customText = "Custom \"quoted\" text";
        config.widgets.get("clock").x = .73f;
        config.widgets.get("clock").scale = 2.2f;
        HudConfig restored = gson.fromJson(gson.toJson(config), HudConfig.class);
        restored.ensureDefaults();
        assertFalse(restored.enabled);
        assertFalse(restored.background);
        assertFalse(restored.snapToGrid);
        assertEquals(config.customText, restored.customText);
        assertEquals(.73f, restored.widgets.get("clock").x, .0001f);
        assertEquals(2.2f, restored.widgets.get("clock").scale, .0001f);
    }

    @Test public void normalizationIsIdempotentAndKeepsWidgetIdentity() {
        HudConfig config = new HudConfig();
        HudConfig.Widget edited = config.widgets.get("fps");
        edited.x = .4f; edited.scale = 1.8f;
        config.ensureDefaults();
        String before = gson.toJson(config);
        config.ensureDefaults();
        assertEquals(before, gson.toJson(config));
        assertSame(edited, config.widgets.get("fps"));
    }

    @Test public void legacyLayoutsKeepExistingOverlaysAndOptIntoNewWidgets() {
        HudConfig config = gson.fromJson("{\"widgets\":{\"fps\":{\"enabled\":true,\"x\":0.4,\"y\":0.7,\"scale\":1.5}}}", HudConfig.class);
        config.ensureDefaults();
        assertTrue(config.widgets.get("fps").enabled);
        assertEquals(.4f, config.widgets.get("fps").x, 0f);
        assertEquals(1.5f, config.widgets.get("fps").scale, 0f);
        assertFalse(config.widgets.get("brand").enabled);
        assertFalse(config.widgets.get("modules").enabled);
    }

    @Test public void compactPresetRemovesRedundantStatsAndRestoresUsableBounds() {
        HudConfig config = new HudConfig();
        config.enabled = false;
        config.widgets.get("brand").scale = 3;
        config.widgets.get("fps").enabled = true;
        config.applyReferencePreset();
        assertTrue(config.enabled);
        assertFalse(config.background);
        assertTrue(config.widgets.get("brand").enabled);
        assertTrue(config.widgets.get("modules").enabled);
        assertTrue(config.widgets.get("coordinates").enabled);
        assertEquals(1f, config.widgets.get("brand").scale, 0f);
        int enabled = 0;
        for (HudConfig.Widget widget : config.widgets.values()) if (widget.enabled) enabled++;
        assertEquals(3, enabled);
    }
}
