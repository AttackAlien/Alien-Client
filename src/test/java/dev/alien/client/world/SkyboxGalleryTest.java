package dev.alien.client.world;

import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class SkyboxGalleryTest {
    @Rule public TemporaryFolder files = new TemporaryFolder();

    @Test public void indexesRealLocalPngsAndCubemapsInStableOrder() throws Exception {
        File directory = files.newFolder("skyboxes");
        ImageIO.write(new BufferedImage(64,64,BufferedImage.TYPE_INT_RGB),"png",
                new File(directory,"Sakura.png"));
        ImageIO.write(new BufferedImage(192,128,BufferedImage.TYPE_INT_RGB),"png",
                new File(directory,"nebula.cube.png"));
        Files.write(new File(directory,"bad.txt").toPath(),new byte[]{1});
        File nested = new File(directory,"sub"); assertTrue(nested.mkdirs());
        List<String> names=SkyboxGallery.list(directory);
        assertEquals(Arrays.asList("nebula.cube.png","Sakura.png"),names);
        assertTrue(SkyboxGallery.list(files.newFolder("empty")).isEmpty());
    }

    @Test public void rejectsUnsafeNamesAndKeepsFavoriteListBounded() {
        WorldConfig c = new WorldConfig();
        assertFalse(SkyboxGallery.validFavorite("../secret.png"));
        assertFalse(SkyboxGallery.validFavorite("https://remote/cube.png"));
        assertFalse(SkyboxGallery.validFavorite("invalid.jar"));
        for (int i=0;i<30;i++) SkyboxGallery.toggleFavorite(c,"sky"+i+".png");
        assertEquals(16,c.favoriteSkyboxes.size());
        assertFalse(c.favoriteSkyboxes.contains("sky0.png"));
        assertTrue(c.favoriteSkyboxes.contains("sky29.png"));
        SkyboxGallery.toggleFavorite(c,"sky29.png");
        assertFalse(SkyboxGallery.isFavorite(c,"sky29.png"));
        c.favoriteSkyboxes.add("../unsafe.png");
        c.favoriteSkyboxes.add("sky28.png");
        c.normalize();
        assertFalse(c.favoriteSkyboxes.contains("../unsafe.png"));
        assertEquals(15,c.favoriteSkyboxes.size());
    }

    @Test public void keepsFavoriteSettingsAcrossJsonAndOldProfiles() {
        Gson gson=new Gson();
        WorldConfig c=new WorldConfig();
        c.skyboxFile="nebula.cube.png";
        SkyboxGallery.toggleFavorite(c,"nebula.cube.png");
        WorldConfig reloaded=gson.fromJson(gson.toJson(c),WorldConfig.class);
        reloaded.normalize();
        assertEquals("nebula.cube.png",reloaded.skyboxFile);
        assertTrue(SkyboxGallery.isFavorite(reloaded,"nebula.cube.png"));
        WorldConfig old=gson.fromJson("{\"theme\":\"Sakura\"}",WorldConfig.class);
        old.normalize();
        assertNotNull(old.favoriteSkyboxes);
        assertTrue(old.favoriteSkyboxes.isEmpty());
    }

    @Test public void indexesAtMost64FilesAndRejectsLink() throws Exception {
        File dir=files.newFolder("many-skyboxes");
        for(int i=0;i<80;i++) Files.write(new File(dir,"sky"+i+".png").toPath(),new byte[]{1,2,3});
        assertEquals(64,SkyboxGallery.list(dir).size());
        File outside=files.newFile("outside.png");
        try {
            Files.createSymbolicLink(new File(dir,"escaping.png").toPath(),outside.toPath());
            assertFalse(SkyboxGallery.list(dir).contains("escaping.png"));
        } catch (UnsupportedOperationException | java.io.IOException ignored) {
            // Symlinks aren't supported on every filesystem.
        }
    }
}
