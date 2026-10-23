package dev.alien.client.module;

import dev.alien.client.config.ClientConfig;
import dev.alien.client.edition.BuildEdition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Ordered edition-aware registry. It owns no Minecraft state, key polling or files. */
public final class ModuleManager {
    public static final int MIN_KEY = 1;
    public static final int MAX_KEY = 255;
    private final boolean allowFull;
    private final Map<String, ModuleDescriptor> modules = new LinkedHashMap<String, ModuleDescriptor>();

    public ModuleManager(boolean allowFull) { this.allowFull = allowFull; }

    /** Every accessor reads the supplied current profile, including after profile swaps. */
    public static ModuleManager visuals(Supplier<ClientConfig> config, boolean allowFull) {
        Objects.requireNonNull(config, "config");
        ModuleManager manager = new ModuleManager(allowFull);
        manager.register(new ModuleDescriptor("hud", "HUD", ModuleCategory.HUD,
                "Show the configured local HUD widgets.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> current(config).hud.enabled, value -> current(config).hud.enabled = value));
        manager.register(new ModuleDescriptor("cosmetics", "Cosmetics", ModuleCategory.COSMETICS,
                "Render locally configured accessories and cosmetic models.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> current(config).cosmetics.enabled, value -> current(config).cosmetics.enabled = value));
        manager.register(new ModuleDescriptor("sky", "Custom sky", ModuleCategory.WORLD,
                "Render the selected procedural or imported sky in the Overworld.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> current(config).world.enabled, value -> current(config).world.enabled = value));
        manager.register(new ModuleDescriptor("visual_time", "Visual time", ModuleCategory.WORLD,
                "Use the selected hour for the custom sky without changing world time.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> current(config).world.timeOverride, value -> current(config).world.timeOverride = value));
        manager.register(new ModuleDescriptor("fog", "Atmospheric fog", ModuleCategory.WORLD,
                "Apply the configured local fog color and density.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> current(config).world.fogEnabled, value -> current(config).world.fogEnabled = value));
        manager.register(new ModuleDescriptor("weather", "Weather preview", ModuleCategory.WORLD,
                "Replace local precipitation rendering; enabling selects snow when following server weather.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> !"Server".equals(current(config).world.weather), value -> {
                    ClientConfig active = current(config);
                    if (!value) active.world.weather = "Server";
                    else if ("Server".equals(active.world.weather)) active.world.weather = "Snow";
                }));
        manager.register(new ModuleDescriptor("effects", "Visual effects", ModuleCategory.EFFECTS,
                "Enable the configured local particles, color filter and camera effects.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> current(config).effects.enabled, value -> current(config).effects.enabled = value));
        manager.register(new ModuleDescriptor("camera_sway", "Camera sway", ModuleCategory.EFFECTS,
                "Enable movement-based camera roll when visual effects are enabled.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> current(config).effects.cameraSway, value -> current(config).effects.cameraSway = value));
        manager.register(new ModuleDescriptor("color_filter", "Color filter", ModuleCategory.EFFECTS,
                "Enable the local color overlay when visual effects are enabled.", GameplayImpact.LOCAL_VISUAL, AccessLevel.SHARED,
                () -> current(config).effects.colorFilter, value -> current(config).effects.colorFilter = value));
        if (allowFull && "unified".equals(BuildEdition.ID)) {
            manager.register(new ModuleDescriptor("aim_assist", "Aim assist", ModuleCategory.COMBAT,
                    "Optional gradual aim help towards visible players; not silent aim.",
                    GameplayImpact.GAMEPLAY_CHANGING, AccessLevel.FULL,
                    () -> current(config).combat.aimAssist,
                    value -> {
                        current(config).combat.aimAssist = value;
                        current(config).combat.preset = "CUSTOM";
                    }));
            manager.register(new ModuleDescriptor("click_assist", "Click assist", ModuleCategory.COMBAT,
                    "Optional repeated vanilla attack actions while left mouse is held.",
                    GameplayImpact.GAMEPLAY_CHANGING, AccessLevel.FULL,
                    () -> current(config).combat.clickAssist,
                    value -> {
                        current(config).combat.clickAssist = value;
                        current(config).combat.preset = "CUSTOM";
                    }));
        }
        if(allowFull && "unified".equals(BuildEdition.ID)) {
            String[] ids={"aimbot","auto_food","auto_potion","anti_aim","bridge","auto_sprint"};
            String[] names={"Aimbot","Auto Food","Auto Potion","Anti Aim / spin","Auto Bridge","Auto Sprint"};
            String[] descriptions={"Instant visible-player aim through the normal camera; uses configured FOV/range.",
                "Eat food from the hotbar below the configured hunger threshold; restores your slot.",
                "Use a hotbar healing or regeneration potion below the health threshold.",
                "Rotate the actual camera while idle; no fake angles or desync.",
                "Place hotbar blocks below your feet using adjacent support and vanilla reach.",
                "Sprint while moving forwards when vanilla hunger and collision rules allow."};
            for(int i=0;i<ids.length;i++){final int n=i;
                manager.register(new ModuleDescriptor(ids[i],names[i],ModuleCategory.COMBAT,descriptions[i],GameplayImpact.GAMEPLAY_CHANGING,AccessLevel.FULL,
                    ()->tool(current(config),n),value->{setTool(current(config),n,value);current(config).combat.preset="CUSTOM";}));
            }
        }
        return manager;
    }
    private static boolean tool(ClientConfig c,int n){
        dev.alien.client.combat.FullToolsConfig f=c.combat.tools;
        return n==0?f.aimbot:n==1?f.autoFood:n==2?f.autoPotion:n==3?f.antiAim:n==4?f.bridge:f.autoSprint;
    }
    private static void setTool(ClientConfig c,int n,boolean value){
        dev.alien.client.combat.FullToolsConfig f=c.combat.tools;
        if(n==0){f.aimbot=value;if(value)f.antiAim=false;}
        if(n==1)f.autoFood=value;if(n==2)f.autoPotion=value;
        if(n==3){f.antiAim=value;if(value){c.combat.aimAssist=false;f.aimbot=false;}}
        if(n==4)f.bridge=value;if(n==5)f.autoSprint=value;
    }

    private static ClientConfig current(Supplier<ClientConfig> config) {
        return Objects.requireNonNull(config.get(), "current profile");
    }

    public void register(ModuleDescriptor module) {
        Objects.requireNonNull(module, "module");
        if (!allowFull && module.accessLevel() == AccessLevel.FULL)
            throw new IllegalArgumentException("Full-only module cannot be registered in the Legit edition: " + module.id());
        if (!allowFull && module.gameplayImpact() != GameplayImpact.LOCAL_VISUAL)
            throw new IllegalArgumentException("Only local visual modules can be registered in the Legit edition: " + module.id());
        if (modules.containsKey(module.id()))
            throw new IllegalArgumentException("Duplicate module ID: " + module.id());
        modules.put(module.id(), module);
    }

    /** Immutable snapshot in registration order. */
    public List<ModuleDescriptor> modules() { return immutable(new ArrayList<ModuleDescriptor>(modules.values())); }

    public ModuleDescriptor find(String id) { return modules.get(id); }

    public List<ModuleDescriptor> category(ModuleCategory category) {
        Objects.requireNonNull(category, "category");
        List<ModuleDescriptor> result = new ArrayList<ModuleDescriptor>();
        for (ModuleDescriptor module : modules.values()) if (module.category() == category) result.add(module);
        return immutable(result);
    }

    /** Matches stable ID, display name, category or description, ignoring letter case. */
    public List<ModuleDescriptor> search(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<ModuleDescriptor> result = new ArrayList<ModuleDescriptor>();
        for (ModuleDescriptor module : modules.values()) {
            String text = module.id() + " " + module.name() + " " + module.category().label() + " " + module.description();
            if (text.toLowerCase(Locale.ROOT).contains(normalized)) result.add(module);
        }
        return immutable(result);
    }

    /** Returns false for an unknown ID; otherwise changes that switch alone. */
    public boolean toggle(String id) {
        ModuleDescriptor module = find(id);
        if (module == null) return false;
        module.toggle();
        return true;
    }

    /**
     * Zero is unbound. Only valid registered IDs are consulted, so arbitrary map
     * entries cannot run actions. Shared keys toggle once each in registration order.
     */
    public boolean toggleKey(int key, Map<String, Integer> bindings) {
        if (key < MIN_KEY || key > MAX_KEY || bindings == null) return false;
        boolean changed = false;
        for (ModuleDescriptor module : modules.values()) {
            Integer bound = bindings.get(module.id());
            if (bound != null && bound == key) {
                module.toggle();
                changed = true;
            }
        }
        return changed;
    }

    private static List<ModuleDescriptor> immutable(List<ModuleDescriptor> modules) {
        return Collections.unmodifiableList(modules);
    }
}
