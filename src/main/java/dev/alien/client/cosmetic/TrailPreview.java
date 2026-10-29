package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/** Editor swatch using the same stops, texture and UV controls as world ribbons. */
final class TrailPreview {
    static String draw(CosmeticConfig.Accessory a,int x,int y,int w,int h) {
        Minecraft m=Minecraft.getMinecraft();TrailDesign d=a.trailDesign;
        int previous=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);String error="";
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_TEXTURE_BIT|GL11.GL_LIGHTING_BIT);
        try {
            GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glShadeModel(GL11.GL_SMOOTH);
            if(d.enabled){TrailTextures.Binding texture=TrailTextures.resolve(d);error=texture.error;GL11.glEnable(GL11.GL_TEXTURE_2D);m.getTextureManager().bindTexture(texture.location);GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL11.GL_TEXTURE_ENV_MODE,GL11.GL_MODULATE);}
            else GL11.glDisable(GL11.GL_TEXTURE_2D);
            boolean animate=AlienClient.get().config().animations&&a.animations&&!Ui.theme().reducedMotion;
            double phase=animate?(System.nanoTime()/1000000000d)%4096:0;
            GL11.glBegin(GL11.GL_QUAD_STRIP);
            try {
                for(int i=0;i<=96;i++){
                    float t=i/96f;int color=d.enabled?d.colorAt(t):a.color;
                    float radius=Math.min(h*.37f,a.trailWidth*55)*(d.enabled?d.widthAt(t):(.15f+.85f*t));
                    float alpha=a.trailOpacity*(d.enabled?d.opacityAt(t):t);
                    double cy=y+h*.5+Math.sin(t*Math.PI*2)*h*.13,u=t*3*d.tiling-phase*d.scroll;
                    GL11.glColor4f((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,alpha);
                    GL11.glTexCoord2d(u,0);GL11.glVertex2d(x+t*w,cy-radius);
                    GL11.glTexCoord2d(u,1);GL11.glVertex2d(x+t*w,cy+radius);
                }
            }finally{GL11.glEnd();}
        } finally {GL11.glPopAttrib();GlStateManager.bindTexture(previous);}
        return error;
    }
}
