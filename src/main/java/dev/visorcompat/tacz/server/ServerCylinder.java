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
    private static class State {ItemStack stack;int slot,ticks,lastAmmo;boolean held,done,shell,eject,allowReload,loader;Vector3f start,lastPull;long pullTime;float pullSpeed,startAngle;BreakActionMotion breakMotion=new BreakActionMotion();CylinderMotion motion=new CylinderMotion();CylinderShake shake=new CylinderShake();Vector3f gestureRight;long lastPose;Phase sent;State(ServerPlayer p){stack=p.getMainHandItem();slot=p.getInventory().selected;var gun=IGun.getIGunOrNull(stack);lastAmmo=gun==null?0:gun.getCurrentAmmoCount(stack);}}
    public static boolean open(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(OPEN);}
    private static boolean valid(ServerPlayer p,State s){return s!=null&&s.stack==p.getMainHandItem()&&s.slot==p.getInventory().selected;}
    public static Phase phase(ServerPlayer p){var s=STATES.get(p.getUUID());return !valid(p,s)?Phase.READY:s.shell?(s.loader?Phase.LOADER:Phase.SHELL):s.held?Phase.CYLINDER_HOLD:open(s.stack)?Phase.CYLINDER_OPEN:Phase.READY;}
    public static boolean canFire(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)&&!s.held&&!open(s.stack)&&!GunDurabilityCompat.jammed(s.stack)&&IGun.getIGunOrNull(s.stack).getCurrentAmmoCount(s.stack)>0;}
    public static boolean allowsReload(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)&&s.allowReload;}
    public static void clear(ServerPlayer p){STATES.remove(p.getUUID());}
    public static void stop(){STATES.clear();}
    public static void cleanup(ServerPlayer p){var s=STATES.get(p.getUUID());if(s!=null&&(!valid(p,s)||!ServerPhysical.enabled(p)||!p.isAlive()||p.isSpectator()))clear(p);}
    public static void shot(ServerPlayer p,ItemStack stack){
        if(!ServerPhysical.enabled(p)||!Profiles.cylinder(stack))return;
        var s=STATES.get(p.getUUID());if(!valid(p,s))return;
        // The bullet constructor runs once per pellet, after TaCZ consumes the shell.
        int live=IGun.getIGunOrNull(stack).getCurrentAmmoCount(stack),used=Math.max(0,s.lastAmmo-live);
        if(used>0)stack.getOrCreateTag().putInt(SPENT,Math.min(64,stack.getOrCreateTag().getInt(SPENT)+used));
        s.lastAmmo=live;
    }
    public static void tick(ServerPlayer p){
        var s=STATES.computeIfAbsent(p.getUUID(),id->new State(p));var sample=ServerPhysical.sample(p);
        if(sample==null||!p.getOffhandItem().isEmpty()){s.held=false;s.shell=false;}
        if(s.held&&!s.shell&&!s.done&&sample!=null)move(p,s,sample.local());
        gestures(p,s);
        s.lastAmmo=IGun.getIGunOrNull(s.stack).getCurrentAmmoCount(s.stack);
        var phase=phase(p);if(phase!=s.sent||++s.ticks%10==0){CompatNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new CompatNetwork.PhysicalState(Profiles.key(s.stack),s.slot,phase,open(s.stack)?16:0));s.sent=phase;}
    }
    private static void move(ServerPlayer p,State s,Vector3f pos){
        var delta=new Vector3f(pos).sub(s.start);
        if(Profiles.get(s.stack).breakAction()&&!s.eject){
            float angle=grabAngle(p,s,pos);
            if(BreakActionMotion.rotated(s.startAngle,angle,open(s.stack))){toggleBreak(p,s,!open(s.stack));s.done=true;}
            return;
        }
        if(s.eject){
            long now=System.nanoTime();if(s.lastPull!=null){float dt=(now-s.pullTime)/1_000_000_000f;if(dt>=.01f){s.pullSpeed=CylinderEjection.pullSpeed(pos.z-s.lastPull.z,dt);s.lastPull.set(pos);s.pullTime=now;}}

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
        if(Profiles.get(s.stack).breakAction()){
            if(spent==0)return;
            s.stack.getOrCreateTag().putInt(SPENT,0);
            HandlingFeedback.cylinderEject(p,pose,0,spent,gravity,s.pullSpeed);p.inventoryMenu.broadcastChanges();return;
        }
        if(live+spent==0)return;
        gun.setCurrentAmmoCount(s.stack,0);s.stack.getOrCreateTag().putInt(SPENT,0);
        int recovered=0;
        boolean caught=false;
        if(ServerAdvanced.active(p)&&p.getOffhandItem().isEmpty()&&org.vmstudio.visor.api.VisorAPI.getVRPlayer(p) instanceof org.vmstudio.visor.api.server.player.VRServerPlayer vr){
            var center=new org.joml.Quaternionf(pose.rotation()).transform(Handling.magazine(Profiles.get(s.stack),CompatNetwork.calibration(p)).mul(pose.worldScale())).add(pose.hand());
            caught=CylinderEjection.canCatch(center,(ServerAdvanced.cylinderOffhand(p)?vr.getPoseData().getMainHand():vr.getPoseData().getOffhand()).getPosition(),pose.worldScale());
        }
        if(live>0&&CylinderEjection.recoverLive(ServerAdvanced.active(p),p.getOffhandItem().isEmpty(),caught)){
            var index=com.tacz.guns.api.TimelessAPI.getCommonGunIndex(gun.getGunId(s.stack)).orElse(null);
            if(index!=null&&IGunOperator.fromLivingEntity(p).needCheckAmmo()&&!index.getGunData().getReloadData().isInfinite()){
                if(gun.useDummyAmmo(s.stack)){int before=gun.getDummyAmmoAmount(s.stack);gun.addDummyAmmoAmount(s.stack,live);recovered=Math.max(0,gun.getDummyAmmoAmount(s.stack)-before);}
                else{var ammo=com.tacz.guns.api.item.builder.AmmoItemBuilder.create().setId(index.getGunData().getAmmoId()).setCount(live).build();p.getInventory().add(ammo);if(!ammo.isEmpty())p.drop(ammo,false);recovered=live;}
                p.displayClientMessage(net.minecraft.network.chat.Component.literal("Recovered "+recovered+" live rounds"),true);
            }
        }
        HandlingFeedback.cylinderEject(p,pose,live-recovered,spent,gravity,s.pullSpeed);p.inventoryMenu.broadcastChanges();
    }
    private static void gestures(ServerPlayer p,State s){
        var pose=ServerPoses.validated(p);
        if(!ServerPhysical.enabled(p)||pose==null||!(org.vmstudio.visor.api.VisorAPI.getVRPlayer(p) instanceof org.vmstudio.visor.api.server.player.VRServerPlayer vr)||!(vr instanceof FreshPose fresh)){
            s.motion.reset();s.breakMotion.reset();s.shake.reset();s.gestureRight=null;s.lastPose=0;return;
        }
        long time=fresh.visorTacz$lastPoseNanos();if(time==s.lastPose)return;
        var data=vr.getPoseData();
        if(s.gestureRight==null){s.gestureRight=data.getHmd().getRotation().transformDirection(new Vector3f(1,0,0));s.gestureRight.y=0;if(s.gestureRight.lengthSquared()<.1f)s.gestureRight.set(1,0,0);s.gestureRight.normalize();}
        float position=new Vector3f((ServerAdvanced.cylinderOffhand(p)?data.getOffhand():data.getMainHand()).getPosition()).sub(data.getHmd().getPosition()).dot(s.gestureRight)/pose.worldScale();
        float dt=s.lastPose==0?.05f:(time-s.lastPose)/1_000_000_000f;
        if(Profiles.get(s.stack).breakAction()){
            float pitch=(float)Math.asin(Math.max(-1,Math.min(1,pose.direction().y)));
            int action=s.breakMotion.update(pitch,dt,open(s.stack));if(action!=0&&!s.held)toggleBreak(p,s,action>0);
            s.lastPose=time;return;
        }
        if(!s.held&&Profiles.get(s.stack).mechanism()!=WeaponProfile.Mechanism.LONETRAIL){
            int action=s.motion.update(position,dt,open(s.stack));
            if(action!=0){s.stack.getOrCreateTag().putBoolean(OPEN,action>0);if(action<0)GunDurabilityCompat.clear(p,s.stack);HandlingFeedback.emit(p,action>0?2:5,pose);p.inventoryMenu.broadcastChanges();}
        }else s.motion.reset();
        float height=((ServerAdvanced.cylinderOffhand(p)?data.getOffhand():data.getMainHand()).getPosition().y()-data.getHmd().getPosition().y())/pose.worldScale();
        if(s.shake.update(height,dt,open(s.stack)&&!s.held&&CylinderCycle.fallsOut(pose.direction().y)))eject(p,s,true);
        s.lastPose=time;
    }
    public static void armOpen(ServerPlayer p){
        var s=STATES.get(p.getUUID());if(!valid(p,s)||!ServerPhysical.enabled(p)||!p.isAlive()||p.isSpectator()||ServerPoses.validated(p)==null||s.held)return;
        if(Profiles.get(s.stack).breakAction()){s.breakMotion.arm((float)Math.asin(Math.max(-1,Math.min(1,ServerPoses.validated(p).direction().y))));return;}
        if(open(s.stack))return;
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
        if(target==Handling.Target.RACK||target==Handling.Target.CASING||target==Handling.Target.POUCH){s.held=true;s.done=false;s.shell=target==Handling.Target.POUCH;s.loader=loader&&(Profiles.get(s.stack).doubleBarrel()||!DescriptorProfiles.details(Profiles.get(s.stack)).visual().loader().isEmpty());s.eject=target==Handling.Target.CASING;s.start=new Vector3f(sample.local());s.startAngle=grabAngle(p,s,sample.local());s.lastPull=new Vector3f(sample.local());s.pullTime=System.nanoTime();s.pullSpeed=0;}
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
        if(Profiles.get(s.stack).doubleBarrel())needed=Math.min(needed,s.loader?2:1);
        int consumed=!api.isReloadingNeedConsumeAmmo()||api.getGunIndex().getGunData().getReloadData().isInfinite()?needed:api.consumeAmmoFromPlayer(needed);
        if(consumed>0){gun.setCurrentAmmoCount(s.stack,count+consumed);s.lastAmmo=count+consumed;s.stack.getOrCreateTag().putInt(SPENT,spent);HandlingFeedback.emit(p,1,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();}
    }
    private static void toggleBreak(ServerPlayer p,State s,boolean opening){
        s.stack.getOrCreateTag().putBoolean(OPEN,opening);s.breakMotion.reset();
        if(opening){s.pullSpeed=.65f;eject(p,s,false);}else GunDurabilityCompat.clear(p,s.stack);
        HandlingFeedback.emit(p,opening?2:5,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();
    }
    private static float grabAngle(ServerPlayer p,State s,Vector3f local){
        if(Profiles.get(s.stack).doubleBarrel())return (float)Math.atan2(local.y-s.start.y,-local.z+.12f);
        var pose=ServerPoses.validated(p);
        if(pose==null||!(org.vmstudio.visor.api.VisorAPI.getVRPlayer(p) instanceof org.vmstudio.visor.api.server.player.VRServerPlayer vr))return Float.NaN;
        var hand=ServerAdvanced.cylinderOffhand(p)?vr.getPoseData().getMainHand():vr.getPoseData().getOffhand();
        // start TRY_MANUAL_FIX
        //var up=new org.joml.Quaternionf(pose.rotation()).conjugate().mul(hand.getRotation()).transform(new Vector3f(0,1,0));
        var handRotation = hand.getRotation().getUnnormalizedRotation(new org.joml.Quaternionf());
        var up = new org.joml.Quaternionf(pose.rotation()).conjugate().mul(handRotation).transform(new Vector3f(0,1,0));
        // end TRY_MANUAL_FIX
        return (float)Math.atan2(-up.x,up.y);
    }
}
