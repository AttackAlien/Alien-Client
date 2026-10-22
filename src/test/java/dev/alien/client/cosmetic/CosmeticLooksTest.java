package dev.alien.client.cosmetic;

import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class CosmeticLooksTest {
    @Test public void eachPresetHasDistinctVisibleAppearanceAndPreservesFit() {
        java.util.Set<String> names=new java.util.HashSet<String>();
        for(String name:CosmeticLooks.NAMES){
            CosmeticConfig.Accessory a=new CosmeticConfig.Accessory();
            a.offsetY=2.5f; a.rotationZ=33f; a.scale=1.5f;
            a.modelFile="external.json";a.textureFile="external.png";
            CosmeticLooks.apply(a,name);
            assertEquals(2.5f,a.offsetY,0);
            assertEquals(33f,a.rotationZ,0);
            assertEquals(1.5f,a.scale,0);
            assertEquals("",a.modelFile);assertEquals("",a.textureFile);
            assertTrue(a.enabled);
            assertTrue(names.add(a.hat+"|"+a.face+"|"+a.back+"|"+a.color));
        }
        assertEquals(4,names.size());
    }

    @Test public void savedLooksAreIndependentCopiesAndSurviveJson() {
        CosmeticConfig config=new CosmeticConfig();
        CosmeticLooks.apply(config.self,"Phantom");
        config.self.scale=2.25f;
        config.saveLook(1,config.self);
        config.self.scale=.8f;
        CosmeticConfig.Accessory saved=config.loadLook(1);
        assertEquals(2.25f,saved.scale,0);
        saved.scale=1.8f;
        assertEquals(2.25f,config.loadLook(1).scale,0);
        CosmeticConfig roundTrip=new Gson().fromJson(new Gson().toJson(config),CosmeticConfig.class);
        roundTrip.normalize();
        assertEquals("FUTURE",roundTrip.loadLook(1).face);
        assertEquals(2.25f,roundTrip.loadLook(1).scale,0);
        roundTrip.saveLook(2,roundTrip.loadLook(1));
        assertEquals(2,roundTrip.savedLooks.size());
    }

    @Test public void discardsUnexpectedSlotsAndClampsMalformedImports() {
        CosmeticConfig config=new CosmeticConfig();
        config.savedLooks.put("999",new CosmeticConfig.Accessory());
        config.savedLooks.put("1",null);
        CosmeticConfig.Accessory unsafe=new CosmeticConfig.Accessory();
        unsafe.scale=Float.POSITIVE_INFINITY;unsafe.modelFile="../bad.json";
        config.savedLooks.put("3",unsafe);
        config.normalize();
        assertEquals(1,config.savedLooks.size());
        assertNull(config.loadLook(4));
        assertNull(config.loadLook(1));
        assertEquals(.25f,config.loadLook(3).scale,0);
        assertEquals("",config.loadLook(3).modelFile);
        assertEquals(1,config.savedLooks.size());
    }

    @Test public void loadingIntoPlayerOverrideDoesNotShareOwnerOrSavedSlot() {
        CosmeticConfig config=new CosmeticConfig();
        config.previewLight=.35f; config.previewBackground="Graphite";
        CosmeticLooks.apply(config.self,"Sakura");
        config.self.offsetX=3; config.self.rotationY=-40; config.self.scale=1.4f;
        config.saveLook(3,config.self);
        String player="12345678-1234-1234-1234-123456789abc";
        config.players.put(player,config.loadLook(3));
        config.players.get(player).color=0x123456;
        config.players.get(player).offsetX=9;
        config.normalize();
        assertEquals(3,config.self.offsetX,0);
        assertEquals(3,config.loadLook(3).offsetX,0);
        assertEquals(9,config.players.get(player).offsetX,0);
        assertEquals(0xEFADCC,config.loadLook(3).color);
        assertEquals(.35f,config.previewLight,0);
        assertEquals("Graphite",config.previewBackground);
    }

    @Test public void olderCosmeticProfilesGetEmptyLookLibrary() {
        CosmeticConfig old=new Gson().fromJson("{\"enabled\":true,\"self\":{\"hat\":\"SAMURAI\"}}",CosmeticConfig.class);
        old.normalize();
        assertNotNull(old.savedLooks);
        assertTrue(old.savedLooks.isEmpty());
        assertEquals("SAMURAI",old.self.hat);
    }
}
