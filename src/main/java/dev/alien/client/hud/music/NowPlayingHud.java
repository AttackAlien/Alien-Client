package dev.alien.client.hud.music;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import dev.alien.client.hud.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import java.nio.IntBuffer;

/** No file reads/decoding here. The artwork cache is bounded to one render-thread texture. */
public final class NowPlayingHud {
    private static final ResourceLocation COVER_SLOT=new ResourceLocation("alienclient","dynamic/now_playing");
    private static DynamicTexture uploaded;
    private static ResourceLocation texture;private static String coverHash="",track="";private static long trackAt;
    private static final IntBuffer SCISSOR=BufferUtils.createIntBuffer(16);
    private NowPlayingHud(){}
    private static HudConfig cfg(){return AlienClient.get().config().hud;}
    public static int height(){return "Card".equals(cfg().music.style)?68:46+(cfg().music.sourceLabel||cfg().music.time?12:0);}
    public static boolean visible(NowPlayingState state,boolean preview){return preview||(!cfg().music.hideInactive||state.active(System.currentTimeMillis()))&&(!cfg().music.hidePaused||!"paused".equals(state.status));}
    public static void maintenance(boolean wanted){NowPlayingState s=NowPlayingService.get().snapshot();if(!wanted||s.cover==null||!cfg().music.artwork)release();}
    private static void release(){
        if(uploaded!=null){uploaded.deleteGlTexture();java.util.Arrays.fill(uploaded.getTextureData(),0);uploaded=null;}
        texture=null;coverHash="";
    }
    public static void render(HudConfig.Widget widget,int sw,int sh,boolean preview){
        Minecraft mc=Minecraft.getMinecraft();NowPlayingState state=NowPlayingService.get().snapshot();if(!visible(state,preview))return;
        NowPlayingConfig c=cfg().music;long now=System.currentTimeMillis();boolean active=state.active(now);int[] b=HudManager.bounds("music",widget,sw,sh);
        int oldTexture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        boolean oldBlend=GL11.glIsEnabled(GL11.GL_BLEND),oldAlpha=GL11.glIsEnabled(GL11.GL_ALPHA_TEST),oldTexturing=GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        int oldSrc=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),oldDst=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),oldSrcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),oldDstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GlStateManager.pushMatrix();
        try{
            GlStateManager.translate(b[0],b[1],0);GlStateManager.scale(widget.scale,widget.scale,1);GlStateManager.enableTexture2D();GlStateManager.enableAlpha();GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            int w=b[4],h=b[5],accent=widget.customColor?widget.color:UltimateHud.themeAccent();float alpha=cfg().opacity*widget.opacity;
            if(!"Transparent".equals(c.style)&&cfg().background){
                if(cfg().shadows)Ui.shadow(0,0,w,h,7,.22f*widget.opacity);
                int base=Ui.alpha(UltimateHud.surface(),alpha);
                if(cfg().gradient)Ui.gradient(0,0,w,h,7,base,Ui.alpha(Ui.mix(UltimateHud.surface(),accent,.07f),alpha));else Ui.rounded(0,0,w,h,7,base);
            }
            int artSize="Card".equals(c.style)?46:30,artY=(h-artSize)/2,tx=c.artwork?artSize+17:29;
            if(c.artwork){
                Ui.rounded(8,artY,artSize,artSize,5,Ui.alpha(Ui.mix(UltimateHud.surface(),accent,.14f),widget.opacity));
                if(active&&state.cover!=null){
                    if(texture==null||!state.artwork.equals(coverHash)){release();uploaded=new DynamicTexture(state.cover);texture=COVER_SLOT;mc.getTextureManager().loadTexture(texture,uploaded);coverHash=state.artwork;}
                    GlStateManager.color(1,1,1,widget.opacity);mc.getTextureManager().bindTexture(texture);
                    Gui.drawScaledCustomSizeModalRect(8,artY,0,0,state.cover.getWidth(),state.cover.getHeight(),artSize,artSize,state.cover.getWidth(),state.cover.getHeight());
                    GlStateManager.color(1,1,1,1);
                }else note(8+artSize/2-5,artY+artSize/2-7,accent,widget.opacity);
            }else note(9,13,accent,widget.opacity);
            String title=active?(state.title.isEmpty()?"Sem título":state.title):emptyTitle(state.status);
            String artist=active?state.artist:emptyDetail(state.status);
            FontRenderer font=cfg().modernFont?Ui.font():mc.fontRendererObj;
            int available=w-tx-10,titleY="Card".equals(c.style)?12:9;
            String key=state.title+"\n"+state.artist+"\n"+state.source;if(!key.equals(track)){track=key;trackAt=System.nanoTime();}
            line(font,title,tx,titleY,available,Ui.alpha(UltimateHud.textColor(),widget.opacity),c.marquee&&AlienClient.get().config().animations,b,widget.scale,sw,sh);
            if(c.artist&&!artist.isEmpty())line(font,artist,tx,titleY+13,available,Ui.alpha(UltimateHud.muted(),widget.opacity),false,b,widget.scale,sw,sh);
            if(active&&c.sourceLabel&&!state.source.isEmpty()){
                String label=state.source+("paused".equals(state.status)?" / pausado":"");
                if("Card".equals(c.style))line(font,label,tx,39,available,Ui.alpha(UltimateHud.muted(),widget.opacity*.8f),false,b,widget.scale,sw,sh);
            }else if(active&&"paused".equals(state.status)){Gui.drawRect(w-8,8,w-6,15,Ui.alpha(accent,widget.opacity));Gui.drawRect(w-4,8,w-2,15,Ui.alpha(accent,widget.opacity));}
            float progress=state.fraction(now);
            if(active&&c.progress&&progress>=0){
                int y=h-6;Ui.rounded(tx,y,available,2,1,Ui.alpha(Ui.mix(UltimateHud.surface(),accent,.18f),widget.opacity));
                if(progress>0)Ui.rounded(tx,y,available*progress,2,1,Ui.alpha(accent,widget.opacity));
            }
            if(active&&c.time&&state.position(now)>=0){
                String clock=NowPlayingState.clock(state.position(now))+(state.durationMs>0?" / "+NowPlayingState.clock(state.durationMs):"");
                if("Card".equals(c.style))line(font,clock,tx,49,available,Ui.alpha(UltimateHud.muted(),widget.opacity*.8f),false,b,widget.scale,sw,sh);
            }
            if(active&&!"Card".equals(c.style)&&(c.sourceLabel||c.time)){
                String footer=c.sourceLabel?state.source:"";
                if(c.time&&state.position(now)>=0)footer+=(footer.isEmpty()?"":" / ")+NowPlayingState.clock(state.position(now))+(state.durationMs>0?" / "+NowPlayingState.clock(state.durationMs):"");
                if("paused".equals(state.status))footer+=(footer.isEmpty()?"":" / ")+"pausado";
                line(font,footer,tx,h-21,available,Ui.alpha(UltimateHud.muted(),widget.opacity*.8f),false,b,widget.scale,sw,sh);
            }
        }finally{
            GlStateManager.popMatrix();GL11.glPopAttrib();GlStateManager.bindTexture(oldTexture);
            // Restore Minecraft's cache as well as actual GL state after the attribute pop.
            GlStateManager.tryBlendFuncSeparate(oldSrc,oldDst,oldSrcAlpha,oldDstAlpha);
            if(oldBlend)GlStateManager.enableBlend();else GlStateManager.disableBlend();
            if(oldAlpha)GlStateManager.enableAlpha();else GlStateManager.disableAlpha();
            if(oldTexturing)GlStateManager.enableTexture2D();else GlStateManager.disableTexture2D();
            GlStateManager.resetColor();
        }
    }
    private static void line(FontRenderer font,String value,int x,int y,int width,int color,boolean scroll,int[] bounds,float scale,int sw,int sh){
        if(width<=0)return;int textWidth=font.getStringWidth(value);if(textWidth<=width){font.drawString(value,x,y,color);return;}
        if(!scroll){font.drawString(font.trimStringToWidth(value,Math.max(0,width-8))+"...",x,y,color);return;}
        Minecraft mc=Minecraft.getMinecraft();boolean clipped=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);int[] previous=null;
        if(clipped){SCISSOR.clear();GL11.glGetInteger(GL11.GL_SCISSOR_BOX,SCISSOR);previous=new int[]{SCISSOR.get(0),SCISSOR.get(1),SCISSOR.get(2),SCISSOR.get(3)};}
        int left=Math.round((bounds[0]+x*scale)*mc.displayWidth/(float)sw),bottom=mc.displayHeight-Math.round((bounds[1]+(y+11)*scale)*mc.displayHeight/(float)sh);
        int right=Math.round((bounds[0]+(x+width)*scale)*mc.displayWidth/(float)sw),top=mc.displayHeight-Math.round((bounds[1]+y*scale)*mc.displayHeight/(float)sh);
        if(previous!=null){left=Math.max(left,previous[0]);bottom=Math.max(bottom,previous[1]);right=Math.min(right,previous[0]+previous[2]);top=Math.min(top,previous[1]+previous[3]);}
        GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(left,bottom,Math.max(0,right-left),Math.max(0,top-bottom));
        double elapsed=Math.max(0,(System.nanoTime()-trackAt)/1e9-1.5);float offset=(float)(elapsed*23%(textWidth+34));
        font.drawString(value,x-offset,y,color,false);font.drawString(value,x-offset+textWidth+34,y,color,false);
        if(previous!=null)GL11.glScissor(previous[0],previous[1],previous[2],previous[3]);else GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    private static void note(int x,int y,int color,float opacity){int c=Ui.alpha(color,opacity);Ui.rounded(x+5,y,2,13,1,c);Gui.drawRect(x+5,y,x+12,y+2,c);Ui.rounded(x,y+10,7,5,2,c);}
    public static String emptyTitle(String status){if("launcher_required".equals(status))return "Abra pelo Alien Launcher";if("unsupported".equals(status))return "Mídia indisponível neste sistema";if("connecting".equals(status))return "Conectando mídia...";if("error".equals(status)||"disconnected".equals(status))return "Aguardando mídia do Windows";return "Nenhuma mídia em reprodução";}
    private static String emptyDetail(String status){return "launcher_required".equals(status)?"Para conectar a mídia do Windows":"Spotify / navegador compatível";}
}
