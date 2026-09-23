package dev.visorcompat.tacz.server;
import java.util.*;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.*;
import dev.visorcompat.tacz.physical.Handling.Phase;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.compat.GunDurabilityCompat;
import com.tacz.guns.api.item.*;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.TimelessAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.PacketDistributor;

public final class ServerPump {
    public static final String OPEN="visor_tacz_pump_open", SPENT="visor_tacz_pump_spent";
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static class State {HandAnchor anchor;float capturedTravel;org.joml.Vector3f previousHand,velocity=new org.joml.Vector3f();InertialPump inertia=new InertialPump(0,0);ItemStack stack;int slot,pull;boolean held,shell;Phase sent;int sentPull=-1;State(ServerPlayer p){stack=p.getMainHandItem();slot=p.getInventory().selected;}}
    public static boolean open(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(OPEN);}
    public static boolean spent(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(SPENT);}
    public static void shot(ServerPlayer p,ItemStack stack){if(ServerPhysical.enabled(p)&&Profiles.pump(stack))stack.getOrCreateTag().putBoolean(SPENT,true);}
    private static boolean valid(ServerPlayer p,State s){return s!=null && s.stack==p.getMainHandItem()&&s.slot==p.getInventory().selected;}
    public static Phase phase(ServerPlayer p){
        var s=STATES.get(p.getUUID());if(!valid(p,s))return Phase.READY;
        if(s.shell)return Phase.SHELL;if(s.held)return Phase.PUMP_HOLD;
        if(open(s.stack))return Phase.PUMP_OPEN;
        return IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack)&&!spent(s.stack)?Phase.READY:Phase.NEED_RACK;
    }
    public static int pull(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)?s.pull:0;}
    public static HandAnchor anchor(ServerPlayer p){var s=STATES.get(p.getUUID());if(!valid(p,s)||s.anchor==null)return null;
        var shift=new org.joml.Quaternionf(s.anchor.rotation()).transform(new org.joml.Vector3f(0,0,s.capturedTravel-s.inertia.travel()));
        return new HandAnchor(new org.joml.Quaternionf(s.anchor.rotation()),new org.joml.Vector3f(s.anchor.offset()).add(shift));
    }
    public static boolean supporting(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)&&s.anchor==null&&s.held&&!s.shell&&!open(s.stack)&&s.pull<4;}
    public static boolean canFire(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)&&s.anchor==null&&!s.shell&&!open(s.stack)&&!spent(s.stack)&&s.pull<4&&!GunDurabilityCompat.jammed(s.stack)&&IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack);}
    public static void clear(ServerPlayer p){STATES.remove(p.getUUID());}
    public static void stop(){STATES.clear();}
    public static void cleanup(ServerPlayer p){var s=STATES.get(p.getUUID());if(s!=null&&(!valid(p,s)||!ServerPhysical.enabled(p)||!p.isAlive()||p.isSpectator()))clear(p);}
    public static void tick(ServerPlayer p){
        var s=STATES.computeIfAbsent(p.getUUID(),id->new State(p));var sample=ServerPhysical.sample(p);
        if(s.held&&(sample==null||!p.getOffhandItem().isEmpty())){s.held=false;s.shell=false;s.anchor=null;}
        if(s.held&&!s.shell&&sample!=null){if(s.anchor!=null)moveInertia(p,s);else move(p,s,sample);}
        if(!s.held)s.pull=open(s.stack)?16:0;
        var phase=phase(p);
        if(phase!=s.sent||s.pull!=s.sentPull||s.anchor!=null){CompatNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new CompatNetwork.PhysicalState(Profiles.key(s.stack),s.slot,phase,s.pull,anchor(p)));s.sent=phase;s.sentPull=s.pull;}
    }
    private static void move(ServerPlayer p,State s,ServerPhysical.Sample sample){
        var rack=Handling.rack(Profiles.get(s.stack),CompatNetwork.calibration(p));var pos=sample.local();
        float travel=pos.z-rack.z,side=pos.x-rack.x,vertical=pos.y-rack.y;
        s.pull=Math.max(0,Math.min(16,Math.round(travel*200)));
        stroke(p,s,travel,side,vertical);
    }
    private static void stroke(ServerPlayer p,State s,float travel,float side,float vertical){
        var gun=IGun.getIGunOrNull(s.stack);
        var transition=PumpCycle.transition(open(s.stack),travel,side,vertical);
        if(transition==PumpCycle.Transition.OPEN){
            int effect=spent(s.stack)?4:gun.hasBulletInBarrel(s.stack)?3:2;
            gun.setBulletInBarrel(s.stack,false);s.stack.getOrCreateTag().putBoolean(OPEN,true);s.stack.getTag().putBoolean(SPENT,false);
            HandlingFeedback.emit(p,effect,ServerPoses.validated(p));
        } else if(transition==PumpCycle.Transition.CLOSE){
            // Pump completion clears only the optional generic jam, never creates Glock/M4 jam types.
            GunDurabilityCompat.clear(p,s.stack);
            if(!GunDurabilityCompat.jammed(s.stack)){
                var chamber=PumpCycle.close(gun.getCurrentAmmoCount(s.stack),gun.hasBulletInBarrel(s.stack));
                gun.setCurrentAmmoCount(s.stack,chamber.magazine());gun.setBulletInBarrel(s.stack,chamber.loaded());
                s.stack.getOrCreateTag().putBoolean(OPEN,false);s.pull=0;HandlingFeedback.emit(p,5,ServerPoses.validated(p));
            }
        }
    }
    public static void grip(ServerPlayer p,boolean held,boolean canceled){
        var s=STATES.get(p.getUUID());if(!valid(p,s)||!ServerPhysical.enabled(p)||!p.isAlive()||p.isSpectator())return;
        var sample=ServerPhysical.sample(p);
        if(!held){
            if(s.held&&!canceled&&sample!=null&&p.getOffhandItem().isEmpty()){
                if(s.shell){
                    var c=CompatNetwork.calibration(p);var profile=Profiles.get(s.stack);
                    boolean port=open(s.stack)&&Handling.inside(sample.local(),JamProfile.of(profile,c).port(),c,ZoneSizes.Zone.PORT);
                    if(port || !open(s.stack)&&Handling.inside(sample.local(),Handling.magazine(profile,c),c,ZoneSizes.Zone.MAGAZINE))insert(p,s,port);
                }
                else if(!s.shell&&s.anchor==null)move(p,s,sample);
            }
            s.held=false;s.shell=false;s.anchor=null;s.sent=null;return;
        }
        if(s.held||sample==null||!p.getOffhandItem().isEmpty())return;
        var target=Handling.target(phase(p),sample.local(),sample.pouch(),Profiles.get(s.stack),CompatNetwork.calibration(p));
        if(target==Handling.Target.RACK){s.held=true;s.shell=false;}
        // This is only a preview. hasInventoryAmmo is for inventory-fed guns
        // and always rejects the M870; insertion below validates/consumes ammo.
        if(target==Handling.Target.POUCH){
            if(!PouchAmmo.available(p,s.stack)){p.displayClientMessage(net.minecraft.network.chat.Component.literal("TaCZ VR: OUT OF AMMO"),true);return;}
            s.held=true;s.shell=true;
        }
    }
    public static void transfer(ServerPlayer p){
        var s=STATES.get(p.getUUID());if(!valid(p,s)||!ServerPhysical.enabled(p)||!s.held||s.shell||!p.getOffhandItem().isEmpty())return;
        var gun=ServerPoses.validated(p);if(gun==null||!(org.vmstudio.visor.api.VisorAPI.getVRPlayer(p) instanceof org.vmstudio.visor.api.server.player.VRServerPlayer vr))return;
        if(s.anchor!=null){
            var main=vr.getPoseData().getMainHand();var q=main.getRotation().getNormalizedRotation(new org.joml.Quaternionf());
            var grip=CompatNetwork.calibration(p).position(new org.joml.Vector3f(main.getPosition()),q,gun.worldScale());
            if(grip.distance(gun.hand())>.20f*gun.worldScale())return;
            s.anchor=null;s.sent=null;
        }else{
            s.anchor=HandAnchor.capture(gun,vr.getPoseData());s.capturedTravel=s.pull*.005f;
            s.inertia=new InertialPump(s.capturedTravel,0);s.previousHand=new org.joml.Vector3f(vr.getPoseData().getOffhand().getPosition());s.velocity.zero();
        }
    }
    private static void moveInertia(ServerPlayer p,State s){
        var vr=(org.vmstudio.visor.api.server.player.VRServerPlayer)org.vmstudio.visor.api.VisorAPI.getVRPlayer(p);
        var pose=vr.getPoseData();var hand=new org.joml.Vector3f(pose.getOffhand().getPosition());
        var velocity=new org.joml.Vector3f(hand).sub(s.previousHand).div(.05f*pose.getWorldScale());
        if(velocity.length()>6)velocity.normalize(6);
        var gun=anchor(p).resolve(pose,Profiles.get(s.stack),CompatNetwork.calibration(p));if(gun==null)return;
        var inverse=new org.joml.Quaternionf(gun.rotation()).conjugate();
        float impulse=inverse.transform(new org.joml.Vector3f(velocity).sub(s.velocity)).z;
        float gravity=new org.joml.Quaternionf(inverse).transform(new org.joml.Vector3f(0,-9.81f,0)).z;
        s.inertia=s.inertia.step(impulse,gravity,.05f);s.previousHand=hand;s.velocity.set(velocity);
        s.pull=Math.round(s.inertia.travel()*200);stroke(p,s,s.inertia.travel(),0,0);
    }
    private static void insert(ServerPlayer p,State s,boolean port){
        if(open(s.stack)!=port)return;
        var gun=(AbstractGunItem)s.stack.getItem();var index=TimelessAPI.getCommonGunIndex(gun.getGunId(s.stack)).orElse(null);if(index==null)return;
        int capacity=com.tacz.guns.util.AttachmentDataUtils.getAmmoCountWithAttachment(s.stack,index.getGunData());
        int count=gun.getCurrentAmmoCount(s.stack);if(port?gun.hasBulletInBarrel(s.stack):count>=capacity)return;
        int consumed;
        if(!IGunOperator.fromLivingEntity(p).needCheckAmmo())consumed=1;
        else if(gun.useDummyAmmo(s.stack))consumed=gun.findAndExtractDummyAmmo(s.stack,1);
        else consumed=p.getCapability(ForgeCapabilities.ITEM_HANDLER).map(cap->gun.findAndExtractInventoryAmmo(cap,s.stack,1)).orElse(0);
        if(port?ActionCycle.portLoad(open(s.stack),gun.hasBulletInBarrel(s.stack),consumed):PumpCycle.canInsert(count,capacity,consumed)){if(port)gun.setBulletInBarrel(s.stack,true);else gun.setCurrentAmmoCount(s.stack,count+1);HandlingFeedback.emit(p,1,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();}
    }
}
