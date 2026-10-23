package dev.alien.client.config;

import dev.alien.client.cosmetic.CosmeticLooks;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/** Cross-feature regressions at the 0.3 Unified / 0.4 redesign boundary. */
public class UnifiedRedesignProfileTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void legacyUnifiedProfileMigratesAppearanceWithoutResettingFeatures() throws Exception {
        File folder = temp.newFolder();
        String legacy = "{\"version\":1,\"accentColor\":-15584170,\"opacity\":0.61,\"animationSpeed\":1.7,"
                + "\"moduleKeys\":{\"aim_assist\":34},\"combat\":{\"preset\":\"FULL\",\"enabled\":true,\"aimAssist\":true},"
                + "\"world\":{\"skyboxFile\":\"night.cube.png\",\"skyboxYaw\":175},"
                + "\"hud\":{\"widgets\":{\"fps\":{\"x\":0.4,\"y\":0.2,\"enabled\":true}}}}";
        Files.write(new File(folder,"config.json").toPath(),legacy.getBytes(StandardCharsets.UTF_8));
        ConfigStore store = new ConfigStore(folder); store.load();
        ClientConfig c = store.config();
        assertEquals(0xff123456,c.ui.primary);
        assertEquals(.61f,c.ui.opacity,.0001f);
        assertEquals(1.7f,c.ui.animationSpeed,.0001f);
        assertTrue(c.combat.enabled); assertTrue(c.combat.aimAssist);
        assertEquals(Integer.valueOf(34),c.moduleKeys.get("aim_assist"));
        assertEquals("night.cube.png",c.world.skyboxFile);
        assertEquals(175f,c.world.skyboxYaw,0);
        assertEquals(.4f,c.hud.widgets.get("fps").x,.0001f);
        store.save(); store.load();
        assertEquals(0xff123456,store.config().ui.primary);
    }

    @Test public void profileRoundTripRetainsBothStudiosCombatAndRedesignPreferences() throws Exception {
        ConfigStore store = new ConfigStore(temp.newFolder()); store.load();
        ClientConfig c = store.config();
        c.combat.applyPreset("FULL"); c.moduleKeys.put("click_assist",35);
        c.ui.applyPreset("Cyber Cyan"); c.ui.menuScale=.85f; c.ui.blur=.4f;
        c.interfacePrefs.favorites.add("aim_assist");
        c.world.skyboxFile="studio.cube.png"; c.world.skyboxYaw=220;
        c.world.favoriteSkyboxes.add("studio.cube.png"); c.world.cloudMode="Hidden";
        CosmeticLooks.apply(c.cosmetics.self,"Sakura");
        c.cosmetics.saveLook(2,c.cosmetics.self); c.cosmetics.previewLight=.45f;
        c.hud.modernFont=false; c.hud.opacity=.72f;
        store.saveProfile("integrated");
        store.resetDefaults(); assertTrue(store.loadProfile("integrated"));
        ClientConfig loaded=store.config();
        assertTrue(loaded.combat.enabled); assertTrue(loaded.combat.clickAssist);
        assertEquals(Integer.valueOf(35),loaded.moduleKeys.get("click_assist"));
        assertEquals("Cyber Cyan",loaded.ui.preset); assertEquals(.85f,loaded.ui.menuScale,0);
        assertEquals(.4f,loaded.ui.blur,0); assertTrue(loaded.interfacePrefs.favorites.contains("aim_assist"));
        assertEquals(220f,loaded.world.skyboxYaw,0);
        assertTrue(loaded.world.favoriteSkyboxes.contains("studio.cube.png"));
        assertEquals("Hidden",loaded.world.cloudMode);
        assertEquals("KITSUNE",loaded.cosmetics.loadLook(2).face);
        assertEquals(.45f,loaded.cosmetics.previewLight,0);
        assertFalse(loaded.hud.modernFont); assertEquals(.72f,loaded.hud.opacity,0);
        loaded.cosmetics.self.color=0x112233;
        assertNotEquals(0x112233,loaded.cosmetics.loadLook(2).color);
    }

    @Test public void explicitDesignTokensWinOverLegacyAliases() throws Exception {
        ConfigStore store=new ConfigStore(temp.newFolder()); store.load();
        store.config().accentColor=0xff112233;
        store.config().ui.primary=0xff445566;
        store.config().ui.opacity=.8f;
        store.save(); store.load();
        assertEquals(0xff445566,store.config().ui.primary);
        assertEquals(.8f,store.config().ui.opacity,0);
    }
}
