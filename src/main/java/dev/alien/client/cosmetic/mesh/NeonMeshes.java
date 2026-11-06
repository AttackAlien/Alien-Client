package dev.alien.client.cosmetic.mesh;

import static dev.alien.client.cosmetic.mesh.MeshBuilder.*;

/** Original dragon membranes, rounded bone sections and halo. No third-party client assets. */
public final class NeonMeshes {
    public static final Mesh HALO=halo();
    private static final Mesh LEFT_ROOT=dragon(1,false),RIGHT_ROOT=mirror(LEFT_ROOT),LEFT_TIP=dragon(1,true),RIGHT_TIP=mirror(LEFT_TIP);
    private NeonMeshes(){}
    public static Mesh dragon(boolean left,boolean tip){return tip?(left?LEFT_TIP:RIGHT_TIP):(left?LEFT_ROOT:RIGHT_ROOT);}
    private static Mesh mirror(Mesh source){
        int count=source.vertexCount();float[] points=new float[count*3],normals=new float[count*3];byte[] materials=new byte[count];
        for(int i=0;i<count;i++){int v=i%3==0?i:i%3==1?i+1:i-1,n=i*3;points[n]=-source.x(v);points[n+1]=source.y(v);points[n+2]=source.z(v);normals[n]=-source.nx(v);normals[n+1]=source.ny(v);normals[n+2]=source.nz(v);materials[i]=(byte)source.material(v);}
        return new Mesh(points,normals,materials);
    }
    private static Mesh halo(){
        MeshBuilder b=new MeshBuilder();
        for(int i=0;i<64;i++)for(int j=0;j<8;j++){
            double a=i*Math.PI/32,a1=(i+1)*Math.PI/32,c=j*Math.PI/4,c1=(j+1)*Math.PI/4;
            b.smoothQuad(hoop(a,c),hoop(a1,c),hoop(a1,c1),hoop(a,c1),hoopNormal(a,c),hoopNormal(a1,c),hoopNormal(a1,c1),hoopNormal(a,c1),Mesh.ACCENT);
        }
        for(int i=0;i<4;i++){double a=i*Math.PI/2;ball(b,Math.cos(a)*5.6,-11.4,Math.sin(a)*5.6,.29,Mesh.HIGHLIGHT);}
        return b.build();
    }
    private static float[] hoop(double a,double t){double r=5.6+Math.cos(t)*.11;return p(Math.cos(a)*r,-11.4+Math.sin(t)*.11,Math.sin(a)*r);}
    private static float[] hoopNormal(double a,double t){return p(Math.cos(a)*Math.cos(t),Math.sin(t),Math.sin(a)*Math.cos(t));}
    private static Mesh dragon(int side,boolean tip){
        MeshBuilder b=new MeshBuilder();float[] hinge=p(side*7.5,-2.8,3.8);
        if(!tip){tube(b,new float[][]{p(side*1.3,2.7,3),p(side*4.2,.3,3.3),hinge},.38,.24,Mesh.ACCENT);ball(b,hinge[0],hinge[1],hinge[2],.49,Mesh.HIGHLIGHT);return b.build();}
        float[][] tips={p(side*14.5,-7,4.3),p(side*21.2,-1.8,4.7),p(side*18.4,6.8,4.9),p(side*10.5,12.4,4.0),p(side*4.5,10.6,3.6)};
        for(int finger=0;finger<tips.length;finger++){
            float[] end=tips[finger],mid=p(hinge[0]*.48+end[0]*.52,(hinge[1]+end[1])*.5-.8,(hinge[2]+end[2])*.5+.15);
            tube(b,new float[][]{hinge,mid,end},finger==0?.25:.15,.045,Mesh.ACCENT);ball(b,end[0],end[1],end[2],.16,Mesh.HIGHLIGHT);
            if(finger==tips.length-1)continue;
            float[] next=tips[finger+1];
            for(int arc=0;arc<20;arc++)for(int row=0;row<10;row++){
                double u=arc/20.0,u1=(arc+1)/20.0,r=row/10.0,r1=(row+1)/10.0;
                float[] a=web(hinge,end,next,u,r),c=web(hinge,end,next,u1,r1),d=web(hinge,end,next,u,r1),v=web(hinge,end,next,u1,r);
                float[] na=webNormal(hinge,end,next,u,r),nv=webNormal(hinge,end,next,u1,r),nc=webNormal(hinge,end,next,u1,r1),nd=webNormal(hinge,end,next,u,r1);
                b.smoothQuad(a,v,c,d,na,nv,nc,nd,Mesh.MAIN);
            }
            float[][] edge=new float[21][];for(int arc=0;arc<=20;arc++)edge[arc]=web(hinge,end,next,arc/20.0,1);tube(b,edge,.075,.075,Mesh.HIGHLIGHT);
        }
        return b.build();
    }
    private static float[] web(float[] h,float[] a,float[] b,double u,double r){
        double inset=Math.sin(Math.PI*u)*.24,edgeX=a[0]*(1-u)+b[0]*u,edgeY=a[1]*(1-u)+b[1]*u,edgeZ=a[2]*(1-u)+b[2]*u;
        edgeX+=(h[0]-edgeX)*inset;edgeY+=(h[1]-edgeY)*inset;
        return p(h[0]+(edgeX-h[0])*r,h[1]+(edgeY-h[1])*r,h[2]+(edgeZ-h[2])*r+Math.sin(r*Math.PI)*Math.sin(u*Math.PI)*.85);
    }
    private static float[] webNormal(float[] h,float[] a,float[] b,double u,double r){
        float[] left=web(h,a,b,Math.max(0,u-.001),r),right=web(h,a,b,Math.min(1,u+.001),r),low=web(h,a,b,u,Math.max(.001,r-.001)),high=web(h,a,b,u,Math.min(1,r+.001));
        double ux=right[0]-left[0],uy=right[1]-left[1],uz=right[2]-left[2],vx=high[0]-low[0],vy=high[1]-low[1],vz=high[2]-low[2];return normalized(uy*vz-uz*vy,uz*vx-ux*vz,ux*vy-uy*vx);
    }
    private static void tube(MeshBuilder b,float[][] path,double start,double end,int material){
        for(int i=0;i<path.length-1;i++)for(int j=0;j<10;j++){
            float[] a=path[i],z=path[i+1];double dx=z[0]-a[0],dy=z[1]-a[1],length=Math.hypot(dx,dy);if(length<1e-5)continue;double nx=-dy/length,ny=dx/length,r=start+(end-start)*i/(path.length-1),r1=start+(end-start)*(i+1)/(path.length-1),t=j*Math.PI/5,t1=(j+1)*Math.PI/5;
            float[] n=p(nx*Math.cos(t),ny*Math.cos(t),Math.sin(t)),n1=p(nx*Math.cos(t1),ny*Math.cos(t1),Math.sin(t1));
            b.smoothQuad(p(a[0]+n[0]*r,a[1]+n[1]*r,a[2]+n[2]*r),p(z[0]+n[0]*r1,z[1]+n[1]*r1,z[2]+n[2]*r1),p(z[0]+n1[0]*r1,z[1]+n1[1]*r1,z[2]+n1[2]*r1),p(a[0]+n1[0]*r,a[1]+n1[1]*r,a[2]+n1[2]*r),n,n,n1,n1,material);
        }
    }
    private static void ball(MeshBuilder b,double x,double y,double z,double radius,int material){for(int i=0;i<12;i++)for(int j=0;j<6;j++){double a=i*Math.PI/6,a1=(i+1)*Math.PI/6,t=-Math.PI/2+j*Math.PI/6,t1=t+Math.PI/6;float[] n=sphere(a,t),n1=sphere(a1,t),n2=sphere(a1,t1),n3=sphere(a,t1);b.smoothQuad(at(x,y,z,radius,n),at(x,y,z,radius,n1),at(x,y,z,radius,n2),at(x,y,z,radius,n3),n,n1,n2,n3,material);}}
    private static float[] sphere(double a,double t){return p(Math.cos(a)*Math.cos(t),Math.sin(t),Math.sin(a)*Math.cos(t));}
    private static float[] at(double x,double y,double z,double r,float[] n){return p(x+n[0]*r,y+n[1]*r,z+n[2]*r);}
}
