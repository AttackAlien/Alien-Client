package dev.alien.client.world;
import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import dev.alien.client.gui.ui.ColorPickerScreen;
import net.minecraft.client.gui.*;
import java.io.IOException;
import java.util.function.Consumer;
/** Controls the actual world renderer; no illustrative preview. */
public final class ParticleStudioScreen extends PremiumScreen {
    private final GuiScreen parent;
    public ParticleStudioScreen(GuiScreen parent){this.parent=parent;}
    private EffectsConfig c(){return AlienClient.get().config().effects;}
    private void slider(int id,int x,int y,int w,String name,float min,float max,float step,float v,Consumer<Float> set){buttonList.add(new Ui.Slider(id,x,y,w,23,name,min,max,step,v,set));}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(470,width-28),x=(width-w)/2,half=(w-8)/2,third=(w-12)/3;EffectsConfig c=c();
        buttonList.add(new Ui.Button(33,width-116,11,102,22,"Effects: "+(c.enabled?"ON":"OFF")));
        String[] presets={"PvP clean","Cinematic glow","Sakura petals"};
        for(int i=0;i<3;i++)buttonList.add(new Ui.Button(1+i,x+i*(third+6),45,third,22,presets[i]));
        String[] shapes={"Diamond","Petal","Spark","Orb"};int quarter=(w-18)/4;
        for(int i=0;i<4;i++)buttonList.add(new Ui.Button(10+i,x+i*(quarter+6),73,quarter,22,shapes[i]).selected(shapes[i].equals(c.shape)));
        slider(20,x,105,half,"Opacity",.1f,1,.05f,c.opacity,v->c.opacity=v);slider(21,x+half+8,105,half,"Glow",0,1,.05f,c.glow,v->c.glow=v);
        slider(22,x,135,half,"Flow",0,2,.05f,c.flow,v->c.flow=v);slider(23,x+half+8,135,half,"Lifetime",.5f,2,.05f,c.lifetime,v->c.lifetime=v);
        slider(24,x,165,half,"Size",1,8,.1f,c.size,v->c.size=v);slider(25,x+half+8,165,half,"Budget",0,256,1,c.budget,v->c.budget=Math.round(v));
        buttonList.add(new Ui.Button(31,x,height-27,third,21,"Primary"));buttonList.add(new Ui.Button(32,x+third+6,height-27,third,21,"Secondary"));
        buttonList.add(new Ui.Button(0,x+(third+6)*2,height-27,third,21,"Done"));
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b.id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}
        if(b.id>=1&&b.id<=3)ParticlePresets.apply(c(),new String[]{"PvP","Cinematic","Sakura"}[b.id-1]);
        if(b.id>=10&&b.id<=13)c().shape=new String[]{"Diamond","Petal","Spark","Orb"}[b.id-10];
        if(b.id==33)c().enabled=!c().enabled;
        if(b.id==31||b.id==32){boolean primary=b.id==31;mc.displayGuiScreen(new ColorPickerScreen(this,primary?"Particle primary":"Particle secondary",0xff000000|(primary?c().color:c().secondaryColor),false,v->{if(primary)c().color=v&0xffffff;else c().secondaryColor=v&0xffffff;}));return;}
        AlienClient.get().save();initGui();
    }
    @Override public void drawContents(int x,int y,float p){Ui.background(width,height);Ui.text("PARTICLE STUDIO",14,17,Ui.theme().text);super.drawContents(x,y,p);}
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
