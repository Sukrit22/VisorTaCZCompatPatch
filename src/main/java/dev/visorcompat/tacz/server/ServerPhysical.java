package dev.visorcompat.tacz.server;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.physical.Handling;
import dev.visorcompat.tacz.physical.Chamber;
import dev.visorcompat.tacz.physical.Jam;
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
        ItemStack stack; int slot; Phase phase=Phase.READY; Phase rackFrom=Phase.READY;
        boolean held, pulled, allowReload, jammedAtRack; Vector3f start=new Vector3f();
        int ticks, reloadStart, lastControl=-20; Phase sent; int sentPull=-1;
        Session(ServerPlayer p) {
            stack=p.getMainHandItem();slot=p.getInventory().selected;
            IGun gun=IGun.getIGunOrNull(stack);
            if(gun!=null && !gun.hasBulletInBarrel(stack) && gun.getCurrentAmmoCount(stack)>0)phase=Phase.NEED_RACK;
        }
    }
    record Sample(Vector3f local, boolean pouch) {}
    public static boolean enabled(ServerPlayer p) { return CompatNetwork.physical(p) && ServerPoses.isVr(p) && Profiles.get(p.getMainHandItem())!=null; }
    public static Phase phase(ServerPlayer p) {if(Profiles.pump(p.getMainHandItem()))return ServerPump.phase(p);Session s=SESSIONS.get(p.getUUID());return s==null?Phase.READY:s.phase;}
    public static int pull(ServerPlayer p) {if(Profiles.pump(p.getMainHandItem()))return ServerPump.pull(p);Session s=SESSIONS.get(p.getUUID());return s==null?0:Math.max(0,s.sentPull);}
    public static boolean supporting(ServerPlayer p) {
        if(Profiles.pump(p.getMainHandItem()))return ServerPump.supporting(p);
        Session s=SESSIONS.get(p.getUUID());return s!=null && s.stack==p.getMainHandItem() && s.phase==Phase.SUPPORT;
    }
    public static boolean canFire(ServerPlayer p) {
        if(!enabled(p)) return true;
        if(Profiles.pump(p.getMainHandItem()))return ServerPump.canFire(p);
        Session s=SESSIONS.get(p.getUUID());return s!=null && s.stack==p.getMainHandItem() && Handling.fireable(s.phase)
            && !GunDurabilityCompat.jammed(s.stack) && IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack);
    }
    public static boolean allowsReload(ServerPlayer p) {
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
        ServerPump.clear(p);
        Session s=SESSIONS.remove(p.getUUID());
        if(s!=null) {
            if(s.phase==Phase.LOADING) IGunOperator.fromLivingEntity(p).cancelReload();
            restore(s.stack);
            send(p,s,Phase.READY,0);
        }
    }
    public static void stop() { SESSIONS.clear();ServerPump.stop(); }
    static Sample sample(ServerPlayer p) {
        GunPose gun=ServerPoses.validated(p);
        if(gun==null || !(VisorAPI.getVRPlayer(p) instanceof VRServerPlayer vr)) return null;
        var pose=vr.getPoseData();
        Vector3f off=new Vector3f(pose.getOffhand().getPosition());
        if(!PoseMath.finite(off) || off.distance(pose.getHmd().getPosition())>1.6f*gun.worldScale()) return null;
        Vector3f forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
        Vector3f pouch=Handling.pouch(pose.getHmd().getPosition(),forward,gun.worldScale(),CompatNetwork.calibration(p));
        return new Sample(Handling.local(gun,off),off.distance(pouch)<.25f*gun.worldScale());
    }
    public static void tick(ServerPlayer p) {
        ServerPump.cleanup(p);
        Session s=SESSIONS.get(p.getUUID());
        if(s!=null && (!enabled(p) || !p.isAlive() || p.isSpectator() || s.stack!=p.getMainHandItem() || s.slot!=p.getInventory().selected)) {
            clear(p);s=null;
        }
        if(!enabled(p) || !p.isAlive() || p.isSpectator()) { restore(p.getMainHandItem());return; }
        if(Profiles.pump(p.getMainHandItem())){ServerPump.tick(p);return;}
        if(s==null) {
            restore(p.getMainHandItem());
            s=new Session(p);SESSIONS.put(p.getUUID(),s);
        }
        if(!s.held && s.phase!=Phase.LOADING && !Handling.magazineOut(s.phase))ServerJams.observe(p);
        s.ticks++;
        Sample pose=sample(p);
        if((pose==null || !p.getOffhandItem().isEmpty()) && s.held) release(p,s,null);
        if(pose!=null && s.held) {
            if(s.phase==Phase.REMOVING && Handling.magazinePulled(s.start,pose.local())) {
                IGun gun=IGun.getIGunOrNull(s.stack);
                s.stack.getOrCreateTag().putInt(RESERVE,gun.getCurrentAmmoCount(s.stack));
                gun.setCurrentAmmoCount(s.stack,0);
                s.phase=Phase.OLD_MAG;HandlingFeedback.emit(p,0,ServerPoses.validated(p));
            } else if(Handling.racking(s.phase) && Handling.racked(s.start,pose.local())) pullRack(p,s);
            else if(s.phase==Phase.SUPPORT && !Handling.near(pose.local(),Handling.support(Profiles.get(s.stack),CompatNetwork.calibration(p)),.24f)) {
                s.phase=Phase.READY;s.held=false;
            }
        }
        if(s.phase==Phase.LOADING) {
            var operator=IGunOperator.fromLivingEntity(p);
            if(!operator.getDataHolder().reloadStateType.isReloading() && s.ticks>s.reloadStart+2) finishReload(s);
            else if(s.ticks-s.reloadStart>300) { operator.cancelReload();finishReload(s); }
        }
        float travel=pose==null?0:Handling.racking(s.phase)?pose.local().z-s.start.z:
            s.phase==Phase.REMOVING?s.start.y-pose.local().y:0;
        int pull=Math.min(14,Math.max(0,Math.round(travel*200)));
        if(s.sent!=s.phase || s.sentPull!=pull) { send(p,s,s.phase,pull);s.sent=s.phase;s.sentPull=pull; }
    }
    private static void finishReload(Session s) {
        IGun gun=IGun.getIGunOrNull(s.stack);
        // For an empty reload, undo TaCZ's automatic chamber step without creating ammo.
        finishSavedReload(s.stack);
        s.phase=gun.hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;
    }
    public static void cancelGrip(ServerPlayer p) {
        if(Profiles.pump(p.getMainHandItem())){ServerPump.grip(p,false,true);return;}
        var s=SESSIONS.get(p.getUUID());if(s!=null)release(p,s,null);
    }
    public static void grip(ServerPlayer p, boolean held) {
        if(Profiles.pump(p.getMainHandItem())){ServerPump.grip(p,held,false);return;}
        Session current=SESSIONS.get(p.getUUID());
        if(current!=null && (current.stack!=p.getMainHandItem() || current.slot!=p.getInventory().selected || !enabled(p))) {
            clear(p);return;
        }
        if(!held) { if(current!=null) release(p,current,sample(p));return; }
        if(!enabled(p) || !p.getOffhandItem().isEmpty()) return;
        Session s=SESSIONS.get(p.getUUID());Sample pose=sample(p);
        if(s==null || s.stack!=p.getMainHandItem() || s.held || pose==null) return;
        s.held=true;s.start.set(pose.local());
        WeaponProfile profile=Profiles.get(s.stack);
        var jam=ServerJams.read(s.stack);
        var target=Handling.target(s.phase,pose.local(),pose.pouch(),profile,CompatNetwork.calibration(p));
        if((s.phase==Phase.READY || s.phase==Phase.NEED_RACK) && jam.kind()==Jam.Kind.STOVEPIPE && jam.remaining()>0
            && Handling.near(pose.local(),JamProfile.of(profile,CompatNetwork.calibration(p)).port(),.035f))target=Handling.Target.CASING;
        switch(target) {
            case POUCH -> {if(jam.kind()==Jam.Kind.DOUBLE_FEED && jam.remaining()>0)s.held=false;else s.phase=Phase.NEW_MAG;}
            case CASING -> {s.rackFrom=s.phase;s.phase=Phase.PLUCKING;}
            case RACK -> {s.rackFrom=s.phase;s.phase=s.rackFrom==Phase.NO_MAG?Phase.RACKING_EMPTY:Phase.RACKING;s.pulled=false;s.jammedAtRack=GunDurabilityCompat.jammed(s.stack);}
            case MAGAZINE -> s.phase=Phase.REMOVING;
            case SUPPORT -> s.phase=Phase.SUPPORT;
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
                if(pose!=null && profile!=null && Handling.near(pose.local(),Handling.magazine(profile,CompatNetwork.calibration(p)),.09f)) insert(p,s);
                else s.phase=Phase.NO_MAG;
            }
            case PLUCKING -> {
                if(pose!=null && pose.local().distance(s.start)>=.045f) {
                    ServerJams.pull(p,false,true);ServerJams.finish(p);
                }
                s.phase=s.rackFrom;
            }
            case RACKING, RACKING_EMPTY -> {
                boolean noMagazine=s.rackFrom==Phase.NO_MAG;
                IGun gun=IGun.getIGunOrNull(s.stack);
                if(pose!=null && (s.pulled || Handling.racked(s.start,pose.local()))) {
                    // Catch a completed pull arriving in the release pose between ticks.
                    pullRack(p,s);
                    if(s.jammedAtRack)ServerJams.finish(p);
                    HandlingFeedback.emit(p,5,ServerPoses.validated(p));
                    Chamber chamber=new Chamber(gun.getCurrentAmmoCount(s.stack),gun.hasBulletInBarrel(s.stack));
                    if(!noMagazine && !GunDurabilityCompat.jammed(s.stack))chamber=chamber.feed();
                    gun.setCurrentAmmoCount(s.stack,chamber.magazine());
                    gun.setBulletInBarrel(s.stack,chamber.loaded());
                    s.phase=noMagazine?Phase.NO_MAG:gun.hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;
                } else s.phase=noMagazine?Phase.NO_MAG:s.pulled?Phase.NEED_RACK:s.rackFrom;
            }
            default -> {}
        }
    }
    private static void pullRack(ServerPlayer p,Session s) {
        if(s.pulled)return;
        s.pulled=true;
        if(s.jammedAtRack && ServerJams.pull(p,s.rackFrom==Phase.NO_MAG))return;
        IGun gun=IGun.getIGunOrNull(s.stack);
        HandlingFeedback.emit(p,gun.hasBulletInBarrel(s.stack)?3:2,ServerPoses.validated(p));
        // Commit ejection immediately; holding the action open must not eject again.
        gun.setBulletInBarrel(s.stack,false);
    }
    private static void insert(ServerPlayer p, Session s) {
        HandlingFeedback.emit(p,1,ServerPoses.validated(p));
        restore(s.stack);
        IGun gun=IGun.getIGunOrNull(s.stack);
        s.rackFrom=gun.hasBulletInBarrel(s.stack)?Phase.READY:Phase.NEED_RACK;
        if(s.rackFrom==Phase.NEED_RACK)s.stack.getOrCreateTag().putBoolean(EMPTY_RELOAD,true);
        var operator=IGunOperator.fromLivingEntity(p);
        s.allowReload=true;
        try { operator.reload(); } finally { s.allowReload=false; }
        if(operator.getDataHolder().reloadStateType.isReloading()) { s.phase=Phase.LOADING;s.reloadStart=s.ticks; }
        else finishReload(s);
    }
    public static void selector(ServerPlayer p) {
        Session s=SESSIONS.get(p.getUUID());Sample pose=sample(p);
        if(!enabled(p) || s==null || s.phase!=Phase.READY || pose==null || s.ticks-s.lastControl<6) return;
        if(Handling.near(pose.local(),Handling.selector(Profiles.get(s.stack),CompatNetwork.calibration(p)),.11f)) {
            IGunOperator.fromLivingEntity(p).fireSelect();s.lastControl=s.ticks;
        }
    }
    private static void send(ServerPlayer p, Session s, Phase phase, int pull) {
        CompatNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),
            new CompatNetwork.PhysicalState(Profiles.key(s.stack),s.slot,phase,pull));
    }
}
