package dev.alien.client.cosmetic;
import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import net.minecraft.client.gui.*;
import java.io.IOException;
/** Selected player's real, profile-backed ribbon settings. */
public final class CosmeticTrailScreen extends PremiumScreen {
    private final GuiScreen parent;private final CosmeticConfig.Accessory a;
    public CosmeticTrailScreen(GuiScreen parent,CosmeticConfig.Accessory a){this.parent=parent;this.a=a;}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(360,width-30),x=(width-w)/2;
        buttonList.add(new Ui.Button(1,x,48,w,22,"Trail: "+(a.trail?"ON":"OFF")));
        buttonList.add(new Ui.Button(2,x,78,w,22,"Style / "+a.trailStyle));
        buttonList.add(new Ui.Slider(3,x,110,w,24,"Width",.04f,.45f,.01f,a.trailWidth,v->a.trailWidth=v));
        buttonList.add(new Ui.Slider(4,x,143,w,24,"Lifetime",.25f,2,.05f,a.trailLifetime,v->a.trailLifetime=v));
        buttonList.add(new Ui.Slider(5,x,176,w,24,"Opacity",.1f,1,.05f,a.trailOpacity,v->a.trailOpacity=v));
        buttonList.add(new Ui.Button(0,width/2-50,height-27,100,21,"Done"));
        for(GuiButton button:buttonList)if(button.id>=3&&button.id<=5)button.enabled="Ribbon".equals(a.trailStyle);
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b.id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(b.id==1)a.trail=!a.trail;if(b.id==2)a.trailStyle="Ribbon".equals(a.trailStyle)?"Particles":"Ribbon";
        AlienClient.get().save();initGui();
    }
    @Override public void drawContents(int x,int y,float p){Ui.background(width,height);Ui.centered("COSMETIC TRAILS",width/2,17,Ui.theme().text);super.drawContents(x,y,p);}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
