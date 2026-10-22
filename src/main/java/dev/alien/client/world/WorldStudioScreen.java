package dev.alien.client.world;

import dev.alien.client.AlienClient;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.gui.Ui;
import dev.alien.client.gui.ui.ColorPickerScreen;
import dev.alien.client.gui.ui.ConfirmScreen;
import dev.alien.client.gui.ui.ModernTextField;
import dev.alien.client.gui.ui.Notifications;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

/** Palette gallery and real, reversible world rendering controls. */
public final class WorldStudioScreen extends dev.alien.client.gui.PremiumScreen {
    private final GuiScreen parent;
    private int page,left,mainX,mainW,bodyY,previewH;
    private ModernTextField skybox;
    private String message="Live in your world · local visual settings";
    private final List<String> imports=new ArrayList<String>();
    public WorldStudioScreen(GuiScreen parent){this.parent=parent;}
    private WorldConfig settings(){return AlienClient.get().config().world;}
    @Override public void initGui(){
        fontRendererObj=Ui.font();Keyboard.enableRepeatEvents(true);buttonList.clear();skybox=null;
        left=20;mainX=106;mainW=width-mainX-22;bodyY=57;
        previewH=page==3?46:Math.max(46,Math.min(96,height-176));
        buttonList.add(new Ui.Button(1,width-164,18,87,23,"Custom sky: "+on(settings().enabled)));
        buttonList.add(new Ui.Button(0,width-71,18,49,23,"Done"));
        String[] tabs={"Gallery","Atmosphere","Weather","Imports"};
        for(int i=0;i<tabs.length;i++)buttonList.add(new Ui.Button(70+i,left+3,58+i*25,71,22,tabs[i]).selected(page==i));
        buttonList.add(new Ui.Button(10,left+3,height-64,71,22,"Presets"));
        buttonList.add(new Ui.Button(3,mainX,height-31,Math.min(105,mainW/2-3),21,"Local time: "+on(settings().timeOverride)));
        int half=(mainW-8)/2;
        buttonList.add(new Ui.Slider(30,mainX+half+8,height-32,half,23,"Hour",0,23,1,settings().visualHour,v->settings().visualHour=Math.round(v)));
        int y=bodyY+previewH+8;
        WorldConfig c=settings();
        if(page==0){
            int rows=3,cardH=Math.max(18,Math.min(32,(height-40-y-8)/rows));
            for(int i=0;i<WorldStudioVisuals.THEMES.length;i++)buttonList.add(new WorldStudioVisuals.SkyCard(100+i,mainX+(i%2)*(half+8),y+(i/2)*(cardH+4),half,cardH,WorldStudioVisuals.THEMES[i],WorldStudioVisuals.THEMES[i].equals(c.theme)&&c.skyboxFile.isEmpty()));
        } else if(page==1){
            // Three compact rows fit the minimum 320 x 240 GUI size.
            buttonList.add(new Ui.Slider(40,mainX,y,half,23,"Sky intensity",.25f,1.5f,.01f,c.brightness,v->settings().brightness=v));
            buttonList.add(new Ui.Button(41,mainX+half+8,y,half,22,"Zenith color"));y+=27;
            buttonList.add(new Ui.Button(42,mainX,y,half,22,"Horizon color"));
            buttonList.add(new Ui.Button(44,mainX+half+8,y,half,22,"Clouds: "+c.cloudMode));y+=27;
            buttonList.add(new Ui.Button(5,mainX,y,half,22,"Sun / moon: "+on(c.sunMoon)));
            buttonList.add(new Ui.Slider(32,mainX+half+8,y,half,23,"Stars",0,1000,1,c.stars,v->settings().stars=Math.round(v)));
        } else if(page==2){
            buttonList.add(new Ui.Button(6,mainX,y,half,22,"Weather: "+c.weather));
            buttonList.add(new Ui.Slider(33,mainX+half+8,y,half,23,"Rain / snow",0,1,.01f,c.weatherIntensity,v->settings().weatherIntensity=v));y+=27;
            buttonList.add(new Ui.Button(4,mainX,y,half,22,"Fog: "+on(c.fogEnabled)));
            buttonList.add(new Ui.Button(43,mainX+half+8,y,half,22,"Fog color"));y+=27;
            buttonList.add(new Ui.Slider(31,mainX,y,half,23,"Fog density",.0001f,.025f,0,c.fogDensity,v->settings().fogDensity=v));
            buttonList.add(new Ui.Slider(45,mainX+half+8,y,half,23,"Effect budget",0,256,1,AlienClient.get().config().performance.maxParticles,v->AlienClient.get().config().performance.maxParticles=Math.round(v)));
        } else {
            skybox=new ModernTextField(50,fontRendererObj,mainX,y+15,mainW,22).placeholder("panorama.png");
            skybox.setMaxStringLength(120);skybox.setText(c.skyboxFile);
            imports.clear();File[] files=new File(AlienClient.get().store().directory(),"skyboxes").listFiles();
            if(files!=null)for(File file:files)if(file.isFile()&&file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".png")&&imports.size()<64)imports.add(file.getName());
            Collections.sort(imports);
            buttonList.add(new Ui.Button(7,mainX,y+43,half,22,"Load panorama"));
            buttonList.add(new Ui.Button(51,mainX+half+8,y+43,half,22,imports.isEmpty()?"No local PNGs":"Browse PNGs"));
            buttonList.add(new Ui.Button(8,mainX,y+70,half,22,"Reset visuals"));
            buttonList.add(new Ui.Button(9,mainX+half+8,y+70,half,22,"Visual effects"));
        }
    }
    private static String on(boolean value){return value?"ON":"OFF";}
    @Override public void drawContents(int mx,int my,float partial){
        WorldStudioVisuals.frame(width,height,"World studio","Sky & atmosphere","world");
        WorldStudioVisuals.panorama(mainX,bodyY,mainW,previewH,settings(),true);
        if(page==3){
            Ui.text("PNG · equirectangular panorama",mainX,bodyY+previewH+10,Ui.theme().muted);
            if(skybox!=null)skybox.drawTextBox();
        }
        for(GuiButton button:buttonList)button.drawButton(mc,mx,my);
        if(height>300)Ui.text(fontRendererObj.trimStringToWidth(message,mainW),mainX,height-48,Ui.theme().muted);
        if(page==1){drawSwatch(41,settings().skyTop);drawSwatch(42,settings().skyBottom);}
        if(page==2)drawSwatch(43,settings().fogColor);
        if(mx>=mainX&&mx<mainX+mainW&&my>=bodyY&&my<bodyY+previewH&&height>270)
            Ui.tooltip("Palette preview; loaded world updates live",mx,my,width,height);
    }
    private void drawSwatch(int id,int color){for(GuiButton b:buttonList)if(b.id==id)Ui.rounded(b.xPosition+b.width-16,b.yPosition+6,10,10,3,0xff000000|color);}
    @Override protected void actionPerformed(GuiButton button)throws IOException{
        WorldConfig c=settings();int id=button.id;
        if(id==0){applyImport();mc.displayGuiScreen(parent);return;}
        if(id>=70&&id<=73){applyImport();page=id-70;initGui();return;}
        if(id>=100&&id<=105){c.applyTheme(WorldStudioVisuals.THEMES[id-100]);c.skyboxFile="";message=c.theme+" palette selected";}
        else if(id==1)c.enabled=!c.enabled;
        else if(id==2){int index=java.util.Arrays.asList(WorldStudioVisuals.THEMES).indexOf(c.theme);c.applyTheme(WorldStudioVisuals.THEMES[(index+1)%6]);c.skyboxFile="";}
        else if(id==3)c.timeOverride=!c.timeOverride;
        else if(id==4)c.fogEnabled=!c.fogEnabled;
        else if(id==5){mc.displayGuiScreen(new CelestialScreen(this));return;}
        else if(id==6){String[] modes={"Server","Clear","Rain","Snow","Storm"};int i=java.util.Arrays.asList(modes).indexOf(c.weather);c.weather=modes[(i+1)%modes.length];}
        else if(id==7){applyImport();Notifications.push("Panorama settings saved");}
        else if(id==8){mc.displayGuiScreen(new ConfirmScreen(this,"Reset world visuals?","Restore local sky, fog and weather defaults.",()->{AlienClient.get().config().world=new WorldConfig();AlienClient.get().save();}));return;}
        else if(id==9){applyImport();mc.displayGuiScreen(new EffectsScreen(this));return;}
        else if(id==10){applyImport();mc.displayGuiScreen(new AtmospherePresetScreen(this));return;}
        else if(id==41||id==42||id==43){
            int initial=id==41?c.skyTop:id==42?c.skyBottom:c.fogColor;
            mc.displayGuiScreen(new ColorPickerScreen(this,id==41?"Zenith color":id==42?"Horizon color":"Fog color",initial,false,value->{
                if(id==41)settings().skyTop=value&0xffffff;else if(id==42)settings().skyBottom=value&0xffffff;else settings().fogColor=value&0xffffff;
                AlienClient.get().save();
            }));return;
        } else if(id==44)c.cloudMode="Vanilla".equals(c.cloudMode)?"Hidden":"Vanilla";
        else if(id==51&&!imports.isEmpty()){
            int i=imports.indexOf(skybox.getText());skybox.setText(imports.get((i+1)%imports.size()));applyImport();
        } else if(id>=30&&id<=45)return;
        AlienClient.get().save();initGui();
    }
    private void applyImport(){if(skybox!=null){settings().skyboxFile=skybox.getText().trim();settings().normalize();AlienClient.get().save();}}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(skybox!=null)skybox.mouseClicked(x,y,button);super.mouseClicked(x,y,button);}
    @Override protected void keyTyped(char ch,int key)throws IOException{if(key==Keyboard.KEY_ESCAPE){applyImport();mc.displayGuiScreen(parent);return;}if(skybox!=null)skybox.textboxKeyTyped(ch,key);}
    @Override public void updateScreen(){if(skybox!=null)skybox.updateCursorCounter();}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}

    private static final class CelestialScreen extends dev.alien.client.gui.PremiumScreen {
        private final GuiScreen parent;
        CelestialScreen(GuiScreen parent){this.parent=parent;}
        private WorldConfig config(){return AlienClient.get().config().world;}
        @Override public void initGui(){
            fontRendererObj=Ui.font();buttonList.clear();int x=width/2-110,y=Math.max(43,height/2-60);WorldConfig c=config();
            buttonList.add(new Ui.Button(1,x,y,220,24,"Sun and moon: "+on(c.sunMoon)));
            buttonList.add(new Ui.Slider(2,x,y+37,220,27,"Sun size",.2f,2,.01f,c.sunSize,v->config().sunSize=v));
            buttonList.add(new Ui.Slider(3,x,y+74,220,27,"Moon size",.2f,2,.01f,c.moonSize,v->config().moonSize=v));
            buttonList.add(new Ui.Button(0,x,y+112,220,24,"Done"));
        }
        @Override public void drawContents(int mx,int my,float partial){Ui.background(width,height);Ui.card(width/2-127,24,254,height-48);Ui.icon("world",width/2-108,36,16,Ui.accent());Ui.text("Celestial bodies",width/2-83,39,Ui.theme().text);for(GuiButton b:buttonList)b.drawButton(mc,mx,my);}
        @Override protected void actionPerformed(GuiButton b){if(b.id==0){mc.displayGuiScreen(parent);return;}if(b.id==1){config().sunMoon=!config().sunMoon;AlienClient.get().save();initGui();}}
        @Override protected void keyTyped(char c,int key){if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);}
        @Override public void onGuiClosed(){AlienClient.get().save();}
        @Override public boolean doesGuiPauseGame(){return false;}
    }
}
