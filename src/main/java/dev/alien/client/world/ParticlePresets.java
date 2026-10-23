package dev.alien.client.world;
/** Particle-only choices: no combat, sky, world time or weather writes. */
public final class ParticlePresets {
    private ParticlePresets() { }
    public static void apply(EffectsConfig c,String name){
        if(!"PvP".equals(name)&&!"Cinematic".equals(name)&&!"Sakura".equals(name))throw new IllegalArgumentException("Unknown particle preset");
        c.enabled=true;c.movement=true;c.landing=true;c.colorFilter=false;c.cameraSway=false;
        boolean pvp="PvP".equals(name),sakura="Sakura".equals(name);
        c.ambient=!pvp;c.trails=!pvp;c.quality=pvp?"Low":"Balanced";c.budget=pvp?24:sakura?72:96;
        c.distance=pvp?12:24;c.size=pvp?1.8f:sakura?3.2f:2.8f;
        c.shape=pvp?"Spark":sakura?"Petal":"Orb";
        c.color=pvp?0x62DCEC:sakura?0xEFABCB:0xB199F7;c.secondaryColor=pvp?0xDCF9FF:sakura?0xFFE2EB:0x69DCE8;
        c.opacity=pvp?.65f:.84f;c.glow=pvp?0:sakura?.22f:.55f;c.flow=pvp?.2f:sakura?1.1f:.65f;c.lifetime=pvp?.65f:sakura?1.25f:1.1f;
        c.normalize();
    }
}
