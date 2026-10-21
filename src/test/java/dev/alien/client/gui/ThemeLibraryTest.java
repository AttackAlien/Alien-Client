package dev.alien.client.gui;

import dev.alien.client.config.ClientConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ThemeLibraryTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Test public void savesAndLoadsVisualSettingsWithoutChangingGameplayModules() throws IOException {
        ThemeLibrary library = new ThemeLibrary(folder.getRoot());
        ClientConfig source = new ClientConfig();
        source.accentColor = 0xff123456; source.opacity = .71f; source.animationSpeed = 1.75f;
        source.rainbowAccent = true; source.animations = false;
        library.save("my_theme", source);
        ClientConfig destination = new ClientConfig();
        destination.menuKey = 88;
        library.load("my_theme", destination);
        assertEquals(0xff123456, destination.accentColor);
        assertEquals(.71f, destination.opacity, .0001f);
        assertEquals(1.75f, destination.animationSpeed, .0001f);
        assertTrue(destination.rainbowAccent); assertFalse(destination.animations);
        assertEquals(88, destination.menuKey);
    }

    @Test public void importsAndClampsUntrustedThemeFields() throws IOException {
        write("themes/imports/shared.json", "{\"version\":1,\"accentColor\":12,\"opacity\":-20,\"animationSpeed\":400}");
        ClientConfig config = new ClientConfig();
        new ThemeLibrary(folder.getRoot()).importTheme("shared", config);
        assertEquals(.3f, config.opacity, 0f);
        assertEquals(3f, config.animationSpeed, 0f);
        assertEquals(0xff00000c, config.accentColor);
        assertTrue(new File(folder.getRoot(), "themes/shared.json").isFile());
    }

    @Test public void exportsCanBeReadByAnotherThemeLibrary() throws IOException {
        ClientConfig config = new ClientConfig(); config.accentColor = 0xffabcdef;
        ThemeLibrary library = new ThemeLibrary(folder.getRoot());
        library.exportTheme("share", config);
        File exported = new File(folder.getRoot(), "themes/exports/share.json");
        assertTrue(exported.isFile());
        File imports = new File(folder.getRoot(), "themes/imports"); Files.createDirectories(imports.toPath());
        Files.copy(exported.toPath(), new File(imports, "copied.json").toPath());
        ClientConfig loaded = new ClientConfig(); library.importTheme("copied", loaded);
        assertEquals(config.accentColor, loaded.accentColor);
    }

    @Test public void rejectsTraversalAndDoesNotWriteOutsideLibrary() throws IOException {
        ThemeLibrary library = new ThemeLibrary(folder.getRoot());
        for (String unsafe : new String[] {"../outside", "a/b", "a\\b", "", "/tmp/alien", "a.json"}) {
            try { library.save(unsafe, new ClientConfig()); fail("Accepted unsafe name: " + unsafe); }
            catch (IOException expected) { }
        }
        assertFalse(new File(folder.getRoot(), "outside.json").exists());
    }

    @Test public void invalidOrUnsupportedThemeDoesNotMutateConfiguration() throws IOException {
        ThemeLibrary library = new ThemeLibrary(folder.getRoot());
        ClientConfig config = new ClientConfig(); config.opacity = .8f;
        for (String json : new String[] {"{broken", "null", "{\"version\":42}", "{\"opacity\":\"NaN\"}"}) {
            write("themes/bad.json", json);
            try { library.load("bad", config); fail("Accepted invalid theme: " + json); }
            catch (IOException expected) { }
            assertEquals(.8f, config.opacity, 0f);
        }
    }

    @Test public void rejectsOversizeAndDeeplyNestedJson() throws IOException {
        ThemeLibrary library = new ThemeLibrary(folder.getRoot());
        write("themes/large.json", new String(new char[65537]).replace('\0', ' '));
        try { library.load("large", new ClientConfig()); fail("Accepted oversized theme"); }
        catch (IOException expected) { }
        StringBuilder deep = new StringBuilder("{\"ignored\":");
        for (int i = 0; i < 20; i++) deep.append('[');
        deep.append('0');
        for (int i = 0; i < 20; i++) deep.append(']');
        deep.append('}'); write("themes/deep.json", deep.toString());
        try { library.load("deep", new ClientConfig()); fail("Accepted deeply nested theme"); }
        catch (IOException expected) { }
    }

    @Test public void rejectsSymbolicLinkDirectoryAndFile() throws IOException {
        File outside = folder.newFolder("outside");
        Files.createSymbolicLink(new File(folder.getRoot(), "themes").toPath(), outside.toPath());
        ThemeLibrary library = new ThemeLibrary(folder.getRoot());
        try { library.save("escape", new ClientConfig()); fail("Followed directory symlink"); }
        catch (IOException expected) { }
        assertFalse(new File(outside, "escape.json").exists());
        Files.delete(new File(folder.getRoot(), "themes").toPath());
        write("outside/target.json", "{}");
        File themes = new File(folder.getRoot(), "themes"); Files.createDirectories(themes.toPath());
        Files.createSymbolicLink(new File(themes, "escape.json").toPath(), new File(outside, "target.json").toPath());
        try { library.load("escape", new ClientConfig()); fail("Followed file symlink"); }
        catch (IOException expected) { }
    }

    private void write(String name, String contents) throws IOException {
        File file = new File(folder.getRoot(), name);
        Files.createDirectories(file.getParentFile().toPath());
        Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
    }
}
