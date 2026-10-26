import dev.alien.client.config.ConfigStore;
import java.io.File;
import java.nio.file.Files;

/** Disposable profile-only reproduction; no Minecraft, network, or user saves. */
public final class ConfigAuditProbe {
    public static void main(String[] args) throws Exception {
        File root = Files.createTempDirectory("alien-profile-audit-").toFile();
        try {
            ConfigStore store = new ConfigStore(root); store.load();
            store.config().theme = "Original server A"; store.saveProfile("server_A");
            store.config().theme = "Personal B"; store.saveProfile("personal_B");
            store.bindServer("audit.example.invalid", "server_A");
            store.loadServerProfile("audit.example.invalid");
            String before = store.previewProfile("server_A").theme;
            store.loadProfile("personal_B");
            String after = store.previewProfile("server_A").theme;
            System.out.println("BOUND_PROFILE_BEFORE=" + before);
            System.out.println("BOUND_PROFILE_AFTER=" + after);
            System.out.println("BINDING_AFTER=" + store.serverProfile("audit.example.invalid"));
            System.out.println("UNRELATED_PROFILE_OVERWRITTEN=" + !before.equals(after));
        } finally { remove(root); }
    }
    private static void remove(File file) {
        File[] children = file.listFiles(); if (children != null) for (File child : children) remove(child);
        if (!file.delete()) file.deleteOnExit();
    }
}
