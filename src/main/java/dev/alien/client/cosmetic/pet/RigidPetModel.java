package dev.alien.client.cosmetic.pet;

import java.io.*;
import java.util.zip.GZIPInputStream;

/** Credited original static Sahur mesh; deliberately contains no fabricated skeleton or clips. */
public final class RigidPetModel {
    private final float[] positions,normals;private final int[] colors,indices;private final byte[] materials;
    public static RigidPetModel load()throws IOException{
        InputStream in=RigidPetModel.class.getResourceAsStream("/assets/alienclient/models/companion/tung.rigid.gz");
        if(in==null)throw new FileNotFoundException("Bundled Tung Tung Sahur model");
        try(InputStream resource=in;GZIPInputStream gzip=new GZIPInputStream(resource)){return new RigidPetModel(gzip);}
    }
    public RigidPetModel(InputStream stream)throws IOException{
        DataInputStream in=new DataInputStream(stream);if(in.readInt()!=0x414c5231||in.readInt()!=1)throw new IOException("Invalid static companion format");
        int vertices=count(in,1,12000),elements=count(in,3,60000),mats=count(in,1,16);
        if(in.readInt()!=0||elements%3!=0)throw new IOException("Invalid static mesh header");
        positions=new float[vertices*3];normals=new float[vertices*3];colors=new int[mats];indices=new int[elements];materials=new byte[vertices];
        for(int i=0;i<mats;i++){colors[i]=in.readInt();if((colors[i]&0xff000000)!=0)throw new IOException("Invalid RGB material");}
        for(int i=0;i<vertices;i++){
            for(int j=0;j<3;j++)positions[i*3+j]=finite(in.readFloat());
            for(int j=0;j<3;j++)normals[i*3+j]=finite(in.readFloat());
            materials[i]=in.readByte();if(materials[i]<0||materials[i]>=mats)throw new IOException("Invalid material index");
        }
        for(int i=0;i<elements;i++){indices[i]=in.readInt();if(indices[i]<0||indices[i]>=vertices)throw new IOException("Invalid triangle index");}
        if(in.read()!=-1)throw new IOException("Trailing static mesh data");
    }
    private static int count(DataInputStream in,int min,int max)throws IOException{int v=in.readInt();if(v<min||v>max)throw new IOException("Invalid static mesh count");return v;}
    private static float finite(float v)throws IOException{if(!Float.isFinite(v)||Math.abs(v)>4)throw new IOException("Invalid static mesh coordinate");return v;}
    public int vertexCount(){return materials.length;}public int triangleCount(){return indices.length/3;}public int indexCount(){return indices.length;}
    public int index(int i){return indices[i];}public int material(int i){return materials[i];}public int color(int material){return colors[material];}
    public float position(int i){return positions[i];}public float normal(int i){return normals[i];}
}
