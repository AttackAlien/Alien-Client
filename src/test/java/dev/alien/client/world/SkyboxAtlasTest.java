package dev.alien.client.world;

import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.*;

public class SkyboxAtlasTest {
    @Test public void recognizesExplicitSuffixOnly() {
        assertTrue(SkyboxAtlas.isCubeFile("nebula.cube.png"));
        assertFalse(SkyboxAtlas.isCubeFile("nebula.png"));
        assertFalse(SkyboxAtlas.isCubeFile("cube.jpg"));
        assertFalse(SkyboxAtlas.isCubeFile(null));
    }
    @Test public void acceptsSquareCellsAndRejectsMalformedLayouts() throws Exception {
        assertEquals(64, SkyboxAtlas.validate(192, 128).faceSize());
        assertEquals(1024, SkyboxAtlas.validate(3072, 2048).faceSize());
        int[][] invalid = {{192,120}, {192,64}, {96,64}, {3075,2050}, {4096,2048}, {0,0}};
        for (int[] pair : invalid) {
            try { SkyboxAtlas.validate(pair[0], pair[1]); fail("Accepted "+pair[0]+"x"+pair[1]); }
            catch (IOException expected) { assertTrue(expected.getMessage().contains("3x2")); }
        }
    }
    @Test public void eachFaceHasIndependentInsetUvBounds() throws Exception {
        SkyboxAtlas atlas = SkyboxAtlas.validate(768, 512);
        for (int face=0; face<6; face++) {
            int col=face%3, row=face/3;
            float u0=atlas.u(face,0), u1=atlas.u(face,1);
            float v0=atlas.v(face,0), v1=atlas.v(face,2);
            assertTrue(u0>col/3F); assertTrue(u1<(col+1)/3F);
            assertTrue(v0<1F-row/2F); assertTrue(v1>1F-(row+1)/2F);
            assertTrue(u0<u1); assertTrue(v1<v0);
            assertEquals(u0,atlas.u(face,3),0F);
            assertEquals(v0,atlas.v(face,1),0F);
        }
    }
    @Test public void cubeCornersRemainAtOriginalSkyRadius() throws Exception {
        SkyboxAtlas atlas = SkyboxAtlas.validate(192,128);
        for (int face=0;face<6;face++) for (int corner=0;corner<4;corner++) {
            double x=atlas.coordinate(face,corner,0);
            double y=atlas.coordinate(face,corner,1);
            double z=atlas.coordinate(face,corner,2);
            assertEquals(100D,Math.sqrt(x*x+y*y+z*z),1e-7);
        }
    }
}
