package dev.alien.client.combat;

import org.junit.Test;
import static org.junit.Assert.*;
public class CombatMathTest {
    @Test public void wrapsYawAcross360WithoutLargeSnap() {
        assertEquals(-2F,CombatMath.wrap(358F),.0001F);
        assertEquals(2F,CombatMath.interpolate(179F,-179F,1F)-179F,.0001F);
    }
    @Test public void calculatesVanillaCardinalDirections() {
        float[] east=CombatMath.lookAt(0,0,0,10,0,0);
        float[] north=CombatMath.lookAt(0,0,0,0,0,-10);
        assertEquals(-90F,east[0],.001F);
        assertEquals(-180F,north[0],.001F);
        assertEquals(0F,east[1],.001F);
    }
    @Test public void smoothInterpolationAndCpsLimits() {
        assertEquals(10F,CombatMath.interpolate(0F,100F,.1F),.01F);
        assertTrue(CombatMath.intervalNanos(12F)>=83333333L);
        assertEquals(1000000000L,CombatMath.intervalNanos(-500F));
    }
}
