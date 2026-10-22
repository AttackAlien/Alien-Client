package dev.alien.client.gui;

import com.lukflug.panelstudio.base.SettingsAnimation;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import static org.junit.Assert.*;

public class PanelStudioAnimationTest {
    @Test public void forwardAndReverseUseElapsedTime() {
        AtomicLong clock = new AtomicLong(1000);
        SettingsAnimation animation = new SettingsAnimation(() -> 200, clock::get);
        animation.initValue(0);
        animation.setValue(1);
        assertEquals(0, animation.getValue(), 1e-9);
        clock.set(1100);
        assertTrue(animation.getValue() > .5 && animation.getValue() < 1);
        clock.set(1200);
        assertEquals(1, animation.getValue(), 1e-9);
        animation.setValue(0);
        clock.set(1300);
        assertTrue(animation.getValue() > 0 && animation.getValue() < .5);
        clock.set(1400);
        assertEquals(0, animation.getValue(), 1e-9);
    }

    @Test public void zeroDurationSnapsToTarget() {
        AtomicLong clock = new AtomicLong(1000);
        SettingsAnimation animation = new SettingsAnimation(() -> 0, clock::get);
        animation.initValue(0);
        animation.setValue(1);
        assertEquals(1, animation.getValue(), 1e-9);
    }

    @Test public void newTargetStartsAtInterpolatedValue() {
        AtomicLong clock = new AtomicLong(1000);
        SettingsAnimation animation = new SettingsAnimation(() -> 200, clock::get);
        animation.initValue(0);
        animation.setValue(1);
        clock.set(1100);
        double current = animation.getValue();
        animation.setValue(0);
        assertEquals(current, animation.getValue(), 1e-9);
    }
}
