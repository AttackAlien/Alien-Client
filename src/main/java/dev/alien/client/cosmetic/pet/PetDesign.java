package dev.alien.client.cosmetic.pet;

/** Per-look preferences only; no entity IDs, animation clocks or world positions are persisted. */
public final class PetDesign {
    public static final String[] PRESETS={"Alien","Mint","Solar","Original"},MODES={"Follow","Shoulder","Orbit"};
    public boolean enabled,animate=true,reactEmotes=true,sleep=true,right=true;
    public String preset="Original",mode="Follow";
    public int color=0x467876,accent=0x303030;
    public float size=1,distance=1.05f,height=1.15f,speed=1;
    public void normalize(){
        if(!has(MODES,mode))mode="Follow";if(!has(PRESETS,preset)&&!"Custom".equals(preset))preset="Custom";
        color&=0xffffff;accent&=0xffffff;size=bound(size,.5f,1.5f,1);distance=bound(distance,.65f,2,1.05f);height=bound(height,.65f,1.8f,1.15f);speed=bound(speed,.4f,1.8f,1);
    }
    public boolean applyPreset(String name){if(!has(PRESETS,name))return false;preset=name;color="Original".equals(name)?0x467876:"Mint".equals(name)?0x68DABC:"Solar".equals(name)?0xEFBB68:0xA98AEF;accent="Original".equals(name)?0x303030:"Mint".equals(name)?0xDFFFB7:"Solar".equals(name)?0xFF826C:0x5CE7D4;enabled=true;return true;}
    public PetDesign copy(){PetDesign p=new PetDesign();p.enabled=enabled;p.animate=animate;p.reactEmotes=reactEmotes;p.sleep=sleep;p.right=right;p.preset=preset;p.mode=mode;p.color=color;p.accent=accent;p.size=size;p.distance=distance;p.height=height;p.speed=speed;return p;}
    private static boolean has(String[] values,String value){for(String v:values)if(v.equals(value))return true;return false;}
    private static float bound(float v,float min,float max,float fallback){return Float.isFinite(v)?Math.max(min,Math.min(max,v)):fallback;}
}
