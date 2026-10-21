package dev.alien.client.world;

import org.junit.Test;
import static org.junit.Assert.*;

public class WorldSettingsTest {
    @Test public void malformedWorldSettingsBecomeBoundedAndValid() {
        WorldConfig c=new WorldConfig();c.theme=null;c.weather="send-packet";c.visualHour=90;c.stars=Integer.MAX_VALUE;
        c.fogDensity=Float.NaN;c.weatherIntensity=Float.POSITIVE_INFINITY;c.skyTop=0xFF445566;c.skyboxFile=null;
        c.normalize();
        assertEquals("Nebula",c.theme);assertEquals("Server",c.weather);assertEquals(23,c.visualHour);assertEquals(1000,c.stars);
        assertEquals(.003F,c.fogDensity,0);assertEquals(1F,c.weatherIntensity,0);assertEquals(0x445566,c.skyTop);assertEquals("",c.skyboxFile);
    }
    @Test public void themePresetsHaveDifferentPalettesAndKeepEffectsOptional() {
        WorldConfig c=new WorldConfig();int first=c.skyTop;c.applyTheme("Aurora");
        assertNotEquals(first,c.skyTop);assertEquals("Aurora",c.theme);assertFalse(c.enabled);
        int aurora=c.skyTop;c.applyTheme("Japanese");assertNotEquals(aurora,c.skyTop);
        c.applyTheme("unknown");assertEquals("Nebula",c.theme);assertEquals(first,c.skyTop);
    }
    @Test public void qualityAndBudgetBothLimitEffects() {
        EffectsConfig c=new EffectsConfig();c.budget=1000;c.quality="Low";assertEquals(32,c.effectiveBudget());
        c.quality="High";assertEquals(256,c.effectiveBudget());c.budget=7;assertEquals(7,c.effectiveBudget());
        c.budget=-20;assertEquals(0,c.effectiveBudget());
    }
    @Test public void malformedEffectsCannotAllocateUnboundedParticlesOrExcessiveTint() {
        EffectsConfig c=new EffectsConfig();c.quality=null;c.budget=Integer.MAX_VALUE;c.distance=Float.NaN;
        c.size=Float.POSITIVE_INFINITY;c.filterStrength=1F;c.swayStrength=Float.NaN;c.color=0xFF112233;c.normalize();
        assertEquals("Balanced",c.quality);assertEquals(256,c.budget);assertEquals(96,c.effectiveBudget());
        assertEquals(24F,c.distance,0);assertEquals(8F,c.size,0);assertEquals(.25F,c.filterStrength,0);
        assertEquals(.4F,c.swayStrength,0);assertEquals(0x112233,c.color);
        assertFalse(c.enabled);assertFalse(c.cameraSway);assertFalse(c.colorFilter);
    }
}
