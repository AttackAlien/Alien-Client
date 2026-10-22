package dev.alien.client.gui;

import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class PanelLayoutConfigTest {
    @Test public void normalizationRepairsMissingColumnsAndClampsDraggedPositions() {
        PanelLayoutConfig config = new Gson().fromJson("{\"columns\":{\"HUD\":{\"customPosition\":true,\"open\":false,\"x\":2,\"y\":-1},\"World\":null,\"unknown\":{}}}", PanelLayoutConfig.class);
        config.normalize();
        assertEquals(6, config.columns.size());
        assertEquals(1f, config.columns.get("HUD").x, 0f);
        assertEquals(0f, config.columns.get("HUD").y, 0f);
        assertFalse(config.columns.get("HUD").open);
        assertTrue(config.columns.get("HUD").customPosition);
        assertTrue(config.columns.get("World").open);
        assertFalse(config.columns.containsKey("unknown"));
    }

    @Test public void normalizationRepairsNullMapAndNonFiniteCoordinates() {
        PanelLayoutConfig config = new PanelLayoutConfig();
        config.columns = null;
        config.normalize();
        PanelLayoutConfig.Column column = config.columns.get("Cosmetics");
        column.x = Float.NaN; column.y = Float.POSITIVE_INFINITY;
        config.normalize();
        assertSame(column, config.columns.get("Cosmetics"));
        assertEquals(0f, column.x, 0f);
        assertEquals(0f, column.y, 0f);
    }

    @Test public void savedPositionsSurviveSerializationAndResetRestoresAutomaticLayout() {
        PanelLayoutConfig config = new PanelLayoutConfig();
        PanelLayoutConfig.Column column = config.columns.get("Effects");
        column.customPosition = true; column.open = false; column.x = .8f; column.y = .2f;
        Gson gson = new Gson();
        PanelLayoutConfig restored = gson.fromJson(gson.toJson(config), PanelLayoutConfig.class);
        restored.normalize();
        assertEquals(.8f, restored.columns.get("Effects").x, 0f);
        assertEquals(.2f, restored.columns.get("Effects").y, 0f);
        assertFalse(restored.columns.get("Effects").open);
        restored.reset();
        assertFalse(restored.columns.get("Effects").customPosition);
        assertTrue(restored.columns.get("Effects").open);
    }
}
