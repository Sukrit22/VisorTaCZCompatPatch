package dev.visorcompat.tacz.network;

import dev.visorcompat.tacz.VisorTacz;
import dev.visorcompat.tacz.Calibration;
import dev.visorcompat.tacz.Profiles;
import dev.visorcompat.tacz.server.ServerPhysical;
import dev.visorcompat.tacz.physical.Handling.Phase;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = VisorTacz.ID)
public final class CompatNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VisorTacz.ID, "main"), () -> "10", "10"::equals, "10"::equals);
    private static final Set<UUID> PHYSICAL = new HashSet<>();
    private static final Set<UUID> ACTIVE = new HashSet<>();
    private static final Map<UUID, Grip> GRIPS = new HashMap<>();
    private CompatNetwork() {}

    public record Feedback(int kind,int entityId,ResourceLocation gun,ResourceLocation ammo,float x,float y,float z,float vx,float vy,float vz,float scale) {}
    public static void register() {
        CHANNEL.messageBuilder(Feedback.class,6,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeVarInt(m.kind());b.writeVarInt(m.entityId());b.writeResourceLocation(m.gun());b.writeResourceLocation(m.ammo());b.writeFloat(m.x());b.writeFloat(m.y());b.writeFloat(m.z());b.writeFloat(m.vx());b.writeFloat(m.vy());b.writeFloat(m.vz());b.writeFloat(m.scale());})
            .decoder(b->new Feedback(b.readVarInt(),b.readVarInt(),b.readResourceLocation(),b.readResourceLocation(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat()))
            .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.HandlingEffects.receive(m));c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(RemoteState.class,5,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeUUID(m.player());b.writeUtf(m.key(),512);b.writeBoolean(m.active());b.writeBoolean(m.physical());
                b.writeEnum(m.phase());b.writeVarInt(m.pull());writeCalibration(b,m.calibration());})
            .decoder(b->new RemoteState(b.readUUID(),b.readUtf(512),b.readBoolean(),b.readBoolean(),b.readEnum(Phase.class),b.readVarInt(),readCalibration(b)))
            .consumerMainThread((m,c)->{
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    ()->()->dev.visorcompat.tacz.client.RemoteGuns.receive(m));c.get().setPacketHandled(true);
            }).add();
        CHANNEL.messageBuilder(PhysicalGrip.class,2,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeBoolean(m.held());b.writeBoolean(m.canceled());}).decoder(b->new PhysicalGrip(b.readBoolean(),b.readBoolean()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null){if(m.canceled())ServerPhysical.cancelGrip(p);else ServerPhysical.grip(p,m.held());}c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(PhysicalSelector.class,3,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{}).decoder(b->new PhysicalSelector())
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)ServerPhysical.selector(p);c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(PhysicalState.class,4,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeUtf(m.key(),512);b.writeVarInt(m.slot());b.writeEnum(m.phase());b.writeVarInt(m.pull());})
            .decoder(b->new PhysicalState(b.readUtf(512),b.readVarInt(),b.readEnum(Phase.class),b.readVarInt()))
            .consumerMainThread((m,c)->{
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    ()->()->dev.visorcompat.tacz.client.PhysicalClient.receive(m));
                c.get().setPacketHandled(true);
            }).add();
        CHANNEL.messageBuilder(Grip.class, 1, NetworkDirection.PLAY_TO_SERVER)
            .encoder((message, buffer) -> {
                buffer.writeUtf(message.key(),512);
                writeCalibration(buffer,message.calibration());
            })
            .decoder(buffer -> new Grip(buffer.readUtf(512),readCalibration(buffer)))
            .consumerMainThread((message,context) -> {
                ServerPlayer player=context.get().getSender();
                if(player!=null && message.calibration().valid()) GRIPS.put(player.getUUID(),message);
                context.get().setPacketHandled(true);
            }).add();
        CHANNEL.messageBuilder(Mode.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> buffer.writeBoolean(message.enabled()).writeBoolean(message.physical()))
                .decoder(buffer -> new Mode(buffer.readBoolean(),buffer.readBoolean()))
                .consumerMainThread((message, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        if (message.enabled() && message.physical()) PHYSICAL.add(player.getUUID());
                        else { PHYSICAL.remove(player.getUUID());ServerPhysical.clear(player); }
                        if (message.enabled()) ACTIVE.add(player.getUUID());
                        else ACTIVE.remove(player.getUUID());
                    }
                    context.get().setPacketHandled(true);
                }).add();
    }
    private static void writeCalibration(net.minecraft.network.FriendlyByteBuf b,Calibration c) {
        b.writeFloat(c.x());b.writeFloat(c.y());b.writeFloat(c.z());b.writeFloat(c.pitch());b.writeFloat(c.yaw());b.writeFloat(c.roll());
        b.writeFloat(c.muzzleX());b.writeFloat(c.muzzleY());b.writeFloat(c.muzzleZ());
        for(var point:c.interactions().points()){b.writeFloat(point.x());b.writeFloat(point.y());b.writeFloat(point.z());}
    }
    private static Calibration readCalibration(net.minecraft.network.FriendlyByteBuf b) {
        float[] v=new float[9];for(int i=0;i<9;i++)v[i]=b.readFloat();
        var p=new dev.visorcompat.tacz.InteractionOffsets.Point[7];
        for(int i=0;i<7;i++)p[i]=new dev.visorcompat.tacz.InteractionOffsets.Point(b.readFloat(),b.readFloat(),b.readFloat());
        return new Calibration(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],
            new dev.visorcompat.tacz.InteractionOffsets(p[0],p[1],p[2],p[3],p[4],p[5],p[6]));
    }
    public record RemoteState(UUID player,String key,boolean active,boolean physical,Phase phase,int pull,Calibration calibration) {}
    public record Grip(String key, Calibration calibration) {}
    public static Calibration calibration(ServerPlayer player) {
        Grip grip = GRIPS.get(player.getUUID());
        return grip != null && grip.key().equals(Profiles.key(player.getMainHandItem()))
            ? grip.calibration() : Calibration.ZERO;
    }
    public record PhysicalGrip(boolean held,boolean canceled) {public PhysicalGrip(boolean held){this(held,false);}}
    public record PhysicalSelector() {}
    public record PhysicalState(String key,int slot,Phase phase,int pull) {}
    public static boolean physical(ServerPlayer player) { return PHYSICAL.contains(player.getUUID()); }
    public record Mode(boolean enabled, boolean physical) {}
    public static boolean enabled(ServerPlayer player) { return ACTIVE.contains(player.getUUID()); }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) ServerPhysical.recoverInventory(player);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) ServerPhysical.clear(player);
        PHYSICAL.remove(event.getEntity().getUUID());
        ACTIVE.remove(event.getEntity().getUUID());
        GRIPS.remove(event.getEntity().getUUID());
        dev.visorcompat.tacz.server.RemoteSync.remove(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { ACTIVE.clear(); PHYSICAL.clear(); GRIPS.clear(); ServerPhysical.stop(); dev.visorcompat.tacz.server.RemoteSync.clear(); }
}
