package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

/** Original procedural accessories rendered only by the local client. */
public final class CosmeticManager {
    private boolean installed;
    private int rendered;
    private int tick;

    public void install() {
        if (installed) return;
        Minecraft mc = Minecraft.getMinecraft();
        for (RenderPlayer renderer : mc.getRenderManager().getSkinMap().values()) renderer.addLayer(new AccessoryLayer(renderer));
        installed = true;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) rendered = 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || mc.isGamePaused()) return;
        tick++;
        if ((tick & 3) != 0 || !config().enabled) return;
        int emitted = 0;
        int budget = Math.min(32, Math.min(Math.max(0, AlienClient.get().config().performance.maxCosmetics),
                AlienClient.get().config().performance.maxParticles));
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (emitted >= budget) break;
            CosmeticConfig.Accessory accessory = forPlayer(player);
            if (accessory == null || !accessory.enabled || player.isInvisibleToPlayer(mc.thePlayer) || !inRange(player)) continue;
            boolean moving = player.motionX * player.motionX + player.motionZ * player.motionZ > .0025;
            if (accessory.aura || (accessory.trail && moving)) {
                double angle = tick * .09 + emitted;
                double radius = accessory.aura ? .5 : .15;
                double y = accessory.aura ? player.posY + .3 + .4 * Math.sin(angle) : player.posY + .1;
                int color = accessory.color;
                mc.theWorld.spawnParticle(EnumParticleTypes.REDSTONE,
                        player.posX + Math.cos(angle) * radius, y, player.posZ + Math.sin(angle) * radius,
                        Math.max(.001, ((color >> 16) & 255) / 255d), ((color >> 8) & 255) / 255d, (color & 255) / 255d);
                emitted++;
            }
        }
    }

    private CosmeticConfig config() { return AlienClient.get().config().cosmetics; }

    private CosmeticConfig.Accessory forPlayer(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && player.getUniqueID().equals(mc.thePlayer.getUniqueID())) return config().self;
        return config().players.get(player.getUniqueID().toString());
    }

    private boolean inRange(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        float distance = Math.min(config().maxDistance, AlienClient.get().config().performance.effectDistance);
        return mc.thePlayer != null && player.getDistanceSqToEntity(mc.thePlayer) <= distance * distance;
    }

    private final class AccessoryLayer implements LayerRenderer<AbstractClientPlayer> {
        private final RenderPlayer renderer;
        private final Map<String, ModelRenderer> models = new HashMap<String, ModelRenderer>();

        AccessoryLayer(RenderPlayer renderer) {
            this.renderer = renderer;
            models.put("JAPANESE", model(new float[][] {
                {-8,-9,-8,16,1,16}, {-6,-10,-6,12,1,12}, {-4,-11,-4,8,1,8}, {-2,-12,-2,4,1,4}}));
            models.put("SAMURAI", model(new float[][] {
                {-5,-9,-5,10,2,10}, {-4,-11,-4,8,2,8}, {-7,-8,-2,2,4,5}, {5,-8,-2,2,4,5}}));
            models.put("SAMURAI_TRIM", model(new float[][] {{-5,-13,-4,1,5,1},{4,-13,-4,1,5,1},{-3,-10,-5,6,1,1}}));
            models.put("CROWN", model(new float[][] {
                {-5,-9,-5,10,1,1}, {-5,-9,4,10,1,1}, {-5,-9,-4,1,1,8}, {4,-9,-4,1,1,8},
                {-5,-12,-5,2,3,2}, {3,-12,-5,2,3,2}, {-1,-13,-5,2,4,1}, {-5,-12,3,2,3,2}, {3,-12,3,2,3,2}}));
            models.put("KITSUNE", model(new float[][] {{-4,-7,-5,8,6,1},{-4,-10,-5,2,3,1},{2,-10,-5,2,3,1},{-1,-4,-6,2,2,1}}));
            models.put("KITSUNE_TRIM", model(new float[][] {{-3,-6,-6,2,1,1},{1,-6,-6,2,1,1},{-1,-3,-7,2,1,1}}));
            models.put("FUTURE", model(new float[][] {{-4,-7,-5,8,4,1},{-5,-6,-4,1,2,3},{4,-6,-4,1,2,3}}));
            models.put("FUTURE_TRIM", model(new float[][] {{-3,-6,-6,6,1,1},{-1,-3,-6,2,1,1}}));
            models.put("GLASSES", model(new float[][] {{-4,-6,-5,3,2,1},{1,-6,-5,3,2,1},{-1,-6,-5,2,1,1},{-5,-6,-4,1,1,5},{4,-6,-4,1,1,5}}));
            models.put("CAPE", model(new float[][] {{-5,1,3,10,15,1}}));
            models.put("BACKPACK", model(new float[][] {{-4,2,2,8,9,4}}));
            models.put("BACKPACK_TRIM", model(new float[][] {{-3,7,6,6,3,1},{-4,0,3,1,3,2},{3,0,3,1,3,2}}));
            models.put("WING_LEFT", model(new float[][] {{1,2,3,6,2,1},{4,4,3,7,2,1},{7,6,3,7,2,1},{10,8,3,6,2,1},{12,10,3,3,2,1}}));
            models.put("WING_RIGHT", model(new float[][] {{-7,2,3,6,2,1},{-11,4,3,7,2,1},{-14,6,3,7,2,1},{-16,8,3,6,2,1},{-15,10,3,3,2,1}}));
            models.put("CHARM", model(new float[][] {{-1,10,-1,2,3,2},{-2,13,-2,4,4,4}}));
        }

        @Override public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbAmount,
                float partialTicks, float age, float yaw, float pitch, float scale) {
            Minecraft mc = Minecraft.getMinecraft();
            boolean preview = CosmeticStudioScreen.drawingPreview;
            if (!config().enabled || mc.thePlayer == null || player.isSpectator() ||
                    player.isInvisibleToPlayer(mc.thePlayer) || (!preview && !inRange(player))) return;
            CosmeticConfig.Accessory a = forPlayer(player);
            if (a == null || !a.enabled || (!preview && rendered >= AlienClient.get().config().performance.maxCosmetics)) return;
            if (!preview) rendered++;
            GlStateManager.pushMatrix();
            try {
                GlStateManager.disableTexture2D();
                if (player.isSneaking()) GlStateManager.translate(0, .2f, 0);
                GlStateManager.pushMatrix();
                try {
                renderer.getMainModel().bipedHead.postRender(.0625f);
                transform(a);
                render(a.hat, a.color);
                render(a.hat + "_TRIM", a.secondaryColor);
                render(a.face, a.color);
                render(a.face + "_TRIM", a.secondaryColor);
                CosmeticAssets.Imported imported = CosmeticAssets.get(a.modelFile, a.textureFile);
                if (imported != null) {
                    if (imported.texture != null) {
                        GlStateManager.enableTexture2D();
                        mc.getTextureManager().bindTexture(imported.texture);
                        GlStateManager.color(1, 1, 1, 1);
                    } else color(a.color);
                    imported.model.render(.0625f);
                    GlStateManager.disableTexture2D();
                }
                } finally { GlStateManager.popMatrix(); }
                GlStateManager.pushMatrix();
                try {
                renderer.getMainModel().bipedBody.postRender(.0625f);
                transform(a);
                if ("WINGS".equals(a.back)) {
                    float flap = (float) Math.sin(age * .12f) * 12;
                    GlStateManager.pushMatrix();
                    try { GlStateManager.rotate(flap, 0, 1, 0); render("WING_LEFT", a.color); }
                    finally { GlStateManager.popMatrix(); }
                    GlStateManager.pushMatrix();
                    try { GlStateManager.rotate(-flap, 0, 1, 0); render("WING_RIGHT", a.secondaryColor); }
                    finally { GlStateManager.popMatrix(); }
                } else {
                    if ("CAPE".equals(a.back)) GlStateManager.rotate(-8 - Math.min(35, limbAmount * 40), 1, 0, 0);
                    render(a.back, a.color); render(a.back + "_TRIM", a.secondaryColor);
                }
                } finally { GlStateManager.popMatrix(); }
                if (a.charm) {
                    GlStateManager.pushMatrix();
                    try {
                        renderer.getMainModel().bipedRightArm.postRender(.0625f);
                        transform(a); render("CHARM", a.secondaryColor);
                    } finally { GlStateManager.popMatrix(); }
                }
            } finally {
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableTexture2D();
                mc.getTextureManager().bindTexture(player.getLocationSkin());
                GlStateManager.popMatrix();
            }
        }

        private void transform(CosmeticConfig.Accessory a) {
            GlStateManager.translate(a.offsetX / 16f, a.offsetY / 16f, a.offsetZ / 16f);
            GlStateManager.rotate(a.rotationX, 1, 0, 0);
            GlStateManager.rotate(a.rotationY, 0, 1, 0);
            GlStateManager.rotate(a.rotationZ, 0, 0, 1);
            GlStateManager.scale(a.scale, a.scale, a.scale);
        }

        private void render(String name, int color) {
            ModelRenderer model = models.get(name);
            if (model != null) { color(color); model.render(.0625f); }
        }

        @Override public boolean shouldCombineTextures() { return false; }
    }

    private static void color(int rgb) {
        GlStateManager.color(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, 1);
    }

    private static ModelRenderer model(float[][] boxes) {
        ModelBase base = new ModelBase() { };
        ModelRenderer renderer = new ModelRenderer(base, 0, 0);
        for (float[] b : boxes) renderer.addBox(b[0], b[1], b[2], (int)b[3], (int)b[4], (int)b[5]);
        return renderer;
    }
}
