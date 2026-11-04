package dev.alien.client.emote;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;

/** Vanilla-compatible limb root; only active emotes draw the two skinned joint segments. */
final class ArticulatedLimb extends ModelRenderer {
    private final ModelRenderer upper,lower;
    private final float hinge;
    float bend;static long drawn;
    ArticulatedLimb(ModelBase model,ModelRenderer original,int u,int v,float x,boolean arm,int width,float inflation){
        super(model,original.boxName);int index=model.boxList.indexOf(original);
        cubeList.addAll(original.cubeList);childModels=original.childModels;mirror=original.mirror;
        ModelBase.copyModelAngles(original,this);offsetX=original.offsetX;offsetY=original.offsetY;offsetZ=original.offsetZ;showModel=original.showModel;isHidden=original.isHidden;
        hinge=arm?4:6;
        upper=new ModelRenderer(model,u,v);upper.mirror=mirror;upper.addBox(x,arm?-2:0,-2,width,6,4,inflation);
        lower=new ModelRenderer(model,u,v+6);lower.mirror=mirror;lower.addBox(x,0,-2,width,6,4,inflation);lower.rotationPointY=hinge;
        // Helpers are not candidates for the vanilla embedded-arrow layer.
        model.boxList.remove(upper);model.boxList.remove(lower);model.boxList.remove(this);if(index>=0)model.boxList.set(index,this);
    }
    @Override public void render(float scale){
        if(isHidden||!showModel)return;
        if(Math.abs(bend)<.0001f){super.render(scale);return;}
        drawn++;GlStateManager.pushMatrix();try{
            GlStateManager.translate(offsetX,offsetY,offsetZ);GlStateManager.translate(rotationPointX*scale,rotationPointY*scale,rotationPointZ*scale);
            if(rotateAngleZ!=0)GlStateManager.rotate(rotateAngleZ*57.29578f,0,0,1);if(rotateAngleY!=0)GlStateManager.rotate(rotateAngleY*57.29578f,0,1,0);if(rotateAngleX!=0)GlStateManager.rotate(rotateAngleX*57.29578f,1,0,0);
            upper.render(scale);lower.rotateAngleX=bend;lower.render(scale);if(childModels!=null)for(ModelRenderer child:childModels)child.render(scale);
        }finally{GlStateManager.popMatrix();}
    }
    void handTransform(float scale){if(Math.abs(bend)<.0001f)return;GlStateManager.translate(0,hinge*scale,0);GlStateManager.rotate(bend*57.29578f,1,0,0);GlStateManager.translate(0,-hinge*scale,0);}
}
