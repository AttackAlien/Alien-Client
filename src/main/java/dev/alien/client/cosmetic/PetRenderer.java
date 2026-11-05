package dev.alien.client.cosmetic;

import dev.alien.client.cosmetic.mesh.*;
import dev.alien.client.cosmetic.pet.*;
import org.lwjgl.opengl.GL11;

/** Shared smooth geometry and a bounded material cache; animation changes transforms only. */
final class PetRenderer {
    private static final int INK=0x111A30,SUIT=0xE3E9F4,BOOT=0x444F6B;
    private PetRenderer(){}
    private static void mesh(Mesh mesh,int color){CosmeticMeshRenderer.drawMascot(mesh,color,color);}
    private static void part(Mesh mesh,double x,double y,double z,double sx,double sy,double sz,int color){
        GL11.glPushMatrix();try{GL11.glTranslated(x/16,y/16,z/16);GL11.glScaled(sx,sy,sz);mesh(mesh,color);}finally{GL11.glPopMatrix();}
    }
    static void draw(PetDesign c,PetPose p){
        GL11.glPushMatrix();try{
            double size=c.size*("Shoulder".equals(c.mode)?.65:1);
            GL11.glScaled(size,size,size);GL11.glTranslated(0,p.bob,0);GL11.glRotated(p.tilt,1,0,0);
            part(PetMeshes.BODY,0,0,0,1,p.breath,1,SUIT);
            part(PetMeshes.COLLAR,0,1.4,0,1,1,1,c.accent);
            part(PetMeshes.COLLAR,0,-.85,0,1.33,.6,1.33,BOOT);
            part(PetMeshes.DOT,0,.7,1.29,1,1,.28,c.accent);
            for(int side=-1;side<=1;side+=2){
                arm(side,c,p);
                GL11.glPushMatrix();try{
                    GL11.glTranslated(side*.8/16,-2.5/16,.2/16);GL11.glRotated(side*p.feet,1,0,0);
                    mesh(PetMeshes.FOOT,BOOT);part(PetMeshes.COLLAR,0,.55,0,.5,.55,.7,c.accent);
                }finally{GL11.glPopMatrix();}
            }
            GL11.glPushMatrix();try{
                GL11.glTranslated(0,4.0/16,0);GL11.glRotated(p.headYaw,0,1,0);
                GL11.glRotated(p.headPitch,1,0,0);GL11.glRotated(p.headTilt,0,0,1);mesh(PetMeshes.HEAD,c.color);
                mesh(PetMeshes.SMILE,INK);
                for(int side=-1;side<=1;side+=2){
                    eye(side,c,p);
                    GL11.glPushMatrix();try{
                        GL11.glTranslated(side*1.05/16,2.5/16,0);GL11.glRotated(-side*(16+p.antenna),0,0,1);
                        part(PetMeshes.STEM,0,.7,0,1,1,1,c.color);
                        GL11.glTranslated(0,1.1/16,0);GL11.glRotated(-side*p.antenna*.65,0,0,1);
                        part(PetMeshes.STEM,0,.28,0,.85,.55,.85,c.color);
                        part(PetMeshes.TIP,0,.83,0,1,1,1,c.accent);
                    }finally{GL11.glPopMatrix();}
                }
            }finally{GL11.glPopMatrix();}
            part(PetMeshes.RING,0,-3.75,0,1,1,1,c.accent);
        }finally{GL11.glPopMatrix();}
    }
    private static void arm(int side,PetDesign c,PetPose p){
        GL11.glPushMatrix();try{
            GL11.glTranslated(side*1.8/16,.9/16,0);
            GL11.glRotated(side<0?p.rightArm:p.leftArm,0,0,1);
            part(PetMeshes.ARM,0,-.55,0,.9,.55,.9,c.color);
            GL11.glTranslated(0,-1.05/16,0);
            GL11.glRotated(side*(side<0?p.rightElbow:p.leftElbow),0,0,1);
            part(PetMeshes.ARM,0,-.48,0,.82,.5,.82,c.color);
            GL11.glTranslated(0,-1.05/16,0);GL11.glRotated(side<0?p.wrist:0,0,0,1);
            part(PetMeshes.TIP,0,0,0,1.1,1.1,1.1,c.accent);
        }finally{GL11.glPopMatrix();}
    }
    private static void eye(int side,PetDesign c,PetPose p){
        GL11.glPushMatrix();try{
            GL11.glTranslated(side*1.05/16,.12/16,1.97/16);
            GL11.glRotated(-side*20,0,0,1);GL11.glScaled(1,p.eyes,1);
            // A shallow rim and inset lens replace the old protruding stacks of spheres.
            part(PetMeshes.EYE,0,0,0,1.1,1.08,.72,lighten(c.color));
            part(PetMeshes.EYE,0,0,.08,1,1,.70,INK);
            part(PetMeshes.DOT,p.gazeX,p.gazeY,.29,1,1.55,.16,c.accent);
            part(PetMeshes.DOT,p.gazeX,p.gazeY,.33,.52,.95,.09,INK);
            part(PetMeshes.DOT,-.18+p.gazeX,.35+p.gazeY,.30,.46,.46,.14,0xF5FFFF);
            part(PetMeshes.DOT,.13+p.gazeX,-.20+p.gazeY,.30,.19,.19,.10,0xF5FFFF);
        }finally{GL11.glPopMatrix();}
    }
    private static int lighten(int c){return (((((c>>16)&255)*4+255)/5)<<16)|(((((c>>8)&255)*4+255)/5)<<8)|(((c&255)*4+255)/5);}
}
