package dev.alien.client.hud.music;

/** Appearance/source preferences only. Current listening metadata is never saved in profiles. */
public final class NowPlayingConfig {
    public static final String[] STYLES={"Compact","Card","Transparent"},SOURCES={"Auto","Spotify","Browser"};
    public String style="Compact",source="Auto";
    public boolean artist=true,artwork=true,progress=true,time,sourceLabel,hidePaused,hideInactive=true,marquee=true;
    public float width=232;
    public void normalize(){if(!has(STYLES,style))style="Compact";if(!has(SOURCES,source))source="Auto";width=Float.isFinite(width)?Math.max(180,Math.min(360,width)):232;}
    private static boolean has(String[] a,String b){for(String s:a)if(s.equals(b))return true;return false;}
}
