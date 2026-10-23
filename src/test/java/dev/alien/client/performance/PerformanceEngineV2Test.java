package dev.alien.client.performance;

import com.google.gson.Gson;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class PerformanceEngineV2Test {
    @Test public void historyUsesBoundedRingAndCachesCorrectPercentiles() {
        FrameMetrics metrics=new FrameMetrics();
        assertEquals(0,metrics.p99(),0);
        for(int i=1;i<=260;i++)metrics.add(i/2d,40);
        assertEquals(240,metrics.count());
        assertEquals(240,metrics.snapshot().length);
        assertEquals(10.5,metrics.snapshot()[0],0);
        assertEquals(130,metrics.snapshot()[239],0);
        assertEquals(124,metrics.p95(),.1);
        assertEquals(129,metrics.p99(),.1);
        assertEquals(181,metrics.spikes());
        assertEquals(metrics.p95(),metrics.p95(),0);
        metrics.clear();
        assertEquals(0,metrics.count());
        assertEquals(0,metrics.spikes());
    }
    @Test public void spikesOnlyCountRecentFramesEvenAfterRingWraps() {
        FrameMetrics metrics = new FrameMetrics();
        for (int i = 0; i < 240; i++) metrics.add(90, 50);
        assertEquals(240, metrics.spikes());
        for (int i = 0; i < 120; i++) metrics.add(16, 50);
        assertEquals(120, metrics.spikes());
        for (int i = 0; i < 120; i++) metrics.add(16, 50);
        assertEquals(0, metrics.spikes());
        assertEquals(240, metrics.count());
        metrics.add(90, 50);
        assertEquals(1, metrics.spikes());
        metrics.add(90, 100);
        assertEquals(0, metrics.spikes());
        metrics.add(90, 80);
        assertEquals(3, metrics.spikes());
        metrics.clear();
        assertEquals(0, metrics.spikes());
        metrics.add(60, 50);
        assertEquals(1, metrics.spikes());
    }

    @Test public void rejectsFramesFromPausedOrStalledWindows() {
        FrameMetrics metrics=new FrameMetrics();
        for(double t:new double[]{Double.NaN,Double.POSITIVE_INFINITY,-1,0,501})
            metrics.add(t,50);
        assertEquals(0,metrics.count());
        metrics.add(16.7,50);
        assertEquals(16.7,metrics.mean(),.0001);
    }
    @Test public void adaptiveChangesOnlyEffectiveBudgetsAndUsesHysteresis() {
        AdaptiveBudget adaptive=new AdaptiveBudget();
        assertFalse(adaptive.update(40,20,90,100L));
        assertFalse(adaptive.update(40,120,90,1_000_000_000L)); // warmup
        assertFalse(adaptive.update(40,150,90,3_000_000_000L)); // cooldown
        assertTrue(adaptive.update(40,240,90,7_000_000_000L));
        assertEquals(.85f,adaptive.scale(),.0001f);
        assertEquals(85,adaptive.cap(100));
        assertEquals(17f,adaptive.cap(20f),.01f);
        assertFalse(adaptive.update(2,240,90,8_000_000_000L));
        assertTrue(adaptive.update(2,240,90,13_000_000_000L));
        assertEquals(.90f,adaptive.scale(),.0001f);
        for(int i=0;i<20;i++)adaptive.update(100,240,90,20_000_000_000L+i*6_000_000_000L);
        assertEquals(.40f,adaptive.scale(),.0001f);
        adaptive.reset();assertEquals(1f,adaptive.scale(),0);
        assertEquals(0,adaptive.cap(0));
    }
    @Test public void twoManualBenchmarksHaveBoundedRealFrameResults() {
        BenchmarkCapture baseline=new BenchmarkCapture();
        for(int i=0;i<=150;i++)baseline.record(10, i*100_000_000L);
        BenchmarkCapture.Result a=baseline.result();
        assertNotNull(a);assertEquals(151,a.samples);
        assertEquals(10,a.meanMs,0);assertEquals(10,a.p95Ms,0);
        BenchmarkCapture effects=new BenchmarkCapture();
        for(int i=0;i<=150;i++)effects.record(20,i*100_000_000L);
        assertEquals(20,effects.result().p99Ms,0);
        baseline.record(100,18_000_000_000L);
        assertEquals(10,baseline.result().meanMs,0);
    }
    @Test public void oldProfilesKeepAutoadaptationOffAndClampNewControls() {
        Gson gson=new Gson();
        PerformanceConfig old=gson.fromJson("{\"maxParticles\":32,\"diagnostics\":true}",PerformanceConfig.class);
        old.normalize();assertFalse(old.adaptiveEnabled);
        assertEquals(90,old.targetFps);
        assertEquals(32,old.maxParticles);
        old.targetFps=1000;old.spikeThresholdMs=Float.NaN;old.normalize();
        assertEquals(240,old.targetFps);
        assertEquals(20,old.spikeThresholdMs,0);
        PerformanceConfig reloaded=gson.fromJson(gson.toJson(old),PerformanceConfig.class);
        reloaded.normalize();assertFalse(reloaded.adaptiveEnabled);
    }
}
