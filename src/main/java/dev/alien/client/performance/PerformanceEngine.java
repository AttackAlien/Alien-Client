package dev.alien.client.performance;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Bounded game-frame telemetry. Only Alien-owned visual budgets are adapted;
 * vanilla render distance, VSync, shaders and the saved profile are unchanged.
 */
public final class PerformanceEngine {
    private final FrameMetrics history = new FrameMetrics();
    private final AdaptiveBudget adaptive = new AdaptiveBudget();
    private final BenchmarkCapture.Result[] results = new BenchmarkCapture.Result[2];
    private BenchmarkCapture capturing;
    private int captureSlot = -1;
    private long lastFrame, lastAdaptiveCheck;
    private Object lastWorld;
    private boolean adaptiveWasEnabled;

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        long now = System.nanoTime();
        if (lastWorld != mc.theWorld) {
            history.clear();
            adaptive.reset();
            capturing = null;
            captureSlot = -1;
            results[0] = results[1] = null;
            lastWorld = mc.theWorld;
            lastFrame = lastAdaptiveCheck = 0;
        }
        PerformanceConfig config = AlienClient.get().config().performance;
        if (!config.adaptiveEnabled && adaptiveWasEnabled) adaptive.reset();
        adaptiveWasEnabled = config.adaptiveEnabled;
        // Do not treat menu, minimized/pause transitions or unloaded worlds as gameplay FPS.
        if (lastFrame != 0 && mc.theWorld != null && mc.thePlayer != null
                && mc.currentScreen == null && !mc.isGamePaused()) {
            double ms = (now - lastFrame) / 1_000_000d;
            if (ms > 0 && ms <= 500 && (config.diagnostics || config.adaptiveEnabled || capturing != null)) {
                history.add(ms, config.spikeThresholdMs);
                // Percentile sorting only once per second, never on every game frame.
                if (config.adaptiveEnabled && (lastAdaptiveCheck == 0 || now - lastAdaptiveCheck >= 1_000_000_000L)) {
                    adaptive.update(history.p95(), history.count(), config.targetFps, now);
                    lastAdaptiveCheck = now;
                }
                if (capturing != null && capturing.record(ms, now)) {
                    results[captureSlot] = capturing.result();
                    capturing = null;
                    captureSlot = -1;
                }
            }
        }
        lastFrame = now;
    }

    public int fps() { return Minecraft.getDebugFPS(); }
    public double frameTimeMs() { return history.mean(); }
    public double percentile95Ms() { return history.p95(); }
    public double percentile99Ms() { return history.p99(); }
    public int spikeCount() { return history.spikes(); }
    public int frameCount() { return history.count(); }
    public double[] frameHistory() { return history.snapshot(); }
    public float qualityScale() {
        return AlienClient.get().config().performance.adaptiveEnabled ? adaptive.scale() : 1f;
    }

    public int particleBudget() {
        PerformanceConfig c = AlienClient.get().config().performance;
        return c.adaptiveEnabled ? adaptive.cap(c.maxParticles) : c.maxParticles;
    }
    public int cosmeticBudget() {
        PerformanceConfig c = AlienClient.get().config().performance;
        return c.adaptiveEnabled ? adaptive.cap(c.maxCosmetics) : c.maxCosmetics;
    }
    public float effectDistance() {
        PerformanceConfig c = AlienClient.get().config().performance;
        // Keep non-zero effective distance within the existing rendering safety bounds.
        return c.adaptiveEnabled ? Math.max(4f, adaptive.cap(c.effectDistance)) : c.effectDistance;
    }

    public long usedMemoryMb() {
        Runtime r = Runtime.getRuntime();
        return (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
    }

    public boolean startBenchmark(int slot) {
        Minecraft mc = Minecraft.getMinecraft();
        if (slot < 0 || slot >= results.length || mc.theWorld == null || mc.thePlayer == null)
            return false;
        results[slot] = null;
        capturing = new BenchmarkCapture();
        captureSlot = slot;
        return true;
    }
    public int benchmarkSlot() { return captureSlot; }
    public BenchmarkCapture.Result benchmarkResult(int slot) {
        return slot < 0 || slot >= results.length ? null : results[slot];
    }
    public void cancelBenchmark() { capturing = null; captureSlot = -1; }

    public static void applyPreset(ClientConfig config, String preset) {
        if (!"Competitive".equals(preset) && !"Balanced".equals(preset)
                && !"Cinematic".equals(preset)) throw new IllegalArgumentException("Unknown preset");
        config.performance.preset = preset;
        if ("Competitive".equals(preset)) {
            config.performance.maxParticles = 16; config.performance.maxCosmetics = 8;
            config.performance.effectDistance = 16;
        } else if ("Cinematic".equals(preset)) {
            config.performance.maxParticles = 200; config.performance.maxCosmetics = 64;
            config.performance.effectDistance = 64;
        } else {
            config.performance.maxParticles = 80; config.performance.maxCosmetics = 32;
            config.performance.effectDistance = 32;
        }
    }
}
