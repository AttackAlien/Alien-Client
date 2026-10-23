package dev.alien.client;

import dev.alien.client.config.ClientConfig;
import dev.alien.client.combat.CombatManager;
import dev.alien.client.combat.CombatSessionGuard;
import dev.alien.client.config.ConfigStore;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.module.ModuleManager;
import dev.alien.client.input.ModuleKeybindManager;
import dev.alien.client.cosmetic.CosmeticManager;
import dev.alien.client.gui.AlienMainMenu;
import dev.alien.client.gui.AlienMenuScreen;
import dev.alien.client.hud.HudManager;
import dev.alien.client.performance.PerformanceEngine;
import dev.alien.client.world.WorldManager;
import java.io.File;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Single Forge client entry point; combat is opt-in inside Unified. */
@Mod(modid = AlienClient.MOD_ID, name = BuildEdition.NAME, version = AlienClient.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]", acceptableRemoteVersions = "*")
public final class AlienClient {
    public static final String MOD_ID = "alienclient";
    public static final String VERSION = "0.3.0-alpha.1";
    public static final Logger LOG = LogManager.getLogger("AlienClient");
    @Mod.Instance(MOD_ID) private static AlienClient instance;
    private final CombatSessionGuard combatSession = new CombatSessionGuard();
    private ConfigStore store;
    private PerformanceEngine performance;
    private KeyBinding menuBinding;
    private ModuleManager modules;
    private ModuleKeybindManager moduleBindings;
    private int lastMenuKey;
    private String serverContext;
    private String appliedBinding;
    private String lastError = "";

    public static AlienClient get() { return instance; }
    public ClientConfig config() { return store.config(); }
    public ConfigStore store() { return store; }
    public PerformanceEngine performance() { return performance; }
    public CombatSessionGuard combatSession() { return combatSession; }
    public ModuleManager modules() { return modules; }
    public String lastError() { return lastError; }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        store = new ConfigStore(new File(event.getModConfigurationDirectory(), BuildEdition.CONFIG_DIRECTORY));
        try { store.load(); }
        catch (IOException e) { report("Could not load preferences; check logs/alien-client config backup", e); }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        menuBinding = new KeyBinding("key.alienclient.menu", config().menuKey, "key.categories.alienclient");
        lastMenuKey = config().menuKey;
        ClientRegistry.registerKeyBinding(menuBinding);
        modules = ModuleManager.visuals(() -> config(), BuildEdition.ALLOW_FULL);
        moduleBindings = new ModuleKeybindManager(modules, () -> config());
        performance = new PerformanceEngine();
        register(this); register(performance); register(new HudManager()); register(new WorldManager());
        if ("unified".equals(BuildEdition.ID)) register(new CombatManager());
        CosmeticManager cosmetics = new CosmeticManager();
        cosmetics.install(); register(cosmetics);
        LOG.info("{} {} initialized: {} registered visual modules; {}", BuildEdition.NAME, VERSION, modules.modules().size(), BuildEdition.STATUS);
    }

    private static void register(Object listener) {
        MinecraftForge.EVENT_BUS.register(listener);
        FMLCommonHandler.instance().bus().register(listener);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (menuBinding.isPressed() && mc.currentScreen == null) mc.displayGuiScreen(AlienMenuScreen.create(null));
        if (moduleBindings.consume(mc.currentScreen == null && mc.thePlayer != null)) save();
    }

    @SubscribeEvent
    public void onOpen(GuiOpenEvent event) {
        if (config().menuReplacement && event.gui != null && event.gui.getClass() == GuiMainMenu.class) event.gui = new AlienMainMenu();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (moduleBindings.synchronize()) save();
        if (lastMenuKey != config().menuKey) {
            menuBinding.setKeyCode(config().menuKey); KeyBinding.resetKeyBindingArrayAndHash();
            lastMenuKey = config().menuKey;
        } else if (menuBinding.getKeyCode() != lastMenuKey) {
            config().menuKey = menuBinding.getKeyCode(); lastMenuKey = config().menuKey; save();
        }
        String next = currentServerKey();
        String desiredBinding = next != null && config().perServerProfiles ? store.serverProfile(next) : null;
        if (java.util.Objects.equals(serverContext, next) && java.util.Objects.equals(appliedBinding, desiredBinding)) return;
        store.leaveServerProfile();
        serverContext = next;
        appliedBinding = desiredBinding;
        if (desiredBinding != null) {
            try { store.save(); store.loadServerProfile(next); }
            catch (IOException e) { report("Could not load server profile", e); }
        } else {
            save();
        }
    }

    public String currentServerKey() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return null;
        if (mc.isSingleplayer() && mc.getIntegratedServer() != null) return "local:" + mc.getIntegratedServer().getFolderName();
        return mc.getCurrentServerData() == null ? null : mc.getCurrentServerData().serverIP.toLowerCase(java.util.Locale.ROOT);
    }

    public void save() {
        try { store.save(); lastError = ""; }
        catch (IOException | IllegalArgumentException e) { report("Could not save Alien Client preferences", e); }
    }

    private void report(String message, Exception e) {
        lastError = message;
        LOG.error(message, e);
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7d[Alien]\u00a7c " + message));
    }
}
