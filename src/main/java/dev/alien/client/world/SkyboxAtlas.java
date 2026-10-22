package dev.alien.client.world;

import java.io.IOException;

/** Six square faces in one PNG: rows (+X, -X, +Y), (-Y, +Z, -Z). */
final class SkyboxAtlas {
    private static final int[][][] CORNERS = {
        {{1,1,-1},{1,1,1},{1,-1,1},{1,-1,-1}},
        {{-1,1,1},{-1,1,-1},{-1,-1,-1},{-1,-1,1}},
        {{-1,1,-1},{1,1,-1},{1,1,1},{-1,1,1}},
        {{-1,-1,1},{1,-1,1},{1,-1,-1},{-1,-1,-1}},
        {{1,1,1},{-1,1,1},{-1,-1,1},{1,-1,1}},
        {{-1,1,-1},{1,1,-1},{1,-1,-1},{-1,-1,-1}}
    };
    private final int size;
    private SkyboxAtlas(int size) { this.size = size; }

    static boolean isCubeFile(String name) {
        return name != null && name.endsWith(".cube.png");
    }

    static SkyboxAtlas validate(int width, int height) throws IOException {
        if (width % 3 != 0 || height % 2 != 0 || width / 3 != height / 2
                || width / 3 < 64 || width / 3 > 1024) {
            throw new IOException("Cubemap requires a 3x2 PNG with six square faces, 64..1024px each");
        }
        return new SkyboxAtlas(width / 3);
    }

    int faceSize() { return size; }

    float u(int face, int corner) {
        check(face, corner);
        return ((face % 3) * size + (corner == 0 || corner == 3 ? .5F : size - .5F)) / (size * 3F);
    }

    float v(int face, int corner) {
        check(face, corner);
        return 1F - ((face / 3) * size + (corner < 2 ? .5F : size - .5F)) / (size * 2F);
    }

    double coordinate(int face, int corner, int axis) {
        check(face, corner);
        if (axis < 0 || axis > 2) throw new IllegalArgumentException("Invalid coordinate axis");
        return CORNERS[face][corner][axis] * (100D / Math.sqrt(3D));
    }

    private static void check(int face, int corner) {
        if (face < 0 || face >= 6 || corner < 0 || corner >= 4)
            throw new IllegalArgumentException("Invalid cubemap face/corner");
    }
}
