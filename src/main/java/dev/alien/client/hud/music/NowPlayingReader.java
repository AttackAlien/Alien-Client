package dev.alien.client.hud.music;

import com.google.gson.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

/** Runs only on the media worker. Bounded JSON/images and hash-matched atomic cover updates. */
public final class NowPlayingReader {
    private String imageHash="";private BufferedImage image;
    public NowPlayingState read(Path directory,long now)throws IOException{
        Path file=directory.resolve("now-playing.json");if(!Files.isRegularFile(file)||Files.isSymbolicLink(file))return NowPlayingState.empty("connecting");
        String text=new String(bounded(file,16384),StandardCharsets.UTF_8);int depth=0;boolean quoted=false,escaped=false;
        for(char c:text.toCharArray())if(quoted){if(escaped)escaped=false;else if(c=='\\')escaped=true;else if(c=='\"')quoted=false;}
            else if(c=='\"')quoted=true;else if(c=='{'||c=='['){if(++depth>8)throw new IOException("Media JSON depth");}else if(c=='}'||c==']')depth--;
        try{
            JsonObject o=new JsonParser().parse(text).getAsJsonObject();if(o.get("schema").getAsInt()!=1)throw new IOException("Media schema");
            String status=string(o,"status");if(!Arrays.asList("playing","paused","idle","error","unsupported").contains(status))throw new IOException("Media status");
            long captured=o.get("capturedAt").getAsLong();NowPlayingState state=new NowPlayingState(status,string(o,"title"),string(o,"artist"),string(o,"album"),string(o,"source"),captured,
                number(o,"positionMs"),number(o,"durationMs"),o.has("rate")?o.get("rate").getAsDouble():1,string(o,"artwork"),null);
            if(!state.fresh(now))return NowPlayingState.empty("disconnected");
            if(!state.active(now)){image=null;imageHash="";return state;}
            BufferedImage cover=null;String sha=state.artwork.toLowerCase(Locale.ROOT);
            if(sha.matches("[0-9a-f]{64}")){
                if(!sha.equals(imageHash)||image==null){
                    Path art=directory.resolve("cover.png");
                    if(Files.isRegularFile(art)&&!Files.isSymbolicLink(art)){
                        byte[] bytes=bounded(art,1048576);
                        if(sha.equals(hash(bytes))){
                            try(ImageInputStream in=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
                                Iterator<ImageReader> readers=ImageIO.getImageReaders(in);
                                if(readers.hasNext()){ImageReader reader=readers.next();try{reader.setInput(in);if(reader.getWidth(0)<=512&&reader.getHeight(0)<=512){image=reader.read(0);imageHash=sha;}}finally{reader.dispose();}}
                            }
                        }
                    }
                }
                if(sha.equals(imageHash))cover=image;
            }
            return new NowPlayingState(state.status,state.title,state.artist,state.album,state.source,state.capturedAt,state.positionMs,state.durationMs,state.rate,state.artwork,cover);
        }catch(RuntimeException failure){throw new IOException("Invalid media snapshot",failure);}
    }
    private static long number(JsonObject o,String field){return o.has(field)&&!o.get(field).isJsonNull()?o.get(field).getAsLong():-1;}
    private static String string(JsonObject o,String field){return o.has(field)&&!o.get(field).isJsonNull()?NowPlayingState.clean(o.get(field).getAsString()):"";}
    private static byte[] bounded(Path path,int limit)throws IOException{try(InputStream in=Files.newInputStream(path);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] block=new byte[4096];int n,total=0;while((n=in.read(block))!=-1){total+=n;if(total>limit)throw new IOException("Media file exceeds limit");out.write(block,0,n);}return out.toByteArray();}}
    public static String hash(byte[] bytes)throws IOException{try{MessageDigest m=MessageDigest.getInstance("SHA-256");StringBuilder b=new StringBuilder();for(byte value:m.digest(bytes))b.append(String.format(Locale.ROOT,"%02x",value&255));return b.toString();}catch(java.security.NoSuchAlgorithmException e){throw new IOException(e);}}
}
