package dev.alien.client.cosmetic.mesh;

/** Immutable cached triangle geometry. Public callers can never mutate its buffers. */
public final class Mesh implements MeshView {
    public static final int MAIN = 0, ACCENT = 1, SHADOW = 2, HIGHLIGHT = 3;
    public static final Mesh EMPTY = new Mesh(new float[0], new float[0], new byte[0]);
    private final float[] positions, normals;
    private final byte[] materials;

    Mesh(float[] positions, float[] normals, byte[] materials) {
        if (positions.length % 9 != 0 || positions.length != normals.length || positions.length / 3 != materials.length)
            throw new IllegalArgumentException("Expected complete triangles with normals and materials");
        this.positions = positions.clone(); this.normals = normals.clone(); this.materials = materials.clone();
        for (float value : positions) if (!Float.isFinite(value)) throw new IllegalArgumentException("Non-finite mesh coordinate");
        for (float value : normals) if (!Float.isFinite(value)) throw new IllegalArgumentException("Non-finite mesh normal");
    }
    @Override public int vertexCount() { return materials.length; }
    @Override public float x(int i) { return positions[i * 3]; }
    @Override public float y(int i) { return positions[i * 3 + 1]; }
    @Override public float z(int i) { return positions[i * 3 + 2]; }
    @Override public float nx(int i) { return normals[i * 3]; }
    @Override public float ny(int i) { return normals[i * 3 + 1]; }
    @Override public float nz(int i) { return normals[i * 3 + 2]; }
    @Override public int material(int i) { return materials[i]; }
    @Override public float shade(int i) { return light(nx(i), ny(i), nz(i)); }

    /** Stable studio-like light even when the caller disables the game's item lighting. */
    static float light(float x, float y, float z) {
        return Math.max(.48f, Math.min(1.03f, .73f + x * -.16f + y * -.20f + z * -.17f));
    }
}
