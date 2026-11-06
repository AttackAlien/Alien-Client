package dev.alien.client.cosmetic.mesh;

import static dev.alien.client.cosmetic.mesh.MeshBuilder.*;
import static dev.alien.client.cosmetic.mesh.CosmeticMeshes.tube;

/** Small cached procedural add-ons fitted to vanilla's head/body/limb pivots. */
public final class AccessoryPartMeshes {
    private static final Mesh HORN_CLASSIC = horns(false), HORN_CRESCENT = horns(true);
    private static final Mesh MASK_ONI = mask(false), MASK_TECH = mask(true);
    private static final Mesh SHOULDER_DRAGON = shoulders(false), SHOULDER_KNIGHT = shoulders(true);
    private static final Mesh TAIL_SPADE = tail(false), TAIL_CRYSTAL = tail(true);
    private static final Mesh ARM_BRACER = guard(false, false), ARM_GAUNTLET = guard(false, true);
    private static final Mesh LEG_GREAVE = guard(true, false), LEG_BOOT = guard(true, true);

    private AccessoryPartMeshes() { }

    public static Mesh get(String slot, String style) {
        if ("HORNS".equals(slot)) return "Crescent".equals(style) ? HORN_CRESCENT : HORN_CLASSIC;
        if ("MASK".equals(slot)) return "Tech".equals(style) ? MASK_TECH : MASK_ONI;
        if ("SHOULDERS".equals(slot)) return "Knight".equals(style) ? SHOULDER_KNIGHT : SHOULDER_DRAGON;
        if ("TAIL".equals(slot)) return "Crystal".equals(style) ? TAIL_CRYSTAL : TAIL_SPADE;
        if ("ARMS".equals(slot)) return "Gauntlets".equals(style) ? ARM_GAUNTLET : ARM_BRACER;
        if ("LEGS".equals(slot)) return "Boots".equals(style) ? LEG_BOOT : LEG_GREAVE;
        return Mesh.EMPTY;
    }

    private static Mesh horns(boolean crescent) {
        MeshBuilder b = new MeshBuilder();
        for (int side : new int[] {-1, 1}) {
            float[][] points = crescent
                    ? new float[][] {{side*3.0f,-6.0f,-.2f},{side*4.3f,-8.5f,-.3f},{side*4.5f,-11.1f,-.4f},{side*3.2f,-13.0f,-.2f}}
                    : new float[][] {{side*2.8f,-6.0f,-.2f},{side*4.0f,-8.2f,-.2f},{side*5.3f,-10.8f,.1f},{side*6.2f,-12.4f,.5f}};
            tube(b, points, new float[] {.95f,.8f,.42f,.025f}, 12, Mesh.ACCENT);
            ellipsoid(b, side*2.8f,-6,-.2f,1.05f,.82f,.72f,16,8,Mesh.MAIN);
        }
        return b.build();
    }

    private static Mesh mask(boolean tech) {
        MeshBuilder b = new MeshBuilder();
        // Curved cheek plates follow the front of the face while leaving eyes visible.
        for (int side : new int[] {-1,1}) {
            ellipsoid(b, side*2.85f,-3.5f,-7.0f,1.5f,2.0f,.46f,20,12,Mesh.MAIN);
            ellipsoid(b, side*2.85f,-3.65f,-7.4f,.62f,.58f,.15f,16,10,Mesh.SHADOW);
            if (tech) {
                tube(b,new float[][] {{side*1.25f,-5.5f,-7.55f},{side*2.35f,-5.8f,-7.75f},{side*3.65f,-5.25f,-7.55f}},
                        new float[] {.15f,.19f,.08f},8,Mesh.ACCENT);
                ellipsoid(b,side*4.05f,-2.6f,-7.05f,.18f,.18f,.13f,10,6,Mesh.HIGHLIGHT);
            } else {
                b.triangle(p(side*1.45,-1.7,-7.45),p(side*3.2,-2.0,-7.4),p(side*2.5,-1.15,-7.4),Mesh.ACCENT);
            }
        }
        ellipsoid(b,0,-5.9f,-7.1f,1.25f,.5f,.42f,18,10,Mesh.MAIN);
        return b.build();
    }

    private static Mesh shoulders(boolean knight) {
        MeshBuilder b = new MeshBuilder();
        ellipsoid(b,0,-.6f,0,4.15f,2.2f,3.5f,24,12,Mesh.MAIN);
        ellipsoid(b,0,-1.55f,-.25f,3.15f,1.15f,2.9f,20,10,Mesh.ACCENT);
        if (knight) {
            for (int side : new int[] {-1,1}) {
                b.triangle(p(side*1.4,-2.5,-2.2),p(side*3.5,-1.2,-2.7),p(side*3.7,1.2,-1.5),Mesh.HIGHLIGHT);
                ellipsoid(b,side*3.45f,.7f,.1f,.65f,.75f,.65f,12,8,Mesh.SHADOW);
            }
        } else {
            for (int side : new int[] {-1,1}) {
                float[][] ridge={{side*1.0f,-.6f,-3.0f},{side*2.6f,.2f,-2.1f},{side*3.8f,.7f,-.2f}};
                tube(b,ridge,new float[] {.23f,.20f,.09f},8,Mesh.HIGHLIGHT);
            }
        }
        return b.build();
    }

    private static Mesh tail(boolean crystal) {
        MeshBuilder b = new MeshBuilder();
        float[][] curve={{0,-1,2.0f},{0,-2,4.2f},{0,-4.3f,6.5f},{0,-5.0f,9.0f},{0,-4.1f,11.3f}};
        tube(b,curve,new float[] {1.1f,.92f,.7f,.48f,.12f},12,Mesh.MAIN);
        if (crystal) {
            ellipsoid(b,0,-4.1f,11.1f,.95f,.9f,1.15f,16,10,Mesh.ACCENT);
            b.triangle(p(0,-2.9,11.0),p(0,-4.1,13.4),p(0,-5.3,11.0),Mesh.HIGHLIGHT);
            b.triangle(p(-1.0,-4.1,11.0),p(0,-4.1,13.4),p(1.0,-4.1,11.0),Mesh.ACCENT);
        } else {
            ellipsoid(b,0,-3.7f,11.2f,.72f,.8f,.92f,16,10,Mesh.ACCENT);
            b.triangle(p(0,-2.4,10.7),p(-1.7,-3.6,11.0),p(0,-4.8,10.7),Mesh.HIGHLIGHT);
            b.triangle(p(0,-2.4,10.7),p(1.7,-3.6,11.0),p(0,-4.8,10.7),Mesh.HIGHLIGHT);
        }
        return b.build();
    }

    private static Mesh guard(boolean leg, boolean heavy) {
        MeshBuilder b = new MeshBuilder();
        float y = leg ? -5.1f : -5.0f;
        ellipsoid(b,0,y,-.15f,heavy?3.0f:2.55f,heavy?4.7f:4.1f,2.6f,20,12,Mesh.MAIN);
        ellipsoid(b,0,y-1.1f,-2.2f,heavy?2.15f:1.5f,heavy?2.8f:2.2f,.42f,16,10,Mesh.ACCENT);
        tube(b,new float[][] {{0,y-3.2f,-2.55f},{0,y,-2.82f},{0,y+3.1f,-2.35f}},
                new float[] {.12f,heavy?.36f:.23f,.10f},8,Mesh.HIGHLIGHT);
        if (heavy) for (int side : new int[] {-1,1})
            ellipsoid(b,side*2.55f,y,-.1f,.42f,1.25f,1.65f,10,8,Mesh.SHADOW);
        return b.build();
    }

    private static void ellipsoid(MeshBuilder b,float cx,float cy,float cz,float rx,float ry,float rz,int sectors,int rows,int material) {
        for(int row=0;row<rows;row++) for(int i=0;i<sectors;i++) {
            double p0=-Math.PI/2+Math.PI*row/rows,p1=-Math.PI/2+Math.PI*(row+1)/rows;
            double a=i*Math.PI*2/sectors,c=(i+1)*Math.PI*2/sectors;
            float[] v0=point(cx,cy,cz,rx,ry,rz,p0,a),v1=point(cx,cy,cz,rx,ry,rz,p1,a);
            float[] v2=point(cx,cy,cz,rx,ry,rz,p1,c),v3=point(cx,cy,cz,rx,ry,rz,p0,c);
            b.smoothQuad(v0,v1,v2,v3,norm(v0,cx,cy,cz,rx,ry,rz),norm(v1,cx,cy,cz,rx,ry,rz),
                    norm(v2,cx,cy,cz,rx,ry,rz),norm(v3,cx,cy,cz,rx,ry,rz),material);
        }
    }
    private static float[] point(float cx,float cy,float cz,float rx,float ry,float rz,double lat,double lon) {
        return p(cx+rx*Math.cos(lat)*Math.cos(lon),cy+ry*Math.sin(lat),cz+rz*Math.cos(lat)*Math.sin(lon));
    }
    private static float[] norm(float[] v,float cx,float cy,float cz,float rx,float ry,float rz) {
        return normalized((v[0]-cx)/(rx*rx),(v[1]-cy)/(ry*ry),(v[2]-cz)/(rz*rz));
    }
}
