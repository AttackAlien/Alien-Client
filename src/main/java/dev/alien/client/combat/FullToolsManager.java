package dev.alien.client.combat;

import dev.alien.client.AlienClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Original vanilla-controller implementations; never alter reach, hitboxes, or server rules. */
public final class FullToolsManager {
    private Object world,profile;
    private int originalSlot=-1,ownedSlot=-1,useKey,pendingSplash;
    private float previousPitch;
    private boolean using,drinking,ownsPitch;
    private EntityPlayer ownedPlayer;
    private long bridgeAt,potionAt,useAt;
    private static boolean busy;
    public static boolean busy(){return busy;}
    private static long placedBlocks,foodsStarted,potionsStarted;
    public static long placedBlocks(){return placedBlocks;}
    public static long foodsStarted(){return foodsStarted;}
    public static long potionsStarted(){return potionsStarted;}
    private static boolean physical(int code){return code<0?Mouse.isButtonDown(code+100):code>0&&code<Keyboard.KEYBOARD_SIZE&&Keyboard.isKeyDown(code);}
    private void stop(Minecraft m){
        if(using)KeyBinding.setKeyBindState(useKey,physical(useKey));
        if(m.thePlayer!=null&&m.thePlayer==ownedPlayer){if(originalSlot>=0&&m.thePlayer.inventory.currentItem==ownedSlot)m.thePlayer.inventory.currentItem=originalSlot;
            if(ownsPitch&&Math.abs(m.thePlayer.rotationPitch-90)<.1f)m.thePlayer.rotationPitch=previousPitch;}
        busy=false;originalSlot=ownedSlot=-1;ownedPlayer=null;using=drinking=ownsPitch=false;pendingSplash=0;
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        Minecraft m=Minecraft.getMinecraft();CombatConfig c=AlienClient.get().config().combat;
        if(world!=m.theWorld||profile!=c){stop(m);world=m.theWorld;profile=c;}
        if(m.thePlayer==null||m.theWorld==null||m.currentScreen!=null||m.thePlayer.isDead||!c.enabled
                ||!AlienClient.get().combatSession().permits(m.theWorld,c,m.isSingleplayer())){stop(m);return;}
        FullToolsConfig f=c.tools;
        if(using){
            if(System.nanoTime()-useAt>6000000000L){stop(m);return;}
            if(m.thePlayer.inventory.currentItem!=ownedSlot||(!drinking&&!f.autoFood)||(drinking&&!f.autoPotion)){stop(m);return;}
            ItemStack held=m.thePlayer.getHeldItem();
            if(pendingSplash>0){
                if(--pendingSplash==0){m.playerController.sendUseItem(m.thePlayer,m.theWorld,held);potionAt=System.nanoTime();stop(m);}
                return;
            }
            boolean finished=held==null||(!drinking&&m.thePlayer.getFoodStats().getFoodLevel()>=f.foodThreshold)
                    ||(drinking&&(!(held.getItem() instanceof ItemPotion)||m.thePlayer.getHealth()>f.potionHealth));
            if(finished){stop(m);potionAt=System.nanoTime();}else KeyBinding.setKeyBindState(useKey,true);
            return;
        }
        if(!m.thePlayer.isUsingItem()&&!physical(m.gameSettings.keyBindUseItem.getKeyCode())){
            if(f.autoPotion&&m.thePlayer.getHealth()<=f.potionHealth&&System.nanoTime()-potionAt>1200000000L){
                int slot=potionSlot(m,f);if(slot>=0){begin(m,slot,true);return;}
            }
            if(f.autoFood&&m.thePlayer.canEat(false)&&m.thePlayer.getFoodStats().getFoodLevel()<f.foodThreshold){
                int slot=foodSlot(m);if(slot>=0){begin(m,slot,false);return;}
            }
        }
        if(f.autoSprint&&m.thePlayer.movementInput.moveForward>0&&!m.thePlayer.isSneaking()
                &&!m.thePlayer.isCollidedHorizontally&&m.thePlayer.getFoodStats().getFoodLevel()>6)m.thePlayer.setSprinting(true);
        if(f.antiAim&&!c.aimAssist&&!f.aimbot&&!Mouse.isButtonDown(0))m.thePlayer.rotationYaw=net.minecraft.util.MathHelper.wrapAngleTo180_float(m.thePlayer.rotationYaw+f.spinSpeed/20f);
        if(f.bridge&&!m.thePlayer.isSpectator()&&!m.thePlayer.isUsingItem())bridge(m);
    }
    private void begin(Minecraft m,int slot,boolean potion){
        ownedPlayer=m.thePlayer;originalSlot=m.thePlayer.inventory.currentItem;ownedSlot=slot;m.thePlayer.inventory.currentItem=slot;
        using=true;busy=true;useAt=System.nanoTime();drinking=potion;useKey=m.gameSettings.keyBindUseItem.getKeyCode();
        if(potion){potionsStarted++;ItemStack held=m.thePlayer.getHeldItem();
            if(ItemPotion.isSplash(held.getMetadata())){previousPitch=m.thePlayer.rotationPitch;ownsPitch=true;m.thePlayer.rotationPitch=90;pendingSplash=2;return;}
        }else foodsStarted++;
        KeyBinding.setKeyBindState(useKey,true);
    }
    private static int foodSlot(Minecraft m){
        int best=-1,heal=0;
        for(int i=0;i<9;i++){ItemStack s=m.thePlayer.inventory.getStackInSlot(i);
            if(s!=null&&s.getItem() instanceof ItemFood&&s.getItem()!=net.minecraft.init.Items.rotten_flesh
                    &&s.getItem()!=net.minecraft.init.Items.spider_eye&&s.getItem()!=net.minecraft.init.Items.poisonous_potato
                    &&!(s.getItem()==net.minecraft.init.Items.fish&&s.getMetadata()==3)){int value=((ItemFood)s.getItem()).getHealAmount(s);if(value>heal){best=i;heal=value;}}
        }return best;
    }
    private static int potionSlot(Minecraft m,FullToolsConfig f){
        for(int i=0;i<9;i++){ItemStack s=m.thePlayer.inventory.getStackInSlot(i);
            if(s==null||!(s.getItem() instanceof ItemPotion)||(!f.splashPotions&&ItemPotion.isSplash(s.getMetadata())))continue;
            java.util.List<PotionEffect> effects=((ItemPotion)s.getItem()).getEffects(s);
            if(effects==null)continue;
            for(PotionEffect effect:effects)if(effect.getPotionID()==Potion.heal.id
                    ||effect.getPotionID()==Potion.regeneration.id&&!m.thePlayer.isPotionActive(Potion.regeneration))return i;
        }return -1;
    }
    private void bridge(Minecraft m){
        if(System.nanoTime()-bridgeAt<100000000L)return;
        BlockPos target=new BlockPos(m.thePlayer.posX,Math.floor(m.thePlayer.getEntityBoundingBox().minY)-1,m.thePlayer.posZ);
        if(!m.theWorld.isAirBlock(target))return;
        int slot=-1;
        for(int i=0;i<9;i++){ItemStack s=m.thePlayer.inventory.getStackInSlot(i);
            if(s!=null&&s.stackSize>0&&s.getItem() instanceof ItemBlock&&((ItemBlock)s.getItem()).getBlock().isFullCube()){slot=i;break;}}
        if(slot<0)return;
        for(EnumFacing side:EnumFacing.values()){
            BlockPos neighbor=target.offset(side);
            if(!m.theWorld.getBlockState(neighbor).getBlock().isFullCube())continue;
            EnumFacing face=side.getOpposite();
            Vec3 hit=new Vec3(neighbor.getX()+.5+face.getFrontOffsetX()*.5,neighbor.getY()+.5+face.getFrontOffsetY()*.5,neighbor.getZ()+.5+face.getFrontOffsetZ()*.5);
            if(m.thePlayer.getPositionEyes(1).squareDistanceTo(hit)>20.25)continue;
            int old=m.thePlayer.inventory.currentItem;m.thePlayer.inventory.currentItem=slot;
            try{if(m.playerController.onPlayerRightClick(m.thePlayer,m.theWorld,m.thePlayer.getHeldItem(),neighbor,face,hit)){
                m.thePlayer.swingItem();placedBlocks++;bridgeAt=System.nanoTime();return;
            }}finally{m.thePlayer.inventory.currentItem=old;}
        }
    }
}
