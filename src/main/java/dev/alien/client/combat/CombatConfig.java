package dev.alien.client.combat;

/** Explicit user-controlled combat presets. Never changes server-side reach or hit rules. */
public final class CombatConfig {
    public String preset = "OFF";
    public boolean enabled;
    public boolean aimAssist;
    public boolean clickAssist;
    public boolean requireAttackHeld = true;
    public boolean visibleTargetsOnly = true;
    public float aimStrength = .15F;
    public float aimFov = 35F;
    public float targetRange = 3.3F;
    public float clicksPerSecond = 6F;

    public void applyPreset(String presetName) {
        if ("OFF".equals(presetName)) {
            preset = "OFF"; enabled = false; aimAssist = false; clickAssist = false;
            requireAttackHeld = true; visibleTargetsOnly = true;
            aimStrength = .15F; aimFov = 35F; targetRange = 3.3F; clicksPerSecond = 6F;
        } else if ("LEGIT".equals(presetName)) {
            preset = "LEGIT"; enabled = true; aimAssist = true; clickAssist = false;
            requireAttackHeld = true; visibleTargetsOnly = true;
            aimStrength = .12F; aimFov = 25F; targetRange = 3.3F; clicksPerSecond = 6F;
        } else if ("FULL".equals(presetName)) {
            preset = "FULL"; enabled = true; aimAssist = true; clickAssist = true;
            requireAttackHeld = false; visibleTargetsOnly = true;
            aimStrength = .55F; aimFov = 110F; targetRange = 5F; clicksPerSecond = 10F;
        } else if ("CUSTOM".equals(presetName)) {
            preset = "CUSTOM";
        } else throw new IllegalArgumentException("Unknown combat preset");
        normalize();
    }

    public void normalize() {
        if (!"OFF".equals(preset) && !"LEGIT".equals(preset)
                && !"FULL".equals(preset) && !"CUSTOM".equals(preset)) preset = "OFF";
        aimStrength = bounded(aimStrength,.01F,1F,.15F);
        aimFov = bounded(aimFov,5F,180F,35F);
        targetRange = bounded(targetRange,2F,6F,3.3F);
        clicksPerSecond = bounded(clicksPerSecond,1F,12F,6F);
    }

    private static float bounded(float value,float min,float max,float fallback) {
        if(Float.isNaN(value)||Float.isInfinite(value))return fallback;
        return Math.max(min,Math.min(max,value));
    }
}
