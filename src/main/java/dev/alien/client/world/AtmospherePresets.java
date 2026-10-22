package dev.alien.client.world;

/** Coherent render-only palettes. Applying one is an explicit local visual choice. */
public final class AtmospherePresets {
    private AtmospherePresets() { }

    public static void apply(WorldConfig world, EffectsConfig effects, String preset) {
        if (!("Crystal".equals(preset) || "Sakura".equals(preset) || "Nebula".equals(preset)))
            throw new IllegalArgumentException("Unknown atmosphere preset");
        world.enabled = true;
        world.applyTheme(preset);
        world.skyboxFile = "";
        world.timeOverride = true;
        world.visualHour = "Crystal".equals(preset) ? 13 : "Sakura".equals(preset) ? 18 : 0;
        world.stars = "Crystal".equals(preset) ? 0 : "Sakura".equals(preset) ? 90 : 420;
        world.fogEnabled = true;
        world.fogDensity = "Nebula".equals(preset) ? .0025F : .0014F;
        world.weather = "Clear";
        effects.enabled = true;
        effects.ambient = true;
        effects.movement = true;
        effects.landing = true;
        effects.trails = true;
        effects.quality = "Balanced";
        effects.budget = 72;
        effects.distance = 24;
        effects.size = "Sakura".equals(preset) ? 3.7F : 3.1F;
        effects.shape = "Crystal".equals(preset) ? "Diamond" : "Sakura".equals(preset) ? "Petal" : "Spark";
        effects.color = "Crystal".equals(preset) ? 0x7DF5AC : "Sakura".equals(preset) ? 0xF4B0D1 : 0xB690FF;
        effects.secondaryColor = "Crystal".equals(preset) ? 0xF4E687 : "Sakura".equals(preset) ? 0xFFF0F8 : 0x85E8F3;
        effects.colorFilter = false;
        effects.cameraSway = false;
        world.normalize(); effects.normalize();
    }
}
