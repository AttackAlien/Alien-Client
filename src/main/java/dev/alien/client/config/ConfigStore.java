package dev.alien.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Atomic, bounded JSON storage. Profile names are identifiers, never paths. */
public final class ConfigStore {
    private static final int MAX_BYTES = 262144;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File directory;
    private ClientConfig current = new ClientConfig();
    private ClientConfig serverBase;
    private String activeServerProfile;
    private Map<String, String> serverProfiles = new LinkedHashMap<>();

    public ConfigStore(File directory) { this.directory = directory; }
    public File directory() { return directory; }
    public synchronized ClientConfig config() { return current; }

    public synchronized void load() throws IOException {
        Files.createDirectories(directory.toPath());
        File main = safeFile(directory, "config.json");
        if (main.isFile()) {
            try { current = read(main); }
            catch (IOException e) {
                File backup = safeFile(directory, "config.invalid-" + System.currentTimeMillis() + ".json");
                Files.copy(main.toPath(), backup.toPath());
                throw new IOException("Invalid configuration preserved as " + backup.getName() + "; defaults are active", e);
            }
        }
        File bindings = safeFile(directory, "servers.json");
        if (bindings.isFile()) {
            try {
                ServerBindings data = gson.fromJson(readBounded(bindings), ServerBindings.class);
                if (data != null && data.profiles != null) {
                    for (Map.Entry<String, String> entry : data.profiles.entrySet()) {
                        if (entry.getKey().length() <= 512 && validName(entry.getValue())) serverProfiles.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (RuntimeException e) { throw new IOException("Invalid server profile bindings", e); }
        }
        current.normalize();
    }

    public synchronized void save() throws IOException {
        current.normalize();
        File destination = activeServerProfile == null ? safeFile(directory, "config.json")
                : safeFile(new File(directory, "profiles"), activeServerProfile + ".json");
        write(destination, gson.toJson(current));
    }

    public synchronized List<String> listProfiles() {
        File profiles = new File(directory, "profiles");
        String[] files = profiles.list((dir, name) -> name.endsWith(".json") && validName(name.substring(0, name.length() - 5)));
        List<String> result = new ArrayList<>();
        if (files != null) for (String file : files) result.add(file.substring(0, file.length() - 5));
        Collections.sort(result);
        return result;
    }

    public synchronized void saveProfile(String name) throws IOException {
        requireName(name); current.normalize();
        write(safeFile(new File(directory, "profiles"), name + ".json"), gson.toJson(current));
    }

    public synchronized boolean loadProfile(String name) throws IOException {
        requireName(name);
        File file = safeFile(new File(directory, "profiles"), name + ".json");
        if (!file.isFile()) return false;
        ClientConfig next = read(file);
        ClientConfig previous = current;
        current = next;
        try { save(); } catch (IOException e) { current = previous; throw e; }
        return true;
    }

    public synchronized ClientConfig previewProfile(String name) throws IOException {requireName(name);return read(safeFile(new File(directory,"profiles"),name+".json"));}
    public synchronized void duplicateProfile(String from,String to) throws IOException {requireName(from);requireName(to);File target=safeFile(new File(directory,"profiles"),to+".json");if(target.exists())throw new IOException("Profile already exists");write(target,gson.toJson(previewProfile(from)));}
    public synchronized void renameProfile(String from,String to) throws IOException {
        duplicateProfile(from,to);Map<String,String> before=new LinkedHashMap<>(serverProfiles);String active=activeServerProfile;
        try{for(Map.Entry<String,String> e:serverProfiles.entrySet())if(from.equals(e.getValue()))e.setValue(to);if(from.equals(activeServerProfile))activeServerProfile=to;saveBindings();Files.delete(safeFile(new File(directory,"profiles"),from+".json").toPath());}
        catch(IOException e){serverProfiles=before;activeServerProfile=active;saveBindings();Files.deleteIfExists(safeFile(new File(directory,"profiles"),to+".json").toPath());throw e;}
    }
    public synchronized String profileJson(String name) throws IOException {return gson.toJson(previewProfile(name));}
    public synchronized void importProfileJson(String name,String text) throws IOException {requireName(name);File target=safeFile(new File(directory,"profiles"),name+".json");if(target.exists())throw new IOException("Profile already exists");validateText(text);ClientConfig checked=decode(text);write(target,gson.toJson(checked));}

    public synchronized void deleteProfile(String name) throws IOException {
        requireName(name);
        if (name.equals(activeServerProfile)) leaveServerProfile();
        Files.deleteIfExists(safeFile(new File(directory, "profiles"), name + ".json").toPath());
        serverProfiles.values().removeAll(Collections.singleton(name));
        saveBindings();
    }

    /** Import <directory>/imports/<name>.json, without automatically activating it. */
    public synchronized boolean importProfile(String name) throws IOException {
        if (name != null && name.endsWith(".json")) name = name.substring(0, name.length() - 5);
        requireName(name);
        File source = safeFile(new File(directory, "imports"), name + ".json");
        if (!source.isFile()) return false;
        ClientConfig validated = read(source);
        File target = safeFile(new File(directory, "profiles"), name + ".json");
        if (target.exists()) throw new IOException("Profile already exists: " + name);
        write(target, gson.toJson(validated));
        return true;
    }

    public synchronized File exportProfile(String name) throws IOException {
        requireName(name);
        File source = safeFile(new File(directory, "profiles"), name + ".json");
        if (!source.isFile()) throw new FileNotFoundException("Save the profile first");
        ClientConfig validated = read(source);
        File target = safeFile(new File(directory, "exports"), name + ".json");
        write(target, gson.toJson(validated));
        return target;
    }

    public synchronized void resetDefaults() throws IOException {
        current = new ClientConfig(); current.normalize(); save();
    }

    public synchronized void bindServer(String server, String profile) throws IOException {
        if (server == null || server.isEmpty() || server.length() > 512) throw new IOException("Invalid server identity");
        requireName(profile);
        if (!safeFile(new File(directory, "profiles"), profile + ".json").isFile()) throw new IOException("Save profile before binding");
        serverProfiles.put(server, profile); saveBindings();
    }
    public synchronized boolean loadServerProfile(String server) throws IOException {
        String profile = serverProfiles.get(server);
        if (profile == null) return false;
        File source = safeFile(new File(directory, "profiles"), profile + ".json");
        if (!source.isFile()) return false;
        ClientConfig loaded = read(source);
        loaded.perServerProfiles = current.perServerProfiles;
        if (serverBase == null) serverBase = current;
        activeServerProfile = profile;
        current = loaded;
        return true;
    }
    public synchronized void leaveServerProfile() {
        if (serverBase != null) { serverBase.perServerProfiles = current.perServerProfiles; current = serverBase; }
        serverBase = null; activeServerProfile = null;
    }
    public synchronized String serverProfile(String server) { return serverProfiles.get(server); }

    private void saveBindings() throws IOException {
        ServerBindings bindings = new ServerBindings(); bindings.profiles.putAll(serverProfiles);
        write(safeFile(directory, "servers.json"), gson.toJson(bindings));
    }
    private static final class ServerBindings { Map<String, String> profiles = new LinkedHashMap<>(); }

    private ClientConfig read(File file) throws IOException {
        return decode(readBounded(file));
    }
    private ClientConfig decode(String text) throws IOException {
        try {
            com.google.gson.JsonElement document = new com.google.gson.JsonParser().parse(text);
            ClientConfig config = gson.fromJson(document, ClientConfig.class);
            if (config == null) throw new IOException("Empty profile");
            config.normalize();
            // Profiles from Unified 0.3 only have these appearance aliases.
            // Migrate once, while respecting explicit 0.4 design tokens.
            if (document.isJsonObject() && (!document.getAsJsonObject().has("ui")
                    || document.getAsJsonObject().get("ui").isJsonNull())) {
                config.ui.primary = config.accentColor;
                config.ui.opacity = config.opacity;
                config.ui.animationSpeed = config.animationSpeed;
                config.ui.preset = "Custom RGB";
                config.ui.normalize();
            }
            return config;
        } catch (RuntimeException e) { throw new IOException("Invalid JSON or unsupported profile", e); }
    }

    private static String readBounded(File file) throws IOException {
        try (InputStream in = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count;
            while ((count = in.read(buffer)) != -1) {
                if (out.size() + count > MAX_BYTES) throw new IOException("JSON limit is 256 KiB");
                out.write(buffer, 0, count);
            }
            String text = new String(out.toByteArray(), StandardCharsets.UTF_8);validateText(text);return text;
        }
    }
    private static void validateText(String text)throws IOException {
            if(text==null||text.getBytes(StandardCharsets.UTF_8).length>MAX_BYTES)throw new IOException("JSON limit is 256 KiB");
            int depth = 0; boolean quoted = false, escaped = false;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (quoted) { if (escaped) escaped = false; else if (c == '\\') escaped = true; else if (c == '"') quoted = false; }
                else if (c == '"') quoted = true;
                else if (c == '{' || c == '[') { if (++depth > 32) throw new IOException("JSON nesting exceeds 32"); }
                else if (c == '}' || c == ']') depth--;
            }
    }

    private static File safeFile(File folder, String filename) throws IOException {
        File file = new File(folder, filename);
        if (!file.getCanonicalFile().getParentFile().equals(folder.getCanonicalFile())) throw new IOException("Path leaves config directory");
        // Reject symlinked files/directories, including profiles/imports/exports.
        for (File path = file; path != null; path = path.getParentFile()) {
            if (Files.isSymbolicLink(path.toPath())) throw new IOException("Symbolic links are not accepted for profile storage");
        }
        return file;
    }
    private static boolean validName(String name) { return name != null && name.matches("[A-Za-z0-9_-]{1,48}"); }
    private static void requireName(String name) throws IOException { if (!validName(name)) throw new IOException("Use 1-48 letters, digits, _ or -"); }
    private static void write(File destination, String json) throws IOException {
        Files.createDirectories(destination.toPath().getParent());
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) throw new IOException("Configuration exceeds 256 KiB");
        Path temp = Files.createTempFile(destination.toPath().getParent(), ".alien-", ".tmp");
        try {
            Files.write(temp, bytes);
            try { Files.move(temp, destination.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, destination.toPath(), StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
