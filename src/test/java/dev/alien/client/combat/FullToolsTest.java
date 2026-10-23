package dev.alien.client.combat;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.module.ModuleManager;
import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;
public class FullToolsTest {
    @Test public void defaultsAndOldProfilesDoNotActivateNewTools(){
        CombatConfig c=new Gson().fromJson("{\"enabled\":true,\"aimAssist\":true}",CombatConfig.class);c.normalize();
        assertFalse(c.tools.autoFood);assertFalse(c.tools.autoPotion);assertFalse(c.tools.bridge);assertFalse(c.tools.aimbot);
    }
    @Test public void offAndLegitClearFullModulesButFullIsDistinct(){
        CombatConfig c=new CombatConfig();c.applyPreset("FULL");assertTrue(c.tools.aimbot);assertTrue(c.tools.autoFood);
        assertTrue(c.tools.autoPotion);assertFalse(c.tools.bridge);assertFalse(c.tools.antiAim);
        c.applyPreset("LEGIT");assertFalse(c.tools.aimbot);assertFalse(c.tools.autoFood);assertTrue(c.aimAssist);
        c.tools.bridge=true;c.applyPreset("OFF");assertFalse(c.tools.bridge);assertFalse(c.enabled);
    }
    @Test public void normalizationBoundsThresholdsAndResolvesConflictingRotation(){
        FullToolsConfig f=new FullToolsConfig();f.foodThreshold=100;f.potionHealth=Float.NaN;f.spinSpeed=Float.POSITIVE_INFINITY;f.aimbot=f.antiAim=true;
        f.normalize();assertEquals(19,f.foodThreshold);assertEquals(12,f.potionHealth,0);assertEquals(120,f.spinSpeed,0);assertFalse(f.antiAim);
    }
    @Test public void registryTogglesRealStateWithoutEnablingMasterAndRemainsEditionBounded(){
        ClientConfig c=new ClientConfig();ModuleManager legacy=ModuleManager.visuals(()->c,false);
        assertNull(legacy.find("auto_food"));
        if("unified".equals(BuildEdition.ID)){
            ModuleManager m=ModuleManager.visuals(()->c,true);assertEquals(17,m.modules().size());
            assertTrue(m.toggle("auto_food"));assertTrue(c.combat.tools.autoFood);assertFalse(c.combat.enabled);
            m.toggle("aimbot");assertTrue(c.combat.tools.aimbot);m.toggle("anti_aim");assertTrue(c.combat.tools.antiAim);assertFalse(c.combat.tools.aimbot);assertFalse(c.combat.aimAssist);
        }
    }
}
