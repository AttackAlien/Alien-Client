package dev.alien.client.gui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Small persistent view preferences, independent of module implementations. */
public final class InterfacePreferences {
    public List<String> favorites = new ArrayList<String>();
    public String selectedCategory = "Discover";
    public boolean activeOnly;
    public boolean favoritesOnly;

    public void normalize() {
        Set<String> unique = new LinkedHashSet<String>();
        if (favorites != null) for (String id : favorites) {
            if (unique.size() >= 64) break;
            if (id != null && id.matches("[a-z][a-z0-9_]{0,47}")) unique.add(id);
        }
        favorites = new ArrayList<String>(unique);
        if (selectedCategory == null || selectedCategory.length() > 32) selectedCategory = "Discover";
    }
}
