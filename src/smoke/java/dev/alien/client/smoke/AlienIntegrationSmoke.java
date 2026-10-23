package dev.alien.client.smoke;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.module.AccessLevel;
import dev.alien.client.module.GameplayImpact;
import dev.alien.client.module.ModuleDescriptor;
import dev.alien.client.cosmetic.CosmeticConfig;
import dev.alien.client.cosmetic.CosmeticStudioScreen;
import dev.alien.client.performance.PerformanceDashboardScreen;
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
    private boolean keysPrepared;
    private boolean skyBeforeKeys;
    private Integer savedSkyKey;
    private int responsiveStep;
    private long blurFramesBefore;
    private boolean responsiveDone;
    private int pickerColor;

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
            // In Forge 1.8.9 the menu's Save and Quit action sends a disconnect
            // packet *before* Minecraft.loadWorld(null) requests integrated-server
            // shutdown. The server may stop as soon as the packet arrives.
            // initiateShutdown then waits for a queued task on a dead server,
            // deadlocking the client thread (captured by the CI SIGQUIT dump).
            // Shut down the integrated server through loadWorld first, while
            // its worker is still alive, then show the usual Minecraft title.
            mc.loadWorld(null);
            mc.displayGuiScreen(new GuiMainMenu());
        } catch (Exception failure) { throw new RuntimeException("Integrated server teardown failed", failure); }
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
                require(BuildEdition.ID.equals(System.getProperty("alien.smoke.edition")),
                        "The selected edition provider matches the actual Gradle runtime");
                net.minecraftforge.fml.common.ModContainer container = Loader.instance().getIndexedModList().get("alienclient");
                require(BuildEdition.NAME.equals(container.getName()) && AlienClient.VERSION.equals(container.getVersion()),
                        "Forge discovers the actual edition name and current version");
                require(BuildEdition.CONFIG_DIRECTORY.equals(AlienClient.get().store().directory().getName())
                                && "config".equals(AlienClient.get().store().directory().getParentFile().getName()),
                        "The actual edition uses its isolated configuration namespace");
                require(BuildEdition.ALLOW_FULL == ("full".equals(BuildEdition.ID) || "unified".equals(BuildEdition.ID)),
                        "The compiled edition policy agrees with its identity");
                java.util.Enumeration<java.net.URL> markers = BuildEdition.class.getClassLoader()
                        .getResources("META-INF/alienclient/edition.json");
                int markerCount = 0;
                while (markers.hasMoreElements()) {
                    java.net.URL marker = markers.nextElement(); markerCount++;
                    try (java.io.Reader reader = new java.io.InputStreamReader(marker.openStream(), StandardCharsets.UTF_8)) {
                        com.google.gson.JsonObject data = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
                        require(BuildEdition.ID.equals(data.get("id").getAsString()),
                                "The loaded resource marker belongs to the current edition");
                    }
                }
                require(markerCount == 1, "Exactly one edition resource is on the running game classpath");
                boolean unified = "unified".equals(BuildEdition.ID);
                require(AlienClient.get().modules().modules().size() == (unified ? 17 : 9),
                        "The runtime preserves all nine visual switches and Unified's eight gameplay switches");
                int visuals = 0, combat = 0;
                for (ModuleDescriptor descriptor : AlienClient.get().modules().modules()) {
                    if (descriptor.accessLevel() == AccessLevel.SHARED
                            && descriptor.gameplayImpact() == GameplayImpact.LOCAL_VISUAL) visuals++;
                    else if (descriptor.accessLevel() == AccessLevel.FULL
                            && descriptor.gameplayImpact() == GameplayImpact.GAMEPLAY_CHANGING) combat++;
                }
                require(visuals == 9 && combat == (unified ? 8 : 0),
                        "Edition access and actual gameplay impact survive the redesign integration");
                require(!config().combat.enabled && !config().combat.aimAssist && !config().combat.clickAssist,
                        "Existing combat features remain disabled on a new profile");
                File loadedFrom = new File(AlienClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File expectedClasses = new File(output.getParentFile(), "classes/main").getCanonicalFile();
                require(loadedFrom.getCanonicalFile().toPath().startsWith(expectedClasses.toPath()),
                        "Smoke loads current main classes directly, excluding the packaged jar (actual " + loadedFrom + ")");
                require(HudManager.bounds("performance", config().hud.widgets.get("performance"), 427, 240)[3] == 70,
                        "Runtime HUD uses the Performance 2.0 four-line diagnostic widget");
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
                if (!keysPrepared) {
                    savedSkyKey = config().moduleKeys.get("sky");
                    skyBeforeKeys = config().world.enabled;
                    config().moduleKeys.put("sky", 34); // Keyboard G; synchronized by the real client tick.
                    mc.displayGuiScreen(null); AlienClient.get().save();
                    keysPrepared = true; since = System.nanoTime(); return;
                }
                KeyBinding.onTick(34);
                FMLCommonHandler.instance().bus().post(new InputEvent.KeyInputEvent());
                require(config().world.enabled != skyBeforeKeys,
                        "The actual registered per-module key toggles the sky in a loaded world");
                KeyBinding.onTick(34);
                FMLCommonHandler.instance().bus().post(new InputEvent.KeyInputEvent());
                require(config().world.enabled == skyBeforeKeys,
                        "A second real binding event restores the visual module switch");
                mc.displayGuiScreen(new WorldStudioScreen(null));
                KeyBinding.onTick(34);
                FMLCommonHandler.instance().bus().post(new InputEvent.KeyInputEvent());
                require(config().world.enabled == skyBeforeKeys,
                        "Per-module shortcuts do not activate while an editor is open");
                mc.displayGuiScreen(null);
                FMLCommonHandler.instance().bus().post(new InputEvent.KeyInputEvent());
                require(config().world.enabled == skyBeforeKeys,
                        "Editor key presses are drained without a deferred world toggle");
                if (savedSkyKey == null) config().moduleKeys.remove("sky");
                else config().moduleKeys.put("sky", savedSkyKey);
                AlienClient.get().save();
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
                    BufferedImage character=ImageIO.read(new File(output,"screenshots/14-smooth-kasa-and-cloth-front.png"));
                    int[] canvas=studio.previewCanvasBounds();double unit=Math.min(100,(canvas[3]-11)/2.4)*studio.previewZoom();
                    double baseline=canvas[1]+canvas[3]-4,center=canvas[0]+canvas[2]/2.;
                    double px=character.getWidth()/(double)studio.width,py=character.getHeight()/(double)studio.height;
                    int warm=0;
                    for(int yy=(int)((baseline-unit*1.79)*py);yy<(baseline-unit*1.56)*py;yy++)
                        for(int xx=(int)((center-unit*.13)*px);xx<(center+unit*.13)*px;xx++){
                            int rgb=character.getRGB(xx,yy),r=rgb>>16&255,g=rgb>>8&255,b=rgb&255;
                            if(r>g*1.12 && r>b*1.2 && r>35)warm++;
                        }
                    require(warm>10,"The live cosmetic preview renders the player's skin as well as the accessory meshes");
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
                if (liveScreenStage == 8) {
                    capture("17-sculpted-wings-back-view");
                    require(GL11.glGetError() == GL11.GL_NO_ERROR,
                            "Smooth meshes, textured cloth and repeated preview poses preserve a clean OpenGL state");
                    mc.displayGuiScreen(new CosmeticStudioScreen(null));
                    click(7); // The new sixth tab: Looks
                    liveScreenStage = 9; since = System.nanoTime(); return;
                }
                if (liveScreenStage == 9) {
                    require(mc.currentScreen instanceof CosmeticStudioScreen,
                            "Cosmetic Looks gallery opens during actual Forge rendering");
                    assertButtonsInsideScreen();
                    capture("30-cosmetic-looks-gallery");
                    click(112); // Sakura builtin look
                    require("KITSUNE".equals(config().cosmetics.self.face)
                                    && config().cosmetics.self.trail,
                            "Sakura preset updates real cosmetic meshes and particle options");
                    int savedColor = config().cosmetics.self.color;
                    click(120); // Save slot 1
                    require(config().cosmetics.savedLooks.containsKey("1"),
                            "Cosmetic look slot persists in active profile config");
                    config().cosmetics.self.color = 0x112233;
                    click(121); // Load slot 1
                    require(savedColor == config().cosmetics.self.color,
                            "Loading saved look restores independent cosmetic snapshot");
                    click(113); // Phantom builtin look
                    require("FUTURE".equals(config().cosmetics.self.face),
                            "Phantom look selects the original futuristic visor");
                    AlienClient.get().save();
                    AlienClient.get().store().load();
                    require(config().cosmetics.savedLooks.containsKey("1"),
                            "Saved look remains available after actual disk config reload");
                    mc.displayGuiScreen(new CosmeticStudioScreen(null));
                    click(7);
                    liveScreenStage = 10; since = System.nanoTime(); return;
                }
                if (liveScreenStage == 10) {
                    capture("31-cosmetic-phantom-look");
                    mc.displayGuiScreen(new PerformanceDashboardScreen(null));
                    liveScreenStage = 11; since = System.nanoTime(); return;
                }
                require(mc.currentScreen instanceof PerformanceDashboardScreen,
                        "Performance Engine 2.0 dashboard opens in actual Minecraft");
                if (liveScreenStage == 11) {
                    assertButtonsInsideScreen();
                    capture("32-performance-engine-dashboard");
                    boolean compactToggle = false;
                    for (GuiButton control : buttons()) if (control.id == 9) compactToggle = true;
                    if (compactToggle) {
                        click(9); // Switch drawing state now; screenshot must wait for the next actual frame.
                        liveScreenStage = 12; since = System.nanoTime(); return;
                    }
                    liveScreenStage = 13;
                }
                if (liveScreenStage == 12) {
                    capture("33-performance-engine-compact-graph");
                    require(mc.currentScreen instanceof PerformanceDashboardScreen,
                            "Compact Graph/Stats view renders on a separate Minecraft frame");
                    click(9);
                    liveScreenStage = 13; since = System.nanoTime(); return;
                }
                click(1);
                require(config().performance.adaptiveEnabled,
                        "Adaptive Alien-only visual budgets can be explicitly enabled");
                click(8);
                require(!config().performance.adaptiveEnabled
                        && config().performance.maxParticles > 0,
                        "Disabling adaptive quality preserves saved performance budgets");
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
                    require(WorldManager.lastImportMessage.startsWith("Loaded panorama smoke.png")
                                    || WorldManager.lastImportMessage.startsWith("Loaded smoke.png"),
                            "Imported PNG skybox decodes and uploads to the real renderer");
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

                // Finish responsive UI checks, then unload through Minecraft on the client tick.
                next(); return;
            }
            case 14:
                if (!responsiveDone) { responsiveScreens(); return; }
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
                    writer.println(BuildEdition.NAME + " " + AlienClient.VERSION + "; actual Minecraft 1.8.9 / Forge 11.15.1.2318; disposable world " + worldFolder);
                    writer.println("This functional smoke run is not a performance benchmark.");
                    for (String check : checks) writer.println("PASS " + check);
                }
                System.out.println("ALIEN_SMOKE_PASS checks=" + checks.size());
                mc.shutdown();
                return;
            default: throw new AssertionError("Unknown stage " + stage);
        }
    }

    private void responsiveScreens() throws Exception {
        if(responsiveStep==0){
            config().ui.blur=.6f;config().ui.font="Modern";config().rainbowAccent=false;
            blurFramesBefore=dev.alien.client.gui.ui.BackdropBlur.renderedFrames();
            mc.displayGuiScreen(new dev.alien.client.gui.ThemeEditorScreen(null));
        } else if(responsiveStep==1){
            require(dev.alien.client.gui.Ui.font() instanceof dev.alien.client.gui.ui.SmoothFont,"Alien screens use the actual bundled antialiased font renderer");
            require(dev.alien.client.gui.ui.BackdropBlur.renderedFrames()>blurFramesBefore,"Optional Gaussian blur executes real framebuffer shader passes");
            assertButtonsInsideScreen();capture("21-theme-editor-colors");
            BufferedImage colors=ImageIO.read(new File(output,"screenshots/21-theme-editor-colors.png"));
            Field pickerX=mc.currentScreen.getClass().getDeclaredField("pickerX"),pickerY=mc.currentScreen.getClass().getDeclaredField("pickerY");
            pickerX.setAccessible(true);pickerY.setAccessible(true);
            int sample=colors.getRGB((pickerX.getInt(mc.currentScreen)+3)*colors.getWidth()/mc.currentScreen.width,(pickerY.getInt(mc.currentScreen)+3)*colors.getHeight()/mc.currentScreen.height);
            require((sample>>16&255)+(sample>>8&255)+(sample&255)>540,"The HSV color canvas is actually visible in the framebuffer");
            click(102);require("Cyber Cyan".equals(config().ui.preset),"Theme palette button updates the actual persistent design tokens");
            click(21);
        } else if(responsiveStep==2){
            assertButtonsInsideScreen();capture("22-theme-style-motion-blur");
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"Custom font, rounded components and blur preserve a clean OpenGL state");
            config().ui.blur=0;config().ui.applyPreset("Alien Purple");
            resizeWindow(960,600);mc.gameSettings.guiScale=2;
            mc.displayGuiScreen(new dev.alien.client.gui.PanelMenuScreen(null));
        } else if(responsiveStep==3){
            assertButtonsInsideScreen();capture("23-clickgui-16x10");
            mc.displayGuiScreen(new HudEditorScreen(null));
            config().hud.widgets.get("fps").x=.5f;config().hud.widgets.get("fps").y=.45f;config().hud.widgets.get("fps").enabled=true;
        } else if(responsiveStep==4){
            assertButtonsInsideScreen();capture("24-hud-editor-16x10");
            resizeWindow(800,600);config().ui.menuScale=.85f;
            mc.displayGuiScreen(new dev.alien.client.gui.ThemeEditorScreen(null));
        } else if(responsiveStep==5){
            assertButtonsInsideScreen();capture("25-theme-editor-4x3-scaled");
            mc.displayGuiScreen(new CosmeticStudioScreen(null));setPreviewPose(35,1.1f);
        } else if(responsiveStep==6){
            assertButtonsInsideScreen();capture("26-cosmetic-studio-4x3-scaled");
            resizeWindow(854,480);config().ui.menuScale=1;
            dev.alien.client.gui.PanelMenuScreen panel=new dev.alien.client.gui.PanelMenuScreen(null);
            mc.displayGuiScreen(panel);panel.searchFor("sky");panel.selectModule("sky");
        } else if(responsiveStep==7){
            capture("27-module-settings-keybind");
            int mw=Math.min(360,mc.currentScreen.width-32),x=Math.max(12,(mc.currentScreen.width-mw)/2),y=(mc.currentScreen.height-Math.min(194,mc.currentScreen.height-28))/2;
            Method mouse=mc.currentScreen.getClass().getDeclaredMethod("mouseClicked",int.class,int.class,int.class);mouse.setAccessible(true);mouse.invoke(mc.currentScreen,x+30,y+130,0);
            Method key=mc.currentScreen.getClass().getDeclaredMethod("keyTyped",char.class,int.class);key.setAccessible(true);key.invoke(mc.currentScreen,'g',34);
            require(Integer.valueOf(34).equals(config().moduleKeys.get("sky")),"The actual ClickGUI key editor assigns a module shortcut");
            config().moduleKeys.remove("sky");pickerColor=config().ui.primary;
            mc.displayGuiScreen(new dev.alien.client.gui.ui.ColorPickerScreen(null,"Live RGBA / HSV",pickerColor,true,v->config().ui.primary=v));
        } else if(responsiveStep==8){
            assertButtonsInsideScreen();capture("28-color-picker-hsv-rgba");
            mc.displayGuiScreen(new AlienMenuScreen(null).openPage(7));
        } else if(responsiveStep==9){
            capture("29-config-profiles");config().ui.primary=pickerColor;config().ui.blur=0;
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"Responsive editors and custom controls preserve OpenGL state across window sizes");
            if("unified".equals(BuildEdition.ID)) {
                dev.alien.client.gui.PanelMenuScreen panel=new dev.alien.client.gui.PanelMenuScreen(null);
                mc.displayGuiScreen(panel);panel.searchFor("aim");panel.selectModule("aim_assist");
            } else mc.displayGuiScreen(new dev.alien.client.world.SkyboxOptionsScreen(null));
        } else if(responsiveStep==10 && "unified".equals(BuildEdition.ID)){
            require(!config().combat.enabled,"Opening redesigned combat module details preserves the master OFF state");
            capture("32-unified-combat-module-details");
            int mw=Math.min(360,mc.currentScreen.width-32),mh=Math.min(194,mc.currentScreen.height-28);
            int x=Math.max(12,(mc.currentScreen.width-mw)/2),y=(mc.currentScreen.height-mh)/2;
            Method mouse=mc.currentScreen.getClass().getDeclaredMethod("mouseClicked",int.class,int.class,int.class);
            mouse.setAccessible(true);mouse.invoke(mc.currentScreen,x+30,y+mh-24,0);
            require(mc.currentScreen instanceof dev.alien.client.gui.CombatStudioScreen,"Redesigned module details open the preserved Unified Combat Studio");
        } else if(responsiveStep==11 && "unified".equals(BuildEdition.ID)){
            assertButtonsInsideScreen();capture("33-unified-combat-studio");
            click(31);
            require(config().combat.aimAssist&&!config().combat.enabled,"A combat child switch changes its setting without silently enabling the master");
            click(31);
            resizeWindow(800,600);config().ui.menuScale=.85f;
            mc.displayGuiScreen(new dev.alien.client.world.SkyboxOptionsScreen(null));
        } else if(responsiveStep==20){
            assertButtonsInsideScreen();capture("35-precise-numeric-input");
            Field input=mc.currentScreen.getClass().getDeclaredField("input");input.setAccessible(true);
            String value=((net.minecraft.client.gui.GuiTextField)input.get(mc.currentScreen)).getText();
            require(Float.parseFloat(value)==.0001f,"Numeric input preserves the precision of the minimum fog density");
            click(1);
            require(config().world.fogDensity==.0001f && mc.currentScreen instanceof dev.alien.client.world.SkyboxOptionsScreen,
                    "Applying an unchanged precise numeric value succeeds and restores its parent screen");
        } else if(responsiveStep==21){
            assertButtonsInsideScreen();capture("36-sky-options-minimal-white");
            mc.displayGuiScreen(new dev.alien.client.world.SkyboxGalleryScreen(null));
        } else if(responsiveStep==22){
            assertButtonsInsideScreen();capture("37-sky-gallery-minimal-white");
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"Minimal White studio screens and numeric editing preserve OpenGL state");
            config().ui.applyPreset("Alien Purple");
            AlienClient.get().store().deleteProfile("smoke_integration");
            responsiveDone=true;disconnectRequested=true;
        } else {
            assertButtonsInsideScreen();capture("34-skybox-options-scaled");
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"Preserved Sky Options render with modern components and no OpenGL errors");
            config().ui.menuScale=1;resizeWindow(854,480);config().ui.applyPreset("Minimal White");
            config().world.fogDensity=.0001f;
            mc.displayGuiScreen(new dev.alien.client.gui.ui.ValueInputScreen(
                    new dev.alien.client.world.SkyboxOptionsScreen(null),"Fog density",.0001f,.04f,.0001f,v->config().world.fogDensity=v));
            responsiveStep=19;
        }
        responsiveStep++;since=System.nanoTime();
    }
    private void resizeWindow(int w,int h) throws Exception {
        org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(w,h));
        Method resize=Minecraft.class.getDeclaredMethod("resize",int.class,int.class);resize.setAccessible(true);resize.invoke(mc,w,h);
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
