package dev.alien.client.cosmetic.neon;

/** Per-look emissive material preferences. Transient GL resources never enter profiles. */
public final class NeonDesign {
    public boolean enabled=true,bloom=true;
    public int color=0x64FFE5;
    public float intensity=.85f,radius=12;
    public String preset="Cyber";
    public void normalize(){intensity=bound(intensity,0,1.5f,.85f);radius=bound(radius,4,24,12);color&=0xffffff;if(!"Cyber".equals(preset)&&!"Crimson".equals(preset)&&!"Violet".equals(preset)&&!"Custom".equals(preset))preset="Custom";}
    public void preset(String name){if("Cyber".equals(name))color=0x64FFE5;else if("Crimson".equals(name))color=0xFF346A;else if("Violet".equals(name))color=0xBB83FF;else return;preset=name;enabled=true;intensity=1;}
    public NeonDesign copy(){NeonDesign d=new NeonDesign();d.enabled=enabled;d.bloom=bloom;d.color=color;d.intensity=intensity;d.radius=radius;d.preset=preset;d.normalize();return d;}
    private static float bound(float v,float lo,float hi,float fallback){return Float.isFinite(v)?Math.max(lo,Math.min(hi,v)):fallback;}
}
