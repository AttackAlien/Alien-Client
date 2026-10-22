package dev.alien.client.world;

/** Cached 2-D triangle fans; rendering expands them into camera-facing world geometry. */
final class ParticleShapes {
    private static final Shape DIAMOND = create(4, false, false);
    private static final Shape PETAL = create(12, true, false);
    private static final Shape SPARK = create(16, false, true);
    private ParticleShapes() { }
    static Shape get(String name) { return "Petal".equals(name) ? PETAL : "Spark".equals(name) ? SPARK : DIAMOND; }

    private static Shape create(int count, boolean petal, boolean star) {
        float[] vertices = new float[count * 6];
        for (int i=0;i<count;i++) {
            int offset=i*6;
            vertices[offset]=0; vertices[offset+1]=0;
            point(vertices,offset+2,i,count,petal,star);
            point(vertices,offset+4,(i+1)%count,count,petal,star);
        }
        return new Shape(vertices);
    }
    private static void point(float[] into,int offset,int index,int count,boolean petal,boolean star) {
        double angle=Math.PI*.5+index*Math.PI*2/count;
        double radius=star && (index&1)!=0 ? .23 : 1;
        double x=Math.cos(angle)*radius, y=Math.sin(angle)*radius;
        if(petal) { x*=.58+.15*y; y*=1.1; x+=.16*(1-y*y); }
        into[offset]=(float)x;into[offset+1]=(float)y;
    }
    static final class Shape {
        private final float[] vertices;
        Shape(float[] vertices) { this.vertices=vertices; }
        int vertexCount() { return vertices.length/2; }
        float u(int i) { return vertices[i*2]; }
        float v(int i) { return vertices[i*2+1]; }
    }
}
