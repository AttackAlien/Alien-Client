package dev.alien.client.cosmetic.pet;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Immutable imported Quaternius mesh/rig. Shared clips; one reusable output per renderer. */
public final class SkinnedPetModel {
    private final int vertices,joints;
    private final float[] positions,normals,weights;
    private final byte[] bones,materials;
    private final int[] indices;
    private final Map<String,Clip> clips=new LinkedHashMap<String,Clip>();
    private final Clip idle,walk,clapping,sitting;
    public static SkinnedPetModel load() throws IOException {
        InputStream in=SkinnedPetModel.class.getResourceAsStream("/assets/alienclient/models/companion/alien.anm.gz");
        if(in==null)throw new FileNotFoundException("Bundled Quaternius alien model");
        try(InputStream resource=in;GZIPInputStream gzip=new GZIPInputStream(resource)){return new SkinnedPetModel(gzip);}
    }
    public SkinnedPetModel(InputStream input)throws IOException {
        DataInputStream in=new DataInputStream(input);
        if(in.readInt()!=0x414c4e32||in.readInt()!=1)throw new IOException("Invalid companion model format");
        vertices=count(in,1,12000);int triangles=count(in,3,60000);joints=count(in,1,64);int animations=count(in,1,16);
        if(triangles%3!=0)throw new IOException("Incomplete triangles");
        positions=new float[vertices*3];normals=new float[vertices*3];weights=new float[vertices*4];
        bones=new byte[vertices*4];materials=new byte[vertices];indices=new int[triangles];
        for(int i=0;i<vertices;i++){
            readFloats(in,positions,i*3,3);readFloats(in,normals,i*3,3);in.readFully(bones,i*4,4);
            readFloats(in,weights,i*4,4);float sum=0;
            for(int j=0;j<4;j++){int n=i*4+j;if((bones[n]&255)>=joints||weights[n]<0)throw new IOException("Invalid skin influence");sum+=weights[n];}
            if(Math.abs(sum-1)>.001)throw new IOException("Invalid skin weight sum");
            materials[i]=in.readByte();if(materials[i]<0||materials[i]>4)throw new IOException("Unknown imported material");
        }
        for(int i=0;i<indices.length;i++){indices[i]=in.readInt();if(indices[i]<0||indices[i]>=vertices)throw new IOException("Invalid mesh index");}
        long total=0;
        for(int i=0;i<animations;i++){
            String name=in.readUTF();float duration=finite(in.readFloat());int frames=count(in,2,601);
            if(name.length()>64||duration<=0||duration>30||clips.containsKey(name))throw new IOException("Invalid animation");
            int size=frames*joints*21;total+=size;if(total>8000000)throw new IOException("Animation budget exceeded");
            float[] values=new float[size];readFloats(in,values,0,size);clips.put(name,new Clip(duration,frames,joints*21,values));
        }
        if(in.read()!=-1)throw new IOException("Trailing companion data");
        idle=required("Idle");walk=required("Walk");clapping=required("Clapping");sitting=required("Sitting");
    }
    private Clip required(String name)throws IOException {Clip c=clips.get(name);if(c==null)throw new IOException("Missing authored clip: "+name);return c;}
    private static int count(DataInputStream in,int min,int max)throws IOException {int value=in.readInt();if(value<min||value>max)throw new IOException("Invalid model count");return value;}
    private static float finite(float value)throws IOException {if(!Float.isFinite(value)||Math.abs(value)>1e8)throw new IOException("Invalid model coordinate");return value;}
    private static void readFloats(DataInputStream in,float[] out,int offset,int count)throws IOException {for(int i=0;i<count;i++)out[offset+i]=finite(in.readFloat());}
    public int vertexCount(){return vertices;}public int triangleCount(){return indices.length/3;}public int jointCount(){return joints;}
    public int index(int i){return indices[i];}public int indexCount(){return indices.length;}public int material(int i){return materials[i];}
    public Set<String> animationNames(){return Collections.unmodifiableSet(clips.keySet());}
    public Pose createPose(){return new Pose(vertices,joints);}
    public void sample(double time,float movement,float rest,float reaction,Pose out){
        float m=unit(movement),s=unit(rest),d=unit(reaction);
        idle.at(time,true);walk.at(time,true);clapping.at(time,true);sitting.at(s*sitting.duration,false);
        for(int i=0;i<out.matrices.length;i++){
            float a=idle.value(i),b=walk.value(i),v=a+(b-a)*m;
            v+=(clapping.value(i)-v)*d;out.matrices[i]=v+(sitting.value(i)-v)*s;
        }
        skin(out);
    }
    public void sampleClip(String name,double time,Pose out){
        Clip c=clips.get(name);if(c==null)throw new IllegalArgumentException(name);c.at(time,false);
        for(int i=0;i<out.matrices.length;i++)out.matrices[i]=c.value(i);skin(out);
    }
    private void skin(Pose out){
        Arrays.fill(out.positions,0);Arrays.fill(out.normals,0);
        for(int i=0;i<vertices;i++){
            int p=i*3;float x=positions[p],y=positions[p+1],z=positions[p+2],nx=normals[p],ny=normals[p+1],nz=normals[p+2];
            for(int influence=0;influence<4;influence++){
                int w=i*4+influence;float weight=weights[w];if(weight==0)continue;int b=(bones[w]&255)*21;float[] a=out.matrices;
                out.positions[p]+=(a[b]*x+a[b+1]*y+a[b+2]*z+a[b+3])*weight;
                out.positions[p+1]+=(a[b+4]*x+a[b+5]*y+a[b+6]*z+a[b+7])*weight;
                out.positions[p+2]+=(a[b+8]*x+a[b+9]*y+a[b+10]*z+a[b+11])*weight;
                out.normals[p]+=(a[b+12]*nx+a[b+13]*ny+a[b+14]*nz)*weight;
                out.normals[p+1]+=(a[b+15]*nx+a[b+16]*ny+a[b+17]*nz)*weight;
                out.normals[p+2]+=(a[b+18]*nx+a[b+19]*ny+a[b+20]*nz)*weight;
            }
            float length=(float)Math.sqrt(out.normals[p]*out.normals[p]+out.normals[p+1]*out.normals[p+1]+out.normals[p+2]*out.normals[p+2]);
            if(length>1e-8){out.normals[p]/=length;out.normals[p+1]/=length;out.normals[p+2]/=length;}
            else{out.normals[p]=out.normals[p+2]=0;out.normals[p+1]=1;}
        }
    }
    private static float unit(float v){return Float.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
    public static final class Pose {
        public final float[] positions,normals;private final float[] matrices;
        private Pose(int vertices,int joints){positions=new float[vertices*3];normals=new float[vertices*3];matrices=new float[joints*21];}
    }
    private static final class Clip {
        final float duration;final int frames,stride;final float[] values;int a,b;float fraction;
        Clip(float duration,int frames,int stride,float[] values){this.duration=duration;this.frames=frames;this.stride=stride;this.values=values;}
        void at(double time,boolean loop){
            if(!Double.isFinite(time))time=0;
            double t=loop?((time%duration)+duration)%duration:Math.max(0,Math.min(duration,time));
            double frame=t/duration*(frames-1);int left=Math.min(frames-1,(int)frame);
            a=left*stride;b=Math.min(frames-1,left+1)*stride;fraction=(float)(frame-left);
        }
        float value(int i){return values[a+i]+(values[b+i]-values[a+i])*fraction;}
    }
}
