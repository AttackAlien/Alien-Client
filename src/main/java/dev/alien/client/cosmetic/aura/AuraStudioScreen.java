package dev.alien.client.cosmetic.aura;

import dev.alien.client.AlienClient;
import dev.alien.client.cosmetic.CosmeticConfig;
import dev.alien.client.gui.*;
import dev.alien.client.gui.ui.ColorPickerScreen;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderManager;
import org.lwjgl.input.*;
import org.lwjgl.opengl.GL11;

/** Appearance and real-event reaction controls backed by the current Unified look. */
public final class AuraStudioScreen extends PremiumScreen {
    public static boolean drawingPreview;
    public static CosmeticConfig.Accessory previewAccessory;
    private final GuiScreen parent;private final Supplier<CosmeticConfig.Accessory> accessory;
    private int page,scroll,left,right,column,top,bottom;
    public AuraStudioScreen(GuiScreen parent,Supplier<CosmeticConfig.Accessory> accessory){this.parent=parent;this.accessory=accessory;}
    private CosmeticConfig.Accessory a(){return accessory.get();}private AuraDesign d(){return a().auraDesign;}
    private void button(int id,int row,String label){buttonList.add(new Ui.Button(id,left+10,top+27+row*29-scroll,column-20,24,label));}
    private void slider(int id,int row,String label,float min,float max,float step,float value,java.util.function.Consumer<Float> set){buttonList.add(new Ui.Slider(id,left+10,top+27+row*29-scroll,column-20,24,label,min,max,step,value,v->{set.accept(v);d().preset="Custom";AlienClient.get().save();}));}
    @Override public void initGui(){
        fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(880,width-24);left=(width-w)/2;column=(w-12)/2;right=left+column+12;top=66;bottom=height-42;
        buttonList.add(new Ui.Button(2,left,35,100,22,"Appearance").selected(page==0));buttonList.add(new Ui.Button(3,left+106,35,100,22,"Reactions").selected(page==1));
        buttonList.add(new Ui.Button(0,left+w-54,8,54,22,"Done"));buttonList.add(new Ui.Button(1,left,height-30,(w-8)/2,22,"Back to world"));
        GuiButton emote=new Ui.Button(4,left+(w+8)/2,height-30,(w-8)/2,22,"Play selected emote");emote.enabled=mc.thePlayer!=null&&AlienClient.get().config().animations&&AlienClient.get().config().emotes.enabled;buttonList.add(emote);
        if(page==0){
            button(10,0,"Aura: "+(a().aura?"ON":"OFF"));button(11,1,"Renderer / "+(d().enabled?"Reactive":"Classic particles"));
            button(12,2,"Preset / "+d().preset);button(13,3,"Shape / "+d().style);button(14,4,"Primary / #"+String.format("%06X",d().color));button(15,5,"Accent / #"+String.format("%06X",d().secondaryColor));
            slider(20,6,"Radius",.35f,1.4f,.05f,d().radius,v->d().radius=v);slider(21,7,"Opacity",.1f,1,.05f,d().opacity,v->d().opacity=v);
            slider(22,8,"Intensity",.2f,1.5f,.05f,d().intensity,v->d().intensity=v);slider(23,9,"Animation speed",.2f,2,.05f,d().speed,v->d().speed=v);
        }else{
            button(30,0,"Movement: "+(d().motion?"ON":"OFF"));button(31,1,"Emotes: "+(d().emotes?"ON":"OFF"));button(32,2,"Night: "+(d().night?"ON":"OFF"));
            slider(33,3,"Movement boost",0,1.5f,.05f,d().motionGain,v->d().motionGain=v);slider(34,4,"Emote boost",0,1.5f,.05f,d().emoteGain,v->d().emoteGain=v);slider(35,5,"Night boost",0,1.5f,.05f,d().nightGain,v->d().nightGain=v);
            slider(36,6,"Response seconds",.1f,1,.05f,d().response,v->d().response=v);slider(37,7,"Idle energy",0,.7f,.05f,d().idle,v->d().idle=v);
        }
        for(GuiButton b:buttonList)if(b.id>=13)b.enabled=d().enabled;
    }
    private boolean content(GuiButton b){return b.id>=10;}
    @Override public void drawContents(int mx,int my,float partial){
        Ui.background(width,height);Ui.text("ALIEN / AURA STUDIO",left,15,Ui.theme().text);Ui.card(left,top,column,bottom-top);Ui.card(right,top,column,bottom-top);
        Ui.text(page==0?"AURA DESIGN":"REAL WORLD TRIGGERS",left+10,top+9,Ui.theme().muted);
        for(GuiButton b:buttonList)if(!content(b))b.drawButton(mc,mx,my);
        float factor=framebufferScale();GL11.glPushAttrib(GL11.GL_SCISSOR_BIT|GL11.GL_ENABLE_BIT);
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(left*factor),mc.displayHeight-Math.round(bottom*factor),Math.round(column*factor),Math.round((bottom-top-24)*factor));for(GuiButton b:buttonList)if(content(b))b.drawButton(mc,mx,my);}finally{GL11.glPopAttrib();}
        Ui.text("LIVE PLAYER PREVIEW",right+12,top+10,Ui.theme().muted);drawPlayer();
        String status=!AlienClient.get().config().cosmetics.enabled?"Cosmetics master is OFF":!a().aura?"Aura OFF":!d().enabled?"Classic / particle aura":!a().animations||!AlienClient.get().config().animations?"Animations are OFF":"Move / emote / night react live";
        Ui.centered(fontRendererObj.trimStringToWidth(status,column-20),right+column/2,bottom-15,Ui.accent());
    }
    private void drawPlayer(){
        if(mc.thePlayer==null){Ui.centered("Enter a world to preview",right+column/2,top+55,Ui.theme().muted);return;}
        RenderManager manager=mc.getRenderManager();float viewY=manager.playerViewY,viewX=manager.playerViewX;boolean shadow=manager.isRenderShadow();boolean old=drawingPreview;CosmeticConfig.Accessory previous=previewAccessory;
        float factor=framebufferScale();GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(right*factor),mc.displayHeight-Math.round((bottom-24)*factor),Math.round(column*factor),Math.round((bottom-top-48)*factor));
            drawingPreview=true;previewAccessory=a();int size=Math.max(18,Math.min((bottom-top-57)/2,Math.round((column-28)/(d().radius*2.5f+1))));
            GuiInventory.drawEntityOnScreen(right+column/2,bottom-33,size,-18,-22,mc.thePlayer);
        }finally{drawingPreview=old;previewAccessory=previous;manager.playerViewY=viewY;manager.playerViewX=viewX;manager.setRenderShadow(shadow);GL11.glPopAttrib();Ui.finishFrame();}
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(!b.enabled)return;
        if(b.id==0){mc.displayGuiScreen(parent);return;}if(b.id==1){mc.displayGuiScreen(null);return;}
        if(b.id==2||b.id==3){page=b.id-2;scroll=0;initGui();return;}
        if(b.id==4){if(AlienClient.get().emotes().request(AlienClient.get().config().emotes.selected))mc.displayGuiScreen(null);else Ui.notify(AlienClient.get().emotes().status());return;}
        if(b.id==10){a().aura=!a().aura;if(a().aura)AlienClient.get().config().cosmetics.enabled=true;}
        else if(b.id==11)d().enabled=!d().enabled;
        else if(b.id==12){int i=Arrays.asList(AuraDesign.PRESETS).indexOf(d().preset);d().applyPreset(AuraDesign.PRESETS[(i+1)%AuraDesign.PRESETS.length]);a().aura=true;AlienClient.get().config().cosmetics.enabled=true;}
        else if(b.id==13){d().style=AuraDesign.STYLES[(Arrays.asList(AuraDesign.STYLES).indexOf(d().style)+1)%AuraDesign.STYLES.length];d().preset="Custom";}
        else if(b.id==14||b.id==15){final boolean first=b.id==14;mc.displayGuiScreen(new ColorPickerScreen(this,first?"Aura primary":"Aura accent",(first?d().color:d().secondaryColor)|0xff000000,false,color->{if(first)d().color=color&0xffffff;else d().secondaryColor=color&0xffffff;d().preset="Custom";AlienClient.get().save();}));return;}
        else if(b.id==30)d().motion=!d().motion;else if(b.id==31)d().emotes=!d().emotes;else if(b.id==32)d().night=!d().night;
        AlienClient.get().save();initGui();
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button!=0)return;for(GuiButton b:buttonList)if((!content(b)||y>=top+24&&y<bottom)&&b.mousePressed(mc,x,y)){if(!(b instanceof Ui.Slider))actionPerformed(b);return;}}
    @Override protected void mouseReleased(int x,int y,int state){for(GuiButton b:buttonList)b.mouseReleased(x,y);}
    @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int delta=Mouse.getEventDWheel();if(delta!=0){int content=(page==0?10:8)*29;scroll=Math.max(0,Math.min(Math.max(0,content-(bottom-top-27)),scroll+(delta<0?29:-29)));initGui();}}
    @Override protected void keyTyped(char ch,int key)throws IOException{if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
