package dev.alien.client.cosmetic.pet;

import dev.alien.client.AlienClient;
import dev.alien.client.cosmetic.*;
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

/** Live fitting room for the same mascot geometry and real-event reactions used in the world. */
public final class PetStudioScreen extends PremiumScreen {
    public static boolean drawingPreview;public static CosmeticConfig.Accessory previewAccessory;
    private final GuiScreen parent;private final Supplier<CosmeticConfig.Accessory> accessory;
    private boolean focus=true;private int page,scroll,left,right,column,top,bottom;
    public PetStudioScreen(GuiScreen parent,Supplier<CosmeticConfig.Accessory> accessory){this.parent=parent;this.accessory=accessory;}
    private CosmeticConfig.Accessory a(){return accessory.get();}private PetDesign d(){return a().pet;}
    private void button(int id,int row,String label){buttonList.add(new Ui.Button(id,left+10,top+27+row*29-scroll,column-20,24,label));}
    private void slider(int id,int row,String label,float min,float max,float value,java.util.function.Consumer<Float> set){buttonList.add(new Ui.Slider(id,left+10,top+27+row*29-scroll,column-20,24,label,min,max,.05f,value,v->{set.accept(v);AlienClient.get().save();}));}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(880,width-24);left=(width-w)/2;column=(w-12)/2;right=left+column+12;top=66;bottom=height-42;
        buttonList.add(new Ui.Button(2,left,35,100,22,"Appearance").selected(page==0));buttonList.add(new Ui.Button(3,left+106,35,100,22,"Behavior").selected(page==1));
        buttonList.add(new Ui.Button(5,left+212,35,Math.max(42,w-212),22,focus?"Player view":"Mascot view"));
        buttonList.add(new Ui.Button(0,left+w-54,8,54,22,"Done"));buttonList.add(new Ui.Button(1,left,height-30,(w-8)/2,22,"Back to world"));
        GuiButton emote=new Ui.Button(4,left+(w+8)/2,height-30,(w-8)/2,22,"Play selected emote");emote.enabled=mc.thePlayer!=null&&AlienClient.get().config().animations&&AlienClient.get().config().emotes.enabled;buttonList.add(emote);
        if(page==0){button(10,0,"Mascot: "+(d().enabled?"ON":"OFF"));button(11,1,"Mode / "+d().mode);button(12,2,"Palette / "+d().preset);button(13,3,"Body / #"+String.format("%06X",d().color));button(14,4,"Accent / #"+String.format("%06X",d().accent));
            slider(20,5,"Size",.5f,1.5f,d().size,v->d().size=v);slider(21,6,"Distance",.65f,2,d().distance,v->d().distance=v);slider(22,7,"Height",.65f,1.8f,d().height,v->d().height=v);slider(23,8,"Animation speed",.4f,1.8f,d().speed,v->d().speed=v);
            if("Shoulder".equals(d().mode))for(GuiButton b:buttonList)if(b.id==21||b.id==22)b.enabled=false;
        }else{button(30,0,"Animation: "+(d().animate?"ON":"OFF"));button(31,1,"React to emotes: "+(d().reactEmotes?"ON":"OFF"));button(32,2,"Sleep when idle: "+(d().sleep?"ON":"OFF"));button(33,3,"Side / "+(d().right?"Right":"Left"));button(34,4,"Reset mascot preferences");}
    }
    @Override public void drawContents(int x,int y,float partial){Ui.background(width,height);Ui.text("ALIEN / MASCOT STUDIO",left,15,Ui.theme().text);Ui.card(left,top,column,bottom-top);Ui.card(right,top,column,bottom-top);Ui.text(page==0?"YOUR ALIEN COMPANION":"REAL OWNER REACTIONS",left+10,top+9,Ui.theme().muted);
        for(GuiButton b:buttonList)if(b.id<10)b.drawButton(mc,x,y);float scale=framebufferScale();GL11.glPushAttrib(GL11.GL_SCISSOR_BIT|GL11.GL_ENABLE_BIT);
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(left*scale),mc.displayHeight-Math.round(bottom*scale),Math.round(column*scale),Math.round((bottom-top-24)*scale));for(GuiButton b:buttonList)if(b.id>=10)b.drawButton(mc,x,y);}finally{GL11.glPopAttrib();}
        Ui.text(focus?"ALIEN / AUTO FIT PREVIEW":"LIVE PLAYER + MASCOT",right+10,top+9,Ui.accent());if(focus)focusPreview(partial);else preview();
        String status=!AlienClient.get().config().cosmetics.enabled?"Cosmetics master is OFF":!a().enabled?"Selected look is hidden":!d().enabled?"Enable your mascot":!d().animate||!a().animations||!AlienClient.get().config().animations?"Animations are OFF":focus?"Player view shows size and position":"Local companion / "+d().mode;
        Ui.centered(fontRendererObj.trimStringToWidth(status,column-16),right+column/2,bottom-17,Ui.theme().muted);
    }
    private void focusPreview(float partial){if(mc.thePlayer==null){Ui.centered("Enter a world to preview",right+column/2,top+50,Ui.theme().muted);return;}float factor=framebufferScale();GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(right*factor),mc.displayHeight-Math.round((bottom-25)*factor),Math.round(column*factor),Math.round((bottom-top-46)*factor));GL11.glTranslated(right+column/2,top+(bottom-top)*.61,130);
            double scale=Math.min((bottom-top-65)/.9,(column-26)/.6)/(d().size*("Shoulder".equals(d().mode)?.65:1));GL11.glScaled(scale,-scale,scale);GL11.glRotated(12,1,0,0);GL11.glRotated(-18,0,1,0);PetManager.renderStudio(a(),partial);
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();Ui.finishFrame();}
    }
    private void preview(){if(mc.thePlayer==null){Ui.centered("Enter a world to preview",right+column/2,top+50,Ui.theme().muted);return;}
        RenderManager manager=mc.getRenderManager();float viewY=manager.playerViewY,viewX=manager.playerViewX;boolean shadow=manager.isRenderShadow(),old=drawingPreview;CosmeticConfig.Accessory previous=previewAccessory;float factor=framebufferScale();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(right*factor),mc.displayHeight-Math.round((bottom-25)*factor),Math.round(column*factor),Math.round((bottom-top-46)*factor));
            drawingPreview=true;previewAccessory=a();float radius="Shoulder".equals(d().mode)?.6f:d().distance+.45f;
            int size=Math.max(16,Math.min(Math.round((bottom-top-70)/2.3f),Math.round((column-24)/(2*radius))));GuiInventory.drawEntityOnScreen(right+column/2,bottom-37,size,0,-10,mc.thePlayer);
        }finally{drawingPreview=old;previewAccessory=previous;manager.playerViewY=viewY;manager.playerViewX=viewX;manager.setRenderShadow(shadow);GL11.glPopAttrib();Ui.finishFrame();}
    }
    @Override protected void actionPerformed(GuiButton b){if(!b.enabled)return;if(b.id==5){focus=!focus;initGui();return;}if(b.id==0){mc.displayGuiScreen(parent);return;}if(b.id==1){mc.displayGuiScreen(null);return;}if(b.id==2||b.id==3){page=b.id-2;scroll=0;initGui();return;}
        if(b.id==4){if(AlienClient.get().emotes().request(AlienClient.get().config().emotes.selected))mc.displayGuiScreen(null);else Ui.notify(AlienClient.get().emotes().status());return;}
        if(b.id==10){d().enabled=!d().enabled;if(d().enabled){a().enabled=true;AlienClient.get().config().cosmetics.enabled=true;}}
        else if(b.id==11)d().mode=PetDesign.MODES[(Arrays.asList(PetDesign.MODES).indexOf(d().mode)+1)%PetDesign.MODES.length];
        else if(b.id==12)d().applyPreset(PetDesign.PRESETS[(Arrays.asList(PetDesign.PRESETS).indexOf(d().preset)+1)%PetDesign.PRESETS.length]);
        else if(b.id==13||b.id==14){final boolean body=b.id==13;mc.displayGuiScreen(new ColorPickerScreen(this,body?"Mascot body":"Mascot accent",(body?d().color:d().accent)|0xff000000,false,color->{if(body)d().color=color&0xffffff;else d().accent=color&0xffffff;d().preset="Custom";AlienClient.get().save();}));return;}
        else if(b.id==30)d().animate=!d().animate;else if(b.id==31)d().reactEmotes=!d().reactEmotes;else if(b.id==32)d().sleep=!d().sleep;else if(b.id==33)d().right=!d().right;
        else if(b.id==34){boolean enabled=d().enabled;a().pet=new PetDesign();a().pet.enabled=enabled;}
        AlienClient.get().save();initGui();
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button!=0)return;for(GuiButton b:buttonList)if((b.id<10||y>=top+24&&y<bottom)&&b.mousePressed(mc,x,y)){if(!(b instanceof Ui.Slider))actionPerformed(b);return;}}
    @Override protected void mouseReleased(int x,int y,int state){for(GuiButton b:buttonList)b.mouseReleased(x,y);}
    @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int delta=Mouse.getEventDWheel();if(delta!=0){int total=(page==0?9:5)*29;scroll=Math.max(0,Math.min(Math.max(0,total-(bottom-top-27)),scroll+(delta<0?29:-29)));initGui();}}
    @Override protected void keyTyped(char ch,int key)throws IOException{if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
