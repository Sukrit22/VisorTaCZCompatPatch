package dev.visorcompat.tacz.server;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;
public final class ServerAdvanced {
    private record Entry(CompatNetwork.Advanced state,long time){}
    private static final Map<UUID,Entry> STATES=new HashMap<>();
    public static void receive(ServerPlayer p,CompatNetwork.Advanced s){
        if(!s.enabled()){clear(p);return;}
        if(!ServerPoses.isVr(p)||!p.isAlive()||p.isSpectator()||s.slot()!=p.getInventory().selected||!s.key().equals(Profiles.key(p.getMainHandItem()))||!s.valid())return;
        if(p.position().distanceTo(new net.minecraft.world.phys.Vec3(s.x(),s.y(),s.z()))>12)return;
        var old=STATES.put(p.getUUID(),new Entry(s,System.nanoTime()));
        if(!s.held()&&(old==null||old.state().held()))ServerPhysical.cancelGrip(p);
        CompatNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(()->p),new CompatNetwork.AdvancedRemote(p.getUUID(),s));
    }
    public static boolean active(ServerPlayer p){return STATES.containsKey(p.getUUID());}
    public static boolean canFire(ServerPlayer p){
        var e=STATES.get(p.getUUID());return e==null||e.state().held()&&e.state().slot()==p.getInventory().selected&&e.state().key().equals(Profiles.key(p.getMainHandItem()))&&System.nanoTime()-e.time()<500_000_000L;
    }
    private static boolean fresh(ServerPlayer p,Entry e){return e!=null&&e.state().slot()==p.getInventory().selected&&e.state().key().equals(Profiles.key(p.getMainHandItem()))&&System.nanoTime()-e.time()<500_000_000L;}
    public static boolean freeMain(ServerPlayer p){var e=STATES.get(p.getUUID());var profile=Profiles.get(p.getMainHandItem());return fresh(p,e)&&(e.state().mount()==1||e.state().mount()==2)&&profile!=null&&profile.supportDistance()==0&&!profile.manualAction();}
    public static boolean cylinderCarry(ServerPlayer p){var e=STATES.get(p.getUUID());return fresh(p,e)&&(e.state().mount()==3||e.state().mount()==4)&&Profiles.cylinder(p.getMainHandItem());}
    public static boolean cylinderOffhand(ServerPlayer p){var e=STATES.get(p.getUUID());return cylinderCarry(p)&&e.state().mount()==4;}
    public static boolean canInteract(ServerPlayer p){return canFire(p)||freeMain(p)||cylinderCarry(p);}
    public static GunPose mountedPose(ServerPlayer p,GunPose normal){
        if((!freeMain(p)&&!cylinderCarry(p))||normal==null)return normal;
        var s=STATES.get(p.getUUID()).state();var profile=Profiles.get(p.getMainHandItem());var c=CompatNetwork.calibration(p);
        var hand=new org.joml.Vector3f(s.x(),s.y(),s.z());var q=new org.joml.Quaternionf(s.qx(),s.qy(),s.qz(),s.qw()).normalize();
        return new GunPose(hand,q,new org.joml.Quaternionf(q).transform(c.muzzleOffset(profile.muzzleOffset()).mul(normal.worldScale())).add(hand),new org.joml.Quaternionf(q).transform(new org.joml.Vector3f(0,0,-1)),normal.worldScale());
    }
    public static void use(ServerPlayer p){if(STATES.containsKey(p.getUUID())&&canInteract(p)&&ServerPoses.validated(p)!=null){if(Profiles.cylinder(p.getMainHandItem()))ServerCylinder.armOpen(p);else ServerPhysical.advancedUse(p);}}
    public static void clear(ServerPlayer p){if(STATES.remove(p.getUUID())!=null)CompatNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(()->p),new CompatNetwork.AdvancedRemote(p.getUUID(),CompatNetwork.Advanced.disabled()));}
    public static void clearAll(){STATES.clear();}
}
