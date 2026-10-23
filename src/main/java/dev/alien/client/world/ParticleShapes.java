package dev.alien.client.world;

/** Cached feathered triangle meshes: center facets, inner silhouette, transparent outer fringe. */
final class ParticleShapes {
    private static final Shape DIAMOND=create(8,"Diamond");
    private static final Shape PETAL=create(24,"Petal");
    private static final Shape SPARK=create(12,"Spark");
    private static final Shape ORB=create(24,"Orb");
    private static final Shape GLOW=create(16,"Orb");
    private ParticleShapes() { }
    static Shape get(String name){return "Petal".equals(name)?PETAL:"Spark".equals(name)?SPARK:"Orb".equals(name)?ORB:DIAMOND;}
    static Shape glow(){return GLOW;}
    private static Shape create(int count,String type){
        float[] v=new float[count*9*4];int at=0;
        for(int i=0;i<count;i++){
            double[] a=point(i,count,type),b=point((i+1)%count,count,type);
            at=vertex(v,at,0,0,1,1);at=vertex(v,at,a[0]*.78,a[1]*.78,.88f,.35f);at=vertex(v,at,b[0]*.78,b[1]*.78,.88f,.35f);
            at=vertex(v,at,a[0]*.78,a[1]*.78,.88f,.35f);at=vertex(v,at,a[0],a[1],0,0);at=vertex(v,at,b[0],b[1],0,0);
            at=vertex(v,at,a[0]*.78,a[1]*.78,.88f,.35f);at=vertex(v,at,b[0],b[1],0,0);at=vertex(v,at,b[0]*.78,b[1]*.78,.88f,.35f);
        }
        return new Shape(v);
    }
    private static int vertex(float[] into,int at,double x,double y,float alpha,float shade){into[at++]=(float)x;into[at++]=(float)y;into[at++]=alpha;into[at++]=shade;return at;}
    private static double[] point(int i,int n,String type){
        double angle=Math.PI*.5+i*Math.PI*2/n;
        double r="Spark".equals(type)?(i%3==0?1:.16):"Diamond".equals(type)?((i&1)==0?1:.60):1;
        double x=Math.cos(angle)*r,y=Math.sin(angle)*r;
        if("Diamond".equals(type)){x*=.72;y*=1.1;}
        if("Petal".equals(type)){x*=.56+.20*y;x+=.15*(1-y*y);y*=1.08;}
        return new double[]{x,y};
    }
    static final class Shape {
        private final float[] data;
        Shape(float[] data){this.data=data;}
        int vertexCount(){return data.length/4;}
        float u(int i){return data[i*4];}float v(int i){return data[i*4+1];}
        float alpha(int i){return data[i*4+2];}float shade(int i){return data[i*4+3];}
    }
}
