package dev.alien.client.studio;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class ProfileThumbnailLoaderTest {
    @Rule public TemporaryFolder files = new TemporaryFolder();
    @Test public void boundsValidDecodedThumbnailTo256Pixels() throws Exception {
        File file = files.newFile("valid.png");
        ImageIO.write(new BufferedImage(1200, 600, BufferedImage.TYPE_INT_RGB), "png", file);
        BufferedImage loaded = new ProfileThumbnailLoader().load(file);
        assertNotNull(loaded); assertTrue(loaded.getWidth() <= 256); assertTrue(loaded.getHeight() <= 256);
        assertEquals(loaded.getWidth(), loaded.getHeight() * 2);
    }
    @Test public void rejectsDimensionsBeforePixelsAreDecoded() throws Exception {
        File file = files.newFile("wide.png");
        ImageIO.write(new BufferedImage(6000, 2, BufferedImage.TYPE_INT_RGB), "png", file);
        ProfileThumbnailLoader loader = new ProfileThumbnailLoader();
        assertNull(loader.load(file)); assertTrue(loader.lastError().contains("dimensions"));
    }
    @Test public void cachesRejectedRevisionAndExplicitCaptureInvalidatesIt() throws Exception {
        File valid = files.newFile("source.png");
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", valid);
        byte[] png = Files.readAllBytes(valid.toPath());
        File file = files.newFile("thumbnail.png"); Files.write(file.toPath(), new byte[png.length]);
        long revision = file.lastModified(); ProfileThumbnailLoader loader = new ProfileThumbnailLoader();
        assertNull(loader.load(file));
        Files.write(file.toPath(), png); assertTrue(file.setLastModified(revision));
        assertNull("Same revision should retain its rejection", loader.load(file));
        loader.invalidate(file); assertNotNull(loader.load(file));
    }
    @Test public void retriesAutomaticallyWhenRejectedFileChanges() throws Exception {
        File file = files.newFile("changed.png"); Files.write(file.toPath(), new byte[]{1, 2, 3});
        ProfileThumbnailLoader loader = new ProfileThumbnailLoader(); assertNull(loader.load(file));
        ImageIO.write(new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB), "png", file);
        assertNotNull(loader.load(file));
    }
}
