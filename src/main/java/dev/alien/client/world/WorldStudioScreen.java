package dev.alien.client.world;

import dev.alien.client.AlienClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Locale;

/** Controls update render settings live; the game world's state is never changed. */
public final class WorldStudioScreen extends GuiScreen {
    private final GuiScreen parent;
    private GuiTextField topColor, bottomColor, skybox;
    private String message = "Local visuals only - server conditions stay unchanged";
    private int left, columnWidth, second;

    public WorldStudioScreen(GuiScreen parent) { this.parent = parent; }
    private WorldConfig settings() { return AlienClient.get().config().world; }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        int panelWidth = Math.min(width - 24, 500);
        left = (width-panelWidth)/2; columnWidth=(panelWidth-8)/2; second=left+columnWidth+8;
        final WorldConfig c = settings();
        int y = 33;
        add(1, left, y, "Custom sky: " + on(c.enabled)); add(2, second, y, "Theme: " + c.theme); y+=22;
        add(3, left, y, "Visual time: " + (c.timeOverride ? "Local" : "Server"));
        buttonList.add(new StudioSlider(30, second, y, columnWidth, "Hour", 0, 23, c.visualHour, new StudioSlider.Change() {
            @Override public void set(float value) { c.visualHour = Math.round(value); }
        })); y+=22;
        add(4, left, y, "Custom fog: " + on(c.fogEnabled));
        buttonList.add(new StudioSlider(31, second, y, columnWidth, "Fog", 0.0001F, .025F, c.fogDensity, new StudioSlider.Change() {
            @Override public void set(float value) { c.fogDensity = value; }
        })); y+=22;
        topColor = new GuiTextField(40, fontRendererObj, left, y, columnWidth, 20);
        topColor.setMaxStringLength(6); topColor.setText(hex(c.skyTop));
        bottomColor = new GuiTextField(41, fontRendererObj, second, y, columnWidth, 20);
        bottomColor.setMaxStringLength(6); bottomColor.setText(hex(c.skyBottom)); y+=22;
        add(5, left, y, "Sun / moon: " + on(c.sunMoon));
        buttonList.add(new StudioSlider(32, second, y, columnWidth, "Stars", 0, 1000, c.stars, new StudioSlider.Change() {
            @Override public void set(float value) { c.stars = Math.round(value); }
        })); y+=22;
        add(6, left, y, "Weather: " + c.weather);
        buttonList.add(new StudioSlider(33, second, y, columnWidth, "Intensity", 0, 1, c.weatherIntensity, new StudioSlider.Change() {
            @Override public void set(float value) { c.weatherIntensity=value; }
        })); y+=22;
        skybox = new GuiTextField(42, fontRendererObj, left, y, columnWidth, 20);
        skybox.setMaxStringLength(120); skybox.setText(c.skyboxFile);
        add(7, second, y, "Apply RGB / PNG"); y+=22;
        add(8, left, y, "Reset world visuals"); add(9, second, y, "Open visual effects");
        int footerWidth = Math.min(118, Math.max(48, (width - 38) / 4));
        int footerLeft = (width - (footerWidth * 4 + 18)) / 2;
        buttonList.add(new GuiButton(10, footerLeft, height-25, footerWidth,20,"Atmosphere"));
        buttonList.add(new GuiButton(12, footerLeft+footerWidth+6,height-25,footerWidth,20,"Sky Gallery"));
        buttonList.add(new GuiButton(11, footerLeft+footerWidth*2+12,height-25,footerWidth,20,"Sky options"));
        buttonList.add(new GuiButton(0, footerLeft+footerWidth*3+18,height-25,footerWidth,20,"Done"));
    }

    private void add(int id, int x, int y, String label) { buttonList.add(new GuiButton(id,x,y,columnWidth,20,label)); }
    private static String on(boolean value) { return value ? "ON" : "OFF"; }
    private static String hex(int color) { return String.format(Locale.ROOT, "%06X", color & 0xFFFFFF); }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        WorldConfig c = settings();
        switch (button.id) {
            case 0: applyFields(); mc.displayGuiScreen(parent); return;
            case 1: c.enabled=!c.enabled; break;
            case 2: {
                String[] themes={"Nebula","Galaxy","Aurora","Japanese","Crystal","Sakura"};
                int index=0; for(int i=0;i<themes.length;i++) if(themes[i].equals(c.theme)) index=i;
                c.applyTheme(themes[(index+1)%themes.length]); break;
            }
            case 3: c.timeOverride=!c.timeOverride; break;
            case 4: c.fogEnabled=!c.fogEnabled; break;
            case 5: c.sunMoon=!c.sunMoon; break;
            case 6: {
                String[] modes={"Server","Clear","Rain","Snow","Storm"};
                int index=0; for(int i=0;i<modes.length;i++) if(modes[i].equals(c.weather)) index=i;
                c.weather=modes[(index+1)%modes.length]; break;
            }
            case 7: applyFields(); break;
            case 8: AlienClient.get().config().world=new WorldConfig(); break;
            case 9: applyFields(); mc.displayGuiScreen(new EffectsScreen(this)); return;
            case 10: applyFields(); mc.displayGuiScreen(new AtmospherePresetScreen(this)); return;
            case 11: applyFields(); mc.displayGuiScreen(new SkyboxOptionsScreen(this)); return;
            case 12: applyFields(); mc.displayGuiScreen(new SkyboxGalleryScreen(this)); return;
            default: return;
        }
        AlienClient.get().save(); initGui();
    }

    private void applyFields() {
        try {
            if (!topColor.getText().matches("[0-9a-fA-F]{6}") || !bottomColor.getText().matches("[0-9a-fA-F]{6}")) {
                message="Use exactly six hexadecimal digits in the color fields"; return;
            }
            WorldConfig c=settings(); c.skyTop=Integer.parseInt(topColor.getText(),16); c.skyBottom=Integer.parseInt(bottomColor.getText(),16);
            c.fogColor=c.skyBottom; c.skyboxFile=skybox.getText().trim(); c.normalize();
            AlienClient.get().save(); message="PNG folder: config/" + dev.alien.client.edition.BuildEdition.CONFIG_DIRECTORY + "/skyboxes";
        } catch (NumberFormatException invalid) { message="Invalid color"; }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj,"ALIEN / WORLD STUDIO",width/2,10,AlienClient.get().config().accentColor);
        topColor.drawTextBox(); bottomColor.drawTextBox(); skybox.drawTextBox();
        super.drawScreen(mouseX,mouseY,partialTicks);
        if (height>290) {
            drawCenteredString(fontRendererObj,message,width/2,height-56,0xC4BCD3);
            drawCenteredString(fontRendererObj,WorldManager.lastImportMessage,width/2,height-43,0x948CA5);
        }
        if (topColor.isFocused() || bottomColor.isFocused()) drawCenteredString(fontRendererObj,"Sky top / horizon RGB hex (RRGGBB)",width/2,22,0xB9ADC9);
        else if (skybox.isFocused()) drawCenteredString(fontRendererObj,"PNG or .cube.png atlas; empty = procedural sky",width/2,22,0xB9ADC9);
    }

    @Override protected void keyTyped(char character,int keyCode) throws IOException {
        if(keyCode==Keyboard.KEY_ESCAPE) { applyFields(); mc.displayGuiScreen(parent); return; }
        topColor.textboxKeyTyped(character,keyCode); bottomColor.textboxKeyTyped(character,keyCode); skybox.textboxKeyTyped(character,keyCode);
    }

    @Override protected void mouseClicked(int x,int y,int button) throws IOException {
        topColor.mouseClicked(x,y,button); bottomColor.mouseClicked(x,y,button); skybox.mouseClicked(x,y,button);
        super.mouseClicked(x,y,button);
    }

    @Override public void updateScreen() { topColor.updateCursorCounter(); bottomColor.updateCursorCounter(); skybox.updateCursorCounter(); }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); AlienClient.get().save(); }
    @Override public boolean doesGuiPauseGame() { return false; }
}
