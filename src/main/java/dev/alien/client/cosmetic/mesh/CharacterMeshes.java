package dev.alien.client.cosmetic.mesh;
import static dev.alien.client.cosmetic.mesh.MeshBuilder.*;

/** Original articulated parka character inspired by Kenny. No extracted client model or texture. */
public final class CharacterMeshes {
    public static final Mesh BODY=body(),HOOD=ellipsoid(0,0,0,6.8,6.8,5.8,Mesh.MAIN),FACE=face(),EYES=eyes();
    public static final Mesh UPPER=ellipsoid(0,-1.7,0,2.10,2.7,2,Mesh.MAIN),LOWER=ellipsoid(0,-1.8,0,1.9,2.4,1.85,Mesh.MAIN),MITTEN=ellipsoid(0,-4.1,0,1.85,1.9,1.8,Mesh.ACCENT);
    public static final Mesh LEG=ellipsoid(0,-1.5,0,2.0,2,2.2,Mesh.MAIN),BOOT=ellipsoid(0,-3.15,.65,2.15,1.25,2.8,Mesh.MAIN);
    private CharacterMeshes(){}
    private static Mesh body(){MeshBuilder b=new MeshBuilder();ellipsoid(b,0,10.2,0,5.5,5.7,3.6,Mesh.MAIN);ellipsoid(b,0,10,3.57,.10,4.1,.10,Mesh.SHADOW);ellipsoid(b,0,10.3,3.67,.23,.34,.16,Mesh.ACCENT);return b.build();}
    private static Mesh face(){MeshBuilder b=new MeshBuilder();ellipsoid(b,0,-.35,5.60,4.75,4.65,.85,Mesh.SHADOW);ellipsoid(b,0,-.2,6.22,3.82,3.60,.42,Mesh.ACCENT);return b.build();}
    private static Mesh eyes(){MeshBuilder b=new MeshBuilder();for(int side=-1;side<=1;side+=2){ellipsoid(b,side*1.52,.1,6.58,1.53,1.80,.38,Mesh.MAIN);ellipsoid(b,side*1.07,.08,6.90,.38,.49,.12,Mesh.ACCENT);}return b.build();}
    private static Mesh ellipsoid(double x,double y,double z,double rx,double ry,double rz,int material){MeshBuilder b=new MeshBuilder();ellipsoid(b,x,y,z,rx,ry,rz,material);return b.build();}
    private static void ellipsoid(MeshBuilder b,double x,double y,double z,double rx,double ry,double rz,int material){
        for(int i=0;i<28;i++)for(int j=0;j<18;j++){
            double a=i*Math.PI/14,a1=(i+1)*Math.PI/14,t=-Math.PI/2+j*Math.PI/18,t1=t+Math.PI/18;
            float[] p=point(x,y,z,rx,ry,rz,a,t),q=point(x,y,z,rx,ry,rz,a1,t),r=point(x,y,z,rx,ry,rz,a1,t1),s=point(x,y,z,rx,ry,rz,a,t1);
            b.smoothQuad(p,q,r,s,normal(rx,ry,rz,a,t),normal(rx,ry,rz,a1,t),normal(rx,ry,rz,a1,t1),normal(rx,ry,rz,a,t1),material);
        }
    }
    private static float[] point(double x,double y,double z,double rx,double ry,double rz,double a,double t){return p(x+Math.cos(a)*Math.cos(t)*rx,y+Math.sin(t)*ry,z+Math.sin(a)*Math.cos(t)*rz);}
    private static float[] normal(double rx,double ry,double rz,double a,double t){return normalized(Math.cos(a)*Math.cos(t)/rx,Math.sin(t)/ry,Math.sin(a)*Math.cos(t)/rz);}
}
