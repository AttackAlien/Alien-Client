package dev.alien.client.effects.ghost;

import java.util.*;

/** Deduplicates confirmed deaths only. Attack attempts are deliberately not a confirmation source. */
public final class GhostLedger {
    private final LinkedHashMap<UUID,Long> accepted=new LinkedHashMap<UUID,Long>();
    public boolean accept(UUID id,long tick){accepted.entrySet().removeIf(e->tick-e.getValue()>200);if(id==null||accepted.containsKey(id))return false;while(accepted.size()>=128)accepted.remove(accepted.keySet().iterator().next());accepted.put(id,tick);return true;}
    public void clear(){accepted.clear();}
    /** 1.8.9 SpawnMob has an ID and type but no UUID. SpawnPlayer carries the player UUID. */
    public static boolean trackedIdentity(boolean player,UUID server,UUID client,Class<?> expected,Class<?> actual){return player?Objects.equals(server,client):expected==actual;}
    public static boolean localKill(UUID local,UUID killer,UUID victim){return local!=null&&local.equals(killer)&&victim!=null&&!local.equals(victim);}
    public static boolean deathTranslation(String key,String killer,String localName,String localDisplay){
        if(key==null||!key.startsWith("death.attack.")||key.length()>100)return false;
        // Only vanilla death translations, never player chat, disconnect, attack or scoreboard text.
        String type=key.substring(13);if(!Arrays.asList("player","player.item","arrow","arrow.item","mob","mob.item","fireball","fireball.item","thrown","thrown.item","explosion.player","fall.player","onFire.player","inFire.player","lava.player","drown.player","magic.player","wither.player","generic.player").contains(type))return false;
        String value=clean(killer);return !value.isEmpty()&&(value.equals(clean(localName))||value.equals(clean(localDisplay)));
    }
    public static String clean(String value){return value==null?"":value.replaceAll("\u00a7[0-9A-FK-ORa-fk-or]","").trim();}
}
