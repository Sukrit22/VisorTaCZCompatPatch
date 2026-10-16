package dev.visorcompat.tacz.server;

import dev.visorcompat.tacz.Profiles;
import dev.visorcompat.tacz.compat.GunDurabilityCompat;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.network.ButtonJamNetwork;
import dev.visorcompat.tacz.physical.ButtonJamHold;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ServerButtonJams {
    private record Session(ItemStack stack,int slot,String key,ButtonJamHold hold){}
    private static final java.util.Map<java.util.UUID,Session> STATES=new java.util.HashMap<>();
    private static boolean eligible(ServerPlayer p){return ServerPoses.isVr(p)&&!CompatNetwork.physical(p)&&!ServerAdvanced.active(p)&&!ServerDual.active(p)&&p.isAlive()&&!p.isSpectator()&&!com.tacz.guns.api.entity.IGunOperator.fromLivingEntity(p).getSynReloadState().getStateType().isReloading()&&Profiles.get(p.getMainHandItem())!=null&&GunDurabilityCompat.supported()&&GunDurabilityCompat.jammed(p.getMainHandItem())&&ServerPoses.validated(p)!=null;}
    public static boolean clearing(ServerPlayer p){return STATES.containsKey(p.getUUID());}
    public static void receive(ServerPlayer p,ButtonJamNetwork.Hold m){
        if(!m.held()){cancel(p);return;}
        if(!eligible(p)||m.slot()!=p.getInventory().selected||!m.key().equals(Profiles.key(p.getMainHandItem()))){cancel(p);ButtonJamNetwork.progress(p,m.slot(),m.key(),0,false);return;}
        var old=STATES.get(p.getUUID());
        if(old!=null&&(old.stack()!=p.getMainHandItem()||old.slot()!=m.slot()||!old.key().equals(m.key()))){cancel(p);return;}
        long tick=p.level().getGameTime();
        if(old==null){old=new Session(p.getMainHandItem(),m.slot(),m.key(),new ButtonJamHold(tick));STATES.put(p.getUUID(),old);}
        old.hold().refresh(tick);
    }
    private static void cancel(ServerPlayer p){var s=STATES.remove(p.getUUID());if(s!=null)ButtonJamNetwork.progress(p,s.slot(),s.key(),0,false);}
    public static void tick(net.minecraftforge.event.TickEvent.PlayerTickEvent event){
        if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END||!(event.player instanceof ServerPlayer p))return;
        var s=STATES.get(p.getUUID());if(s==null)return;
        long tick=p.level().getGameTime();
        if(!eligible(p)||s.stack()!=p.getMainHandItem()||s.slot()!=p.getInventory().selected||!s.hold().alive(tick)){cancel(p);return;}
        if(s.hold().complete(tick)){
            if(GunDurabilityCompat.clear(p,s.stack()))ServerJams.reconcile(p,s.stack());
            cancel(p);return;
        }
        ButtonJamNetwork.progress(p,s.slot(),s.key(),s.hold().elapsed(tick),true);
    }
    public static void shoot(com.tacz.guns.api.event.common.GunShootEvent e){if(e.getLogicalSide().isServer()&&e.getShooter() instanceof ServerPlayer p&&clearing(p))e.setCanceled(true);}
    public static void fire(com.tacz.guns.api.event.common.GunFireEvent e){if(e.getLogicalSide().isServer()&&e.getShooter() instanceof ServerPlayer p&&clearing(p))e.setCanceled(true);}
    public static void reload(com.tacz.guns.api.event.common.GunReloadEvent e){if(e.getLogicalSide().isServer()&&e.getEntity() instanceof ServerPlayer p&&clearing(p))e.setCanceled(true);}
    public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){STATES.remove(e.getEntity().getUUID());}
    public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent e){STATES.clear();}
}


