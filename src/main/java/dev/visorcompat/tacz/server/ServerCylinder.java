package dev.visorcompat.tacz.server;
import java.util.*;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.*;
import dev.visorcompat.tacz.physical.Handling.Phase;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.compat.GunDurabilityCompat;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

/** Experimental aggregate cylinder/breech loading. No live rounds are stored in addon NBT. */
public final class ServerCylinder {
    public static final String OPEN="visor_tacz_cylinder_open",SPENT="visor_tacz_cylinder_spent";
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static class State {ItemStack stack;int slot,ticks;boolean held,done,shell,eject,allowReload,loader;Vector3f start;CylinderMotion motion=new CylinderMotion();CylinderShake shake=new CylinderShake();Vector3f gestureRight;long lastPose;Phase sent;State(ServerPlayer p){stack=p.getMainHandItem();slot=p.getInventory().selected;}}
    public static boolean open(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(OPEN);}
    private static boolean valid(ServerPlayer p,State s){return s!=null&&s.stack==p.getMainHandItem()&&s.slot==p.getInventory().selected;}
    public static Phase phase(ServerPlayer p){var s=STATES.get(p.getUUID());return !valid(p,s)?Phase.READY:s.shell?(s.loader?Phase.LOADER:Phase.SHELL):s.held?Phase.CYLINDER_HOLD:open(s.stack)?Phase.CYLINDER_OPEN:Phase.READY;}
    public static boolean canFire(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)&&!s.held&&!open(s.stack)&&!GunDurabilityCompat.jammed(s.stack)&&IGun.getIGunOrNull(s.stack).getCurrentAmmoCount(s.stack)>0;}
    public static boolean allowsReload(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)&&s.allowReload;}
    public static void clear(ServerPlayer p){STATES.remove(p.getUUID());}
    public static void stop(){STATES.clear();}
    public static void cleanup(ServerPlayer p){var s=STATES.get(p.getUUID());if(s!=null&&(!valid(p,s)||!ServerPhysical.enabled(p)||!p.isAlive()||p.isSpectator()))clear(p);}
    public static void shot(ServerPlayer p,ItemStack stack){if(ServerPhysical.enabled(p)&&Profiles.cylinder(stack))stack.getOrCreateTag().putInt(SPENT,Math.min(64,stack.getOrCreateTag().getInt(SPENT)+1));}
    public static void tick(ServerPlayer p){
        var s=STATES.computeIfAbsent(p.getUUID(),id->new State(p));var sample=ServerPhysical.sample(p);
        if(sample==null||!p.getOffhandItem().isEmpty()){s.held=false;s.shell=false;}
        if(s.held&&!s.shell&&!s.done&&sample!=null)move(p,s,sample.local());
        gestures(p,s);
        var phase=phase(p);if(phase!=s.sent||++s.ticks%10==0){CompatNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new CompatNetwork.PhysicalState(Profiles.key(s.stack),s.slot,phase,open(s.stack)?16:0));s.sent=phase;}
    }
    private static void move(ServerPlayer p,State s,Vector3f pos){
        var delta=new Vector3f(pos).sub(s.start);
        if(s.eject){
            if(open(s.stack)&&delta.z>=.045f&&Math.abs(delta.x)<.12f&&Math.abs(delta.y)<.12f){
                eject(p,s,false);s.done=true;
            }
        }else if(CylinderCycle.toggle(delta.x,delta.y,delta.z)){
            s.motion.reset();boolean wasOpen=open(s.stack);s.stack.getOrCreateTag().putBoolean(OPEN,!wasOpen);s.done=true;
            if(wasOpen)GunDurabilityCompat.clear(p,s.stack);
            HandlingFeedback.emit(p,wasOpen?5:2,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();
        }
    }
    private static void eject(ServerPlayer p,State s,boolean gravity){
        var pose=ServerPoses.validated(p);if(pose==null)return;
        var gun=IGun.getIGunOrNull(s.stack);int live=Math.max(0,Math.min(64,gun.getCurrentAmmoCount(s.stack)));
        int spent=CylinderCycle.spent(s.stack.getOrCreateTag().getInt(SPENT),live,64);
        if(live+spent==0)return;
        gun.setCurrentAmmoCount(s.stack,0);s.stack.getOrCreateTag().putInt(SPENT,0);
        HandlingFeedback.cylinderEject(p,pose,live,spent,gravity);p.inventoryMenu.broadcastChanges();
    }
    private static void gestures(ServerPlayer p,State s){
        var pose=ServerPoses.validated(p);
        if(!ServerAdvanced.active(p)||pose==null||!(org.vmstudio.visor.api.VisorAPI.getVRPlayer(p) instanceof org.vmstudio.visor.api.server.player.VRServerPlayer vr)||!(vr instanceof FreshPose fresh)){
            s.motion.reset();s.shake.reset();s.gestureRight=null;s.lastPose=0;return;
        }
        long time=fresh.visorTacz$lastPoseNanos();if(time==s.lastPose)return;
        var data=vr.getPoseData();
        if(s.gestureRight==null){s.gestureRight=data.getHmd().getRotation().transformDirection(new Vector3f(1,0,0));s.gestureRight.y=0;if(s.gestureRight.lengthSquared()<.1f)s.gestureRight.set(1,0,0);s.gestureRight.normalize();}
        float position=new Vector3f((ServerAdvanced.cylinderOffhand(p)?data.getOffhand():data.getMainHand()).getPosition()).sub(data.getHmd().getPosition()).dot(s.gestureRight)/pose.worldScale();
        float dt=s.lastPose==0?.05f:(time-s.lastPose)/1_000_000_000f;
        if(!s.held&&Profiles.get(s.stack).mechanism()!=WeaponProfile.Mechanism.LONETRAIL){
            int action=s.motion.update(position,dt,open(s.stack));
            if(action!=0){s.stack.getOrCreateTag().putBoolean(OPEN,action>0);if(action<0)GunDurabilityCompat.clear(p,s.stack);HandlingFeedback.emit(p,action>0?2:5,pose);p.inventoryMenu.broadcastChanges();}
        }else s.motion.reset();
        float height=((ServerAdvanced.cylinderOffhand(p)?data.getOffhand():data.getMainHand()).getPosition().y()-data.getHmd().getPosition().y())/pose.worldScale();
        if(s.shake.update(height,dt,open(s.stack)&&!s.held&&CylinderCycle.fallsOut(pose.direction().y)))eject(p,s,true);
        s.lastPose=time;
    }
    public static void armOpen(ServerPlayer p){
        var s=STATES.get(p.getUUID());if(!valid(p,s)||!ServerAdvanced.active(p)||s.held||open(s.stack)||Profiles.get(s.stack).mechanism()==WeaponProfile.Mechanism.LONETRAIL)return;
        if(!(org.vmstudio.visor.api.VisorAPI.getVRPlayer(p) instanceof org.vmstudio.visor.api.server.player.VRServerPlayer vr))return;
        var pose=vr.getPoseData();s.gestureRight=pose.getHmd().getRotation().transformDirection(new Vector3f(1,0,0));s.gestureRight.y=0;
        if(s.gestureRight.lengthSquared()<.1f)s.gestureRight.set(1,0,0);s.gestureRight.normalize();s.motion.armOpen();
        float position=new Vector3f((ServerAdvanced.cylinderOffhand(p)?pose.getOffhand():pose.getMainHand()).getPosition()).sub(pose.getHmd().getPosition()).dot(s.gestureRight)/pose.getWorldScale();
        s.motion.update(position,.05f,false);
    }

    public static void grip(ServerPlayer p,boolean held,boolean canceled){grip(p,held,canceled,true);}
    public static void grip(ServerPlayer p,boolean held,boolean canceled,boolean loader){
        var s=STATES.get(p.getUUID());if(!valid(p,s)||!ServerPhysical.enabled(p)||!p.isAlive()||p.isSpectator())return;
        var sample=ServerPhysical.sample(p);
        if(!held){
            if(s.held&&!canceled&&sample!=null&&p.getOffhandItem().isEmpty()){
                if(s.shell&&Handling.inside(sample.local(),Handling.magazine(Profiles.get(s.stack),CompatNetwork.calibration(p)),CompatNetwork.calibration(p),ZoneSizes.Zone.MAGAZINE))insert(p,s);
                else if(!s.shell&&!s.done)move(p,s,sample.local());
            }
            s.held=false;s.shell=false;return;
        }
        if(s.held||sample==null||!p.getOffhandItem().isEmpty())return;
        var target=Handling.target(phase(p),sample.local(),sample.pouch(),Profiles.get(s.stack),CompatNetwork.calibration(p));
        if(target==Handling.Target.POUCH&&!PouchAmmo.available(p,s.stack)){p.displayClientMessage(net.minecraft.network.chat.Component.literal("TaCZ VR: OUT OF AMMO"),true);return;}
        if(target==Handling.Target.RACK||target==Handling.Target.CASING||target==Handling.Target.POUCH){s.held=true;s.done=false;s.shell=target==Handling.Target.POUCH;s.loader=loader&&!DescriptorProfiles.details(Profiles.get(s.stack)).visual().loader().isEmpty();s.eject=target==Handling.Target.CASING;s.start=new Vector3f(sample.local());}
    }
    private static void insert(ServerPlayer p,State s){
        var api=new ModernKineticGunScriptAPI();api.setItemStack(s.stack);api.setShooter(p);api.setDataHolder(IGunOperator.fromLivingEntity(p).getDataHolder());
        if(api.getGunIndex()==null)return;
        var gun=IGun.getIGunOrNull(s.stack);int count=gun.getCurrentAmmoCount(s.stack);
        int capacity=com.tacz.guns.util.AttachmentDataUtils.getAmmoCountWithAttachment(s.stack,api.getGunIndex().getGunData());
        int spent=CylinderCycle.spent(s.stack.getOrCreateTag().getInt(SPENT),count,capacity);
        if(!CylinderCycle.canInsert(open(s.stack),count,spent,capacity))return;
        s.allowReload=true;boolean canceled;
        try{canceled=net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new com.tacz.guns.api.event.common.GunReloadEvent(p,s.stack,net.minecraftforge.fml.LogicalSide.SERVER));}finally{s.allowReload=false;}
        if(canceled||p.getMainHandItem()!=s.stack)return;
        // Recheck after extension callbacks. Never consume for a full/closed cylinder.
        capacity=com.tacz.guns.util.AttachmentDataUtils.getAmmoCountWithAttachment(s.stack,api.getGunIndex().getGunData());
        count=gun.getCurrentAmmoCount(s.stack);spent=CylinderCycle.spent(s.stack.getOrCreateTag().getInt(SPENT),count,capacity);if(!CylinderCycle.canInsert(open(s.stack),count,spent,capacity))return;
        int needed=CylinderCycle.loadCount(open(s.stack),count,spent,capacity,s.loader);
        int consumed=!api.isReloadingNeedConsumeAmmo()||api.getGunIndex().getGunData().getReloadData().isInfinite()?needed:api.consumeAmmoFromPlayer(needed);
        if(consumed>0){gun.setCurrentAmmoCount(s.stack,count+consumed);s.stack.getOrCreateTag().putInt(SPENT,spent);HandlingFeedback.emit(p,1,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();}
    }
}
