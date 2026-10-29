package dev.alien.client.cosmetic;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import dev.alien.client.gui.ui.ColorPickerScreen;
import dev.alien.client.gui.ui.ValueInputScreen;
import java.awt.Desktop;
import java.io.IOException;
import java.util.*;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** Real per-player settings, a material preview and draggable three-stop curve editor. */
public final class CosmeticTrailScreen extends PremiumScreen {
    private final GuiScreen parent;
    private final CosmeticConfig.Accessory a;
    private int left,panelWidth,column,right,bodyTop=72,bodyBottom,scroll,maximumScroll,selectedStop=1,drag=-1;
    private int previewY,graphX,graphY,graphWidth,graphHeight=54;
    private boolean opacity;
    private GuiButton activeControl;
    private List<String> textures;
    private String message="Drag points / right-click for an exact value. Tail -> Head.";
    private static final String[] STOPS={"Tail","Middle","Head"};

    public CosmeticTrailScreen(GuiScreen parent,CosmeticConfig.Accessory a){this.parent=parent;this.a=a;}
    public int[] curveCanvasBounds(){return new int[]{graphX,graphY,graphWidth,graphHeight};}
    public int[] materialCanvasBounds(){return new int[]{right+12,previewY+20,column-24,58};}
    private TrailDesign d(){return a.trailDesign;}
    private boolean fixed(int id){return id==0||id==1||id==2||id==10;}
    private void button(int id,int x,int y,int w,String text){buttonList.add(new Ui.Button(id,x,y,w,20,text));}
    @Override public void initGui(){
        fontRendererObj=Ui.font();a.normalize();buttonList.clear();activeControl=null;drag=-1;
        panelWidth=Math.min(760,width-24);left=(width-panelWidth)/2;bodyBottom=height-31;
        boolean wide=panelWidth>=520;column=wide?(panelWidth-12)/2:panelWidth;right=wide?left+column+12:left;
        int contentHeight=wide?254:516;
        maximumScroll=Math.max(0,contentHeight-(bodyBottom-bodyTop));scroll=Math.min(scroll,maximumScroll);
        int top=bodyTop-scroll,rightTop=wide?top:top+258;
        int third=(panelWidth-12)/3;
        button(1,left,43,third,"Trail: "+(a.trail?"ON":"OFF"));
        button(2,left+third+6,43,third,"Style / "+a.trailStyle);
        button(10,left+(third+6)*2,43,panelWidth-(third+6)*2,"Designer: "+(d().enabled?"ON":"OFF"));
        buttonList.add(new Ui.Slider(3,left+12,top+28,column-24,23,"Base width",.04f,.45f,.01f,a.trailWidth,v->a.trailWidth=v));
        buttonList.add(new Ui.Slider(4,left+12,top+57,column-24,23,"Lifetime / seconds",.25f,2,.05f,a.trailLifetime,v->a.trailLifetime=v));
        buttonList.add(new Ui.Slider(5,left+12,top+86,column-24,23,"Master opacity",.1f,1,.05f,a.trailOpacity,v->a.trailOpacity=v));
        button(11,left+12,top+119,column-24,"Texture / "+("PNG".equals(d().texture)?d().customTexture:d().texture));
        buttonList.add(new Ui.Slider(14,left+12,top+146,column-24,23,"Texture repeats / block",.25f,8,.05f,d().tiling,v->{d().tiling=v;d().preset="Custom";}));
        buttonList.add(new Ui.Slider(15,left+12,top+175,column-24,23,"Texture scroll",-2,2,.05f,d().scroll,v->{d().scroll=v;d().preset="Custom";}));
        button(12,left+12,top+207,(column-30)/2,"PNG folder");button(13,left+18+(column-30)/2,top+207,(column-30)/2,"Reload PNGs");
        button(30,left+12,top+228,column-24,"Preset / "+d().preset);
        previewY=rightTop;int stopWidth=(column-32)/3;
        for(int i=0;i<3;i++)buttonList.add(new Ui.Button(20+i,right+12+i*(stopWidth+4),rightTop+84,stopWidth,20,STOPS[i]).selected(selectedStop==i));
        button(23,right+12,rightTop+110,column-24,STOPS[selectedStop]+" color / #"+String.format(Locale.ROOT,"%06X",d().color(selectedStop)));
        buttonList.add(new Ui.Button(26,right+12,rightTop+139,(column-30)/2,20,"Width curve").selected(!opacity));
        buttonList.add(new Ui.Button(27,right+18+(column-30)/2,rightTop+139,(column-30)/2,20,"Opacity curve").selected(opacity));
        graphX=right+24;graphY=rightTop+182;graphWidth=column-48;
        button(0,left+panelWidth-90,height-27,90,"Done");
        boolean ribbon="Ribbon".equals(a.trailStyle);
        for(GuiButton b:buttonList){if(b.id>=3&&b.id<=5)b.enabled=ribbon;else if(b.id==10)b.enabled=ribbon;else if(b.id>=11)b.enabled=ribbon&&d().enabled;}
        if(textures==null){textures=new ArrayList<String>(Arrays.asList(TrailDesign.TEXTURES));for(String file:TrailTextureLoader.files(TrailTextures.directory()))textures.add("PNG / "+file);}
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(!b.enabled)return;
        if(b.id==0){mc.displayGuiScreen(parent);return;}
        if(b.id==1)a.trail=!a.trail;
        else if(b.id==2)a.trailStyle="Ribbon".equals(a.trailStyle)?"Particles":"Ribbon";
        else if(b.id==10)d().enabled=!d().enabled;
        else if(b.id==11){
            String current="PNG".equals(d().texture)?"PNG / "+d().customTexture:d().texture;
            String next=textures.get((textures.indexOf(current)+1)%textures.size());
            d().texture=next.startsWith("PNG / ")?"PNG":next;d().customTexture=next.startsWith("PNG / ")?next.substring(6):"";d().preset="Custom";
        }else if(b.id==12){
            try{TrailTextureLoader.ensureDirectory(TrailTextures.directory());GuiScreen.setClipboardString(TrailTextures.directory().getAbsolutePath());if(Desktop.isDesktopSupported())Desktop.getDesktop().open(TrailTextures.directory());message="Folder path copied / add a PNG, then Reload PNGs.";}
            catch(Exception failure){message="PNG folder: config/alien-client/cosmetics/trails";}
        }else if(b.id==13){TrailTextures.reloadImported();textures=null;message="PNGs reloaded / maximum 1 MiB and 1024 x 1024";}
        else if(b.id>=20&&b.id<=22)selectedStop=b.id-20;
        else if(b.id==23){final int stop=selectedStop;mc.displayGuiScreen(new ColorPickerScreen(this,STOPS[stop]+" trail color",d().color(stop)|0xff000000,false,rgb->d().color(stop,rgb)));return;}
        else if(b.id==26)opacity=false;
        else if(b.id==27)opacity=true;
        else if(b.id==30){int at=Arrays.asList(TrailDesign.PRESETS).indexOf(d().preset);d().applyPreset(TrailDesign.PRESETS[(at+1)%TrailDesign.PRESETS.length]);}
        AlienClient.get().save();initGui();
    }
    @Override public void drawContents(int mx,int my,float partial){
        Ui.background(width,height);Ui.text("ALIEN / TRAIL STUDIO",left,15,Ui.theme().text);
        for(GuiButton b:buttonList)if(fixed(b.id))b.drawButton(mc,mx,my);
        String materialError="";float scale=framebufferScale();GL11.glPushAttrib(GL11.GL_SCISSOR_BIT|GL11.GL_ENABLE_BIT);
        try{
            GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(left*scale),mc.displayHeight-Math.round(bodyBottom*scale),Math.round(panelWidth*scale),Math.round((bodyBottom-bodyTop)*scale));
            int top=bodyTop-scroll;
            Ui.card(left,top,column,254);Ui.card(right,previewY,column,254);
            Ui.text("RIBBON MATERIAL",left+12,top+10,Ui.theme().muted);Ui.text("LIVE MATERIAL PREVIEW",right+12,previewY+8,Ui.theme().muted);
            Ui.rounded(right+12,previewY+23,column-24,53,5,0xff10121c);
            if("Ribbon".equals(a.trailStyle))materialError=TrailPreview.draw(a,right+20,previewY+23,column-40,53);
            else Ui.text("Particle mode / ribbon preview paused",right+20,previewY+45,Ui.theme().muted);
            drawCurve();
            for(GuiButton b:buttonList)if(!fixed(b.id))b.drawButton(mc,mx,my);
        }finally{GL11.glPopAttrib();}
        String note="Particles".equals(a.trailStyle)?"Particle style uses its existing settings; select Ribbon to design.":message;
        if(!materialError.isEmpty())note="Soft fallback / "+materialError;
        Ui.text(fontRendererObj.trimStringToWidth(note,panelWidth-102),left,height-18,materialError.isEmpty()?Ui.theme().muted:0xffff8799);
        if(maximumScroll>0)Ui.text("Scroll",left+panelWidth-44,bodyBottom-13,Ui.theme().muted);
    }
    private void drawCurve(){
        boolean editable=d().enabled&&"Ribbon".equals(a.trailStyle);
        Ui.rounded(graphX-6,graphY-8,graphWidth+12,graphHeight+25,4,0xff11131d);
        for(int y=0;y<=2;y++)Ui.rounded(graphX,graphY+y*graphHeight/2f,graphWidth,1,0,0xff333645);
        for(int i=0;i<64;i++){
            float t=i/64f,next=(i+1)/64f;
            float value=opacity?d().opacityAt(t):d().widthAt(t),v2=opacity?d().opacityAt(next):d().widthAt(next);
            float yy=graphY+graphHeight*(1-value/(opacity?1:1.5f)),yy2=graphY+graphHeight*(1-v2/(opacity?1:1.5f));
            Gui.drawRect(Math.round(graphX+t*graphWidth),Math.round(Math.min(yy,yy2)),Math.round(graphX+next*graphWidth)+1,Math.round(Math.max(yy,yy2))+2,editable?(d().colorAt(t)|0xff000000):0xff666875);
        }
        for(int i=0;i<3;i++){
            float t=i*.5f,value=opacity?d().opacityAt(t):d().widthAt(t),y=graphY+graphHeight*(1-value/(opacity?1:1.5f));
            Ui.rounded(graphX+t*graphWidth-5,y-5,10,10,5,i==selectedStop?0xffffffff:0xff9396aa);
            Ui.rounded(graphX+t*graphWidth-3,y-3,6,6,3,d().color(i)|0xff000000);
        }
        Ui.text(String.format(Locale.ROOT,"%s / %.2f%s",STOPS[selectedStop],opacity?d().opacityAt(selectedStop*.5f):d().widthAt(selectedStop*.5f),opacity?" opacity":" x width"),graphX,graphY-19,Ui.theme().muted);
        Ui.text("Tail",graphX,graphY+graphHeight+6,Ui.theme().muted);Ui.text("Head",graphX+graphWidth-25,graphY+graphHeight+6,Ui.theme().muted);
    }
    private void point(int y){d().point(selectedStop,opacity,(1-(y-graphY)/(float)graphHeight)*(opacity?1:1.5f));}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{
        if(d().enabled&&"Ribbon".equals(a.trailStyle)&&y>=bodyTop&&y<bodyBottom&&x>=graphX-7&&x<=graphX+graphWidth+7&&y>=graphY-7&&y<=graphY+graphHeight+7){
            selectedStop=Math.max(0,Math.min(2,Math.round((x-graphX)*2f/graphWidth)));
            if(button==1){final int stop=selectedStop;final boolean channel=opacity;mc.displayGuiScreen(new ValueInputScreen(this,STOPS[stop]+(channel?" opacity":" width multiplier"),0,channel?1:1.5f,channel?d().opacityAt(stop*.5f):d().widthAt(stop*.5f),v->d().point(stop,channel,v)));return;}
            if(button==0){drag=selectedStop;point(y);}return;
        }
        if(button!=0)return;
        for(GuiButton b:buttonList)if((fixed(b.id)||(y>=bodyTop&&y<bodyBottom))&&b.mousePressed(mc,x,y)){activeControl=b;if(!(b instanceof Ui.Slider))actionPerformed(b);break;}
    }
    @Override protected void mouseClickMove(int x,int y,int button,long elapsed){if(button==0&&drag>=0)point(y);}
    @Override protected void mouseReleased(int x,int y,int button){
        if(activeControl!=null){activeControl.mouseReleased(x,y);activeControl=null;}
        if(button==0){boolean moved=drag>=0;drag=-1;AlienClient.get().save();if(moved)initGui();}
    }
    @Override public void handleMouseInput()throws IOException{
        super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0&&drag<0){scroll=Math.max(0,Math.min(maximumScroll,scroll+(wheel<0?1:-1)*35*Math.max(1,Math.abs(wheel)/120)));initGui();}
    }
    @Override protected void keyTyped(char character,int key)throws IOException{if(key==Keyboard.KEY_ESCAPE){mc.displayGuiScreen(parent);return;}super.keyTyped(character,key);}
    @Override public void onGuiClosed(){drag=-1;AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
