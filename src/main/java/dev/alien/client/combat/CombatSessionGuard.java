package dev.alien.client.combat;

/** Permission is ephemeral and bound to both loaded world and active combat profile. */
public final class CombatSessionGuard {
    private Object world;
    private Object profile;
    private boolean approved;

    private void observe(Object currentWorld, Object currentProfile) {
        if (world != currentWorld || profile != currentProfile || currentWorld == null || currentProfile == null) {
            world = currentWorld;
            profile = currentProfile;
            approved = false;
        }
    }

    public boolean permits(Object currentWorld, Object currentProfile, boolean singleplayer) {
        observe(currentWorld, currentProfile);
        return currentWorld != null && currentProfile != null && (singleplayer || approved);
    }

    public boolean isApproved(Object currentWorld, Object currentProfile) {
        observe(currentWorld, currentProfile);
        return currentWorld != null && currentProfile != null && approved;
    }

    public boolean approve(Object currentWorld, Object currentProfile, boolean singleplayer) {
        observe(currentWorld, currentProfile);
        if (currentWorld == null || currentProfile == null || singleplayer) return false;
        approved = true;
        return true;
    }

    public void revoke() { approved = false; }
}
