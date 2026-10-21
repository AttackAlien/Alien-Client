package dev.alien.client.smoke;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.cosmetic.CosmeticStudioScreen;
import dev.alien.client.gui.AlienMainMenu;
import dev.alien.client.gui.AlienMenuScreen;
import dev.alien.client.hud.HudConfig;
import dev.alien.client.hud.HudEditorScreen;
import dev.alien.client.hud.HudManager;
import dev.alien.client.world.EffectsScreen;
import dev.alien.client.world.VisualStyleScreen;
import dev.alien.client.world.WorldManager;
import dev.alien.client.world.WorldStudioScreen;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.opengl.GL11;

/**
 * Test-only mod, compiled into a separate source set. Exercises actual client
 * screens and renderers under a real OpenGL context; never ships in the mod jar.
 */
@Mod(modid = "alienclient_smoke", name = "Alien integration smoke", version = "1",
        clientSideOnly = true, dependencies = "required-after:alienclient")
public final class AlienIntegrationSmoke {
    private final List<String> checks = new ArrayList<>();
    private Minecraft mc;
    private File output;
    private int stage;
    private long since, started;
    private boolean done;
    private long originalServerTime;
    private boolean originalRain, originalThunder;
    private String worldFolder;
    private int auroraSkyPixel;
    private boolean minimumDistancePending;
    private int liveScreenStage;
    private boolean disconnectRequested;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) throws Exception {
        if (!Boolean.getBoolean("alien.smoke")) return;
        mc = Minecraft.getMinecraft();
        output = new File(System.getProperty("alien.smoke.output", "../build/smoke")).getCanonicalFile();
        Files.createDirectories(output.toPath());
        Files.deleteIfExists(new File(output, "result.txt").toPath());
        // This task always uses its disposable build/smoke-runtime game folder.
        AlienClient.get().store().resetDefaults();
        started = since = System.nanoTime();
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (done || !disconnectRequested || event.phase != TickEvent.Phase.START) return;
        disconnectRequested = false;
        try {
            // Use the game's own Save and Quit action while its server is still
            // alive. Closing the channel a frame earlier can auto-stop the
            // server before initiateShutdown schedules its final task.
            mc.displayGuiScreen(new GuiIngameMenu());
            click(1);
        } catch (Exception failure) { throw new RuntimeException("Vanilla Save and Quit failed", failure); }
    }

    @SubscribeEvent
    public void render(TickEvent.RenderTickEvent event) {
        if (done || event.phase != TickEvent.Phase.END) return;
        try {
            if (elapsed(started) > 180_000) throw new AssertionError("Smoke timed out at stage " + stage);
            if (elapsed(since) < 700) return;
            step();
        } catch (Throwable failure) {
            done = true;
            try (PrintWriter writer = new PrintWriter(new File(output, "result.txt"), "UTF-8")) {
                writer.println("FAIL at stage " + stage);
                for (String check : checks) writer.println("PASS " + check);
                failure.printStackTrace(writer);
            } catch (Exception writing) { writing.printStackTrace(); }
            failure.printStackTrace();
            System.err.println("ALIEN_SMOKE_FAIL stage=" + stage);
            System.exit(1);
        }
    }

    private void step() throws Exception {
        switch (stage) {
            case 0:
                if (!(mc.currentScreen instanceof GuiMainMenu)) return;
                require(Loader.isModLoaded("alienclient"), "Forge discovers the real Alien Client mod");
                File loadedFrom = new File(AlienClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File expectedClasses = new File(output.getParentFile(), "classes/main").getCanonicalFile();
                require(loadedFrom.getCanonicalFile().toPath().startsWith(expectedClasses.toPath()),
                        "Smoke loads current main classes directly, excluding the packaged jar (actual " + loadedFrom + ")");
                require(HudManager.bounds("performance", config().hud.widgets.get("performance"), 427, 240)[3] == 54,
                        "Runtime HUD uses the latest three-line diagnostic widget");
                require(mc.currentScreen instanceof AlienMainMenu, "Optional main menu replaces vanilla title screen");
                mc.gameSettings.pauseOnLostFocus = false;
                mc.gameSettings.limitFramerate = 30;
                // Vanilla 1.8.9 skips the entire sky renderer below four chunks.
                mc.gameSettings.renderDistanceChunks = 4;
                while (GL11.glGetError() != GL11.GL_NO_ERROR) { }
                capture("01-title");
                mc.displayGuiScreen(new AlienMenuScreen(mc.currentScreen));
                next(); return;
            case 1:
                require(mc.currentScreen instanceof AlienMenuScreen, "Alien control center initializes and renders");
                capture("02-dashboard");
                click(11); next(); return;
            case 2: {
                boolean old = config().rainbowAccent;
                click(103);
                require(config().rainbowAccent != old, "Appearance toggle changes and persists the actual config");
                require(new File(AlienClient.get().store().directory(), "config.json").isFile(), "Config is written to the actual game directory");
                capture("03-appearance");
                click(12); next(); return;
            }
            case 3:
                click(200);
                require("Competitive".equals(config().performance.preset) && config().performance.maxParticles == 16,
                        "Performance preset control applies a real bounded effects budget");
                capture("04-performance");
                mc.displayGuiScreen(new HudEditorScreen(new AlienMenuScreen(null)));
                next(); return;
            case 4: {
                assertButtonsInsideScreen();
                HudConfig.Widget fps = config().hud.widgets.get("fps");
                float old = fps.scale;
                float oldX = fps.x;
                click(2); require(fps.scale > old, "HUD scale control edits the live widget");
                click(6); require(fps.x > .5f, "HUD alignment control repositions the live widget");
                // Keep subsequent live-world captures on the standard readable layout.
                fps.x = oldX; fps.scale = old; AlienClient.get().save();
                capture("05-hud-editor-preview");
                mc.displayGuiScreen(new WorldStudioScreen(null));
                next(); return;
            }
            case 5:
                assertButtonsInsideScreen();
                click(1); require(config().world.enabled, "World Studio custom sky switch is functional");
                click(3); require(config().world.timeOverride, "World Studio local visual time switch is functional");
                capture("06-world-studio");
                mc.displayGuiScreen(new EffectsScreen(null));
                next(); return;
            case 6:
                assertButtonsInsideScreen();
                click(1); require(config().effects.enabled, "Effects master switch is functional");
                capture("07-effects-studio");
                mc.displayGuiScreen(new VisualStyleScreen(null));
                next(); return;
            case 7:
                assertButtonsInsideScreen(); click(1); click(2);
                require(config().effects.cameraSway && config().effects.colorFilter, "Camera and tint controls change actual optional effects");
                capture("08-camera-filter");
                worldFolder = "AlienSmoke-" + System.currentTimeMillis();
                mc.launchIntegratedServer(worldFolder, "Alien smoke disposable world",
                        new WorldSettings(123456789L, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT));
                next(); return;
            case 8:
                if (mc.theWorld == null || mc.thePlayer == null || mc.getIntegratedServer() == null || mc.thePlayer.ticksExisted < 40) return;
                require(true, "Actual integrated Minecraft world starts and the local player joins");
                originalServerTime = mc.getIntegratedServer().worldServerForDimension(0).getWorldTime();
                originalRain = mc.getIntegratedServer().worldServerForDimension(0).getWorldInfo().isRaining();
                originalThunder = mc.getIntegratedServer().worldServerForDimension(0).getWorldInfo().isThundering();
                config().world.enabled = true; config().world.applyTheme("Aurora");
                config().world.timeOverride = true; config().world.visualHour = 0;
                config().world.weather = "Snow"; config().world.weatherIntensity = 1;
                config().cosmetics.self.hat = "SAMURAI"; config().cosmetics.self.face = "KITSUNE";
                config().cosmetics.self.back = "WINGS"; config().cosmetics.self.charm = true;
                config().cosmetics.self.aura = true; config().cosmetics.self.trail = true;
                config().effects.enabled = true;
                config().hud.widgets.get("performance").enabled = true;
                config().hud.widgets.get("inventory").enabled = true;
                config().performance.maxParticles = 80; config().performance.maxCosmetics = 32;
                mc.gameSettings.thirdPersonView = 1;
                mc.displayGuiScreen(null); AlienClient.get().save();
                next(); return;
            case 9: {
                if (elapsed(since) < 2500) return;
                if (minimumDistancePending) {
                    capture("09b-custom-sky-minimum-distance");
                    int lowDistanceSky = skyPixel("09b-custom-sky-minimum-distance");
                    require(((lowDistanceSky >> 16) & 255) + ((lowDistanceSky >> 8) & 255) + (lowDistanceSky & 255) < 380,
                            "Custom midnight sky also renders at the vanilla minimum two-chunk distance");
                    BufferedImage frame = ImageIO.read(new File(output, "screenshots/09b-custom-sky-minimum-distance.png"));
                    int ground = frame.getRGB(frame.getWidth() * 7 / 8, frame.getHeight() * 4 / 5);
                    require(((ground >> 8) & 255) > ((ground >> 16) & 255) && ((ground >> 8) & 255) > (ground & 255),
                            "Minimum-distance sky fallback remains behind the visible grass terrain");
                    mc.gameSettings.renderDistanceChunks = 4;
                    require(config().menuKey == 54, "Default registered menu shortcut is Right Shift");
                    KeyBinding.onTick(config().menuKey);
                    FMLCommonHandler.instance().bus().post(new InputEvent.KeyInputEvent());
                    require(mc.currentScreen instanceof AlienMenuScreen, "Registered menu key event opens the Alien menu inside the world");
                    next(); return;
                }
                long currentTime = mc.getIntegratedServer().worldServerForDimension(0).getWorldTime();
                require(currentTime >= originalServerTime && currentTime - originalServerTime < 500,
                        "Visual midnight leaves actual server time advancing normally");
                require(originalRain == mc.getIntegratedServer().worldServerForDimension(0).getWorldInfo().isRaining()
                                && originalThunder == mc.getIntegratedServer().worldServerForDimension(0).getWorldInfo().isThundering(),
                        "Visual snow leaves actual server rain/thunder state unchanged");
                capture("09-local-world-aurora-cosmetics-hud");
                auroraSkyPixel = skyPixel("09-local-world-aurora-cosmetics-hud");
                require(((auroraSkyPixel >> 16) & 255) + ((auroraSkyPixel >> 8) & 255) + (auroraSkyPixel & 255) < 380,
                        "Custom midnight sky visibly replaces the vanilla daytime sky");
                mc.gameSettings.renderDistanceChunks = 2;
                minimumDistancePending = true; since = System.nanoTime();
                return;
            }
            case 10: {
                if (liveScreenStage == 0) {
                    require(mc.theWorld != null && mc.currentScreen instanceof AlienMenuScreen,
                            "Alien module menu remains open and renders inside the loaded world");
                    capture("09c-alien-menu-inside-world");
                    mc.displayGuiScreen(new WorldStudioScreen(new AlienMenuScreen(null)));
                    liveScreenStage = 1; since = System.nanoTime(); return;
                }
                if (liveScreenStage == 1) {
                    require(mc.theWorld != null && mc.currentScreen instanceof WorldStudioScreen,
                            "Sky and weather editor renders inside the loaded world");
                    capture("09d-world-studio-inside-world");
                    mc.displayGuiScreen(new CosmeticStudioScreen(null));
                    liveScreenStage = 2; since = System.nanoTime(); return;
                }
                assertButtonsInsideScreen();
                String old = config().cosmetics.self.hat;
                click(10); require(!old.equals(config().cosmetics.self.hat), "Cosmetic accessory selector edits the actual player preset");
                capture("10-cosmetic-live-player-preview");
                mc.displayGuiScreen(new HudEditorScreen(null));
                next(); return;
            }
            case 11:
                capture("11-hud-editor-live-world");
                AlienClient.get().store().saveProfile("smoke_integration");
                config().theme = "Transient smoke edit";
                require(AlienClient.get().store().loadProfile("smoke_integration"), "Actual runtime profile save and load succeeds");
                require(!"Transient smoke edit".equals(config().theme), "Profile load restores the saved preferences");
                AlienClient.get().store().bindServer(AlienClient.get().currentServerKey(), "smoke_integration");
                writeSkybox();
                config().world.skyboxFile = "smoke.png"; config().world.enabled = true;
                mc.displayGuiScreen(null);
                next(); return;
            case 12:
                if (elapsed(since) < 3000) return;
                // The core applies the newly bound profile on its next client tick.
                // Apply the import afterward to test the renderer with that active profile.
                config().world.skyboxFile = "smoke.png";
                next(); return;
            case 13:
                if (elapsed(since) < 3000) return;
                require(WorldManager.lastImportMessage.startsWith("Loaded smoke.png"), "Imported PNG skybox decodes and uploads to the real renderer");
                capture("12-imported-skybox-live-world");
                int importedPixel = skyPixel("12-imported-skybox-live-world");
                require(colorDistance(importedPixel, auroraSkyPixel) > 50, "Imported skybox visibly changes the rendered sky");
                require(AlienClient.get().lastError().isEmpty(), "No Alien Client persistence errors during the runtime workflow");
                require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL reports no error after studios and live world rendering");
                AlienClient.get().store().deleteProfile("smoke_integration");
                // Request the actual vanilla menu action at the next tick.
                // Keep the channel open until that synchronous action runs.
                disconnectRequested = true;
                next(); return;
            case 14:
                if (mc.theWorld != null) return;
                require(true, "Normal connection teardown unloads the disposable world");
                mc.displayGuiScreen(new GuiMainMenu());
                next(); return;
            case 15:
                require(mc.currentScreen instanceof AlienMainMenu, "Disconnect restores the optional Alien title menu");
                capture("13-title-after-disconnect");
                done = true;
                try (PrintWriter writer = new PrintWriter(new File(output, "result.txt"), "UTF-8")) {
                    writer.println("PASS " + checks.size() + " checks");
                    writer.println("Actual Minecraft 1.8.9 / Forge 11.15.1.2318; disposable world " + worldFolder);
                    writer.println("This functional smoke run is not a performance benchmark.");
                    for (String check : checks) writer.println("PASS " + check);
                }
                System.out.println("ALIEN_SMOKE_PASS checks=" + checks.size());
                mc.shutdown();
                return;
            default: throw new AssertionError("Unknown stage " + stage);
        }
    }

    private ClientConfig config() { return AlienClient.get().config(); }
    private void next() { stage++; since = System.nanoTime(); }
    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
    private void require(boolean condition, String check) {
        if (!condition) throw new AssertionError(check);
        checks.add(check); System.out.println("ALIEN_SMOKE_CHECK " + check);
    }

    @SuppressWarnings("unchecked")
    private List<GuiButton> buttons() throws Exception {
        Field field = GuiScreen.class.getDeclaredField("buttonList"); field.setAccessible(true);
        return (List<GuiButton>) field.get(mc.currentScreen);
    }
    private void click(int id) throws Exception {
        GuiScreen screen = mc.currentScreen;
        GuiButton button = null;
        for (GuiButton candidate : buttons()) if (candidate.id == id) { button = candidate; break; }
        if (button == null) throw new AssertionError("Missing button " + id + " on " + screen.getClass().getName());
        Method action = screen.getClass().getDeclaredMethod("actionPerformed", GuiButton.class);
        action.setAccessible(true); action.invoke(screen, button);
    }
    private void assertButtonsInsideScreen() throws Exception {
        for (GuiButton button : buttons()) {
            if (!button.visible) continue;
            if (button.xPosition < 0 || button.yPosition < 0 || button.xPosition + button.width > mc.currentScreen.width
                    || button.yPosition + button.height > mc.currentScreen.height)
                throw new AssertionError("Off-screen button " + button.id + " " + button.displayString + " at "
                        + button.xPosition + "," + button.yPosition + " on " + mc.currentScreen.getClass().getName()
                        + " " + mc.currentScreen.width + "x" + mc.currentScreen.height);
        }
        require(true, mc.currentScreen.getClass().getSimpleName() + " controls fit " + mc.currentScreen.width + "x" + mc.currentScreen.height);
    }
    private void capture(String name) throws Exception {
        ScreenShotHelper.saveScreenshot(output, name + ".png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
        File png = new File(output, "screenshots/" + name + ".png");
        if (!png.isFile() || png.length() < 1024) throw new AssertionError("Missing/empty screenshot " + name);
        BufferedImage image = ImageIO.read(png);
        if (image == null || image.getWidth() != mc.displayWidth || image.getHeight() != mc.displayHeight)
            throw new AssertionError("Invalid framebuffer screenshot " + name);
        require(true, "Framebuffer capture " + name);
    }
    private void writeSkybox() throws Exception {
        File directory = new File(AlienClient.get().store().directory(), "skyboxes"); Files.createDirectories(directory.toPath());
        BufferedImage image = new BufferedImage(256, 128, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
            image.setRGB(x, y, ((20 + y) << 16) | ((30 + x / 2) << 8) | 150);
        ImageIO.write(image, "png", new File(directory, "smoke.png"));
    }
    private int skyPixel(String name) throws Exception {
        BufferedImage image = ImageIO.read(new File(output, "screenshots/" + name + ".png"));
        // Clear sky to the right of the character, below the diagnostic HUD.
        int x = image.getWidth() * 3 / 4, y = image.getHeight() / 3;
        int pixel = image.getRGB(x, y);
        System.out.println("ALIEN_SMOKE_SKY " + name + " x=" + x + " y=" + y + " rgb=" + Integer.toHexString(pixel & 0xFFFFFF));
        return pixel;
    }
    private static int colorDistance(int first, int second) {
        return Math.abs(((first >> 16) & 255) - ((second >> 16) & 255))
                + Math.abs(((first >> 8) & 255) - ((second >> 8) & 255))
                + Math.abs((first & 255) - (second & 255));
    }
}
