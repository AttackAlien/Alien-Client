package dev.alien.client.cosmetic;

/** Optional local third-person appearance. It never changes the actual entity or hitbox. */
public final class CharacterDesign {
    public static final String[] MODELS={"Vanilla","Alien","Tung Tung Sahur","Kenny"};
    public String model="Vanilla";
    public boolean animate=true,emotes=true;
    public float size=1;
    public void normalize(){boolean found=false;for(String name:MODELS)found|=name.equals(model);if(!found)model="Vanilla";size=Float.isFinite(size)?Math.max(.75f,Math.min(1.25f,size)):1;}
    public boolean custom(){return !"Vanilla".equals(model);}
    public CharacterDesign copy(){CharacterDesign d=new CharacterDesign();d.model=model;d.animate=animate;d.emotes=emotes;d.size=size;d.normalize();return d;}
}
