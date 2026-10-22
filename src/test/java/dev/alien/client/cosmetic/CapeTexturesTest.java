package dev.alien.client.cosmetic;

import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class CapeTexturesTest {
    @Test public void originalFabricIsOpaqueBoundedAndDeterministic() {
        CosmeticConfig.Accessory accessory=new CosmeticConfig.Accessory();
        BufferedImage first=CapeTextures.create(accessory),second=CapeTextures.create(accessory);
        assertEquals(128,first.getWidth());assertEquals(256,first.getHeight());
        int varied=0;
        for(int y=0;y<256;y++)for(int x=0;x<128;x++) {
            assertEquals(first.getRGB(x,y),second.getRGB(x,y));
            assertEquals(255,(first.getRGB(x,y)>>>24)&255);
            if(first.getRGB(x,y)!=first.getRGB(0,0))varied++;
        }
        assertTrue("Fabric includes shading and emblem, not a solid rectangle",varied>10000);
    }
    @Test public void EmblemAndPaletteAffectTheActualTexture() {
        CosmeticConfig.Accessory accessory=new CosmeticConfig.Accessory();
        accessory.capePattern="ALIEN";BufferedImage alien=CapeTextures.create(accessory);
        accessory.capePattern="MOON";BufferedImage moon=CapeTextures.create(accessory);
        accessory.capePattern="PLAIN";BufferedImage plain=CapeTextures.create(accessory);
        assertTrue(differences(alien,moon)>500);assertTrue(differences(moon,plain)>500);
        accessory.color=0x101827;accessory.secondaryColor=0xE6C185;
        assertTrue(differences(plain,CapeTextures.create(accessory))>20000);
    }
    private static int differences(BufferedImage a,BufferedImage b) {
        int count=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getRGB(x,y)!=b.getRGB(x,y))count++;
        return count;
    }
}
