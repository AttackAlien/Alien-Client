package dev.alien.client.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ConfigStoreTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private ConfigStore store;
    @Before public void setup() throws IOException { store = new ConfigStore(temp.newFolder("config")); store.load(); }

    @Test public void roundTripAllSectionsAndColors() throws IOException {
        store.config().theme = "My theme";
        store.config().accentColor = 0xff123456;
        store.config().performance.maxParticles = 42;
        store.save();
        ConfigStore reloaded = new ConfigStore(store.directory()); reloaded.load();
        assertEquals("My theme", reloaded.config().theme);
        assertEquals(0xff123456, reloaded.config().accentColor);
        assertEquals(42, reloaded.config().performance.maxParticles);
        assertNotNull(reloaded.config().hud);
        assertNotNull(reloaded.config().cosmetics);
        assertNotNull(reloaded.config().world);
        assertNotNull(reloaded.config().effects);
    }
    @Test public void profileLifecycle() throws IOException {
        store.config().theme = "Saved"; store.saveProfile("balanced_1");
        store.config().theme = "Changed";
        assertTrue(store.loadProfile("balanced_1"));
        assertEquals("Saved", store.config().theme);
        assertEquals("balanced_1", store.listProfiles().get(0));
        store.deleteProfile("balanced_1"); assertTrue(store.listProfiles().isEmpty());
        assertFalse(store.loadProfile("balanced_1"));
    }
    @Test public void validatesBeforeReplacingActiveConfig() throws IOException {
        store.config().theme = "Keep";
        write(new File(store.directory(), "profiles/broken.json"), "{\"version\":99}");
        try { store.loadProfile("broken"); fail("Unsupported version accepted"); } catch (IOException expected) { }
        assertEquals("Keep", store.config().theme);
    }
    @Test public void rejectsTraversalNames() throws IOException {
        for (String bad : new String[]{"../escape", "/tmp/evil", "..", "x/y", "", "with space"}) {
            try { store.saveProfile(bad); fail("Accepted " + bad); } catch (IOException expected) { }
        }
    }
    @Test public void importedProfilesAreBoundedAndNeverOverwrite() throws IOException {
        File imported = new File(store.directory(), "imports/night.json");
        write(imported, "{\"theme\":\"Night\",\"opacity\":9,\"world\":null}");
        assertTrue(store.importProfile("night.json"));
        assertTrue(store.loadProfile("night")); assertEquals(1f, store.config().opacity, 0);
        assertNotNull(store.config().world);
        try { store.importProfile("night"); fail("Overwrote profile"); } catch (IOException expected) { }
        File exported = store.exportProfile("night"); assertTrue(exported.isFile());
    }
    @Test public void rejectsOversizedAndDeepJson() throws IOException {
        char[] large = new char[262145]; java.util.Arrays.fill(large, ' ');
        write(new File(store.directory(), "imports/huge.json"), new String(large));
        try { store.importProfile("huge"); fail(); } catch (IOException expected) { }
        String deep = "null";
        for (int i = 0; i < 40; i++) deep = "[" + deep + "]";
        write(new File(store.directory(), "imports/deep.json"), deep);
        try { store.importProfile("deep"); fail(); } catch (IOException expected) { }
    }
    @Test public void corruptConfigPreservedBeforeDefaults() throws IOException {
        write(new File(store.directory(), "config.json"), "not json at all");
        ConfigStore reloaded = new ConfigStore(store.directory());
        try { reloaded.load(); fail(); } catch (IOException expected) { }
        assertEquals("Nebula", reloaded.config().theme);
        assertEquals(1, store.directory().listFiles((dir,name) -> name.startsWith("config.invalid-")).length);
    }
    @Test public void perServerSaveDoesNotOverwriteGlobalDefaults() throws IOException {
        store.config().theme = "Global"; store.save();
        store.config().theme = "Server"; store.saveProfile("server");
        store.config().theme = "Global";
        store.bindServer("play.example.com", "server");
        assertTrue(store.loadServerProfile("play.example.com"));
        assertEquals("Server", store.config().theme);
        store.config().theme = "Edited server"; store.save(); store.leaveServerProfile();
        assertEquals("Global", store.config().theme);
        ConfigStore check = new ConfigStore(store.directory()); check.load();
        assertEquals("Global", check.config().theme);
        assertTrue(check.loadServerProfile("play.example.com"));
        assertEquals("Edited server", check.config().theme);
    }
    @Test public void deletingBoundProfileRemovesBinding() throws IOException {
        store.saveProfile("server"); store.bindServer("play.example.com", "server");
        store.loadServerProfile("play.example.com");
        store.deleteProfile("server"); store.save();
        assertFalse(store.loadServerProfile("play.example.com"));
        assertFalse(store.listProfiles().contains("server"));
    }
    @Test public void resetWritesUsableDefaults() throws IOException {
        store.config().opacity = .5f; store.resetDefaults();
        assertEquals(.94f, store.config().opacity, .001f);
        ConfigStore check = new ConfigStore(store.directory()); check.load(); assertEquals(.94f, check.config().opacity, .001f);
    }
    @Test public void serverProfilesCannotOverrideGlobalActivationPreference() throws IOException {
        store.config().perServerProfiles = false; store.saveProfile("server");
        store.config().perServerProfiles = true; store.bindServer("play.example.com", "server");
        store.loadServerProfile("play.example.com"); assertTrue(store.config().perServerProfiles);
        store.config().perServerProfiles = false; store.leaveServerProfile();
        assertFalse(store.config().perServerProfiles);
    }
    @Test public void rejectsSymlinkedImports() throws IOException {
        File outside = temp.newFolder("outside");
        write(new File(outside, "escaped.json"), "{}");
        Files.createSymbolicLink(new File(store.directory(), "imports").toPath(), outside.toPath());
        try { store.importProfile("escaped"); fail("Followed symlink"); } catch (IOException expected) { }
    }
    private static void write(File file, String text) throws IOException {
        Files.createDirectories(file.toPath().getParent()); Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
    }
}
