package dev.alien.client.edition;

/** One installable mod with per-profile Legit, Full and Custom combat settings. */
public final class BuildEdition {
    public static final String ID = "unified";
    public static final String NAME = "Alien Client";
    public static final boolean ALLOW_FULL = true;
    /** Reuse the old Legit config root so HUD and visuals are retained. */
    public static final String CONFIG_DIRECTORY = "alien-client";
    public static final String STATUS = "Unified / configurable combat";
    private BuildEdition() { }
}
