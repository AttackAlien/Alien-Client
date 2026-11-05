package dev.alien.client.cosmetic.mesh;

/** Original smooth parts, built once in model pixels. No geometry is allocated by animation. */
public final class PetMeshes {
    public static final Mesh HEAD=ellipsoid(2.95,2.85,2.25,24,16,true,Mesh.MAIN),
        BODY=ellipsoid(1.65,2.1,1.3,16,10,false,Mesh.MAIN),
        ARM=ellipsoid(.6,1.25,.65,10,6,false,Mesh.MAIN),
        FOOT=ellipsoid(.7,.8,.95,10,6,false,Mesh.MAIN),
        EYE=ellipsoid(.78,1.14,.27,12,8,false,Mesh.ACCENT),
        DOT=ellipsoid(.3,.3,.25,6,4,false,Mesh.MAIN),
        STEM=ellipsoid(.18,.85,.18,8,6,false,Mesh.MAIN),
        TIP=ellipsoid(.42,.42,.42,10,6,false,Mesh.ACCENT),
        COLLAR=torus(1.15,.9,.11,24),RING=torus(2.45,2.45,.065,32),SMILE=smile();
    private PetMeshes(){}
    private static Mesh ellipsoid(double rx,double ry,double rz,int columns,int rows,boolean alien,int material){
        MeshBuilder b=new MeshBuilder();
        for(int y=0;y<rows;y++)for(int x=0;x<columns;x++){
            double a=x*Math.PI*2/columns,c=(x+1)*Math.PI*2/columns,
                v=-Math.PI/2+y*Math.PI/rows,w=-Math.PI/2+(y+1)*Math.PI/rows;
            float[] p=point(rx,ry,rz,a,v,alien),q=point(rx,ry,rz,c,v,alien),
                r=point(rx,ry,rz,c,w,alien),s=point(rx,ry,rz,a,w,alien);
            b.smoothQuad(p,s,r,q,normal(p,rx,ry,rz,alien),normal(s,rx,ry,rz,alien),
                normal(r,rx,ry,rz,alien),normal(q,rx,ry,rz,alien),material);
        }
        return b.build();
    }
    private static float[] point(double rx,double ry,double rz,double u,double v,boolean alien){
        double factor=alien?.86+.14*Math.sin(v):1;
        return MeshBuilder.p(Math.cos(u)*Math.cos(v)*rx*factor,Math.sin(v)*ry,Math.sin(u)*Math.cos(v)*rz);
    }
    private static float[] normal(float[] p,double x,double y,double z,boolean alien){
        // The tapered head needs the gradient of its actual surface, including f'(y).
        double f=alien?.86+.14*p[1]/y:1;
        return MeshBuilder.normalized(p[0]/(x*x*f*f),p[1]/(y*y)-(alien?p[0]*p[0]*.14/(x*x*f*f*f*y):0),p[2]/(z*z));
    }
    private static Mesh torus(double rx,double rz,double tube,int segments){
        MeshBuilder b=new MeshBuilder();
        for(int i=0;i<segments;i++)for(int j=0;j<4;j++){
            double a=i*Math.PI*2/segments,c=(i+1)*Math.PI*2/segments,v=j*Math.PI/2,w=(j+1)*Math.PI/2;
            b.smoothQuad(ringPoint(rx,rz,tube,a,v),ringPoint(rx,rz,tube,a,w),ringPoint(rx,rz,tube,c,w),ringPoint(rx,rz,tube,c,v),
                ringNormal(a,v),ringNormal(a,w),ringNormal(c,w),ringNormal(c,v),Mesh.ACCENT);
        }
        return b.build();
    }
    private static float[] ringPoint(double rx,double rz,double tube,double a,double v){return MeshBuilder.p(Math.cos(a)*(rx+tube*Math.cos(v)),tube*Math.sin(v),Math.sin(a)*(rz+tube*Math.cos(v)));}
    private static float[] ringNormal(double a,double v){return MeshBuilder.p(Math.cos(a)*Math.cos(v),Math.sin(v),Math.sin(a)*Math.cos(v));}
    private static Mesh smile(){
        MeshBuilder b=new MeshBuilder();
        for(int i=0;i<10;i++)for(int j=0;j<4;j++){
            double a=-1+i*.2,c=a+.2,v=j*Math.PI/2,w=(j+1)*Math.PI/2;
            b.smoothQuad(smilePoint(a,v),smilePoint(a,w),smilePoint(c,w),smilePoint(c,v),
                smileNormal(a,v),smileNormal(a,w),smileNormal(c,w),smileNormal(c,v),Mesh.MAIN);
        }
        return b.build();
    }
    private static float[] smilePoint(double t,double v){float[] n=smileNormal(t,v);return MeshBuilder.p(t*.42+n[0]*.05,-1.40+.12*t*t+n[1]*.05,1.99+n[2]*.05);}
    private static float[] smileNormal(double t,double v){double length=Math.hypot(.42,.24*t);return MeshBuilder.p(-.24*t/length*Math.cos(v),.42/length*Math.cos(v),Math.sin(v));}
}
