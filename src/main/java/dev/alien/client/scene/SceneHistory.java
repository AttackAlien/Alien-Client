package dev.alien.client.scene;
/** Small bounded samples shared by effects and tests. Coordinates never leave this client. */
public final class SceneHistory {
    public static final int LIMIT=32;
    private final double[] x=new double[LIMIT],y=new double[LIMIT],z=new double[LIMIT],time=new double[LIMIT];
    private int size;
    public int size(){return size;}public void clear(){size=0;}
    public void add(double px,double py,double pz,double now,int capacity){
        if(!Double.isFinite(px)||!Double.isFinite(py)||!Double.isFinite(pz)||!Double.isFinite(now))return;
        capacity=Math.max(1,Math.min(LIMIT,capacity));
        while(size>=capacity)removeFirst();x[size]=px;y[size]=py;z[size]=pz;time[size++]=now;
    }
    private void removeFirst(){--size;System.arraycopy(x,1,x,0,size);System.arraycopy(y,1,y,0,size);System.arraycopy(z,1,z,0,size);System.arraycopy(time,1,time,0,size);}
    public void expire(double now,double lifetime){while(size>0&&now-time[0]>lifetime)removeFirst();}
    public double x(int i){return x[i];}public double y(int i){return y[i];}public double z(int i){return z[i];}public double age(int i,double now){return now-time[i];}
    public double lastDistanceSq(double px,double py,double pz){if(size==0)return Double.POSITIVE_INFINITY;double a=px-x[size-1],b=py-y[size-1],c=pz-z[size-1];return a*a+b*b+c*c;}
}
