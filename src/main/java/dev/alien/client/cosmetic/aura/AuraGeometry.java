package dev.alien.client.cosmetic.aura;

/** Original feathered ring bands and crystal/rune triangles, with a hard shared unit budget. */
public final class AuraGeometry {
    public interface Sink {void vertex(double x,double y,double z,int color,float alpha);}
    private AuraGeometry(){}
    public static int emit(AuraDesign d,AuraResponse state,float partial,int budget,boolean glow,Sink out){
        if(budget<=0)return 0;
        double phase=state.phase(partial)*Math.PI,motion=state.movement(partial),dance=state.dance(partial),night=state.night(partial);
        float energy=state.energy(d,partial),alpha=d.opacity*Math.min(1,(float)Math.sqrt(energy))* (glow?.28f:.95f);
        if(alpha<.001f)return 0;
        double pulse=.82*Math.sin(phase*1.4)+.18*Math.sin(phase*2.8-.45),radius=d.radius*(1+.14*motion+.1*night+.10*dance*pulse),width=(.026+.022*energy)*(glow?3:1);
        int primary=mix(d.color,d.secondaryColor,(float)(.25*motion+.4*night)),used=0;
        if("Orbit".equals(d.style)){
            for(int plane=0;plane<3&&budget-used>=12;plane++)used+=ring(out,Math.min(32,(budget-used)/(3-plane)),radius*.78,.85,phase+plane*2.1,plane+1,width,primary,d.secondaryColor,alpha);
        }else{
            int segments=Math.min(48,Math.max(12,(budget-8)/2));
            if(budget>=12)used+=ring(out,Math.min(segments,budget),radius,.09,phase,0,width,primary,d.secondaryColor,alpha);
            if(budget-used>=12)used+=ring(out,Math.min(segments,budget-used),radius*("Runes".equals(d.style)?.78:.60),"Runes".equals(d.style)?.1:.65, -phase*.7,0,width*.65,d.secondaryColor,primary,alpha*.65f);
        }
        int motes=Math.min(8,budget-used);
        for(int i=0;i<motes;i++){
            double a=phase*.5+i*Math.PI/4,r=radius*("Runes".equals(d.style)?1.1:.94),y="Runes".equals(d.style)?.12:.65+Math.sin(a*1.6+i*.17)*.28;
            if("Runes".equals(d.style))glyph(out,Math.cos(a)*r,y,Math.sin(a)*r,a,(glow?.018:.008)*(1+energy*.35),d.secondaryColor,alpha);
            else crystal(out,Math.cos(a)*r,y,Math.sin(a)*r,(glow?.065:.035)*(1+energy*.35),d.secondaryColor,alpha);used++;
        }
        return used;
    }
    private static int ring(Sink out,int n,double r,double y,double phase,int plane,double width,int a,int b,float alpha){
        for(int i=0;i<n;i++){
            double theta=i*Math.PI*2/n,theta2=(i+1)*Math.PI*2/n;
            float brightness=(float)(.65+.35*Math.sin(theta*3-phase*2));int color=mix(a,b,(float)(.5+.5*Math.sin(theta-phase)));
            for(int side=-1;side<=1;side+=2){
                point(out,theta,r,y,phase,plane,color,alpha*brightness);point(out,theta2,r,y,phase,plane,color,alpha*brightness);point(out,theta2,r+width*side,y,phase,plane,color,0);
                point(out,theta,r,y,phase,plane,color,alpha*brightness);point(out,theta2,r+width*side,y,phase,plane,color,0);point(out,theta,r+width*side,y,phase,plane,color,0);
            }
        }return n;
    }
    private static void point(Sink out,double a,double r,double y,double phase,int plane,int color,float alpha){
        double x=Math.cos(a)*r,z=Math.sin(a)*r;
        if(plane>0){double tilt=.65+(plane-1)*.6,yy=z*Math.sin(tilt);z*=Math.cos(tilt);double turn=phase*.25+plane*Math.PI/3;double xx=x*Math.cos(turn)-z*Math.sin(turn);z=x*Math.sin(turn)+z*Math.cos(turn);x=xx;y+=yy;}
        out.vertex(x,y,z,color,alpha);
    }
    private static void glyph(Sink out,double x,double y,double z,double angle,double width,int color,float alpha){
        stroke(out,x,y,z,angle,0,-.10,0,.10,width,color,alpha);stroke(out,x,y,z,angle,0,-.10,.08,-.02,width,color,alpha);
        stroke(out,x,y,z,angle,.08,-.02,0,.025,width,color,alpha);stroke(out,x,y,z,angle,0,.025,.08,.10,width,color,alpha);
    }
    private static void stroke(Sink out,double x,double y,double z,double angle,double ax,double az,double bx,double bz,double w,int color,float alpha){
        double dx=bx-ax,dz=bz-az,length=Math.hypot(dx,dz),nx=-dz/length*w,nz=dx/length*w;
        runePoint(out,x,y,z,angle,ax+nx,az+nz,color,alpha);runePoint(out,x,y,z,angle,bx+nx,bz+nz,color,alpha);runePoint(out,x,y,z,angle,bx-nx,bz-nz,color,alpha);
        runePoint(out,x,y,z,angle,ax+nx,az+nz,color,alpha);runePoint(out,x,y,z,angle,bx-nx,bz-nz,color,alpha);runePoint(out,x,y,z,angle,ax-nx,az-nz,color,alpha);
    }
    private static void runePoint(Sink out,double x,double y,double z,double angle,double u,double v,int color,float alpha){out.vertex(x+u*Math.cos(angle)-v*Math.sin(angle),y,z+u*Math.sin(angle)+v*Math.cos(angle),color,alpha);}
    private static void crystal(Sink out,double x,double y,double z,double size,int color,float alpha){
        for(int i=0;i<4;i++){double a=i*Math.PI/2,b=(i+1)*Math.PI/2;
            out.vertex(x,y+size*2,z,color,alpha);out.vertex(x+Math.cos(a)*size,y,z+Math.sin(a)*size,color,alpha*.5f);out.vertex(x+Math.cos(b)*size,y,z+Math.sin(b)*size,color,alpha*.5f);
            out.vertex(x,y-size*2,z,color,alpha);out.vertex(x+Math.cos(b)*size,y,z+Math.sin(b)*size,color,alpha*.5f);out.vertex(x+Math.cos(a)*size,y,z+Math.sin(a)*size,color,alpha*.5f);
        }
    }
    private static int mix(int a,int b,float f){return ((int)((a>>16&255)*(1-f)+(b>>16&255)*f)<<16)|((int)((a>>8&255)*(1-f)+(b>>8&255)*f)<<8)|(int)((a&255)*(1-f)+(b&255)*f);}
}
