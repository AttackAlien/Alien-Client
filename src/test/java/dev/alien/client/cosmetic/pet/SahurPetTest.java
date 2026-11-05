package dev.alien.client.cosmetic.pet;

import com.google.gson.*;
import dev.alien.client.config.ConfigStore;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class SahurPetTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void actualStaticMeshPreservesAllMaterialsAndFiniteNormals()throws Exception{
        RigidPetModel m=RigidPetModel.load();assertEquals(1792,m.vertexCount());assertEquals(872,m.triangleCount());Set<Integer> mats=new HashSet<Integer>();
        for(int i=0;i<m.vertexCount();i++){
            mats.add(m.material(i));double length=0;
            for(int j=0;j<3;j++){assertTrue(Float.isFinite(m.position(i*3+j))&&Math.abs(m.position(i*3+j))<1);length+=m.normal(i*3+j)*m.normal(i*3+j);}
            assertEquals(1,Math.sqrt(length),1e-5);
        }
        assertEquals(6,mats.size());assertEquals(0xCE832F,m.color(2));assertEquals(0xffffff,m.color(3));assertEquals(0x141414,m.color(4));
    }
    @Test public void importedFacePointsTowardTheStudiosFront()throws Exception{
        RigidPetModel m=RigidPetModel.load();double eyes=0,head=0;int eyeCount=0,headCount=0;
        for(int i=0;i<m.vertexCount();i++){
            if(m.material(i)==3){eyes+=m.position(i*3+2);eyeCount++;}
            if(m.material(i)==2&&m.position(i*3+1)>.2){head+=m.position(i*3+2);headCount++;}
        }
        assertTrue("Eyes must be in front of the wooden head, not hidden on its back",eyes/eyeCount>head/headCount+.01);
    }
    @Test public void staticAssetAndOriginalGlbMatchTheAttributionRecord()throws Exception{
        JsonObject info;
        try(Reader in=new InputStreamReader(getClass().getResourceAsStream("/assets/alienclient/models/companion/tung.rigid.json"),"UTF-8")){info=new JsonParser().parse(in).getAsJsonObject();}
        assertEquals("CalinnHotCake",info.get("author").getAsString());assertEquals("CC-BY-3.0",info.get("license").getAsString());assertEquals(0,info.get("authoredAnimations").getAsInt());
        try(InputStream in=new FileInputStream("third_party/tung-tung-sahur/Tung-Tung-Sahur.glb")){assertEquals(info.get("sourceSha256").getAsString(),hash(in));}
        try(InputStream in=getClass().getResourceAsStream("/assets/alienclient/models/companion/tung.rigid.gz")){assertEquals(info.get("outputSha256").getAsString(),hash(in));}
    }
    @Test public void olderAndInvalidProfilesRetainTheAlienAndTheirExistingSettings(){
        PetDesign p=new Gson().fromJson("{\"enabled\":true,\"mode\":\"Orbit\",\"color\":123,\"preset\":\"Custom\",\"size\":1.25}",PetDesign.class);p.normalize();
        assertEquals("Alien",p.model);assertTrue(p.enabled);assertEquals("Orbit",p.mode);assertEquals(123,p.color);assertEquals(1.25f,p.size,0);
        p.model="missing";p.normalize();assertEquals("Alien",p.model);p.model=null;p.normalize();assertEquals("Alien",p.model);
    }
    @Test public void modelSelectionAndRealProfilesKeepIndependentLookChoices()throws Exception{
        ConfigStore store=new ConfigStore(temp.newFolder());store.load();PetDesign p=store.config().cosmetics.self.pet;
        p.enabled=false;p.mode="Orbit";p.size=1.2f;p.speed=1.5f;p.setModel("Tung Tung Sahur");
        assertFalse(p.enabled);assertEquals("Orbit",p.mode);assertEquals(1.2f,p.size,0);assertEquals(1.5f,p.speed,0);assertEquals(0xCE832F,p.color);
        store.config().cosmetics.saveLook(2,store.config().cosmetics.self);store.saveProfile("sahur");p.setModel("Alien");
        assertTrue(store.config().cosmetics.loadLook(2).pet.tung());assertTrue(store.loadProfile("sahur"));assertTrue(store.config().cosmetics.self.pet.tung());
        p=store.config().cosmetics.self.pet;p.applyPreset("Mint");assertEquals("Tung Tung Sahur",p.model);assertEquals(0x68DABC,p.color);
    }
    @Test public void corruptStaticMeshFailsBeforeUse()throws Exception{
        for(byte[] b:new byte[][]{new byte[0],new byte[24],header(Integer.MAX_VALUE),header(1792)})try{new RigidPetModel(new ByteArrayInputStream(b));fail("Invalid mesh accepted");}catch(IOException expected){}
    }
    private static byte[] header(int vertices)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);for(int n:new int[]{0x414c5231,1,vertices,2616,6,0})out.writeInt(n);return bytes.toByteArray();}
    private static String hash(InputStream in)throws Exception{MessageDigest d=MessageDigest.getInstance("SHA-256");byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)d.update(b,0,n);StringBuilder result=new StringBuilder();for(byte v:d.digest())result.append(String.format(Locale.ROOT,"%02x",v&255));return result.toString();}
}
