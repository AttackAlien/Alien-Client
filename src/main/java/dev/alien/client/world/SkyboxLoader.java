package dev.alien.client.world;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/** Validates imports before allocation; accepts local basenames, never URLs or paths. */
final class SkyboxLoader {
    private SkyboxLoader() { }

    static File resolve(File directory, String name) throws IOException {
        if (name == null || name.isEmpty()) return null;
        if (name.length()>160 || !name.matches("[A-Za-z0-9_ .-]+\\.png") || name.contains(".."))
            throw new IOException("Use a PNG filename, without directories");
        File canonicalDirectory=directory.getCanonicalFile();
        File file=new File(canonicalDirectory,name).getCanonicalFile();
        if(!canonicalDirectory.equals(file.getParentFile())) throw new IOException("Skybox must stay inside skyboxes directory");
        return file;
    }

    static BufferedImage read(File file) throws IOException {
        if(!file.isFile() || file.length()>16L*1024L*1024L) throw new IOException("PNG missing or larger than 16 MiB");
        try(ImageInputStream stream=ImageIO.createImageInputStream(file)) {
            if(stream==null)throw new IOException("Cannot read PNG");
            Iterator<ImageReader> readers=ImageIO.getImageReaders(stream);
            if(!readers.hasNext())throw new IOException("Unsupported PNG");
            ImageReader reader=readers.next();
            try {
                reader.setInput(stream);
                int width=reader.getWidth(0),height=reader.getHeight(0);
                if(!"png".equalsIgnoreCase(reader.getFormatName()) || width<2 || height<2 || width>4096 || height>2048)
                    throw new IOException("PNG must be 2..4096 by 2..2048 pixels");
                BufferedImage image=reader.read(0);
                if(image==null)throw new IOException("Cannot decode PNG");
                return image;
            }finally{reader.dispose();}
        }
    }
}
