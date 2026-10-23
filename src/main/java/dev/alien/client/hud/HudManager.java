package dev.alien.client.hud;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import net.minecraft.client.gui.FontRenderer;
import dev.alien.client.performance.PerformanceEngine;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
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
        if (mc.thePlayer == null || !config.enabled) return;
        // Alien screens draw their own chrome and previews over the world.
        if (mc.currentScreen != null && mc.currentScreen.getClass().getName().startsWith("dev.alien.client.")) return;
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
            || "performance".equals(id) || "text".equals(id) || "brand".equals(id) || "modules".equals(id);
    }

    static HudConfig config() {
        HudConfig config = AlienClient.get().config().hud;
        config.ensureDefaults();
        return config;
    }

    public static int[] bounds(String id, HudConfig.Widget widget, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        int w = 96, h = 20;
        if ("brand".equals(id)) { w = Math.max(124, font().getStringWidth("ALIEN  |  " + Minecraft.getDebugFPS() + " FPS") + 14); h = 22; }
        else if ("modules".equals(id)) {
            List<String> labels = moduleLabels();
            w = 84;
            for (String label : labels) w = Math.max(w, font().getStringWidth(label) + 12);
            h = Math.max(12, labels.size() * 12);
        }
        else if ("keys".equals(id)) { w = 76; h = 76; }
        else if ("inventory".equals(id)) { w = 168; h = 60; }
        else if ("armor".equals(id)) { w = 92; h = 82; }
        else if ("performance".equals(id)) { w = 180; h = 70; }
        else if ("coordinates".equals(id)) { w = 158; h = 20; }
        else if ("text".equals(id)) w = Math.max(20, font().getStringWidth(config().customText) + 12);
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
        boolean custom = "brand".equals(id) || "modules".equals(id);
        if (!custom) {
            backdrop(b[4],b[5]);
        }
        if ("brand".equals(id)) brand(b[4]);
        else if ("modules".equals(id)) modules(b[4], preview);
        else if ("fps".equals(id)) text(Minecraft.getDebugFPS() + " FPS", 7, 6, 0xffeeeeff);
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
                text(String.format("P95 %.1f / P99 %.1f ms", metrics.percentile95Ms(),
                        metrics.percentile99Ms()), 7, 22, 0xffb6bdd1);
                text("Heap " + metrics.usedMemoryMb() + " MB  /  Spikes " + metrics.spikeCount(),
                        7, 38, 0xffb6bdd1);
                text("Alien quality " + Math.round(metrics.qualityScale() * 100) + "%",
                        7, 54, 0xffb6bdd1);
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

    private static void brand(int width) {
        int accent = 0xff000000 | AlienClient.get().config().accentColor;
        backdrop(width,22);
        Gui.drawRect(0, 2, 2, 18, accent);
        text("\u00a7lALIEN", 7, 6, 0xfffaf8ff);
        int divider = font().getStringWidth("\u00a7lALIEN") + 13;
        Gui.drawRect(divider, 5, divider + 1, 15, 0x887f788d);
        text(Minecraft.getDebugFPS() + " FPS", divider + 7, 6, 0xffe8e5f0);
        Gui.drawRect(7, 19, 28, 20, accent);
    }

    private static List<String> moduleLabels() {
        List<String> labels = HudModules.active(AlienClient.get().config());
        Collections.sort(labels, new Comparator<String>() {
            @Override public int compare(String a, String b) {
                return Integer.compare(font().getStringWidth(b), font().getStringWidth(a));
            }
        });
        return labels;
    }

    private static void modules(int width, boolean preview) {
        List<String> labels = moduleLabels();
        if (labels.isEmpty() && preview) { text("No active visuals", 6, 2, 0xff9991a8); return; }
        int y = 0, accent = 0xff000000 | AlienClient.get().config().accentColor;
        for (String label : labels) {
            int rowWidth = font().getStringWidth(label) + 12;
            if (config().background) Ui.rounded(0,y,rowWidth,12,3,Ui.alpha(AlienClient.get().config().ui.surface,config().opacity));
            Gui.drawRect(0, y + 2, 1, y + 10, accent);
            text(label, 6, y + 2, 0xffefedf7);
            y += 12;
        }
    }

    private static FontRenderer font() { return config().modernFont ? Ui.font() : Minecraft.getMinecraft().fontRendererObj; }
    private static void backdrop(int w,int h) {
        if (!config().background) return;
        int base=Ui.alpha(AlienClient.get().config().ui.surface,config().opacity);
        if(config().shadows) Ui.shadow(0,0,w,h,5,.25f);
        if(config().gradient) Ui.gradient(0,0,w,h,5,base,Ui.alpha(AlienClient.get().config().ui.surfaceRaised,config().opacity));
        else Ui.rounded(0,0,w,h,5,base);
        if(config().borders) Ui.rounded(0,4,2,h-8,1,Ui.accent());
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
        font().drawStringWithShadow(text, x, y, color);
    }
    private static void key(KeyBinding binding, String label, int x, int y, int w, int h) {
        int code = binding.getKeyCode();
        boolean pressed = code < 0 ? Mouse.isButtonDown(code + 100) : code > 0 && code < Keyboard.KEYBOARD_SIZE && Keyboard.isKeyDown(code);
        Ui.rounded(x,y,w,h,4,pressed ? Ui.accent() : Ui.alpha(AlienClient.get().config().ui.surfaceRaised,config().opacity));
        int tw = font().getStringWidth(label);
        text(label, x + (w - tw) / 2, y + (h - 8) / 2, 0xffeeeeff);
    }
}
