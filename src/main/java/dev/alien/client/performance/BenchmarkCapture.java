package dev.alien.client.performance;

import java.util.Arrays;

/**
 * A bounded 15-second in-game sample. Two explicitly named sessions can be
 * compared; no visuals are automatically toggled and these are NOT FPS claims.
 */
public final class BenchmarkCapture {
    public static final long DURATION_NANOS = 15_000_000_000L;
    private final double[] times = new double[4096];
    private int count, cursor;
    private double total;
    private long start = -1;
    private Result result;

    public boolean record(double millis, long now) {
        if (result != null) return true;
        if (!(millis > 0) || millis > 500 || Double.isNaN(millis) || Double.isInfinite(millis)) return false;
        if (start < 0) start = now;
        if (count < times.length) count++; else total -= times[cursor];
        times[cursor] = millis;
        total += millis;
        cursor = (cursor + 1) % times.length;
        if (now - start >= DURATION_NANOS) {
            double[] sorted = Arrays.copyOf(times, count);
            Arrays.sort(sorted);
            result = new Result(count, total / count,
                    sorted[Math.max(0, (int)Math.ceil(count * .95) - 1)],
                    sorted[Math.max(0, (int)Math.ceil(count * .99) - 1)]);
            return true;
        }
        return false;
    }
    public Result result() { return result; }
    public int samples() { return count; }

    public static final class Result {
        public final int samples;
        public final double meanMs, p95Ms, p99Ms;
        Result(int samples, double mean, double p95, double p99) {
            this.samples = samples;
            meanMs = mean;
            p95Ms = p95;
            p99Ms = p99;
        }
    }
}
