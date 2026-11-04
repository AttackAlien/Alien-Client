package dev.alien.client.scene;
import dev.alien.client.AlienClient;
import net.minecraft.client.entity.AbstractClientPlayer;import net.minecraft.client.renderer.entity.*;import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.GlStateManager;import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
/** Uses vanilla poses, transforms and item renderer. Only the selected material changes. */
final class ScenePlayerRenderer extends RenderPlayer {
    static long glassBodies,glassHeld;
    ScenePlayerRenderer(RenderManager manager,boolean slim){super(manager,slim);layerRenderers.clear();addLayer(new MaterialItemLayer(this));addLayer(new ScenePlayerLayer(this));}
    @Override protected void renderModel(AbstractClientPlayer p,float a,float b,float c,float d,float e,float scale){
        if("Glass".equals(AlienClient.get().config().scenes.mode)){
            SceneShaders.glass(()->{super.renderModel(p,a,b,c,d,e,scale);glassBodies++;});
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,GL11.GL_LINE);GL11.glLineWidth(1.1f);GL11.glColor4f(.85f,.92f,1,.65f);mainModel.render(p,a,b,c,d,e,scale);}finally{GL11.glPopAttrib();}
        }else{
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glDepthMask(false);GlStateManager.color(1,1,1,.14f);super.renderModel(p,a,b,c,d,e,scale);}finally{GL11.glPopAttrib();GlStateManager.color(1,1,1,1);}
        }
    }
    private static final class MaterialItemLayer extends LayerHeldItem {
        MaterialItemLayer(RendererLivingEntity<?> r){super(r);}
        @Override public void doRenderLayer(EntityLivingBase e,float a,float b,float partial,float age,float yaw,float pitch,float scale){
            if("Glass".equals(AlienClient.get().config().scenes.mode))SceneShaders.glass(()->{super.doRenderLayer(e,a,b,partial,age,yaw,pitch,scale);if(e.getHeldItem()!=null)glassHeld++;});else super.doRenderLayer(e,a,b,partial,age,yaw,pitch,scale);
        }
    }
}
