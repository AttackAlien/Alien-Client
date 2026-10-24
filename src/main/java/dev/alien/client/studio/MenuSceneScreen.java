package dev.alien.client.studio;
import dev.alien.client.AlienClient;import dev.alien.client.gui.*;import net.minecraft.client.gui.*;import java.io.IOException;
/** Live controls for the menu diorama. No server/world is started by this screen. */
public final class MenuSceneScreen extends PremiumScreen {
    private final GuiScreen parent;private int dragX,dragY;private boolean dragging;
    public MenuSceneScreen(GuiScreen p){parent=p;}private StudioConfig c(){return AlienClient.get().config().studio;}
    @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();int x=width-196,y=46,step=Math.min(31,(height-86)/6),bh=Math.min(24,step-4);buttonList.add(new Ui.Button(1,x,y,180,bh,"Scene / "+c().scene));buttonList.add(new Ui.Button(2,x,y+step,180,bh,"Time / "+c().time));buttonList.add(new Ui.Button(3,x,y+step*2,180,bh,"Weather / "+c().weather));buttonList.add(new Ui.Button(4,x,y+step*3,180,bh,"Camera / "+(c().orbit?"Orbit":"Fixed")));buttonList.add(new Ui.Button(5,x,y+step*4,180,bh,"3D backdrop / "+(c().menuScene?"ON":"OFF")));buttonList.add(new Ui.Button(6,x,y+step*5,180,bh,"Reset camera"));buttonList.add(new Ui.Button(0,x,height-35,180,24,"Done"));}
    private String next(String s,String[] a){for(int i=0;i<a.length;i++)if(a[i].equals(s))return a[(i+1)%a.length];return a[0];}
    @Override protected void actionPerformed(GuiButton b){if(b.id==0){AlienClient.get().save();mc.displayGuiScreen(parent);return;}if(b.id==1)c().scene=next(c().scene,StudioConfig.SCENES);if(b.id==2)c().time=next(c().time,StudioConfig.TIMES);if(b.id==3)c().weather=next(c().weather,StudioConfig.WEATHER);if(b.id==4)c().orbit=!c().orbit;if(b.id==5)c().menuScene=!c().menuScene;if(b.id==6){c().cameraYaw=25;c().cameraPitch=16;c().cameraDistance=25;}AlienClient.get().save();initGui();}
    @Override public void drawContents(int x,int y,float p){if(!MenuScene.render(width,height))Ui.background(width,height);Ui.rounded(width-207,35,199,height-42,12,Ui.alpha(Ui.theme().surface,.88f));Ui.text("ALIEN / MENU SCENE",16,16,Ui.theme().text);Ui.text("Drag scenery to orbit / scroll to zoom",16,height-22,Ui.theme().text);super.drawContents(x,y,p);}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button==0&&x<width-210){dragging=true;dragX=x;dragY=y;c().orbit=false;}super.mouseClicked(x,y,button);}
    @Override protected void mouseClickMove(int x,int y,int b,long t){if(dragging){c().cameraYaw+=(x-dragX)*.5f;c().cameraPitch+=(y-dragY)*.3f;c().normalize();dragX=x;dragY=y;}}
    @Override protected void mouseReleased(int x,int y,int b){dragging=false;AlienClient.get().save();super.mouseReleased(x,y,b);}
    @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int wheel=org.lwjgl.input.Mouse.getEventDWheel();if(wheel!=0){c().cameraDistance+=wheel>0?-1:1;c().normalize();AlienClient.get().save();}}
}
