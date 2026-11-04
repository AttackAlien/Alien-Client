import dev.alien.client.AlienClient;
import dev.alien.client.config.ConfigStore;
import dev.alien.client.studio.ProfileLibraryScreen;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/** Invokes the actual private thumbnail method; rejected dimensions return before OpenGL. */
public final class ThumbnailAuditProbe {
    public static void main(String[] args) throws Exception {
        File fixture = new File(args[0]);
        File root = Files.createTempDirectory("alien-thumbnail-audit-").toFile();
        Field instance = AlienClient.class.getDeclaredField("instance"); instance.setAccessible(true);
        Object previous = instance.get(null);
        try {
            File screenshots = new File(root, "screenshots"); screenshots.mkdirs();
            Files.copy(fixture.toPath(), new File(screenshots, "profile-audit.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
            AlienClient client = new AlienClient();
            Field store = AlienClient.class.getDeclaredField("store"); store.setAccessible(true); store.set(client, new ConfigStore(root));
            instance.set(null, client);
            ProfileLibraryScreen screen = new ProfileLibraryScreen(null);
            Field selected = ProfileLibraryScreen.class.getDeclaredField("selected"); selected.setAccessible(true); selected.set(screen, "audit");
            Field photos = ProfileLibraryScreen.class.getDeclaredField("photos"); photos.setAccessible(true);
            Method photo = ProfileLibraryScreen.class.getDeclaredMethod("photo", int.class, int.class, int.class, int.class); photo.setAccessible(true);
            System.out.println("COMPRESSED_BYTES=" + fixture.length());
            System.out.println("ACTUAL_PRODUCTION_METHOD=ProfileLibraryScreen.photo");
            long started = System.nanoTime();
            for (int frame = 0; frame < 3; frame++) photo.invoke(screen, 0, 0, 80, 45);
            System.out.println("ACTUAL_METHOD_CALLS=3");
            System.out.println("CACHED_TEXTURES=" + ((Map<?, ?>)photos.get(screen)).size());
            System.out.println("TOTAL_MS=" + (System.nanoTime() - started) / 1000000);
            System.out.println("MINECRAFT_OR_OPENGL_STARTED=false");
        } finally { instance.set(null, previous); remove(root); }
    }
    private static void remove(File file) {
        File[] children = file.listFiles(); if (children != null) for (File child : children) remove(child);
        if (!file.delete()) file.deleteOnExit();
    }
}
