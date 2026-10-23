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
    @Test public void optionalSkyboxSettingsNormalizeButPreserveOldFields() {
        WorldConfig c=new WorldConfig();c.skyboxYaw=Float.NaN;c.skyboxDrift=Float.POSITIVE_INFINITY;
        c.skyboxBrightness=-5F;c.skyboxFile="nebula.cube.png";c.weather="Rain";c.timeOverride=true;
        c.normalize();
        assertEquals(0F,c.skyboxYaw,0F);assertEquals(0F,c.skyboxDrift,0F);
        assertEquals(.2F,c.skyboxBrightness,0F);
        assertEquals("nebula.cube.png",c.skyboxFile);assertEquals("Rain",c.weather);assertTrue(c.timeOverride);
        c.skyboxYaw=900F;c.skyboxDrift=8F;c.skyboxBrightness=Float.NaN;c.normalize();
        assertEquals(360F,c.skyboxYaw,0F);assertEquals(2F,c.skyboxDrift,0F);
        assertEquals(1F,c.skyboxBrightness,0F);
    }

    @Test public void unifiedSkyProfileKeepsCubemapOptionsAlongsideRedesignSettings() {
        String json="{\"skyboxFile\":\"nebula.cube.png\",\"skyboxYaw\":147,\"skyboxDrift\":0.65,"
                +"\"skyboxBrightness\":0.45,\"twinklingStars\":true,\"brightness\":1.2,"
                +"\"sunSize\":1.5,\"moonSize\":0.8,\"cloudMode\":\"Hidden\","
                +"\"favoriteSkyboxes\":[\"nebula.cube.png\"]}";
        com.google.gson.Gson gson=new com.google.gson.Gson();
        WorldConfig c=gson.fromJson(json,WorldConfig.class);c.normalize();
        WorldConfig reloaded=gson.fromJson(gson.toJson(c),WorldConfig.class);reloaded.normalize();
        assertEquals("nebula.cube.png",reloaded.skyboxFile);
        assertEquals(147F,reloaded.skyboxYaw,0);assertEquals(.65F,reloaded.skyboxDrift,.0001F);
        assertEquals(.45F,reloaded.skyboxBrightness,.0001F);assertTrue(reloaded.twinklingStars);
        assertEquals(1.2F,reloaded.brightness,.0001F);assertEquals(1.5F,reloaded.sunSize,0);
        assertEquals(.8F,reloaded.moonSize,.0001F);assertEquals("Hidden",reloaded.cloudMode);
        assertTrue(reloaded.favoriteSkyboxes.contains("nebula.cube.png"));
        assertEquals(0x607890,reloaded.skyColor(0x506478));
    }

    @Test public void oldCubemapProfilesReceiveNeutralRedesignAndGalleryDefaults() {
        WorldConfig c=new com.google.gson.Gson().fromJson(
                "{\"skyboxFile\":\"old.cube.png\",\"skyboxYaw\":90}",WorldConfig.class);
        c.normalize();
        assertEquals(1F,c.brightness,0);assertEquals(1F,c.sunSize,0);assertEquals(1F,c.moonSize,0);
        assertEquals("Vanilla",c.cloudMode);assertTrue(c.favoriteSkyboxes.isEmpty());
        assertEquals(90F,c.skyboxYaw,0);assertEquals(0x506478,c.skyColor(0x506478));
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
