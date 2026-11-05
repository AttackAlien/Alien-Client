package dev.alien.client.hud.music;

import com.google.gson.Gson;
import dev.alien.client.config.*;
import dev.alien.client.hud.HudConfig;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import static org.junit.Assert.*;

public class NowPlayingTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private static final long NOW=1800000000000L;
    private NowPlayingState state(String status,long position,long duration,double rate){return new NowPlayingState(status,"Title","Artist","Album","Chrome",NOW,position,duration,rate,"",null);}
    private Path directory()throws Exception{return temp.newFolder().toPath();}
    private void write(Path dir,String extra)throws Exception{Files.write(dir.resolve("now-playing.json"),("{\"schema\":1,\"status\":\"playing\",\"capturedAt\":"+NOW+","+extra+"}").getBytes(StandardCharsets.UTF_8));}
    @Test public void playingTimeUsesElapsedProviderTimeAndPlaybackRate(){NowPlayingState s=state("playing",10000,200000,1.5);assertEquals(13000,s.position(NOW+2000));assertEquals(.065f,s.fraction(NOW+2000),.00001);}
    @Test public void pausedTimeNeverAnimates(){NowPlayingState s=state("paused",51000,200000,2);assertEquals(51000,s.position(NOW+5000));}
    @Test public void seekAndTrackChangeTakeNewProviderPosition(){assertEquals(91000,state("playing",90000,100000,1).position(NOW+1000));assertEquals(1000,state("playing",0,250000,1).position(NOW+1000));}
    @Test public void unknownTimelineDoesNotInventProgress(){assertEquals(-1,state("playing",-1,-1,1).position(NOW));assertEquals(-1,state("playing",12000,-1,1).fraction(NOW),0);}
    @Test public void staleAndFarFutureSnapshotsCannotRemainVisible(){assertFalse(state("playing",0,100,1).active(NOW+6001));assertFalse(state("paused",0,100,1).active(NOW-2001));assertEquals(-1,state("playing",0,100,1).position(NOW+7000));}
    @Test public void boundsAndRatesAreRepaired(){assertEquals(100,state("playing",99,100,2).position(NOW+100));assertEquals(1,state("playing",0,100,Double.NaN).rate,0);assertEquals(1,state("playing",0,100,-1).rate,0);assertEquals(-1,state("playing",0,Long.MAX_VALUE,1).durationMs);}
    @Test public void formattingCodesControlsAndUnboundedTitlesAreRemoved(){assertEquals("ab c",NowPlayingState.clean(" a\nb\u00a7 c "));assertEquals(512,NowPlayingState.clean(new String(new char[900]).replace('\0','a')).length());assertEquals("1:01:01",NowPlayingState.clock(3661000));}
    @Test public void importedProfilesKeepOldWidgetsAndOnlyMusicPreferences()throws Exception{
        ConfigStore store=new ConfigStore(temp.newFolder());store.load();store.config().hud.widgets.get("fps").x=.73f;store.config().hud.music.style="Transparent";store.config().hud.music.source="Browser";store.config().hud.widgets.get("music").enabled=true;
        store.saveProfile("music");store.resetDefaults();assertTrue(store.loadProfile("music"));assertEquals(.73f,store.config().hud.widgets.get("fps").x,0);assertEquals("Transparent",store.config().hud.music.style);assertEquals("Browser",store.config().hud.music.source);
        String json=new Gson().toJson(store.config());assertFalse(json.contains("capturedAt"));assertFalse(json.contains("positionMs"));assertFalse(json.contains("now-playing.json"));
    }
    @Test public void legacyAndInvalidMusicConfigRemainOptIn(){HudConfig c=new Gson().fromJson("{\"music\":null}",HudConfig.class);c.ensureDefaults();assertFalse(c.widgets.get("music").enabled);c.music.width=Float.NaN;c.music.source="YouTubeGuess";c.music.style=null;c.ensureDefaults();assertEquals(232,c.music.width,0);assertEquals("Auto",c.music.source);assertEquals("Compact",c.music.style);}
    @Test public void readerPreservesUnicodeAndReportsStaleData()throws Exception{Path d=directory();write(d,"\"title\":\"Canção 漢字\",\"source\":\"Chrome\",\"positionMs\":null");NowPlayingReader r=new NowPlayingReader();assertEquals("Canção 漢字",r.read(d,NOW).title);assertEquals(-1,r.read(d,NOW).position(NOW));assertEquals("disconnected",r.read(d,NOW+8000).status);}
    @Test public void invalidSchemaMalformedAndDeepJsonAreRejected()throws Exception{Path d=directory();for(String json:new String[]{"{", "{\"schema\":99}", "{\"schema\":1,\"status\":\"pretend\"}","[[[[[[[[[[]]]]]]]]]]"}){Files.write(d.resolve("now-playing.json"),json.getBytes(StandardCharsets.UTF_8));try{new NowPlayingReader().read(d,NOW);fail(json);}catch(IOException expected){}}}
    @Test public void oversizedSnapshotIsRejectedBeforeParsing()throws Exception{Path d=directory();Files.write(d.resolve("now-playing.json"),new byte[16385]);try{new NowPlayingReader().read(d,NOW);fail();}catch(IOException expected){}}
    @Test public void coverIsOnlyUsedWithMatchingChecksumAndCachedOnce()throws Exception{Path d=directory();BufferedImage im=new BufferedImage(64,64,BufferedImage.TYPE_INT_RGB);ImageIO.write(im,"png",d.resolve("cover.png").toFile());byte[] bytes=Files.readAllBytes(d.resolve("cover.png"));write(d,"\"artwork\":\""+NowPlayingReader.hash(bytes)+"\"");NowPlayingReader r=new NowPlayingReader();BufferedImage first=r.read(d,NOW).cover;assertNotNull(first);assertSame(first,r.read(d,NOW).cover);write(d,"\"artwork\":\""+new String(new char[64]).replace('\0','a')+"\"");assertNull(r.read(d,NOW).cover);}
    @Test public void oversizedImageIsNotDecodedOrAttached()throws Exception{Path d=directory();ImageIO.write(new BufferedImage(513,8,BufferedImage.TYPE_INT_RGB),"png",d.resolve("cover.png").toFile());write(d,"\"artwork\":\""+NowPlayingReader.hash(Files.readAllBytes(d.resolve("cover.png")))+"\"");assertNull(new NowPlayingReader().read(d,NOW).cover);}
    @Test public void symlinkSnapshotIsNotFollowed()throws Exception{Path d=directory(),other=temp.newFile().toPath();Files.write(other,"{}".getBytes(StandardCharsets.UTF_8));Files.createSymbolicLink(d.resolve("now-playing.json"),other);assertEquals("connecting",new NowPlayingReader().read(d,NOW).status);}
}
