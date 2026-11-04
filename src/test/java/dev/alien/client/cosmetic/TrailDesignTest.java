package dev.alien.client.cosmetic;

import com.google.gson.Gson;
import dev.alien.client.config.ConfigStore;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class TrailDesignTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void interpolatesActualThreeStopColorsWidthAndOpacity() {
        TrailDesign d=new TrailDesign();d.tailColor=0;d.middleColor=0xff0000;d.headColor=0xffffff;
        d.tailWidth=0;d.middleWidth=1.5f;d.headWidth=.25f;
        d.tailOpacity=.1f;d.middleOpacity=.8f;d.headOpacity=.4f;
        assertEquals(0,d.colorAt(0));assertEquals(0xff0000,d.colorAt(.5f));assertEquals(0xffffff,d.colorAt(1));
        assertEquals(0x800000,d.colorAt(.25f));assertEquals(0xff8080,d.colorAt(.75f));
        assertEquals(.75f,d.widthAt(.25f),1e-6);assertEquals(.875f,d.widthAt(.75f),1e-6);
        assertEquals(.45f,d.opacityAt(.25f),1e-6);assertEquals(.6f,d.opacityAt(.75f),1e-6);
        assertEquals(d.tailColor,d.colorAt(Float.NaN));assertEquals(d.headColor,d.colorAt(99));
    }
    @Test public void malformedImportsHaveFiniteBoundedCurvesAndSafeNames() {
        TrailDesign d=new TrailDesign();d.texture="https://external";d.customTexture="../escape.png";d.preset=null;
        d.tiling=Float.POSITIVE_INFINITY;d.scroll=99;d.tailWidth=Float.NaN;d.middleWidth=9;d.headOpacity=-1;d.tailColor=-1;
        d.normalize();assertEquals("Soft",d.texture);assertEquals("",d.customTexture);assertEquals("Custom",d.preset);
        assertEquals(.25f,d.tiling,0);assertEquals(2,d.scroll,0);assertEquals(0,d.tailWidth,0);
        assertEquals(1.5f,d.middleWidth,0);assertEquals(0,d.headOpacity,0);assertEquals(0xffffff,d.tailColor);
    }
    @Test public void legacyJsonPreservesRibbonAndParticleChoicesWithoutEnablingDesigner() {
        for(String style:new String[]{"Ribbon","Particles"}) {
            CosmeticConfig.Accessory a=new Gson().fromJson("{\"trail\":true,\"trailStyle\":\""+style+"\",\"trailWidth\":0.3,\"color\":123}",CosmeticConfig.Accessory.class);
            a.normalize();assertFalse(a.trailDesign.enabled);assertEquals(style,a.trailStyle);assertEquals(.3f,a.trailWidth,0);assertEquals(123,a.color);
            a.trailDesign=null;a.normalize();assertNotNull(a.trailDesign);assertFalse(a.trailDesign.enabled);
        }
    }
    @Test public void savedLooksAndPlayerCopiesDoNotShareEditableStops() {
        CosmeticConfig c=new CosmeticConfig();c.self.trailDesign.applyPreset("Nebula");c.saveLook(2,c.self);
        CosmeticConfig.Accessory other=c.self.copy();other.trailDesign.color(1,0x123456);other.trailDesign.point(1,true,.2f);
        assertNotEquals(other.trailDesign.middleColor,c.self.trailDesign.middleColor);
        CosmeticConfig.Accessory saved=c.loadLook(2);saved.trailDesign.headWidth=0;
        assertTrue(c.loadLook(2).trailDesign.headWidth>0);
        CosmeticConfig restored=new Gson().fromJson(new Gson().toJson(c),CosmeticConfig.class);restored.normalize();
        assertEquals("Nebula",restored.loadLook(2).trailDesign.preset);assertNotSame(restored.self.trailDesign,restored.loadLook(2).trailDesign);
    }
    @Test public void realUnifiedProfilesExportImportAndRestoreAllTrailFields() throws Exception {
        ConfigStore store=new ConfigStore(temp.newFolder());store.load();
        CosmeticConfig.Accessory a=store.config().cosmetics.self;a.hat="SAKURA";a.trail=true;a.trailDesign.applyPreset("Ember");
        a.trailDesign.texture="PNG";a.trailDesign.customTexture="my-streak.png";a.trailDesign.scroll=-.7f;a.trailDesign.tiling=3.2f;
        a.trailDesign.point(0,false,.35f);a.trailDesign.point(2,true,.25f);a.trailDesign.color(1,0x123456);
        store.config().cosmetics.saveLook(3,a);store.saveProfile("trail");String text=store.profileJson("trail");
        store.resetDefaults();store.importProfileJson("imported",text);assertTrue(store.loadProfile("imported"));
        a=store.config().cosmetics.self;assertEquals("SAKURA",a.hat);assertTrue(a.trail&&a.trailDesign.enabled);
        assertEquals("my-streak.png",a.trailDesign.customTexture);assertEquals(-.7f,a.trailDesign.scroll,0);assertEquals(3.2f,a.trailDesign.tiling,0);
        assertEquals(.35f,a.trailDesign.tailWidth,0);assertEquals(.25f,a.trailDesign.headOpacity,0);assertEquals(0x123456,a.trailDesign.middleColor);
        assertEquals(.25f,store.config().cosmetics.loadLook(3).trailDesign.headOpacity,0);
    }
    @Test public void presetsProduceDistinctMaterialsAndEditsBecomeCustom() {
        java.util.Set<String> looks=new java.util.HashSet<String>();
        for(String name:TrailDesign.PRESETS){TrailDesign d=new TrailDesign();assertTrue(d.applyPreset(name));d.normalize();assertTrue(d.enabled);assertTrue(TrailDesign.isBuiltin(d.texture));assertTrue(looks.add(d.texture+":"+d.colorAt(.5f)));d.point(1,false,.3f);assertEquals("Custom",d.preset);}
        TrailDesign d=new TrailDesign();assertFalse(d.applyPreset("not-implemented"));assertFalse(d.enabled);
    }
    @Test public void textureDistanceUsesRecordedMovementAndSurvivesExpiryWithoutStretchedUvs() {
        RibbonPath p=new RibbonPath();p.add(0,0,0,0);p.add(3,0,4,1);p.add(6,0,8,2);
        assertEquals(2.5,p.distanceAt(0,.5),1e-9);assertEquals(10,p.distanceAt(1,1),1e-9);
        p.expire(3,3);assertEquals(2,p.size());assertEquals(5,p.distanceAt(0,0),1e-9);
        p.add(100,0,100,4);assertEquals(1,p.size());assertEquals(0,p.distanceAt(0,0),0);
    }
}
