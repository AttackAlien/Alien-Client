package dev.alien.client.emote;

import com.google.gson.Gson;
import dev.alien.client.config.*;
import java.util.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class EmoteTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void classicsAreFirstAndAllLibrarySamplesStayFiniteAndBounded(){
        assertArrayEquals(new String[]{"floss","orange_justice","take_the_l"},Arrays.copyOf(EmoteLibrary.IDS,3));
        EmotePose p=new EmotePose();Set<String> poses=new HashSet<String>();
        for(String id:EmoteLibrary.IDS){
            for(int i=0;i<500;i++){EmoteLibrary.sample(id,i*.037,p);assertTrue(p.finite());for(float angle:p.angles)assertTrue(Math.abs(angle)<3.5);assertTrue(Math.abs(p.lift)<.2);}
            EmoteLibrary.sample(id,.8,p);assertTrue(poses.add(Arrays.toString(p.angles)));
        }
    }
    @Test public void eachRequestedDanceActuallyChangesAcrossTime(){
        for(String id:Arrays.copyOf(EmoteLibrary.IDS,3)){EmotePose a=new EmotePose(),b=new EmotePose();EmoteLibrary.sample(id,.2,a);EmoteLibrary.sample(id,.7,b);assertFalse(Arrays.equals(a.angles,b.angles));}
    }
    @Test public void invalidLibraryInputClearsPreviousPose(){
        EmotePose p=new EmotePose();EmoteLibrary.sample("floss",1,p);assertTrue(p.visible());EmoteLibrary.sample("unknown",1,p);assertFalse(p.visible());
        EmoteLibrary.sample("orange_justice",Double.NaN,p);assertTrue(p.finite());EmoteLibrary.sample(null,0,p);assertFalse(p.visible());
    }
    @Test public void tickPlaybackDoesNotDependOnNumberOfRenderedSamples(){
        EmotePlayback a=new EmotePlayback(),b=new EmotePlayback();a.play("floss",1,true);b.play("floss",1,true);
        EmotePose x=new EmotePose(),y=new EmotePose();
        for(int t=0;t<80;t++){a.update(.05);b.update(.05);for(int frame=0;frame<12;frame++)a.sample(frame*.004,x);}
        a.sample(0,x);b.sample(0,y);assertArrayEquals(x.angles,y.angles,0);assertEquals(a.phase(),b.phase(),0);
    }
    @Test public void smoothStartSwitchAndStopHaveNoPoseDiscontinuity(){
        EmotePlayback p=new EmotePlayback();EmotePose a=new EmotePose(),b=new EmotePose();p.play("floss",1,true);p.sample(0,a);assertFalse(a.visible());
        p.update(.25);p.sample(0,a);assertTrue(a.visible());p.play("take_the_l",1,true);p.sample(0,b);assertArrayEquals(a.angles,b.angles,0);
        p.update(.25);p.sample(0,a);p.stop();p.sample(0,b);assertArrayEquals(a.angles,b.angles,0);p.update(.1);p.sample(0,b);
        assertTrue(b.finite());for(float angle:b.angles)assertTrue(Math.abs(angle)<3.5);p.update(.1);assertFalse(p.active());p.sample(0,b);assertFalse(b.visible());
    }
    @Test public void speedAndLoopHaveBoundedPhase(){
        EmotePlayback p=new EmotePlayback();p.play("orange_justice",1.5f,true);for(int i=0;i<10000;i++)p.update(.05);assertTrue(p.active());assertTrue(p.phase()>=0&&p.phase()<4);
        p.clear();p.play("floss",.5f,true);p.update(.2);assertEquals(.1,p.phase(),1e-9);
    }
    @Test public void singleShotGesturesFinishEvenWhenLoopIsRequested(){
        for(String id:new String[]{"dab","wave"}){EmotePlayback p=new EmotePlayback();assertFalse(EmoteLibrary.loopable(id));p.play(id,1,true);for(int i=0;i<100;i++)p.update(.05);assertFalse(p.active());}
        EmotePlayback p=new EmotePlayback();p.play("floss",1,false);for(int i=0;i<100;i++)p.update(.05);assertFalse(p.active());
    }
    @Test public void invalidPlaybackCannotCorruptActiveDance(){
        EmotePlayback p=new EmotePlayback();assertFalse(p.play("unknown",1,true));assertFalse(p.active());p.play("floss",Float.NaN,true);p.update(Double.NaN);p.update(-1);p.update(.05);assertEquals(.05,p.phase(),1e-9);
        assertFalse(p.play(null,1,true));assertEquals("floss",p.id());EmotePose pose=new EmotePose();p.sample(Double.POSITIVE_INFINITY,pose);assertTrue(pose.finite());p.clear();assertFalse(p.active());
    }
    @Test public void corruptPreferencesNormalizeWithoutSharedSlotArrays(){
        EmoteConfig c=new EmoteConfig();c.speed=Float.NaN;c.wheelKey=-300;c.selected="missing";c.slots=new String[]{"wave",null,"bad"};c.normalize();
        assertEquals(1,c.speed,0);assertEquals(48,c.wheelKey);assertEquals("floss",c.selected);assertEquals(8,c.slots.length);assertEquals("wave",c.slots[0]);assertEquals("orange_justice",c.slots[1]);
        String[] before=c.slots;c.normalize();assertNotSame(before,c.slots);EmoteConfig d=new EmoteConfig();c.slots[7]="dab";assertEquals("robot",d.slots[7]);
    }
    @Test public void oldProfilesGainDefaultsWithoutChangingExistingFeatures(){
        ClientConfig c=new Gson().fromJson("{\"emotes\":null,\"animations\":false,\"cosmetics\":{\"self\":{\"hat\":\"SAKURA\",\"trail\":true}}}",ClientConfig.class);c.normalize();
        assertNotNull(c.emotes);assertFalse(c.animations);assertEquals("SAKURA",c.cosmetics.self.hat);assertTrue(c.cosmetics.self.trail);assertEquals("floss",c.emotes.selected);
    }
    @Test public void realUnifiedProfileRoundTripKeepsEmotesTrailsAndHudTogether()throws Exception{
        ConfigStore store=new ConfigStore(temp.newFolder());store.load();store.config().emotes.slots[0]="take_the_l";store.config().emotes.speed=.75f;store.config().emotes.wheelKey=47;store.config().emotes.hideHeldItem=false;
        store.config().cosmetics.self.trailDesign.applyPreset("Nebula");store.config().hud.applyPreset("PvP");store.saveProfile("dance");String json=store.profileJson("dance");
        assertFalse(json.contains("phase"));assertFalse(json.contains("pendingTicks"));store.resetDefaults();store.importProfileJson("copy",json);assertTrue(store.loadProfile("copy"));
        assertEquals("take_the_l",store.config().emotes.slots[0]);assertEquals(.75f,store.config().emotes.speed,0);assertEquals(47,store.config().emotes.wheelKey);assertFalse(store.config().emotes.hideHeldItem);
        assertEquals("Nebula",store.config().cosmetics.self.trailDesign.preset);assertTrue(store.config().hud.widgets.get("crosshair").enabled);
    }

    @Test public void articulatedClassicsHaveFiniteMovingElbowsAndKneesWithoutLoopJumps(){
        for(String id:Arrays.copyOf(EmoteLibrary.IDS,3)){
            EmotePose a=new EmotePose(),b=new EmotePose();float[] min={9,9,9,9},max={-9,-9,-9,-9};
            for(int t=0;t<400;t++){EmoteLibrary.sample(id,t*.01,a);for(int j=0;j<4;j++){assertTrue(Math.abs(a.bends[j])<1.6);min[j]=Math.min(min[j],a.bends[j]);max[j]=Math.max(max[j],a.bends[j]);}}
            assertTrue(max[1]-min[1]>.05);assertTrue(max[2]-min[2]>.05);assertTrue(max[3]-min[3]>.05);
            EmoteLibrary.sample(id,3.99999,a);EmoteLibrary.sample(id,.00001,b);assertArrayEquals(a.angles,b.angles,.001f);assertArrayEquals(a.bends,b.bends,.001f);
        }
    }
    @Test public void jointBlendsAndStoppingPreserveTheCurrentPose(){
        EmotePlayback p=new EmotePlayback();EmotePose a=new EmotePose(),b=new EmotePose();p.play("orange_justice",1,true);p.update(.25);p.sample(0,a);
        p.play("take_the_l",1,true);p.sample(0,b);assertArrayEquals(a.bends,b.bends,0);p.update(.25);p.sample(0,a);p.stop();p.sample(0,b);assertArrayEquals(a.bends,b.bends,0);
        p.update(.1);p.sample(0,b);for(float bend:b.bends)assertTrue(Math.abs(bend)<1.6);p.update(.1);p.sample(0,b);assertArrayEquals(new float[4],b.bends,0);
    }
    @Test public void copiedJointsAreIndependentAndParticipateInPoseValidation(){
        EmotePose a=new EmotePose(),b=new EmotePose();a.bends[0]=1;b.copy(a);a.clear();assertFalse(a.visible());assertTrue(b.visible());assertEquals(1,b.bends[0],0);
        b.bends[3]=Float.NaN;assertFalse(b.finite());b.clear();assertTrue(b.finite());
    }
    @Test public void articulationPreferenceSurvivesRealProfileRoundTrip()throws Exception{
        ConfigStore store=new ConfigStore(temp.newFolder());store.load();assertTrue(store.config().emotes.articulated);store.config().emotes.articulated=false;store.saveProfile("rig");store.resetDefaults();assertTrue(store.loadProfile("rig"));assertFalse(store.config().emotes.articulated);
    }
}
