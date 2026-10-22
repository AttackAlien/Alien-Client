package dev.alien.client.cosmetic;

/** Bundled cosmetic combinations built entirely from the client’s original meshes. */
public final class CosmeticLooks {
    public static final String[] NAMES = {"Ronin", "Celestial", "Sakura", "Phantom"};
    private CosmeticLooks() { }

    /** Only appearance is replaced: retain the owner's fit/position/rotation. */
    public static void apply(CosmeticConfig.Accessory a, String name) {
        if (a == null) throw new IllegalArgumentException("Accessory is missing");
        if (!"Ronin".equals(name) && !"Celestial".equals(name)
                && !"Sakura".equals(name) && !"Phantom".equals(name))
            throw new IllegalArgumentException("Unknown cosmetic look");
        a.enabled = true;
        a.modelFile = "";
        a.textureFile = "";
        a.animations = true;
        a.charm = false;
        a.aura = false;
        a.trail = false;
        a.capePattern = "ALIEN";
        a.detailLevel = "Balanced";
        a.capeFlutter = a.capeLength = a.hatSize = 1;
        if ("Ronin".equals(name)) {
            a.hat="SAMURAI"; a.face="KITSUNE"; a.back="CAPE";
            a.color=0x252333; a.secondaryColor=0xD6B56C;
            a.capeFlutter=1.15f; a.capeLength=1.05f;
        } else if ("Celestial".equals(name)) {
            a.hat="CROWN"; a.face="NONE"; a.back="WINGS"; a.aura=true;
            a.color=0xAAB4ED; a.secondaryColor=0x74E0D4;
            a.capePattern="STARS";
        } else if ("Sakura".equals(name)) {
            a.hat="JAPANESE"; a.face="KITSUNE"; a.back="CAPE"; a.trail=true;
            a.color=0xEFADCC; a.secondaryColor=0x775AB1;
            a.capePattern="MOON"; a.capeFlutter=.85f;
        } else {
            a.hat="NONE"; a.face="FUTURE"; a.back="WINGS"; a.charm=true;
            a.color=0x20233C; a.secondaryColor=0x54EFDF;
            a.capePattern="PLAIN"; a.detailLevel="High";
        }
        a.normalize();
    }
}
