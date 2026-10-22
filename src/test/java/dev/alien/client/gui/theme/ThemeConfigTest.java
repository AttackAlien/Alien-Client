package dev.alien.client.gui.theme;

import org.junit.Test;
import static org.junit.Assert.*;

public class ThemeConfigTest {
    @Test public void namedPalettesRemainReadableAndIndependentOfGeometryPreferences() {
        ThemeConfig theme=new ThemeConfig();theme.radius=11;theme.menuScale=.8f;theme.reducedMotion=true;
        for(String name:ThemeConfig.PRESETS){theme.applyPreset(name);assertEquals(name,theme.preset);assertTrue(contrast(theme.text,theme.surface)>4.5);assertTrue(contrast(theme.muted,theme.surface)>3);}
        assertEquals(11,theme.radius,0);assertEquals(.8f,theme.menuScale,0);assertTrue(theme.reducedMotion);
        assertEquals(0,theme.effectiveAnimationSpeed(true),0);
    }
    @Test public void importedInvalidTokensAreRepairedWithoutEnablingMotion() {
        ThemeConfig theme=new ThemeConfig();theme.preset=null;theme.font="../../font";theme.opacity=Float.NaN;
        theme.radius=999;theme.shadow=-10;theme.animationSpeed=Float.POSITIVE_INFINITY;theme.menuScale=0;theme.reducedMotion=true;
        theme.normalize();assertEquals("Custom RGB",theme.preset);assertEquals("Modern",theme.font);assertEquals(.96f,theme.opacity,0);
        assertEquals(14,theme.radius,0);assertEquals(0,theme.shadow,0);assertEquals(1,theme.animationSpeed,0);assertEquals(.75f,theme.menuScale,0);
        ThemeConfig copy=theme.copy();copy.primary=0;assertNotEquals(copy.primary,theme.primary);assertTrue(copy.reducedMotion);
    }
    @Test(expected=IllegalArgumentException.class) public void unknownPresetIsRejected() { new ThemeConfig().applyPreset("fake"); }
    private static double contrast(int a,int b){double x=luminance(a),y=luminance(b);return(Math.max(x,y)+.05)/(Math.min(x,y)+.05);}
    private static double luminance(int color){return .2126*channel(color>>16&255)+.7152*channel(color>>8&255)+.0722*channel(color&255);}
    private static double channel(int channel){double v=channel/255d;return v<=.03928?v/12.92:Math.pow((v+.055)/1.055,2.4);}
}
