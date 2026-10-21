package dev.alien.client.cosmetic;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** A bounded local box-model format, deliberately not a script or arbitrary resource loader. */
final class CosmeticAssets {
    private static final int MAX_BYTES = 1024 * 1024;
    private static final Gson GSON = new Gson();
    private static final Map<String, Imported> CACHE = new LinkedHashMap<String, Imported>();
    static String lastError = "";

    static File directory() {
        return new File(Minecraft.getMinecraft().mcDataDir, "config/alien-client/cosmetics");
    }

    static synchronized void reload() {
        for (Imported imported : CACHE.values()) {
            if (imported != null && imported.texture != null) Minecraft.getMinecraft().getTextureManager().deleteTexture(imported.texture);
        }
        CACHE.clear(); lastError = "";
    }

    static Imported get(String modelName, String textureName) {
        if (modelName == null || modelName.isEmpty()) return null;
        String key = modelName + ":" + textureName;
        if (CACHE.containsKey(key)) return CACHE.get(key);
        if (CACHE.size() >= 32) return null;
        Imported imported = null;
        try { imported = load(modelName, textureName); }
        catch (Exception e) { lastError = "Import rejected: " + e.getMessage(); }
        CACHE.put(key, imported);
        return imported;
    }

    static List<String> models() {
        File[] files = directory().listFiles();
        List<String> result = new ArrayList<String>();
        result.add("");
        if (files != null) for (File file : files) {
            if (file.isFile() && !CosmeticConfig.safeName(file.getName(), ".json").isEmpty()) result.add(file.getName());
        }
        Collections.sort(result);
        return result;
    }

    static List<String> textures() {
        File[] files = directory().listFiles();
        List<String> result = new ArrayList<String>(); result.add("");
        if (files != null) for (File file : files) {
            if (file.isFile() && !CosmeticConfig.safeName(file.getName(), ".png").isEmpty()) result.add(file.getName());
        }
        Collections.sort(result); return result;
    }

    static void writeExample() throws IOException {
        File dir = directory();
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("Cannot create cosmetics directory");
        File file = new File(dir, "example.json");
        if (file.exists()) return;
        try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            out.write("{\n  \"boxes\": [{\"x\":-2,\"y\":-11,\"z\":-2,\"width\":4,\"height\":3,\"depth\":4}]\n}\n");
        }
    }

    private static File checked(String name, String extension) throws IOException {
        if (CosmeticConfig.safeName(name, extension).isEmpty()) throw new IOException("Invalid filename");
        File dir = directory().getCanonicalFile();
        File file = new File(dir, name).getCanonicalFile();
        if (!dir.equals(file.getParentFile()) || !file.isFile() || file.length() > MAX_BYTES)
            throw new IOException("Missing, oversized, or outside-directory file");
        return file;
    }

    private static Imported load(String name, String textureName) throws IOException {
        List<Box> boxes;
        try (Reader reader = new InputStreamReader(new FileInputStream(checked(name, ".json")), StandardCharsets.UTF_8)) {
            boxes = parseModel(reader);
        }
        ModelBase base = new ModelBase() { };
        base.textureWidth = 64; base.textureHeight = 64;
        ModelRenderer model = new ModelRenderer(base, 0, 0);
        for (Box box : boxes) {
            model.addBox(box.x, box.y, box.z, box.width, box.height, box.depth);
        }
        ResourceLocation texture = null;
        if (textureName != null && !textureName.isEmpty()) {
            File imageFile = checked(textureName, ".png");
            try (ImageInputStream input = ImageIO.createImageInputStream(imageFile)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IOException("Unsupported image");
                ImageReader reader = readers.next();
                try {
                    if (!"png".equalsIgnoreCase(reader.getFormatName())) throw new IOException("Texture must be a PNG image");
                    reader.setInput(input);
                    int width = reader.getWidth(0), height = reader.getHeight(0);
                    if (width < 1 || width > 1024 || height < 1 || height > 1024) throw new IOException("PNG dimensions must be 1..1024");
                    BufferedImage image = reader.read(0);
                    texture = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("alien_cosmetic", new DynamicTexture(image));
                } finally { reader.dispose(); }
            }
        }
        return new Imported(model, texture);
    }

    /** Pure validation boundary: testable without initializing Minecraft or OpenGL. */
    static List<Box> parseModel(Reader reader) throws IOException {
        Definition definition;
        try { definition = GSON.fromJson(reader, Definition.class); }
        catch (RuntimeException e) { throw new IOException("Malformed model JSON", e); }
        if (definition == null || definition.boxes == null || definition.boxes.isEmpty() || definition.boxes.size() > 128)
            throw new IOException("Model requires 1 to 128 boxes");
        for (Box box : definition.boxes) {
            if (box == null || !finiteBound(box.x, 64) || !finiteBound(box.y, 64) || !finiteBound(box.z, 64) ||
                    box.width < 1 || box.width > 32 || box.height < 1 || box.height > 32 || box.depth < 1 || box.depth > 32)
                throw new IOException("Invalid box dimensions (positions +/-64, dimensions 1..32)");
        }
        return definition.boxes;
    }

    private static boolean finiteBound(float v, float bound) {
        return !Float.isNaN(v) && !Float.isInfinite(v) && Math.abs(v) <= bound;
    }
    private static final class Definition { List<Box> boxes; }
    static final class Box { float x, y, z; int width, height, depth; }
    static final class Imported {
        final ModelRenderer model; final ResourceLocation texture;
        Imported(ModelRenderer model, ResourceLocation texture) { this.model = model; this.texture = texture; }
    }
}
