package dev.alien.client.cosmetic.mesh;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/** Regression checks exercise silhouette, real openings, topology and fabric deformation. */
public class CosmeticMeshTest {
    private static Mesh[] builtins() {
        return new Mesh[]{CosmeticMeshes.hat("JAPANESE"),CosmeticMeshes.hat("SAMURAI"),CosmeticMeshes.hat("CROWN"),CosmeticMeshes.hat("SAKURA"),
                CosmeticMeshes.face("KITSUNE"),CosmeticMeshes.face("FUTURE"),CosmeticMeshes.face("GLASSES"),
                CosmeticMeshes.back("WINGS",true),CosmeticMeshes.back("WINGS",false),CosmeticMeshes.back("BACKPACK",true),CosmeticMeshes.charm()};
    }

    @Test public void builtinsAreCachedAndUnknownSelectionsAreEmpty() {
        assertSame(CosmeticMeshes.hat("JAPANESE"),CosmeticMeshes.hat("JAPANESE"));
        assertSame(CosmeticMeshes.face("KITSUNE"),CosmeticMeshes.face("KITSUNE"));
        assertSame(CosmeticMeshes.back("WINGS",true),CosmeticMeshes.back("WINGS",true));
        assertSame(Mesh.EMPTY,CosmeticMeshes.hat("NONE"));
        assertSame(Mesh.EMPTY,CosmeticMeshes.face("unknown"));
        assertSame(Mesh.EMPTY,CosmeticMeshes.back("CAPE",false));
    }

    @Test public void everyBuiltinIsACompleteFiniteNonDegenerateTriangleStream() {
        for(MeshView mesh:builtins()) {
            assertTrue(mesh.vertexCount()>30);assertEquals(0,mesh.vertexCount()%3);
            checkFiniteNormals(mesh);
            for(int i=0;i<mesh.vertexCount();i+=3) {
                double ux=mesh.x(i+1)-mesh.x(i),uy=mesh.y(i+1)-mesh.y(i),uz=mesh.z(i+1)-mesh.z(i);
                double vx=mesh.x(i+2)-mesh.x(i),vy=mesh.y(i+2)-mesh.y(i),vz=mesh.z(i+2)-mesh.z(i);
                double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;
                assertTrue("Degenerate triangle",nx*nx+ny*ny+nz*nz>1e-10);
            }
        }
    }

    @Test public void japaneseHatHasACircularBrimAndRaisedConicalCenter() {
        Mesh mesh=CosmeticMeshes.hat("JAPANESE");
        Set<Integer> angles=new HashSet<Integer>();
        float highest=100,largest=0;
        for(int i=0;i<mesh.vertexCount();i++) {
            double radius=Math.hypot(mesh.x(i),mesh.z(i));
            highest=Math.min(highest,mesh.y(i));largest=Math.max(largest,(float)radius);
            if(Math.abs(radius-8.72)<.015)angles.add((int)Math.round(Math.atan2(mesh.z(i),mesh.x(i))*10000));
        }
        assertEquals(8.72f,largest,.025f);
        assertTrue(highest<-13.5f);
        assertTrue("Circular rim must sample all sectors, not four corners",angles.size()>=48);
    }

    @Test public void vertexLightingNormalsFollowTriangleWinding() {
        for(MeshView mesh:builtins())for(int i=0;i<mesh.vertexCount();i+=3) {
            double ux=mesh.x(i+1)-mesh.x(i),uy=mesh.y(i+1)-mesh.y(i),uz=mesh.z(i+1)-mesh.z(i);
            double vx=mesh.x(i+2)-mesh.x(i),vy=mesh.y(i+2)-mesh.y(i),vz=mesh.z(i+2)-mesh.z(i);
            double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;
            double dot=nx*(mesh.nx(i)+mesh.nx(i+1)+mesh.nx(i+2))
                    +ny*(mesh.ny(i)+mesh.ny(i+1)+mesh.ny(i+2))+nz*(mesh.nz(i)+mesh.nz(i+1)+mesh.nz(i+2));
            assertTrue("Inverted vertex normal would shade or cull the surface incorrectly",dot>=-1e-6);
        }
    }

    @Test public void samuraiHasSculptedHornsAndSeparateCurvedNeckGuard() {
        Mesh mesh=CosmeticMeshes.hat("SAMURAI");
        float top=0,bottom=-20;
        int nonAxialNormals=0;
        for(int i=0;i<mesh.vertexCount();i++) {
            top=Math.min(top,mesh.y(i));bottom=Math.max(bottom,mesh.y(i));
            if(Math.abs(mesh.nx(i))>.1f&&Math.abs(mesh.nz(i))>.1f)nonAxialNormals++;
        }
        assertTrue(top<-17);assertTrue(bottom>-4);
        assertTrue(nonAxialNormals>100);
    }

    @Test public void foxMaskAndGlassesHaveOpenEyeRegions() {
        Mesh mask=CosmeticMeshes.face("KITSUNE");
        assertFalse("Left eye opening covered",coversXY(mask,-2,-5.15));
        assertFalse("Right eye opening covered",coversXY(mask,2,-5.15));
        assertTrue("Mask bridge missing",coversXY(mask,.1,-5.15));
        Mesh glasses=CosmeticMeshes.face("GLASSES");
        assertFalse("Left lens should remain open",coversXY(glasses,-2.10,-5.10));
        assertFalse("Right lens should remain open",coversXY(glasses,2.10,-5.10));
    }

    @Test public void wingsHaveMirroredTaperedSilhouettes() {
        Mesh left=CosmeticMeshes.back("WINGS",true),right=CosmeticMeshes.back("WINGS",false);
        float minLeft=100,maxLeft=-100,minRight=100,maxRight=-100;
        for(int i=0;i<left.vertexCount();i++){minLeft=Math.min(minLeft,left.x(i));maxLeft=Math.max(maxLeft,left.x(i));}
        for(int i=0;i<right.vertexCount();i++){minRight=Math.min(minRight,right.x(i));maxRight=Math.max(maxRight,right.x(i));}
        assertTrue(minLeft>0);assertTrue(maxRight<0);
        assertEquals(maxLeft,-minRight,.05f);assertEquals(minLeft,-maxRight,.05f);
        assertTrue(maxLeft>14);
    }

    @Test public void clothShouldersRemainPinnedWhileItsHemMoves() {
        CapeCloth cape=new CapeCloth(14,6);
        cape.update(0,0,1);
        float[] initial=new float[cape.vertexCount()*3];
        for(int i=0;i<cape.vertexCount();i++){initial[i*3]=cape.x(i);initial[i*3+1]=cape.y(i);initial[i*3+2]=cape.z(i);}
        cape.update(17,1.3f,1.4f);
        boolean moved=false;
        for(int i=0;i<cape.vertexCount();i++) {
            if(cape.v(i)==0) {
                assertEquals(initial[i*3],cape.x(i),0);assertEquals(initial[i*3+1],cape.y(i),0);assertEquals(initial[i*3+2],cape.z(i),0);
            } else if(cape.v(i)>.8&&Math.abs(initial[i*3+2]-cape.z(i))>.1) moved=true;
        }
        assertTrue("The cloth must bend rather than rotate a rigid panel",moved);
    }

    @Test public void fabricTextureCoordinatesStayAttachedDuringAnimation() {
        CapeCloth cape=new CapeCloth(20,8);
        float[] uv=new float[cape.vertexCount()*2];
        for(int i=0;i<cape.vertexCount();i++){uv[i*2]=cape.u(i);uv[i*2+1]=cape.v(i);}
        cape.update(512,2,2);
        for(int i=0;i<cape.vertexCount();i++) {
            assertEquals(uv[i*2],cape.u(i),0);assertEquals(uv[i*2+1],cape.v(i),0);
            assertTrue(cape.u(i)>=0&&cape.u(i)<=1&&cape.v(i)>=0&&cape.v(i)<=1);
        }
    }

    @Test public void capeHasRoundedTaperedHemInsteadOfARectangle() {
        CapeCloth cape=new CapeCloth(20,8);
        cape.update(0,0,0);
        float edgeY=0,centerY=0,edgeX=0;
        for(int i=0;i<cape.vertexCount();i++)if(cape.v(i)==1) {
            if(cape.u(i)==0){edgeY=cape.y(i);edgeX=Math.abs(cape.x(i));}
            if(cape.u(i)==.5f)centerY=cape.y(i);
        }
        assertTrue(centerY-edgeY>.5f);
        assertTrue(edgeX<4.5f);
        boolean bent=false;
        for(int i=0;i<cape.vertexCount();i++)if(cape.v(i)>.7&&cape.z(i)>3.8)bent=true;
        assertTrue(bent);
    }

    @Test public void capeRejectsInvalidResolutionAndHasBoundedQualityLevels() {
        CapeCloth low=new CapeCloth(8,4),high=new CapeCloth(20,8);
        assertEquals(8*4*4,low.triangleCount());assertEquals(20*8*4,high.triangleCount());
        try{new CapeCloth(1,2);fail();}catch(IllegalArgumentException expected){}
        try{new CapeCloth(20,200);fail();}catch(IllegalArgumentException expected){}
    }

    @Test public void malformedAnimationInputsCannotProduceInvalidGeometry() {
        CapeCloth cape=new CapeCloth();
        cape.update(Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY);checkFiniteNormals(cape);
        cape.update(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);checkFiniteNormals(cape);
        for(int i=0;i<cape.vertexCount();i++) {
            assertTrue(Math.abs(cape.x(i))<7);assertTrue(cape.y(i)>=.9&&cape.y(i)<18);assertTrue(cape.z(i)>1&&cape.z(i)<9);
        }
    }

    private static void checkFiniteNormals(MeshView mesh) {
        for(int i=0;i<mesh.vertexCount();i++) {
            assertTrue(Float.isFinite(mesh.x(i))&&Float.isFinite(mesh.y(i))&&Float.isFinite(mesh.z(i)));
            double length=Math.sqrt(mesh.nx(i)*mesh.nx(i)+mesh.ny(i)*mesh.ny(i)+mesh.nz(i)*mesh.nz(i));
            assertEquals("Normal must be normalized",1,length,.002);
            assertTrue(mesh.material(i)>=0&&mesh.material(i)<=3);
            assertTrue(Float.isFinite(mesh.shade(i))&&mesh.shade(i)>=.48f&&mesh.shade(i)<=1.031f);
        }
    }

    private static boolean coversXY(MeshView mesh,double x,double y) {
        for(int i=0;i<mesh.vertexCount();i+=3) {
            double ax=mesh.x(i),ay=mesh.y(i),bx=mesh.x(i+1),by=mesh.y(i+1),cx=mesh.x(i+2),cy=mesh.y(i+2);
            double denominator=(by-cy)*(ax-cx)+(cx-bx)*(ay-cy);
            if(Math.abs(denominator)<1e-7)continue;
            double a=((by-cy)*(x-cx)+(cx-bx)*(y-cy))/denominator;
            double b=((cy-ay)*(x-cx)+(ax-cx)*(y-cy))/denominator;
            if(a>1e-5&&b>1e-5&&1-a-b>1e-5)return true;
        }
        return false;
    }
}
