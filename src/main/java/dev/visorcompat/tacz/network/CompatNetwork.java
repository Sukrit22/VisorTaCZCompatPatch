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
            new ResourceLocation(VisorTacz.ID, "main"), () -> "25", "25"::equals, "25"::equals);
    private static final Set<UUID> DESCRIPTORS = new HashSet<>();
    private static final Set<UUID> MODE_KNOWN = new HashSet<>();
    private static final Set<UUID> TRANSFER_ANYTIME = new HashSet<>();
    private static final Set<UUID> PHYSICAL = new HashSet<>();
    private static final Set<UUID> ACTIVE = new HashSet<>();
    private static final Map<UUID, Grip> GRIPS = new HashMap<>();
    private CompatNetwork() {}

    public record Feedback(int kind,int entityId,ResourceLocation gun,ResourceLocation ammo,float x,float y,float z,float vx,float vy,float vz,float scale,float gunScale) {}
    public record MainAction(boolean transfer,boolean held,boolean canceled) {}
    public record PistolRelease() {}
    public record Advanced(boolean enabled,boolean held,int mount,int slot,String key,float x,float y,float z,float qx,float qy,float qz,float qw){
        public static Advanced disabled(){return new Advanced(false,false,0,-1,"",0,0,0,0,0,0,1);}
        public boolean valid(){float n=qx*qx+qy*qy+qz*qz+qw*qw;return mount>=0&&mount<=4&&(!held||mount==0)&&slot>=0&&slot<9&&Float.isFinite(x+y+z+n)&&n>.9f&&n<1.1f;}
        public static void write(Advanced s,net.minecraft.network.FriendlyByteBuf b){b.writeBoolean(s.enabled);b.writeBoolean(s.held);b.writeInt(s.mount);b.writeInt(s.slot);b.writeUtf(s.key,512);b.writeFloat(s.x);b.writeFloat(s.y);b.writeFloat(s.z);b.writeFloat(s.qx);b.writeFloat(s.qy);b.writeFloat(s.qz);b.writeFloat(s.qw);}
        public static Advanced read(net.minecraft.network.FriendlyByteBuf b){return new Advanced(b.readBoolean(),b.readBoolean(),b.readInt(),b.readInt(),b.readUtf(512),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat());}
    }
    public record AdvancedRemote(UUID player,Advanced state){}
    public record AdvancedUse(){}
    public record DroppedMagazine(net.minecraft.world.item.ItemStack stack,float scale,float modelScale,org.joml.Vector3f position,org.joml.Quaternionf rotation,org.joml.Vector3f velocity,org.joml.Vector3f spin,boolean loaded,boolean descriptors,String profileDigest){}
    public record BackendAck(boolean accepted){}
    public static void register() {
        CHANNEL.messageBuilder(BackendAck.class,13,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->b.writeBoolean(m.accepted())).decoder(b->new BackendAck(b.readBoolean()))
            .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.CompatSettings.backendAck(m.accepted()));c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(DroppedMagazine.class,12,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeItem(m.stack());b.writeFloat(m.scale());b.writeFloat(m.modelScale());b.writeVector3f(m.position());b.writeQuaternion(m.rotation());b.writeVector3f(m.velocity());b.writeVector3f(m.spin());b.writeBoolean(m.loaded());b.writeBoolean(m.descriptors());b.writeUtf(m.profileDigest(),64);})
            .decoder(b->new DroppedMagazine(b.readItem(),b.readFloat(),b.readFloat(),b.readVector3f(),b.readQuaternion(),b.readVector3f(),b.readVector3f(),b.readBoolean(),b.readBoolean(),b.readUtf(64)))
            .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.MagazineEffects.receive(m));c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(Advanced.class,9,NetworkDirection.PLAY_TO_SERVER).encoder(Advanced::write).decoder(Advanced::read)
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)dev.visorcompat.tacz.server.ServerAdvanced.receive(p,m);c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(AdvancedRemote.class,10,NetworkDirection.PLAY_TO_CLIENT).encoder((m,b)->{b.writeUUID(m.player());Advanced.write(m.state(),b);}).decoder(b->new AdvancedRemote(b.readUUID(),Advanced.read(b)))
            .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.AdvancedRemote.receive(m));c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(AdvancedUse.class,11,NetworkDirection.PLAY_TO_SERVER).encoder((m,b)->{}).decoder(b->new AdvancedUse())
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)dev.visorcompat.tacz.server.ServerAdvanced.use(p);c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(PistolRelease.class,8,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{}).decoder(b->new PistolRelease())
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)ServerPhysical.pistolRelease(p);c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(MainAction.class,7,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeBoolean(m.transfer());b.writeBoolean(m.held());b.writeBoolean(m.canceled());})
            .decoder(b->new MainAction(b.readBoolean(),b.readBoolean(),b.readBoolean()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null){if(m.transfer())ServerPhysical.transfer(p);else ServerPhysical.mainGrip(p,m.held(),m.canceled());}c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(Feedback.class,6,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeVarInt(m.kind());b.writeVarInt(m.entityId());b.writeResourceLocation(m.gun());b.writeResourceLocation(m.ammo());b.writeFloat(m.x());b.writeFloat(m.y());b.writeFloat(m.z());b.writeFloat(m.vx());b.writeFloat(m.vy());b.writeFloat(m.vz());b.writeFloat(m.scale());b.writeFloat(m.gunScale());})
            .decoder(b->new Feedback(b.readVarInt(),b.readVarInt(),b.readResourceLocation(),b.readResourceLocation(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat()))
            .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.HandlingEffects.receive(m));c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(RemoteState.class,5,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeUUID(m.player());b.writeUtf(m.key(),512);b.writeBoolean(m.active());b.writeBoolean(m.physical());
                b.writeEnum(m.phase());b.writeVarInt(m.pull());CalibrationCodec.write(b,m.calibration());b.writeBoolean(m.magazineLoaded());dev.visorcompat.tacz.HandAnchor.write(b,m.anchor());b.writeBoolean(m.descriptors());b.writeUtf(m.profileDigest(),64);})
            .decoder(b->new RemoteState(b.readUUID(),b.readUtf(512),b.readBoolean(),b.readBoolean(),b.readEnum(Phase.class),b.readVarInt(),CalibrationCodec.read(b),b.readBoolean(),dev.visorcompat.tacz.HandAnchor.read(b),b.readBoolean(),b.readUtf(64)))
            .consumerMainThread((m,c)->{
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    ()->()->dev.visorcompat.tacz.client.RemoteGuns.receive(m));c.get().setPacketHandled(true);
            }).add();
        CHANNEL.messageBuilder(PhysicalGrip.class,2,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeBoolean(m.held());b.writeBoolean(m.canceled());b.writeBoolean(m.loader());}).decoder(b->new PhysicalGrip(b.readBoolean(),b.readBoolean(),b.readBoolean()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null){if(m.canceled())ServerPhysical.cancelGrip(p);else if(dev.visorcompat.tacz.Profiles.cylinder(p.getMainHandItem()))dev.visorcompat.tacz.server.ServerCylinder.grip(p,m.held(),false,m.loader());else ServerPhysical.grip(p,m.held());}c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(PhysicalSelector.class,3,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{}).decoder(b->new PhysicalSelector())
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)ServerPhysical.selector(p);c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(PhysicalState.class,4,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeUtf(m.key(),512);b.writeVarInt(m.slot());b.writeEnum(m.phase());b.writeVarInt(m.pull());dev.visorcompat.tacz.HandAnchor.write(b,m.anchor());})
            .decoder(b->new PhysicalState(b.readUtf(512),b.readVarInt(),b.readEnum(Phase.class),b.readVarInt(),dev.visorcompat.tacz.HandAnchor.read(b)))
            .consumerMainThread((m,c)->{
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    ()->()->dev.visorcompat.tacz.client.PhysicalClient.receive(m));
                c.get().setPacketHandled(true);
            }).add();
        CHANNEL.messageBuilder(Grip.class, 1, NetworkDirection.PLAY_TO_SERVER)
            .encoder((message, buffer) -> {
                buffer.writeUtf(message.key(),512);
                CalibrationCodec.write(buffer,message.calibration());
            })
            .decoder(buffer -> new Grip(buffer.readUtf(512),CalibrationCodec.read(buffer)))
            .consumerMainThread((message,context) -> {
                ServerPlayer player=context.get().getSender();
                if(player!=null && message.calibration().valid()) GRIPS.put(player.getUUID(),message);
                context.get().setPacketHandled(true);
            }).add();
        CHANNEL.messageBuilder(Mode.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> {buffer.writeBoolean(message.enabled()).writeBoolean(message.physical()).writeBoolean(message.transferAnytime()).writeBoolean(message.descriptors());buffer.writeUtf(message.digest(),64);})
                .decoder(buffer -> new Mode(buffer.readBoolean(),buffer.readBoolean(),buffer.readBoolean(),buffer.readBoolean(),buffer.readUtf(64)))
                .consumerMainThread((message, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        boolean backend=message.descriptors()&&!dev.visorcompat.tacz.DescriptorProfiles.digest().equals("invalid")&&message.digest().equals(dev.visorcompat.tacz.DescriptorProfiles.digest());
                        if(backend!=descriptors(player)){ServerPhysical.clear(player);dev.visorcompat.tacz.server.ServerAdvanced.clear(player);}
                        if(backend)DESCRIPTORS.add(player.getUUID());else DESCRIPTORS.remove(player.getUUID());
                        if(message.descriptors())CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->player),new BackendAck(backend));
                        MODE_KNOWN.add(player.getUUID());
                        if(message.transferAnytime())TRANSFER_ANYTIME.add(player.getUUID());else TRANSFER_ANYTIME.remove(player.getUUID());
                        if (backend && message.enabled() && message.physical()) PHYSICAL.add(player.getUUID());
                        else { PHYSICAL.remove(player.getUUID());ServerPhysical.clear(player); }
                        if (message.enabled()&&backend) ACTIVE.add(player.getUUID());
                        else {ACTIVE.remove(player.getUUID());dev.visorcompat.tacz.server.ServerAdvanced.clear(player);}
                    }
                    context.get().setPacketHandled(true);
                }).add();
    }
    public record RemoteState(UUID player,String key,boolean active,boolean physical,Phase phase,int pull,Calibration calibration,boolean magazineLoaded,dev.visorcompat.tacz.HandAnchor anchor,boolean descriptors,String profileDigest) {}
    public record Grip(String key, Calibration calibration) {}
    public static Calibration calibration(ServerPlayer player) {
        Grip grip = GRIPS.get(player.getUUID());
        return grip != null && grip.key().equals(Profiles.key(player.getMainHandItem()))
            ? grip.calibration() : Calibration.ZERO;
    }
    public record PhysicalGrip(boolean held,boolean canceled,boolean loader) {public PhysicalGrip(boolean held){this(held,false,true);}public PhysicalGrip(boolean held,boolean canceled){this(held,canceled,true);}}
    public record PhysicalSelector() {}
    public record PhysicalState(String key,int slot,Phase phase,int pull,dev.visorcompat.tacz.HandAnchor anchor) {public PhysicalState(String key,int slot,Phase phase,int pull){this(key,slot,phase,pull,null);}}
    public static boolean modeKnown(ServerPlayer player){return MODE_KNOWN.contains(player.getUUID());}
    public static boolean physical(ServerPlayer player) { return PHYSICAL.contains(player.getUUID()); }
    public static boolean transferAnytime(ServerPlayer p){return TRANSFER_ANYTIME.contains(p.getUUID());}
    public static boolean descriptors(ServerPlayer p){return DESCRIPTORS.contains(p.getUUID());}
    public record Mode(boolean enabled, boolean physical,boolean transferAnytime,boolean descriptors,String digest) {}
    public static boolean enabled(ServerPlayer player) { return ACTIVE.contains(player.getUUID()); }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) ServerPhysical.recoverInventory(player);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer p)dev.visorcompat.tacz.server.ServerAdvanced.clear(p);
        if(event.getEntity() instanceof ServerPlayer player) ServerPhysical.clear(player);
        DESCRIPTORS.remove(event.getEntity().getUUID());MODE_KNOWN.remove(event.getEntity().getUUID());
        PHYSICAL.remove(event.getEntity().getUUID());TRANSFER_ANYTIME.remove(event.getEntity().getUUID());
        ACTIVE.remove(event.getEntity().getUUID());
        GRIPS.remove(event.getEntity().getUUID());
        dev.visorcompat.tacz.server.RemoteSync.remove(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { dev.visorcompat.tacz.server.ServerAdvanced.clearAll();DESCRIPTORS.clear();MODE_KNOWN.clear(); TRANSFER_ANYTIME.clear(); ACTIVE.clear(); PHYSICAL.clear(); GRIPS.clear(); ServerPhysical.stop(); dev.visorcompat.tacz.server.RemoteSync.clear(); }
}
