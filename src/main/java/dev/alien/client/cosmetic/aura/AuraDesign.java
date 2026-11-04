package dev.alien.client.cosmetic.aura;

/** Optional per-look appearance. Old profiles retain the original particle aura. */
public final class AuraDesign {
    public static final String[] PRESETS={"Arcane","Neon","Celestial"},STYLES={"Runes","Orbit","Halo"};
    public boolean enabled,motion=true,emotes=true,night=true;
    public String preset="Arcane",style="Runes";
    public int color=0xAE8BFF,secondaryColor=0x5CE4D5;
    public float radius=.75f,opacity=.85f,intensity=1,speed=1,response=.22f,idle=.25f;
    public float motionGain=.8f,emoteGain=.9f,nightGain=.5f;
    public void normalize(){
        if(!has(STYLES,style))style="Runes";if(!has(PRESETS,preset)&&!"Custom".equals(preset))preset="Custom";
        color&=0xffffff;secondaryColor&=0xffffff;
        radius=finite(radius,.35f,1.4f,.75f);opacity=finite(opacity,.1f,1,.85f);intensity=finite(intensity,.2f,1.5f,1);
        speed=finite(speed,.2f,2,1);response=finite(response,.1f,1,.22f);idle=finite(idle,0,.7f,.25f);
        motionGain=finite(motionGain,0,1.5f,.8f);emoteGain=finite(emoteGain,0,1.5f,.9f);nightGain=finite(nightGain,0,1.5f,.5f);
    }
    public boolean applyPreset(String name){
        if(!has(PRESETS,name))return false;
        preset=name;style="Neon".equals(name)?"Orbit":"Celestial".equals(name)?"Halo":"Runes";
        color="Neon".equals(name)?0x45E9F3:"Celestial".equals(name)?0xA7C8FF:0xAE8BFF;
        secondaryColor="Neon".equals(name)?0xFF65CF:"Celestial".equals(name)?0xFFF1B7:0x5CE4D5;
        radius="Neon".equals(name)?.65f:"Celestial".equals(name)?.85f:.75f;
        opacity=.85f;intensity=1;speed="Neon".equals(name)?1.25f:.8f;response=.22f;idle=.25f;
        motionGain=.8f;emoteGain=.9f;nightGain=.5f;motion=emotes=night=true;enabled=true;return true;
    }
    public AuraDesign copy(){AuraDesign d=new AuraDesign();d.enabled=enabled;d.motion=motion;d.emotes=emotes;d.night=night;d.preset=preset;d.style=style;d.color=color;d.secondaryColor=secondaryColor;d.radius=radius;d.opacity=opacity;d.intensity=intensity;d.speed=speed;d.response=response;d.idle=idle;d.motionGain=motionGain;d.emoteGain=emoteGain;d.nightGain=nightGain;return d;}
    private static boolean has(String[] values,String v){for(String s:values)if(s.equals(v))return true;return false;}
    private static float finite(float v,float min,float max,float fallback){return Float.isFinite(v)?Math.max(min,Math.min(max,v)):fallback;}
}
