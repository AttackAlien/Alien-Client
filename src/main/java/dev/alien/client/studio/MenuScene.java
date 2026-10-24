package dev.alien.client.studio;
import dev.alien.client.AlienClient;import dev.alien.client.gui.Ui;
import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.texture.TextureAtlasSprite;import net.minecraft.client.renderer.texture.TextureMap;import org.lwjgl.opengl.*;import org.lwjgl.util.glu.GLU;
/** Original cached 3D diorama, not a running/saved world or an external panorama. */
public final class MenuScene {
    private static int geometry;private static String key="";private static long frames;private static double opened=System.nanoTime()/1e9;
    private static TextureAtlasSprite top,side,wood,leaves,snow,flower,water;
    public static void invalidate(){key="";}
    private MenuScene(){}public static long frames(){return frames;}
    public static boolean render(int w,int h){
        if(AlienClient.get()==null||!AlienClient.get().config().studio.menuScene||Minecraft.getMinecraft().theWorld!=null)return false;
        StudioConfig c=AlienClient.get().config().studio;boolean night="Night".equals(c.time),sunset="Sunset".equals(c.time);double now=System.nanoTime()/1e9-opened;
        Ui.gradient(0,0,w,h,0,night?0xff11142c:sunset?0xff655082:0xff73b5e2,night?0xff655181:sunset?0xffedb292:0xffd6e7e7);
        int matrix=GL11.glGetInteger(GL11.GL_MATRIX_MODE);GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();GLU.gluPerspective(58,w/(float)h,.1f,150);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();GL11.glLoadIdentity();
        try{
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(true);GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glBindTexture(GL11.GL_TEXTURE_2D,Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());GL11.glDisable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1f);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_BLEND);
            GL11.glTranslatef(0,-2.5f,-c.cameraDistance);GL11.glRotatef(c.cameraPitch,1,0,0);GL11.glRotatef(c.cameraYaw+(c.orbit&&AlienClient.get().config().animations&&!Ui.theme().reducedMotion?(float)(Math.sin(now*.08)*12):0),0,1,0);
            String next=c.scene+"/"+c.time;if(!next.equals(key)||geometry==0){if(geometry!=0)GL11.glDeleteLists(geometry,1);geometry=GL11.glGenLists(1);GL11.glNewList(geometry,GL11.GL_COMPILE);build(c);GL11.glEndList();key=next;}GL11.glCallList(geometry);
            GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glDepthMask(false);
            // Water is translucent geometry over the river bed; subtle moving highlights are actual 3D strips.
            GL11.glBegin(GL11.GL_QUADS);GL11.glColor4f(.4f*shade,.72f*shade,.9f*shade,.75f);for(int x=-40;x<40;x++){double z=Math.sin(x*.17)*2;texture=water;quad(x,.28,z-2,x+1,.28,z+2);}GL11.glEnd();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBegin(GL11.GL_LINES);GL11.glColor4f(.8f,.94f,1,.35f);for(int i=0;i<34;i++){double x=((i*2.17+now*.4)%30)-15,z=Math.sin(x*.17)*2+Math.sin(i*5)*1.5;GL11.glVertex3d(x,.29,z);GL11.glVertex3d(x+.45,.29,z);}GL11.glEnd();
            if(!"Clear".equals(c.weather)){boolean snow="Snow".equals(c.weather);GL11.glPointSize(3);GL11.glBegin(snow?GL11.GL_POINTS:GL11.GL_LINES);GL11.glColor4f(.85f,.9f,1,.58f);for(int i=0;i<120;i++){double x=Math.sin(i*9.17)*16,z=Math.cos(i*5.31)*15,y=12-((i*.67+now*(snow?.9:5))%12);GL11.glVertex3d(x,y,z);if(!snow)GL11.glVertex3d(x+.1,y-.6,z);}GL11.glEnd();if("Mixed".equals(c.weather)){GL11.glPointSize(4);GL11.glBegin(GL11.GL_POINTS);for(int i=0;i<50;i++)GL11.glVertex3d(Math.sin(i*8.3)*16,10-((i*.4+now*.7)%10),Math.cos(i*4.2)*15);GL11.glEnd();}}
            frames++;return true;
        }finally{GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPopMatrix();GL11.glPopAttrib();GL11.glMatrixMode(matrix);}
    }
    private static float shade=1;private static TextureAtlasSprite texture;
    private static TextureAtlasSprite sprite(String name){return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/"+name);}
    private static void build(StudioConfig c){shade="Night".equals(c.time)?.43f:"Sunset".equals(c.time)?.8f:1;boolean sakura="Sakura".equals(c.scene),alpine="Alpine".equals(c.scene);
        top=sprite("grass_top");side=sprite("grass_side");wood=sprite("log_oak");leaves=sprite("leaves_oak");snow=sprite("snow");water=sprite("water_still");flower=sprite("flower_oxeye_daisy");
        GL11.glBegin(GL11.GL_QUADS);
        for(int x=-40;x<=40;x++)for(int z=-40;z<=40;z++){boolean river=Math.abs(z-Math.sin(x*.17)*2)<2.2;float y=river?-.5f:(float)(Math.floor((Math.sin(x*.22)+Math.cos(z*.18))*.7)*.5+.5);box(x,y-1,z,1,1,1,river?0x98957a:alpine?0xc4d6d5:0x689452);}
        for(int i=0;i<22;i++){float x=(float)Math.sin(i*14.1)*28,z=-3-(float)Math.abs(Math.cos(i*7.21))*25;if(Math.abs(z-Math.sin(x*.17)*2)<4)continue;
            for(int trunk=0;trunk<5;trunk++)box(x,trunk,z,1,1,1,0x705548);
            for(int yy=0;yy<(alpine?5:3);yy++){int r=alpine?Math.max(0,2-yy/2):yy==2?1:2;for(int xx=-r;xx<=r;xx++)for(int zz=-r;zz<=r;zz++){if(Math.abs(xx)==r&&Math.abs(zz)==r&&(yy%2==0))continue;box(x+xx,3+yy,z+zz,1,1,1,alpine&&yy==4?0xdfebeb:sakura?(yy==2?0xf1c4d9:0xd99cb9):0x60874e);}}
        }
        for(int i=0;i<260;i++){float x=(float)Math.sin(i*4.19)*32,z=(float)Math.cos(i*7.23)*30;if(Math.abs(z-Math.sin(x*.17)*2)<3)continue;float y=(float)(Math.floor((Math.sin(x*.22)+Math.cos(z*.18))*.7)*.5+.5);texture=flower;tint(i%3==0?0xf2c3f2:i%3==1?0xffe8a8:0xffffff,1);cross(x,y,z,.8f);}
        GL11.glEnd();
    }
    private static void tint(int rgb,float light){GL11.glColor4f((rgb>>16&255)/255f*light*shade,(rgb>>8&255)/255f*light*shade,(rgb&255)/255f*light*shade,1);}
    private static void vertex(double x,double y,double z,float u,float v){GL11.glTexCoord2f(u==0?texture.getMinU():texture.getMaxU(),v==0?texture.getMinV():texture.getMaxV());GL11.glVertex3d(x,y,z);}
    private static void box(float x,float y,float z,float a,float b,float c,int rgb){boolean grass=rgb==0x689452,leaf=rgb==0x60874e||rgb==0xd99cb9||rgb==0xf1c4d9,log=rgb==0x705548,ice=rgb==0xc4d6d5||rgb==0xdfebeb;
        texture=grass?top:log?wood:leaf?leaves:ice?snow:side;tint(grass?0x96bb72:leaf?rgb==0x60874e?0x8db971:rgb:0xffffff,1);quad(x,y+b,z,x+a,y+b,z+c);
        texture=grass?side:log?wood:leaf?leaves:ice?snow:side;tint(leaf?rgb==0x60874e?0x8db971:rgb:0xffffff,.7f);vertex(x,y,z,0,1);vertex(x+a,y,z,1,1);vertex(x+a,y+b,z,1,0);vertex(x,y+b,z,0,0);vertex(x,y,z+c,0,1);vertex(x,y+b,z+c,0,0);vertex(x+a,y+b,z+c,1,0);vertex(x+a,y,z+c,1,1);
        tint(leaf?rgb==0x60874e?0x8db971:rgb:0xffffff,.85f);vertex(x,y,z,0,1);vertex(x,y+b,z,0,0);vertex(x,y+b,z+c,1,0);vertex(x,y,z+c,1,1);vertex(x+a,y,z,0,1);vertex(x+a,y,z+c,1,1);vertex(x+a,y+b,z+c,1,0);vertex(x+a,y+b,z,0,0);
    }
    private static void cross(float x,float y,float z,float size){vertex(x,y,z,0,1);vertex(x+size,y,z+size,1,1);vertex(x+size,y+size,z+size,1,0);vertex(x,y+size,z,0,0);vertex(x+size,y,z,0,1);vertex(x,y,z+size,1,1);vertex(x,y+size,z+size,1,0);vertex(x+size,y+size,z,0,0);}
    private static void quad(double x,double y,double z,double x2,double y2,double z2){vertex(x,y,z,0,0);vertex(x2,y,z,1,0);vertex(x2,y2,z2,1,1);vertex(x,y2,z2,0,1);}
}
