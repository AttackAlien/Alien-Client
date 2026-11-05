package dev.alien.client.cosmetic;

import dev.alien.client.cosmetic.pet.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/** Actual imported Quaternius mesh and original rig clips, rendered in one batch. */
final class PetRenderer {
    private static final SkinnedPetModel MODEL=load();
    private static final SkinnedPetModel.Pose POSE=MODEL.createPose();
    private static final int[] COLORS=new int[MODEL.vertexCount()];
    private PetRenderer(){}
    private static SkinnedPetModel load(){try{return SkinnedPetModel.load();}catch(java.io.IOException e){throw new IllegalStateException("Cannot load bundled Quaternius alien",e);}}
    static int vertices(){return MODEL.vertexCount();}static int triangles(){return MODEL.triangleCount();}static int clips(){return MODEL.animationNames().size();}
    static void draw(PetDesign c,PetPose p){
        MODEL.sample(p.animationTime,p.movement,p.rest,p.reaction,POSE);
        for(int i=0;i<MODEL.vertexCount();i++){
            int material=MODEL.material(i),base=material==0?c.accent:material==1?c.color:material==2?0x3A2F2F:material==3?0x272727:0xD3D3D3;
            int n=i*3;float nx=POSE.normals[n],ny=POSE.normals[n+1],nz=POSE.normals[n+2];
            float light=Math.max(.48f,Math.min(1.06f,.76f-nx*.14f+ny*.22f+nz*.20f));
            float shine=material==3?(float)Math.pow(Math.max(0,-nx*.31f+ny*.79f+nz*.53f),22)*.20f:0;
            int r=Math.min(255,Math.round(((base>>16)&255)*light+shine*255)),g=Math.min(255,Math.round(((base>>8)&255)*light+shine*255)),b=Math.min(255,Math.round((base&255)*light+shine*255));
            COLORS[i]=(r<<16)|(g<<8)|b;
        }
        GL11.glPushMatrix();try{
            double size=c.size*("Shoulder".equals(c.mode)?.65:1);GL11.glScaled(size,size,size);GL11.glTranslated(0,p.bob,0);
            Tessellator tess=Tessellator.getInstance();WorldRenderer buffer=tess.getWorldRenderer();buffer.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_COLOR);
            for(int i=0;i<MODEL.indexCount();i++){
                int v=MODEL.index(i),n=v*3,color=COLORS[v];
                buffer.pos(POSE.positions[n],POSE.positions[n+1],POSE.positions[n+2]).color((color>>16)&255,(color>>8)&255,color&255,255).endVertex();
            }
            tess.draw();
        }finally{GL11.glPopMatrix();}
    }
}
