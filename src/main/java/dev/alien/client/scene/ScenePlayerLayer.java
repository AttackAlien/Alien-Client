package dev.alien.client.scene;
import dev.alien.client.AlienClient;
import java.awt.*;import java.awt.geom.*;import java.awt.image.BufferedImage;
import net.minecraft.client.Minecraft;import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.*;import net.minecraft.client.renderer.GlStateManager;import net.minecraft.client.renderer.entity.RenderPlayer;import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;import org.lwjgl.util.glu.Sphere;import org.lwjgl.util.glu.Cylinder;
/** Original cute face art and posed decorative skeleton. Depth remains enabled. */
public final class ScenePlayerLayer implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer renderer;private static int drawnThisFrame;public static void beginFrame(){drawnThisFrame=0;}private static ResourceLocation open,blink;private static int sphere,cylinder;private static long cuteFrames,boneFrames;
    public ScenePlayerLayer(RenderPlayer r){renderer=r;}
    public static long cuteFrames(){return cuteFrames;}public static long boneFrames(){return boneFrames;}
    private static void primitives(){if(sphere!=0)return;sphere=GL11.glGenLists(1);GL11.glNewList(sphere,GL11.GL_COMPILE);new Sphere().draw(1,16,10);GL11.glEndList();cylinder=GL11.glGenLists(1);GL11.glNewList(cylinder,GL11.GL_COMPILE);new Cylinder().draw(1,1,1,10,1);GL11.glEndList();}
    @Override public void doRenderLayer(AbstractClientPlayer p,float walk,float amount,float partial,float age,float yaw,float pitch,float scale){
        SceneConfig c=AlienClient.get().config().scenes;Minecraft m=Minecraft.getMinecraft();
        if(!c.enabled||(!c.cute&&!"Skeleton".equals(c.mode))||drawnThisFrame>=Math.min(32,AlienClient.get().performance().cosmeticBudget())||p.isSpectator()||p.isInvisibleToPlayer(m.thePlayer)||p.getDistanceSqToEntity(m.thePlayer)>c.distance*c.distance)return;
        drawnThisFrame++;int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);float oldLightX=net.minecraft.client.renderer.OpenGlHelper.lastBrightnessX,oldLightY=net.minecraft.client.renderer.OpenGlHelper.lastBrightnessY;GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit,240,240);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            if("Skeleton".equals(c.mode)){bones(renderer.getMainModel(),c,scale);boneFrames++;}
            if(c.cute&&!"Glass".equals(c.mode)){face(p,scale);cuteFrames++;}
        }finally{GL11.glPopAttrib();net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit,oldLightX,oldLightY);GlStateManager.bindTexture(texture);}
    }
    private void face(AbstractClientPlayer p,float scale){Minecraft m=Minecraft.getMinecraft();if(open==null){open=m.getTextureManager().getDynamicTextureLocation("alien_cute_open",new DynamicTexture(faceTexture(false)));blink=m.getTextureManager().getDynamicTextureLocation("alien_cute_blink",new DynamicTexture(faceTexture(true)));}
        GL11.glPushMatrix();try{renderer.getMainModel().bipedHead.postRender(scale);GL11.glScalef(scale,scale,scale);GL11.glEnable(GL11.GL_TEXTURE_2D);m.getTextureManager().bindTexture(p.ticksExisted%87<4?blink:open);GL11.glColor4f(1,1,1,1);
            GL11.glBegin(GL11.GL_QUADS);GL11.glTexCoord2f(0,0);GL11.glVertex3f(-4,-8,-4.28f);GL11.glTexCoord2f(0,1);GL11.glVertex3f(-4,0,-4.28f);GL11.glTexCoord2f(1,1);GL11.glVertex3f(4,0,-4.28f);GL11.glTexCoord2f(1,0);GL11.glVertex3f(4,-8,-4.28f);GL11.glEnd();
        }finally{GL11.glPopMatrix();}}
    public static BufferedImage faceTexture(boolean closed){BufferedImage im=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);Graphics2D g=im.createGraphics();try{g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        for(int x:new int[]{12,39}){g.setColor(new Color(35,25,48));if(closed){g.setStroke(new BasicStroke(3));g.drawArc(x,22,13,8,10,160);}else{g.fill(new Ellipse2D.Float(x,17,14,23));g.setColor(new Color(136,112,169));g.fillOval(x+3,28,8,10);g.setColor(Color.WHITE);g.fillOval(x+2,19,6,8);g.fillOval(x+8,30,3,4);}}
        g.setColor(new Color(248,133,183,180));g.fillOval(7,39,13,6);g.fillOval(44,39,13,6);g.setColor(new Color(78,38,78));g.setStroke(new BasicStroke(2));g.drawArc(26,39,6,7,180,180);g.drawArc(32,39,6,7,180,180);g.setColor(new Color(236,125,158));g.fillOval(30,44,4,3);
        return im;}finally{g.dispose();}}
    private void bones(ModelPlayer model,SceneConfig c,float scale){primitives();GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDepthMask(false);
        for(int pass=0;pass<2;pass++){float width=pass==0?1.7f:1;float alpha=pass==0?.13f:.88f;GL11.glColor4f((c.color>>16&255)/255f,(c.color>>8&255)/255f,(c.color&255)/255f,alpha);
            part(model.bipedHead,scale,()->{ball(0,-4.2,0,3.15,3.5,2.8);bone(0,-1,0,0,0,0,.43*width);
                GL11.glColor4f(.035f,.012f,.065f,alpha);ball(-1.25,-4.7,-2.5,.82,1,.55);ball(1.25,-4.7,-2.5,.82,1,.55);ball(0,-3.15,-2.75,.3,.5,.32);
                GL11.glColor4f((c.color>>16&255)/255f,(c.color>>8&255)/255f,(c.color&255)/255f,alpha);for(int tooth=-2;tooth<=2;tooth++)ball(tooth*.52,-1.65,-2.0,.22,.43,.3);
            });
            part(model.bipedBody,scale,()->{bone(0,.2,0,0,10,0,.26*width);for(int rib=0;rib<5;rib++)for(int side:new int[]{-1,1}){double y=1.7+rib*1.35;bone(0,y,-.2,side*2.6,y+.4,-.65,.16*width);bone(side*2.6,y+.4,-.65,side*2.3,y+1.15,.1,.16*width);}ball(0,10,0,2,.65,1);});
            for(int side:new int[]{-1,1}){ModelRenderer arm=side<0?model.bipedRightArm:model.bipedLeftArm,leg=side<0?model.bipedRightLeg:model.bipedLeftLeg;
                part(arm,scale,()->{double x=side;bone(x,0,0,x,5.3,0,.26*width);ball(x,5.3,0,.5,.5,.5);bone(x,5.3,0,x,10.5,0,.22*width);ball(x,10.5,0,.52,.6,.38);});
                part(leg,scale,()->{bone(0,0,0,0,5.4,0,.32*width);ball(0,5.4,0,.55,.55,.55);bone(0,5.4,0,0,10.8,0,.28*width);ball(0,11.0,-.5,.6,.4,1.1);});}
        }}
    private static void part(ModelRenderer part,float scale,Runnable draw){GL11.glPushMatrix();try{part.postRender(scale);GL11.glScalef(scale,scale,scale);draw.run();}finally{GL11.glPopMatrix();}}
    private static void ball(double x,double y,double z,double rx,double ry,double rz){GL11.glPushMatrix();GL11.glTranslated(x,y,z);GL11.glScaled(rx,ry,rz);GL11.glCallList(sphere);GL11.glPopMatrix();}
    private static void bone(double x,double y,double z,double a,double b,double c,double radius){double dx=a-x,dy=b-y,dz=c-z,len=Math.sqrt(dx*dx+dy*dy+dz*dz);if(len<1e-5)return;GL11.glPushMatrix();GL11.glTranslated(x,y,z);GL11.glRotated(Math.toDegrees(Math.acos(dz/len)),-dy,dx,0);GL11.glScaled(radius,radius,len);GL11.glCallList(cylinder);GL11.glPopMatrix();}
    @Override public boolean shouldCombineTextures(){return false;}
}
