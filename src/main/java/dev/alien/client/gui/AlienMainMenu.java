package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import dev.alien.client.edition.BuildEdition;
import java.io.IOException;
import java.util.Locale;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.GuiModList;

/** Original vector identity and custom controls; no vanilla menu textures. */
public final class AlienMainMenu extends GuiMainMenu {
    private int left,top,panelWidth,panelHeight,actionX,actionWidth,heroWidth;
    private int nativeWidth,nativeHeight;
    private float appliedScale=1;
    private long opened=System.nanoTime();
    private boolean sceneMode;

    @Override public void setWorldAndResolution(net.minecraft.client.Minecraft mc,int w,int h){
        nativeWidth=w;nativeHeight=h;appliedScale=Math.min(Ui.theme().menuScale,Math.min(w/320f,h/240f));
        super.setWorldAndResolution(mc,Math.round(w/appliedScale),Math.round(h/appliedScale));
    }
    @Override public void drawScreen(int x,int y,float partial){
        float desired=Math.min(Ui.theme().menuScale,Math.min(nativeWidth/320f,nativeHeight/240f));
        if(desired!=appliedScale&&!org.lwjgl.input.Mouse.isButtonDown(0))setWorldAndResolution(mc,nativeWidth,nativeHeight);
        org.lwjgl.opengl.GL11.glPushMatrix();try{org.lwjgl.opengl.GL11.glScalef(appliedScale,appliedScale,1);drawContents(Math.round(x/appliedScale),Math.round(y/appliedScale),partial);}finally{org.lwjgl.opengl.GL11.glPopMatrix();}
    }
    @Override public void initGui(){
        fontRendererObj=Ui.font();buttonList.clear();sceneMode=AlienClient.get().config().studio.menuScene;
        if(sceneMode){actionWidth=Math.min(235,width-36);actionX=(width-actionWidth)/2;int h=Math.max(21,Math.min(27,(height-130)/5)),y=Math.max(117,height/2-12);buttonList.add(new MenuAction(1,actionX,y,actionWidth,h,"Singleplayer","world"));buttonList.add(new MenuAction(2,actionX,y+h+5,actionWidth,h,"Multiplayer","grid"));buttonList.add(new MenuAction(3,actionX,y+(h+5)*2,actionWidth,h,"Alien Studios","spark"));buttonList.add(new Ui.Button(4,actionX,y+(h+5)*3,(actionWidth-6)/2,22,"Options"));buttonList.add(new Ui.Button(5,actionX+(actionWidth+6)/2,y+(h+5)*3,(actionWidth-6)/2,22,"Quit"));buttonList.add(new Ui.Button(6,width-62,8,52,20,"Mods"));if(height<300){buttonList.add(new Ui.Button(7,10,8,64,20,"Customize"));buttonList.add(new Ui.Button(8,80,8,80,20,"Gallery"));buttonList.add(new Ui.Button(9,166,8,80,20,"Scene"));}else{buttonList.add(new Ui.Button(7,10,8,78,20,"Customize"));buttonList.add(new Ui.Button(8,94,8,97,20,"Module Gallery"));buttonList.add(new Ui.Button(9,width-120,height-40,108,22,"Scene controls"));}return;}

        panelWidth=Math.min(width-24,710);
        panelHeight=Math.min(height-24,360);left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        heroWidth=Math.round(panelWidth*.49f);actionX=left+heroWidth+8;actionWidth=panelWidth-heroWidth-24;
        int buttonHeight=Math.max(21,Math.min(31,(panelHeight-84)/4));
        int y=top+48;
        buttonList.add(new MenuAction(1,actionX,y,actionWidth,buttonHeight,"Singleplayer","world"));
        buttonList.add(new MenuAction(2,actionX,y+buttonHeight+7,actionWidth,buttonHeight,"Multiplayer","grid"));
        buttonList.add(new MenuAction(3,actionX,y+(buttonHeight+7)*2,actionWidth,buttonHeight,"Alien Studios","spark"));
        int footerY=y+(buttonHeight+7)*3;
        buttonList.add(new Ui.Button(4,actionX,footerY,(actionWidth-6)/2,21,"Options"));
        buttonList.add(new Ui.Button(5,actionX+(actionWidth+6)/2,footerY,(actionWidth-6)/2,21,"Quit"));
        buttonList.add(new Ui.Button(6,width-60,7,50,18,"Mods"));
        buttonList.add(new Ui.Button(7,10,7,75,18,"Customize"));
        buttonList.add(new Ui.Button(8,91,7,85,18,"Module Gallery"));
        buttonList.add(new Ui.Button(9,width-112,height-40,100,20,"Scene controls"));
    }
    public void drawContents(int mouseX,int mouseY,float partialTicks){
        if(sceneMode){drawInteractive(mouseX,mouseY,partialTicks);return;}
        int surface=AlienClient.get().config().ui.surface,raised=AlienClient.get().config().ui.surfaceRaised;
        int text=AlienClient.get().config().ui.text,muted=AlienClient.get().config().ui.muted;
        if(!dev.alien.client.studio.MenuScene.render(width,height))drawGradientRect(0,0,width,height,Ui.mix(surface,0xff02040a,.28f),surface);
        double phase=AlienClient.get().config().animations&&!AlienClient.get().config().ui.reducedMotion?System.currentTimeMillis()/5500d:0;
        float glowX=(float)(width*.26+Math.sin(phase)*width*.025),glowY=(float)(height*.48+Math.cos(phase)*height*.025);
        for(int i=8;i>=1;i--){
            float size=34+i*14;
            Ui.rounded(glowX-size,glowY-size,size*2,size*2,size,Ui.alpha(Ui.accent(),.012f));
        }
        for(int i=0;i<27;i++){
            int x=Math.floorMod(i*137+43,Math.max(1,width)),y=Math.floorMod(i*73+11,Math.max(1,height));
            Ui.rounded(x,y,1.2f,1.2f,.6f,Ui.alpha(AlienClient.get().config().ui.secondary,.16f));
        }
        Ui.shadow(left,top,panelWidth,panelHeight,14,AlienClient.get().config().ui.shadow);
        Ui.rounded(left,top,panelWidth,panelHeight,14,Ui.alpha(surface,AlienClient.get().config().ui.opacity));
        Ui.rounded(actionX-9,top+11,actionWidth+18,panelHeight-22,10,raised);
        int heroCenter=left+heroWidth/2;
        Ui.rounded(heroCenter-26,top+30,52,52,16,Ui.mix(surface,Ui.accent(),.1f));
        Ui.icon("alien",heroCenter-17,top+39,34,Ui.accent());
        GlStateManager.pushMatrix();
        try{
            GlStateManager.translate(heroCenter,top+94,0);GlStateManager.scale(2.5f,2.5f,1);
            drawCenteredString(fontRendererObj,"ALIEN",0,0,text);
        }finally{GlStateManager.popMatrix();}
        String edition=BuildEdition.ID.toUpperCase(Locale.ROOT)+" EDITION";
        drawCenteredString(fontRendererObj,edition,heroCenter,top+124,muted);
        if(panelHeight>230){
            drawCenteredString(fontRendererObj,"Your world. Reimagined.",heroCenter,top+155,text);
            drawCenteredString(fontRendererObj,"Make every detail yours.",heroCenter,top+172,muted);
        }else if(panelWidth>360)drawCenteredString(fontRendererObj,"Your world. Reimagined.",heroCenter,top+151,muted);
        String badge="unified".equals(BuildEdition.ID)?"Unified / experimental":BuildEdition.ALLOW_FULL?"Architecture preview":"Visuals & performance";
        int badgeW=Math.min(heroWidth-20,fontRendererObj.getStringWidth(badge)+18);
        Ui.rounded(heroCenter-badgeW/2,top+panelHeight-33,badgeW,18,9,Ui.mix(surface,Ui.accent(),.12f));
        drawCenteredString(fontRendererObj,badge,heroCenter,top+panelHeight-28,Ui.accent());
        fontRendererObj.drawString("LET'S PLAY",actionX+1,top+25,muted);
        for(GuiButton button:buttonList)button.drawButton(mc,mouseX,mouseY);
        fontRendererObj.drawString("Minecraft Java 1.8.9 / Forge",12,height-15,muted);
        String version="ALIEN "+AlienClient.VERSION;
        fontRendererObj.drawString(version,width-fontRendererObj.getStringWidth(version)-12,height-15,muted);
        float intro=Math.min(1,(System.nanoTime()-opened)/220000000f);
        if(AlienClient.get().config().animations&&!AlienClient.get().config().ui.reducedMotion&&intro<1)Ui.rounded(left,top,panelWidth,panelHeight,14,Ui.alpha(surface,(1-intro)*.65f));
    }
    private void drawInteractive(int x,int y,float partial){dev.alien.client.studio.MenuScene.render(width,height);Ui.gradient(0,0,width,height,0,0x10210e35,0x6631204c);int center=width/2,logoY=Math.max(33,height/2-110);Ui.shadow(center-22,logoY,44,44,15,.4f);Ui.rounded(center-22,logoY,44,44,15,Ui.alpha(Ui.theme().surface,.82f));Ui.icon("alien",center-16,logoY+6,32,Ui.accent());GlStateManager.pushMatrix();try{GlStateManager.translate(center,logoY+52,0);GlStateManager.scale(2.1f,2.1f,1);drawCenteredString(fontRendererObj,"ALIEN",0,0,Ui.theme().text);}finally{GlStateManager.popMatrix();}drawCenteredString(fontRendererObj,"YOUR WORLD. REIMAGINED.",center,logoY+78,Ui.theme().text);for(GuiButton b:buttonList)b.drawButton(mc,x,y);Ui.text("Minecraft 1.8.9 / "+AlienClient.VERSION,12,height-15,0xffeee8ff);}
    @Override protected void actionPerformed(GuiButton button)throws IOException{
        if(button.id==1)mc.displayGuiScreen(new GuiSelectWorld(this));
        if(button.id==2)mc.displayGuiScreen(new dev.alien.client.studio.StudioMultiplayerScreen(this));
        if(button.id==3)mc.displayGuiScreen(AlienMenuScreen.create(this));
        if(button.id==4)mc.displayGuiScreen(new GuiOptions(this,mc.gameSettings));
        if(button.id==5)mc.shutdown();
        if(button.id==6)mc.displayGuiScreen(new GuiModList(this));
        if(button.id==7)mc.displayGuiScreen(new ThemeEditorScreen(this));
        if(button.id==8)mc.displayGuiScreen(new dev.alien.client.studio.ModuleGalleryScreen(this));
        if(button.id==9)mc.displayGuiScreen(new dev.alien.client.studio.MenuSceneScreen(this));
    }
    @Override public void updateScreen(){ }
    @Override protected void keyTyped(char character,int key){ }
    private final class MenuAction extends Ui.Button{
        private final String icon;
        MenuAction(int id,int x,int y,int w,int h,String label,String icon){super(id,x,y,w,h,label);this.icon=icon;}
        @Override public void drawButton(net.minecraft.client.Minecraft minecraft,int mx,int my){
            hovered=mx>=xPosition&&my>=yPosition&&mx<xPosition+width&&my<yPosition+height;
            int base=AlienClient.get().config().ui.surface;
            Ui.rounded(xPosition,yPosition,width,height,7,Ui.alpha(Ui.mix(base,Ui.accent(),hovered?.28f:.12f),sceneMode?.85f:1));
            Ui.icon(icon,xPosition+10,yPosition+(height-12)/2f,12,Ui.accent());
            fontRendererObj.drawString(displayString,xPosition+31,yPosition+(height-10)/2,AlienClient.get().config().ui.text);
            fontRendererObj.drawString(">",xPosition+width-15,yPosition+(height-10)/2,AlienClient.get().config().ui.muted);
        }
    }
}
