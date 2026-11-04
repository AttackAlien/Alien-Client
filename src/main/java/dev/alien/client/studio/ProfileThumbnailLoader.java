package dev.alien.client.studio;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bounds dimensions before decoding and remembers rejected file revisions. */
public final class ProfileThumbnailLoader {
    private static final int MAX_FAILURES = 64;
    private final Map<String, Rejected> rejected = new LinkedHashMap<String, Rejected>();
    private String lastError = "";

    public BufferedImage load(File file) {
        String key = file.getAbsolutePath(); long size = file.length(), modified = file.lastModified();
        Rejected previous = rejected.get(key);
        if (previous != null && previous.size == size && previous.modified == modified) return null;
        rejected.remove(key);
        try {
            if (!file.isFile() || Files.isSymbolicLink(file.toPath()) || size > 16000000)
                throw new IOException("Thumbnail missing, linked, or larger than 16 MB");
            try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
                if (input == null) throw new IOException("Cannot open thumbnail");
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IOException("Unsupported thumbnail");
                ImageReader reader = readers.next();
                try {
                    reader.setInput(input);
                    int width = reader.getWidth(0), height = reader.getHeight(0);
                    if (!"png".equalsIgnoreCase(reader.getFormatName()) || width < 1 || height < 1 || width > 4096 || height > 4096)
                        throw new IOException("Thumbnail PNG dimensions must be 1..4096");
                    ImageReadParam parameters = reader.getDefaultReadParam();
                    int sample = Math.max(1, (Math.max(width, height) + 255) / 256);
                    parameters.setSourceSubsampling(sample, sample, 0, 0);
                    BufferedImage image = reader.read(0, parameters);
                    if (image == null) throw new IOException("Cannot decode thumbnail");
                    lastError = "";
                    return image;
                } finally { reader.dispose(); }
            }
        } catch (IOException | RuntimeException failure) {
            if (rejected.size() == MAX_FAILURES) rejected.remove(rejected.keySet().iterator().next());
            rejected.put(key, new Rejected(size, modified));
            lastError = failure.getMessage() == null ? "Invalid thumbnail" : failure.getMessage();
            return null;
        }
    }
    public String lastError() { return lastError; }
    public void invalidate(File file) { rejected.remove(file.getAbsolutePath()); }
    public void clear() { rejected.clear(); lastError = ""; }
    private static final class Rejected {
        final long size, modified;
        Rejected(long size, long modified) { this.size = size; this.modified = modified; }
    }
}
