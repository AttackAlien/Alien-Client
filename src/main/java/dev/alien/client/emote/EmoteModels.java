package dev.alien.client.emote;

import dev.alien.client.AlienClient;
import java.util.List;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Retains existing player renderers/layers; only vanilla player and armor models gain pose hooks. */
final class EmoteModels {
    @SuppressWarnings({"rawtypes","unchecked"}) static boolean prepare(RenderPlayer renderer,AbstractClientPlayer player){
        if(renderer.getMainModel() instanceof PlayerModel)return true;
        if(renderer.getMainModel().getClass()!=ModelPlayer.class)return false;
        try{
            ReflectionHelper.setPrivateValue(RendererLivingEntity.class,renderer,new PlayerModel("slim".equals(player.getSkinType())),"mainModel","field_77045_g");
            List layers=ReflectionHelper.getPrivateValue(RendererLivingEntity.class,renderer,"layerRenderers","field_177097_h");
            for(int i=0;i<layers.size();i++){
                if(layers.get(i).getClass()==LayerBipedArmor.class)layers.set(i,new ArmorLayer(renderer));
                else if(layers.get(i).getClass()==LayerCustomHead.class)layers.set(i,new LayerCustomHead(renderer.getMainModel().bipedHead));
                else if(layers.get(i) instanceof LayerHeldItem)layers.set(i,new HeldItemLayer((LayerRenderer)layers.get(i)));
            }
            return true;
        }catch(RuntimeException failure){AlienClient.LOG.warn("Emote pose hook unavailable",failure);return false;}
    }
    private static final class Rig {
        final ModelRenderer[] parts;final float[] points=new float[18];final EmotePose pose=new EmotePose();
        Rig(ModelBiped model){parts=new ModelRenderer[]{model.bipedHead,model.bipedBody,model.bipedRightArm,model.bipedLeftArm,model.bipedRightLeg,model.bipedLeftLeg};for(int i=0;i<6;i++){points[i*3]=parts[i].rotationPointX;points[i*3+1]=parts[i].rotationPointY;points[i*3+2]=parts[i].rotationPointZ;}}
        void reset(){for(int i=0;i<6;i++){ModelRenderer p=parts[i];p.rotateAngleX=p.rotateAngleY=p.rotateAngleZ=0;p.rotationPointX=points[i*3];p.rotationPointY=points[i*3+1];p.rotationPointZ=points[i*3+2];}}
        void apply(Entity entity,float age,boolean armor){
            EmoteManager manager=AlienClient.get().emotes();if(manager==null||!manager.modelPose(entity,age,pose))return;
            for(int i=0;i<6;i++){parts[i].rotateAngleX+=pose.angles[i*3];parts[i].rotateAngleY+=pose.angles[i*3+1];parts[i].rotateAngleZ+=pose.angles[i*3+2];}
            manager.modelApplied(armor);
        }
    }
    private static final class PlayerModel extends ModelPlayer {
        final Rig rig;
        PlayerModel(boolean slim){super(0,slim);rig=new Rig(this);}
        @Override public void setRotationAngles(float a,float b,float age,float yaw,float pitch,float scale,Entity entity){
            rig.reset();super.setRotationAngles(a,b,age,yaw,pitch,scale,entity);rig.apply(entity,age,false);
            copyModelAngles(bipedHead,bipedHeadwear);copyModelAngles(bipedBody,bipedBodyWear);
            copyModelAngles(bipedRightArm,bipedRightArmwear);copyModelAngles(bipedLeftArm,bipedLeftArmwear);
            copyModelAngles(bipedRightLeg,bipedRightLegwear);copyModelAngles(bipedLeftLeg,bipedLeftLegwear);
        }
    }
    private static final class ArmorModel extends ModelBiped {
        final Rig rig;
        ArmorModel(float scale){super(scale);rig=new Rig(this);}
        @Override public void setRotationAngles(float a,float b,float age,float yaw,float pitch,float scale,Entity entity){rig.reset();super.setRotationAngles(a,b,age,yaw,pitch,scale,entity);rig.apply(entity,age,true);copyModelAngles(bipedHead,bipedHeadwear);}
    }
    private static final class ArmorLayer extends LayerBipedArmor {
        ArmorLayer(RendererLivingEntity<?> renderer){super(renderer);}
        @Override protected void initArmor(){modelLeggings=new ArmorModel(.5f);modelArmor=new ArmorModel(1);}
    }
    private static final class HeldItemLayer implements LayerRenderer<net.minecraft.entity.EntityLivingBase> {
        final LayerRenderer<net.minecraft.entity.EntityLivingBase> original;
        @SuppressWarnings("unchecked") HeldItemLayer(LayerRenderer<?> original){this.original=(LayerRenderer<net.minecraft.entity.EntityLivingBase>)original;}
        @Override public void doRenderLayer(net.minecraft.entity.EntityLivingBase entity,float a,float b,float partial,float age,float yaw,float pitch,float scale){if(!AlienClient.get().emotes().hideItem(entity))original.doRenderLayer(entity,a,b,partial,age,yaw,pitch,scale);}
        @Override public boolean shouldCombineTextures(){return original.shouldCombineTextures();}
    }
}
