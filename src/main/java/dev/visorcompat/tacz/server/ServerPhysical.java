package dev.visorcompat.tacz.server;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.physical.Handling;
import dev.visorcompat.tacz.physical.Chamber;
import dev.visorcompat.tacz.physical.ActionState;
import dev.visorcompat.tacz.physical.ActionCycle;
import dev.visorcompat.tacz.physical.BoltState;
import dev.visorcompat.tacz.physical.Jam;
import dev.visorcompat.tacz.physical.ManualCycle;
import dev.visorcompat.tacz.physical.JamProfile;
import dev.visorcompat.tacz.compat.GunDurabilityCompat;
import dev.visorcompat.tacz.physical.Handling.Phase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import java.util.*;

public final class ServerPhysical {
    private static final String RESERVE="visor_tacz_reserved_magazine";
    private static final String EMPTY_RELOAD="visor_tacz_empty_reload";
    private static final Map<UUID, Session> SESSIONS=new HashMap<>();
    private static final class Session {
        HandAnchor anchor,supportAnchor,mainAnchor; boolean mainInput; GunPose previousGun,lastGun;
        ItemStack stack; int slot; Phase phase=Phase.READY; Phase rackFrom=Phase.READY;
        boolean held, pulled, allowReload, jammedAtRack, shot; Vector3f previous; Vector3f start=new Vector3f();
        int ticks, reloadStart, lastControl=-20; Phase sent; int sentPull=-1;
        Session(ServerPlayer p) {
            stack=p.getMainHandItem();slot=p.getInventory().selected;
            IGun gun=IGun.getIGunOrNull(stack);
            if(gun!=null && !gun.hasBulletInBarrel(stack) && gun.getCurrentAmmoCount(stack)>0)phase=Phase.NEED_RACK;
        }
    }
    record Sample(Vector3f local, boolean pouch) {}
    public static boolean enabled(ServerPlayer p) { return CompatNetwork.physical(p) && ServerPoses.isVr(p) && Profiles.physical(p.getMainHandItem()); }
    public static Phase phase(ServerPlayer p) {if(Profiles.cylinder(p.getMainHandItem()))return ServerCylinder.phase(p);if(Profiles.pump(p.getMainHandItem()))return ServerPump.phase(p);Session s=SESSIONS.get(p.getUUID());return s==null?Phase.READY:s.phase;}
    public static int pull(ServerPlayer p) {if(Profiles.cylinder(p.getMainHandItem()))return ServerCylinder.open(p.getMainHandItem())?16:0;if(Profiles.pump(p.getMainHandItem()))return ServerPump.pull(p);Session s=SESSIONS.get(p.getUUID());return s==null?0:Math.max(0,s.sentPull);}
    public static HandAnchor anchor(ServerPlayer p){if(Profiles.pump(p.getMainHandItem()))return ServerPump.anchor(p);var s=SESSIONS.get(p.getUUID());return s!=null&&s.stack==p.getMainHandItem()?(s.anchor!=null?s.anchor:s.mainAnchor):null;}
    public static boolean supporting(ServerPlayer p) {
        if(Profiles.pump(p.getMainHandItem()))return ServerPump.supporting(p);
        Session s=SESSIONS.get(p.getUUID());return s!=null && s.stack==p.getMainHandItem() && s.phase==Phase.SUPPORT;
    }
    public static boolean canFire(ServerPlayer p) {
        if(!enabled(p)) return true;
        if(Profiles.cylinder(p.getMainHandItem()))return ServerCylinder.canFire(p);
        if(Profiles.pump(p.getMainHandItem()))return ServerPump.canFire(p);
        Session s=SESSIONS.get(p.getUUID());return s!=null && s.stack==p.getMainHandItem() && Handling.fireable(s.phase)
            && s.anchor==null && !(Profiles.bolt(s.stack) && dev.visorcompat.tacz.physical.BoltState.blocked(s.stack))
            && !ManualCycle.spent(s.stack) && !ActionState.locked(s.stack) && !GunDurabilityCompat.jammed(s.stack) && IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack);
    }
    public static void shot(ServerPlayer p){var s=SESSIONS.get(p.getUUID());if(s!=null&&s.stack==p.getMainHandItem()){s.shot=true;if(ManualCycle.enabled(s.stack))s.stack.getOrCreateTag().putBoolean(ManualCycle.SPENT,true);}}
    public static boolean allowsReload(ServerPlayer p) {
        if(enabled(p)&&Profiles.cylinder(p.getMainHandItem()))return ServerCylinder.allowsReload(p);
        Session s=SESSIONS.get(p.getUUID());return !enabled(p) || (s!=null && s.allowReload);
    }
    private static void restore(ItemStack stack) {
        IGun gun=IGun.getIGunOrNull(stack);
        if(gun!=null && stack.hasTag() && stack.getTag().contains(RESERVE)) {
            // Magazine rounds were removed from the gun exactly once; restore exactly once.
            gun.setCurrentAmmoCount(stack,Math.max(0,stack.getTag().getInt(RESERVE)));
            stack.getTag().remove(RESERVE);
        }
        finishSavedReload(stack);
    }
    private static void finishSavedReload(ItemStack stack) {
        IGun gun=IGun.getIGunOrNull(stack);
        if(gun==null || !stack.hasTag() || !stack.getTag().getBoolean(EMPTY_RELOAD))return;
        // Native empty reload may already have committed ammo before disconnect.
        // Keep that ammo, but still require the physical charging stroke.
        Chamber chamber=new Chamber(gun.getCurrentAmmoCount(stack),gun.hasBulletInBarrel(stack)).unchamber();
        gun.setBulletInBarrel(stack,chamber.loaded());
        gun.setCurrentAmmoCount(stack,chamber.magazine());
        stack.getTag().remove(EMPTY_RELOAD);
    }
    /** Recover saved reservations even if the gun was not selected at login. */
    public static void recoverInventory(ServerPlayer p) {
        for(int i=0;i<p.getInventory().getContainerSize();i++)restore(p.getInventory().getItem(i));
    }
    public static void clear(ServerPlayer p) {
        ServerPump.clear(p);ServerCylinder.clear(p);
        Session s=SESSIONS.remove(p.getUUID());
        if(s!=null) {
            if(s.phase==Phase.LOADING) IGunOperator.fromLivingEntity(p).cancelReload();
            restore(s.stack);
            send(p,s,Phase.READY,0);
        }
    }
    public static void stop() { SESSIONS.clear();ServerPump.stop();ServerCylinder.stop(); }
    static Sample sample(ServerPlayer p) {
        GunPose gun=ServerPoses.validated(p);
        if(gun==null || !(VisorAPI.getVRPlayer(p) instanceof VRServerPlayer vr)) return null;
        var pose=vr.getPoseData();
        var session=SESSIONS.get(p.getUUID());
        Vector3f off=new Vector3f(ServerAdvanced.freeMain(p)||anchor(p)!=null&&!anchor(p).mainHand()&&!Profiles.pump(p.getMainHandItem())?pose.getMainHand().getPosition():pose.getOffhand().getPosition());
        if(!PoseMath.finite(off) || off.distance(pose.getHmd().getPosition())>1.6f*gun.worldScale()) return null;
        Vector3f forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
        Vector3f pouch=Handling.pouch(pose.getHmd().getPosition(),forward,gun.worldScale(),CompatNetwork.calibration(p));
        return new Sample(Handling.local(gun,off),Handling.inPouch(off,pose.getHmd().getPosition(),forward,gun.worldScale(),CompatNetwork.calibration(p)));
    }
    public static void tick(ServerPlayer p) {
        ServerPump.cleanup(p);ServerCylinder.cleanup(p);
        Session s=SESSIONS.get(p.getUUID());
        if(s!=null && (!enabled(p) || !p.isAlive() || p.isSpectator() || s.stack!=p.getMainHandItem() || s.slot!=p.getInventory().selected)) {
            clear(p);s=null;
        }
        if(!enabled(p) || !p.isAlive() || p.isSpectator()) { restore(p.getMainHandItem());return; }
        if(Profiles.cylinder(p.getMainHandItem())){ServerCylinder.tick(p);return;}
        if(Profiles.pump(p.getMainHandItem())){ServerPump.tick(p);return;}
        if(s==null) {
            restore(p.getMainHandItem());
            s=new Session(p);SESSIONS.put(p.getUUID(),s);
        }
        if(!s.held && s.phase!=Phase.LOADING && !Handling.magazineOut(s.phase))ServerJams.observe(p);
        if(!s.held && (s.phase==Phase.READY || s.phase==Phase.NEED_RACK))
            s.phase=!ManualCycle.spent(s.stack) && IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack) && s.anchor==null && !(Profiles.bolt(s.stack) && dev.visorcompat.tacz.physical.BoltState.blocked(s.stack))?Phase.READY:Phase.NEED_RACK;
        if(s.shot){
            var profile=Profiles.get(s.stack);var gun=IGun.getIGunOrNull(s.stack);
            if(!profile.manualAction()&&!profile.smg()&&!gun.hasBulletInBarrel(s.stack)&&gun.getCurrentAmmoCount(s.stack)==0)ActionState.locked(s.stack,true);
            s.shot=false;
        }
        if(Profiles.bolt(s.stack)&&BoltState.open(s.stack))ActionState.lifted(s.stack,true);
        s.previousGun=s.lastGun;s.lastGun=ServerPoses.validated(p);
        s.ticks++;
        if(!ServerAdvanced.active(p) && Profiles.bolt(s.stack) && s.phase==Phase.SUPPORT && s.held && s.supportAnchor!=null && s.anchor==null && VisorAPI.getVRPlayer(p) instanceof VRServerPlayer vr){
            var projected=s.supportAnchor.resolve(vr.getPoseData(),Profiles.get(s.stack),CompatNetwork.calibration(p));
            var main=vr.getPoseData().getMainHand();
            var grip=CompatNetwork.calibration(p).position(new Vector3f(main.getPosition()),main.getRotation().getNormalizedRotation(new org.joml.Quaternionf()),vr.getPoseData().getWorldScale());
            if(!mayTransfer(p,s)){
                var current=ServerPoses.validated(p);
                if(current!=null)s.supportAnchor=HandAnchor.capture(current,vr.getPoseData());
            }else if(projected!=null && grip.distance(projected.hand())>.16f*projected.worldScale()){
                s.anchor=s.supportAnchor;s.held=false;s.phase=Phase.NEED_RACK;
            }
        }
        Sample pose=sample(p);
        if(pose==null || !p.getOffhandItem().isEmpty()){if(s.held)release(p,s,null);s.anchor=null;s.mainAnchor=null;if(s.phase==Phase.SUPPORT)s.phase=Phase.READY;}
        if(pose!=null && !s.held && Profiles.get(s.stack).supportDistance()==0
            && (s.phase==Phase.READY||s.phase==Phase.NEED_RACK||s.phase==Phase.SUPPORT)){
            var center=Handling.support(Profiles.get(s.stack),CompatNetwork.calibration(p));
            boolean touch=p.getOffhandItem().isEmpty()&&(s.phase==Phase.SUPPORT?Handling.supportRetained(pose.local(),center,CompatNetwork.calibration(p)):Handling.inside(pose.local(),center,CompatNetwork.calibration(p),ZoneSizes.Zone.SUPPORT));
            s.phase=touch?Phase.SUPPORT:IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;
        }
        if(pose!=null && s.held) {
            if(s.phase==Phase.REMOVING && Handling.magazinePulled(s.start,pose.local())) {
                IGun gun=IGun.getIGunOrNull(s.stack);
                s.stack.getOrCreateTag().putInt(RESERVE,gun.getCurrentAmmoCount(s.stack));
                gun.setCurrentAmmoCount(s.stack,0);
                s.phase=Phase.OLD_MAG;HandlingFeedback.emit(p,0,ServerPoses.validated(p));
            } else if(Handling.racking(s.phase) && Profiles.bolt(s.stack)) moveBolt(p,s,pose);
            else if(Handling.racking(s.phase))moveRack(p,s,pose);
            else if(s.phase==Phase.SUPPORT && !Profiles.bolt(s.stack) && !Handling.supportRetained(pose.local(),Handling.support(Profiles.get(s.stack),CompatNetwork.calibration(p)),CompatNetwork.calibration(p))) {
                s.phase=Phase.READY;s.held=false;
            }
        }
        if(pose!=null && !s.held && Profiles.get(s.stack).smg() && ActionState.locked(s.stack)
            && s.previous!=null && p.getOffhandItem().isEmpty() && ActionCycle.releaseSweep(s.previous,pose.local(),Handling.release(Profiles.get(s.stack),CompatNetwork.calibration(p)),CompatNetwork.calibration(p).zones().release()))releaseAction(p,s);
        if(pose!=null && !s.held && Profiles.bolt(s.stack) && !BoltState.open(s.stack) && ActionState.lifted(s.stack)
            && s.previous!=null && (s.phase==Phase.READY||s.phase==Phase.NEED_RACK||s.phase==Phase.NO_MAG)
            && ActionCycle.boltSlap(s.previous,pose.local(),Handling.rack(Profiles.get(s.stack),CompatNetwork.calibration(p)),CompatNetwork.calibration(p).zones().rack())){
            ActionState.lifted(s.stack,false);HandlingFeedback.emit(p,5,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();
            if(s.phase!=Phase.NO_MAG)s.phase=IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;
        }

        s.previous=pose==null||s.held||s.phase==Phase.LOADING||s.phase==Phase.OLD_MAG||s.phase==Phase.NEW_MAG?null:new Vector3f(pose.local());
        if(s.phase==Phase.LOADING) {
            var operator=IGunOperator.fromLivingEntity(p);
            if(!operator.getDataHolder().reloadStateType.isReloading() && s.ticks>s.reloadStart+2) finishReload(s);
            else if(s.ticks-s.reloadStart>300) { operator.cancelReload();finishReload(s); }
        }
        float travel=pose==null?0:Handling.racking(s.phase)?pose.local().z-s.start.z:
            s.phase==Phase.REMOVING?s.start.y-pose.local().y:0;
        int pull=Math.min(Profiles.bolt(s.stack)?16:14,Math.max(0,Math.round(travel*200)));
        if(s.sent!=s.phase || s.sentPull!=pull || s.anchor!=null || s.ticks%10==0) { send(p,s,s.phase,pull);s.sent=s.phase;s.sentPull=pull; }
    }
    private static void finishReload(Session s) {
        IGun gun=IGun.getIGunOrNull(s.stack);
        // For an empty reload, undo TaCZ's automatic chamber step without creating ammo.
        finishSavedReload(s.stack);
        s.phase=gun.hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;
    }
    public static void cancelGrip(ServerPlayer p) {
        if(Profiles.cylinder(p.getMainHandItem())){ServerCylinder.grip(p,false,true);return;}
        if(Profiles.pump(p.getMainHandItem())){ServerPump.grip(p,false,true);return;}
        var s=SESSIONS.get(p.getUUID());if(s!=null){release(p,s,null);s.anchor=null;s.mainAnchor=null;}
    }
    public static void grip(ServerPlayer p, boolean held) {
        if(Profiles.cylinder(p.getMainHandItem())){ServerCylinder.grip(p,held,false);return;}
        if(Profiles.pump(p.getMainHandItem())){ServerPump.grip(p,held,false);return;}
        Session current=SESSIONS.get(p.getUUID());
        if(current!=null && (current.stack!=p.getMainHandItem() || current.slot!=p.getInventory().selected || !enabled(p))) {
            clear(p);return;
        }
        if(current!=null && current.anchor!=null && !current.mainInput){if(!held){release(p,current,null);current.anchor=null;}return;}
        if(!held) { if(current!=null) release(p,current,sample(p));return; }
        if(!enabled(p) || !p.getOffhandItem().isEmpty()) return;
        Session s=SESSIONS.get(p.getUUID());Sample pose=sample(p);
        if(s==null || s.stack!=p.getMainHandItem() || s.held || pose==null) return;
        if(s.phase==Phase.SUPPORT && Profiles.get(s.stack).supportDistance()==0)s.phase=IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;
        s.held=true;s.start.set(pose.local());
        WeaponProfile profile=Profiles.get(s.stack);
        var jam=ServerJams.read(s.stack);
        var target=Handling.target(s.phase,pose.local(),pose.pouch(),profile,CompatNetwork.calibration(p),dev.visorcompat.tacz.physical.BoltState.open(s.stack),ActionState.locked(s.stack),ActionState.lifted(s.stack));
        if((s.phase==Phase.READY || s.phase==Phase.NEED_RACK) && jam.kind()==Jam.Kind.STOVEPIPE && jam.remaining()>0
            && Handling.inside(pose.local(),JamProfile.of(profile,CompatNetwork.calibration(p)).port(),CompatNetwork.calibration(p),ZoneSizes.Zone.PORT))target=Handling.Target.CASING;
        if(ServerAdvanced.active(p)&&s.anchor!=null&&Handling.boltGrab(pose.local(),profile,CompatNetwork.calibration(p),BoltState.open(s.stack),ActionState.lifted(s.stack)))target=Handling.Target.RACK;
        if(s.anchor!=null && target!=Handling.Target.RACK){s.held=false;return;}
        switch(target) {
            case RELEASE -> {s.held=false;releaseAction(p,s);}
            case POUCH -> {if(jam.kind()==Jam.Kind.DOUBLE_FEED && jam.remaining()>0)s.held=false;else s.phase=Phase.NEW_MAG;}
            case CASING -> {s.rackFrom=s.phase;s.phase=Phase.PLUCKING;}
            case RACK -> {s.rackFrom=s.phase;s.phase=s.rackFrom==Phase.NO_MAG?Phase.RACKING_EMPTY:Phase.RACKING;s.pulled=false;s.jammedAtRack=GunDurabilityCompat.jammed(s.stack);
                if(profile.bolt()){
                    if(s.anchor!=null)s.start.set(pose.local()).sub(0,ActionState.lifted(s.stack)?.035f:0,BoltState.open(s.stack)?.08f:0);
                    else s.start.set(Handling.rack(profile,CompatNetwork.calibration(p)));
                }}
            case MAGAZINE -> s.phase=Phase.REMOVING;
            case SUPPORT -> {s.phase=Phase.SUPPORT;
                var gun=ServerPoses.validated(p);
                if(transferable(profile) && gun!=null && VisorAPI.getVRPlayer(p) instanceof VRServerPlayer vr)s.supportAnchor=HandAnchor.capture(gun,vr.getPoseData());
            }
            default -> s.held=false;
        }
    }
    private static void release(ServerPlayer p, Session s, Sample pose) {
        if(!s.held) return;
        if(!p.isAlive() || p.isSpectator() || !p.getOffhandItem().isEmpty())pose=null;
        s.held=false;
        switch(s.phase) {
            case REMOVING, SUPPORT -> s.phase=Phase.READY;
            case OLD_MAG -> s.phase=Phase.NO_MAG;
            case NEW_MAG -> {
                WeaponProfile profile=Profiles.get(s.stack);
                if(pose!=null && profile!=null && Handling.inside(pose.local(),Handling.magazine(profile,CompatNetwork.calibration(p)),CompatNetwork.calibration(p),ZoneSizes.Zone.MAGAZINE)) insert(p,s);
                else s.phase=Phase.NO_MAG;
            }
            case PLUCKING -> {
                if(pose!=null && pose.local().distance(s.start)>=.045f) {
                    ServerJams.pull(p,false,true);ServerJams.finish(p);
                }
                s.phase=s.rackFrom;
            }
            case RACKING, RACKING_EMPTY -> {
                if(Profiles.bolt(s.stack)){
                    if(pose!=null)moveBolt(p,s,pose);
                    s.phase=s.rackFrom==Phase.NO_MAG?Phase.NO_MAG:IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack) && !dev.visorcompat.tacz.physical.BoltState.blocked(s.stack)?Phase.READY:Phase.NEED_RACK;
                    break;
                }
                if(pose!=null)moveRack(p,s,pose);
                if(s.pulled && pose!=null){
                    if(Profiles.get(s.stack).smg()){
                        // Letting go rearward parks the MP5 in its notch; continuing forward while held closes it.
                        ActionState.locked(s.stack,true);
                        if(s.jammedAtRack)ServerJams.finish(p);
                    }else finishRack(p,s);
                }
                var gun=IGun.getIGunOrNull(s.stack);
                s.phase=s.rackFrom==Phase.NO_MAG?Phase.NO_MAG:gun.hasBulletInBarrel(s.stack)&&!ActionState.locked(s.stack)?Phase.READY:Phase.NEED_RACK;

            }
            default -> {}
        }
    }
    private static void releaseAction(ServerPlayer p,Session s){
        if(s.phase!=Phase.READY&&s.phase!=Phase.NEED_RACK&&s.phase!=Phase.NO_MAG)return;
        if(!ActionState.locked(s.stack)||GunDurabilityCompat.jammed(s.stack))return;
        var gun=IGun.getIGunOrNull(s.stack);
        var chamber=ActionCycle.release(new Chamber(gun.getCurrentAmmoCount(s.stack),gun.hasBulletInBarrel(s.stack)),true,s.phase!=Phase.NO_MAG,false);
        gun.setCurrentAmmoCount(s.stack,chamber.magazine());gun.setBulletInBarrel(s.stack,chamber.loaded());
        ActionState.locked(s.stack,false);
        if(s.phase!=Phase.NO_MAG)s.phase=chamber.loaded()?Phase.READY:Phase.NEED_RACK;
        HandlingFeedback.emit(p,5,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();
    }
    private static void moveBolt(ServerPlayer p,Session s,Sample pose) {
        var gun=IGun.getIGunOrNull(s.stack);var pos=pose.local();
        var old=BoltState.open(s.stack)?ActionCycle.Bolt.OPEN:ActionState.lifted(s.stack)?ActionCycle.Bolt.LIFTED:ActionCycle.Bolt.LOWERED;
        var next=ActionCycle.bolt(old,pos.z-s.start.z,pos.y-s.start.y,pos.x-s.start.x);
        if(next==old)return;
        if(next==ActionCycle.Bolt.OPEN){
            HandlingFeedback.emit(p,BoltState.spent(s.stack)?4:gun.hasBulletInBarrel(s.stack)?3:2,ServerPoses.validated(p));
            gun.setBulletInBarrel(s.stack,false);s.stack.getOrCreateTag().putBoolean(BoltState.SPENT,false);
        }
        if(old==ActionCycle.Bolt.OPEN&&next==ActionCycle.Bolt.LIFTED){
            if(s.jammedAtRack)GunDurabilityCompat.clear(p,s.stack);
            var chamber=new Chamber(gun.getCurrentAmmoCount(s.stack),gun.hasBulletInBarrel(s.stack));
            if(s.rackFrom!=Phase.NO_MAG&&!GunDurabilityCompat.jammed(s.stack))chamber=chamber.feed();
            gun.setCurrentAmmoCount(s.stack,chamber.magazine());gun.setBulletInBarrel(s.stack,chamber.loaded());
            HandlingFeedback.emit(p,5,ServerPoses.validated(p));
        }
        s.stack.getOrCreateTag().putBoolean(BoltState.OPEN,next==ActionCycle.Bolt.OPEN);
        ActionState.lifted(s.stack,next!=ActionCycle.Bolt.LOWERED);
        if(next==ActionCycle.Bolt.LOWERED){s.phase=s.rackFrom==Phase.NO_MAG?Phase.NO_MAG:gun.hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;s.held=false;}
        p.inventoryMenu.broadcastChanges();
    }

    private static void moveRack(ServerPlayer p,Session s,Sample pose){
        var pos=pose.local();
        var transition=ActionCycle.rack(s.pulled,pos.z-s.start.z,pos.x-s.start.x,pos.y-s.start.y);
        if(transition==dev.visorcompat.tacz.physical.PumpCycle.Transition.OPEN){
            pullRack(p,s);if(Profiles.get(s.stack).smg())ActionState.locked(s.stack,true);
        }else if(transition==dev.visorcompat.tacz.physical.PumpCycle.Transition.CLOSE)finishRack(p,s);
    }
    private static void finishRack(ServerPlayer p,Session s){
        if(!s.pulled)return;
        if(s.jammedAtRack)ServerJams.finish(p);
        var gun=IGun.getIGunOrNull(s.stack);
        var chamber=new Chamber(gun.getCurrentAmmoCount(s.stack),gun.hasBulletInBarrel(s.stack));
        if(s.rackFrom!=Phase.NO_MAG&&!GunDurabilityCompat.jammed(s.stack))chamber=chamber.feed();
        gun.setCurrentAmmoCount(s.stack,chamber.magazine());gun.setBulletInBarrel(s.stack,chamber.loaded());
        ActionState.locked(s.stack,false);s.pulled=false;s.jammedAtRack=GunDurabilityCompat.jammed(s.stack);
        HandlingFeedback.emit(p,5,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();
    }
    private static void pullRack(ServerPlayer p,Session s) {
        if(s.pulled)return;
        s.pulled=true;
        if(s.jammedAtRack && ServerJams.pull(p,s.rackFrom==Phase.NO_MAG))return;
        if(ManualCycle.spent(s.stack)){s.stack.getOrCreateTag().remove(ManualCycle.SPENT);HandlingFeedback.emit(p,4,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();return;}
        IGun gun=IGun.getIGunOrNull(s.stack);
        HandlingFeedback.emit(p,gun.hasBulletInBarrel(s.stack)?3:2,ServerPoses.validated(p));
        // Commit ejection immediately; holding the action open must not eject again.
        gun.setBulletInBarrel(s.stack,false);
    }
    private static void insert(ServerPlayer p, Session s) {
        var api=new com.tacz.guns.item.ModernKineticGunScriptAPI();
        api.setItemStack(s.stack);api.setShooter(p);
        var operator=IGunOperator.fromLivingEntity(p);api.setDataHolder(operator.getDataHolder());
        if(api.getGunIndex()==null){s.phase=Phase.NO_MAG;return;}
        boolean canceled;
        s.allowReload=true;
        try { canceled=net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
            new com.tacz.guns.api.event.common.GunReloadEvent(p,s.stack,net.minecraftforge.fml.LogicalSide.SERVER)); }
        finally { s.allowReload=false; }
        if(canceled || p.getMainHandItem()!=s.stack){s.phase=Phase.NO_MAG;return;}
        // The physical insertion already completed the reload gesture. Transfer native ammo
        // immediately, preserving the chamber and the removed magazine's saved rounds.
        restore(s.stack);
        int needed=Math.max(0,api.getNeededAmmoAmount());
        boolean consume=api.isReloadingNeedConsumeAmmo()&&!api.getGunIndex().getGunData().getReloadData().isInfinite();
        if(needed>0)api.putAmmoInMagazine(consume?api.consumeAmmoFromPlayer(needed):needed);
        var gun=IGun.getIGunOrNull(s.stack);
        s.phase=gun.hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;
        s.rackFrom=s.phase;
        HandlingFeedback.emit(p,1,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();
    }
    private static boolean mayTransfer(ServerPlayer p,Session s){
        var gun=IGun.getIGunOrNull(s.stack);
        return gun!=null && (!Profiles.bolt(s.stack)||dev.visorcompat.tacz.physical.TransferPolicy.mayStart(CompatNetwork.transferAnytime(p),BoltState.spent(s.stack),BoltState.open(s.stack),ActionState.lifted(s.stack),gun.hasBulletInBarrel(s.stack),GunDurabilityCompat.jammed(s.stack)));
    }
    public static boolean transferable(WeaponProfile profile){return profile!=null&&(profile.bolt()||profile.pump()||profile.mechanism()==WeaponProfile.Mechanism.MAGAZINE&&profile.supportDistance()>0);}
    public static void transfer(ServerPlayer p){
        if(Profiles.pump(p.getMainHandItem())){ServerPump.transfer(p);return;}
        var s=SESSIONS.get(p.getUUID());if(!enabled(p)||s==null||!transferable(Profiles.get(s.stack))||!p.getOffhandItem().isEmpty())return;
        var gun=ServerPoses.validated(p);if(gun==null)return;
        if(s.anchor!=null){
            var pose=((VRServerPlayer)VisorAPI.getVRPlayer(p)).getPoseData();
            var main=pose.getMainHand();var grip=CompatNetwork.calibration(p).position(new Vector3f(main.getPosition()),main.getRotation().getNormalizedRotation(new org.joml.Quaternionf()),gun.worldScale());
            if(grip.distance(gun.hand())>(Profiles.bolt(s.stack)&&ServerAdvanced.active(p)?dev.visorcompat.tacz.physical.RegripZone.ENTER:.16f)*gun.worldScale())return;
            if(Profiles.bolt(s.stack)&&(BoltState.blocked(s.stack)||GunDurabilityCompat.jammed(s.stack)))return;
            var restored=ServerAdvanced.active(p)?gun:GunPose.resolve(pose,Profiles.get(s.stack),false,CompatNetwork.calibration(p));
            s.mainAnchor=Profiles.bolt(s.stack)&&restored!=null?HandAnchor.captureMain(restored,pose):null;
            if(s.held)release(p,s,null);s.anchor=null;s.held=true;s.phase=Phase.SUPPORT;s.supportAnchor=HandAnchor.capture(gun,pose);
        }else if(s.phase==Phase.SUPPORT && s.held && (ServerAdvanced.active(p)||mayTransfer(p,s))){
            s.anchor=HandAnchor.capture(gun,((VRServerPlayer)VisorAPI.getVRPlayer(p)).getPoseData());s.held=false;s.phase=Phase.NEED_RACK;
        }
        send(p,s,s.phase,0);
    }
    public static void mainGrip(ServerPlayer p,boolean held,boolean canceled){
        var s=SESSIONS.get(p.getUUID());if(s==null||s.anchor==null&&!ServerAdvanced.freeMain(p))return;
        s.mainInput=true;
        try{if(canceled)release(p,s,null);else grip(p,held);}finally{s.mainInput=false;}
    }
    public static void pistolRelease(ServerPlayer p){
        var s=SESSIONS.get(p.getUUID());if(!enabled(p)||s==null||s.stack!=p.getMainHandItem()||Profiles.get(s.stack).supportDistance()!=0||s.held&&s.phase!=Phase.SUPPORT)return;
        boolean support=s.phase==Phase.SUPPORT;if(support)s.phase=Phase.READY;
        releaseAction(p,s);if(support)s.phase=Phase.SUPPORT;
    }
    public static void advancedUse(ServerPlayer p){
        var s=SESSIONS.get(p.getUUID());if(!enabled(p)||s==null||s.stack!=p.getMainHandItem()||s.anchor!=null||s.held&&s.phase!=Phase.SUPPORT||s.ticks-s.lastControl<6)return;
        var profile=Profiles.get(s.stack);if(profile.manualAction())return;
        var gun=IGun.getIGunOrNull(s.stack);
        if(dev.visorcompat.tacz.physical.ActionState.locked(s.stack)&&(gun.getCurrentAmmoCount(s.stack)>0||s.phase==Phase.NO_MAG)){
            boolean support=s.phase==Phase.SUPPORT;if(support)s.phase=Phase.READY;releaseAction(p,s);if(support)s.phase=Phase.SUPPORT;
            s.lastControl=s.ticks;send(p,s,s.phase,0);return;
        }
        if(s.phase!=Phase.READY&&s.phase!=Phase.NEED_RACK&&s.phase!=Phase.SUPPORT)return;
        var released=ServerPoses.validated(p);
        if(released!=null){
            var c=CompatNetwork.calibration(p);var offset=new org.joml.Quaternionf(released.rotation()).transform(Handling.magazine(profile).mul(c.gunScale()*released.worldScale()));
            var old=s.previousGun;var motion=old==null?new dev.visorcompat.tacz.physical.ReleaseMotion(new Vector3f(),new Vector3f()):dev.visorcompat.tacz.physical.ReleaseMotion.between(old.hand(),old.rotation(),released.hand(),released.rotation(),offset,.05f,released.worldScale());
            CompatNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new CompatNetwork.DroppedMagazine(s.stack.copy(),released.worldScale(),c.gunScale(),new Vector3f(released.hand()).add(offset),new org.joml.Quaternionf(released.rotation()),motion.velocity(),motion.spin(),gun.getCurrentAmmoCount(s.stack)>0,CompatNetwork.descriptors(p),CompatNetwork.descriptors(p)?DescriptorProfiles.digest():""));
        }
        // Reuse the existing reservation contract; removing the preview does not discard rounds.
        s.stack.getOrCreateTag().putInt(RESERVE,gun.getCurrentAmmoCount(s.stack));gun.setCurrentAmmoCount(s.stack,0);
        s.held=false;s.phase=Phase.NO_MAG;s.lastControl=s.ticks;HandlingFeedback.emit(p,0,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();send(p,s,s.phase,0);
    }
    public static void selector(ServerPlayer p) {
        Session s=SESSIONS.get(p.getUUID());Sample pose=sample(p);
        if(!enabled(p) || s==null || (s.phase!=Phase.READY&&s.phase!=Phase.SUPPORT) || pose==null || s.ticks-s.lastControl<6) return;
        if(Profiles.get(s.stack).selector() && Handling.inside(pose.local(),Handling.selector(Profiles.get(s.stack),CompatNetwork.calibration(p)),CompatNetwork.calibration(p),ZoneSizes.Zone.SELECTOR)) {
            IGunOperator.fromLivingEntity(p).fireSelect();s.lastControl=s.ticks;
        }
    }
    private static void send(ServerPlayer p, Session s, Phase phase, int pull) {
        CompatNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),
            new CompatNetwork.PhysicalState(Profiles.key(s.stack),s.slot,phase,pull,anchor(p)));
    }
}
