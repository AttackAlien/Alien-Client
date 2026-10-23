package dev.alien.client.hud;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/** Read-only overlays. Every value comes from the loaded client world or input events. */
public final class UltimateHud extends Gui {
    private static EntityLivingBase target;
    private static Object world;
    private static long seen;
    private static final Map<String,Long> renderAt=new HashMap<String,Long>();
    private static float health=1,trail=1,dt=.016f,alpha=1;
    private static int accent;
    private static final Map<Integer,Float> keyFade=new HashMap<Integer,Float>();
    private UltimateHud() { }
    private static Minecraft mc(){return Minecraft.getMinecraft();}
    private static HudConfig cfg(){return AlienClient.get().config().hud;}
    public static void observe(EntityLivingBase entity){
        if(entity==null||entity==mc().thePlayer||entity.worldObj!=mc().theWorld||entity.isDead)return;
        if(target!=entity){health=trail=HudMath.ratio(entity.getHealth(),entity.getMaxHealth());}
        target=entity;seen=System.nanoTime();
    }
    public static void tick(){
        Minecraft m=mc();
        if(world!=m.theWorld){world=m.theWorld;target=null;keyFade.clear();renderAt.clear();}
        if(m.thePlayer==null){target=null;return;}
        if(m.currentScreen==null&&m.objectMouseOver!=null&&m.objectMouseOver.entityHit instanceof EntityLivingBase)
            observe((EntityLivingBase)m.objectMouseOver.entityHit);
        if(target!=null&&(target.isDead||target.worldObj!=m.theWorld||m.theWorld.getEntityByID(target.getEntityId())!=target
                ||target.getHealth()<=0||m.thePlayer.getDistanceToEntity(target)>cfg().targetRange
                ||System.nanoTime()-seen>cfg().targetHold*1e9))target=null;
    }
    public static int trackedEntity(){return target==null?-1:target.getEntityId();}
    public static float displayedHealth(){return health;}
    public static boolean handles(String id){return "target".equals(id)||"crosshair".equals(id)||"keys".equals(id)
            ||"cps".equals(id)||"armor".equals(id)||"indicators".equals(id);}
    public static boolean crosshairActive(){Minecraft m=mc();HudConfig h=cfg();
        return m.thePlayer!=null&&m.currentScreen==null&&h.enabled&&h.widgets.get("crosshair").enabled
                &&!(h.hideWithDebug&&m.gameSettings.showDebugInfo)&&m.gameSettings.thirdPersonView==0&&!m.thePlayer.isSpectator();}
    public static int themeAccent(){return "Cyan".equals(cfg().theme)?0xff43d9ed:"Light".equals(cfg().theme)?0xff7455c3:Ui.accent();}
    public static int textColor(){return "Light".equals(cfg().theme)?0xff202636:Ui.theme().text;}
    public static int muted(){return "Light".equals(cfg().theme)?0xff536074:Ui.theme().muted;}
    public static int surface(){return "Light".equals(cfg().theme)?0xffeef1f7:"Midnight".equals(cfg().theme)?0xff10121b:Ui.theme().surface;}
    private static int color(int rgb){return Ui.alpha(rgb,alpha*((rgb>>>24)/255f));}
    private static void text(String s,int x,int y,int rgb){(cfg().modernFont?Ui.font():mc().fontRendererObj).drawString(s,x,y,color(rgb),false);}
    private static void card(int w,int h){
        if(!cfg().background)return;
        if(cfg().shadows)Ui.shadow(0,0,w,h,6,.2f*alpha);
        Ui.gradient(0,0,w,h,6,color(surface()),color(Ui.mix(surface(),accent,cfg().gradient?.09f:0)));
        if(cfg().borders)Ui.rounded(0,6,2,h-12,1,color(accent));
    }
    public static void render(String id,HudConfig.Widget widget,int sw,int sh,boolean preview){
        if("target".equals(id)&&target==null&&!preview)return;
        if("crosshair".equals(id)&&!preview&&!crosshairActive())return;
        long now=System.nanoTime();Long previous=renderAt.put(id,now);dt=previous==null?.016f:Math.min(.1f,Math.max(.001f,(now-previous)/1e9f));
        alpha=cfg().opacity*widget.opacity;
        accent=widget.customColor?widget.color:themeAccent();
        int[] b=HudManager.bounds(id,widget,sw,sh);
        GlStateManager.pushMatrix();GlStateManager.translate(b[0],b[1],0);GlStateManager.scale(widget.scale,widget.scale,1);
        try{
            if(!"crosshair".equals(id))card(b[4],b[5]);
            if("target".equals(id))target(preview);
            else if("crosshair".equals(id))crosshair();
            else if("keys".equals(id))keys();
            else if("cps".equals(id)){
                text("CLICKS / SECOND",8,6,muted());
                String left=cfg().cpsLeft?HudManager.leftCps()+" LMB":"",right=cfg().cpsRight?HudManager.rightCps()+" RMB":"";
                text(left+(left.isEmpty()||right.isEmpty()?"":"   ")+right,8,22,textColor());
            }else if("armor".equals(id))armor();
            else if("indicators".equals(id))indicators();
        }finally{GlStateManager.color(1,1,1,1);GlStateManager.popMatrix();}
    }
    private static void target(boolean preview){
        EntityLivingBase entity=target!=null?target:mc().thePlayer;
        if(entity==null){text("Join a world for preview",8,22,muted());return;}
        float next=HudMath.ratio(entity.getHealth(),entity.getMaxHealth());
        float seconds=dt;
        if(!AlienClient.get().config().animations||Ui.theme().reducedMotion)health=trail=next;
        else {health=HudMath.smooth(health,next,seconds,cfg().targetSmoothing);trail=HudMath.smooth(trail,next,seconds,Math.min(3,cfg().targetSmoothing));}
        if(cfg().targetAvatar&&entity instanceof AbstractClientPlayer){
            ResourceLocation skin=((AbstractClientPlayer)entity).getLocationSkin();
            GlStateManager.color(1,1,1,alpha);mc().getTextureManager().bindTexture(skin);
            Gui.drawScaledCustomSizeModalRect(7,8,8,8,8,8,34,34,64,64);
            Gui.drawScaledCustomSizeModalRect(7,8,40,8,8,8,34,34,64,64);
            GlStateManager.color(1,1,1,1);
        }else{Ui.rounded(7,8,34,34,6,color(Ui.mix(surface(),accent,.3f)));text(entity.getName().substring(0,1),19,20,textColor());}
        text(Ui.font().trimStringToWidth(entity.getName(),116),49,9,textColor());
        text(String.format(Locale.ROOT,"%.1f / %.1f HP",entity.getHealth(),entity.getMaxHealth()),49,24,muted());
        Ui.rounded(8,46,158,6,3,color(Ui.mix(surface(),accent,.17f)));
        Ui.rounded(8,46,158*trail,6,3,color(0xffb095b4));Ui.rounded(8,46,158*health,6,3,color(accent));
        if(preview&&target==null)text("Your player / editor preview",8,57,muted());
    }
    private static void crosshair(){
        CrosshairConfig h=cfg().crosshair;
        float speed=mc().thePlayer==null?0:HudMath.speed(mc().thePlayer.posX-mc().thePlayer.prevPosX,mc().thePlayer.posZ-mc().thePlayer.prevPosZ);
        float gap=h.gap+(h.dynamic?Math.min(1,speed/5)*h.spread:0);
        if(h.outline)crossShape(h,gap,h.thickness+2,0xff080b12,1);
        crossShape(h,gap,h.thickness,accent,0);
    }
    private static void crossShape(CrosshairConfig h,float gap,float thick,int rgb,float pad){
        float c=48,t=thick/2,len=h.length+pad;
        if("Circle".equals(h.shape)){
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_LINE_BIT|GL11.GL_COLOR_BUFFER_BIT);
            try{GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glLineWidth(thick);
                GL11.glColor4f((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,alpha);
                GL11.glBegin(GL11.GL_LINE_LOOP);for(int i=0;i<48;i++){double a=i*Math.PI/24;GL11.glVertex2d(c+Math.cos(a)*(gap+len/2),c+Math.sin(a)*(gap+len/2));}GL11.glEnd();
            }finally{GL11.glPopAttrib();}
        }else if(!"Dot".equals(h.shape)){
            Ui.rounded(c-gap-len,c-t,len,thick,.5f,color(rgb));Ui.rounded(c+gap,c-t,len,thick,.5f,color(rgb));
            Ui.rounded(c-t,c+gap,thick,len,.5f,color(rgb));if(!"T".equals(h.shape))Ui.rounded(c-t,c-gap-len,thick,len,.5f,color(rgb));
        }
        if(h.dot||"Dot".equals(h.shape))Ui.rounded(c-t,c-t,thick,thick,thick/2,color(rgb));
    }
    private static void keys(){
        key(mc().gameSettings.keyBindForward,28,4,22,22);key(mc().gameSettings.keyBindLeft,4,28,22,22);
        key(mc().gameSettings.keyBindBack,28,28,22,22);key(mc().gameSettings.keyBindRight,52,28,22,22);
        key(mc().gameSettings.keyBindJump,4,52,70,18);
        int y=74;
        if(cfg().mouseKeys){keyBox(-100,"LMB",Mouse.isButtonDown(0),4,y,34,18);keyBox(-99,"RMB",Mouse.isButtonDown(1),40,y,34,18);y+=22;}
        if(cfg().sprintKey)keyBox(-500,"SPRINT",mc().thePlayer!=null&&mc().thePlayer.isSprinting(),4,y,70,16);
    }
    private static void key(KeyBinding binding,int x,int y,int w,int h){
        int code=binding.getKeyCode();String label=code<0?Mouse.getButtonName(code+100):code>0&&code<Keyboard.KEYBOARD_SIZE?Keyboard.getKeyName(code):"--";
        if(binding==mc().gameSettings.keyBindJump)label="JUMP";
        keyBox(code,label,binding.isKeyDown(),x,y,w,h);
    }
    private static void keyBox(int code,String name,boolean down,int x,int y,int w,int h){
        float old=keyFade.containsKey(code)?keyFade.get(code):0;
        float value=!AlienClient.get().config().animations||Ui.theme().reducedMotion?(down?1:0):HudMath.smooth(old,down?1:0,dt,18);
        keyFade.put(code,value);Ui.rounded(x,y,w,h,4,color(Ui.mix(Ui.mix(surface(),accent,.13f),accent,value)));
        String label=Ui.font().trimStringToWidth(name,w-4);text(label,x+(w-Ui.font().getStringWidth(label))/2,y+(h-9)/2,textColor());
    }
    private static void armor(){
        if(mc().thePlayer==null){text("Join a world / real armor",8,8,muted());return;}
        RenderHelper.enableGUIStandardItemLighting();
        try{for(int i=3;i>=0;i--){ItemStack stack=mc().thePlayer.inventory.armorInventory[i];int y=5+(3-i)*24;
            if(stack!=null)mc().getRenderItem().renderItemAndEffectIntoGUI(stack,6,y);
        }}finally{RenderHelper.disableStandardItemLighting();GlStateManager.disableLighting();}
        for(int i=3;i>=0;i--){ItemStack stack=mc().thePlayer.inventory.armorInventory[i];int y=5+(3-i)*24;
            if(stack==null){text("Empty slot",29,y+5,muted());continue;}
            int maximum=stack.getMaxDamage(),remaining=Math.max(0,maximum-stack.getItemDamage());
            float ratio=HudMath.ratio(remaining,maximum);
            text(cfg().armorPercent?Math.round(ratio*100)+"%":remaining+" / "+maximum,29,y+2,textColor());
            Ui.rounded(29,y+15,88,3,1,color(Ui.mix(surface(),accent,.2f)));Ui.rounded(29,y+15,88*ratio,3,1,color(ratio<.2f?0xffff697f:accent));
        }
    }
    private static void indicators(){
        if(mc().thePlayer==null){text("Join a world / live indicators",8,8,muted());return;}
        int y=7;
        if(cfg().indicatorHealth){text(String.format(Locale.ROOT,"Health %.1f / %.1f",mc().thePlayer.getHealth(),mc().thePlayer.getMaxHealth()),8,y,textColor());y+=15;}
        if(cfg().indicatorSpeed){text(String.format(Locale.ROOT,"Speed %.2f b/s",HudMath.speed(mc().thePlayer.posX-mc().thePlayer.prevPosX,mc().thePlayer.posZ-mc().thePlayer.prevPosZ)),8,y,muted());y+=15;}
        if(cfg().indicatorFacing){text("Facing "+mc().thePlayer.getHorizontalFacing().getName(),8,y,muted());y+=15;}
        if(cfg().indicatorEffects){
            int n=0;for(PotionEffect effect:mc().thePlayer.getActivePotionEffects()){
                if(n++==4)break;
                text(Ui.font().trimStringToWidth(I18n.format(Potion.potionTypes[effect.getPotionID()].getName())+" "+(effect.getAmplifier()+1)+" / "+Potion.getDurationString(effect),145),8,y,muted());y+=14;
            }
            if(n==0)text("No active effects",8,y,muted());
        }
    }
}
