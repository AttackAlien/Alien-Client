package dev.alien.client.hud;

import dev.alien.client.AlienClient;
import dev.alien.client.performance.PerformanceEngine;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Informational HUD only; this class never changes input, player state, or packets. */
public final class HudManager {
    private static final Deque<Long> LEFT = new ArrayDeque<Long>();
    private static final Deque<Long> RIGHT = new ArrayDeque<Long>();
    private static final SimpleDateFormat CLOCK = new SimpleDateFormat("HH:mm:ss");

    @SubscribeEvent
    public void mouse(InputEvent.MouseInputEvent event) {
        // Keep click history bounded even when the CPS widget or the whole HUD is disabled.
        count(LEFT);
        count(RIGHT);
        if (!Mouse.getEventButtonState()) return;
        if (Mouse.getEventButton() == 0) LEFT.addLast(System.nanoTime());
        if (Mouse.getEventButton() == 1) RIGHT.addLast(System.nanoTime());
    }

    @SubscribeEvent
    public void overlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        HudConfig config = config();
        if (mc.thePlayer == null || !config.enabled || mc.currentScreen instanceof HudEditorScreen) return;
        if (config.hideWithDebug && mc.gameSettings.showDebugInfo) return;
        ScaledResolution resolution = new ScaledResolution(mc);
        for (Map.Entry<String, HudConfig.Widget> entry : config.widgets.entrySet()) {
            if (entry.getValue() != null && entry.getValue().enabled && known(entry.getKey()))
                render(entry.getKey(), entry.getValue(), resolution.getScaledWidth(), resolution.getScaledHeight(), false);
        }
    }

    static boolean known(String id) {
        return "fps".equals(id) || "cps".equals(id) || "keys".equals(id) || "coordinates".equals(id)
            || "ping".equals(id) || "clock".equals(id) || "inventory".equals(id) || "armor".equals(id)
            || "performance".equals(id) || "text".equals(id);
    }

    static HudConfig config() {
        HudConfig config = AlienClient.get().config().hud;
        config.ensureDefaults();
        return config;
    }

    public static int[] bounds(String id, HudConfig.Widget widget, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        int w = 96, h = 20;
        if ("keys".equals(id)) { w = 76; h = 76; }
        else if ("inventory".equals(id)) { w = 168; h = 60; }
        else if ("armor".equals(id)) { w = 92; h = 82; }
        else if ("performance".equals(id)) { w = 158; h = 54; }
        else if ("coordinates".equals(id)) { w = 158; h = 20; }
        else if ("text".equals(id)) w = Math.max(20, mc.fontRendererObj.getStringWidth(config().customText) + 12);
        int width = Math.round(w * widget.scale), height = Math.round(h * widget.scale);
        int x = Math.max(0, Math.min(Math.round(widget.x * screenWidth), Math.max(0, screenWidth - width)));
        int y = Math.max(0, Math.min(Math.round(widget.y * screenHeight), Math.max(0, screenHeight - height)));
        return new int[] { x, y, width, height, w, h };
    }

    public static void render(String id, HudConfig.Widget widget, int sw, int sh, boolean preview) {
        Minecraft mc = Minecraft.getMinecraft();
        int[] b = bounds(id, widget, sw, sh);
        GlStateManager.pushMatrix();
        GlStateManager.translate(b[0], b[1], 0);
        GlStateManager.scale(widget.scale, widget.scale, 1f);
        GlStateManager.enableBlend();
        if (config().background) Gui.drawRect(0, 0, b[4], b[5], 0xb5161825);
        Gui.drawRect(0, 0, 2, b[5], 0xff000000 | AlienClient.get().config().accentColor);
        if ("fps".equals(id)) text(Minecraft.getDebugFPS() + " FPS", 7, 6, 0xffeeeeff);
        else if ("cps".equals(id)) text(count(LEFT) + " / " + count(RIGHT) + " CPS", 7, 6, 0xffeeeeff);
        else if ("ping".equals(id)) text(ping(mc) + " ms", 7, 6, 0xffeeeeff);
        else if ("clock".equals(id)) text(CLOCK.format(new Date()), 7, 6, 0xffeeeeff);
        else if ("coordinates".equals(id)) {
            String value = mc.thePlayer == null ? "XYZ 0 / 64 / 0" : String.format("XYZ %.0f / %.0f / %.0f", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            text(value, 7, 6, 0xffeeeeff);
        } else if ("text".equals(id)) text(config().customText, 7, 6, 0xffeeeeff);
        else if ("keys".equals(id)) {
            key(mc.gameSettings.keyBindForward, "W", 28, 3, 22, 22);
            key(mc.gameSettings.keyBindLeft, "A", 4, 27, 22, 22);
            key(mc.gameSettings.keyBindBack, "S", 28, 27, 22, 22);
            key(mc.gameSettings.keyBindRight, "D", 52, 27, 22, 22);
            key(mc.gameSettings.keyBindJump, "SPACE", 4, 51, 70, 21);
        } else if ("performance".equals(id)) {
            PerformanceEngine metrics = AlienClient.get().performance();
            if (AlienClient.get().config().performance.diagnostics) {
                text(String.format("Frame %.1f ms", metrics.frameTimeMs()), 7, 6, 0xffeeeeff);
                text(String.format("p95 %.1f ms", metrics.percentile95Ms()), 7, 22, 0xffb6bdd1);
                text("Heap " + metrics.usedMemoryMb() + " MB", 7, 38, 0xffb6bdd1);
            } else {
                text("Diagnostics disabled", 7, 6, 0xff8790a5);
                text("Enable in Performance", 7, 22, 0xff8790a5);
            }
        } else if ("inventory".equals(id)) {
            if (mc.thePlayer == null) { text("Inventory preview", 7, 6, 0xffeeeeff); }
            else {
                RenderHelper.enableGUIStandardItemLighting();
                for (int index = 9; index < 36; index++) {
                    ItemStack stack = mc.thePlayer.inventory.mainInventory[index];
                    int x = 5 + (index - 9) % 9 * 18, y = 3 + (index - 9) / 9 * 18;
                    Gui.drawRect(x, y, x + 17, y + 17, 0x443a4058);
                    if (stack != null) {
                        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, x, y, null);
                    }
                }
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableLighting();
            }
        } else if ("armor".equals(id)) {
            if (mc.thePlayer == null) text("Armor preview", 7, 6, 0xffeeeeff);
            else {
                for (int index = 3; index >= 0; index--) {
                    ItemStack stack = mc.thePlayer.inventory.armorInventory[index];
                    int y = 3 + (3 - index) * 19;
                    if (stack == null) { text("--", 28, y + 5, 0xff8790a5); continue; }
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 6, y);
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableLighting();
                    String durability = stack.isItemStackDamageable() ? Integer.toString(stack.getMaxDamage() - stack.getItemDamage()) : "--";
                    text(durability, 28, y + 5, 0xffeeeeff);
                }
            }
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    private static int count(Deque<Long> clicks) {
        long cutoff = System.nanoTime() - 1000000000L;
        while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) clicks.removeFirst();
        return clicks.size();
    }
    private static String ping(Minecraft mc) {
        if (mc.thePlayer == null || mc.getNetHandler() == null) return "--";
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        return info == null ? "--" : Integer.toString(info.getResponseTime());
    }
    private static void text(String text, int x, int y, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, x, y, color);
    }
    private static void key(KeyBinding binding, String label, int x, int y, int w, int h) {
        int code = binding.getKeyCode();
        boolean pressed = code < 0 ? Mouse.isButtonDown(code + 100) : code > 0 && code < Keyboard.KEYBOARD_SIZE && Keyboard.isKeyDown(code);
        Gui.drawRect(x, y, x + w, y + h, pressed ? (0xff000000 | AlienClient.get().config().accentColor) : 0x9943495f);
        int tw = Minecraft.getMinecraft().fontRendererObj.getStringWidth(label);
        text(label, x + (w - tw) / 2, y + (h - 8) / 2, 0xffeeeeff);
    }
}
