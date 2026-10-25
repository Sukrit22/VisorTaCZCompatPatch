package dev.visorcompat.tacz.server;

import com.tacz.guns.api.item.IGun;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.compat.GunDurabilityCompat;
import dev.visorcompat.tacz.network.*;
import dev.visorcompat.tacz.physical.*;
import dev.visorcompat.tacz.physical.Handling.Phase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import java.util.*;

/** Per-slot, exact-stack Physical Glock sessions. Stowing preserves the gun's state. */
public final class DualPhysicalServer {
    private static final String RESERVE="visor_tacz_dual_reserved_magazine";
    private static final Map<UUID,Map<Integer,Session>> PLAYERS=new HashMap<>();
    private static final class Session {
        final ItemStack stack;final int slot;final String key;final DualPhysicalMagazine magazine;
        Phase phase=Phase.READY,from=Phase.READY;boolean down,gripped,pulled,shot;int pull,cue=-1,ticks;
        final Vector3f start=new Vector3f();Vector3f previous;
        Session(int slot,ItemStack stack){this.stack=stack;this.slot=slot;key=Profiles.key(stack);magazine=new DualPhysicalMagazine(stack.hasTag()&&stack.getTag().contains(RESERVE)?stack.getTag().getInt(RESERVE):-1);if(magazine.removed())phase=Phase.NO_MAG;else if(!IGun.getIGunOrNull(stack).hasBulletInBarrel(stack))phase=Phase.NEED_RACK;}
    }
    private record Sample(Vector3f local,boolean pouch){}
    public static boolean supported(ServerPlayer p,ItemStack stack){var gun=IGun.getIGunOrNull(stack);return ServerDual.physicalRequested(p)&&CompatNetwork.physical(p)&&Profiles.physical(stack)&&gun!=null&&DualPhysicalMagazine.supported(gun.getGunId(stack).toString());}
    private static Session session(ServerPlayer p,int hand){var map=PLAYERS.get(p.getUUID());if(map==null)return null;var s=map.get(ServerDual.slot(p,hand));return s!=null&&s.stack==ServerDual.stack(p,hand)&&s.key.equals(Profiles.key(s.stack))?s:null;}
    public static boolean canFire(ServerPlayer p,int hand){var stack=ServerDual.stack(p,hand);if(!supported(p,stack))return true;var s=session(p,hand);var gun=IGun.getIGunOrNull(stack);return s!=null&&DualPhysicalMagazine.canFire(s.gripped,ActionState.locked(stack),GunDurabilityCompat.jammed(stack),new Chamber(gun.getCurrentAmmoCount(stack),gun.hasBulletInBarrel(stack)));}
    private static boolean free(ServerPlayer p,int hand){var other=ServerDual.stack(p,1-hand);return DualPhysicalMagazine.free(ServerDual.held(p,hand),ServerDual.held(p,1-hand),(p.getInventory().offhand.get(0).isEmpty()||Profiles.get(p.getInventory().offhand.get(0))!=null)&&(other.isEmpty()||Profiles.get(other)!=null));}
    private static Sample sample(ServerPlayer p,int hand){
        if(!free(p,hand)||!(VisorAPI.getVRPlayer(p) instanceof VRServerPlayer vr))return null;
        var gun=ServerDual.validated(p,hand);if(gun==null)return null;
        var pose=vr.getPoseData();var at=new Vector3f(hand==0?pose.getOffhand().getPosition():pose.getMainHand().getPosition());
        if(!PoseMath.finite(at)||at.distance(pose.getHmd().getPosition())>1.6f*gun.worldScale()||ServerPoses.blocked(p,pose.getHmd().getPositionVec3(),new net.minecraft.world.phys.Vec3(at)))return null;
        var forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
        return new Sample(Handling.local(gun,at),Handling.inPouch(at,pose.getHmd().getPosition(),forward,gun.worldScale(),ServerDual.calibration(p,hand)));
    }
    private static Vector3f point(int hand,Vector3f point){/* TaCZ geometry stays authored in both hands. */return point;}
    private static Handling.Target target(ServerPlayer p,int hand,Session s,Sample pose){
        var c=ServerDual.calibration(p,hand);var profile=Profiles.get(s.stack);
        // Dedicated Glock milestone has no support/selector ownership transitions.
        if(s.phase==Phase.NO_MAG&&pose.pouch())return Handling.Target.POUCH;
        if(s.phase!=Phase.READY&&s.phase!=Phase.NEED_RACK&&s.phase!=Phase.NO_MAG)return Handling.Target.NONE;
        if(ActionState.locked(s.stack)&&Handling.inside(pose.local(),point(hand,Handling.release(profile,c)),c,ZoneSizes.Zone.RELEASE))return Handling.Target.RELEASE;
        if(Handling.inside(pose.local(),point(hand,Handling.rack(profile,c)),c,ZoneSizes.Zone.RACK))return Handling.Target.RACK;
        return !s.magazine.removed()&&Handling.inside(pose.local(),point(hand,Handling.magazine(profile,c)),c,ZoneSizes.Zone.MAGAZINE)?Handling.Target.MAGAZINE:Handling.Target.NONE;
    }
    public static void tick(ServerPlayer p){
        if(!ServerDual.physicalRequested(p)||!CompatNetwork.physical(p)){clear(p);return;}
        var map=PLAYERS.computeIfAbsent(p.getUUID(),id->new HashMap<>());
        for(var it=map.values().iterator();it.hasNext();){var s=it.next();if(p.getInventory().getItem(s.slot)!=s.stack||!s.key.equals(Profiles.key(s.stack))){restore(s);it.remove();}}
        for(var s:map.values()){boolean held=false;for(int h=0;h<2;h++)held|=ServerDual.slot(p,h)==s.slot&&ServerDual.stack(p,h)==s.stack&&ServerDual.held(p,h);if(!held){cancel(s);s.down=true;s.previous=null;}}
        for(int hand=0;hand<2;hand++){
            var stack=ServerDual.stack(p,hand);if(!supported(p,stack))continue;
            int slot=ServerDual.slot(p,hand);var s=map.get(slot);if(s==null){ServerDual.operator(p,hand).beginPhysicalHandling();s=new Session(slot,stack);map.put(slot,s);}var gun=IGun.getIGunOrNull(stack);
            if(s.shot){if(!s.magazine.removed()&&!gun.hasBulletInBarrel(stack)&&gun.getCurrentAmmoCount(stack)==0){ActionState.locked(stack,true);sync(p,s);}s.shot=false;}
            var pose=sample(p,hand);boolean down=ServerDual.grip(p,1-hand);
            if(pose==null||ServerDual.gripCanceled(p,1-hand)){cancel(s);s.down=down;s.previous=null;send(p,s);continue;}
            if(down&&!s.down){s.down=true;var target=target(p,hand,s,pose);s.start.set(pose.local());s.pulled=false;
                if(target==Handling.Target.MAGAZINE){s.gripped=true;s.phase=Phase.REMOVING;}
                else if(target==Handling.Target.POUCH){s.gripped=true;s.phase=Phase.NEW_MAG;}
                else if(target==Handling.Target.RACK){s.gripped=true;s.from=s.phase;s.phase=s.magazine.removed()?Phase.RACKING_EMPTY:Phase.RACKING;}
                else if(target==Handling.Target.RELEASE)releaseSlide(p,hand,s);
            }
            if(s.gripped){
                if(s.phase==Phase.REMOVING&&Handling.magazinePulled(s.start,pose.local())){var next=s.magazine.remove(new Chamber(gun.getCurrentAmmoCount(stack),gun.hasBulletInBarrel(stack)));gun.setCurrentAmmoCount(stack,next.magazine());stack.getOrCreateTag().putInt(RESERVE,s.magazine.reserved());s.phase=Phase.OLD_MAG;s.cue=0;sync(p,s);}
                else if(Handling.racking(s.phase)){var move=ActionCycle.rack(s.pulled,pose.local().z-s.start.z,pose.local().x-s.start.x,pose.local().y-s.start.y);if(move==PumpCycle.Transition.OPEN){s.pulled=true;boolean live=gun.hasBulletInBarrel(stack);var jam=ServerJams.read(stack);if(jam.kind()!=Jam.Kind.NONE){var after=jam.pull(s.magazine.removed());stack.getOrCreateTag().putString(ServerJams.TYPE,after.kind().name());stack.getTag().putInt(ServerJams.LEFT,after.remaining());}gun.setBulletInBarrel(stack,false);s.cue=live?3:2;sync(p,s);}else if(move==PumpCycle.Transition.CLOSE)closeSlide(p,hand,s);}
            }
            if(!down&&s.down){s.down=false;if(s.gripped){s.gripped=false;
                if(s.phase==Phase.REMOVING)s.phase=ready(s)?Phase.READY:Phase.NEED_RACK;
                else if(s.phase==Phase.OLD_MAG)s.phase=Phase.NO_MAG;
                else if(s.phase==Phase.NEW_MAG){var c=ServerDual.calibration(p,hand);if(Handling.inside(pose.local(),point(hand,Handling.magazine(Profiles.get(stack),c)),c,ZoneSizes.Zone.MAGAZINE)&&insert(p,hand,s)){s.phase=ready(s)?Phase.READY:Phase.NEED_RACK;s.cue=1;}else s.phase=Phase.NO_MAG;sync(p,s);}
                else if(Handling.racking(s.phase)){if(s.pulled)closeSlide(p,hand,s);s.phase=s.magazine.removed()?Phase.NO_MAG:ready(s)?Phase.READY:Phase.NEED_RACK;}
            }}
            if(!s.gripped&&s.previous!=null&&ActionState.locked(stack)){var c=ServerDual.calibration(p,hand);if(ActionCycle.releaseSweep(s.previous,pose.local(),point(hand,Handling.release(Profiles.get(stack),c)),c.zones().release()))releaseSlide(p,hand,s);}
            if(!s.gripped&&(s.phase==Phase.READY||s.phase==Phase.NEED_RACK))s.phase=ready(s)?Phase.READY:Phase.NEED_RACK;
            s.pull=s.gripped?Math.min(14,Math.max(0,Math.round((s.phase==Phase.REMOVING?s.start.y-pose.local().y:Handling.racking(s.phase)?pose.local().z-s.start.z:0)*200))):0;
            s.previous=s.gripped?null:new Vector3f(pose.local());send(p,s);
        }
    }
    private static boolean insert(ServerPlayer p,int hand,Session session){return ServerDual.operator(p,hand).loadPhysicalMagazine(()->restore(session));}
    private static boolean ready(Session s){return IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack)&&!ActionState.locked(s.stack)&&!GunDurabilityCompat.jammed(s.stack);}
    private static void closeSlide(ServerPlayer p,int hand,Session s){if(!s.pulled)return;if(GunDurabilityCompat.jammed(s.stack)&&ServerJams.read(s.stack).remaining()==0&&GunDurabilityCompat.clearBound(p,s.stack))ServerJams.reconcile(p,s.stack);var gun=IGun.getIGunOrNull(s.stack);var next=new Chamber(gun.getCurrentAmmoCount(s.stack),gun.hasBulletInBarrel(s.stack));if(!s.magazine.removed()&&!GunDurabilityCompat.jammed(s.stack))next=next.feed();gun.setCurrentAmmoCount(s.stack,next.magazine());gun.setBulletInBarrel(s.stack,next.loaded());ActionState.locked(s.stack,false);s.pulled=false;s.cue=5;sync(p,s);}
    public static void use(ServerPlayer p,int hand){var s=session(p,hand);if(s!=null&&ServerDual.validated(p,hand)!=null)releaseSlide(p,hand,s);}
    private static void releaseSlide(ServerPlayer p,int hand,Session s){if(s.gripped||!ActionState.locked(s.stack)||GunDurabilityCompat.jammed(s.stack))return;var gun=IGun.getIGunOrNull(s.stack);var next=ActionCycle.release(new Chamber(gun.getCurrentAmmoCount(s.stack),gun.hasBulletInBarrel(s.stack)),true,!s.magazine.removed(),false);gun.setCurrentAmmoCount(s.stack,next.magazine());gun.setBulletInBarrel(s.stack,next.loaded());ActionState.locked(s.stack,false);if(!s.magazine.removed())s.phase=ready(s)?Phase.READY:Phase.NEED_RACK;s.cue=5;sync(p,s);}
    public static void shot(ServerPlayer p,int hand){var s=session(p,hand);if(s!=null)s.shot=true;}
    private static void cancel(Session s){s.gripped=false;s.pulled=false;s.pull=0;if(s.phase==Phase.REMOVING)s.phase=Phase.READY;else if(s.phase==Phase.OLD_MAG||s.phase==Phase.NEW_MAG)s.phase=Phase.NO_MAG;else if(Handling.racking(s.phase))s.phase=s.magazine.removed()?Phase.NO_MAG:Phase.NEED_RACK;}
    private static void restore(Session s){var gun=IGun.getIGunOrNull(s.stack);if(gun!=null){var c=s.magazine.restore(new Chamber(gun.getCurrentAmmoCount(s.stack),gun.hasBulletInBarrel(s.stack)));gun.setCurrentAmmoCount(s.stack,c.magazine());}if(s.stack.hasTag())s.stack.getTag().remove(RESERVE);}
    public static void recoverInventory(ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(stack.hasTag()&&stack.getTag().contains(RESERVE)&&IGun.getIGunOrNull(stack)!=null)restore(new Session(i,stack));}}
    public static void clear(ServerPlayer p){var map=PLAYERS.remove(p.getUUID());if(map!=null)for(var s:map.values())restore(s);boolean saved=false;for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);saved|=stack.hasTag()&&stack.getTag().contains(RESERVE);}if(saved)recoverInventory(p);if(map!=null||saved)p.inventoryMenu.broadcastChanges();}
    public static void clearAll(){for(var map:PLAYERS.values())for(var s:map.values())restore(s);PLAYERS.clear();}
    private static void sync(ServerPlayer p,Session s){p.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(-2,0,s.slot,s.stack.copy()));p.inventoryMenu.broadcastChanges();if(s.cue>=0)send(p,s);}
    private static void send(ServerPlayer p,Session s){DualNetwork.physical(p,new DualNetwork.Physical(s.slot,s.key,s.phase.ordinal(),s.pull,s.cue));s.cue=-1;s.ticks++;}
}
