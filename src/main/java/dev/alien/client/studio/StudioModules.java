package dev.alien.client.studio;
import dev.alien.client.config.ClientConfig;import dev.alien.client.module.*;import dev.alien.client.scene.SceneConfig;import java.util.*;import java.util.function.*;
/** Presentation adapters to existing preferences; no shadow module state or invented toggles. */
public final class StudioModules {
    public static final class Entry {public final String id,title,category,description;private final BooleanSupplier get;private final Consumer<Boolean> set;Entry(String id,String title,String cat,String desc,BooleanSupplier g,Consumer<Boolean> s){this.id=id;this.title=title;category=cat;description=desc;get=g;set=s;}public boolean enabled(){return get.getAsBoolean();}public void toggle(){set.accept(!enabled());}}
    private static void add(List<Entry> l,String id,String title,String cat,String d,BooleanSupplier g,Consumer<Boolean> s){l.add(new Entry(id,title,cat,d,g,s));}
    public static List<Entry> entries(ClientConfig c,ModuleManager manager){List<Entry> l=new ArrayList<Entry>();if(manager!=null)for(ModuleDescriptor m:manager.modules())add(l,m.id(),m.name(),m.category()==ModuleCategory.COMBAT?"Combat":"Render",m.description(),m::enabled,m::setEnabled);
        SceneConfig s=c.scenes;
        add(l,"scene_sky","Living Sky","Render","Animated procedural skies and palettes.",()->s.enabled&&s.skyIndex()>0,v->{s.sky=v?"Galaxy":"Off";if(v)s.enabled=true;});
        add(l,"ambience","Ambience","Render","Existing bounded ambient particles.",()->c.effects.enabled&&c.effects.ambient,v->{c.effects.ambient=v;if(v)c.effects.enabled=true;});
        add(l,"mist","Mist","Render","Depth-based local distance mist.",()->s.enabled&&s.mist,v->{s.mist=v;if(v)s.enabled=true;});
        add(l,"afterglow","Afterglow","Render","Dissolving light at actual entity deaths.",()->s.enabled&&s.afterglow,v->{s.afterglow=v;if(v)s.enabled=true;});
        add(l,"radiance","Radiance","Render","Luminous screen edges before the HUD.",()->s.enabled&&s.radiance,v->{s.radiance=v;if(v)s.enabled=true;});
        add(l,"target_esp","Target ESP","Render","Animated ring around your visible HUD target.",()->s.enabled&&s.targetEsp,v->{s.targetEsp=v;if(v)s.enabled=true;});
        add(l,"shockwave","Shockwave","Render","Light wave on actual player landing.",()->s.enabled&&s.shockwave,v->{s.shockwave=v;if(v)s.enabled=true;});
        add(l,"bokeh","Bokeh","Render","Depth-aware focus blur; center stays sharp.",()->s.enabled&&s.bokeh,v->{s.bokeh=v;if(v)s.enabled=true;});
        add(l,"cute","Cute World","Render","Blinking original player face artwork.",()->s.enabled&&s.cute,v->{s.cute=v;if(v)s.enabled=true;});
        for(String mode:new String[]{"Cyberspace","Skeleton","Glass","Chroma"})add(l,"scene_"+mode.toLowerCase(Locale.ROOT),mode.equals("Skeleton")?"X-Ray bones":mode,"Render","Existing local "+mode+" scene material.",()->s.enabled&&mode.equals(s.mode),v->{s.mode=v?mode:"Natural";if(v)s.enabled=true;});
        add(l,"zoom","Zoom / hold C","Player","Hold C in-world for optical FOV zoom.",()->c.studio.zoom,v->c.studio.zoom=v);
        add(l,"hand_movement","Hand Movement","Player","Subtle animated first-person hand sway.",()->c.studio.handMovement,v->c.studio.handMovement=v);
        add(l,"name_tags","Name Tags","Player","Visible player names with actual health.",()->c.studio.nameTags,v->c.studio.nameTags=v);
        for(Map.Entry<String,dev.alien.client.hud.HudConfig.Widget> pair:c.hud.widgets.entrySet()){String id=pair.getKey();dev.alien.client.hud.HudConfig.Widget widget=pair.getValue();if(widget==null)continue;add(l,"hud_"+id,dev.alien.client.hud.HudConfig.label(id),"HUD","Existing editable HUD widget; drag/scale in HUD Studio.",()->c.hud.enabled&&widget.enabled,v->{widget.enabled=v;if(v)c.hud.enabled=true;});}return l;
    }
    public static Set<String> enabled(ClientConfig c,ModuleManager m){Set<String> ids=new LinkedHashSet<String>();for(Entry e:entries(c,m))if(e.enabled())ids.add(e.id);return ids;}
}
