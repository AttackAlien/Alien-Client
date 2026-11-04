package dev.alien.client.hud;
import com.google.gson.Gson;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.config.ConfigStore;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
public class HudUltimateTest {
    @Rule public TemporaryFolder folder=new TemporaryFolder();
    @Test public void legacyLayoutsKeepPositionsAndNewWidgetsAreOptIn(){
        HudConfig h=new Gson().fromJson("{\"widgets\":{\"fps\":{\"enabled\":true,\"x\":0.7,\"scale\":1.8}}}",HudConfig.class);h.ensureDefaults();
        assertEquals(.7f,h.widgets.get("fps").x,0);assertEquals(1.8f,h.widgets.get("fps").scale,0);
        assertFalse(h.widgets.get("target").enabled);assertFalse(h.widgets.get("crosshair").enabled);
        assertNotNull(h.crosshair);assertEquals(15,h.widgets.size());
    }
    @Test public void presetsAreHudOnlyAndKeepFutureWidgets(){
        ClientConfig c=new ClientConfig();c.combat.applyPreset("FULL");c.world.skyboxFile="night.cube.png";
        c.hud.widgets.put("future",new HudConfig.Widget(false,.2f,.7f,2));
        for(String name:new String[]{"PvP","Survival","Cinematic"}){
            c.hud.applyPreset(name);assertNotNull(c.hud.widgets.get("future"));assertTrue(c.combat.enabled);
            assertEquals("FULL",c.combat.preset);assertEquals("night.cube.png",c.world.skyboxFile);
            assertEquals(16,c.hud.widgets.size());
        }
        assertFalse(c.hud.widgets.get("crosshair").enabled);assertFalse(c.hud.widgets.get("keys").enabled);
    }
    @Test public void invalidImportedValuesCannotPoisonGeometry(){
        HudConfig c=new HudConfig();c.targetHold=Float.NaN;c.targetRange=Float.POSITIVE_INFINITY;
        c.crosshair.length=Float.NaN;c.crosshair.thickness=100;c.crosshair.gap=-50;c.widgets.get("target").opacity=Float.NaN;
        c.ensureDefaults();assertEquals(3,c.targetHold,0);assertEquals(32,c.targetRange,0);
        assertEquals(6,c.crosshair.length,0);assertEquals(4,c.crosshair.thickness,0);assertEquals(0,c.crosshair.gap,0);
        assertEquals(1,c.widgets.get("target").opacity,0);
    }
    @Test public void smoothingIsFrameRateIndependentAndBounded(){
        float thirty=1,oneTwenty=1;
        for(int i=0;i<30;i++)thirty=HudMath.smooth(thirty,.25f,1/30f,8);
        for(int i=0;i<120;i++)oneTwenty=HudMath.smooth(oneTwenty,.25f,1/120f,8);
        assertEquals(thirty,oneTwenty,.00001f);assertTrue(thirty>=.25f&&thirty<.251f);
        assertEquals(0,HudMath.ratio(Float.NaN,20),0);assertEquals(0,HudMath.ratio(4,0),0);assertEquals(1,HudMath.ratio(40,20),0);
    }
    @Test public void profileRoundTripPreservesFullAndHudPreferencesIndependently()throws Exception{
        ConfigStore store=new ConfigStore(folder.newFolder());store.load();ClientConfig c=store.config();
        c.combat.applyPreset("FULL");c.hud.applyPreset("PvP");c.hud.theme="Cyan";c.hud.crosshair.shape="Circle";
        c.hud.widgets.get("target").opacity=.55f;c.hud.indicatorSpeed=false;store.saveProfile("ultimate");store.resetDefaults();
        assertTrue(store.loadProfile("ultimate"));assertEquals("Circle",store.config().hud.crosshair.shape);
        assertEquals(.55f,store.config().hud.widgets.get("target").opacity,0);assertFalse(store.config().hud.indicatorSpeed);
        assertTrue(store.config().combat.tools.aimbot);assertEquals("FULL",store.config().combat.preset);
    }
}
