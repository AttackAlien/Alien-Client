package dev.alien.client.world;

import dev.alien.client.gui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

/** Original vector palette previews, not screenshots or simulated game evidence. */
final class WorldStudioVisuals {
    static final String[] THEMES={"Nebula","Galaxy","Aurora","Japanese","Crystal","Sakura"};
    private WorldStudioVisuals() { }
    static void frame(int width,int height,String title,String subtitle,String icon) {
        Ui.background(width,height);Ui.card(12,10,width-24,height-20);
        Ui.icon(icon,23,22,16,Ui.accent());Ui.text(title,47,19,Ui.theme().text);
        Ui.text(subtitle,47,33,Ui.theme().muted);
        Ui.rounded(20,51,77,height-91,6,Ui.alpha(Ui.theme().surface,.66f));
    }
    static void panorama(float x,float y,float w,float h,WorldConfig c,boolean labels) {
        Ui.gradient(x,y,w,h,7,0xff000000|c.skyColor(c.skyTop),0xff000000|c.skyColor(c.skyBottom));
        if(!c.skyboxFile.isEmpty() && WorldManager.importedPreview!=null) {
            net.minecraft.client.renderer.texture.ITextureObject texture=Minecraft.getMinecraft().getTextureManager().getTexture(WorldManager.importedPreview);
            if(texture!=null) {
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_TEXTURE_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_COLOR_BUFFER_BIT);
                try {
                    GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture.getGlTextureId());GL11.glColor4f(1,1,1,1);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glTexCoord2f(0,0);GL11.glVertex2f(x,y);GL11.glTexCoord2f(0,1);GL11.glVertex2f(x,y+h);
                    GL11.glTexCoord2f(1,1);GL11.glVertex2f(x+w,y+h);GL11.glTexCoord2f(1,0);GL11.glVertex2f(x+w,y);
                    GL11.glEnd();
                } finally {GL11.glPopAttrib();}
            }
        } else {
            int stars=Math.min(26,c.stars/25);
            for(int i=0;i<stars;i++) {
                float sx=x+5+Math.floorMod(i*79+17,Math.max(1,(int)w-10));
                float sy=y+4+Math.floorMod(i*41+9,Math.max(1,(int)(h*.62f)-5));
                Ui.rounded(sx,sy,i%5==0?1.4f:.7f,i%5==0?1.4f:.7f,.5f,0xc4eff6ff);
            }
            if("Aurora".equals(c.theme)) for(int i=0;i<12;i++) {
                float yy=y+h*.25f+(float)Math.sin(i*.5)*h*.12f;
                Ui.gradient(x+i*w/12,yy,w/12+1,h*.28f,0,0x2d65edcf,0x0065edcf);
            }
            if(c.sunMoon)Ui.rounded(x+w*.77f,y+h*.17f,Math.max(3,h*.16f*c.moonSize),Math.max(3,h*.16f*c.moonSize),h,0xbbe8e5f5);
            if("Vanilla".equals(c.cloudMode))for(int i=0;i<3;i++) {
                float cx=x+w*(.08f+i*.27f);Ui.rounded(cx,y+h*.43f,w*.17f,Math.max(2,h*.07f),2,0x1effffff);
            }
        }
        if(labels) {
            Ui.rounded(x+7,y+7,Ui.font().getStringWidth(c.skyboxFile.isEmpty()?c.theme:"Imported panorama")+12,17,4,0x710b0c17);
            Ui.text(c.skyboxFile.isEmpty()?c.theme:"Imported panorama",x+13,y+11,0xfff4f0ff);
            Ui.text("Palette preview",x+8,y+h-14,0xdaf4f0ff);
        }
    }
    static final class SkyCard extends GuiButton {
        final String theme; private final boolean selected;private float hover;
        SkyCard(int id,int x,int y,int w,int h,String theme,boolean selected){super(id,x,y,w,h,theme);this.theme=theme;this.selected=selected;}
        @Override public void drawButton(Minecraft mc,int mx,int my) {
            if(!visible)return;boolean over=mx>=xPosition&&mx<xPosition+width&&my>=yPosition&&my<yPosition+height;
            hover=Ui.animate(hover,over?1:0);WorldConfig palette=new WorldConfig();palette.applyTheme(theme);
            Ui.gradient(xPosition,yPosition,width,height,5,0xff000000|palette.skyTop,0xff000000|palette.skyBottom);
            if(selected)Ui.rounded(xPosition+3,yPosition+4,2,height-8,1,Ui.accent());
            if(hover>0)Ui.rounded(xPosition,yPosition,width,height,5,Ui.alpha(0xffffff,hover*.08f));
            Ui.text(theme,xPosition+9,yPosition+(height-10)/2f,0xfff3f2fa);
            if(selected)Ui.text("\u2713",xPosition+width-16,yPosition+(height-10)/2f,0xffffffff);
        }
    }
}
