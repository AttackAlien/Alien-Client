package dev.alien.client.scene;
/** Optional local visuals. Saved with Unified profiles; no packets or world mutations. */
public final class SceneConfig {
    public static final String[] MODES={"Natural","Cyberspace","Skeleton","Glass","Chroma"};
    public static final String[] SKIES={"Off","Cosmic","BlackHole","Aurora","SnowClouds","Fire","Sunset","Ocean","Storm"};
    public boolean enabled=false,shockwave=false,cute=false;
    public String mode="Natural",sky="Off";
    public int color=0xB48CFF;
    public float strength=.85f,radius=5,lifetime=1.8f,chromaRadius=4,chromaMemory=10,distance=32;
    public void normalize(){
        mode=choice(mode,MODES);sky=choice(sky,SKIES);color&=0xffffff;
        strength=bound(strength,.1f,1,.85f);radius=bound(radius,1,12,5);lifetime=bound(lifetime,.4f,4,1.8f);
        chromaRadius=bound(chromaRadius,1,12,4);chromaMemory=bound(chromaMemory,1,30,10);distance=bound(distance,4,64,32);
    }
    private static String choice(String s,String[] options){for(String o:options)if(o.equals(s))return o;return options[0];}
    private static float bound(float v,float min,float max,float fallback){return Float.isFinite(v)?Math.max(min,Math.min(max,v)):fallback;}
    public int modeIndex(){for(int i=0;i<MODES.length;i++)if(MODES[i].equals(mode))return i;return 0;}
    public int skyIndex(){for(int i=0;i<SKIES.length;i++)if(SKIES[i].equals(sky))return i;return 0;}
}
