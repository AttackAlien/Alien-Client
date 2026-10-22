package dev.alien.client.gui.theme;

import java.awt.Color;
import java.util.Locale;

/** HSV editing preserves the alpha channel. Hex is shown in familiar RRGGBBAA order. */
public final class ColorValue {
    public float hue, saturation, brightness, alpha;
    public ColorValue(int argb) { set(argb); }
    public void set(int argb) {
        float[] hsv=Color.RGBtoHSB((argb>>16)&255,(argb>>8)&255,argb&255,null);
        hue=hsv[0];saturation=hsv[1];brightness=hsv[2];alpha=((argb>>>24)&255)/255f;
    }
    public int argb() { return (Math.round(clamp(alpha)*255)<<24)|(Color.HSBtoRGB(clamp(hue),clamp(saturation),clamp(brightness))&0xffffff); }
    public static int parseHex(String text) {
        String cleaned=text==null?"":text.trim();
        if(cleaned.startsWith("#"))cleaned=cleaned.substring(1);
        if(!cleaned.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?"))throw new IllegalArgumentException("Use RRGGBB or RRGGBBAA");
        long value=Long.parseLong(cleaned,16);
        return cleaned.length()==6?0xff000000|(int)value:((int)value>>>8)|((int)value<<24);
    }
    public static String hex(int argb) { return String.format(Locale.ROOT,"%06X%02X",argb&0xffffff,(argb>>>24)&255); }
    public static int rgba(int red,int green,int blue,int alpha) {
        return clampByte(alpha)<<24|clampByte(red)<<16|clampByte(green)<<8|clampByte(blue);
    }
    private static int clampByte(int value){return Math.max(0,Math.min(255,value));}
    public static float clamp(float value){return !Float.isFinite(value)?0:Math.max(0,Math.min(1,value));}
}
