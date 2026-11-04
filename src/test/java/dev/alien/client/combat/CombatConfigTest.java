package dev.alien.client.combat;

import com.google.gson.Gson;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.module.ModuleManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class CombatConfigTest {
    @Test public void isDisabledByDefaultAndOldProfilesAreSafe() {
        ClientConfig c=new Gson().fromJson("{\"version\":1,\"theme\":\"Nebula\"}",ClientConfig.class);
        c.normalize();
        assertEquals("OFF",c.combat.preset);
        assertFalse(c.combat.enabled);
        assertFalse(c.combat.aimAssist);
        assertFalse(c.combat.clickAssist);
        assertEquals("Nebula",c.theme);
    }

    @Test public void legitFullAndCustomAreOneConfigNotSeparateClients() {
        CombatConfig c=new CombatConfig();
        c.applyPreset("LEGIT");
        assertTrue(c.enabled);
        assertTrue(c.aimAssist);
        assertFalse(c.clickAssist);
        assertTrue(c.requireAttackHeld);
        assertEquals(.12F,c.aimStrength,0);
        c.applyPreset("FULL");
        assertTrue(c.aimAssist);
        assertTrue(c.clickAssist);
        assertFalse(c.requireAttackHeld);
        c.aimFov=71F;
        c.applyPreset("CUSTOM");
        assertEquals(71F,c.aimFov,0F);
        assertEquals("CUSTOM",c.preset);
        c.applyPreset("OFF");
        assertFalse(c.enabled);
        assertFalse(c.clickAssist);
    }

    @Test public void everyNumericParameterClampsNonFiniteAndExcessiveInputs() {
        CombatConfig c=new CombatConfig();
        c.preset="invalid";
        c.aimStrength=Float.NaN;
        c.aimFov=Float.POSITIVE_INFINITY;
        c.targetRange=10000F;
        c.clicksPerSecond=-1F;
        c.normalize();
        assertEquals("OFF",c.preset);
        assertEquals(.15F,c.aimStrength,0);
        assertEquals(35F,c.aimFov,0);
        assertEquals(6F,c.targetRange,0);
        assertEquals(1F,c.clicksPerSecond,0);
    }

    @Test public void savedSettingsRestoreAcrossJson() {
        ClientConfig c=new ClientConfig();
        c.combat.applyPreset("FULL");
        c.combat.clicksPerSecond=9;
        c.combat.visibleTargetsOnly=false;
        ClientConfig from=new Gson().fromJson(new Gson().toJson(c),ClientConfig.class);
        from.normalize();
        assertTrue(from.combat.enabled);
        assertTrue(from.combat.clickAssist);
        assertFalse(from.combat.visibleTargetsOnly);
        assertEquals(9F,from.combat.clicksPerSecond,0F);
    }

    @Test public void unifiedRegistryAddsRealCombatSwitchesWithoutAffectingVisualOnlyRegistry() {
        ClientConfig c=new ClientConfig();
        ModuleManager legacy=ModuleManager.visuals(()->c,false);
        assertEquals(9,legacy.modules().size());
        assertNull(legacy.find("aim_assist"));
        if("unified".equals(BuildEdition.ID)) {
            ModuleManager unified=ModuleManager.visuals(()->c,true);
            assertEquals(17,unified.modules().size());
            assertNotNull(unified.find("aim_assist"));
            assertNotNull(unified.find("click_assist"));
            assertTrue(unified.toggle("aim_assist"));
            assertTrue(c.combat.aimAssist);
        }
    }
}
