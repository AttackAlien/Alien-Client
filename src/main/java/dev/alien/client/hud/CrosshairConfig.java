package dev.alien.client.hud;
public final class CrosshairConfig {
    public String shape="Plus";
    public float gap=3,length=6,thickness=1,spread=3;
    public boolean dot=true,outline=true,centered=true,dynamic=true;
    public void normalize() {
        if(!"Plus".equals(shape)&&!"T".equals(shape)&&!"Circle".equals(shape)&&!"Dot".equals(shape))shape="Plus";
        gap=HudMath.clamp(gap,0,12,3);length=HudMath.clamp(length,1,16,6);
        thickness=HudMath.clamp(thickness,.5f,4,1);spread=HudMath.clamp(spread,0,12,3);
    }
}
