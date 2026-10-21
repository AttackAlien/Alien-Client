package dev.alien.client.world;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class SkyboxLoaderTest {
    @Rule public TemporaryFolder files=new TemporaryFolder();

    @Test public void acceptsBoundedPngAndEmptyImport()throws Exception {
        File dir=files.newFolder("skyboxes");
        File file=new File(dir,"aurora.png");ImageIO.write(new BufferedImage(16,8,BufferedImage.TYPE_INT_RGB),"png",file);
        BufferedImage loaded=SkyboxLoader.read(SkyboxLoader.resolve(dir,"aurora.png"));
        assertEquals(16,loaded.getWidth());assertEquals(8,loaded.getHeight());assertNull(SkyboxLoader.resolve(dir,""));
    }

    @Test public void rejectsTraversalUrlsAndNestedPaths()throws Exception {
        File dir=files.newFolder("skyboxes");
        for(String name:new String[]{"../outside.png","sub/model.png","https://host/sky.png","..\\outside.png","not-a-png.jpg"}){
            try{SkyboxLoader.resolve(dir,name);fail("accepted "+name);}catch(IOException expected){assertNotNull(expected.getMessage());}
        }
    }

    @Test public void rejectsSymbolicLinksEscapingImportDirectory()throws Exception {
        File dir=files.newFolder("skyboxes");File outside=files.newFile("external.png");
        try{Files.createSymbolicLink(new File(dir,"escape.png").toPath(),outside.toPath());}
        catch(UnsupportedOperationException failure){org.junit.Assume.assumeNoException(failure);return;}
        catch(IOException failure){org.junit.Assume.assumeNoException(failure);return;}
        try{SkyboxLoader.resolve(dir,"escape.png");fail("accepted escaping symlink");}catch(IOException expected){assertTrue(expected.getMessage().contains("inside"));}
    }

    @Test public void rejectsOversizedDimensionsBeforeDecodingPixels()throws Exception {
        File file=files.newFile("too-wide.png");ImageIO.write(new BufferedImage(4097,2,BufferedImage.TYPE_INT_RGB),"png",file);
        try{SkyboxLoader.read(file);fail("accepted oversized PNG");}catch(IOException expected){assertTrue(expected.getMessage().contains("4096"));}
    }

    @Test public void rejectsCorruptInput()throws Exception {
        File file=files.newFile("corrupt.png");Files.write(file.toPath(),new byte[]{1,2,3,4});
        try{SkyboxLoader.read(file);fail("accepted corrupt PNG");}catch(IOException expected){assertNotNull(expected.getMessage());}
    }
}
