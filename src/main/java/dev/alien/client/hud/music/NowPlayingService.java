package dev.alien.client.hud.music;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/** Opt-in native helper. Polling, process lifecycle and decoding never run on the render thread. */
public final class NowPlayingService {
    private static final NowPlayingService INSTANCE=new NowPlayingService();
    private volatile NowPlayingState state=NowPlayingState.empty("idle");
    private volatile boolean wanted,closed;private volatile String source="Auto";private volatile boolean art=true;
    private volatile File root;private Thread worker;
    private volatile Process runningChild;private volatile Path activeDirectory;
    private NowPlayingService(){Runtime.getRuntime().addShutdownHook(new Thread(()->{closed=true;wanted=false;Process p=runningChild;if(p!=null){p.destroy();try{p.waitFor(700,TimeUnit.MILLISECONDS);}catch(InterruptedException ignored){}}clean(activeDirectory);},"Alien music shutdown"));}
    public static NowPlayingService get(){return INSTANCE;}
    public NowPlayingState snapshot(){NowPlayingState s=state;return s.active(System.currentTimeMillis())||!("playing".equals(s.status)||"paused".equals(s.status))?s:NowPlayingState.empty("disconnected");}
    public synchronized void request(boolean active,NowPlayingConfig config,File directory){
        wanted=active;source=config.source;art=config.artwork;root=directory;
        if(active&&worker==null){worker=new Thread(this::run,"Alien current media");worker.setDaemon(true);worker.start();}
    }
    private void run(){
        Process child=null;Path directory=null;String key="";long retryAt=0;NowPlayingReader reader=new NowPlayingReader();
        while(!closed){
            try{
                String executable=System.getenv("ALIEN_MEDIA_BRIDGE_EXE"),next=source+"/"+art;
                if(!wanted&&(child!=null||directory!=null||!"idle".equals(state.status))||child!=null&&(!next.equals(key)||!child.isAlive())){
                    if(child!=null){child.destroy();if(!child.waitFor(700,TimeUnit.MILLISECONDS))child.destroyForcibly();child=null;}
                    clean(directory);directory=null;activeDirectory=null;runningChild=null;reader=new NowPlayingReader();key="";state=NowPlayingState.empty(wanted?"connecting":"idle");
                }
                if(wanted&&child==null&&System.nanoTime()>=retryAt){
                    if(executable==null||!new File(executable).isFile()){state=NowPlayingState.empty("launcher_required");retryAt=System.nanoTime()+2000000000L;}
                    else{
                        Path parent=new File(root,"media-runtime").toPath();Files.createDirectories(parent);directory=Files.createTempDirectory(parent,"session-");
                        String pid=ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
                        child=new ProcessBuilder(executable,"--media-bridge","--output",directory.toAbsolutePath().toString(),"--parent",pid,"--source",source,"--art",Boolean.toString(art)).redirectErrorStream(true).start();
                        runningChild=child;activeDirectory=directory;key=next;retryAt=System.nanoTime()+2000000000L;
                    }
                }
                if(wanted&&child!=null&&directory!=null)state=reader.read(directory,System.currentTimeMillis());
            }catch(Exception failure){if(child==null){clean(directory);directory=null;activeDirectory=null;}state=NowPlayingState.empty("error");retryAt=System.nanoTime()+2000000000L;}
            try{Thread.sleep(500);}catch(InterruptedException ignored){closed=true;}
        }
        if(child!=null)child.destroy();clean(directory);
    }
    private static void clean(Path path){if(path==null)return;try(DirectoryStream<Path> files=Files.newDirectoryStream(path)){for(Path p:files)if(!Files.isDirectory(p))Files.deleteIfExists(p);}catch(IOException ignored){}try{Files.deleteIfExists(path);}catch(IOException ignored){}}
}
