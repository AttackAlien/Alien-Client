package dev.alien.client.performance;

import java.util.Arrays;

/** Pure Java bounded frame-time recorder. Sorting is cached until samples change. */
public final class FrameMetrics {
    private final double[] history = new double[240];
    private final boolean[] spikeHistory = new boolean[history.length];
    private double lastSpikeThreshold = Double.NaN;
    private int count, cursor, spikes;
    private double sum, cachedP95, cachedP99;
    private boolean dirty = true;

    public void add(double millis, double spikeThreshold) {
        if (Double.isNaN(millis) || Double.isInfinite(millis) || millis <= 0 || millis > 500) return;
        if (Double.compare(spikeThreshold, lastSpikeThreshold) != 0) {
            // A threshold edit must reclassify recent samples, not accumulate old flags.
            lastSpikeThreshold = spikeThreshold;
            spikes = 0;
            for (int i = 0; i < count; i++) {
                spikeHistory[i] = history[i] >= spikeThreshold;
                if (spikeHistory[i]) spikes++;
            }
        }
        if (count == history.length) {
            sum -= history[cursor];
            if (spikeHistory[cursor]) spikes--;
        } else count++;
        history[cursor] = millis;
        spikeHistory[cursor] = millis >= spikeThreshold;
        if (spikeHistory[cursor]) spikes++;
        sum += millis;
        cursor = (cursor + 1) % history.length;
        dirty = true;
    }

    public int count() { return count; }
    public int spikes() { return spikes; }
    public double mean() { return count == 0 ? 0 : sum / count; }
    public double p95() { refresh(); return cachedP95; }
    public double p99() { refresh(); return cachedP99; }

    private void refresh() {
        if (!dirty) return;
        if (count == 0) { cachedP95 = cachedP99 = 0; dirty = false; return; }
        double[] sorted = Arrays.copyOf(history, count);
        Arrays.sort(sorted);
        cachedP95 = sorted[Math.max(0, (int) Math.ceil(count * .95) - 1)];
        cachedP99 = sorted[Math.max(0, (int) Math.ceil(count * .99) - 1)];
        dirty = false;
    }

    /** Oldest to newest; returns a copy and is safe for graph rendering. */
    public double[] snapshot() {
        double[] result = new double[count];
        int start = count == history.length ? cursor : 0;
        for (int i = 0; i < count; i++) result[i] = history[(start + i) % history.length];
        return result;
    }

    public void clear() {
        count = cursor = spikes = 0;
        sum = cachedP95 = cachedP99 = 0;
        lastSpikeThreshold = Double.NaN;
        Arrays.fill(spikeHistory, false);
        dirty = true;
    }
}
