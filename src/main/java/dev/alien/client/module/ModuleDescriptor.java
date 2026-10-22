package dev.alien.client.module;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Immutable metadata and live access to one existing configuration switch. */
public final class ModuleDescriptor {
    private final String id, name, description;
    private final ModuleCategory category;
    private final GameplayImpact gameplayImpact;
    private final AccessLevel accessLevel;
    private final BooleanSupplier enabled;
    private final Consumer<Boolean> setter;

    public ModuleDescriptor(String id, String name, ModuleCategory category, String description,
                            GameplayImpact gameplayImpact, AccessLevel accessLevel,
                            BooleanSupplier enabled, Consumer<Boolean> setter) {
        if (id == null || !id.matches("[a-z][a-z0-9_]{0,47}"))
            throw new IllegalArgumentException("Module IDs must be stable lowercase identifiers");
        if (name == null || name.trim().isEmpty() || description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("Modules require a name and a description");
        this.id = id;
        this.name = name;
        this.category = Objects.requireNonNull(category, "category");
        this.description = description;
        this.gameplayImpact = Objects.requireNonNull(gameplayImpact, "gameplayImpact");
        this.accessLevel = Objects.requireNonNull(accessLevel, "accessLevel");
        this.enabled = Objects.requireNonNull(enabled, "enabled");
        this.setter = Objects.requireNonNull(setter, "setter");
    }

    public String id() { return id; }
    public String name() { return name; }
    public ModuleCategory category() { return category; }
    public String description() { return description; }
    public GameplayImpact gameplayImpact() { return gameplayImpact; }
    public AccessLevel accessLevel() { return accessLevel; }

    /** Reports this switch's setting; parent switches remain independent. */
    public boolean enabled() { return enabled.getAsBoolean(); }

    public void setEnabled(boolean value) { setter.accept(value); }

    public void toggle() { setEnabled(!enabled()); }
}
