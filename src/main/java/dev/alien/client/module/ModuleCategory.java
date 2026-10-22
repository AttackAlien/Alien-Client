package dev.alien.client.module;

public enum ModuleCategory {
    HUD("HUD"),
    COSMETICS("Cosmetics"),
    WORLD("World"),
    EFFECTS("Effects"),
    COMBAT("Combat");

    private final String label;

    ModuleCategory(String label) { this.label = label; }

    public String label() { return label; }
}
