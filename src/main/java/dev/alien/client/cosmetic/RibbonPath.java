package dev.alien.client.cosmetic;

/** Bounded world-space history. Teleports and invalid coordinates never form a ribbon. */
public final class RibbonPath {
    public static final int CAPACITY=48;
    private final double[] x=new double[CAPACITY],y=new double[CAPACITY],z=new double[CAPACITY];
    private final long[] born=new long[CAPACITY];
    private final double[] distance=new double[CAPACITY];
    private int size;
    public int size(){return size;}
    public void clear(){size=0;}
    public void expire(long now,int lifetime){while(size>0&&now-born[0]>=lifetime)removeFirst();}
    private void removeFirst(){--size;System.arraycopy(x,1,x,0,size);System.arraycopy(y,1,y,0,size);System.arraycopy(z,1,z,0,size);System.arraycopy(born,1,born,0,size);System.arraycopy(distance,1,distance,0,size);}
    public void add(double px,double py,double pz,long tick){
        if(!Double.isFinite(px)||!Double.isFinite(py)||!Double.isFinite(pz)){clear();return;}
        if(size>0){double dx=px-x[size-1],dy=py-y[size-1],dz=pz-z[size-1],d=dx*dx+dy*dy+dz*dz;
            if(d>64)clear();else if(d<.0004)return;
        }
        if(size==CAPACITY)removeFirst();
        distance[size]=size==0?0:distance[size-1]+Math.sqrt(Math.pow(px-x[size-1],2)+Math.pow(py-y[size-1],2)+Math.pow(pz-z[size-1],2));
        x[size]=px;y[size]=py;z[size]=pz;born[size++]=tick;
    }
    public float alpha(int i,double now,int lifetime){float t=(float)Math.max(0,Math.min(1,1-(now-born[i])/lifetime));return t*t*(3-2*t);}
    public double coordinate(int axis,int i){i=Math.max(0,Math.min(size-1,i));return axis==0?x[i]:axis==1?y[i]:z[i];}
    public double distanceAt(int segment,double t){if(size==0)return 0;int a=Math.max(0,Math.min(size-1,segment)),b=Math.min(size-1,a+1);return distance[a]+(distance[b]-distance[a])*Math.max(0,Math.min(1,t));}
    /** Catmull-Rom through recorded positions. No generated player movement. */
    public double sample(int axis,int segment,double t){
        double a=coordinate(axis,segment-1),b=coordinate(axis,segment),c=coordinate(axis,segment+1),d=coordinate(axis,segment+2);
        return .5*((2*b)+(-a+c)*t+(2*a-5*b+4*c-d)*t*t+(-a+3*b-3*c+d)*t*t*t);
    }
}
