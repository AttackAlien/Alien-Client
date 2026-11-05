package dev.alien.client.cosmetic.pet;

import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import com.google.gson.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImportedPetModelTest {
    @Test public void sourceAndConvertedResourceMatchTheirCreditedProvenance()throws Exception{
        JsonObject info;
        try(Reader in=new InputStreamReader(getClass().getResourceAsStream("/assets/alienclient/models/companion/alien.anm.json"),"UTF-8")){info=new JsonParser().parse(in).getAsJsonObject();}
        assertEquals("Quaternius",info.get("author").getAsString());assertEquals("CC0-1.0",info.get("license").getAsString());
        try(InputStream in=new FileInputStream("third_party/quaternius-alien/Alien.glb")){assertEquals(info.get("sourceSha256").getAsString(),hash(in));}
        try(InputStream in=getClass().getResourceAsStream("/assets/alienclient/models/companion/alien.anm.gz")){assertEquals(info.get("outputSha256").getAsString(),hash(in));}
    }
    @Test public void bundledAssetContainsTheChosenAuthorsActualMeshAndAllFourteenClips()throws Exception{
        SkinnedPetModel m=SkinnedPetModel.load();assertEquals(7642,m.vertexCount());assertEquals(3774,m.triangleCount());assertEquals(45,m.jointCount());
        assertEquals(14,m.animationNames().size());assertTrue(m.animationNames().containsAll(Arrays.asList("Idle","Walk","Clapping","Sitting","Swimming","Run","Jump")));
        Set<Integer> materials=new HashSet<Integer>();for(int i=0;i<m.vertexCount();i++)materials.add(m.material(i));assertEquals(5,materials.size());
    }
    @Test public void everyAuthoredClipProducesFiniteBoundedSkinnedVerticesAndUnitNormals()throws Exception{
        SkinnedPetModel m=SkinnedPetModel.load();SkinnedPetModel.Pose p=m.createPose();
        for(String clip:m.animationNames())for(double time:new double[]{0,.25,.7,1,2,10}){
            m.sampleClip(clip,time,p);
            for(int i=0;i<p.positions.length;i+=3){
                assertTrue(Float.isFinite(p.positions[i])&&Float.isFinite(p.positions[i+1])&&Float.isFinite(p.positions[i+2]));
                assertTrue(Math.abs(p.positions[i])<2&&Math.abs(p.positions[i+1])<2&&Math.abs(p.positions[i+2])<2);
                assertEquals(1,Math.sqrt(p.normals[i]*p.normals[i]+p.normals[i+1]*p.normals[i+1]+p.normals[i+2]*p.normals[i+2]),1e-4);
            }
        }
    }
    @Test public void realMovementReactionAndRestSelectDifferentOriginalDeformations()throws Exception{
        SkinnedPetModel m=SkinnedPetModel.load();SkinnedPetModel.Pose idle=m.createPose(),moving=m.createPose(),reaction=m.createPose(),rest=m.createPose();
        m.sample(.45,0,0,0,idle);m.sample(.45,1,0,0,moving);m.sample(.45,0,0,1,reaction);m.sample(.45,0,1,0,rest);
        assertTrue(changed(idle,moving)>500);assertTrue(changed(idle,reaction)>500);assertTrue(changed(idle,rest)>500);
    }
    @Test public void repeatedSamplingCannotAdvancePlaybackAndCrossfadesRemainContinuous()throws Exception{
        SkinnedPetModel m=SkinnedPetModel.load();SkinnedPetModel.Pose a=m.createPose(),b=m.createPose();m.sample(.65,.3f,.2f,.4f,a);
        for(int i=0;i<100;i++)m.sampleClip("Run",i*.1,b);
        m.sample(.65,.3f,.2f,.4f,b);assertArrayEquals(a.positions,b.positions,0);
        m.sample(.65001,.30001f,.20001f,.40001f,b);for(int i=0;i<a.positions.length;i++)assertEquals(a.positions[i],b.positions[i],.001);
    }
    @Test public void invalidCountsAndTruncationFailBeforeAnyRendering()throws Exception{
        for(byte[] bytes:new byte[][]{new byte[0],new byte[24],header(Integer.MAX_VALUE),header(7642)}){
            try{new SkinnedPetModel(new ByteArrayInputStream(bytes));fail("Corrupt model accepted");}catch(IOException expected){}
        }
    }
    private static int changed(SkinnedPetModel.Pose a,SkinnedPetModel.Pose b){int count=0;for(int i=0;i<a.positions.length;i+=3)if(Math.abs(a.positions[i]-b.positions[i])+Math.abs(a.positions[i+1]-b.positions[i+1])+Math.abs(a.positions[i+2]-b.positions[i+2])>.001)count++;return count;}
    private static String hash(InputStream in)throws Exception{MessageDigest d=MessageDigest.getInstance("SHA-256");byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)d.update(b,0,n);StringBuilder result=new StringBuilder();for(byte v:d.digest())result.append(String.format(Locale.ROOT,"%02x",v&255));return result.toString();}
    private static byte[] header(int vertices)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);for(int n:new int[]{0x414c4e32,1,vertices,11322,45,14})out.writeInt(n);return bytes.toByteArray();}
}
