package dev.alien.client.input;

import dev.alien.client.config.ClientConfig;
import dev.alien.client.module.ModuleDescriptor;
import dev.alien.client.module.ModuleManager;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

/** Real Controls bindings backed by the current profile, including profile swaps. */
public final class ModuleKeybindManager {
    private final ModuleManager modules;
    private final Supplier<ClientConfig> config;
    private final Map<String, KeyBinding> bindings = new LinkedHashMap<String, KeyBinding>();
    private final Map<String, Integer> previous = new LinkedHashMap<String, Integer>();

    public ModuleKeybindManager(ModuleManager modules, Supplier<ClientConfig> config) {
        this.modules = modules; this.config = config;
        for (ModuleDescriptor module : modules.modules()) {
            int code = configured(module.id());
            KeyBinding binding = new KeyBinding("Alien / " + module.name(), code, "Alien Modules");
            ClientRegistry.registerKeyBinding(binding);
            bindings.put(module.id(), binding); previous.put(module.id(), code);
        }
    }

    private int configured(String id) {
        Integer code = config.get().moduleKeys.get(id);
        return code == null || code < 0 || code > 255 ? 0 : code;
    }

    /** Returns true only when Minecraft Controls changed a saved key. */
    public boolean synchronize() {
        boolean save = false, changed = false;
        for (Map.Entry<String, KeyBinding> entry : bindings.entrySet()) {
            String id = entry.getKey(); KeyBinding binding = entry.getValue();
            int wanted = configured(id), old = previous.get(id);
            if (wanted != old) { binding.setKeyCode(wanted); previous.put(id, wanted); changed = true; }
            else if (binding.getKeyCode() != old) {
                int selected = binding.getKeyCode();
                // Mouse buttons use negative codes; module shortcuts are keyboard-only.
                if (selected < 0 || selected > 255) { selected = 0; binding.setKeyCode(0); changed = true; }
                config.get().moduleKeys.put(id, selected); previous.put(id, selected); save = true;
            }
        }
        if (changed) KeyBinding.resetKeyBindingArrayAndHash();
        return save;
    }

    /** Always drain presses, so opening a screen never queues an accidental toggle. */
    public boolean consume(boolean allowToggle) {
        Set<Integer> pressed = new LinkedHashSet<Integer>();
        for (KeyBinding binding : bindings.values()) {
            boolean down = false;
            while (binding.isPressed()) down = true;
            if (down && binding.getKeyCode() > 0) pressed.add(binding.getKeyCode());
        }
        boolean changed = false;
        if (allowToggle) for (Integer key : pressed) changed |= modules.toggleKey(key, config.get().moduleKeys);
        return changed;
    }
}
