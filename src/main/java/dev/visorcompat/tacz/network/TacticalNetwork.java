package dev.visorcompat.tacz.network;

import dev.visorcompat.tacz.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;

public final class TacticalNetwork {
    private TacticalNetwork(){}
    public record Input(boolean pinch,boolean down,boolean canceled,int slot,String key){}
    public record Setting(String key,TacticalCalibration value,boolean enabled){}
    public static void register(){
        CompatNetwork.CHANNEL.messageBuilder(Input.class,26,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeBoolean(m.pinch());b.writeBoolean(m.down());b.writeBoolean(m.canceled());b.writeInt(m.slot());b.writeUtf(m.key(),512);})
            .decoder(b->new Input(b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readInt(),b.readUtf(512)))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null&&p.getInventory().selected==m.slot()&&Profiles.key(p.getMainHandItem()).equals(m.key()))dev.visorcompat.tacz.server.TacticalServer.input(p,m.pinch(),m.down(),m.canceled());c.get().setPacketHandled(true);}).add();
        CompatNetwork.CHANNEL.messageBuilder(Setting.class,27,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeUtf(m.key(),512);write(b,m.value());b.writeBoolean(m.enabled());}).decoder(b->new Setting(b.readUtf(512),read(b),b.readBoolean()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null&&m.value().valid())dev.visorcompat.tacz.server.TacticalServer.setting(p,m);c.get().setPacketHandled(true);}).add();
    }
    private static void pose(FriendlyByteBuf b,InspectionCalibration c){b.writeFloat(c.x());b.writeFloat(c.y());b.writeFloat(c.z());b.writeFloat(c.pitch());b.writeFloat(c.yaw());b.writeFloat(c.roll());}
    private static InspectionCalibration pose(FriendlyByteBuf b){return new InspectionCalibration(b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat());}
    private static void write(FriendlyByteBuf b,TacticalCalibration c){pose(b,c.normalGrip());pose(b,c.pinchGrip());pose(b,c.magwellEntry());b.writeFloat(c.pitchTolerance());b.writeFloat(c.yawTolerance());b.writeFloat(c.rollTolerance());b.writeFloat(c.entryRadius());b.writeFloat(c.seatedDepth());b.writeFloat(c.seatedTolerance());b.writeBoolean(c.alternativeControls());}
    private static TacticalCalibration read(FriendlyByteBuf b){return new TacticalCalibration(pose(b),pose(b),pose(b),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readBoolean());}
}
