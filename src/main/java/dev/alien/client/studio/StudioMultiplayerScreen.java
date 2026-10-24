package dev.alien.client.studio;
import dev.alien.client.AlienClient;import dev.alien.client.gui.Ui;import dev.alien.client.gui.ui.BackdropBlur;import net.minecraft.client.gui.*;import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.Tessellator;import net.minecraftforge.fml.relauncher.ReflectionHelper;import org.lwjgl.opengl.GL11;import java.io.IOException;
/** Native entries, LAN discovery, ping and connection over a transparent, clipped list. */
public final class StudioMultiplayerScreen extends GuiMultiplayer {
    private final GuiScreen back;
    public StudioMultiplayerScreen(GuiScreen parent){super(parent);back=parent;}
    private static boolean scene(){return AlienClient.get().config().studio.menuScene&&Minecraft.getMinecraft().theWorld==null;}
    @Override public void initGui(){super.initGui();ServerSelectionList previous=ReflectionHelper.getPrivateValue(GuiMultiplayer.class,this,"serverListSelector","field_146803_h");if(!(previous instanceof SceneServers)){SceneServers list=new SceneServers(this,mc,width,height);list.func_148195_a(getServerList());list.setSelectedSlotIndex(previous.func_148193_k());ReflectionHelper.setPrivateValue(GuiMultiplayer.class,this,list,"serverListSelector","field_146803_h");}}
    @Override public void drawWorldBackground(int tint){if(!MenuScene.render(width,height)){super.drawWorldBackground(tint);return;}BackdropBlur.renderMenu(.45f);Ui.rounded(0,0,width,height,0,0x66301846);}
    @Override protected void actionPerformed(GuiButton b)throws IOException{if(b.id==8&&b.enabled){mc.displayGuiScreen(new StudioMultiplayerScreen(back));return;}super.actionPerformed(b);}
    @Override protected void keyTyped(char ch,int key)throws IOException{if(key==org.lwjgl.input.Keyboard.KEY_F5){mc.displayGuiScreen(new StudioMultiplayerScreen(back));return;}super.keyTyped(ch,key);}
    private static final class SceneServers extends ServerSelectionList {
        SceneServers(GuiMultiplayer owner,Minecraft mc,int w,int h){super(owner,mc,w,h,32,h-64,36);}
        @Override protected void drawContainerBackground(Tessellator t){if(!scene())super.drawContainerBackground(t);}
        @Override protected void overlayBackground(int start,int end,int a,int b){if(!scene())super.overlayBackground(start,end,a,b);}
        @Override public void drawScreen(int x,int y,float partial){if(!scene()){super.drawScreen(x,y,partial);return;}Minecraft m=Minecraft.getMinecraft();float scale=m.displayWidth/(float)this.width;GL11.glPushAttrib(GL11.GL_SCISSOR_BIT|GL11.GL_ENABLE_BIT);try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(0,m.displayHeight-Math.round(bottom*scale),m.displayWidth,Math.round((bottom-top)*scale));super.drawScreen(x,y,partial);}finally{GL11.glPopAttrib();}}
    }
}
