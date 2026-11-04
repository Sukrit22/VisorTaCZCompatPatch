package dev.visorcompat.tacz.server;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.*;
import dev.visorcompat.tacz.physical.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import java.util.*;

/** Advanced single-gun tactical transactions. No native reload animation owns these rounds. */
public final class TacticalServer {
    private TacticalServer(){}
    private static final Map<UUID,TacticalNetwork.Setting> SETTINGS=new HashMap<>();
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private static final class Session {
        final ItemStack stack;boolean grabDown,pinchDown,removing,removingPinch,mainHand;final Vector3f start=new Vector3f();
        Session(ItemStack stack){this.stack=stack;}
    }
    public static boolean supported(ItemStack stack){var p=Profiles.get(stack);return p!=null&&(p.mechanism()==WeaponProfile.Mechanism.MAGAZINE||p.mechanism()==WeaponProfile.Mechanism.SMG);}
    private static boolean requested(ServerPlayer p){var s=SETTINGS.get(p.getUUID());return s!=null&&s.enabled();}
    public static boolean enabled(ServerPlayer p){return requested(p)&&ServerAdvanced.active(p)&&ServerPhysical.enabled(p)&&supported(p.getMainHandItem());}
    public static void setting(ServerPlayer p,TacticalNetwork.Setting value){SETTINGS.put(p.getUUID(),value);}
    public static TacticalCalibration calibration(ServerPlayer p){var value=SETTINGS.get(p.getUUID());return value!=null&&value.key().equals(Profiles.key(p.getMainHandItem()))?value.value():TacticalCalibration.DEFAULT;}
    private static Session session(ServerPlayer p){
        var s=SESSIONS.get(p.getUUID());boolean main=ServerAdvanced.freeMain(p);
        if(s==null||s.stack!=p.getMainHandItem()){
            if(s!=null&&TacticalState.exists(s.stack))TacticalState.guide(s.stack,0);
            s=new Session(p.getMainHandItem());if(TacticalState.exists(s.stack))TacticalState.guide(s.stack,0);
            s.mainHand=main;SESSIONS.put(p.getUUID(),s);
        }else if(s.mainHand!=main){cancel(p);s.mainHand=main;}
        return s;
    }
    public static boolean supportAllowed(ServerPlayer p){var stack=p.getMainHandItem();return enabled(p)&&calibration(p).alternativeControls()&&TacticalState.count(stack,false)<0&&TacticalState.count(stack,true)>=0&&TacticalState.old(stack,true);}
    public static boolean busy(ServerPlayer p){var s=SESSIONS.get(p.getUUID());return enabled(p)&&(TacticalState.count(p.getMainHandItem(),false)>=0||TacticalState.count(p.getMainHandItem(),true)>=0&&!supportAllowed(p)||s!=null&&s.removing);}
    private record Sample(GunPose gun,Vector3f hand,Quaternionf rotation,Vector3f local,boolean pouch){}
    private static Sample sample(ServerPlayer p){
        if(!enabled(p)||!ServerAdvanced.canInteract(p)||!p.getOffhandItem().isEmpty()||!p.isAlive()||p.isSpectator()||!(VisorAPI.getVRPlayer(p) instanceof VRServerPlayer vr))return null;
        var gun=ServerPoses.validated(p);if(gun==null)return null;
        var pose=vr.getPoseData();var hand=ServerAdvanced.freeMain(p)?pose.getMainHand():pose.getOffhand();
        var position=new Vector3f(hand.getPosition());if(position.distance(pose.getHmd().getPosition())>1.6f*gun.worldScale()||!PoseMath.finite(position))return null;
        if(ServerPoses.blocked(p,pose.getHmd().getPositionVec3(),new net.minecraft.world.phys.Vec3(position)))return null;
        var forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
        return new Sample(gun,position,hand.getRotation().getNormalizedRotation(new Quaternionf()),Handling.local(gun,position),Handling.inPouch(position,pose.getHmd().getPosition(),forward,gun.worldScale(),CompatNetwork.calibration(p)));
    }
    /** True only when tactical owns this fresh press/release. Other controls fall through. */
    public static boolean input(ServerPlayer p,boolean pinch,boolean down,boolean canceled){
        if(!enabled(p))return false;var s=session(p);var stack=s.stack;var ledger=TacticalState.read(stack);
        boolean was=pinch?s.pinchDown:s.grabDown;
        if(!down){
            if(pinch)s.pinchDown=false;else s.grabDown=false;
            if(s.removingPinch==pinch)s.removing=false;
            if(!was)return false;
            var at=sample(p);
            // Lost focus/pose is not a deliberate discard. Keep the reservation until resumed or normalized.
            if(!canceled&&at!=null&&ledger.rounds(pinch)>=0){
                int count=ledger.release(pinch);TacticalState.write(stack,ledger);TacticalState.guide(stack,0);
                if(at.pouch())refund(p,stack,count,TacticalState.refundable(stack,pinch));
                else droppedFromHand(p,stack,at,pinch,count>0);
                sync(p);
            }
            return true;
        }
        if(was)return true;
        var at=sample(p);if(at==null)return false;
        var phase=ServerPhysical.phase(p);if(Handling.racking(phase)||phase==Handling.Phase.LOADING||phase==Handling.Phase.PLUCKING)return false;
        if(ledger.rounds(pinch)>=0){
            // Recovery after a canceled gesture needs a fresh press; the prop remains at this hand.
            if(pinch)s.pinchDown=true;else s.grabDown=true;return true;
        }
        if(at.pouch()){
            var jam=ServerJams.read(stack);if(jam.kind()==Jam.Kind.DOUBLE_FEED&&jam.remaining()>0)return true;
            var api=api(p,stack);if(api.getGunIndex()==null)return true;
            boolean consume=api.isReloadingNeedConsumeAmmo()&&!api.getGunIndex().getGunData().getReloadData().isInfinite();
            int capacity=Math.max(0,Math.min(4096,api.getMaxAmmoCount()));
            int count=consume?api.consumeAmmoFromPlayer(capacity):capacity;
            if(count<=0){p.displayClientMessage(net.minecraft.network.chat.Component.literal("TaCZ VR: OUT OF AMMO"),true);return true;}
            ledger.take(pinch,count);TacticalState.write(stack,ledger);TacticalState.refundable(stack,pinch,consume);
            TacticalState.unanchor(stack,pinch);TacticalState.old(stack,pinch,false);
            if(pinch)s.pinchDown=true;else s.grabDown=true;sync(p);return true;
        }
        if(!pinch&&supportAllowed(p)&&Handling.inside(at.local(),Handling.support(Profiles.get(stack),CompatNetwork.calibration(p)),CompatNetwork.calibration(p),ZoneSizes.Zone.SUPPORT)
            &&at.local().distance(Handling.support(Profiles.get(stack),CompatNetwork.calibration(p)))<at.local().distance(Handling.magazine(Profiles.get(stack),CompatNetwork.calibration(p))))return false;
        if((!pinch||calibration(p).alternativeControls())&&!ledger.out()&&Handling.inside(at.local(),Handling.magazine(Profiles.get(stack),CompatNetwork.calibration(p)),CompatNetwork.calibration(p),ZoneSizes.Zone.MAGAZINE)){
            if(pinch)s.pinchDown=true;else s.grabDown=true;s.removing=true;s.removingPinch=pinch;s.start.set(at.local());
            var world=new Quaternionf(at.gun().rotation()).transform(Handling.magazine(Profiles.get(stack),CompatNetwork.calibration(p)).mul(at.gun().worldScale())).add(at.gun().hand());
            var local=new Quaternionf(at.rotation()).conjugate().transform(world.sub(at.hand())).div(at.gun().worldScale());
            TacticalState.capture(stack,pinch,local,new Quaternionf(at.rotation()).conjugate().mul(at.gun().rotation()));return true;
        }
        return false;
    }
    public static void tick(ServerPlayer p){
        if(!enabled(p)){if(!requested(p)||!ServerPoses.isVr(p)||!CompatNetwork.physical(p))normalizeInventory(p);return;}
        var s=session(p);var at=sample(p);if(at==null){s.removing=false;s.grabDown=false;s.pinchDown=false;if(TacticalState.exists(s.stack))TacticalState.guide(s.stack,0);return;}
        var stack=s.stack;var ledger=TacticalState.read(stack);var gun=IGun.getIGunOrNull(stack);
        if(s.removing&&(s.removingPinch?s.pinchDown:s.grabDown)&&Handling.magazinePulled(s.start,at.local())){
            if(ledger.remove(s.removingPinch,gun.getCurrentAmmoCount(stack))){gun.setCurrentAmmoCount(stack,0);TacticalState.write(stack,ledger);TacticalState.refundable(stack,s.removingPinch,paid(p,stack));TacticalState.old(stack,s.removingPinch,true);HandlingFeedback.emit(p,0,at.gun());sync(p);}
            s.removing=false;
        }
        if(!ledger.out())return;
        int active=TacticalState.guide(stack);var c=calibration(p);var entry=TacticalPose.entry(Profiles.get(stack),CompatNetwork.calibration(p),c);var rail=TacticalPose.rotation(c.magwellEntry());
        var candidates=new ArrayList<>(List.of(false,true));
        candidates.sort(Comparator.comparingDouble(pinch->TacticalPose.magazine(stack,pinch,c,at.gun(),at.hand(),at.rotation()).position().distanceSquared(entry)));
        for(boolean pinch:candidates){
            int id=pinch?2:1;if(active!=0&&active!=id||ledger.rounds(pinch)<0||!(pinch?s.pinchDown:s.grabDown))continue;
            var pose=TacticalPose.magazine(stack,pinch,c,at.gun(),at.hand(),at.rotation());
            var guide=MagazineGuide.solve(pose.position(),pose.rotation(),entry,rail,c,active==id);
            if(guide.engaged()){
                if(active!=id){TacticalState.guide(stack,id);sync(p);}
                if(guide.seated()&&!(ServerJams.read(stack).kind()==Jam.Kind.DOUBLE_FEED&&ServerJams.read(stack).remaining()>0)&&ServerPhysical.authorizeTacticalInsertion(p)){
                    // A changed attachment can reduce capacity while a magazine is outside the gun.
                    int capacity=api(p,stack).getMaxAmmoCount();int rounds=ledger.insert(pinch);int loaded=Math.min(rounds,Math.max(0,capacity));
                    TacticalState.write(stack,ledger);TacticalState.guide(stack,0);gun.setCurrentAmmoCount(stack,loaded);
                    refund(p,stack,rounds-loaded,TacticalState.refundable(stack,pinch));
                    HandlingFeedback.emit(p,1,at.gun());sync(p);
                }
                break;
            }else if(active==id){TacticalState.guide(stack,0);sync(p);break;}
        }
    }
    /** Main-hand Use can release the latch while a fresh magazine is already held. */
    public static boolean eject(ServerPlayer p){
        if(!enabled(p))return false;var at=sample(p);if(at==null)return false;var s=session(p);var ledger=TacticalState.read(s.stack);
        if(ledger.out())return false;
        var phase=ServerPhysical.phase(p);if(Handling.racking(phase)||phase==Handling.Phase.LOADING||s.removing)return false;
        var gun=IGun.getIGunOrNull(s.stack);dropped(p,s.stack,at.gun(),gun.getCurrentAmmoCount(s.stack)>0);
        ledger.eject();TacticalState.write(s.stack,ledger);gun.setCurrentAmmoCount(s.stack,0);HandlingFeedback.emit(p,0,at.gun());sync(p);return true;
    }
    private static com.tacz.guns.item.ModernKineticGunScriptAPI api(ServerPlayer p,ItemStack stack){var api=new com.tacz.guns.item.ModernKineticGunScriptAPI();api.setItemStack(stack);api.setShooter(p);api.setDataHolder(IGunOperator.fromLivingEntity(p).getDataHolder());return api;}
    private static boolean paid(ServerPlayer p,ItemStack stack){var api=api(p,stack);return api.getGunIndex()!=null&&api.isReloadingNeedConsumeAmmo()&&!api.getGunIndex().getGunData().getReloadData().isInfinite();}
    private static void refund(ServerPlayer p,ItemStack stack,int count,boolean paid){
        if(count<=0||!paid)return;var gun=IGun.getIGunOrNull(stack);if(gun==null)return;
        if(gun.useDummyAmmo(stack)){gun.addDummyAmmoAmount(stack,count);return;}
        var index=com.tacz.guns.api.TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);if(index==null)return;
        while(count>0){int batch=Math.min(64,count);var ammo=com.tacz.guns.api.item.builder.AmmoItemBuilder.create().setId(index.getGunData().getAmmoId()).setCount(batch).build();count-=batch;p.getInventory().add(ammo);if(!ammo.isEmpty())p.drop(ammo,false);}
    }
    private static void droppedFromHand(ServerPlayer p,ItemStack stack,Sample at,boolean pinch,boolean loaded){
        var c=CompatNetwork.calibration(p);var local=TacticalPose.magazine(stack,pinch,calibration(p),at.gun(),at.hand(),at.rotation());
        var position=new Quaternionf(at.gun().rotation()).transform(new Vector3f(local.position()).mul(at.gun().worldScale())).add(at.gun().hand());
        var rotation=new Quaternionf(at.gun().rotation()).mul(local.rotation());
        CompatNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new CompatNetwork.DroppedMagazine(p.getUUID(),p.getInventory().selected,stack.copy(),at.gun().worldScale(),c.gunScale(),position,rotation,CompatNetwork.physics(p),loaded,CompatNetwork.descriptors(p),CompatNetwork.descriptors(p)?DescriptorProfiles.digest():"",true));
    }
    private static void dropped(ServerPlayer p,ItemStack stack,GunPose pose,boolean loaded){var c=CompatNetwork.calibration(p);CompatNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new CompatNetwork.DroppedMagazine(p.getUUID(),p.getInventory().selected,stack.copy(),pose.worldScale(),c.gunScale(),new Vector3f(pose.hand()),new Quaternionf(pose.rotation()),CompatNetwork.physics(p),loaded,CompatNetwork.descriptors(p),CompatNetwork.descriptors(p)?DescriptorProfiles.digest():""));}
    public static void cancel(ServerPlayer p){var s=SESSIONS.get(p.getUUID());if(s!=null){s.grabDown=false;s.pinchDown=false;s.removing=false;TacticalState.guide(s.stack,0);}}
    public static void normalizeInventory(ServerPlayer p){
        boolean changed=false;
        // Never restore an old ItemStack into an inventory slot: deletion/drop must remain deletion/drop.
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            var stack=p.getInventory().getItem(i);if(!TacticalState.exists(stack))continue;
            int grab=TacticalState.count(stack,false),pinch=TacticalState.count(stack,true);boolean gp=TacticalState.refundable(stack,false),pp=TacticalState.refundable(stack,true);
            TacticalState.clear(stack);refund(p,stack,grab,gp);refund(p,stack,pinch,pp);changed=true;
        }
        SESSIONS.remove(p.getUUID());if(changed)sync(p);
    }
    public static void logout(ServerPlayer p){normalizeInventory(p);SETTINGS.remove(p.getUUID());}
    public static void stop(){SESSIONS.clear();SETTINGS.clear();}
    private static void sync(ServerPlayer p){p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();}
}
