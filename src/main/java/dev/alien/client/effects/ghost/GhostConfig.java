package dev.alien.client.effects.ghost;

/** Independent optional kill effect; old particle/scene preferences remain separate. */
public final class GhostConfig {
    public boolean enabled,calm;
    public boolean players=true,mobs=true,particles=true,echoes=true;
    public int color=0xA77DFF,secondaryColor=0x74F0DA,maxGhosts=3;
    public float duration=2.6f,height=2.2f,size=.92f,opacity=.72f,glow=.55f,distance=32;
    public String preset="Neon";
    public void normalize(){duration=bound(duration,1.5f,4.5f,2.6f);height=bound(height,.6f,4,2.2f);size=bound(size,.5f,1.5f,.92f);opacity=bound(opacity,.15f,1,.72f);glow=bound(glow,0,1,.55f);distance=bound(distance,8,48,32);maxGhosts=Math.max(1,Math.min(6,maxGhosts));color&=0xffffff;secondaryColor&=0xffffff;if(!"Neon".equals(preset)&&!"Spectral".equals(preset)&&!"Rose".equals(preset)&&!"Custom".equals(preset))preset="Custom";}
    public GhostConfig copy(){GhostConfig d=new GhostConfig();d.enabled=enabled;d.calm=calm;d.players=players;d.mobs=mobs;d.particles=particles;d.echoes=echoes;d.color=color;d.secondaryColor=secondaryColor;d.maxGhosts=maxGhosts;d.duration=duration;d.height=height;d.size=size;d.opacity=opacity;d.glow=glow;d.distance=distance;d.preset=preset;d.normalize();return d;}
    public void preset(String name){if("Neon".equals(name)){color=0xA77DFF;secondaryColor=0x74F0DA;}else if("Spectral".equals(name)){color=0x73C9FF;secondaryColor=0xD5F7FF;}else if("Rose".equals(name)){color=0xEC91D8;secondaryColor=0xC6A2FF;}else return;preset=name;}
    private static float bound(float v,float min,float max,float fallback){return Float.isFinite(v)?Math.max(min,Math.min(max,v)):fallback;}
}
