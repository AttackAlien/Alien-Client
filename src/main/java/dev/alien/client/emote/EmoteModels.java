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
    static float bend(ModelRenderer part){return part instanceof ArticulatedLimb?((ArticulatedLimb)part).bend:0;}
    static void hand(ModelBiped model,float scale){if(model.bipedRightArm instanceof ArticulatedLimb)((ArticulatedLimb)model.bipedRightArm).handTransform(scale);}
    private static void wrap(ModelBiped m,boolean skin,boolean slim,float inflation){
        m.bipedRightArm=new ArticulatedLimb(m,m.bipedRightArm,40,16,slim?-2:-3,true,slim?3:4,inflation);
        m.bipedLeftArm=new ArticulatedLimb(m,m.bipedLeftArm,skin?32:40,skin?48:16,-1,true,slim?3:4,inflation);
        m.bipedRightLeg=new ArticulatedLimb(m,m.bipedRightLeg,0,16,-2,false,4,inflation);
        m.bipedLeftLeg=new ArticulatedLimb(m,m.bipedLeftLeg,skin?16:0,skin?48:16,-2,false,4,inflation);
        if(skin){ModelPlayer p=(ModelPlayer)m;p.bipedRightArmwear=new ArticulatedLimb(p,p.bipedRightArmwear,40,32,slim?-2:-3,true,slim?3:4,.25f);
            p.bipedLeftArmwear=new ArticulatedLimb(p,p.bipedLeftArmwear,48,48,-1,true,slim?3:4,.25f);
            p.bipedRightLegwear=new ArticulatedLimb(p,p.bipedRightLegwear,0,32,-2,false,4,.25f);p.bipedLeftLegwear=new ArticulatedLimb(p,p.bipedLeftLegwear,0,48,-2,false,4,.25f);}
    }
    private static final class Rig {
        final ModelRenderer[] parts,joints;final float[] points=new float[18];final EmotePose pose=new EmotePose();
        Rig(ModelBiped model){parts=new ModelRenderer[]{model.bipedHead,model.bipedBody,model.bipedRightArm,model.bipedLeftArm,model.bipedRightLeg,model.bipedLeftLeg};joints=model instanceof ModelPlayer?new ModelRenderer[]{model.bipedRightArm,model.bipedLeftArm,model.bipedRightLeg,model.bipedLeftLeg,((ModelPlayer)model).bipedRightArmwear,((ModelPlayer)model).bipedLeftArmwear,((ModelPlayer)model).bipedRightLegwear,((ModelPlayer)model).bipedLeftLegwear}:new ModelRenderer[]{model.bipedRightArm,model.bipedLeftArm,model.bipedRightLeg,model.bipedLeftLeg};for(int i=0;i<6;i++){points[i*3]=parts[i].rotationPointX;points[i*3+1]=parts[i].rotationPointY;points[i*3+2]=parts[i].rotationPointZ;}}
        void reset(){for(ModelRenderer joint:joints)((ArticulatedLimb)joint).bend=0;for(int i=0;i<6;i++){ModelRenderer p=parts[i];p.rotateAngleX=p.rotateAngleY=p.rotateAngleZ=0;p.rotationPointX=points[i*3];p.rotationPointY=points[i*3+1];p.rotationPointZ=points[i*3+2];}}
        void apply(Entity entity,float age,boolean armor){
            EmoteManager manager=AlienClient.get().emotes();if(manager==null||!manager.modelPose(entity,age,pose))return;
            for(int i=0;i<6;i++){parts[i].rotateAngleX+=pose.angles[i*3];parts[i].rotateAngleY+=pose.angles[i*3+1];parts[i].rotateAngleZ+=pose.angles[i*3+2];}
            if(AlienClient.get().config().emotes.articulated)for(int i=0;i<joints.length;i++)((ArticulatedLimb)joints[i]).bend=pose.bends[i%4];
            manager.modelApplied(armor);
        }
    }
    private static final class PlayerModel extends ModelPlayer {
        final Rig rig;
        PlayerModel(boolean slim){super(0,slim);wrap(this,true,slim,0);rig=new Rig(this);}
        @Override public void postRenderArm(float scale){super.postRenderArm(scale);hand(this,scale);}
        @Override public void setRotationAngles(float a,float b,float age,float yaw,float pitch,float scale,Entity entity){
            rig.reset();super.setRotationAngles(a,b,age,yaw,pitch,scale,entity);rig.apply(entity,age,false);
            copyModelAngles(bipedHead,bipedHeadwear);copyModelAngles(bipedBody,bipedBodyWear);
            copyModelAngles(bipedRightArm,bipedRightArmwear);copyModelAngles(bipedLeftArm,bipedLeftArmwear);
            copyModelAngles(bipedRightLeg,bipedRightLegwear);copyModelAngles(bipedLeftLeg,bipedLeftLegwear);
        }
    }
    private static final class ArmorModel extends ModelBiped {
        final Rig rig;
        ArmorModel(float scale){super(scale);wrap(this,false,false,scale);rig=new Rig(this);}
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
