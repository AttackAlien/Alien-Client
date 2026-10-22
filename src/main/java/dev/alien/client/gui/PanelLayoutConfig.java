package dev.alien.client.gui;

import java.util.LinkedHashMap;
import java.util.Map;

/** Serializable positions are fractions of the available panel travel range. */
public final class PanelLayoutConfig {
    public static final String[] CATEGORIES = {"HUD", "Cosmetics", "World", "Effects", "Client"};
    public Map<String, Column> columns = new LinkedHashMap<String, Column>();

    public PanelLayoutConfig() { normalize(); }
    public void normalize() {
        if (columns == null) columns = new LinkedHashMap<String, Column>();
        Map<String, Column> repaired = new LinkedHashMap<String, Column>();
        for (String id : CATEGORIES) {
            Column column = columns.get(id);
            if (column == null) column = new Column();
            column.x = finite(column.x) ? Math.max(0, Math.min(1, column.x)) : 0;
            column.y = finite(column.y) ? Math.max(0, Math.min(1, column.y)) : 0;
            repaired.put(id, column);
        }
        columns = repaired;
    }
    public void reset() {
        columns.clear(); normalize();
    }
    private static boolean finite(float n) { return !Float.isNaN(n) && !Float.isInfinite(n); }
    public static final class Column {
        public boolean customPosition;
        public boolean open = true;
        public float x, y;
    }
}
