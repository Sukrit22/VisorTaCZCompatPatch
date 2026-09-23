package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import java.util.*;
import org.joml.*;
public final class AdvancedRemote {
    private record Entry(CompatNetwork.Advanced value,long time){}
    private static final Map<UUID,Entry> STATES=new HashMap<>();
    public static void receive(CompatNetwork.AdvancedRemote packet){if(packet.state().enabled()&&packet.state().valid())STATES.put(packet.player(),new Entry(packet.state(),System.nanoTime()));else STATES.remove(packet.player());}
    public static boolean freeMain(UUID id){var e=STATES.get(id);return e!=null&&(e.value().mount()==1||e.value().mount()==2)&&System.nanoTime()-e.time()<500_000_000L;}
    public static void clear(){STATES.clear();}
    public static GunPose pose(UUID id,String key,GunPose normal,WeaponProfile profile,Calibration c){
        var e=STATES.get(id);if(e==null||normal==null||!e.value().key().equals(key)||System.nanoTime()-e.time()>1_000_000_000L)return normal;
        var s=e.value();if(s.held())return normal;
        var hand=new Vector3f(s.x(),s.y(),s.z());var q=new Quaternionf(s.qx(),s.qy(),s.qz(),s.qw()).normalize();
        return new GunPose(hand,q,new Quaternionf(q).transform(c.muzzleOffset(profile.muzzleOffset()).mul(normal.worldScale())).add(hand),new Quaternionf(q).transform(new Vector3f(0,0,-1)),normal.worldScale());
    }
}
