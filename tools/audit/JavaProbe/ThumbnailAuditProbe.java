import java.io.File;
import javax.imageio.ImageIO;

/** Same decoding-before-validation sequence as ProfileLibraryScreen.photo. */
public final class ThumbnailAuditProbe {
    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        System.out.println("COMPRESSED_BYTES=" + file.length());
        if (file.length() > 16000000) throw new IllegalArgumentException("Fixture exceeds production byte limit");
        long started = System.nanoTime();
        int guardReached = 0, errors = 0;
        for (int frame = 0; frame < 3; frame++) {
            try {
                java.awt.image.BufferedImage image = ImageIO.read(file);
                if (image.getWidth() > 4096 || image.getHeight() > 4096) { guardReached++; continue; }
            } catch (java.io.IOException failure) {
                errors++; System.out.println("READ_ERROR=" + failure.getClass().getName() + "; cause=" + failure.getCause());
            }
        }
        System.out.println("UNCACHED_DECODE_ATTEMPTS=3");
        System.out.println("DIMENSION_GUARD_REACHED=" + guardReached);
        System.out.println("READ_ERRORS=" + errors);
        System.out.println("TOTAL_MS=" + (System.nanoTime() - started) / 1000000);
    }
}
