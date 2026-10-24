package dev.alien.client.studio;
import dev.alien.client.gui.Ui;import dev.alien.client.gui.ui.BackdropBlur;import net.minecraft.client.gui.*;
/** Vanilla server list, ping and connect behavior over the real menu framebuffer. */
public final class StudioMultiplayerScreen extends GuiMultiplayer {
    public StudioMultiplayerScreen(GuiScreen parent){super(parent);}
    @Override public void drawWorldBackground(int tint){if(!MenuScene.render(width,height)){super.drawWorldBackground(tint);return;}BackdropBlur.renderMenu(.45f);Ui.rounded(0,0,width,height,0,0x66301846);}
}
