package dev.alien.client.cosmetic;

import com.google.gson.Gson;
import dev.alien.client.cosmetic.mesh.AccessoryPartMeshes;
import org.junit.Test;
import static org.junit.Assert.*;

public class AccessoryPartDesignTest {
    @Test public void oldLooksGainDisabledPartsWithoutChangingTheirExistingWardrobe() {
        CosmeticConfig.Accessory look = new Gson().fromJson("{\"hat\":\"SAMURAI\",\"face\":\"KITSUNE\",\"back\":\"CAPE\"}", CosmeticConfig.Accessory.class);
        look.normalize();
        assertEquals("SAMURAI", look.hat);
        assertEquals("KITSUNE", look.face);
        assertEquals("CAPE", look.back);
        assertEquals(6, look.parts.size());
        for (AccessoryPartDesign part : look.parts.values()) assertFalse(part.enabled);
    }

    @Test public void fittedPartsClampAndCopyIndependentlyIntoSavedLooks() {
        CosmeticConfig.Accessory look = new CosmeticConfig.Accessory();
        AccessoryPartDesign horns = look.parts.get("HORNS"); horns.enabled = true; horns.x = 99; horns.scale = .01f;
        look.parts.get("TAIL").style = "Crystal";
        look.normalize();
        assertEquals(8, horns.x, 0);
        assertEquals(.5f, horns.scale, 0);
        CosmeticConfig config = new CosmeticConfig(); config.saveLook(1, look);
        look.parts.get("HORNS").x = 0; look.parts.get("TAIL").enabled = true;
        CosmeticConfig.Accessory saved = config.loadLook(1);
        assertEquals(8, saved.parts.get("HORNS").x, 0);
        assertFalse(saved.parts.get("TAIL").enabled);
        assertEquals("Crystal", saved.parts.get("TAIL").style);
    }

    @Test public void eachAccessoryGroupHasTwoActualDistinctMeshes() {
        String[][] slots = {{"HORNS", "Classic", "Crescent"}, {"MASK", "Oni", "Tech"},
                {"SHOULDERS", "Dragon", "Knight"}, {"TAIL", "Spade", "Crystal"},
                {"ARMS", "Bracers", "Gauntlets"}, {"LEGS", "Greaves", "Boots"}};
        for (String[] slot : slots) {
            assertTrue(slot[0], AccessoryPartMeshes.get(slot[0], slot[1]).vertexCount() > 100);
            assertTrue(slot[0], AccessoryPartMeshes.get(slot[0], slot[2]).vertexCount() > 100);
            assertNotSame(slot[0], AccessoryPartMeshes.get(slot[0], slot[1]), AccessoryPartMeshes.get(slot[0], slot[2]));
        }
    }
}
