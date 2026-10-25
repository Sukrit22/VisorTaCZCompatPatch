package dev.visorcompat.tacz.physical;
import java.util.*;
/** Server-confirmed visual clocks are keyed by inventory slot and gun identity. */
public final class DualShotFeedback {
    private record Shot(String key,long time,boolean silence){}
    private final Map<Integer,Shot> shots=new HashMap<>();
    public void fired(int slot,String key,long time,boolean silence){if(slot>=0&&slot<9)shots.put(slot,new Shot(key,time,silence));}
    public float age(int slot,String key,long now){var shot=shots.get(slot);return shot==null||!shot.key().equals(key)||now<shot.time()?Float.POSITIVE_INFINITY:(now-shot.time())/1_000_000_000f;}
    public float cycle(int slot,String key,long now){return ShotMotion.cycle(age(slot,key,now));}
    public boolean flash(int slot,String key,long now){var shot=shots.get(slot);return shot!=null&&!shot.silence()&&age(slot,key,now)<.05f;}
    public void clear(int slot){shots.remove(slot);}
    public void clear(){shots.clear();}
    public static String glockSound(int cue){return switch(cue){case 0->"glock_17/glock17_reload_magout";case 1->"glock_17/glock17_reload_magin";case 2,3,4->"glock_17/glock17_inspect_rackon";case 5,6->"glock_17/glock17_inspect_rackhit";default->"";};}
}
