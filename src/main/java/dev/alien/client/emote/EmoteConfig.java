package dev.alien.client.emote;

/** Profile preferences only; active playback never persists across world/profile changes. */
public final class EmoteConfig {
    public boolean articulated=true;
    public boolean enabled=true,loop=true,autoThirdPerson=true,hideHeldItem=true;
    public int wheelKey=48;
    public float speed=1;
    public String selected="floss";
    public String[] slots={"floss","orange_justice","take_the_l","best_mates","dab","jubilation","wave","robot"};
    public void normalize(){
        if(wheelKey<0||wheelKey>255)wheelKey=48;
        speed=Float.isFinite(speed)?Math.max(.5f,Math.min(1.5f,speed)):1;
        if(!EmoteLibrary.valid(selected))selected="floss";
        String[] defaults=new EmoteConfig().slots,checked=new String[8];
        for(int i=0;i<8;i++)checked[i]=slots!=null&&i<slots.length&&EmoteLibrary.valid(slots[i])?slots[i]:defaults[i];
        slots=checked;
    }
}
