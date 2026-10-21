package dev.alien.client.performance;

import dev.alien.client.config.ClientConfig;

public final class PerformanceConfig {
    public String preset = "Balanced";
    public int maxParticles = 80;
    public int maxCosmetics = 32;
    public float effectDistance = 32f;
    public boolean diagnostics = true;

    public void normalize() {
        if (!"Competitive".equals(preset) && !"Balanced".equals(preset) && !"Cinematic".equals(preset)) preset = "Balanced";
        maxParticles = Math.max(0, Math.min(512, maxParticles));
        maxCosmetics = Math.max(0, Math.min(128, maxCosmetics));
        effectDistance = ClientConfig.clamp(effectDistance, 4, 128);
    }
}
