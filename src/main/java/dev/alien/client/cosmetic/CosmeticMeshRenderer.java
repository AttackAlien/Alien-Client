package dev.alien.client.cosmetic;

import dev.alien.client.cosmetic.mesh.Mesh;
import dev.alien.client.cosmetic.mesh.MeshView;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;

/** Bounded cache of static accessory geometry, colored without loading external assets. */
final class CosmeticMeshRenderer {
    private static final int CACHE_LIMIT = 48;
    private static final Map<Key, Integer> LISTS = new LinkedHashMap<Key, Integer>(64, .75f, true);
    private CosmeticMeshRenderer() { }

    static void draw(Mesh mesh, int primary, int secondary) {
        if (mesh.vertexCount() == 0) return;
        Key key = new Key(mesh, primary, secondary);
        Integer list = LISTS.get(key);
        if (list == null) {
            if (LISTS.size() >= CACHE_LIMIT) {
                Iterator<Map.Entry<Key, Integer>> entries = LISTS.entrySet().iterator();
                Map.Entry<Key, Integer> oldest = entries.next();
                GL11.glDeleteLists(oldest.getValue(), 1); entries.remove();
            }
            int generated = GL11.glGenLists(1);
            if (generated == 0) { drawStream(mesh, primary, secondary); return; }
            GL11.glNewList(generated, GL11.GL_COMPILE);
            drawStream(mesh, primary, secondary);
            GL11.glEndList(); LISTS.put(key, generated); list = generated;
        }
        GL11.glCallList(list);
    }

    private static void drawStream(MeshView mesh, int primary, int secondary) {
        GL11.glBegin(GL11.GL_TRIANGLES);
        for (int i = 0; i < mesh.vertexCount(); i++) {
            int material = mesh.material(i);
            int color = material == Mesh.ACCENT ? secondary : material == Mesh.HIGHLIGHT ? mix(primary, secondary, .65f) : primary;
            float light = mesh.shade(i) * (material == Mesh.SHADOW ? .64f : 1f);
            float specular=(material==Mesh.ACCENT||material==Mesh.HIGHLIGHT)
                    ?(float)Math.pow(Math.max(0,-mesh.nx(i)*.31f-mesh.ny(i)*.79f-mesh.nz(i)*.53f),18)*.28f:0;
            GL11.glColor4f(Math.min(1,((color >> 16) & 255) / 255f * light+specular), Math.min(1,((color >> 8) & 255) / 255f * light+specular), Math.min(1,(color & 255) / 255f * light+specular), 1);
            GL11.glNormal3f(mesh.nx(i), mesh.ny(i), mesh.nz(i));
            GL11.glVertex3f(mesh.x(i) / 16f, mesh.y(i) / 16f, mesh.z(i) / 16f);
        }
        GL11.glEnd();
    }

    private static int mix(int a, int b, float f) {
        return ((int)(((a >> 16) & 255) * (1-f) + ((b >> 16) & 255) * f) << 16)
                | ((int)(((a >> 8) & 255) * (1-f) + ((b >> 8) & 255) * f) << 8)
                | (int)((a & 255) * (1-f) + (b & 255) * f);
    }

    private static final class Key {
        final Mesh mesh; final int primary, secondary;
        Key(Mesh mesh, int primary, int secondary) { this.mesh=mesh; this.primary=primary; this.secondary=secondary; }
        @Override public int hashCode() { return System.identityHashCode(mesh) * 31 + primary * 17 + secondary; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Key)) return false;
            Key key=(Key)other; return key.mesh==mesh && key.primary==primary && key.secondary==secondary;
        }
    }
}
