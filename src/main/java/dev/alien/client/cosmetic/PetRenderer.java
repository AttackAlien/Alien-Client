package dev.alien.client.cosmetic;

import dev.alien.client.cosmetic.pet.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/** Original credited meshes, one batch per companion, no runtime asset downloads. */
final class PetRenderer {
    private static final SkinnedPetModel MODEL=loadAlien();private static final RigidPetModel TUNG=loadTung();
    private static final SkinnedPetModel.Pose POSE=MODEL.createPose();
    private static final int[] COLORS=new int[Math.max(MODEL.vertexCount(),TUNG.vertexCount())];
    private static String lastModel="";
    private PetRenderer(){}
    private static SkinnedPetModel loadAlien(){try{return SkinnedPetModel.load();}catch(java.io.IOException e){throw new IllegalStateException("Cannot load bundled Quaternius alien",e);}}
    private static RigidPetModel loadTung(){try{return RigidPetModel.load();}catch(java.io.IOException e){throw new IllegalStateException("Cannot load bundled CalinnHotCake Sahur",e);}}
    static int vertices(){return MODEL.vertexCount();}static int triangles(){return MODEL.triangleCount();}static int clips(){return MODEL.animationNames().size();}
    static int tungVertices(){return TUNG.vertexCount();}static int tungTriangles(){return TUNG.triangleCount();}static String lastModel(){return lastModel;}
    static void draw(PetDesign c,PetPose p){
        boolean tung=c.tung();int vertices=tung?TUNG.vertexCount():MODEL.vertexCount();
        if(!tung)MODEL.sample(p.animationTime,p.movement,p.rest,p.reaction,POSE);
        for(int i=0;i<vertices;i++){
            int material=tung?TUNG.material(i):MODEL.material(i),base=tung?tungColor(c,material):material==0?c.accent:material==1?c.color:material==2?0x3A2F2F:material==3?0x272727:0xD3D3D3;
            int n=i*3;float nx=tung?TUNG.normal(n):POSE.normals[n],ny=tung?TUNG.normal(n+1):POSE.normals[n+1],nz=tung?TUNG.normal(n+2):POSE.normals[n+2];
            float light=Math.max(.48f,Math.min(1.06f,.76f-nx*.14f+ny*.22f+nz*.20f));
            boolean eye=tung?material==4:material==3;float shine=eye?(float)Math.pow(Math.max(0,-nx*.31f+ny*.79f+nz*.53f),22)*.20f:0;
            int r=Math.min(255,Math.round(((base>>16)&255)*light+shine*255)),g=Math.min(255,Math.round(((base>>8)&255)*light+shine*255)),b=Math.min(255,Math.round((base&255)*light+shine*255));
            COLORS[i]=(r<<16)|(g<<8)|b;
        }
        GL11.glPushMatrix();try{
            double size=c.size*("Shoulder".equals(c.mode)?.65:1);GL11.glScaled(size,size,size);GL11.glTranslated(0,p.bob,0);
            // Sahur has no authored rig: only honest whole-model hover/lean reactions.
            if(tung){GL11.glRotated(p.tilt,1,0,0);GL11.glRotated(Math.sin(p.animationTime*2.5)*p.reaction*8,0,1,0);}
            Tessellator tess=Tessellator.getInstance();WorldRenderer buffer=tess.getWorldRenderer();buffer.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_COLOR);
            int elements=tung?TUNG.indexCount():MODEL.indexCount();
            for(int i=0;i<elements;i++){
                int v=tung?TUNG.index(i):MODEL.index(i),n=v*3,color=COLORS[v];
                double x=tung?TUNG.position(n):POSE.positions[n],y=tung?TUNG.position(n+1):POSE.positions[n+1],z=tung?TUNG.position(n+2):POSE.positions[n+2];
                buffer.pos(x,y,z).color((color>>16)&255,(color>>8)&255,color&255,255).endVertex();
            }
            tess.draw();lastModel=c.model;
        }finally{GL11.glPopMatrix();}
    }
    private static int tungColor(PetDesign c,int material){
        if("Original".equals(c.preset)||material>=3)return TUNG.color(material);
        int color=material==2?c.color:c.accent;if(material!=1)return color;
        return ((int)(((color>>16)&255)*.67f)<<16)|((int)(((color>>8)&255)*.67f)<<8)|(int)((color&255)*.67f);
    }
}
