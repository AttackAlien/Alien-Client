package dev.alien.client.cosmetic;

import dev.alien.client.cosmetic.mesh.*;
import dev.alien.client.cosmetic.pet.*;
import org.lwjgl.opengl.GL11;

/** Uses the existing bounded mesh display-list cache. No textures or models downloaded at runtime. */
final class PetRenderer {
    private PetRenderer(){}
    private static void mesh(Mesh mesh,int color){CosmeticMeshRenderer.draw(mesh,color,color);}
    private static void part(Mesh mesh,double x,double y,double z,int color){GL11.glPushMatrix();try{GL11.glTranslated(x/16,y/16,z/16);mesh(mesh,color);}finally{GL11.glPopMatrix();}}
    static void draw(PetDesign c,PetPose p){
        GL11.glPushMatrix();try{
            double size=c.size*("Shoulder".equals(c.mode)?.65:1);GL11.glScaled(size,size,size);GL11.glTranslated(0,p.bob,0);GL11.glRotated(p.tilt,1,0,0);
            mesh(PetMeshes.BODY,0xCDD5E8);part(PetMeshes.DOT,0,.7,1.35,c.accent);
            for(int side=-1;side<=1;side+=2){GL11.glPushMatrix();try{GL11.glTranslated(side*2.0/16,1.0/16,0);GL11.glRotated(side<0?p.rightArm:p.leftArm,0,0,1);part(PetMeshes.ARM,0,-1.05,0,c.color);part(PetMeshes.TIP,0,-2,0,c.accent);}finally{GL11.glPopMatrix();}
                GL11.glPushMatrix();try{GL11.glTranslated(side*.8/16,-2.5/16,.2/16);GL11.glRotated(side*p.feet,1,0,0);mesh(PetMeshes.FOOT,0x42445F);}finally{GL11.glPopMatrix();}}
            GL11.glPushMatrix();try{
                GL11.glTranslated(0,3.9/16,0);GL11.glRotated(p.headYaw,0,1,0);GL11.glRotated(p.headTilt,0,0,1);mesh(PetMeshes.HEAD,c.color);
                for(int side=-1;side<=1;side+=2){
                    GL11.glPushMatrix();try{GL11.glTranslated(side*1.05/16,.1/16,1.98/16);GL11.glRotated(-side*23,0,0,1);GL11.glScaled(1,p.eyes,1);mesh(PetMeshes.EYE,0x11182C);part(PetMeshes.DOT,-side*.1,.25,.27,c.accent);part(PetMeshes.DOT,-side*.3,.55,.3,0xF1FDFF);}finally{GL11.glPopMatrix();}
                    GL11.glPushMatrix();try{GL11.glTranslated(side*1.05/16,2.6/16,0);GL11.glRotated(-side*(16+p.antenna),0,0,1);part(PetMeshes.STEM,0,.7,0,c.color);part(PetMeshes.TIP,0,1.65,0,c.accent);}finally{GL11.glPopMatrix();}
                }
            }finally{GL11.glPopMatrix();}
            part(PetMeshes.RING,0,-4,0,c.accent);
        }finally{GL11.glPopMatrix();}
    }
}
