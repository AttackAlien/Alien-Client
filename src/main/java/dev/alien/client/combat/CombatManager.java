package dev.alien.client.combat;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.edition.BuildEdition;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

/**
 * Client tick aiming/click helpers. Uses vanilla player controller calls;
 * never edits reach, combat packets, entity hitboxes, or server world state.
 * Disabled by default, and entirely unavailable in legacy Legit builds.
 */
public final class CombatManager {
    private long lastClick;
    private EntityPlayer lastTarget;

    @SubscribeEvent public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase!=TickEvent.Phase.END || !BuildEdition.ALLOW_FULL) return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc.theWorld==null||mc.thePlayer==null||mc.currentScreen!=null||mc.thePlayer.isDead) {
            lastTarget=null;return;
        }
        ClientConfig root=AlienClient.get().config();
        CombatConfig c=root.combat;
        if(c==null||!c.enabled){lastTarget=null;return;}
        boolean held=Mouse.isButtonDown(0);
        if(c.aimAssist && (!c.requireAttackHeld || held)) {
            EntityPlayer target=selectTarget(mc,c);
            lastTarget=target;
            if(target!=null) {
                float[] aim=CombatMath.lookAt(mc.thePlayer.posX,mc.thePlayer.posY+mc.thePlayer.getEyeHeight(),
                        mc.thePlayer.posZ,target.posX,target.posY+target.getEyeHeight(),target.posZ);
                // Tick-only, bounded, gradual orientation; no silent aim/packet spoof.
                mc.thePlayer.rotationYaw=CombatMath.interpolate(mc.thePlayer.rotationYaw,aim[0],c.aimStrength);
                mc.thePlayer.rotationPitch=Math.max(-90F,Math.min(90F,
                        CombatMath.interpolate(mc.thePlayer.rotationPitch,aim[1],c.aimStrength)));
            }
        } else lastTarget=null;
        // Repeated clicks require the physical attack button and a vanilla
        // ray-traced entity target within 3 blocks (not a KillAura).
        if(c.clickAssist && held && mc.objectMouseOver!=null
                && mc.objectMouseOver.typeOfHit==MovingObjectPosition.MovingObjectType.ENTITY
                && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
            EntityPlayer target=(EntityPlayer)mc.objectMouseOver.entityHit;
            if(target!=mc.thePlayer && !target.isDead && target.getHealth()>0F
                    && mc.thePlayer.getDistanceToEntity(target)<=3F
                    && (!c.visibleTargetsOnly || mc.thePlayer.canEntityBeSeen(target))) {
                long now=System.nanoTime();
                if(now-lastClick>=CombatMath.intervalNanos(c.clicksPerSecond)) {
                    lastClick=now;
                    mc.thePlayer.swingItem();
                    mc.playerController.attackEntity(mc.thePlayer,target);
                }
            }
        }
    }

    private EntityPlayer selectTarget(Minecraft mc,CombatConfig c) {
        EntityPlayer best=null;
        float bestScore=Float.MAX_VALUE;
        for(EntityPlayer other:mc.theWorld.playerEntities) {
            if(other==mc.thePlayer||other.isDead||other.getHealth()<=0F ||other.isSpectator()
                    ||other.isInvisible()||mc.thePlayer.getDistanceToEntity(other)>c.targetRange)continue;
            if(c.visibleTargetsOnly && !mc.thePlayer.canEntityBeSeen(other))continue;
            float[] angles=CombatMath.lookAt(mc.thePlayer.posX,mc.thePlayer.posY+mc.thePlayer.getEyeHeight(),
                    mc.thePlayer.posZ,other.posX,other.posY+other.getEyeHeight(),other.posZ);
            float score=CombatMath.angularDistance(mc.thePlayer.rotationYaw,mc.thePlayer.rotationPitch,angles[0],angles[1]);
            if(score<=c.aimFov/2F&&score<bestScore){bestScore=score;best=other;}
        }
        return best;
    }
}
