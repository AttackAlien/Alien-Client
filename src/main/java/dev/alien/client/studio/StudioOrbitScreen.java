package dev.alien.client.studio;
import dev.alien.client.AlienClient;import dev.alien.client.gui.*;import net.minecraft.client.gui.*;import java.io.IOException;
/** Compact radial navigation. Every destination is a real existing editor. */
public final class StudioOrbitScreen extends AlienMenuScreen {
 private final GuiScreen back;private int cx,cy,r;private String focus="Render";public StudioOrbitScreen(GuiScreen parent){super(parent);back=parent;}
 @Override public void initGui(){fontRendererObj=Ui.font();buttonList.clear();cx=width/2;cy=height/2;r=Math.max(53,Math.min(91,(height-92)/2));int bh=height<300?20:24;String[] labels={"Render","Player","HUD","World","Cosmetics","Configs","Studios","Combat"};for(int i=0;i<labels.length;i++){if(i==7&&!"unified".equals(dev.alien.client.edition.BuildEdition.ID))continue;double angle=-Math.PI/2+i*Math.PI/4;buttonList.add(new Ui.Button(i+1,cx+(int)(Math.cos(angle)*r)-34,cy+(int)(Math.sin(angle)*r)-bh/2,68,bh,labels[i]));}buttonList.add(new Ui.Button(0,cx-45,height-29,90,21,"Back"));}
 @Override public void drawContents(int mx,int my,float partial){Ui.background(width,height);Ui.centered("ALIEN / ORBIT",cx,15,Ui.theme().text);Ui.rounded(cx-35,cy-35,70,70,35,Ui.alpha(Ui.accent(),.15f));Ui.icon("alien",cx-24,cy-24,48,Ui.accent());for(GuiButton b:buttonList){if(mx>=b.xPosition&&mx<b.xPosition+b.width&&my>=b.yPosition&&my<b.yPosition+b.height&&b.id!=0)focus=b.displayString;b.drawButton(mc,mx,my);}if(height>=300)Ui.centered(focus,cx,height-52,Ui.theme().text);}
 @Override protected void actionPerformed(GuiButton b){if(b.id==0){mc.displayGuiScreen(back);return;}if(b.id>=1&&b.id<=3)mc.displayGuiScreen(new ModuleGalleryScreen(this).openCategory(new String[]{"Render","Player","HUD"}[b.id-1]));if(b.id==4)mc.displayGuiScreen(new dev.alien.client.world.WorldStudioScreen(this));if(b.id==5)mc.displayGuiScreen(new dev.alien.client.cosmetic.CosmeticStudioScreen(this));if(b.id==6)mc.displayGuiScreen(new ProfileLibraryScreen(this));if(b.id==7)mc.displayGuiScreen(new AlienMenuScreen(this));if(b.id==8)mc.displayGuiScreen(new CombatStudioScreen(this));}
 @Override public void updateScreen(){}
 @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button==0)for(GuiButton b:buttonList)if(b.mousePressed(mc,x,y)){actionPerformed(b);break;}}
 @Override protected void keyTyped(char c,int key){if(key==org.lwjgl.input.Keyboard.KEY_ESCAPE)mc.displayGuiScreen(back);}
 @Override public boolean doesGuiPauseGame(){return false;}
}
