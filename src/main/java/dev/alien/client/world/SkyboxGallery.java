package dev.alien.client.world;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Safe bounded index of local skyboxes, with no network access or implicit import. */
public final class SkyboxGallery {
    private static final int MAX_RESULTS = 64;
    private SkyboxGallery() { }

    public static List<String> list(File directory) {
        if (directory == null || !directory.isDirectory() || Files.isSymbolicLink(directory.toPath()))
            return Collections.emptyList();
        File[] candidates = directory.listFiles();
        if (candidates == null) return Collections.emptyList();
        List<String> names = new ArrayList<String>();
        for (File candidate : candidates) {
            try {
                if (!candidate.isFile() || Files.isSymbolicLink(candidate.toPath())
                        || candidate.length() < 1 || candidate.length() > 16L * 1024L * 1024L)
                    continue;
                File validated = SkyboxLoader.resolve(directory, candidate.getName());
                if (validated != null && validated.isFile() && validated.equals(candidate.getCanonicalFile()))
                    names.add(candidate.getName());
            } catch (IOException ignored) {
                // Malformed or out-of-root names must never appear in the picker.
            }
        }
        Collections.sort(names, new Comparator<String>() {
            @Override public int compare(String a, String b) {
                int folded = a.compareToIgnoreCase(b);
                return folded != 0 ? folded : a.compareTo(b);
            }
        });
        if (names.size() > MAX_RESULTS) return new ArrayList<String>(names.subList(0, MAX_RESULTS));
        return names;
    }

    public static boolean validFavorite(String name) {
        return name != null && name.length() <= 120 && !name.contains("..")
                && name.matches("[A-Za-z0-9_ .-]+\\.png");
    }

    public static void toggleFavorite(WorldConfig config, String name) {
        if (!validFavorite(name)) return;
        config.normalize();
        if (config.favoriteSkyboxes.remove(name)) return;
        if (config.favoriteSkyboxes.size() >= 16) config.favoriteSkyboxes.remove(0);
        config.favoriteSkyboxes.add(name);
    }

    public static boolean isFavorite(WorldConfig config, String name) {
        return config.favoriteSkyboxes != null && config.favoriteSkyboxes.contains(name);
    }
}
