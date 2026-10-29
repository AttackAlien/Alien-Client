package dev.alien.client.cosmetic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Pure bounded PNG import; no Minecraft, OpenGL, URLs or executable content. */
public final class TrailTextureLoader {
    public static final int MAX_BYTES=1024*1024, MAX_DIMENSION=1024;
    private TrailTextureLoader() { }
    public static void ensureDirectory(File directory) throws IOException {
        rejectLinks(directory); Files.createDirectories(directory.toPath());
    }
    private static void rejectLinks(File file) throws IOException {
        for (File p=file.getAbsoluteFile(); p!=null; p=p.getParentFile())
            if (Files.isSymbolicLink(p.toPath())) throw new IOException("Linked texture paths are not accepted");
    }
    public static BufferedImage load(File directory, String name) throws IOException {
        if (CosmeticConfig.safeName(name,".png").isEmpty()) throw new IOException("Choose a local PNG filename");
        File file=new File(directory,name); rejectLinks(file);
        if (!file.getCanonicalFile().getParentFile().equals(directory.getCanonicalFile()) || !file.isFile() || file.length()>MAX_BYTES)
            throw new IOException("PNG missing or >1 MiB");
        try (ImageInputStream input=ImageIO.createImageInputStream(file)) {
            if (input==null) throw new IOException("Cannot open PNG");
            Iterator<ImageReader> readers=ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Unsupported PNG");
            ImageReader reader=readers.next();
            try {
                reader.setInput(input);
                if (!"png".equalsIgnoreCase(reader.getFormatName())) throw new IOException("Texture must be PNG");
                int w=reader.getWidth(0),h=reader.getHeight(0);
                if (w<1 || h<1 || w>MAX_DIMENSION || h>MAX_DIMENSION) throw new IOException("PNG dimensions: 1..1024");
                BufferedImage image=reader.read(0);
                if (image==null) throw new IOException("Cannot decode PNG");
                return image;
            } finally { reader.dispose(); }
        } catch (RuntimeException invalid) { throw new IOException("Cannot decode PNG",invalid); }
    }
    public static List<String> files(File directory) {
        List<String> names=new ArrayList<String>();
        try { rejectLinks(directory); } catch (IOException e) { return names; }
        File[] files=directory.listFiles();
        if (files!=null) for (File file:files) {
            if (file.isFile() && !Files.isSymbolicLink(file.toPath()) && !CosmeticConfig.safeName(file.getName(),".png").isEmpty()) names.add(file.getName());
        }
        Collections.sort(names); return names.size()>64 ? new ArrayList<String>(names.subList(0,64)) : names;
    }
    /** Original small alpha masks, tinted by the editable gradient in the renderer. */
    public static BufferedImage builtin(String name) {
        if (!TrailDesign.isBuiltin(name)) throw new IllegalArgumentException("Unknown trail material");
        BufferedImage image=new BufferedImage(128,32,BufferedImage.TYPE_INT_ARGB);
        for (int y=0;y<32;y++) for (int x=0;x<128;x++) {
            double v=(y+.5)/32, u=x/128d, edge=Math.pow(Math.max(0,Math.sin(v*Math.PI)),.75), mask=1;
            if ("Silk".equals(name)) mask=.3+.7*Math.pow(.5+.5*Math.cos(v*4*Math.PI+Math.sin(u*2*Math.PI)*1.8),2);
            else if ("Dashes".equals(name)) mask=Math.pow(.5+.5*Math.cos(u*8*Math.PI-v*2*Math.PI),5);
            else if ("Weave".equals(name)) mask=.2+.8*Math.abs(Math.sin(u*8*Math.PI+v*4*Math.PI)*Math.sin(u*8*Math.PI-v*4*Math.PI));
            int alpha=(int)Math.round(255*edge*mask); image.setRGB(x,y,(alpha<<24)|0xffffff);
        }
        return image;
    }
}
