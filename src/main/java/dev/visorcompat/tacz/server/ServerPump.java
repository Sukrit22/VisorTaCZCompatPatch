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
    private static class State {ItemStack stack;int slot,pull;boolean held,shell;Phase sent;int sentPull=-1;State(ServerPlayer p){stack=p.getMainHandItem();slot=p.getInventory().selected;}}
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
    public static boolean supporting(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)&&s.held&&!s.shell&&!open(s.stack)&&s.pull<4;}
    public static boolean canFire(ServerPlayer p){var s=STATES.get(p.getUUID());return valid(p,s)&&!s.shell&&!open(s.stack)&&!spent(s.stack)&&s.pull<4&&!GunDurabilityCompat.jammed(s.stack)&&IGun.getIGunOrNull(s.stack).hasBulletInBarrel(s.stack);}
    public static void clear(ServerPlayer p){STATES.remove(p.getUUID());}
    public static void stop(){STATES.clear();}
    public static void cleanup(ServerPlayer p){var s=STATES.get(p.getUUID());if(s!=null&&(!valid(p,s)||!ServerPhysical.enabled(p)||!p.isAlive()||p.isSpectator()))clear(p);}
    public static void tick(ServerPlayer p){
        var s=STATES.computeIfAbsent(p.getUUID(),id->new State(p));var sample=ServerPhysical.sample(p);
        if(s.held&&(sample==null||!p.getOffhandItem().isEmpty())){s.held=false;s.shell=false;}
        if(s.held&&!s.shell&&sample!=null)move(p,s,sample);
        if(!s.held)s.pull=open(s.stack)?16:0;
        var phase=phase(p);
        if(phase!=s.sent||s.pull!=s.sentPull){CompatNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new CompatNetwork.PhysicalState(Profiles.key(s.stack),s.slot,phase,s.pull));s.sent=phase;s.sentPull=s.pull;}
    }
    private static void move(ServerPlayer p,State s,ServerPhysical.Sample sample){
        var rack=Handling.rack(Profiles.get(s.stack),CompatNetwork.calibration(p));var pos=sample.local();
        float travel=pos.z-rack.z,side=pos.x-rack.x,vertical=pos.y-rack.y;
        s.pull=Math.max(0,Math.min(16,Math.round(travel*200)));
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
                if(s.shell && Handling.near(sample.local(),Handling.magazine(Profiles.get(s.stack),CompatNetwork.calibration(p)),.085f))insert(p,s);
                else if(!s.shell)move(p,s,sample);
            }
            s.held=false;s.shell=false;return;
        }
        if(s.held||sample==null||!p.getOffhandItem().isEmpty())return;
        var target=Handling.target(phase(p),sample.local(),sample.pouch(),Profiles.get(s.stack),CompatNetwork.calibration(p));
        if(target==Handling.Target.RACK){s.held=true;s.shell=false;}
        // This is only a preview. hasInventoryAmmo is for inventory-fed guns
        // and always rejects the M870; insertion below validates/consumes ammo.
        if(target==Handling.Target.POUCH){s.held=true;s.shell=true;}
    }
    private static void insert(ServerPlayer p,State s){
        var gun=(AbstractGunItem)s.stack.getItem();var index=TimelessAPI.getCommonGunIndex(gun.getGunId(s.stack)).orElse(null);if(index==null)return;
        int capacity=com.tacz.guns.util.AttachmentDataUtils.getAmmoCountWithAttachment(s.stack,index.getGunData());
        int count=gun.getCurrentAmmoCount(s.stack);if(count>=capacity)return;
        int consumed;
        if(!IGunOperator.fromLivingEntity(p).needCheckAmmo())consumed=1;
        else if(gun.useDummyAmmo(s.stack))consumed=gun.findAndExtractDummyAmmo(s.stack,1);
        else consumed=p.getCapability(ForgeCapabilities.ITEM_HANDLER).map(cap->gun.findAndExtractInventoryAmmo(cap,s.stack,1)).orElse(0);
        if(PumpCycle.canInsert(count,capacity,consumed)){gun.setCurrentAmmoCount(s.stack,count+1);HandlingFeedback.emit(p,1,ServerPoses.validated(p));p.inventoryMenu.broadcastChanges();}
    }
}
