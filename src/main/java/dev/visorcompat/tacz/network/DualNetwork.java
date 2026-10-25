package dev.visorcompat.tacz.network;
import dev.visorcompat.tacz.Calibration;
import net.minecraftforge.network.NetworkDirection;
/** Explicit per-hand input; no native selected-slot mutation. */
public final class DualNetwork {
 private static long sequence;
 public record Input(long sequence,boolean enabled,int mainSlot,int offSlot,boolean mainHeld,boolean offHeld,boolean mainTrigger,boolean offTrigger,boolean mainReload,boolean offReload,boolean mainAim,boolean offAim,boolean mainJam,boolean offJam,boolean physical,int mainGrip,int offGrip,Calibration mainCalibration,Calibration offCalibration,dev.visorcompat.tacz.GunPose mainPose,dev.visorcompat.tacz.GunPose offPose){}
 public static void send(boolean enabled,int mainSlot,int offSlot,boolean mainHeld,boolean offHeld,boolean mainTrigger,boolean offTrigger,boolean mainReload,boolean offReload,boolean mainAim,boolean offAim,boolean mainJam,boolean offJam,boolean physical,int mainGrip,int offGrip,Calibration mainCalibration,Calibration offCalibration){
  CompatNetwork.CHANNEL.sendToServer(new Input(++sequence,enabled,mainSlot,offSlot,mainHeld,offHeld,mainTrigger,offTrigger,mainReload,offReload,mainAim,offAim,mainJam,offJam,physical,mainGrip,offGrip,mainCalibration,offCalibration,enabled?dev.visorcompat.tacz.client.DualClient.pose(org.vmstudio.visor.api.common.HandType.MAIN,org.vmstudio.visor.api.client.player.pose.PlayerPoseType.TICK):null,enabled?dev.visorcompat.tacz.client.DualClient.pose(org.vmstudio.visor.api.common.HandType.OFFHAND,org.vmstudio.visor.api.client.player.pose.PlayerPoseType.TICK):null));
 }
 public record Visual(java.util.UUID player,boolean enabled,net.minecraft.world.item.ItemStack main,net.minecraft.world.item.ItemStack off,dev.visorcompat.tacz.GunPose mainPose,dev.visorcompat.tacz.GunPose offPose,Calibration mainCal,Calibration offCal){}
 private static void writePose(net.minecraft.network.FriendlyByteBuf b,dev.visorcompat.tacz.GunPose p){b.writeBoolean(p!=null);if(p==null)return;b.writeVector3f(p.hand());b.writeQuaternion(p.rotation());b.writeFloat(p.worldScale());}
 private static dev.visorcompat.tacz.GunPose readPose(net.minecraft.network.FriendlyByteBuf b){if(!b.readBoolean())return null;var pos=b.readVector3f();var q=b.readQuaternion();float scale=b.readFloat();float n=q.lengthSquared();if(!dev.visorcompat.tacz.PoseMath.finite(pos)||!Float.isFinite(n)||n<.9f||n>1.1f||!Float.isFinite(scale)||scale<.25f||scale>4)return null;return new dev.visorcompat.tacz.GunPose(pos,q,new org.joml.Vector3f(pos),q.transform(new org.joml.Vector3f(0,0,-1)),scale);}
 public record Shot(int slot,net.minecraft.world.item.ItemStack stack,boolean silence){}
 public record Status(int slot,String key,int ammo,boolean chamber,int action,long remaining,boolean emptyReload,int cue){}
 public record Physical(int slot,String key,int phase,int pull,int cue){}
 public static void physical(net.minecraft.server.level.ServerPlayer player,Physical state){CompatNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->player),state);}
 public static void register(){
 CompatNetwork.CHANNEL.messageBuilder(Physical.class,24,NetworkDirection.PLAY_TO_CLIENT)
 .encoder((m,b)->{b.writeInt(m.slot);b.writeUtf(m.key,512);b.writeInt(m.phase);b.writeInt(m.pull);b.writeInt(m.cue);})
 .decoder(b->new Physical(b.readInt(),b.readUtf(512),b.readInt(),b.readInt(),b.readInt()))
 .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.DualPhysicalClient.receive(m));c.get().setPacketHandled(true);}).add();
 CompatNetwork.CHANNEL.messageBuilder(Status.class,21,NetworkDirection.PLAY_TO_CLIENT)
 .encoder((m,b)->{b.writeInt(m.slot);b.writeUtf(m.key,512);b.writeInt(m.ammo);b.writeBoolean(m.chamber);b.writeInt(m.action);b.writeLong(m.remaining);b.writeBoolean(m.emptyReload);b.writeInt(m.cue);})
 .decoder(b->new Status(b.readInt(),b.readUtf(512),b.readInt(),b.readBoolean(),b.readInt(),b.readLong(),b.readBoolean(),b.readInt()))
 .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.DualStatus.receive(m));c.get().setPacketHandled(true);}).add();
 CompatNetwork.CHANNEL.messageBuilder(Shot.class,20,NetworkDirection.PLAY_TO_CLIENT)
 .encoder((m,b)->{b.writeInt(m.slot);b.writeItem(m.stack);b.writeBoolean(m.silence);}).decoder(b->new Shot(b.readInt(),b.readItem(),b.readBoolean()))
 .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.DualEffects.receive(m));c.get().setPacketHandled(true);}).add();
 CompatNetwork.CHANNEL.messageBuilder(Visual.class,19,NetworkDirection.PLAY_TO_CLIENT)
 .encoder((m,b)->{b.writeUUID(m.player);b.writeBoolean(m.enabled);b.writeItem(m.main);b.writeItem(m.off);writePose(b,m.mainPose);writePose(b,m.offPose);CalibrationCodec.write(b,m.mainCal);CalibrationCodec.write(b,m.offCal);})
 .decoder(b->new Visual(b.readUUID(),b.readBoolean(),b.readItem(),b.readItem(),readPose(b),readPose(b),CalibrationCodec.read(b),CalibrationCodec.read(b)))
 .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.DualRemote.receive(m));c.get().setPacketHandled(true);}).add();CompatNetwork.CHANNEL.messageBuilder(Input.class,18,NetworkDirection.PLAY_TO_SERVER)
  .encoder((m,b)->{b.writeLong(m.sequence);b.writeBoolean(m.enabled);b.writeInt(m.mainSlot);b.writeInt(m.offSlot);b.writeBoolean(m.mainHeld);b.writeBoolean(m.offHeld);b.writeBoolean(m.mainTrigger);b.writeBoolean(m.offTrigger);b.writeBoolean(m.mainReload);b.writeBoolean(m.offReload);b.writeBoolean(m.mainAim);b.writeBoolean(m.offAim);b.writeBoolean(m.mainJam);b.writeBoolean(m.offJam);b.writeBoolean(m.physical);b.writeByte(m.mainGrip);b.writeByte(m.offGrip);CalibrationCodec.write(b,m.mainCalibration);CalibrationCodec.write(b,m.offCalibration);writePose(b,m.mainPose);writePose(b,m.offPose);})
  .decoder(b->new Input(b.readLong(),b.readBoolean(),b.readInt(),b.readInt(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readUnsignedByte(),b.readUnsignedByte(),CalibrationCodec.read(b),CalibrationCodec.read(b),readPose(b),readPose(b)))
  .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)dev.visorcompat.tacz.server.ServerDual.receive(p,m);c.get().setPacketHandled(true);}).add();}
}
