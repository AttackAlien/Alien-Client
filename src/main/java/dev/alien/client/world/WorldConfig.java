package dev.alien.client.world;

/** Render-only settings. No field is applied to the world's game rules or packets. */
public final class WorldConfig {
    public boolean enabled = false;
    public String theme = "Nebula";
    public int skyTop = 0x180D38;
    public int skyBottom = 0x704779;
    public int fogColor = 0x796B9D;
    public boolean fogEnabled = false;
    public float fogDensity = 0.003F;
    public boolean timeOverride = false;
    public int visualHour = 18;
    public boolean sunMoon = true;
    public int stars = 320;
    /** Server, Clear, Rain, Snow or Storm. Only the precipitation renderer changes. */
    public String weather = "Server";
    public float weatherIntensity = 0.5F;
    /** A PNG basename under config/alien-client/skyboxes; blank uses procedural sky. */
    public String skyboxFile = "";

    public void normalize() {
        if (!("Nebula".equals(theme) || "Galaxy".equals(theme) || "Aurora".equals(theme) || "Japanese".equals(theme)
                || "Crystal".equals(theme) || "Sakura".equals(theme))) theme = "Nebula";
        if (weather == null || !("Server".equals(weather) || "Clear".equals(weather)
                || "Rain".equals(weather) || "Snow".equals(weather) || "Storm".equals(weather))) weather = "Server";
        if (skyboxFile == null || skyboxFile.length() > 160) skyboxFile = "";
        visualHour = Math.max(0, Math.min(23, visualHour));
        stars = Math.max(0, Math.min(1000, stars));
        fogDensity = Float.isNaN(fogDensity) ? 0.003F : Math.max(0.0001F, Math.min(0.04F, fogDensity));
        weatherIntensity = Float.isNaN(weatherIntensity) ? 0.5F : Math.max(0F, Math.min(1F, weatherIntensity));
        skyTop &= 0xFFFFFF; skyBottom &= 0xFFFFFF; fogColor &= 0xFFFFFF;
    }

    public void applyTheme(String next) {
        theme = next;
        if ("Galaxy".equals(next)) {
            skyTop = 0x050D2A; skyBottom = 0x423170; fogColor = 0x605479;
        } else if ("Aurora".equals(next)) {
            skyTop = 0x061F32; skyBottom = 0x286D73; fogColor = 0x5A8A88;
        } else if ("Crystal".equals(next)) {
            skyTop = 0x209ED5; skyBottom = 0xC8F4F6; fogColor = 0xD4EDF0;
        } else if ("Sakura".equals(next)) {
            skyTop = 0x606FA1; skyBottom = 0xF1B7CA; fogColor = 0xEAD1DC;
        } else if ("Japanese".equals(next)) {
            skyTop = 0x393263; skyBottom = 0xD78C97; fogColor = 0xC096A3;
        } else {
            theme = "Nebula";
            skyTop = 0x180D38; skyBottom = 0x704779; fogColor = 0x796B9D;
        }
    }
}
