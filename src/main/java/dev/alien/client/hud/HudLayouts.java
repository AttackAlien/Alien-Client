package dev.alien.client.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Bounded HUD-only exchange; importing never replaces other client settings. */
public final class HudLayouts {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private HudLayouts() { }
    private static File path(File root,String folder) throws IOException {
        File dir=new File(root,"hud/"+folder),file=new File(dir,"layout.json");
        for(File p=file;p!=null;p=p.getParentFile()) if(Files.isSymbolicLink(p.toPath()))throw new IOException("HUD paths cannot use symbolic links");
        Files.createDirectories(dir.toPath());return file;
    }
    public static void exportLayout(File root,HudConfig config) throws IOException {
        File target=path(root,"exports"),temp=File.createTempFile("hud-", ".tmp",target.getParentFile());
        try { Files.write(temp.toPath(),GSON.toJson(config).getBytes(StandardCharsets.UTF_8));Files.move(temp.toPath(),target.toPath(),StandardCopyOption.REPLACE_EXISTING); }
        finally {Files.deleteIfExists(temp.toPath());}
    }
    public static HudConfig importLayout(File root) throws IOException {
        File file=path(root,"imports");if(!file.isFile() || file.length()>65536)throw new IOException("Use hud/imports/layout.json, maximum 64 KiB");
        byte[] bytes=Files.readAllBytes(file.toPath());if(bytes.length>65536)throw new IOException("HUD layout exceeds 64 KiB");
        String json=new String(bytes,StandardCharsets.UTF_8);int depth=0;boolean quoted=false,escaped=false;
        for(char c:json.toCharArray()) {if(quoted){if(escaped)escaped=false;else if(c=='\\')escaped=true;else if(c=='\"')quoted=false;}
            else if(c=='\"')quoted=true;else if(c=='{'||c=='['){if(++depth>16)throw new IOException("HUD JSON nesting limit is 16");}else if(c=='}'||c==']')depth--;}
        try {HudConfig config=GSON.fromJson(json,HudConfig.class);if(config==null)throw new IOException("Missing HUD config");config.ensureDefaults();return config;}
        catch(RuntimeException failure){throw new IOException("Invalid HUD layout",failure);}
    }
}
