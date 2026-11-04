package dev.alien.client.emote;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.*;
import java.io.IOException;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderManager;
import org.lwjgl.input.*;
import org.lwjgl.opengl.GL11;

/** Real player preview and profile-backed library/wheel controls. No playback is serialized. */
public final class EmoteStudioScreen extends PremiumScreen {
    private final GuiScreen parent;
    private int page,slot,scroll,left,top,bottom,column,right;private boolean paused,captureKey;
    private double seconds=.8;private long last;
    public EmoteStudioScreen(GuiScreen parent){this.parent=parent;}
    private EmoteConfig c(){return AlienClient.get().config().emotes;}
    private void button(int id,int x,int y,int w,String text){buttonList.add(new Ui.Button(id,x,y,w,22,text));}
    @Override public void initGui(){
        fontRendererObj=Ui.font();buttonList.clear();int w=Math.min(830,width-24);left=(width-w)/2;column=(w-12)/2;right=left+column+12;top=66;bottom=height-42;
        for(int i=0;i<3;i++)buttonList.add(new Ui.Button(10+i,left+i*91,35,87,22,new String[]{"Library","Wheel","Settings"}[i]).selected(page==i));
        button(0,left+w-54,8,54,"Done");
        int step=28,y=top+24-scroll;
        if(page==0){for(int i=0;i<EmoteLibrary.IDS.length;i++)buttonList.add(new Ui.Button(100+i,left+9,y+i*step,column-18,24,EmoteLibrary.name(EmoteLibrary.IDS[i])).selected(c().selected.equals(EmoteLibrary.IDS[i])));}
        else if(page==1){
            for(int i=0;i<8;i++)buttonList.add(new Ui.Button(200+i,left+9,y+i*step,column-18,24,(i+1)+" / "+EmoteLibrary.name(c().slots[i])).selected(slot==i));
            int gap=Math.min(30,Math.max(23,(bottom-top-52)/3));
            button(31,right+12,top+48,column-24,"Assign selected emote");button(32,right+12,top+48+gap,column-24,"Restore default wheel");button(33,right+12,top+48+gap*2,column-24,"Open wheel");
        }else{
            button(21,left+9,y,column-18,"Emotes: "+(c().enabled?"ON":"OFF"));
            buttonList.add(new Ui.Slider(22,left+9,y+step,column-18,24,"Speed",.5f,1.5f,.05f,c().speed,v->{c().speed=v;AlienClient.get().save();}));
            button(23,left+9,y+step*2,column-18,"Loop dances: "+(c().loop?"ON":"OFF"));
            button(24,left+9,y+step*3,column-18,"Auto third person: "+(c().autoThirdPerson?"ON":"OFF"));
            button(25,left+9,y+step*4,column-18,captureKey?"Press key / Del clears":"Wheel key: "+(c().wheelKey==0?"Unbound":Keyboard.getKeyName(c().wheelKey)));
            button(26,left+9,y+step*5,column-18,"Hide held item: "+(c().hideHeldItem?"ON":"OFF"));
            button(27,left+9,y+step*6,column-18,"Articulated limbs: "+(c().articulated?"ON":"OFF"));
        }
        int foot=(w-12)/3;button(1,left,height-30,foot,"Play in world");button(2,left+foot+6,height-30,foot,"Stop");button(3,left+2*(foot+6),height-30,w-2*(foot+6),paused?"Resume preview":"Pause preview");
        for(GuiButton b:buttonList)if(b.id==1)b.enabled=mc.thePlayer!=null&&c().enabled&&AlienClient.get().config().animations;
        last=System.nanoTime();
    }
    private boolean scrolling(int id){return id>=100||id>=21&&id<=27;}
    @Override public void drawContents(int mx,int my,float partial){
        long now=System.nanoTime();if(!paused&&AlienClient.get().config().animations&&!Ui.theme().reducedMotion)seconds+=Math.min(.1,(now-last)/1e9)*c().speed;last=now;
        Ui.background(width,height);Ui.text("ALIEN / EMOTE STUDIO",left,15,Ui.theme().text);
        Ui.card(left,top,column,bottom-top);Ui.card(right,top,column,bottom-top);
        Ui.text(page==0?"CLASSIC COLLECTION":page==1?"8 WHEEL SLOTS":"PLAYBACK",left+10,top+8,Ui.theme().muted);
        for(GuiButton b:buttonList)if(!scrolling(b.id))b.drawButton(mc,mx,my);
        float scale=framebufferScale();GL11.glPushAttrib(GL11.GL_SCISSOR_BIT|GL11.GL_ENABLE_BIT);
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(left*scale),mc.displayHeight-Math.round(bottom*scale),Math.round(column*scale),Math.round((bottom-top-21)*scale));
            for(GuiButton b:buttonList)if(scrolling(b.id))b.drawButton(mc,mx,my);
        }finally{GL11.glPopAttrib();}
        if(page==1){Ui.text("SELECTED /",right+12,top+12,Ui.theme().muted);Ui.text(fontRendererObj.trimStringToWidth(EmoteLibrary.name(c().selected),column-24),right+12,top+30,Ui.accent());}
        else drawPlayer();
    }
    private void drawPlayer(){
        Ui.text(fontRendererObj.trimStringToWidth(EmoteLibrary.name(c().selected),column-20),right+10,top+9,Ui.accent());
        if(mc.thePlayer==null){Ui.centered("Enter a world to preview",right+column/2,top+55,Ui.theme().muted);return;}
        final RenderManager manager=mc.getRenderManager();final float viewY=manager.playerViewY,viewX=manager.playerViewX;final boolean shadows=manager.isRenderShadow();
        float scale=framebufferScale();GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(right*scale),mc.displayHeight-Math.round((bottom-18)*scale),Math.round(column*scale),Math.round((bottom-top-42)*scale));
            final int size=Math.max(18,Math.min((bottom-top-54)/2,column/3));
            AlienClient.get().emotes().preview(c().selected,seconds,()->GuiInventory.drawEntityOnScreen(right+column/2,bottom-25,size,-18,0,mc.thePlayer));
        }finally{manager.playerViewY=viewY;manager.playerViewX=viewX;manager.setRenderShadow(shadows);GL11.glPopAttrib();Ui.finishFrame();}
        Ui.centered("Local animation / no audio",right+column/2,bottom-13,Ui.theme().muted);
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(!b.enabled)return;
        if(b.id==0){mc.displayGuiScreen(parent);return;}
        if(b.id>=10&&b.id<=12){page=b.id-10;scroll=0;captureKey=false;initGui();return;}
        if(b.id>=100&&b.id<108){c().selected=EmoteLibrary.IDS[b.id-100];seconds=.8;}
        else if(b.id>=200&&b.id<208)slot=b.id-200;
        else if(b.id==1){if(AlienClient.get().emotes().request(c().selected))mc.displayGuiScreen(null);return;}
        else if(b.id==2)AlienClient.get().emotes().stop();
        else if(b.id==3)paused=!paused;
        else if(b.id==21)c().enabled=!c().enabled;
        else if(b.id==23)c().loop=!c().loop;
        else if(b.id==24)c().autoThirdPerson=!c().autoThirdPerson;
        else if(b.id==25)captureKey=true;
        else if(b.id==26)c().hideHeldItem=!c().hideHeldItem;
        else if(b.id==27)c().articulated=!c().articulated;
        else if(b.id==31)c().slots[slot]=c().selected;
        else if(b.id==32)c().slots=new EmoteConfig().slots;
        else if(b.id==33){mc.displayGuiScreen(new EmoteWheelScreen(this,false));return;}
        AlienClient.get().save();initGui();
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{
        for(GuiButton b:buttonList)if((!scrolling(b.id)||y>=top+21&&y<bottom)&&b.mousePressed(mc,x,y)){if(!(b instanceof Ui.Slider))actionPerformed(b);return;}
    }
    @Override protected void mouseReleased(int x,int y,int state){for(GuiButton b:buttonList)b.mouseReleased(x,y);}
    @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int d=Mouse.getEventDWheel();if(d!=0){int content=page==2?7*28:8*28;scroll=Math.max(0,Math.min(Math.max(0,content-(bottom-top-24)),scroll+(d<0?28:-28)));initGui();}}
    @Override protected void keyTyped(char ch,int key)throws IOException{
        if(captureKey){if(key!=Keyboard.KEY_ESCAPE)c().wheelKey=key==Keyboard.KEY_DELETE?0:key;captureKey=false;AlienClient.get().save();initGui();return;}
        if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);
    }
    @Override public void onGuiClosed(){AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
