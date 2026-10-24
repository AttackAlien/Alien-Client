package dev.alien.client.scene;
import dev.alien.client.AlienClient;import dev.alien.client.cosmetic.CosmeticConfig;
import dev.alien.client.gui.*;import dev.alien.client.gui.ui.ColorPickerScreen;
import net.minecraft.client.gui.*;import java.io.IOException;
/** All nine reference features control real renderers, with Alien branding and profiles. */
public final class SceneStudioScreen extends PremiumScreen {
    private final GuiScreen parent;public SceneStudioScreen(GuiScreen p){parent=p;}
    private SceneConfig c(){return AlienClient.get().config().scenes;}
    private CosmeticConfig.Accessory outfit(){return AlienClient.get().config().cosmetics.self;}
    private String mark(boolean on){return on?"ON":"OFF";}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(520,width-28),x=(width-w)/2,half=(w-8)/2,step=Math.min(36,(height-115)/5);SceneConfig c=c();
        String[] names={"Shockwave / "+mark(c.shockwave),"Cape / "+mark("CAPE".equals(outfit().back)),"Trail / "+mark(outfit().trail),"Cute World / "+mark(c.cute),"Cyberspace / "+mark("Cyberspace".equals(c.mode)),"X-Ray bones / "+mark("Skeleton".equals(c.mode)),"Glass / "+mark("Glass".equals(c.mode)),"Chroma / "+mark("Chroma".equals(c.mode)),"Sky / "+c.sky};
        for(int i=0;i<names.length;i++)buttonList.add(new Ui.Button(i+1,x+(i%2)*(half+8),46+(i/2)*step,half,step-4,names[i]));
        buttonList.add(new Ui.Button(20,x+half+8,46+4*step,half,step-4,"Scene FX / "+mark(c.enabled)));
        buttonList.add(new Ui.Slider(30,x,height-62,half,23,"Strength",.1f,1,.05f,c.strength,v->c.strength=v));
        buttonList.add(new Ui.Slider(31,x+half+8,height-62,half,23,"Shockwave radius",1,12,.25f,c.radius,v->c.radius=v));
        buttonList.add(new Ui.Button(22,width-113,6,99,21,"Sky styles"));buttonList.add(new Ui.Button(21,x,height-28,half,21,"Choose accent"));buttonList.add(new Ui.Button(0,x+half+8,height-28,half,21,"Done"));
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{SceneConfig c=c();CosmeticConfig.Accessory a=outfit();
        if(b.id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(b.id==1){c.shockwave=!c.shockwave;c.enabled=true;}
        if(b.id==2){a.back="CAPE".equals(a.back)?"NONE":"CAPE";a.animations=true;a.capeShimmer=true;AlienClient.get().config().cosmetics.enabled=true;}
        if(b.id==3){a.trail=!a.trail;a.trailStyle="Ribbon";a.animations=true;AlienClient.get().config().cosmetics.enabled=true;}
        if(b.id==4){c.cute=!c.cute;c.enabled=true;}
        if(b.id>=5&&b.id<=8){String next=SceneConfig.MODES[b.id-4];c.mode=next.equals(c.mode)?"Natural":next;c.enabled=true;}
        if(b.id==9){c.sky=SceneConfig.SKIES[(c.skyIndex()+1)%SceneConfig.SKIES.length];c.enabled=true;}
        if(b.id==20)c.enabled=!c.enabled;
        if(b.id==21){mc.displayGuiScreen(new ColorPickerScreen(this,"Scene accent",0xff000000|c.color,false,v->c.color=v&0xffffff));return;}
        if(b.id==22){mc.displayGuiScreen(new SceneSkyScreen(this));return;}
        AlienClient.get().save();initGui();
    }
    @Override public void drawContents(int x,int y,float partial){Ui.background(width,height);Ui.text("ALIEN / SCENE STUDIO",14,14,Ui.theme().text);Ui.text(Ui.font().trimStringToWidth(SceneShaders.status(),width-28),14,29,Ui.theme().muted);super.drawContents(x,y,partial);}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
