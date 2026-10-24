package dev.alien.client.gui;

import com.lukflug.panelstudio.base.SettingsAnimation;
import dev.alien.client.AlienClient;
import dev.alien.client.gui.theme.ThemeConfig;
import dev.alien.client.gui.ui.SmoothFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;
import java.util.Locale;
import java.util.function.Consumer;

/** Original immediate-mode controls with PanelStudio elapsed-time hover interpolation. */
public final class Ui extends Gui {
    public static final int TEXT = 0xfff0eff9, MUTED = 0xff9496ae, PANEL = 0xff14151f;
    private static final ThemeConfig FALLBACK = new ThemeConfig();
    private Ui() { }
    public static ThemeConfig theme() {
        return AlienClient.get() == null || AlienClient.get().config() == null || AlienClient.get().config().ui == null
                ? FALLBACK : AlienClient.get().config().ui;
    }
    public static FontRenderer font() {
        return "Vanilla".equals(theme().font) ? Minecraft.getMinecraft().fontRendererObj : SmoothFont.get();
    }
    public static int accent() {
        if (AlienClient.get() != null && AlienClient.get().config().rainbowAccent)
            return 0xff000000 | java.awt.Color.HSBtoRGB((System.currentTimeMillis()%12000L)/12000f,.65f,1f);
        return theme().primary | 0xff000000;
    }
    public static int alpha(int color,float amount) { return (Math.max(0,Math.min(255,Math.round(255*amount)))<<24)|(color&0xffffff); }
    public static int mix(int a,int b,float f) {
        f=Math.max(0,Math.min(1,f)); float g=1-f;
        return 0xff000000|((int)(((a>>16)&255)*g+((b>>16)&255)*f)<<16)
                |((int)(((a>>8)&255)*g+((b>>8)&255)*f)<<8)|(int)((a&255)*g+(b&255)*f);
    }
    public static float animate(float current,float target) {
        boolean motion=AlienClient.get()!=null && AlienClient.get().config().animations && !theme().reducedMotion;
        if(!motion)return target;
        float frame=AlienClient.get().performance()==null?16.67f:(float)AlienClient.get().performance().frameTimeMs();
        frame=Math.max(1,Math.min(50,frame));
        float speed=theme().animationSpeed;
        return current+(target-current)*(1-(float)Math.exp(-frame*.014f*speed));
    }
    public static void text(String value,float x,float y,int color) { font().drawString(value,x,y,color,false); }
    public static void centered(String value,float x,float y,int color) { text(value,x-font().getStringWidth(value)/2f,y,color); }
    public static void notify(String message) { dev.alien.client.gui.ui.Notifications.push(message); }
    public static void notifications(int width,int height) { dev.alien.client.gui.ui.Notifications.render(width,height); }
    public static void background(int width,int height) {
        Minecraft mc=Minecraft.getMinecraft();
        boolean menu=mc.theWorld==null&&dev.alien.client.studio.MenuScene.render(width,height);
        if(menu&&theme().blur>0)dev.alien.client.gui.ui.BackdropBlur.renderMenu(theme().blur);
        if(mc.theWorld!=null && theme().blur>0) dev.alien.client.gui.ui.BackdropBlur.render(theme().blur);
        float opacity=mc.theWorld==null?(menu?.52f:1f):.32f+.32f*theme().opacity;
        gradient(0,0,width,height,0,alpha(theme().surface,opacity),alpha(mix(theme().surface,theme().primary,.09f),Math.min(1,opacity+.1f)));
    }
    public static void panel(int x,int y,int w,int h,int color) { rounded(x,y,w,h,Math.min(theme().radius,Math.min(w,h)/2f),color); }
    public static void card(float x,float y,float w,float h) {
        shadow(x,y,w,h,theme().radius,theme().shadow);
        gradient(x,y,w,h,theme().radius,alpha(theme().surfaceRaised,theme().opacity),alpha(theme().surface,theme().opacity));
    }
    public static void shadow(float x,float y,float w,float h,float radius,float strength) {
        if(strength<=0)return;
        strength=Math.min(1,strength);
        for(int i=5;i>=1;i--) {
            float grow=i*1.4f;
            rounded(x-grow,y-grow+2,w+grow*2,h+grow*2,radius+grow,alpha(0, strength*.025f*(6-i)));
        }
    }
    public static void rounded(float x,float y,float w,float h,float radius,int color) { gradient(x,y,w,h,radius,color,color); }
    public static void gradient(float x,float y,float w,float h,float radius,int top,int bottom) {
        if(w<=0||h<=0)return;
        radius=Math.max(0,Math.min(radius,Math.min(w,h)/2));
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_LIGHTING_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glBegin(GL11.GL_TRIANGLE_FAN); vertex(x+w/2,y+h/2,x,y,h,top,bottom,1);
            for(int corner=0;corner<4;corner++)for(int step=0;step<=8;step++) {
                double angle=Math.toRadians(-90+corner*90+step*90d/8);
                float cx=corner<2?x+w-radius:x+radius,cy=(corner==0||corner==3)?y+radius:y+h-radius;
                vertex(cx+(float)Math.cos(angle)*radius,cy+(float)Math.sin(angle)*radius,x,y,h,top,bottom,1);
            }
            vertex(x+w-radius,y,x,y,h,top,bottom,1);GL11.glEnd();
            // A transparent outer fringe gives a stable subpixel edge without polygon smoothing.
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            for(int k=0;k<=36;k++) {
                int index=k%36,corner=index/9,step=index%9;
                double angle=Math.toRadians(-90+corner*90+step*90d/8);
                float cx=corner<2?x+w-radius:x+radius,cy=(corner==0||corner==3)?y+radius:y+h-radius;
                float dx=(float)Math.cos(angle),dy=(float)Math.sin(angle);
                vertex(cx+dx*radius,cy+dy*radius,x,y,h,top,bottom,1);
                vertex(cx+dx*(radius+.55f),cy+dy*(radius+.55f),x,y,h,top,bottom,0);
            }
            GL11.glEnd();
        } finally { GL11.glPopAttrib(); }
    }
    private static void vertex(float vx,float vy,float x,float y,float h,int top,int bottom,float opacity) {
        float f=Math.max(0,Math.min(1,(vy-y)/h)),g=1-f;
        GL11.glColor4f((((top>>16)&255)*g+((bottom>>16)&255)*f)/255,
                (((top>>8)&255)*g+((bottom>>8)&255)*f)/255,((top&255)*g+(bottom&255)*f)/255,
                (((top>>>24)&255)*g+((bottom>>>24)&255)*f)/255*opacity);
        GL11.glVertex2f(vx,vy);
    }
    public static void slider(float x,float y,float w,float h,String label,String value,float fraction,boolean hover) {
        float top=h>=26?y+2:y-1,track=h>=26?y+h-7:y+h-5;
        text(font().trimStringToWidth(label,Math.max(20,(int)w-font().getStringWidth(value)-10)),x,top,theme().muted);
        text(value,x+w-font().getStringWidth(value),top,hover?accent():theme().text);
        rounded(x,track,w,3,1.5f,alpha(theme().muted,.18f));
        float f=Math.max(0,Math.min(1,fraction));
        if(f>0)gradient(x,track,w*f,3,1.5f,accent(),theme().secondary);
        shadow(x+w*f-3,track-2,6,7,3,hover?.5f:0);
        rounded(x+w*f-3,track-2,6,7,3,theme().text);
    }
    public static void switchControl(float x,float y,float w,float h,String label,boolean value,float hover) {
        text(font().trimStringToWidth(label,Math.max(10,(int)w-34)),x+9,y+(h-10)/2,theme().text);
        float sx=x+w-29,sy=y+(h-10)/2;
        rounded(sx,sy,21,11,5.5f,value?accent():mix(theme().surfaceRaised,theme().muted,.25f));
        rounded(sx+(value?12:2),sy+2,7,7,3.5f,value?0xffffffff:theme().muted);
    }
    public static void tooltip(String value,int mx,int my,int sw,int sh) {
        int w=Math.min(sw-16,font().getStringWidth(value)+18),h=25,x=Math.min(sw-w-8,mx+10),y=Math.min(sh-h-8,my+12);
        shadow(x,y,w,h,5,.7f);rounded(x,y,w,h,5,0xf51b1b27);
        text(font().trimStringToWidth(value,w-16),x+8,y+8,0xffeeeeff);
    }
    /** Small original vector pictograms; no third-party icon atlas. */
    public static void icon(String id,float x,float y,float size,int color) {
        String key=id==null?"":id.toLowerCase(Locale.ROOT);
        float u=size/16f;
        if(key.contains("alien")) {
            rounded(x+u,y+u,14*u,11*u,6*u,color);
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT);
            try{GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glColor4f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,1);GL11.glBegin(GL11.GL_TRIANGLES);GL11.glVertex2f(x+2*u,y+8*u);GL11.glVertex2f(x+14*u,y+8*u);GL11.glVertex2f(x+8*u,y+15*u);GL11.glEnd();}finally{GL11.glPopAttrib();}
            GL11.glPushMatrix();GL11.glTranslatef(x+5*u,y+7*u,0);GL11.glRotatef(28,0,0,1);rounded(-2*u,-u,4*u,2*u,u,theme().surface);GL11.glPopMatrix();
            GL11.glPushMatrix();GL11.glTranslatef(x+11*u,y+7*u,0);GL11.glRotatef(-28,0,0,1);rounded(-2*u,-u,4*u,2*u,u,theme().surface);GL11.glPopMatrix();
        }
        else if(key.equals("combat")) {line(x+3*u,y+3*u,x+13*u,y+13*u,2*u,color);line(x+13*u,y+3*u,x+3*u,y+13*u,2*u,color);line(x+u,y+10*u,x+6*u,y+15*u,1.5f*u,color);line(x+10*u,y+15*u,x+15*u,y+10*u,1.5f*u,color);}
        else if(key.equals("grid")) { for(int a=0;a<2;a++)for(int b=0;b<2;b++)rounded(x+(2+a*7)*u,y+(2+b*7)*u,5*u,5*u,u,color); }
        else if(key.equals("eye")) { ring(x+8*u,y+8*u,5*u,u,color);rounded(x+6*u,y+6*u,4*u,4*u,2*u,color); }
        else if(key.equals("spark")) {line(x+8*u,y+u,x+8*u,y+15*u,1.2f*u,color);line(x+u,y+8*u,x+15*u,y+8*u,1.2f*u,color);line(x+4*u,y+4*u,x+12*u,y+12*u,u,color);line(x+12*u,y+4*u,x+4*u,y+12*u,u,color);}
        else if(key.equals("folder")) {rounded(x+u,y+5*u,14*u,9*u,2*u,color);rounded(x+u,y+2*u,6*u,5*u,1.5f*u,color);}
        else if(key.equals("player")) {rounded(x+5*u,y+u,6*u,6*u,3*u,color);rounded(x+2*u,y+9*u,12*u,6*u,3*u,color);}
        else if(key.equals("close")) {line(x+3*u,y+3*u,x+13*u,y+13*u,1.4f*u,color);line(x+13*u,y+3*u,x+3*u,y+13*u,1.4f*u,color);}
        else if(key.equals("sun")) {ring(x+8*u,y+8*u,3*u,u,color);for(int i=0;i<8;i++){double a=i*Math.PI/4;line(x+8*u+(float)Math.cos(a)*5*u,y+8*u+(float)Math.sin(a)*5*u,x+8*u+(float)Math.cos(a)*7*u,y+8*u+(float)Math.sin(a)*7*u,u,color);}}
        else if(key.equals("power")) {ring(x+8*u,y+8*u,5*u,u,color);line(x+8*u,y+u,x+8*u,y+8*u,2*u,color);}
        else if(key.contains("search")) { ring(x+6*u,y+6*u,4*u,1.5f*u,color);line(x+9*u,y+9*u,x+14*u,y+14*u,1.5f*u,color); }
        else if(key.contains("hud")) { rounded(x,y,size,size,3*u,alpha(color,.15f));rounded(x+3*u,y+3*u,10*u,3*u,1,color);rounded(x+3*u,y+8*u,4*u,5*u,1,color);rounded(x+9*u,y+8*u,4*u,5*u,1,color); }
        else if(key.contains("cosmetic")) { line(x+2*u,y+7*u,x+8*u,y+2*u,1.5f*u,color);line(x+8*u,y+2*u,x+14*u,y+7*u,1.5f*u,color);line(x+1*u,y+8*u,x+15*u,y+8*u,1.5f*u,color);ring(x+8*u,y+12*u,3*u,1*u,color); }
        else if(key.contains("world")||key.contains("sky")) { ring(x+8*u,y+8*u,6*u,1.2f*u,color);line(x+2*u,y+8*u,x+14*u,y+8*u,1*u,color);line(x+8*u,y+2*u,x+8*u,y+14*u,1*u,color); }
        else if(key.contains("effect")||key.contains("performance")) { line(x+10*u,y+1*u,x+5*u,y+9*u,2*u,color);line(x+5*u,y+9*u,x+11*u,y+8*u,2*u,color);line(x+11*u,y+8*u,x+7*u,y+15*u,2*u,color); }
        else if(key.contains("star")||key.contains("favorite")) { for(int i=0;i<5;i++){ double a=-Math.PI/2+i*Math.PI*2/5,b=a+Math.PI*4/5;line(x+8*u+(float)Math.cos(a)*6*u,y+8*u+(float)Math.sin(a)*6*u,x+8*u+(float)Math.cos(b)*6*u,y+8*u+(float)Math.sin(b)*6*u,1.1f*u,color); } }
        else if(key.contains("config")||key.contains("setting")) { for(int i=0;i<3;i++){line(x+2*u,y+(4+i*4)*u,x+14*u,y+(4+i*4)*u,1*u,color);rounded(x+(i==1?9:5)*u,y+(2+i*4)*u,3*u,4*u,1,color);} }
        else if(key.contains("theme")||key.contains("color")) { rounded(x+2*u,y+2*u,12*u,12*u,6*u,color);rounded(x+5*u,y+5*u,3*u,3*u,1.5f*u,theme().surface); }
        else { rounded(x+3*u,y+3*u,10*u,10*u,3*u,alpha(color,.2f));line(x+5*u,y+8*u,x+11*u,y+8*u,1.5f*u,color);line(x+8*u,y+5*u,x+8*u,y+11*u,1.5f*u,color); }
    }
    private static void line(float x1,float y1,float x2,float y2,float width,int color) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_LINE_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        try {GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glEnable(GL11.GL_LINE_SMOOTH);GL11.glLineWidth(Math.max(1,width));GL11.glColor4f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,(color>>>24)/255f);GL11.glBegin(GL11.GL_LINES);GL11.glVertex2f(x1,y1);GL11.glVertex2f(x2,y2);GL11.glEnd();}finally{GL11.glPopAttrib();}
    }
    private static void ring(float x,float y,float r,float width,int color) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_LINE_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        try {GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glEnable(GL11.GL_LINE_SMOOTH);GL11.glLineWidth(Math.max(1,width));GL11.glColor4f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,(color>>>24)/255f);GL11.glBegin(GL11.GL_LINE_LOOP);for(int i=0;i<28;i++){double a=i*Math.PI/14;GL11.glVertex2f(x+(float)Math.cos(a)*r,y+(float)Math.sin(a)*r);}GL11.glEnd();}finally{GL11.glPopAttrib();}
    }
    public static class Button extends GuiButton {
        private boolean selected;private float hover,press,toggleProgress;private long pressedAt;
        // Retains PR #3's MIT PanelStudio foundation in the redesigned controls.
        private final SettingsAnimation hoverAnimation = new SettingsAnimation(() -> {
            if (AlienClient.get() == null || !AlienClient.get().config().animations || theme().reducedMotion) return 0;
            float speed = Math.max(.25f, theme().animationSpeed);
            return Math.max(1, Math.round(150f / speed));
        }, System::currentTimeMillis);
        public Button(int id,int x,int y,int width,int height,String text){super(id,x,y,width,height,text);hoverAnimation.initValue(0);}
        public Button selected(boolean value){selected=value;return this;}
        @Override public void drawButton(Minecraft mc,int mx,int my){
            if(!visible)return;
            hovered=mx>=xPosition&&my>=yPosition&&mx<xPosition+width&&my<yPosition+height;
            double target=hovered&&enabled&&theme().hoverEffects?1:0;
            if(hoverAnimation.getTarget()!=target)hoverAnimation.setValue(target);
            hover=(float)hoverAnimation.getValue();press=animate(press,System.currentTimeMillis()-pressedAt<100?1:0);
            int base=selected?mix(theme().surfaceRaised,accent(),.28f):theme().surfaceRaised;
            gradient(xPosition,yPosition,width,height,Math.min(theme().radius,6),mix(base,accent(),hover*.12f+press*.12f),base);
            String lower=displayString.toLowerCase(Locale.ROOT);boolean toggle=lower.endsWith(": on")||lower.endsWith(": off");
            if(toggle){boolean on=lower.endsWith(": on");toggleProgress=animate(toggleProgress,on?1:0);String label=displayString.substring(0,displayString.length()-(on?4:5));text(font().trimStringToWidth(label,Math.max(10,width-34)),xPosition+9,yPosition+(height-10)/2f,theme().text);float sx=xPosition+width-29,sy=yPosition+(height-10)/2f;rounded(sx,sy,21,11,5.5f,mix(mix(theme().surfaceRaised,theme().muted,.25f),accent(),toggleProgress));rounded(sx+2+10*toggleProgress,sy+2,7,7,3.5f,mix(theme().muted,0xffffffff,toggleProgress));}
            else {
                if(selected)rounded(xPosition+3,yPosition+height*.25f,2,height*.5f,1,accent());
                int color=enabled?(selected?0xffffffff:theme().text):theme().muted;
                String value=font().trimStringToWidth(displayString,Math.max(0,width-16));
                text(value,xPosition+8,yPosition+(height-font().FONT_HEIGHT)/2f,color);
            }
            mouseDragged(mc,mx,my);
        }
        @Override public boolean mousePressed(Minecraft mc,int x,int y){boolean yes=super.mousePressed(mc,x,y);if(yes)pressedAt=System.currentTimeMillis();return yes;}
    }
    public static class Slider extends GuiButton {
        private final String label;private final float min,max,step;private float value;private boolean dragging;private final Consumer<Float> setter;
        public Slider(int id,int x,int y,int w,int h,String label,float min,float max,float step,float value,Consumer<Float> setter){super(id,x,y,w,h,label);this.label=label;this.min=min;this.max=max;this.step=step;this.value=value;this.setter=setter;}
        public float value(){return value;}
        @Override public void drawButton(Minecraft mc,int mx,int my){if(!visible)return;mouseDragged(mc,mx,my);String val=step>=1?Integer.toString(Math.round(value)):String.format(Locale.ROOT,"%.2f",value);slider(xPosition,yPosition,width,height,label,val,(value-min)/(max-min),mx>=xPosition&&my>=yPosition&&mx<xPosition+width&&my<yPosition+height);}
        private void update(int mx){float next=min+Math.max(0,Math.min(1,(mx-xPosition)/(float)width))*(max-min);next=Math.max(min,Math.min(max,step>0?Math.round(next/step)*step:next));if(next!=value){value=next;setter.accept(next);}}
        @Override public boolean mousePressed(Minecraft mc,int x,int y){if(!super.mousePressed(mc,x,y))return false;if(y<yPosition+12&&x>xPosition+width-46){mc.displayGuiScreen(new dev.alien.client.gui.ui.ValueInputScreen(mc.currentScreen,label,min,max,value,next->{value=next;setter.accept(next);}));return true;}dragging=true;update(x);return true;}
        @Override protected void mouseDragged(Minecraft mc,int x,int y){if(dragging)update(x);}
        @Override public void mouseReleased(int x,int y){dragging=false;}
    }
}
