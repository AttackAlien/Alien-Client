package dev.alien.client.cosmetic.mesh;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import static dev.alien.client.cosmetic.mesh.MeshBuilder.*;

/**
 * Original curved accessory designs. Built once, shared by all players and suitable
 * for display-list caching. Model units and attachment origins match vanilla's biped.
 */
public final class CosmeticMeshes {
    private static final int SECTORS = 48;
    private static final double TAU = Math.PI * 2;
    private static final Map<String,Mesh> HATS, FACES;
    private static final Mesh WING_LEFT, WING_RIGHT, BACKPACK, CHARM;
    static {
        Map<String,Mesh> hats = new LinkedHashMap<String,Mesh>();
        hats.put("JAPANESE",kasa()); hats.put("SAMURAI",samurai()); hats.put("CROWN",crown());
        HATS=Collections.unmodifiableMap(hats);
        Map<String,Mesh> faces = new LinkedHashMap<String,Mesh>();
        faces.put("KITSUNE",kitsune()); faces.put("FUTURE",visor()); faces.put("GLASSES",glasses());
        FACES=Collections.unmodifiableMap(faces);
        WING_LEFT=wing(1); WING_RIGHT=wing(-1); BACKPACK=backpack(); CHARM=crystalCharm();
    }
    private CosmeticMeshes() { }
    public static Mesh hat(String name) { Mesh mesh=HATS.get(name); return mesh==null?Mesh.EMPTY:mesh; }
    public static Mesh face(String name) { Mesh mesh=FACES.get(name); return mesh==null?Mesh.EMPTY:mesh; }
    public static Mesh back(String name,boolean left) {
        if ("WINGS".equals(name)) return left?WING_LEFT:WING_RIGHT;
        if ("BACKPACK".equals(name)) return BACKPACK;
        return Mesh.EMPTY; // CAPE uses the reusable CapeCloth stream instead.
    }
    public static Mesh charm() { return CHARM; }

    private static Mesh kasa() {
        MeshBuilder b=new MeshBuilder();
        // A five-ring, 48-sector conical straw shell, not stacked square boxes.
        float[][] profile={{.04f,-13.7f},{1.5f,-13.3f},{3.6f,-12.2f},{6.2f,-10.35f},{8.6f,-8.85f}};
        lathe(b,profile,SECTORS,0,TAU,Mesh.MAIN);
        lathe(b,new float[][]{{8.6f,-8.85f},{8.72f,-8.67f},{8.62f,-8.46f}},SECTORS,0,TAU,Mesh.ACCENT);
        // Warmly shaded underside and a circular inner headband.
        lathe(b,new float[][]{{8.62f,-8.46f},{6.0f,-9.80f},{4.3f,-10.15f}},SECTORS,0,TAU,Mesh.SHADOW);
        lathe(b,new float[][]{{4.3f,-10.15f},{4.30f,-8.2f},{4.05f,-8.15f}},SECTORS,0,TAU,Mesh.SHADOW);
        // Narrow radial binding threads follow the cone; 12 accents imply real weave.
        for(int rib=0;rib<12;rib++) {
            double a=rib*TAU/12, d=.008;
            for(int ring=0;ring<profile.length-1;ring++) {
                float[] inner=profile[ring],outer=profile[ring+1];
                b.quad(radial(inner[0],inner[1]-.018,a-d),radial(outer[0],outer[1]-.018,a-d),
                        radial(outer[0],outer[1]-.018,a+d),radial(inner[0],inner[1]-.018,a+d),Mesh.HIGHLIGHT);
            }
        }
        return b.build();
    }

    private static Mesh samurai() {
        MeshBuilder b=new MeshBuilder();
        lathe(b,new float[][]{{.03f,-13.6f},{1.8f,-13.30f},{3.5f,-12.3f},{4.9f,-10.9f},{5.45f,-8.5f}},
                SECTORS,0,TAU,Mesh.MAIN);
        lathe(b,new float[][]{{5.45f,-8.5f},{6.2f,-8.25f},{6.35f,-7.85f}},SECTORS,0,TAU,Mesh.ACCENT);
        // Three overlapping neck-guard arcs leave the face open.
        for(int i=0;i<3;i++) {
            float radius=5.35f+i*.6f,y=-7.9f+i*1.5f;
            lathe(b,new float[][]{{radius,y},{radius+.55f,y+1.25f}},28,-.15,Math.PI+.15,Mesh.MAIN);
            lathe(b,new float[][]{{radius+.55f,y+1.25f},{radius+.6f,y+1.50f}},28,-.15,Math.PI+.15,Mesh.ACCENT);
        }
        for(int side:new int[]{-1,1}) {
            float[][] horn={{side*1.0f,-11.5f,-5.3f},{side*2.6f,-12.2f,-5.5f},
                    {side*4.3f,-14.0f,-5.1f},{side*5.2f,-16.0f,-4.4f},{side*4.9f,-17.2f,-3.8f}};
            tube(b,horn,new float[]{.85f,.78f,.60f,.33f,.025f},10,Mesh.ACCENT);
        }
        // A narrow central crest joins the gold maedate horns.
        ellipsoid(b,0,-11.4f,-5.35f,.75f,1.1f,.35f,12,8,Mesh.HIGHLIGHT);
        return b.build();
    }

    private static Mesh crown() {
        MeshBuilder b=new MeshBuilder();
        final double inner=4.85,outer=5.15;
        for(int i=0;i<SECTORS;i++) {
            double a=i*TAU/SECTORS,c=(i+1)*TAU/SECTORS;
            double ya=-9.8-3.4*Math.pow(Math.max(0,Math.cos(a*6)),4);
            double yc=-9.8-3.4*Math.pow(Math.max(0,Math.cos(c*6)),4);
            b.quad(radial(outer,-8.5,c),radial(outer,yc,c),radial(outer,ya,a),radial(outer,-8.5,a),Mesh.MAIN);
            b.quad(radial(inner,-8.5,a),radial(inner,ya,a),radial(inner,yc,c),radial(inner,-8.5,c),Mesh.SHADOW);
            b.quad(radial(inner,ya,a),radial(outer,ya,a),radial(outer,yc,c),radial(inner,yc,c),Mesh.ACCENT);
        }
        lathe(b,new float[][]{{5.18f,-9.10f},{5.18f,-8.55f}},SECTORS,0,TAU,Mesh.ACCENT);
        for(int i=0;i<6;i++) {
            double a=i*TAU/6;
            ellipsoid(b,(float)Math.cos(a)*5.18f,-9.5f,(float)Math.sin(a)*5.18f,.48f,.67f,.48f,10,6,Mesh.HIGHLIGHT);
        }
        return b.build();
    }

    private static Mesh kitsune() {
        MeshBuilder b=new MeshBuilder();
        int columns=24,rows=18;
        for(int row=0;row<rows;row++) for(int col=0;col<columns;col++) {
            double x0=-4.35+8.7*col/columns,x1=-4.35+8.7*(col+1)/columns;
            double y0=-7.55+6.8*row/rows,y1=-7.55+6.8*(row+1)/rows;
            double x=(x0+x1)*.5,y=(y0+y1)*.5;
            double edge=4.35*(.72+.28*Math.sin((y+7.55)/6.8*Math.PI));
            if(Math.abs(x)>edge || eyeHole(x,y))continue;
            maskQuad(b,x0,y0,x1,y1,Mesh.MAIN);
        }
        // Sculpted muzzle, triangular nose, independent tapered fox ears.
        ellipsoid(b,0,-2.5f,-5.65f,1.30f,1.0f,1.15f,20,10,Mesh.HIGHLIGHT);
        b.triangle(p(-.58,-3.3,-6.72),p(.58,-3.3,-6.72),p(0,-2.65,-6.95),Mesh.SHADOW);
        for(int side:new int[]{-1,1}) {
            float[] lower=p(side*2.05,-7.0,-4.8),tip=p(side*3.75,-11.45,-4.25),outer=p(side*4.4,-6.8,-4.0);
            float[] inner=p(side*3.15,-7.0,-5.0);
            b.triangle(lower,tip,inner,Mesh.MAIN);b.triangle(inner,tip,outer,Mesh.MAIN);
            b.triangle(p(side*2.85,-7.45,-5.02),p(side*3.70,-10.35,-4.65),p(side*3.98,-7.45,-4.52),Mesh.ACCENT);
            eyeOutline(b,side*2.0,-5.15,side);
            // Three cheek slashes curve around each eye rather than hiding it.
            for(int stripe=0;stripe<3;stripe++) {
                double x=side*(2.4+stripe*.35), y=-3.95+stripe*.55;
                b.triangle(p(x,y,maskZ(x,y)-.055),p(x+side*1.1,y-.25,maskZ(x+side*1.1,y-.25)-.055),
                        p(x+side*.72,y+.22,maskZ(x+side*.72,y+.22)-.055),Mesh.ACCENT);
            }
        }
        return b.build();
    }
    private static boolean eyeHole(double x,double y) {
        double center=Math.signum(x)*2;
        double ey=-5.15+(Math.abs(x)-2)*.25;
        return Math.pow((x-center)/1.10,2)+Math.pow((y-ey)/.48,2)<1.10;
    }
    private static double maskZ(double x,double y) {
        return -4.20-1.20*Math.max(0,1-x*x/22)-.18*Math.sin((y+7.5)/7*Math.PI);
    }
    private static void maskQuad(MeshBuilder b,double x0,double y0,double x1,double y1,int mat) {
        b.smoothQuad(p(x0,y0,maskZ(x0,y0)),p(x0,y1,maskZ(x0,y1)),p(x1,y1,maskZ(x1,y1)),p(x1,y0,maskZ(x1,y0)),
                normalized(x0*.109,0,-1),normalized(x0*.109,0,-1),normalized(x1*.109,0,-1),normalized(x1*.109,0,-1),mat);
    }
    private static void eyeOutline(MeshBuilder b,double cx,double cy,int side) {
        for(int i=0;i<24;i++) {
            double a=i*TAU/24,c=(i+1)*TAU/24;
            double x0=cx+Math.cos(a)*1.14,y0=cy+Math.sin(a)*.49+(Math.abs(cx+Math.cos(a)*1.14)-2)*.25;
            double x1=cx+Math.cos(c)*1.14,y1=cy+Math.sin(c)*.49+(Math.abs(cx+Math.cos(c)*1.14)-2)*.25;
            double ox0=cx+Math.cos(a)*1.30,oy0=cy+Math.sin(a)*.66+(Math.abs(cx+Math.cos(a)*1.30)-2)*.25;
            double ox1=cx+Math.cos(c)*1.30,oy1=cy+Math.sin(c)*.66+(Math.abs(cx+Math.cos(c)*1.30)-2)*.25;
            b.quad(p(x0,y0,maskZ(x0,y0)-.045),p(ox0,oy0,maskZ(ox0,oy0)-.045),
                    p(ox1,oy1,maskZ(ox1,oy1)-.045),p(x1,y1,maskZ(x1,y1)-.045),Mesh.ACCENT);
        }
    }

    private static Mesh visor() {
        MeshBuilder b=new MeshBuilder();
        for(int i=0;i<32;i++) {
            double x0=-4.5+i*9.0/32,x1=-4.5+(i+1)*9.0/32;
            double z0=-5.3+.043*x0*x0,z1=-5.3+.043*x1*x1;
            double y0=-6.8+Math.abs(x0)*.13,y1=-6.8+Math.abs(x1)*.13;
            b.smoothQuad(p(x0,y0,z0),p(x0,-3.5,z0),p(x1,-3.5,z1),p(x1,y1,z1),
                    normalized(x0*.086,0,-1),normalized(x0*.086,0,-1),normalized(x1*.086,0,-1),normalized(x1*.086,0,-1),Mesh.SHADOW);
            b.quad(p(x0,y0-.22,z0-.04),p(x0,y0+.1,z0-.04),p(x1,y1+.1,z1-.04),p(x1,y1-.22,z1-.04),Mesh.ACCENT);
            b.quad(p(x0,-4.75,z0-.06),p(x0,-4.58,z0-.06),p(x1,-4.58,z1-.06),p(x1,-4.75,z1-.06),Mesh.HIGHLIGHT);
        }
        for(int side:new int[]{-1,1})tube(b,new float[][]{{side*4.3f,-5.7f,-4.3f},{side*4.65f,-5.5f,-2},{side*4.55f,-5.3f,1}},new float[]{.4f,.35f,.2f},8,Mesh.MAIN);
        return b.build();
    }
    private static Mesh glasses() {
        MeshBuilder b=new MeshBuilder();
        for(int side:new int[]{-1,1}) {
            ellipseRing(b,side*2.10,-5.10,-4.85,1.65,1.2,.22,32,Mesh.MAIN);
            tube(b,new float[][]{{side*3.8f,-5.1f,-4.5f},{side*4.5f,-5.1f,-3},{side*4.5f,-5.1f,1.2f},{side*4.25f,-4.8f,2.1f}},
                    new float[]{.18f,.18f,.16f,.12f},8,Mesh.ACCENT);
        }
        tube(b,new float[][]{{-.50f,-5.2f,-4.9f},{0,-5.48f,-5.1f},{.5f,-5.2f,-4.9f}},new float[]{.17f,.17f,.17f},8,Mesh.ACCENT);
        return b.build();
    }

    private static Mesh wing(int side) {
        MeshBuilder b=new MeshBuilder();
        tube(b,new float[][]{{side*1.3f,2.5f,3.0f},{side*4.2f,.7f,3.1f},{side*8.1f,.15f,3.5f},{side*12.6f,2.6f,3.6f}},
                new float[]{.55f,.60f,.44f,.10f},10,Mesh.ACCENT);
        for(int i=0;i<9;i++) {
            float startX=side*(2.0f+i*1.04f),startY=2.2f-(float)Math.sin(i*.34)*1.5f;
            float endX=side*(6.1f+i*1.12f),endY=9.5f+i*.55f;
            feather(b,startX,startY,3.4f,endX,endY,3.7f,1.0f-i*.025f,i%3==0?Mesh.ACCENT:Mesh.MAIN);
        }
        for(int i=0;i<6;i++) feather(b,side*(1.9f+i*1.15f),2.0f,3.18f,side*(5.6f+i*1.1f),7.2f+i*.2f,3.35f,.75f,Mesh.HIGHLIGHT);
        return b.build();
    }
    private static void feather(MeshBuilder b,float x0,float y0,float z0,float x1,float y1,float z1,float width,int mat) {
        double dx=x1-x0,dy=y1-y0,length=Math.sqrt(dx*dx+dy*dy),px=-dy/length,py=dx/length;
        float[] oldLeft=null,oldRight=null;
        for(int step=0;step<=10;step++) {
            double t=step/10.0,half=width*Math.pow(Math.sin(Math.PI*t),.65);
            double x=x0+dx*t+(x1>0?1:-1)*Math.sin(t*Math.PI)*.65,y=y0+dy*t,z=z0+(z1-z0)*t-Math.sin(t*Math.PI)*.25;
            float[] left=p(x+px*half,y+py*half,z),right=p(x-px*half,y-py*half,z);
            if(oldLeft!=null) {
                float[] n=p(0,0,1);
                b.smoothQuad(oldLeft,oldRight,right,left,n,n,n,n,mat);
                // The reverse surface stays visible without changing global culling state.
                float[] back=p(0,0,-1);
                b.smoothQuad(left,right,oldRight,oldLeft,back,back,back,back,Mesh.SHADOW);
            }
            oldLeft=left;oldRight=right;
        }
    }
    private static Mesh backpack() {
        MeshBuilder b=new MeshBuilder();
        ellipsoid(b,0,6.8f,4.2f,4.25f,5.2f,2.25f,32,18,Mesh.MAIN);
        ellipsoid(b,0,3.9f,6.15f,3.5f,1.9f,.5f,24,12,Mesh.ACCENT);
        ellipseRing(b,0,1.0,4.35,1.25,.85,.20,24,Mesh.ACCENT);
        for(int side:new int[]{-1,1}) {
            float[][] strap=new float[15][];float[] thickness=new float[15];
            for(int i=0;i<strap.length;i++) {
                double a=Math.PI*1.15*i/(strap.length-1);
                strap[i]=p(side*(3.0+.6*Math.sin(a)),4.5+4.8*Math.cos(a),2.9-1.3*Math.sin(a));
                thickness[i]=.27f;
            }
            tube(b,strap,thickness,10,Mesh.SHADOW);
        }
        return b.build();
    }
    private static Mesh crystalCharm() {
        MeshBuilder b=new MeshBuilder();
        ellipseRing(b,0,10.3,0,.72,.88,.17,20,Mesh.ACCENT);
        float[] top=p(0,11.4,0),bottom=p(0,17.0,0);
        for(int i=0;i<8;i++) {
            double a=i*TAU/8,c=(i+1)*TAU/8;
            float[] p0=radial(1.15,13.1,a),p1=radial(1.15,13.1,c);
            b.triangle(top,p0,p1,i%2==0?Mesh.MAIN:Mesh.HIGHLIGHT);
            b.triangle(p0,bottom,p1,i%2==0?Mesh.ACCENT:Mesh.MAIN);
        }
        return b.build();
    }

    private static float[] radial(double radius,double y,double angle) { return p(Math.cos(angle)*radius,y,Math.sin(angle)*radius); }
    private static void lathe(MeshBuilder b,float[][] rings,int sectors,double from,double to,int mat) {
        for(int row=0;row<rings.length-1;row++) {
            double r0=rings[row][0],y0=rings[row][1],r1=rings[row+1][0],y1=rings[row+1][1];
            double dr=r1-r0,dy=y1-y0;
            for(int i=0;i<sectors;i++) {
                double a=from+(to-from)*i/sectors,c=from+(to-from)*(i+1)/sectors;
                float[] na=normalized(dy*Math.cos(a),-dr,dy*Math.sin(a)),nc=normalized(dy*Math.cos(c),-dr,dy*Math.sin(c));
                b.smoothQuad(radial(r0,y0,a),radial(r1,y1,a),radial(r1,y1,c),radial(r0,y0,c),na,na,nc,nc,mat);
            }
        }
    }
    private static void ellipsoid(MeshBuilder b,float cx,float cy,float cz,float rx,float ry,float rz,int sectors,int rows,int mat) {
        for(int row=0;row<rows;row++) for(int i=0;i<sectors;i++) {
            double p0=-Math.PI/2+Math.PI*row/rows,p1=-Math.PI/2+Math.PI*(row+1)/rows;
            double a=i*TAU/sectors,c=(i+1)*TAU/sectors;
            float[] v0=ellipsoidPoint(cx,cy,cz,rx,ry,rz,p0,a),v1=ellipsoidPoint(cx,cy,cz,rx,ry,rz,p1,a);
            float[] v2=ellipsoidPoint(cx,cy,cz,rx,ry,rz,p1,c),v3=ellipsoidPoint(cx,cy,cz,rx,ry,rz,p0,c);
            b.smoothQuad(v0,v1,v2,v3,ellipsoidNormal(v0,cx,cy,cz,rx,ry,rz),ellipsoidNormal(v1,cx,cy,cz,rx,ry,rz),
                    ellipsoidNormal(v2,cx,cy,cz,rx,ry,rz),ellipsoidNormal(v3,cx,cy,cz,rx,ry,rz),mat);
        }
    }
    private static float[] ellipsoidPoint(float cx,float cy,float cz,float rx,float ry,float rz,double latitude,double longitude) {
        return p(cx+rx*Math.cos(latitude)*Math.cos(longitude),cy+ry*Math.sin(latitude),cz+rz*Math.cos(latitude)*Math.sin(longitude));
    }
    private static float[] ellipsoidNormal(float[] v,float cx,float cy,float cz,float rx,float ry,float rz) {
        return normalized((v[0]-cx)/(rx*rx),(v[1]-cy)/(ry*ry),(v[2]-cz)/(rz*rz));
    }
    private static void tube(MeshBuilder b,float[][] centers,float[] radii,int sides,int mat) {
        float[][][] rings=tubeRings(centers,radii,sides);
        for(int row=0;row<centers.length-1;row++) {
            float[][] first=rings[row],second=rings[row+1];
            for(int i=0;i<sides;i++) {
                int j=(i+1)%sides;
                float[] na=normalized(first[i][0]-centers[row][0],first[i][1]-centers[row][1],first[i][2]-centers[row][2]);
                float[] nb=normalized(second[i][0]-centers[row+1][0],second[i][1]-centers[row+1][1],second[i][2]-centers[row+1][2]);
                float[] nc=normalized(second[j][0]-centers[row+1][0],second[j][1]-centers[row+1][1],second[j][2]-centers[row+1][2]);
                float[] nd=normalized(first[j][0]-centers[row][0],first[j][1]-centers[row][1],first[j][2]-centers[row][2]);
                b.smoothQuad(first[j],second[j],second[i],first[i],nd,nc,nb,na,mat);
            }
        }
        float[][] start=rings[0],end=rings[centers.length-1];
        for(int i=0;i<sides;i++) { int j=(i+1)%sides;b.triangle(centers[0],start[j],start[i],mat);b.triangle(centers[centers.length-1],end[i],end[j],mat); }
    }
    private static float[][][] tubeRings(float[][] centers,float[] radii,int sides) {
        float[][][] result=new float[centers.length][sides][];
        float[] previous=null;
        for(int at=0;at<centers.length;at++) {
            int before=Math.max(0,at-1),after=Math.min(centers.length-1,at+1);
            float[] tangent=normalized(centers[after][0]-centers[before][0],centers[after][1]-centers[before][1],centers[after][2]-centers[before][2]);
            float[] perpendicular=null;
            if(previous!=null) {
                double dot=previous[0]*tangent[0]+previous[1]*tangent[1]+previous[2]*tangent[2];
                double x=previous[0]-dot*tangent[0],y=previous[1]-dot*tangent[1],z=previous[2]-dot*tangent[2];
                if(x*x+y*y+z*z>1e-8)perpendicular=normalized(x,y,z);
            }
            if(perpendicular==null)perpendicular=Math.abs(tangent[1])<.9?normalized(-tangent[2],0,tangent[0]):normalized(tangent[1],-tangent[0],0);
            float[] second=normalized(tangent[1]*perpendicular[2]-tangent[2]*perpendicular[1],tangent[2]*perpendicular[0]-tangent[0]*perpendicular[2],tangent[0]*perpendicular[1]-tangent[1]*perpendicular[0]);
            for(int i=0;i<sides;i++) {
                double a=i*TAU/sides,ca=Math.cos(a)*radii[at],sa=Math.sin(a)*radii[at];
                result[at][i]=p(centers[at][0]+perpendicular[0]*ca+second[0]*sa,centers[at][1]+perpendicular[1]*ca+second[1]*sa,centers[at][2]+perpendicular[2]*ca+second[2]*sa);
            }
            previous=perpendicular;
        }
        return result;
    }
    private static void ellipseRing(MeshBuilder b,double cx,double cy,double cz,double rx,double ry,double thickness,int segments,int mat) {
        float[][] centers=new float[segments+1][];float[] radii=new float[segments+1];
        for(int i=0;i<=segments;i++) { double a=i*TAU/segments;centers[i]=p(cx+Math.cos(a)*rx,cy+Math.sin(a)*ry,cz);radii[i]=(float)thickness; }
        tube(b,centers,radii,8,mat);
    }
}
