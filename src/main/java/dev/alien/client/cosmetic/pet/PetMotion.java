package dev.alien.client.cosmetic.pet;

/** Tick-driven damped follower. Teleports snap both interpolation endpoints; nothing enters the world entity list. */
public final class PetMotion {
    private boolean initialized;private double x,y,z,px,py,pz,time,previousTime,idle;
    private float moving,previousMoving,sleeping,previousSleeping,dancing,previousDancing,yaw,previousYaw;
    public void update(PetDesign c,double dt,double ownerX,double ownerY,double ownerZ,float ownerYaw,double velocity,boolean emote,boolean animated){
        if(!Double.isFinite(dt)||dt<=0||!Double.isFinite(ownerX)||!Double.isFinite(ownerY)||!Double.isFinite(ownerZ)||!Float.isFinite(ownerYaw))return;
        ownerYaw=wrap(ownerYaw);dt=Math.min(.25,dt);previousTime=time;previousMoving=moving;previousSleeping=sleeping;previousDancing=dancing;previousYaw=yaw;px=x;py=y;pz=z;
        if(animated)time+=dt*c.speed;
        double angle=Math.toRadians(ownerYaw),side=c.right?1:-1,r=c.distance,back=.55,h=c.height;
        if("Shoulder".equals(c.mode)){r=.42;back=.02;h=1.6;}
        if("Orbit".equals(c.mode)){angle+=time*.65;back=0;}
        double tx=ownerX-Math.cos(angle)*r*side+Math.sin(angle)*back,ty=ownerY+h,tz=ownerZ-Math.sin(angle)*r*side-Math.cos(angle)*back;
        double difference=(x-tx)*(x-tx)+(y-ty)*(y-ty)+(z-tz)*(z-tz);boolean snap=!initialized||difference>144;
        double blend=animated?1-Math.exp(-dt*8):1;
        if(snap){x=px=tx;y=py=ty;z=pz=tz;yaw=previousYaw=ownerYaw;initialized=true;}
        else{x+=(tx-x)*blend;y+=(ty-y)*blend;z+=(tz-z)*blend;float delta=wrap(ownerYaw-yaw);yaw+=(float)(delta*(1-Math.exp(-dt*10)));}
        float speed=Double.isFinite(velocity)?(float)Math.max(0,Math.min(1,velocity/4)):0;
        moving+=(speed-moving)*(float)(1-Math.exp(-dt*9));
        boolean active=speed>.08||emote;idle=active?0:Math.min(30,idle+dt);
        float sleep=c.sleep&&idle>6?1:0,dance=c.reactEmotes&&emote?1:0;
        sleeping+=(sleep-sleeping)*(float)(1-Math.exp(-dt*(active?12:3)));dancing+=(dance-dancing)*(float)(1-Math.exp(-dt*10));
    }
    public double x(float f){return lerp(px,x,f);}public double y(float f){return lerp(py,y,f);}public double z(float f){return lerp(pz,z,f);}
    public float yaw(float f){return previousYaw+wrap(yaw-previousYaw)*unit(f);}
    public float movement(float f){return (float)lerp(previousMoving,moving,f);}public float sleeping(float f){return (float)lerp(previousSleeping,sleeping,f);}public float dancing(float f){return (float)lerp(previousDancing,dancing,f);}
    public double time(float f){return lerp(previousTime,time,f);}
    public void pose(float partial,boolean animated,PetPose pose){pose.sample(time(partial),movement(partial),sleeping(partial),dancing(partial),animated);}
    private static double lerp(double a,double b,float f){return a+(b-a)*unit(f);}private static float unit(float v){return Float.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
    private static float wrap(float v){v%=360;if(v>=180)v-=360;if(v< -180)v+=360;return v;}
}
