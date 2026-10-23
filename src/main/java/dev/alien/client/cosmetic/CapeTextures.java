package dev.alien.client.cosmetic;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.io.InputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

/** Original fabric and emblems. Eight texture slots are reused during live color editing. */
final class CapeTextures {
    private static final Map<String, ResourceLocation> CACHE = new LinkedHashMap<String, ResourceLocation>(16,.75f,true);
    private static final BufferedImage WEAVE=loadWeave();
    private CapeTextures() { }

    private static BufferedImage loadWeave(){
        try(InputStream stream=CapeTextures.class.getResourceAsStream("/assets/alienclient/textures/cosmetic/fabric_pattern_07_rough_1k.jpg")){
            return stream==null?null:ImageIO.read(stream);
        }catch(IOException failure){return null;}
    }

    static ResourceLocation get(CosmeticConfig.Accessory accessory) {
        String key=accessory.capePattern+":"+accessory.color+":"+accessory.secondaryColor;
        ResourceLocation existing=CACHE.get(key);
        if(existing!=null)return existing;
        ResourceLocation slot;
        if(CACHE.size()<8) slot=new ResourceLocation("alienclient","generated/cape-"+CACHE.size());
        else {
            Iterator<Map.Entry<String,ResourceLocation>> iterator=CACHE.entrySet().iterator();
            slot=iterator.next().getValue(); iterator.remove();
            Minecraft.getMinecraft().getTextureManager().deleteTexture(slot);
        }
        Minecraft.getMinecraft().getTextureManager().loadTexture(slot,new DynamicTexture(create(accessory)));
        CACHE.put(key,slot);return slot;
    }

    static BufferedImage create(CosmeticConfig.Accessory a) {
        BufferedImage image=new BufferedImage(128,256,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Color main=new Color(a.color&0xffffff),trim=new Color(a.secondaryColor&0xffffff);
            g.setPaint(new GradientPaint(0,0,main.brighter(),128,200,main.darker()));
            g.fillRect(0,0,128,256);
            // Poly Haven CC0 roughness map is used as a subtle woven-color detail, not a PBR claim.
            if(WEAVE!=null)for(int y=0;y<256;y++)for(int x=0;x<128;x++){
                int tex=WEAVE.getRGB(x*WEAVE.getWidth()/128,y*WEAVE.getHeight()/256),base=image.getRGB(x,y);
                float light=.83f+(tex&255)/255f*.22f;
                image.setRGB(x,y,0xff000000|(Math.min(255,Math.round((base>>16&255)*light))<<16)
                        |(Math.min(255,Math.round((base>>8&255)*light))<<8)|Math.min(255,Math.round((base&255)*light)));
            }
            g.setColor(new Color(255,255,255,10));
            for(int x=0;x<128;x+=3)g.drawLine(x,0,x,256);
            g.setColor(new Color(0,0,0,18));
            for(int y=0;y<256;y+=3)g.drawLine(0,y,128,y);
            g.setColor(trim);g.setStroke(new BasicStroke(2.4f));
            g.draw(new RoundRectangle2D.Float(7,7,114,242,12,16));
            g.setColor(new Color(trim.getRed(),trim.getGreen(),trim.getBlue(),100));
            g.setStroke(new BasicStroke(.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,10,new float[]{2,3},0));
            g.draw(new RoundRectangle2D.Float(11,11,106,234,8,12));
            g.setStroke(new BasicStroke(2));g.setColor(trim);
            if("MOON".equals(a.capePattern)) {
                Area crescent=new Area(new Ellipse2D.Float(34,77,60,76));
                crescent.subtract(new Area(new Ellipse2D.Float(49,68,58,72)));
                g.fill(crescent);star(g,91,83,7);star(g,92,137,4);
            } else if("STARS".equals(a.capePattern)) {
                Random random=new Random(0xA11E);
                for(int i=0;i<15;i++)star(g,24+random.nextInt(80),40+random.nextInt(167),3+random.nextInt(5));
                g.setStroke(new BasicStroke(.8f));g.drawLine(38,87,77,110);g.drawLine(77,110,58,160);
            } else if(!"PLAIN".equals(a.capePattern)) {
                Path2D head=new Path2D.Float();
                head.moveTo(64,72);head.curveTo(14,65,24,125,64,157);
                head.curveTo(104,125,114,65,64,72);head.closePath();g.fill(head);
                g.setColor(main.darker());
                Path2D eye=new Path2D.Float();eye.moveTo(39,98);eye.curveTo(51,96,58,109,59,118);
                eye.curveTo(47,119,39,110,39,98);eye.closePath();g.fill(eye);
                AffineTransform mirror=AffineTransform.getTranslateInstance(128,0);mirror.scale(-1,1);g.fill(mirror.createTransformedShape(eye));
                g.setColor(trim);star(g,64,181,9);
                g.setStroke(new BasicStroke(1.5f));g.drawLine(37,196,91,196);
            }
        } finally { g.dispose(); }
        return image;
    }

    private static void star(Graphics2D g,float x,float y,float size) {
        Path2D star=new Path2D.Float();star.moveTo(x,y-size);
        star.lineTo(x+size*.3,y-size*.3);star.lineTo(x+size,y);
        star.lineTo(x+size*.3,y+size*.3);star.lineTo(x,y+size);
        star.lineTo(x-size*.3,y+size*.3);star.lineTo(x-size,y);
        star.lineTo(x-size*.3,y-size*.3);star.closePath();g.fill(star);
    }
}
