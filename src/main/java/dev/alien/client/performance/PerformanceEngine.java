package dev.alien.client.performance;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Measures actual rendered intervals; deliberately makes no benchmark claims. */
public final class PerformanceEngine {
    private long lastFrame;
    private final double[] samples = new double[240];
    private int cursor, count;

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long now = System.nanoTime();
        if (lastFrame != 0 && AlienClient.get().config().performance.diagnostics) {
            double ms = (now - lastFrame) / 1_000_000d;
            if (ms > 0 && ms < 10_000) { samples[cursor] = ms; cursor = (cursor + 1) % samples.length; count = Math.min(count + 1, samples.length); }
        }
        lastFrame = now;
    }

    public int fps() { return Minecraft.getDebugFPS(); }
    public double frameTimeMs() {
        if (count == 0) return 0;
        double total = 0;
        for (int i = 0; i < count; i++) total += samples[i];
        return total / count;
    }
    public double percentile95Ms() {
        if (count == 0) return 0;
        double[] sorted = java.util.Arrays.copyOf(samples, count);
        java.util.Arrays.sort(sorted);
        return sorted[Math.max(0, (int) Math.ceil(count * .95) - 1)];
    }
    public long usedMemoryMb() { Runtime r = Runtime.getRuntime(); return (r.totalMemory() - r.freeMemory()) / (1024 * 1024); }

    public static void applyPreset(ClientConfig config, String preset) {
        if (!"Competitive".equals(preset) && !"Balanced".equals(preset) && !"Cinematic".equals(preset)) throw new IllegalArgumentException("Unknown preset");
        config.performance.preset = preset;
        if ("Competitive".equals(preset)) {
            config.performance.maxParticles = 16; config.performance.maxCosmetics = 8; config.performance.effectDistance = 16;
        } else if ("Cinematic".equals(preset)) {
            config.performance.maxParticles = 200; config.performance.maxCosmetics = 64; config.performance.effectDistance = 64;
        } else {
            config.performance.maxParticles = 80; config.performance.maxCosmetics = 32; config.performance.effectDistance = 32;
        }
    }
}
