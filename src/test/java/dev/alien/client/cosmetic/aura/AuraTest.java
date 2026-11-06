package dev.alien.client.cosmetic.aura;

import com.google.gson.Gson;
import dev.alien.client.config.ConfigStore;
import dev.alien.client.cosmetic.CosmeticConfig;
import java.util.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class AuraTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void oldProfilesKeepTheLegacyAuraAndNoReactiveOptIn(){
        CosmeticConfig.Accessory a=new Gson().fromJson("{\"aura\":true,\"color\":123,\"trail\":true}",CosmeticConfig.Accessory.class);a.normalize();assertTrue(a.aura&&a.trail);assertEquals(123,a.color);assertFalse(a.auraDesign.enabled);
        a.auraDesign=null;a.normalize();assertNotNull(a.auraDesign);assertFalse(a.auraDesign.enabled);
    }
    @Test public void malformedPreferencesCannotProduceNonFiniteGeometry(){
        AuraDesign d=new AuraDesign();d.radius=Float.NaN;d.opacity=Float.POSITIVE_INFINITY;d.intensity=99;d.speed=-5;d.response=0;d.color=-1;d.style="unknown";d.preset=null;d.motionGain=Float.NaN;d.normalize();
        assertEquals(.75f,d.radius,0);assertEquals(.85f,d.opacity,0);assertEquals(1.5f,d.intensity,0);assertEquals(.2f,d.speed,0);assertEquals(.1f,d.response,0);assertEquals(0xffffff,d.color);assertEquals("Runes",d.style);assertEquals("Custom",d.preset);assertEquals(.8f,d.motionGain,0);
    }
    @Test public void visualNightFollowsHourAndWrapsSafely(){
        assertEquals(1,AuraResponse.nightAt(0),0);assertEquals(1,AuraResponse.nightAt(24),0);assertEquals(1,AuraResponse.nightAt(-24),0);assertEquals(0,AuraResponse.nightAt(12),0);assertEquals(0,AuraResponse.nightAt(6),0);assertEquals(0,AuraResponse.nightAt(Double.NaN),0);
        assertTrue(AuraResponse.nightAt(19)<AuraResponse.nightAt(21));
    }
    @Test public void independentTriggersActuallyRaiseAndReleaseEnergy(){
        AuraDesign d=new AuraDesign();AuraResponse state=new AuraResponse();
        for(int i=0;i<80;i++)state.update(d,.05,5.6,true,0);
        assertTrue(state.movement(1)>.99&&state.dance(1)>.99&&state.night(1)>.99);assertTrue(state.energy(d,1)>d.idle+1);
        d.motion=d.emotes=d.night=false;for(int i=0;i<100;i++)state.update(d,.05,5.6,true,0);
        assertTrue(state.movement(1)<.001&&state.dance(1)<.001&&state.night(1)<.001);assertEquals(d.idle,state.energy(d,1),.001);
    }
    @Test public void eachTriggerCanBeEnabledAlone(){
        for(int mode=0;mode<3;mode++){AuraDesign d=new AuraDesign();d.motion=mode==0;d.emotes=mode==1;d.night=mode==2;AuraResponse s=new AuraResponse();for(int i=0;i<50;i++)s.update(d,.05,5.6,true,0);
            assertEquals(mode==0?1:0,s.movement(1),.001);assertEquals(mode==1?1:0,s.dance(1),.001);assertEquals(mode==2?1:0,s.night(1),.001);}
    }
    @Test public void responseInterpolatesWithoutAdvancingWhenRendered(){
        AuraDesign d=new AuraDesign();AuraResponse s=new AuraResponse();s.update(d,.05,5.6,true,0);
        assertEquals(0,s.movement(0),0);float end=s.movement(1);assertTrue(end>0&&end<1);assertTrue(s.movement(.5f)>0&&s.movement(.5f)<end);
        double phase=s.phase(1);for(int i=0;i<1000;i++){s.movement(.7f);s.energy(d,.5f);s.phase(.3f);}assertEquals(phase,s.phase(1),0);
    }
    @Test public void renderFpsAndTickSubdivisionDoNotChangeTheReaction(){
        AuraDesign d=new AuraDesign();AuraResponse a=new AuraResponse(),b=new AuraResponse();
        for(int i=0;i<100;i++){a.update(d,.05,4,true,0);b.update(d,.025,4,true,0);b.update(d,.025,4,true,0);for(int frame=0;frame<10;frame++)a.energy(d,frame*.1f);}
        assertEquals(a.energy(d,1),b.energy(d,1),1e-5);assertEquals(a.phase(1),b.phase(1),1e-9);
    }
    @Test public void badTimeAndMovementDoNotPoisonStateAndClearResetsIt(){
        AuraDesign d=new AuraDesign();AuraResponse s=new AuraResponse();s.update(d,Double.NaN,1,true,0);assertEquals(0,s.phase(1),0);
        for(int i=0;i<20;i++)s.update(d,.05,Double.POSITIVE_INFINITY,false,Double.NaN);assertEquals(0,s.movement(1),0);assertEquals(0,s.night(1),0);assertTrue(Float.isFinite(s.energy(d,Float.NaN)));s.clear();assertEquals(0,s.phase(1),0);
    }
    @Test public void presetsAreDistinctAndUnknownNamesCannotPartiallyMutate(){
        Set<String> styles=new HashSet<String>();for(String name:AuraDesign.PRESETS){AuraDesign d=new AuraDesign();assertTrue(d.applyPreset(name));assertTrue(d.enabled);assertTrue(styles.add(d.style));d.normalize();assertEquals(name,d.preset);}
        AuraDesign d=new AuraDesign();String before=new Gson().toJson(d);assertFalse(d.applyPreset("fake"));assertEquals(before,new Gson().toJson(d));
    }
    @Test public void everyMeshIsFiniteBudgetedAndHasFeatheredTransparentEdges(){
        for(String name:AuraDesign.PRESETS){AuraDesign d=new AuraDesign();d.applyPreset(name);AuraResponse s=new AuraResponse();for(int i=0;i<30;i++)s.update(d,.05,5.6,true,0);
            for(int budget=0;budget<=192;budget++){final int[] vertices={0},transparent={0},opaque={0};int used=AuraGeometry.emit(d,s,.5f,budget,false,(x,y,z,color,alpha)->{assertTrue(Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z));assertTrue(Math.abs(x)<3&&Math.abs(y)<3&&Math.abs(z)<3);assertTrue(alpha>=0&&alpha<=1);vertices[0]++;if(alpha==0)transparent[0]++;else opaque[0]++;});
                assertTrue(used<=budget);assertTrue(vertices[0]<=used*24);assertEquals(0,vertices[0]%3);if(budget>=24){assertTrue(transparent[0]>0);assertTrue(opaque[0]>0);}
            }
        }
    }
    @Test public void zeroIdleAndNoEventsProducesNoHiddenGeometry(){AuraDesign d=new AuraDesign();d.idle=0;AuraResponse s=new AuraResponse();assertEquals(0,AuraGeometry.emit(d,s,1,192,true,(x,y,z,c,a)->fail("Unexpected vertex")));}
    @Test public void profileExportImportAndLookSnapshotsKeepIndependentAuraDesigns()throws Exception{
        ConfigStore store=new ConfigStore(temp.newFolder());store.load();CosmeticConfig.Accessory a=store.config().cosmetics.self;a.aura=true;a.auraDesign.applyPreset("Neon");a.auraDesign.motion=false;a.auraDesign.nightGain=1.25f;a.auraDesign.color=0x123456;store.config().cosmetics.saveLook(1,a);
        a.auraDesign.radius=1.3f;assertNotEquals(a.auraDesign.radius,store.config().cosmetics.loadLook(1).auraDesign.radius,0);
        store.config().emotes.slots[0]="take_the_l";a.trailDesign.applyPreset("Ember");store.saveProfile("auras");String text=store.profileJson("auras");store.resetDefaults();store.importProfileJson("copy",text);assertTrue(store.loadProfile("copy"));a=store.config().cosmetics.self;
        assertTrue(a.aura&&a.auraDesign.enabled);assertEquals("Orbit",a.auraDesign.style);assertFalse(a.auraDesign.motion);assertEquals(1.25f,a.auraDesign.nightGain,0);assertEquals(0x123456,a.auraDesign.color);assertEquals(1.3f,a.auraDesign.radius,0);assertEquals("take_the_l",store.config().emotes.slots[0]);assertEquals("Ember",a.trailDesign.preset);
        CosmeticConfig.Accessory copy=a.copy();copy.auraDesign.color=0;assertEquals(0x123456,a.auraDesign.color);
    }
    @Test public void boundedAuraClockHasNoVisibleGeometryJumpAtWrap(){
        AuraDesign d=new AuraDesign();d.applyPreset("Celestial");d.speed=1;AuraResponse state=new AuraResponse();for(int i=0;i<479;i++)state.update(d,.25,0,false,12);state.update(d,.249,0,false,12);
        java.util.List<double[]> before=new java.util.ArrayList<double[]>();AuraGeometry.emit(d,state,1,104,false,(x,y,z,c,a)->before.add(new double[]{x,y,z}));
        state.update(d,.002,0,false,12);final int[] index={0};AuraGeometry.emit(d,state,1,104,false,(x,y,z,c,a)->{double[] old=before.get(index[0]++);assertEquals(old[0],x,.02);assertEquals(old[1],y,.02);assertEquals(old[2],z,.02);});assertEquals(before.size(),index[0]);
    }
}
