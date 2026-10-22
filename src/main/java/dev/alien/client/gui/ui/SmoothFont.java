package dev.alien.client.gui.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * A bounded pair of antialiased, four-times-resolution Open Sans atlases.
 * FontRenderer is only an adapter for existing Forge text fields and layouts;
 * all glyph rasterization, measurement, formatting and drawing is independent.
 */
public final class SmoothFont extends FontRenderer {
    private static final float SAMPLE = 4f;
    private static final float BASELINE = 8f;
    private static final int ATLAS_SIZE = 1024;
    private static final int CELL_SIZE = 64;
    private static final String EXTRA = "\u2013\u2014\u2018\u2019\u201c\u201d\u2022\u2026\u203a\u2039\u2190\u2191\u2192\u2193\u2605\u2606\u2713\u2715\u25b8\u25be\u25a1\u25a0";
    private static SmoothFont instance;
    private final Atlas regular;
    private final Atlas semibold;

    public static SmoothFont get() {
        if (instance == null) {
            int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            try { instance = new SmoothFont(Minecraft.getMinecraft()); }
            finally { net.minecraft.client.renderer.GlStateManager.bindTexture(previous); }
        }
        return instance;
    }

    private SmoothFont(Minecraft mc) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), false);
        FONT_HEIGHT = 10;
        regular = new Atlas(mc, "regular");
        semibold = new Atlas(mc, "semibold");
    }

    @Override public int drawStringWithShadow(String text, float x, float y, int color) {
        return drawString(text, x, y, color, true);
    }

    @Override public int drawString(String text, int x, int y, int color) {
        return drawString(text, (float)x, (float)y, color, false);
    }

    @Override public int drawString(String text, float x, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return (int)x;
        if ((color & 0xfc000000) == 0) color |= 0xff000000;
        // Bind raw GL textures inside the attribute snapshot. The Minecraft
        // texture binding cache is never touched by these drawing passes.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT
                | GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            if (shadow) render(text, x + .45f, y + .6f, color, true);
            render(text, x, y, color, false);
        } finally { GL11.glPopAttrib(); }
        return (int)Math.ceil(x + width(text));
    }

    private void render(String text, float startX, float startY, int original, boolean shadow) {
        float x = startX, y = startY;
        boolean bold = false, italic = false, underline = false, strike = false;
        int color = original;
        Atlas bound = regular;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, bound.texture);
        GL11.glBegin(GL11.GL_QUADS);
        try {
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == '\u00a7' && i + 1 < text.length()) {
                    char code = Character.toLowerCase(text.charAt(++i));
                    int colorIndex = "0123456789abcdef".indexOf(code);
                    if (colorIndex >= 0) {
                        color = (original & 0xff000000) | getColorCode(code);
                        bold = italic = underline = strike = false;
                    } else if (code == 'l') bold = true;
                    else if (code == 'o') italic = true;
                    else if (code == 'n') underline = true;
                    else if (code == 'm') strike = true;
                    else if (code == 'r') { color = original; bold = italic = underline = strike = false; }
                    continue;
                }
                if (ch == '\n') { x = startX; y += FONT_HEIGHT; continue; }
                if (ch == '\r') continue;
                Atlas atlas = bold ? semibold : regular;
                if (atlas != bound) {
                    GL11.glEnd(); GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.texture);
                    GL11.glBegin(GL11.GL_QUADS); bound = atlas;
                }
                Glyph glyph = atlas.glyph(ch);
                rgba(color, shadow);
                if (glyph.width > 0) {
                    float left = x + glyph.bearingX, top = y + BASELINE + glyph.bearingY;
                    float right = left + glyph.width, bottom = top + glyph.height;
                    float shear = italic ? 1.1f : 0;
                    GL11.glTexCoord2f(glyph.u0, glyph.v0); GL11.glVertex3f(left + shear, top, 0);
                    GL11.glTexCoord2f(glyph.u0, glyph.v1); GL11.glVertex3f(left, bottom, 0);
                    GL11.glTexCoord2f(glyph.u1, glyph.v1); GL11.glVertex3f(right, bottom, 0);
                    GL11.glTexCoord2f(glyph.u1, glyph.v0); GL11.glVertex3f(right + shear, top, 0);
                }
                if (underline || strike) {
                    GL11.glEnd(); GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glBegin(GL11.GL_QUADS);
                    if (underline) decoration(x, y + 9, glyph.advance);
                    if (strike) decoration(x, y + 5, glyph.advance);
                    GL11.glEnd(); GL11.glEnable(GL11.GL_TEXTURE_2D);
                    GL11.glBegin(GL11.GL_QUADS);
                }
                x += glyph.advance;
            }
        } finally { GL11.glEnd(); }
    }

    private static void decoration(float x, float y, float width) {
        GL11.glVertex3f(x,y,0); GL11.glVertex3f(x,y+.5f,0);
        GL11.glVertex3f(x+width,y+.5f,0); GL11.glVertex3f(x+width,y,0);
    }

    private static void rgba(int color, boolean shadow) {
        float a = ((color >>> 24) & 255) / 255f;
        if (shadow) GL11.glColor4f(.015f,.02f,.035f,a*.55f);
        else GL11.glColor4f(((color >> 16) & 255)/255f,((color >> 8) & 255)/255f,(color & 255)/255f,a);
    }

    private float width(String text) {
        if (text == null) return 0;
        float current = 0, longest = 0;
        boolean bold = false;
        for (int i=0; i<text.length(); i++) {
            char ch=text.charAt(i);
            if (ch=='\u00a7' && i+1<text.length()) {
                char code=Character.toLowerCase(text.charAt(++i));
                if (code=='l') bold=true;
                else if (code=='r' || "0123456789abcdef".indexOf(code)>=0) bold=false;
            } else if(ch=='\n') {longest=Math.max(longest,current);current=0;}
            else if(ch!='\r') current+=(bold?semibold:regular).glyph(ch).advance;
        }
        return Math.max(longest,current);
    }

    @Override public int getStringWidth(String text) { return (int)Math.ceil(width(text)); }
    @Override public int getCharWidth(char ch) {
        if(ch=='\u00a7')return -1;
        if(ch=='\n'||ch=='\r')return 0;
        return (int)Math.ceil(regular.glyph(ch).advance);
    }
    @Override public String trimStringToWidth(String text,int width) { return trimStringToWidth(text,width,false); }
    @Override public String trimStringToWidth(String text,int maxWidth,boolean reverse) {
        if(text==null || maxWidth<=0)return "";
        if(reverse) {
            // Suffix widths are measured using the same glyph advances as drawing.
            int start=text.length();
            while(start>0 && width(text.substring(start-1))<=maxWidth)start--;
            if(start>0 && text.charAt(start-1)=='\u00a7')start++;
            return text.substring(Math.min(start,text.length()));
        }
        float used=0;boolean bold=false;int end=0;
        while(end<text.length()) {
            char ch=text.charAt(end);
            if(ch=='\u00a7' && end+1<text.length()) {
                char code=Character.toLowerCase(text.charAt(end+1));
                if(code=='l')bold=true;
                else if(code=='r'||"0123456789abcdef".indexOf(code)>=0)bold=false;
                end+=2;continue;
            }
            float step=(bold?semibold:regular).glyph(ch).advance;
            if(ch=='\n'||used+step>maxWidth)break;
            used+=step;end++;
        }
        return text.substring(0,end);
    }

    @Override public List<String> listFormattedStringToWidth(String text,int maxWidth) {
        List<String> result=new ArrayList<String>();
        if(text==null || text.isEmpty()){result.add("");return result;}
        for(String paragraph:text.split("\\n",-1)) {
            String rest=paragraph;
            if(rest.isEmpty()){result.add("");continue;}
            while(!rest.isEmpty()) {
                String fit=trimStringToWidth(rest,Math.max(1,maxWidth));
                if(fit.length()==rest.length()){result.add(rest);break;}
                int cut=fit.lastIndexOf(' ');
                if(cut<=0)cut=fit.length();
                // Always consume at least one real glyph for very narrow wraps.
                while(cut<rest.length() && (cut==0 || onlyFormats(rest.substring(0,cut))))cut++;
                String line=rest.substring(0,cut);result.add(line);
                int skip=cut;if(skip<rest.length()&&rest.charAt(skip)==' ')skip++;
                rest=FontRenderer.getFormatFromString(line)+rest.substring(skip);
                if(onlyFormats(rest))break;
            }
        }
        return result;
    }
    private static boolean onlyFormats(String value){return value.replaceAll("\u00a7.","").isEmpty();}
    @Override public void drawSplitString(String text,int x,int y,int width,int color) {
        for(String line:listFormattedStringToWidth(text,width)){drawString(line,x,y,color);y+=FONT_HEIGHT;}
    }
    @Override public int splitStringWidth(String text,int width){return listFormattedStringToWidth(text,width).size()*FONT_HEIGHT;}

    private static final class Atlas {
        final Glyph[] glyphs=new Glyph[65536];
        final int texture;
        Atlas(Minecraft mc,String weight) {
            BufferedImage image=new BufferedImage(ATLAS_SIZE,ATLAS_SIZE,BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics=image.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                Font font;
                try(InputStream stream=SmoothFont.class.getResourceAsStream("/assets/alienclient/fonts/opensans-"+weight+".ttf")) {
                    if(stream==null)throw new IllegalStateException("Missing bundled Open Sans "+weight);
                    font=Font.createFont(Font.TRUETYPE_FONT,stream).deriveFont(36f);
                } catch(Exception failure){throw new IllegalStateException("Cannot load bundled Open Sans",failure);}
                graphics.setFont(font);graphics.setColor(Color.WHITE);
                FontRenderContext context=graphics.getFontRenderContext();
                StringBuilder characters=new StringBuilder();
                for(char ch=32;ch<256;ch++)characters.append(ch);
                characters.append(EXTRA);
                for(int i=0;i<characters.length();i++) {
                    char ch=characters.charAt(i);
                    int cellX=(i%16)*CELL_SIZE,cellY=(i/16)*CELL_SIZE;
                    if(cellY+CELL_SIZE>ATLAS_SIZE)throw new IllegalStateException("Font atlas capacity exceeded");
                    if(!font.canDisplay(ch) && EXTRA.indexOf(ch)>=0) {
                        symbol(graphics,ch,cellX+4,cellY+4,"semibold".equals(weight));
                        glyphs[ch]=new Glyph(-.5f,-8f,9f,9f,8f,
                                cellX/(float)ATLAS_SIZE,cellY/(float)ATLAS_SIZE,
                                (cellX+36)/(float)ATLAS_SIZE,(cellY+36)/(float)ATLAS_SIZE);
                        continue;
                    }
                    GlyphVector vector=font.createGlyphVector(context,new char[]{ch});
                    java.awt.Rectangle bounds=vector.getPixelBounds(context,0,0);
                    int padding=2,w=bounds.width+padding*2,h=bounds.height+padding*2;
                    if(w>=CELL_SIZE || h>=CELL_SIZE)throw new IllegalStateException("Font glyph exceeds bounded atlas cell");
                    graphics.drawGlyphVector(vector,cellX+padding-bounds.x,cellY+padding-bounds.y);
                    glyphs[ch]=new Glyph((bounds.x-padding)/SAMPLE,(bounds.y-padding)/SAMPLE,
                            w/SAMPLE,h/SAMPLE,vector.getGlyphMetrics(0).getAdvanceX()/SAMPLE,
                            cellX/(float)ATLAS_SIZE,cellY/(float)ATLAS_SIZE,
                            (cellX+w)/(float)ATLAS_SIZE,(cellY+h)/(float)ATLAS_SIZE);
                }
            } finally {graphics.dispose();}
            // Creation uses Minecraft's texture uploader once. Restore its own
            // cache and actual binding together before any normal drawing.
            int previous=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            try {
                DynamicTexture dynamic=new DynamicTexture(image);
                ResourceLocation location=new ResourceLocation("alienclient","ui/font-"+weight);
                mc.getTextureManager().loadTexture(location,dynamic);
                texture=dynamic.getGlTextureId();
                net.minecraft.client.renderer.GlStateManager.bindTexture(texture);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL11.GL_CLAMP);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL11.GL_CLAMP);
            } finally {net.minecraft.client.renderer.GlStateManager.bindTexture(previous);}
        }
        Glyph glyph(char ch){Glyph result=glyphs[ch];return result==null?glyphs['?']:result;}

        /** Original vector symbols fill the few shapes absent from Open Sans. */
        private static void symbol(Graphics2D graphics,char ch,int x,int y,boolean bold) {
            AffineTransform previous=graphics.getTransform();
            java.awt.Stroke stroke=graphics.getStroke();
            try {
                graphics.translate(x,y);graphics.setStroke(new BasicStroke(bold?3.6f:3f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                if(ch>='\u2190' && ch<='\u2193') {
                    double angle=ch=='\u2190'?Math.PI:ch=='\u2191'?-Math.PI/2:ch=='\u2193'?Math.PI/2:0;
                    graphics.rotate(angle,14,14);
                    Path2D path=new Path2D.Float();path.moveTo(3,14);path.lineTo(25,14);
                    path.moveTo(17,6);path.lineTo(25,14);path.lineTo(17,22);graphics.draw(path);
                } else if(ch=='\u2605' || ch=='\u2606') {
                    Path2D path=new Path2D.Float();
                    for(int i=0;i<10;i++) {
                        double angle=-Math.PI/2+i*Math.PI/5, radius=(i&1)==0?12:5.3;
                        double px=14+Math.cos(angle)*radius,py=14+Math.sin(angle)*radius;
                        if(i==0)path.moveTo(px,py);else path.lineTo(px,py);
                    }
                    path.closePath();if(ch=='\u2605')graphics.fill(path);else graphics.draw(path);
                } else if(ch=='\u2713') {
                    Path2D path=new Path2D.Float();path.moveTo(4,15);path.lineTo(11,22);path.lineTo(25,6);graphics.draw(path);
                } else if(ch=='\u2715') {
                    graphics.drawLine(6,6,22,22);graphics.drawLine(22,6,6,22);
                } else if(ch=='\u25b8' || ch=='\u25be') {
                    if(ch=='\u25be')graphics.rotate(Math.PI/2,14,14);
                    Path2D path=new Path2D.Float();path.moveTo(8,4);path.lineTo(23,14);path.lineTo(8,24);path.closePath();graphics.fill(path);
                } else if(ch=='\u25a0') graphics.fillRoundRect(4,4,20,20,3,3);
                else graphics.drawRoundRect(4,4,20,20,3,3);
            } finally {graphics.setTransform(previous);graphics.setStroke(stroke);}
        }
    }
    private static final class Glyph {
        final float bearingX,bearingY,width,height,advance,u0,v0,u1,v1;
        Glyph(float x,float y,float w,float h,float advance,float u0,float v0,float u1,float v1){
            bearingX=x;bearingY=y;width=w;height=h;this.advance=advance;
            this.u0=u0;this.v0=v0;this.u1=u1;this.v1=v1;
        }
    }
}
