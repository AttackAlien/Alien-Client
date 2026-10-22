package dev.alien.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.gui.theme.ThemeConfig;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Local, bounded theme exchange: no scripts, remote URLs or executable content. */
public final class ThemeLibrary {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File directory;

    public ThemeLibrary(File configDirectory) { directory = new File(configDirectory, "themes"); }

    public void save(String name, ClientConfig config) throws IOException { write(file("", name), new Theme(config)); }
    public void exportTheme(String name, ClientConfig config) throws IOException { write(file("exports", name), new Theme(config)); }
    public void load(String name, ClientConfig config) throws IOException { read(file("", name)).apply(config); }
    public void importTheme(String name, ClientConfig config) throws IOException {
        Theme theme = read(file("imports", name));
        write(file("", name), theme);
        theme.apply(config);
    }

    private File file(String subdirectory, String name) throws IOException {
        if (name == null || !name.matches("[A-Za-z0-9_-]{1,40}")) throw new IOException("Use 1-40 letters, numbers, _ or - for the name.");
        File parent = subdirectory.isEmpty() ? directory : new File(directory, subdirectory);
        File result = new File(parent, name + ".json");
        if (!result.getCanonicalFile().getParentFile().equals(parent.getCanonicalFile())) throw new IOException("Unsafe theme path.");
        for (File path = result; path != null; path = path.getParentFile()) {
            if (Files.isSymbolicLink(path.toPath())) throw new IOException("Theme paths cannot contain symbolic links.");
        }
        Files.createDirectories(parent.toPath());
        return result;
    }

    private static Theme read(File file) throws IOException {
        if (!file.isFile() || file.length() > 65536L) throw new IOException("Theme missing or larger than 64 KiB.");
        try (InputStream stream = new FileInputStream(file); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count;
            while ((count = stream.read(buffer)) != -1) {
                if (bytes.size() + count > 65536) throw new IOException("Theme larger than 64 KiB.");
                bytes.write(buffer, 0, count);
            }
            String json = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            int depth = 0; boolean quoted = false, escaped = false;
            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);
                if (quoted) { if (escaped) escaped = false; else if (c == '\\') escaped = true; else if (c == '"') quoted = false; }
                else if (c == '"') quoted = true;
                else if (c == '{' || c == '[') { if (++depth > 16) throw new IOException("Theme JSON nesting limit is 16."); }
                else if (c == '}' || c == ']') depth--;
            }
            Theme theme = GSON.fromJson(json, Theme.class);
            if (theme == null || theme.version != 1 || !Float.isFinite(theme.opacity) || !Float.isFinite(theme.animationSpeed))
                throw new IOException("Invalid theme or unsupported version.");
            return theme;
        } catch (RuntimeException ex) { throw new IOException("Invalid theme JSON.", ex); }
    }

    private static void write(File target, Theme theme) throws IOException {
        File temporary = File.createTempFile("alien-theme-", ".tmp", target.getParentFile());
        try {
            try (Writer writer = Files.newBufferedWriter(temporary.toPath(), StandardCharsets.UTF_8)) { GSON.toJson(theme, writer); }
            try { Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary.toPath()); }
    }

    private static final class Theme {
        int version = 1;
        int accentColor = 0xffa882ff;
        float opacity = .94f, animationSpeed = 1f;
        boolean animations = true, rainbowAccent;
        ThemeConfig ui;
        Theme() { }
        Theme(ClientConfig config) {
            accentColor = config.accentColor; opacity = config.opacity;
            animations = config.animations; animationSpeed = config.animationSpeed; rainbowAccent = config.rainbowAccent;
            ui = config.ui == null ? new ThemeConfig() : config.ui.copy();
            // The public legacy API still edits these aliases directly.
            if ((ui.primary & 0xffffff) != (config.accentColor & 0xffffff)) ui.primary = config.accentColor;
            ui.opacity = config.opacity;
            ui.animationSpeed = config.animationSpeed;
        }
        void apply(ClientConfig config) {
            config.accentColor = 0xff000000 | accentColor;
            config.opacity = Math.max(.3f, Math.min(1f, opacity));
            config.animationSpeed = Math.max(.25f, Math.min(3f, animationSpeed));
            config.animations = animations; config.rainbowAccent = rainbowAccent;
            if (ui != null) {
                config.ui = ui.copy(); config.ui.normalize();
                config.accentColor = config.ui.primary; config.opacity = config.ui.opacity;
                config.animationSpeed = config.ui.animationSpeed;
            } else {
                // Legacy theme files contain only accent/opacity/motion fields.
                // Keep the existing advanced layout tokens, and adapt those old fields.
                if (config.ui == null) config.ui = new ThemeConfig();
                config.ui.primary = config.accentColor; config.ui.opacity = config.opacity;
                config.ui.animationSpeed = config.animationSpeed;
                config.ui.preset = "Custom RGB"; config.ui.normalize();
            }
            config.theme = "Custom";
        }
    }
}
