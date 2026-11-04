package dev.alien.client.studio;
/** Local presentation preferences. Kept in the same versioned Unified profile. */
public final class StudioConfig {
    public boolean menuScene=true,orbit=true,handMovement=false,nameTags=false,zoom=false;
    public String scene="Grove",time="Day",weather="Clear";
    public float cameraYaw=25,cameraPitch=16,cameraDistance=25,zoomFactor=3,handStrength=.6f;
    public static final String[] SCENES={"Grove","Sakura","Alpine"},TIMES={"Day","Sunset","Night"},WEATHER={"Clear","Rain","Snow","Mixed"};
    public void normalize(){scene=choice(scene,SCENES);time=choice(time,TIMES);weather=choice(weather,WEATHER);cameraYaw=finite(cameraYaw,-360,360,25);cameraPitch=finite(cameraPitch,-5,65,16);cameraDistance=finite(cameraDistance,14,40,25);zoomFactor=finite(zoomFactor,1.5f,8,3);handStrength=finite(handStrength,0,1,.6f);}
    private static String choice(String v,String[] options){for(String s:options)if(s.equals(v))return s;return options[0];}
    public static float finite(float v,float lo,float hi,float fallback){return Float.isFinite(v)?Math.max(lo,Math.min(hi,v)):fallback;}
}
