package dev.alien.client.performance;

/**
 * In-memory cap on Alien visual work. Hysteresis is conservative, changes at
 * most once per 5 seconds, and NEVER modifies saved user preferences.
 */
public final class AdaptiveBudget {
    private float scale = 1f;
    private long lastChange;
    private int warmupSamples;

    public float scale() { return scale; }

    public boolean update(double p95Ms, int samples, int targetFps, long nowNanos) {
        if (samples < 120 || !(p95Ms > 0) || Double.isNaN(p95Ms) || Double.isInfinite(p95Ms))
            return false;
        if (warmupSamples == 0) {
            warmupSamples = samples;
            lastChange = nowNanos;
            return false;
        }
        if (nowNanos - lastChange < 5_000_000_000L || nowNanos < lastChange) return false;
        double budgetMs = 1000d / Math.max(30, Math.min(240, targetFps));
        float before = scale;
        if (p95Ms > budgetMs * 1.20) scale = Math.max(.4f, scale - .15f);
        else if (p95Ms < budgetMs * .82) scale = Math.min(1f, scale + .05f);
        if (before != scale) lastChange = nowNanos;
        return before != scale;
    }

    public int cap(int configured) { return configured <= 0 ? 0 : Math.max(0, Math.min(configured, Math.round(configured * scale))); }
    public float cap(float configured) { return configured <= 0 ? 0 : configured * scale; }
    public void reset() { scale = 1f; warmupSamples = 0; lastChange = 0; }
}
