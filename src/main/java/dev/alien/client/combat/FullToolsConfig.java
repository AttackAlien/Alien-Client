package dev.alien.client.combat;
/** Unified optional gameplay modules. All are off on old/new profiles until chosen. */
public final class FullToolsConfig {
    public boolean aimbot,autoFood,autoPotion,antiAim,bridge,autoSprint;
    public int foodThreshold=14;
    public float potionHealth=12,spinSpeed=120;
    public boolean splashPotions=true;
    public void normalize(){foodThreshold=Math.max(1,Math.min(19,foodThreshold));
        potionHealth=bounded(potionHealth,1,20,12);spinSpeed=bounded(spinSpeed,15,720,120);
        if(aimbot)antiAim=false;
    }
    public void off(){aimbot=autoFood=autoPotion=antiAim=bridge=autoSprint=false;}
    public void full(){off();aimbot=autoFood=autoPotion=autoSprint=true;}
    private static float bounded(float v,float min,float max,float fallback){return Float.isFinite(v)?Math.max(min,Math.min(max,v)):fallback;}
}
