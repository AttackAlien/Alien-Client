package dev.alien.client.smoke;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.cosmetic.CosmeticConfig;
import dev.alien.client.cosmetic.CosmeticStudioScreen;
import dev.alien.client.gui.AlienMainMenu;
import dev.alien.client.gui.AlienMenuScreen;
import dev.alien.client.hud.HudConfig;
import dev.alien.client.hud.HudEditorScreen;
import dev.alien.client.hud.HudManager;
import dev.alien.client.world.AtmospherePresets;
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
    private int capeMotionFrame;
    private int[] firstCapePixels;
    private int changedCapePixels;
    private int atmosphereStage;
    private int crystalSkyPixel;
    private long atmosphereServerTime;
    private boolean atmosphereRain, atmosphereThunder;

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
            long minimumWait = stage == 10 && liveScreenStage == 6 ? 100 : 700;
            if (elapsed(since) < minimumWait) return;
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
                    require("PanelMenuScreen".equals(mc.currentScreen.getClass().getSimpleName()),
                            "The actual registered shortcut opens the floating panel menu layout");
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
                if (liveScreenStage == 2) {
                    assertButtonsInsideScreen();
                    String old = config().cosmetics.self.hat;
                    click(10);
                    require(!old.equals(config().cosmetics.self.hat), "Cosmetic accessory selector edits the actual player preset");
                    liveScreenStage = 3; since = System.nanoTime(); return;
                }
                if (liveScreenStage == 3) {
                    // Capture after a fresh rendered frame, not in the same frame
                    // that changed the selector and rebuilt its controls.
                    capture("10-cosmetic-live-player-preview");
                    prepareClothPreset();
                    mc.displayGuiScreen(new CosmeticStudioScreen(null));
                    setPreviewPose(35, 1.1f);
                    liveScreenStage = 4; since = System.nanoTime(); return;
                }
                if (liveScreenStage == 4) {
                    CosmeticStudioScreen studio = (CosmeticStudioScreen) mc.currentScreen;
                    require(Math.abs(studio.previewYaw() - 35) < .01f,
                            "Cosmetic preview supports a stable user-selected turntable angle");
                    require(Math.abs(studio.previewZoom() - 1.1f) < .01f,
                            "Cosmetic preview supports bounded user-selected zoom");
                    capture("14-smooth-kasa-and-cloth-front");
                    setPreviewPose(180, 1.1f);
                    liveScreenStage = 5; since = System.nanoTime(); return;
                }
                if (liveScreenStage == 5) {
                    capture("15-cloth-cape-emblem-back");
                    capeMotionFrame = 0; firstCapePixels = null; changedCapePixels = 0;
                    liveScreenStage = 6; since = System.nanoTime(); return;
                }
                if (liveScreenStage == 6) {
                    String filename = String.format(java.util.Locale.ROOT, "cape-motion-%02d", capeMotionFrame);
                    capture(filename);
                    int[] pixels = previewBodyPixels(filename);
                    if (firstCapePixels == null) firstCapePixels = pixels;
                    else {
                        if (pixels.length != firstCapePixels.length) throw new AssertionError("Preview changed size during motion capture");
                        int changed = 0;
                        for (int i = 0; i < pixels.length; i++) if (colorDistance(pixels[i], firstCapePixels[i]) > 20) changed++;
                        changedCapePixels = Math.max(changedCapePixels, changed);
                    }
                    capeMotionFrame++; since = System.nanoTime();
                    if (capeMotionFrame < 12) return;
                    require(changedCapePixels >= 12,
                            "Animated cloth produces visible changes inside the back-view player region across actual rendered frames");
                    Files.write(new File(output, "cape-motion-metrics.txt").toPath(),
                            ("12 original framebuffer frames, minimum interval 100 ms; maximum changed body-region pixels="
                                    + changedCapePixels + "\n").getBytes(StandardCharsets.UTF_8));
                    CosmeticConfig.Accessory a = config().cosmetics.self;
                    a.hat = "SAMURAI"; a.face = "KITSUNE"; a.back = "CAPE";
                    a.color = 0x292130; a.secondaryColor = 0xD6B56C; a.capePattern = "MOON";
                    AlienClient.get().save();
                    mc.displayGuiScreen(new CosmeticStudioScreen(null)); setPreviewPose(35, 1.1f);
                    liveScreenStage = 7; return;
                }
                if (liveScreenStage == 7) {
                    capture("16-sculpted-samurai-kitsune");
                    CosmeticConfig.Accessory a = config().cosmetics.self;
                    a.hat = "CROWN"; a.face = "NONE"; a.back = "WINGS";
                    a.color = 0xB79AFF; a.secondaryColor = 0x5EDBCD;
                    AlienClient.get().save();
                    mc.displayGuiScreen(new CosmeticStudioScreen(null)); setPreviewPose(150, 1.05f);
                    liveScreenStage = 8; since = System.nanoTime(); return;
                }
                capture("17-sculpted-wings-back-view");
                require(GL11.glGetError() == GL11.GL_NO_ERROR,
                        "Smooth meshes, textured cloth and repeated preview poses preserve a clean OpenGL state");
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
            case 13: {
                if (elapsed(since) < 3000) return;
                if (atmosphereStage == 0) {
                    require(WorldManager.lastImportMessage.startsWith("Loaded smoke.png"), "Imported PNG skybox decodes and uploads to the real renderer");
                    capture("12-imported-skybox-live-world");
                    int importedPixel = skyPixel("12-imported-skybox-live-world");
                    require(colorDistance(importedPixel, auroraSkyPixel) > 50, "Imported skybox visibly changes the rendered sky");
                    require(AlienClient.get().lastError().isEmpty(), "No Alien Client persistence errors during the runtime workflow");
                    require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL reports no error after studios and live world rendering");
                    atmosphereServerTime = mc.getIntegratedServer().worldServerForDimension(0).getWorldTime();
                    atmosphereRain = mc.getIntegratedServer().worldServerForDimension(0).getWorldInfo().isRaining();
                    atmosphereThunder = mc.getIntegratedServer().worldServerForDimension(0).getWorldInfo().isThundering();
                    prepareClothPreset();
                    config().hud.applyReferencePreset();
                    AtmospherePresets.apply(config().world, config().effects, "Crystal");
                    require(config().world.enabled && "Crystal".equals(config().world.theme)
                                    && config().effects.enabled && config().effects.ambient && "Diamond".equals(config().effects.shape),
                            "Crystal preset enables the real cyan sky and ambient diamond particle settings");
                    require(config().hud.enabled && config().hud.widgets.get("brand").enabled
                                    && config().hud.widgets.get("modules").enabled && !config().hud.background,
                            "Reference HUD enables its live brand and active visual modules list");
                    AlienClient.get().save();
                    atmosphereStage = 1; since = System.nanoTime(); return;
                }
                if (atmosphereStage == 1) {
                    require("Procedural sky active".equals(WorldManager.lastImportMessage),
                            "Applying a procedural preset releases the previously uploaded PNG skybox");
                    capture("18-crystal-cyan-sky-particles-hud");
                    crystalSkyPixel = skyPixel("18-crystal-cyan-sky-particles-hud");
                    require(((crystalSkyPixel >> 8) & 255) > ((crystalSkyPixel >> 16) & 255)
                                    && (crystalSkyPixel & 255) > ((crystalSkyPixel >> 16) & 255),
                            "Crystal preset produces an actual cyan sky in the game framebuffer");
                    AtmospherePresets.apply(config().world, config().effects, "Sakura");
                    require("Sakura".equals(config().world.theme) && config().effects.enabled
                                    && config().effects.ambient && "Petal".equals(config().effects.shape),
                            "Sakura preset changes the actual sky palette and ambient petal particle settings");
                    AlienClient.get().save();
                    atmosphereStage = 2; since = System.nanoTime(); return;
                }
                if (atmosphereStage == 2) {
                    capture("19-sakura-petals-sky-hud");
                    require(colorDistance(skyPixel("19-sakura-petals-sky-hud"), crystalSkyPixel) > 50,
                            "Sakura produces a visibly different rendered sky from Crystal");
                    config().world.enabled = false; config().world.timeOverride = false;
                    config().world.weather = "Server"; config().world.fogEnabled = false;
                    config().effects.enabled = false;
                    AlienClient.get().save();
                    atmosphereStage = 3; since = System.nanoTime(); return;
                }
                require(mc.theWorld.provider.getSkyRenderer() == null && mc.theWorld.provider.getWeatherRenderer() == null,
                        "Disabling local sky and weather restores the vanilla dimension renderers");
                capture("20-vanilla-sky-restored-after-presets");
                long finalServerTime = mc.getIntegratedServer().worldServerForDimension(0).getWorldTime();
                require(finalServerTime > atmosphereServerTime && finalServerTime - atmosphereServerTime < 500,
                        "Atmosphere presets leave actual server time advancing normally");
                require(atmosphereRain == mc.getIntegratedServer().worldServerForDimension(0).getWorldInfo().isRaining()
                                && atmosphereThunder == mc.getIntegratedServer().worldServerForDimension(0).getWorldInfo().isThundering(),
                        "Atmosphere presets leave actual server rain and thunder unchanged");
                require(GL11.glGetError() == GL11.GL_NO_ERROR,
                        "Ambient particles and restored sky preserve a clean OpenGL state");
                AlienClient.get().store().deleteProfile("smoke_integration");
                // Request the actual vanilla menu action at the next tick.
                // Keep the channel open until that synchronous action runs.
                disconnectRequested = true;
                next(); return;
            }
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
    private void prepareClothPreset() {
        // Competitive was tested earlier; use the requested high cloth detail
        // for these visual checks instead of its deliberate low-detail cap.
        config().performance.preset = "Balanced";
        CosmeticConfig.Accessory a = config().cosmetics.self;
        a.enabled = true; a.hat = "JAPANESE"; a.face = "NONE"; a.back = "CAPE";
        a.charm = false; a.aura = false; a.trail = false;
        a.color = 0x252333; a.secondaryColor = 0xD6B56C;
        a.offsetX = a.offsetY = a.offsetZ = a.rotationX = a.rotationY = a.rotationZ = 0;
        a.scale = 1; a.hatSize = 1; a.capeLength = 1.1f; a.capeFlutter = 1.3f;
        a.animations = true; a.detailLevel = "High"; a.capePattern = "ALIEN";
        AlienClient.get().save();
    }
    private void setPreviewPose(float yaw, float zoom) {
        CosmeticStudioScreen studio = (CosmeticStudioScreen) mc.currentScreen;
        studio.setPreviewYaw(yaw); studio.setPreviewZoom(zoom);
    }
    /** Sample the model area only: excludes HUD clock, panel labels and controls. */
    private int[] previewBodyPixels(String name) throws Exception {
        BufferedImage frame = ImageIO.read(new File(output, "screenshots/" + name + ".png"));
        CosmeticStudioScreen studio = (CosmeticStudioScreen) mc.currentScreen;
        int[] bounds = studio.previewCanvasBounds();
        double sx = frame.getWidth() / (double) mc.currentScreen.width;
        double sy = frame.getHeight() / (double) mc.currentScreen.height;
        double modelScale = Math.min(100, (bounds[3] - 11) / 2.4) * studio.previewZoom();
        double baseline = bounds[1] + bounds[3] - 4;
        // This narrow rectangle lies over the back cloth itself, away from
        // independently animated arms, the head, the HUD clock and panel art.
        int x = (int) Math.round((bounds[0] + bounds[2] * .5 - modelScale * .18) * sx);
        int y = (int) Math.round((baseline - modelScale * 1.30) * sy);
        int w = (int) Math.round(modelScale * .36 * sx);
        int h = (int) Math.round(modelScale * .65 * sy);
        if (x < 0 || y < 0 || w <= 0 || h <= 0 || x + w > frame.getWidth() || y + h > frame.getHeight())
            throw new AssertionError("Invalid cosmetic preview sample region");
        return frame.getRGB(x, y, w, h, null, 0, w);
    }
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
