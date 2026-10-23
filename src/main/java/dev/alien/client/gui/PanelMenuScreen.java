package dev.alien.client.gui;

import dev.alien.client.AlienClient;
import dev.alien.client.config.ClientConfig;
import dev.alien.client.edition.BuildEdition;
import dev.alien.client.cosmetic.CosmeticStudioScreen;
import dev.alien.client.hud.HudEditorScreen;
import dev.alien.client.module.ModuleCategory;
import dev.alien.client.module.ModuleDescriptor;
import dev.alien.client.module.GameplayImpact;
import dev.alien.client.world.EffectsScreen;
import dev.alien.client.world.WorldStudioScreen;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** Catalog-driven ClickGUI. Only real, registered modules appear in either edition. */
public final class PanelMenuScreen extends AlienMenuScreen {
    private String[] categories;
    private String[] icons;
    private final GuiScreen parent;
    private GuiTextField search;
    private int left,top,panelWidth,panelHeight,sidebar,contentX,contentWidth,listTop,listBottom;
    private int targetScroll;
    private float scroll;
    private String query="",selectedModule,capturingKey,toast="";
    private long toastUntil,opened=System.nanoTime();
    private final List<Card> cards=new ArrayList<Card>();
    private GuiButton activeButton;

    public PanelMenuScreen(GuiScreen parent){super(parent);this.parent=parent;}
    private ClientConfig config(){return AlienClient.get().config();}
    private InterfacePreferences prefs(){return config().interfacePrefs;}

    @Override public void initGui(){
        fontRendererObj=Ui.font();Keyboard.enableRepeatEvents(true);prefs().normalize();
        boolean combat=!AlienClient.get().modules().category(ModuleCategory.COMBAT).isEmpty();
        categories=combat?new String[]{"Discover","Combat","Render","World","Cosmetics","HUD","Performance","Utility","Configs"}
                :new String[]{"Discover","Render","World","Cosmetics","HUD","Performance","Utility","Configs"};
        icons=combat?new String[]{"grid","combat","eye","world","spark","grid","chart","settings","folder"}
                :new String[]{"grid","eye","world","spark","grid","chart","settings","folder"};
        panelWidth=Math.min(width-20,760);panelHeight=Math.min(height-18,470);
        left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        sidebar=panelWidth<380?82:106;contentX=left+sidebar+13;contentWidth=panelWidth-sidebar-26;
        listTop=top+70;listBottom=top+panelHeight-34;
        search=new GuiTextField(1,fontRendererObj,contentX,top+40,Math.max(72,contentWidth-58),19);
        search.setEnableBackgroundDrawing(false);search.setTextColor(config().ui.text);search.setMaxStringLength(48);search.setText(query);
        buttonList.clear();
        buttonList.add(new Ui.Button(900,contentX,top+panelHeight-27,Math.min(72,contentWidth/3),20,"Settings"));
        buttonList.add(new Ui.Button(905,contentX+Math.min(72,contentWidth/3)+6,top+panelHeight-27,Math.min(64,contentWidth/3),20,"Themes"));
        buttonList.add(new Ui.Button(902,left+panelWidth-57,top+panelHeight-27,46,20,"Close"));
        if(!isCategory(prefs().selectedCategory))prefs().selectedCategory="Discover";
    }
    private boolean isCategory(String s){for(String c:categories)if(c.equals(s))return true;return false;}
    private List<ModuleDescriptor> visibleModules(){
        List<ModuleDescriptor> found=new ArrayList<ModuleDescriptor>();String category=prefs().selectedCategory;
        String filter=query.trim().toLowerCase(Locale.ROOT);
        for(ModuleDescriptor module:AlienClient.get().modules().modules()){
            if("Combat".equals(category)&&module.category()!=ModuleCategory.COMBAT)continue;
            if("Render".equals(category)&&module.category()!=ModuleCategory.EFFECTS)continue;
            if("World".equals(category)&&module.category()!=ModuleCategory.WORLD)continue;
            if("Cosmetics".equals(category)&&module.category()!=ModuleCategory.COSMETICS)continue;
            if("HUD".equals(category)&&module.category()!=ModuleCategory.HUD)continue;
            if(prefs().activeOnly&&!module.enabled())continue;
            if(prefs().favoritesOnly&&!prefs().favorites.contains(module.id()))continue;
            if(!filter.isEmpty()&&!(module.name()+" "+module.description()+" "+module.category().label()).toLowerCase(Locale.ROOT).contains(filter))continue;
            found.add(module);
        }
        return found;
    }
    @Override public void drawContents(int mouseX,int mouseY,float partialTicks){
        Ui.background(width,height);
        Ui.shadow(left,top,panelWidth,panelHeight,config().ui.radius,config().ui.shadow);
        Ui.rounded(left,top,panelWidth,panelHeight,config().ui.radius,Ui.alpha(config().ui.surface,config().ui.opacity));
        Ui.rounded(left+4,top+4,sidebar-4,panelHeight-8,Math.max(3,config().ui.radius-2),config().ui.surfaceRaised);
        Ui.icon("alien",left+12,top+14,21,Ui.accent());
        fontRendererObj.drawString("ALIEN",left+39,top+15,config().ui.text);
        fontRendererObj.drawString(BuildEdition.ID.toUpperCase(Locale.ROOT),left+39,top+28,config().ui.muted);
        int rowY=top+57,rowHeight=Math.max(14,Math.min(25,(panelHeight-84)/categories.length));
        for(int i=0;i<categories.length;i++){
            boolean selected=categories[i].equals(prefs().selectedCategory);
            if(selected)Ui.rounded(left+9,rowY,sidebar-15,rowHeight-2,5,Ui.mix(config().ui.surfaceRaised,Ui.accent(),.17f));
            else if(inside(mouseX,mouseY,left+9,rowY,sidebar-15,rowHeight-2))Ui.rounded(left+9,rowY,sidebar-15,rowHeight-2,5,Ui.mix(config().ui.surfaceRaised,config().ui.text,.04f));
            Ui.icon(icons[i],left+15,rowY+5,10,selected?Ui.accent():config().ui.muted);
            fontRendererObj.drawString(fontRendererObj.trimStringToWidth(categories[i],sidebar-40),left+31,rowY+5,selected?config().ui.text:config().ui.muted);
            rowY+=rowHeight;
        }
        fontRendererObj.drawString("FORGE 1.8.9",left+15,top+panelHeight-17,config().ui.muted);
        fontRendererObj.drawString(prefs().selectedCategory,contentX,top+16,config().ui.text);
        String edition="unified".equals(BuildEdition.ID)?"UNIFIED / COMBAT "+(config().combat.enabled?"ON":"OFF"):
                BuildEdition.ALLOW_FULL?"FULL / VISUAL BASE":"VISUALS & PERFORMANCE";
        int labelWidth=fontRendererObj.getStringWidth(edition);
        if(contentWidth>235)fontRendererObj.drawString(edition,contentX+contentWidth-labelWidth,top+17,config().ui.muted);
        Ui.rounded(search.xPosition,search.yPosition,search.width,search.height,5,config().ui.surfaceRaised);
        Ui.icon("search",search.xPosition+6,search.yPosition+5,9,config().ui.muted);
        search.xPosition+=20;search.yPosition+=4;search.width-=24;search.drawTextBox();search.width+=24;search.yPosition-=4;search.xPosition-=20;
        if(!search.isFocused()&&query.isEmpty())fontRendererObj.drawString("Search modules",search.xPosition+20,search.yPosition+5,config().ui.muted);
        filterChip(contentX+contentWidth-51,top+40,23,"star",prefs().favoritesOnly,mouseX,mouseY);
        filterChip(contentX+contentWidth-24,top+40,23,"power",prefs().activeOnly,mouseX,mouseY);
        renderCards(mouseX,mouseY);
        for(GuiButton button:buttonList)button.drawButton(mc,mouseX,mouseY);
        if(selectedModule!=null)drawDetails(mouseX,mouseY);
        if(System.currentTimeMillis()<toastUntil){
            int w=Math.min(width-30,fontRendererObj.getStringWidth(toast)+24);
            Ui.shadow((width-w)/2f,height-35,w,24,7,.45f);Ui.rounded((width-w)/2f,height-35,w,24,7,config().ui.surfaceRaised);
            fontRendererObj.drawString(fontRendererObj.trimStringToWidth(toast,w-20),(width-w)/2+10,height-28,config().ui.text);
        }
        float opening=Math.min(1,(System.nanoTime()-opened)/180000000f);
        if(config().animations&&!config().ui.reducedMotion&&opening<1)Ui.rounded(left,top,panelWidth,panelHeight,config().ui.radius,Ui.alpha(config().ui.surface,(1-opening)*.65f));
    }
    private void filterChip(int x,int y,int w,String icon,boolean selected,int mx,int my){
        Ui.rounded(x,y,w,19,5,selected?Ui.mix(config().ui.surfaceRaised,Ui.accent(),.24f):config().ui.surfaceRaised);
        Ui.icon(icon,x+7,y+5,9,selected?Ui.accent():config().ui.muted);
    }
    private void renderCards(int mouseX,int mouseY){
        List<ModuleDescriptor> modules=visibleModules();cards.clear();
        int rowHeight=48,columns=contentWidth>=490?2:1,cardWidth=(contentWidth-(columns-1)*8)/columns;
        int rows=(modules.size()+columns-1)/columns,max=Math.max(0,rows*rowHeight-(listBottom-listTop));
        targetScroll=Math.max(0,Math.min(max,targetScroll));
        scroll+= (targetScroll-scroll)*(config().animations&&!config().ui.reducedMotion?.32f:1f);
        float factor=framebufferScale();
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT|GL11.GL_ENABLE_BIT);
        try{
            GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(Math.round(contentX*factor),mc.displayHeight-Math.round(listBottom*factor),Math.round(contentWidth*factor),Math.round(Math.max(0,listBottom-listTop)*factor));
            int index=0;
            for(ModuleDescriptor module:modules){
                int x=contentX+(index%columns)*(cardWidth+8),y=listTop+(index/columns)*rowHeight-Math.round(scroll);index++;
                cards.add(new Card(module,x,y,cardWidth,42));
                if(y+42<listTop||y>listBottom)continue;
                boolean hover=inside(mouseX,mouseY,x,y,cardWidth,42);
                Ui.rounded(x,y,cardWidth,42,7,hover?Ui.mix(config().ui.surfaceRaised,config().ui.text,.025f):config().ui.surfaceRaised);
                Ui.rounded(x+7,y+8,25,25,7,Ui.mix(config().ui.surfaceRaised,Ui.accent(),module.enabled()?.16f:.06f));
                Ui.icon(icon(module),x+14,y+15,11,module.enabled()?Ui.accent():config().ui.muted);
                fontRendererObj.drawString(fontRendererObj.trimStringToWidth(module.name(),cardWidth-111),x+40,y+8,config().ui.text);
                fontRendererObj.drawString(fontRendererObj.trimStringToWidth(module.description(),cardWidth-53),x+40,y+24,config().ui.muted);
                Ui.icon("star",x+cardWidth-53,y+10,10,prefs().favorites.contains(module.id())?config().ui.secondary:config().ui.muted);
                toggle(x+cardWidth-31,y+10,module.enabled());
            }
            if(modules.isEmpty()){
                Ui.icon("search",contentX+contentWidth/2-10,listTop+22,20,config().ui.muted);
                String empty=prefs().favoritesOnly?"Favorite modules appear here":"No matching modules";
                drawCenteredString(fontRendererObj,empty,contentX+contentWidth/2,listTop+55,config().ui.muted);
            }
        }finally{GL11.glPopAttrib();}
        if(max>0){
            int available=listBottom-listTop,thumb=Math.max(18,available*available/(rows*rowHeight));
            Ui.rounded(contentX+contentWidth-2,listTop+scroll/max*(available-thumb),2,thumb,1,Ui.alpha(Ui.accent(),.65f));
        }
    }
    private String icon(ModuleDescriptor module){
        if(module.category()==ModuleCategory.COMBAT)return "combat";
        if(module.category()==ModuleCategory.COSMETICS)return "spark";
        if(module.category()==ModuleCategory.HUD)return "grid";
        if(module.category()==ModuleCategory.WORLD)return "world";
        return "eye";
    }
    private void toggle(int x,int y,boolean on){
        Ui.rounded(x,y,23,12,6,on?Ui.accent():Ui.mix(config().ui.surfaceRaised,config().ui.muted,.25f));
        Ui.rounded(x+(on?13:2),y+2,8,8,4,0xfff7f8ff);
    }
    private int modalX(){return Math.max(12,(width-Math.min(360,width-32))/2);}
    private int modalWidth(){return Math.min(360,width-32);}
    private int modalY(){return (height-Math.min(194,height-28))/2;}
    private void drawDetails(int mx,int my){
        ModuleDescriptor module=AlienClient.get().modules().find(selectedModule);if(module==null){selectedModule=null;return;}
        int x=modalX(),y=modalY(),w=modalWidth(),h=Math.min(194,height-28);
        drawRect(0,0,width,height,0xa0000206);Ui.shadow(x,y,w,h,12,.8f);Ui.rounded(x,y,w,h,12,config().ui.surface);
        Ui.rounded(x+12,y+12,28,28,8,Ui.mix(config().ui.surface,Ui.accent(),.18f));Ui.icon(icon(module),x+20,y+20,12,Ui.accent());
        fontRendererObj.drawString(module.name(),x+49,y+14,config().ui.text);
        String impact=module.gameplayImpact()==GameplayImpact.LOCAL_VISUAL?"local visual":"changes gameplay";
        fontRendererObj.drawString(module.category().label()+" / "+impact,x+49,y+29,config().ui.muted);
        Ui.icon("close",x+w-24,y+18,10,config().ui.muted);
        int lineY=y+54;
        for(String line:fontRendererObj.listFormattedStringToWidth(module.description(),w-28)){
            fontRendererObj.drawString(line,x+14,lineY,config().ui.muted);lineY+=12;if(lineY>y+80)break;
        }
        Ui.rounded(x+12,y+88,w-24,25,6,config().ui.surfaceRaised);
        String enabledLabel=module.category()==ModuleCategory.COMBAT&&!config().combat.enabled?"Configured / combat master OFF":"Enabled";
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(enabledLabel,w-83),x+22,y+96,config().ui.text);toggle(x+w-46,y+94,module.enabled());
        Ui.rounded(x+12,y+119,w-24,25,6,config().ui.surfaceRaised);
        fontRendererObj.drawString("Shortcut",x+22,y+127,config().ui.text);
        Integer code=config().moduleKeys.get(module.id());String key=module.id().equals(capturingKey)?"Press a key...":code==null||code==0?"Unbound":Keyboard.getKeyName(code);
        fontRendererObj.drawString(key,x+w-25-fontRendererObj.getStringWidth(key),y+127,module.id().equals(capturingKey)?Ui.accent():config().ui.muted);
        Ui.rounded(x+12,y+h-35,w-24,23,6,Ui.mix(config().ui.surfaceRaised,Ui.accent(),.17f));
        drawCenteredString(fontRendererObj,"Open "+editorName(module)+"  >",x+w/2,y+h-27,config().ui.text);
    }
    private String editorName(ModuleDescriptor module){
        if(module.category()==ModuleCategory.COMBAT)return "Combat Studio";
        if(module.category()==ModuleCategory.COSMETICS)return "Cosmetic Studio";
        if(module.category()==ModuleCategory.HUD)return "HUD Editor";
        if(module.category()==ModuleCategory.WORLD)return "World Studio";
        return "Effects Studio";
    }
    private void openEditor(ModuleDescriptor module){
        if(module.category()==ModuleCategory.COMBAT)mc.displayGuiScreen(new CombatStudioScreen(this));
        else if(module.category()==ModuleCategory.COSMETICS)mc.displayGuiScreen(new CosmeticStudioScreen(this));
        else if(module.category()==ModuleCategory.HUD)mc.displayGuiScreen(new HudEditorScreen(this));
        else if(module.category()==ModuleCategory.WORLD)mc.displayGuiScreen(new WorldStudioScreen(this));
        else mc.displayGuiScreen(new EffectsScreen(this));
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{
        if(selectedModule!=null){
            int mx=modalX(),my=modalY(),mw=modalWidth(),mh=Math.min(194,height-28);ModuleDescriptor module=AlienClient.get().modules().find(selectedModule);
            if(button!=0)return;
            if(inside(x,y,mx+mw-31,my+10,24,24)||!inside(x,y,mx,my,mw,mh)){selectedModule=null;capturingKey=null;return;}
            if(inside(x,y,mx+12,my+88,mw-24,25)){module.toggle();AlienClient.get().save();return;}
            if(inside(x,y,mx+12,my+119,mw-24,25)){capturingKey=module.id();return;}
            if(inside(x,y,mx+12,my+mh-35,mw-24,23)){openEditor(module);return;}
            return;
        }
        boolean searchHit=inside(x,y,search.xPosition,search.yPosition,search.width,search.height);
        search.xPosition+=20;search.yPosition+=4;search.width-=24;search.mouseClicked(x,y,button);search.width+=24;search.yPosition-=4;search.xPosition-=20;
        if(searchHit)search.setFocused(true);if(button!=0)return;
        for(GuiButton control:buttonList)if(control.mousePressed(mc,x,y)){activeButton=control;actionPerformed(control);return;}
        int rowY=top+57,rowHeight=Math.max(14,Math.min(25,(panelHeight-84)/categories.length));
        for(String category:categories){
            if(inside(x,y,left+9,rowY,sidebar-15,rowHeight-2)){
                if("Performance".equals(category)){mc.displayGuiScreen(new AlienMenuScreen(this).openPage(2));return;}
                if("Utility".equals(category)){mc.displayGuiScreen(new AlienMenuScreen(this).openPage(1));return;}
                if("Configs".equals(category)){mc.displayGuiScreen(new AlienMenuScreen(this).openPage(7));return;}
                prefs().selectedCategory=category;targetScroll=0;scroll=0;AlienClient.get().save();return;
            }rowY+=rowHeight;
        }
        if(inside(x,y,contentX+contentWidth-51,top+40,23,19)){prefs().favoritesOnly=!prefs().favoritesOnly;AlienClient.get().save();return;}
        if(inside(x,y,contentX+contentWidth-24,top+40,23,19)){prefs().activeOnly=!prefs().activeOnly;AlienClient.get().save();return;}
        if(y<listTop||y>=listBottom)return;
        for(Card card:cards)if(inside(x,y,card.x,card.y,card.width,card.height)){
            if(x>=card.x+card.width-35){card.module.toggle();notice(card.module.name()+(card.module.enabled()?" enabled":" disabled"));}
            else if(x>=card.x+card.width-58){if(!prefs().favorites.remove(card.module.id()))prefs().favorites.add(card.module.id());}
            else selectedModule=card.module.id();
            AlienClient.get().save();return;
        }
    }
    @Override protected void actionPerformed(GuiButton b){
        if(b.id==900)mc.displayGuiScreen(new AlienMenuScreen(this));
        if(b.id==905)mc.displayGuiScreen(new ThemeEditorScreen(this));
        if(b.id==902)mc.displayGuiScreen(parent);
    }
    @Override protected void keyTyped(char c,int key)throws IOException{
        if(capturingKey!=null){
            if(key!=Keyboard.KEY_ESCAPE)config().moduleKeys.put(capturingKey,key==Keyboard.KEY_DELETE||key==Keyboard.KEY_BACK?0:key);
            capturingKey=null;AlienClient.get().save();return;
        }
        if(selectedModule!=null){if(key==Keyboard.KEY_ESCAPE)selectedModule=null;return;}
        if(search.textboxKeyTyped(c,key)){query=search.getText();targetScroll=0;return;}
        if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent);
    }
    @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0&&selectedModule==null)targetScroll+=wheel>0?-36:36;}
    @Override protected void mouseReleased(int x,int y,int state){if(activeButton!=null){activeButton.mouseReleased(x,y);activeButton=null;}}
    @Override public void updateScreen(){search.updateCursorCounter();}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);AlienClient.get().save();}
    @Override public boolean doesGuiPauseGame(){return false;}
    private static boolean inside(int x,int y,int bx,int by,int w,int h){return x>=bx&&y>=by&&x<bx+w&&y<by+h;}
    private void notice(String text){toast=text;toastUntil=System.currentTimeMillis()+1800;}
    public void searchFor(String text){query=text==null?"":text;search.setText(query);targetScroll=0;}
    public void selectModule(String id){if(AlienClient.get().modules().find(id)!=null)selectedModule=id;}
    public String selectedModule(){return selectedModule;}
    private static final class Card{
        final ModuleDescriptor module;final int x,y,width,height;
        Card(ModuleDescriptor module,int x,int y,int width,int height){this.module=module;this.x=x;this.y=y;this.width=width;this.height=height;}
    }
}
