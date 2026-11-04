package dev.alien.client.cosmetic.mesh;

/** Original rounded alien parts, built once in model pixels and shared by every local mascot. */
public final class PetMeshes {
    public static final Mesh HEAD=ellipsoid(2.9,3,2.2,16,10,true),BODY=ellipsoid(1.65,2.1,1.3,14,8,false),
        ARM=ellipsoid(.6,1.25,.65,10,6,false),FOOT=ellipsoid(.7,.8,.95,10,6,false),
        EYE=ellipsoid(.78,1.14,.27,12,8,false),DOT=ellipsoid(.3,.3,.25,8,6,false),
        STEM=ellipsoid(.18,.85,.18,8,6,false),TIP=ellipsoid(.42,.42,.42,10,6,false),RING=ring();
    private PetMeshes(){}
    private static Mesh ellipsoid(double rx,double ry,double rz,int columns,int rows,boolean alien){MeshBuilder b=new MeshBuilder();
        for(int y=0;y<rows;y++)for(int x=0;x<columns;x++){
            double a=x*Math.PI*2/columns,c=(x+1)*Math.PI*2/columns,v=-Math.PI/2+y*Math.PI/rows,w=-Math.PI/2+(y+1)*Math.PI/rows;
            float[] p=point(rx,ry,rz,a,v,alien),q=point(rx,ry,rz,c,v,alien),r=point(rx,ry,rz,c,w,alien),s=point(rx,ry,rz,a,w,alien);
            b.smoothQuad(p,q,r,s,normal(p,rx,ry,rz),normal(q,rx,ry,rz),normal(r,rx,ry,rz),normal(s,rx,ry,rz),Mesh.MAIN);
        }return b.build();}
    private static float[] point(double rx,double ry,double rz,double u,double v,boolean alien){double factor=alien?.68+.32*(Math.sin(v)+1)/2:1;return MeshBuilder.p(Math.cos(u)*Math.cos(v)*rx*factor,Math.sin(v)*ry,Math.sin(u)*Math.cos(v)*rz);}
    private static float[] normal(float[] p,double x,double y,double z){return MeshBuilder.normalized(p[0]/(x*x),p[1]/(y*y),p[2]/(z*z));}
    private static Mesh ring(){MeshBuilder b=new MeshBuilder();for(int i=0;i<32;i++){double a=i*Math.PI/16,c=(i+1)*Math.PI/16;b.quad(MeshBuilder.p(Math.cos(a)*2.6,0,Math.sin(a)*2.6),MeshBuilder.p(Math.cos(c)*2.6,0,Math.sin(c)*2.6),MeshBuilder.p(Math.cos(c)*2.35,0,Math.sin(c)*2.35),MeshBuilder.p(Math.cos(a)*2.35,0,Math.sin(a)*2.35),Mesh.MAIN);}return b.build();}
}
