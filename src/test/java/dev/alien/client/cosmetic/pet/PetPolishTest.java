package dev.alien.client.cosmetic.pet;

import dev.alien.client.cosmetic.mesh.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PetPolishTest {
    @Test public void smoothPartsHaveUnitNormalsAndOutwardWindingWithinSharedGeometryBudget(){
        int vertices=0;
        for(Mesh mesh:new Mesh[]{PetMeshes.HEAD,PetMeshes.BODY,PetMeshes.ARM,PetMeshes.FOOT,PetMeshes.EYE,
                PetMeshes.DOT,PetMeshes.STEM,PetMeshes.TIP,PetMeshes.COLLAR,PetMeshes.RING,PetMeshes.SMILE}){
            vertices+=mesh.vertexCount();
            for(int i=0;i<mesh.vertexCount();i++){
                assertEquals(1,Math.sqrt(mesh.nx(i)*mesh.nx(i)+mesh.ny(i)*mesh.ny(i)+mesh.nz(i)*mesh.nz(i)),1e-5);
                assertTrue(Float.isFinite(mesh.x(i))&&Float.isFinite(mesh.y(i))&&Float.isFinite(mesh.z(i)));
            }
            for(int i=0;i<mesh.vertexCount();i+=3){
                double ax=mesh.x(i+1)-mesh.x(i),ay=mesh.y(i+1)-mesh.y(i),az=mesh.z(i+1)-mesh.z(i),
                    bx=mesh.x(i+2)-mesh.x(i),by=mesh.y(i+2)-mesh.y(i),bz=mesh.z(i+2)-mesh.z(i);
                double nx=mesh.nx(i)+mesh.nx(i+1)+mesh.nx(i+2),
                    ny=mesh.ny(i)+mesh.ny(i+1)+mesh.ny(i+2),nz=mesh.nz(i)+mesh.nz(i+1)+mesh.nz(i+2);
                assertTrue("Triangle winding must agree with its smoothed normals",(ay*bz-az*by)*nx+(az*bx-ax*bz)*ny+(ax*by-ay*bx)*nz>0);
            }
        }
        assertTrue("Shared geometry, including face and collar, must remain bounded",vertices<7000);
    }
    @Test public void headNormalsFollowTheTaperedSurfaceRatherThanAnUnmodifiedSphere(){
        Mesh mesh=PetMeshes.HEAD;
        for(int i=0;i<mesh.vertexCount();i++){
            double x=mesh.x(i),y=mesh.y(i),z=mesh.z(i),f=.86+.14*y/2.85;
            double nx=x/(2.95*2.95*f*f),ny=y/(2.85*2.85)-x*x*.14/(2.95*2.95*f*f*f*2.85),nz=z/(2.25*2.25);
            double length=Math.sqrt(nx*nx+ny*ny+nz*nz);
            assertEquals(nx/length,mesh.nx(i),1e-5);assertEquals(ny/length,mesh.ny(i),1e-5);assertEquals(nz/length,mesh.nz(i),1e-5);
        }
    }
    @Test public void secondaryJointsAndBlinkStayContinuousAndAnimationOffClearsThem(){
        PetPose a=new PetPose(),b=new PetPose();
        for(int i=0;i<10000;i++){
            double t=i*.001;a.sample(t,1,0,1,true);b.sample(t+.0001,1,0,1,true);
            assertTrue(Math.abs(a.rightElbow-b.rightElbow)<.02);assertTrue(Math.abs(a.wrist-b.wrist)<.02);
            assertTrue(Math.abs(a.eyes-b.eyes)<.01);assertTrue(a.breath>.97&&a.breath<1.03);
            assertTrue(Math.abs(a.gazeX)<=.09f&&Math.abs(a.gazeY)<=.045f);
        }
        a.sample(5,1,1,1,false);
        assertEquals(1,a.breath,0);assertEquals(1,a.eyes,0);assertEquals(0,a.headPitch,0);
        assertEquals(0,a.rightElbow,0);assertEquals(0,a.leftElbow,0);assertEquals(0,a.wrist,0);
        assertEquals(0,a.gazeX,0);assertEquals(0,a.gazeY,0);
    }
    @Test public void waveUsesElbowAndWristWhileSleepingSettlesTheGaze(){
        PetPose idle=new PetPose(),wave=new PetPose(),sleep=new PetPose();
        idle.sample(.2,0,0,0,true);wave.sample(.2,0,0,1,true);sleep.sample(.2,0,1,0,true);
        assertTrue(wave.rightElbow>idle.rightElbow+20);assertTrue(Math.abs(wave.wrist)>10);
        assertTrue(Math.abs(wave.rightArm)>45);assertEquals(0,idle.wrist,0);
        assertEquals(0,sleep.gazeX,0);assertEquals(0,sleep.gazeY,0);assertTrue(sleep.eyes<.1&&sleep.headPitch>10);
    }
}
