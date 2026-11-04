package dev.alien.client.world;
import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;
public class ParticlePolishTest {
    @Test public void fadeIsSmoothFiniteAndZeroAtBirthAndDeath(){
        assertEquals(0,ParticleAppearance.fade(60,60,0),0);assertEquals(1,ParticleAppearance.fade(60,30,0),0);
        assertEquals(0,ParticleAppearance.fade(60,0,0),0);assertEquals(0,ParticleAppearance.fade(0,3,Float.NaN),0);
        float old=0;for(int i=0;i<=6;i++){float f=ParticleAppearance.fade(60,60-i,0);assertTrue(f>=old&&f<=1);old=f;}
        assertEquals(2.5,ParticleAppearance.interpolate(0,10,.25f),0);
    }
    @Test public void cachedMeshesHaveTransparentEdgesAndOpaqueCores(){
        for(String name:new String[]{"Diamond","Petal","Spark","Orb"}){
            ParticleShapes.Shape m=ParticleShapes.get(name);assertSame(m,ParticleShapes.get(name));
            boolean transparent=false,opaque=false;
            assertTrue(m.vertexCount()<=288);
            for(int i=0;i<m.vertexCount();i++){assertTrue(Float.isFinite(m.u(i)));assertTrue(Float.isFinite(m.v(i)));
                assertTrue(m.alpha(i)>=0&&m.alpha(i)<=1);transparent|=m.alpha(i)==0;opaque|=m.alpha(i)==1;
            }
            assertTrue(transparent);assertTrue(opaque);
        }
    }
    @Test public void oldProfilesAndMalformedFieldsRemainBounded(){
        Gson gson=new Gson();EffectsConfig old=gson.fromJson("{\"movement\":true}",EffectsConfig.class);old.normalize();
        assertTrue(old.movement);assertEquals(.8f,old.opacity,0);assertEquals(1,old.lifetime,0);
        old.glow=Float.NaN;old.opacity=Float.POSITIVE_INFINITY;old.flow=-1;old.lifetime=99;old.normalize();
        assertEquals(.4f,old.glow,0);assertEquals(.8f,old.opacity,0);assertEquals(0,old.flow,0);assertEquals(2,old.lifetime,0);
    }
    @Test public void presetsGiveARealLowCostOptionAndRoundTripPolishedControls(){
        EffectsConfig c=new EffectsConfig();ParticlePresets.apply(c,"Cinematic");assertEquals("Orb",c.shape);assertTrue(c.glow>0);assertTrue(c.ambient);
        EffectsConfig restored=new Gson().fromJson(new Gson().toJson(c),EffectsConfig.class);restored.normalize();assertEquals(c.glow,restored.glow,0);assertEquals(c.flow,restored.flow,0);
        ParticlePresets.apply(c,"PvP");assertFalse(c.ambient);assertEquals(0,c.glow,0);assertTrue(c.effectiveBudget()<=24);
        ParticlePresets.apply(c,"Sakura");assertEquals("Petal",c.shape);assertTrue(c.ambient);
    }
}
