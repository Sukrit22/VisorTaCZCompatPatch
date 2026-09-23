package dev.visorcompat.tacz.server;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;
public final class RemoteSync {
    private static final Map<UUID,CompatNetwork.RemoteState> LAST=new HashMap<>();
    public static void tick(ServerPlayer p) {
        boolean active=ServerPoses.isVr(p) && p.isAlive() && Profiles.get(p.getMainHandItem())!=null;
        var state=new CompatNetwork.RemoteState(p.getUUID(),Profiles.key(p.getMainHandItem()),active,
            ServerPhysical.enabled(p),ServerPhysical.phase(p),ServerPhysical.pull(p),CompatNetwork.calibration(p),active && dev.visorcompat.tacz.physical.PouchAmmo.magazineLoaded(p,ServerPhysical.phase(p)),ServerPhysical.anchor(p),CompatNetwork.descriptors(p),CompatNetwork.descriptors(p)?DescriptorProfiles.digest():"");
        var last=LAST.get(p.getUUID());
        if((active || last!=null && last.active()) && (!state.equals(last) || p.tickCount%20==0))
            CompatNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(()->p),state);
        if(active)LAST.put(p.getUUID(),state);else LAST.remove(p.getUUID());
    }
    public static void remove(UUID id) {LAST.remove(id);}
    public static void clear() {LAST.clear();}
}
