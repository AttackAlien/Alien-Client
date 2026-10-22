package dev.alien.client.cosmetic.mesh;

import java.util.Arrays;

/** Package-private construction helpers; used once for the cached original models. */
final class MeshBuilder {
    private float[] positions = new float[2048], normals = new float[2048];
    private byte[] materials = new byte[683];
    private int count;
    void triangle(float[] a, float[] b, float[] c, int material) {
        if (!hasArea(a,b,c)) return;
        float[] normal = normal(a, b, c);
        vertex(a, normal, material); vertex(b, normal, material); vertex(c, normal, material);
    }
    void smoothTriangle(float[] a, float[] b, float[] c, float[] na, float[] nb, float[] nc, int material) {
        if (!hasArea(a,b,c)) return;
        vertex(a, na, material); vertex(b, nb, material); vertex(c, nc, material);
    }
    void quad(float[] a, float[] b, float[] c, float[] d, int material) {
        triangle(a,b,c,material); triangle(a,c,d,material);
    }
    void smoothQuad(float[] a, float[] b, float[] c, float[] d, float[] na, float[] nb, float[] nc, float[] nd, int material) {
        smoothTriangle(a,b,c,na,nb,nc,material); smoothTriangle(a,c,d,na,nc,nd,material);
    }
    private void vertex(float[] p, float[] normal, int material) {
        if ((count + 1) * 3 > positions.length) {
            positions = Arrays.copyOf(positions, positions.length * 2);
            normals = Arrays.copyOf(normals, normals.length * 2);
        }
        if (count == materials.length) materials = Arrays.copyOf(materials, materials.length * 2);
        int offset = count * 3;
        System.arraycopy(p,0,positions,offset,3); System.arraycopy(normal,0,normals,offset,3);
        materials[count++] = (byte)material;
    }
    Mesh build() { return new Mesh(Arrays.copyOf(positions,count*3),Arrays.copyOf(normals,count*3),Arrays.copyOf(materials,count)); }
    static float[] p(double x,double y,double z) { return new float[]{(float)x,(float)y,(float)z}; }
    static float[] normalized(double x,double y,double z) {
        double length = Math.sqrt(x*x+y*y+z*z);
        return length < 1e-8 ? p(0,-1,0) : p(x/length,y/length,z/length);
    }
    static float[] normal(float[] a,float[] b,float[] c) {
        double ux=b[0]-a[0],uy=b[1]-a[1],uz=b[2]-a[2],vx=c[0]-a[0],vy=c[1]-a[1],vz=c[2]-a[2];
        return normalized(uy*vz-uz*vy,uz*vx-ux*vz,ux*vy-uy*vx);
    }
    private static boolean hasArea(float[] a,float[] b,float[] c) {
        double ux=b[0]-a[0],uy=b[1]-a[1],uz=b[2]-a[2],vx=c[0]-a[0],vy=c[1]-a[1],vz=c[2]-a[2];
        double x=uy*vz-uz*vy,y=uz*vx-ux*vz,z=ux*vy-uy*vx;
        return x*x+y*y+z*z>1e-10;
    }
}
