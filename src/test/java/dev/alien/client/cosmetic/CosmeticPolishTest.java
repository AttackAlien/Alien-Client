package dev.alien.client.cosmetic;
import com.google.gson.Gson;
import dev.alien.client.cosmetic.mesh.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class CosmeticPolishTest {
    @Test public void bundledCc0MaterialPreservesVerifiedSourceAndCredit()throws Exception{
        java.security.MessageDigest hash=java.security.MessageDigest.getInstance("SHA-256");
        try(java.io.InputStream in=getClass().getResourceAsStream("/assets/alienclient/textures/cosmetic/fabric_pattern_07_rough_1k.jpg")){
            assertNotNull(in);byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)hash.update(buffer,0,n);
        }
        StringBuilder hex=new StringBuilder();for(byte b:hash.digest())hex.append(String.format("%02x",b&255));
        assertEquals("3d90ac40b5c32a15649cc8525b7eeed133ea46b63a0e3eae5e34911d5c967e3a",hex.toString());
        try(java.io.InputStream in=getClass().getResourceAsStream("/assets/alienclient/textures/cosmetic/NOTICE.txt")){
            assertNotNull(in);java.util.Scanner s=new java.util.Scanner(in,"UTF-8").useDelimiter("\\A");String notice=s.next();assertTrue(notice.contains("CC0")&&notice.contains("Rob Tuytel"));
        }
    }
    @Test public void ribbonRecordsRealHistoryAndDropsTeleportsAndExpiredPoints(){
        RibbonPath p=new RibbonPath();p.add(0,1,0,0);p.add(.2,1,0,1);p.add(.4,1,.1,2);
        assertEquals(3,p.size());assertEquals(.2,p.sample(0,1,0),1e-8);assertEquals(.4,p.sample(0,1,1),1e-8);
        assertTrue(p.alpha(0,4,18)<p.alpha(2,4,18));p.add(.4,1,.1,3);assertEquals(3,p.size());
        p.add(100,1,100,4);assertEquals(1,p.size());p.expire(24,20);assertEquals(0,p.size());
        p.add(Double.NaN,0,0,25);assertEquals(0,p.size());
        for(int i=0;i<100;i++)p.add(i*.1,1,0,i);assertEquals(RibbonPath.CAPACITY,p.size());
    }
    @Test public void springRespondsToMovementRelaxesAndRemainsFinite(){
        CapeDynamics d=new CapeDynamics();d.update(0,0,0,0,0);
        for(int i=1;i<=40;i++)d.update(i,3,1,0,3);
        assertTrue(d.bend()>2);assertTrue(d.sway()>0);
        float bend=d.bend();for(int i=41;i<=120;i++)d.update(i,0,0,0,0);
        assertTrue(d.bend()<bend*.01);d.update(Float.NaN,0,0,0,0);assertEquals(0,d.bend(),0);
        d.update(0,0,0,0,0);d.update(.5f,Float.POSITIVE_INFINITY,Float.NaN,Float.NEGATIVE_INFINITY,0);
        assertTrue(Float.isFinite(d.bend())&&Float.isFinite(d.sway()));
    }
    @Test public void springIsStableAcrossRenderRatesAndDoesNotSharePlayers(){
        CapeDynamics slow=new CapeDynamics(),fast=new CapeDynamics(),other=new CapeDynamics();
        slow.update(0,0,0,0,0);fast.update(0,0,0,0,0);other.update(0,0,0,0,0);
        for(int i=1;i<=40;i++)slow.update(i,2,0,0,0);
        for(int i=1;i<=160;i++)fast.update(i*.25f,2,0,0,0);
        assertEquals(slow.bend(),fast.bend(),.02);assertEquals(0,other.bend(),0);
        CapeCloth cape=new CapeCloth();cape.update(80,1,1,slow.bend(),fast.sway());
        for(int i=0;i<cape.vertexCount();i++){assertTrue(Float.isFinite(cape.z(i)));if(cape.v(i)==0)assertEquals(3.15,cape.z(i),1e-5);}
    }
    @Test public void ribbonPreferencesSurviveCopiesSavedLooksAndLegacyJson(){
        Gson g=new Gson();CosmeticConfig.Accessory a=g.fromJson("{\"trail\":true}",CosmeticConfig.Accessory.class);a.normalize();
        assertEquals("Ribbon",a.trailStyle);a.hat="SAKURA";a.trailWidth=.3f;a.trailLifetime=1.4f;a.trailOpacity=.7f;
        CosmeticConfig c=new CosmeticConfig();c.saveLook(1,a);CosmeticConfig.Accessory copy=c.loadLook(1);
        assertEquals("SAKURA",copy.hat);assertEquals(.3f,copy.trailWidth,0);assertEquals(1.4f,copy.trailLifetime,0);assertEquals(.7f,copy.trailOpacity,0);
        a.trailWidth=Float.NaN;a.trailLifetime=100;a.trailOpacity=-1;a.normalize();
        assertEquals(.04f,a.trailWidth,0);assertEquals(2,a.trailLifetime,0);assertEquals(.1f,a.trailOpacity,0);
        assertTrue(CosmeticMeshes.hat("SAKURA").vertexCount()>1000);
    }
}
