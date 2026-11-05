package dev.alien.client.hud.music;

import java.awt.image.BufferedImage;

/** Immutable current snapshot. Playback time follows the provider, not the rendering frame count. */
public final class NowPlayingState {
    public final String status,title,artist,album,source,artwork;
    public final long capturedAt,positionMs,durationMs;
    public final double rate;
    public final BufferedImage cover;
    public NowPlayingState(String status,String title,String artist,String album,String source,long capturedAt,long positionMs,long durationMs,double rate,String artwork,BufferedImage cover){
        this.status=status;this.title=clean(title);this.artist=clean(artist);this.album=clean(album);this.source=clean(source);this.capturedAt=capturedAt;
        this.durationMs=durationMs>0&&durationMs<=604800000?durationMs:-1;
        this.positionMs=positionMs>=0?Math.min(positionMs,this.durationMs>0?this.durationMs:604800000):-1;
        this.rate=Double.isFinite(rate)&&rate>0&&rate<=4?rate:1;this.artwork=artwork==null?"":artwork;this.cover=cover;
    }
    public static NowPlayingState empty(String reason){return new NowPlayingState(reason,"","","","",System.currentTimeMillis(),-1,-1,1,"",null);}
    public boolean active(long now){return ("playing".equals(status)||"paused".equals(status))&&fresh(now);}
    public boolean fresh(long now){long age=now-capturedAt;return age>=-2000&&age<=6000;}
    public long position(long now){if(positionMs<0||!active(now))return -1;long elapsed="playing".equals(status)?Math.max(0,now-capturedAt):0;return Math.min(durationMs>0?durationMs:604800000,positionMs+(long)(elapsed*rate));}
    public float fraction(long now){long position=position(now);return durationMs>0&&position>=0?Math.max(0,Math.min(1,position/(float)durationMs)):-1;}
    public static String clean(String text){if(text==null)return "";StringBuilder out=new StringBuilder();for(int i=0;i<text.length()&&out.length()<512;i++){char c=text.charAt(i);if(!Character.isISOControl(c)&&c!='\u00a7')out.append(c);}return out.toString().trim();}
    public static String clock(long ms){if(ms<0)return "";long seconds=ms/1000;return seconds>=3600?String.format(java.util.Locale.ROOT,"%d:%02d:%02d",seconds/3600,seconds/60%60,seconds%60):String.format(java.util.Locale.ROOT,"%d:%02d",seconds/60,seconds%60);}
}
