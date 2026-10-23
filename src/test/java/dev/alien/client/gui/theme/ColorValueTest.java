package dev.alien.client.gui.theme;
import org.junit.Test;
import static org.junit.Assert.*;
public class ColorValueTest {
    @Test public void hexAndHsvPreserveRgbaAcrossEdgeColors(){
        for(int c:new int[]{0,0xffffffff,0x8036abed,0x014402ff,0xff000000,0x66ff0088}){
            assertEquals(c,ColorValue.parseHex(ColorValue.hex(c)));assertEquals(c,new ColorValue(c).argb());
        }
        assertEquals(0xff12abcd,ColorValue.parseHex("#12ABCD"));
    }
    @Test public void channelEditingClampsAndRetainsTransparency(){
        assertEquals(0x0014ffff,ColorValue.rgba(20,999,256,-2));
        ColorValue c=new ColorValue(0x80123456);c.hue=.8f;c.saturation=.7f;c.brightness=.9f;
        assertEquals(128,c.argb()>>>24);c.alpha=Float.NaN;assertEquals(0,c.argb()>>>24);
    }
    @Test(expected=IllegalArgumentException.class)public void invalidHexCannotBecomeAnUnexpectedColor(){ColorValue.parseHex("12345oops");}
}
