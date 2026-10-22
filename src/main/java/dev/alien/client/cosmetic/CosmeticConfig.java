package dev.alien.client.cosmetic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Local rendering preferences. These values are never sent to a server. */
public final class CosmeticConfig {
    public boolean enabled = true;
    public float maxDistance = 32;
    public float previewLight = .8f;
    public String previewBackground = "Violet";
    public Accessory self = new Accessory();
    public Map<String, Accessory> players = new LinkedHashMap<String, Accessory>();

    public void normalize() {
        maxDistance = clamp(maxDistance, 4, 96);
        previewLight = clamp(previewLight, .15f, 1f);
        if (!"Graphite".equals(previewBackground) && !"Violet".equals(previewBackground) && !"Slate".equals(previewBackground)) previewBackground = "Violet";
        if (self == null) self = new Accessory();
        self.normalize();
        if (players == null) players = new LinkedHashMap<String, Accessory>();
        Map<String, Accessory> valid = new LinkedHashMap<String, Accessory>();
        for (Map.Entry<String, Accessory> entry : players.entrySet()) {
            if (valid.size() >= 128) break;
            try {
                String id = UUID.fromString(entry.getKey()).toString();
                if (entry.getValue() != null) {
                    entry.getValue().normalize();
                    valid.put(id, entry.getValue());
                }
            } catch (RuntimeException ignored) { }
        }
        players = valid;
    }

    public static final class Accessory {
        public boolean enabled = true;
        public String hat = "JAPANESE";
        public String face = "NONE";
        public String back = "NONE";
        public boolean charm;
        public boolean aura;
        public boolean trail;
        public float offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ;
        public float scale = 1;
        public float hatSize = 1;
        public float capeLength = 1;
        public float capeFlutter = 1;
        public boolean animations = true;
        public String detailLevel = "Balanced";
        public String capePattern = "ALIEN";
        public int color = 0xB79AFF;
        public int secondaryColor = 0x36DCC9;
        /** Optional basename in the dedicated cosmetics directory; never a URL. */
        public String modelFile = "";
        public String textureFile = "";

        public void normalize() {
            hat = choice(hat, "NONE", "JAPANESE", "SAMURAI", "CROWN");
            face = choice(face, "NONE", "KITSUNE", "FUTURE", "GLASSES");
            back = choice(back, "NONE", "CAPE", "WINGS", "BACKPACK");
            offsetX = clamp(offsetX, -16, 16); offsetY = clamp(offsetY, -16, 16);
            offsetZ = clamp(offsetZ, -16, 16);
            rotationX = clamp(rotationX, -180, 180); rotationY = clamp(rotationY, -180, 180);
            rotationZ = clamp(rotationZ, -180, 180); scale = clamp(scale, .25f, 3);
            hatSize = clamp(hatSize, .75f, 1.35f);
            capeLength = clamp(capeLength, .65f, 1.35f);
            capeFlutter = clamp(capeFlutter, 0, 2);
            detailLevel = choice(detailLevel, "Low", "Balanced", "High");
            capePattern = choice(capePattern, "ALIEN", "MOON", "STARS", "PLAIN");
            modelFile = safeName(modelFile, ".json");
            textureFile = safeName(textureFile, ".png");
        }

        public Accessory copy() {
            Accessory a = new Accessory();
            a.enabled = enabled; a.hat = hat; a.face = face; a.back = back;
            a.charm = charm; a.aura = aura; a.trail = trail;
            a.offsetX = offsetX; a.offsetY = offsetY; a.offsetZ = offsetZ;
            a.rotationX = rotationX; a.rotationY = rotationY; a.rotationZ = rotationZ;
            a.scale = scale; a.color = color; a.secondaryColor = secondaryColor;
            a.hatSize = hatSize; a.capeLength = capeLength; a.capeFlutter = capeFlutter;
            a.animations = animations; a.detailLevel = detailLevel;
            a.capePattern = capePattern;
            a.modelFile = modelFile; a.textureFile = textureFile;
            return a;
        }
    }

    static String safeName(String name, String extension) {
        if (name == null || name.length() > 64 || name.contains("..") ||
                !name.matches("[A-Za-z0-9][A-Za-z0-9._-]*") || !name.endsWith(extension)) return "";
        return name;
    }

    private static String choice(String value, String... choices) {
        for (String choice : choices) if (choice.equals(value)) return choice;
        return choices[0];
    }

    static float clamp(float value, float min, float max) {
        return Float.isNaN(value) || Float.isInfinite(value) ? min : Math.max(min, Math.min(max, value));
    }
}
