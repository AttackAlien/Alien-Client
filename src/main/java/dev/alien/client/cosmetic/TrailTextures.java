package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

/** Four builtins, eight reusable GPU slots and 32 cached rejected imports. Reload is explicit. */
final class TrailTextures {
    private static final Map<String,Binding> BUILTINS=new HashMap<String,Binding>();
    private static final LinkedHashMap<String,Binding> IMPORTED=new LinkedHashMap<String,Binding>(16,.75f,true);
    private static final Map<String,Binding> REJECTED=new LinkedHashMap<String,Binding>();
    static final class Binding {
        final ResourceLocation location; final String name,error; final int slot;
        Binding(ResourceLocation location,String name,String error,int slot) { this.location=location;this.name=name;this.error=error;this.slot=slot; }
    }
    static File directory() { return new File(AlienClient.get().store().directory(),"cosmetics/trails"); }
    static Binding resolve(TrailDesign d) {
        if (!"PNG".equals(d.texture)) return builtin(TrailDesign.isBuiltin(d.texture)?d.texture:"Soft");
        String name=d.customTexture; Binding cached=IMPORTED.get(name);
        if (cached!=null) return cached;
        if (REJECTED.containsKey(name)) return REJECTED.get(name);
        try {
            BufferedImage image=TrailTextureLoader.load(directory(),name);
            int slot=IMPORTED.size();
            if (slot>=8) { String first=IMPORTED.keySet().iterator().next(); Binding old=IMPORTED.remove(first);slot=old.slot;Minecraft.getMinecraft().getTextureManager().deleteTexture(old.location); }
            ResourceLocation location=new ResourceLocation("alienclient","trail/imported_"+slot);
            Minecraft.getMinecraft().getTextureManager().loadTexture(location,new DynamicTexture(image));
            cached=new Binding(location,name,"",slot); IMPORTED.put(name,cached);return cached;
        } catch (Exception failure) {
            Binding fallback=builtin("Soft");
            cached=new Binding(fallback.location,"Soft / fallback",failure.getMessage()==null?"PNG rejected":failure.getMessage(),-1);
            if (REJECTED.size()>=32) REJECTED.remove(REJECTED.keySet().iterator().next());
            REJECTED.put(name,cached);return cached;
        }
    }
    private static Binding builtin(String name) {
        Binding binding=BUILTINS.get(name);
        if (binding==null) {
            ResourceLocation location=new ResourceLocation("alienclient","trail/builtin_"+name.toLowerCase(Locale.ROOT));
            Minecraft.getMinecraft().getTextureManager().loadTexture(location,new DynamicTexture(TrailTextureLoader.builtin(name)));
            binding=new Binding(location,name,"",-1);BUILTINS.put(name,binding);
        }
        return binding;
    }
    static void reloadImported() {
        for (Binding b:IMPORTED.values()) Minecraft.getMinecraft().getTextureManager().deleteTexture(b.location);
        IMPORTED.clear();REJECTED.clear();
    }
}
