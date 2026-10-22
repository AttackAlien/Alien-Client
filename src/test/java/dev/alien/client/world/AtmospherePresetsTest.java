package dev.alien.client.world;

import org.junit.Test;
import static org.junit.Assert.*;

public class AtmospherePresetsTest {
    @Test public void freshInstallHasNoParticleEmissionUntilOptIn(){
        EffectsConfig c=new EffectsConfig();
        assertFalse(c.enabled);assertFalse(c.movement);assertFalse(c.landing);assertFalse(c.trails);assertFalse(c.ambient);
    }
    @Test public void crystalPresetHasCoherentBrightPaletteAndBoundedDiamondEffects(){
        WorldConfig w=new WorldConfig();EffectsConfig e=new EffectsConfig();w.skyboxFile="old.png";
        AtmospherePresets.apply(w,e,"Crystal");
        assertTrue(w.enabled);assertEquals("Crystal",w.theme);assertEquals(13,w.visualHour);assertEquals(0,w.stars);
        assertEquals("",w.skyboxFile);assertTrue(w.timeOverride);assertTrue(w.fogEnabled);assertEquals("Clear",w.weather);
        assertTrue(e.enabled);assertTrue(e.ambient);assertEquals("Diamond",e.shape);assertEquals(72,e.effectiveBudget());
        assertTrue(((w.skyTop>>8)&255)>((w.skyTop>>16)&255));assertFalse(e.cameraSway);assertFalse(e.colorFilter);
    }
    @Test public void sakuraAndNebulaAreDifferentWholePalettes(){
        WorldConfig w=new WorldConfig();EffectsConfig e=new EffectsConfig();
        AtmospherePresets.apply(w,e,"Sakura");int sakura=w.skyBottom;
        assertEquals("Petal",e.shape);assertEquals(18,w.visualHour);assertEquals("Sakura",w.theme);
        AtmospherePresets.apply(w,e,"Nebula");
        assertEquals("Spark",e.shape);assertEquals(0,w.visualHour);assertNotEquals(sakura,w.skyBottom);
        assertTrue(e.effectiveBudget()<=96);
    }
    @Test public void unknownPresetCannotPartiallyChangeAnActiveConfiguration(){
        WorldConfig w=new WorldConfig();EffectsConfig e=new EffectsConfig();
        try {AtmospherePresets.apply(w,e,"run-script");fail();}catch(IllegalArgumentException expected){}
        assertFalse(w.enabled);assertFalse(e.enabled);assertEquals("Nebula",w.theme);
    }
    @Test public void malformedShapesAndOldProfilesNormalizeWithoutUnboundedGeometry(){
        EffectsConfig e=new EffectsConfig();e.shape="spherewith999999sides";e.secondaryColor=0xff123456;e.normalize();
        assertEquals("Diamond",e.shape);assertEquals(0x123456,e.secondaryColor);
        EffectsConfig old=new com.google.gson.Gson().fromJson("{\"enabled\":true,\"movement\":true}",EffectsConfig.class);
        old.normalize();assertEquals("Diamond",old.shape);assertTrue(old.movement);assertFalse(old.ambient);
    }
    @Test public void shapeTriangleStreamsAreCachedFiniteAndVisuallyDifferent(){
        ParticleShapes.Shape diamond=ParticleShapes.get("Diamond"),petal=ParticleShapes.get("Petal"),spark=ParticleShapes.get("Spark");
        assertSame(diamond,ParticleShapes.get("invalid"));assertSame(petal,ParticleShapes.get("Petal"));
        assertNotEquals(diamond.vertexCount(),petal.vertexCount());assertNotEquals(petal.vertexCount(),spark.vertexCount());
        for(ParticleShapes.Shape shape:new ParticleShapes.Shape[]{diamond,petal,spark}){
            assertEquals(0,shape.vertexCount()%3);assertTrue(shape.vertexCount()<=48);
            for(int i=0;i<shape.vertexCount();i++){
                assertTrue(Float.isFinite(shape.u(i)));assertTrue(Float.isFinite(shape.v(i)));
                assertTrue(Math.abs(shape.u(i))<=1.2);assertTrue(Math.abs(shape.v(i))<=1.2);
            }
        }
    }
}
