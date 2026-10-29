package dev.alien.client.cosmetic;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class TrailTextureLoaderTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void customPngPreservesItsRealRgbAndAlpha() throws Exception {
        File directory=temp.newFolder();BufferedImage image=new BufferedImage(12,7,BufferedImage.TYPE_INT_ARGB);
        image.setRGB(3,2,0x4080ff20);ImageIO.write(image,"png",new File(directory,"streak.png"));
        BufferedImage loaded=TrailTextureLoader.load(directory,"streak.png");assertEquals(12,loaded.getWidth());assertEquals(7,loaded.getHeight());assertEquals(0x4080ff20,loaded.getRGB(3,2));
    }
    @Test public void rejectsDimensionsFromHeaderBeforeAttemptingMissingPixelData() throws Exception {
        File directory=temp.newFolder();ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
        out.writeLong(0x89504e470d0a1a0aL);ByteArrayOutputStream header=new ByteArrayOutputStream();DataOutputStream h=new DataOutputStream(header);
        h.writeInt(6000);h.writeInt(6000);h.write(new byte[]{8,6,0,0,0});chunk(out,"IHDR",header.toByteArray());chunk(out,"IEND",new byte[0]);
        Files.write(new File(directory,"header.png").toPath(),bytes.toByteArray());
        try{TrailTextureLoader.load(directory,"header.png");fail("Large image accepted");}catch(IOException expected){assertTrue(expected.getMessage(),expected.getMessage().contains("dimensions"));}
    }
    @Test public void rejectsOversizedNonPngAndPathInputs() throws Exception {
        File directory=temp.newFolder();Files.write(new File(directory,"bad.png").toPath(),new byte[]{1,2,3});
        Files.write(new File(directory,"large.png").toPath(),new byte[TrailTextureLoader.MAX_BYTES+1]);
        for(String name:new String[]{"bad.png","large.png","../escape.png","https://host/skin.png","thing.jpg",""}){
            try{TrailTextureLoader.load(directory,name);fail(name);}catch(IOException expected){assertNotNull(expected.getMessage());}
        }
    }
    @Test public void rejectsLinkedTextureFilesAndListsOnlySafeLocalPngs() throws Exception {
        File directory=temp.newFolder();File outside=temp.newFile("outside.png");
        Files.createSymbolicLink(new File(directory,"linked.png").toPath(),outside.toPath());
        try{TrailTextureLoader.load(directory,"linked.png");fail("Link accepted");}catch(IOException expected){assertTrue(expected.getMessage().contains("Linked"));}
        Files.write(new File(directory,"b.png").toPath(),new byte[]{1});Files.write(new File(directory,"a.png").toPath(),new byte[]{1});
        Files.write(new File(directory,"not-a-png.txt").toPath(),new byte[]{1});
        assertEquals(Arrays.asList("a.png","b.png"),TrailTextureLoader.files(directory));
    }
    @Test public void originalBuiltinAlphaMasksAreSmallDistinctAndTintable() {
        List<int[]> previous=new ArrayList<int[]>();
        for(String name:TrailDesign.TEXTURES){BufferedImage image=TrailTextureLoader.builtin(name);assertEquals(128,image.getWidth());assertEquals(32,image.getHeight());
            int[] pixels=image.getRGB(0,0,128,32,null,0,128);for(int[] other:previous)assertFalse(name+" duplicates another mask",Arrays.equals(other,pixels));previous.add(pixels);
            boolean translucent=false;for(int rgba:pixels){assertEquals(0xffffff,rgba&0xffffff);if((rgba>>>24)>0&&(rgba>>>24)<255)translucent=true;}assertTrue(translucent);
        }
    }
    private static void chunk(DataOutputStream out,String type,byte[] body)throws IOException {
        byte[] name=type.getBytes("US-ASCII");out.writeInt(body.length);out.write(name);out.write(body);CRC32 crc=new CRC32();crc.update(name);crc.update(body);out.writeInt((int)crc.getValue());
    }
}
