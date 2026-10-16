package dev.visorcompat.tacz.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

/** Registration belongs to CompatNetwork.register; IDs 22 and 23. */
public final class ButtonJamNetwork {
    private ButtonJamNetwork(){}
    public record Hold(boolean held,int slot,String key){}
    public record Progress(int slot,String key,int ticks,boolean active){}
    public static void register(){
        CompatNetwork.CHANNEL.messageBuilder(Hold.class,22,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeBoolean(m.held());b.writeVarInt(m.slot());b.writeUtf(m.key(),512);})
            .decoder(b->new Hold(b.readBoolean(),b.readVarInt(),b.readUtf(512)))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)dev.visorcompat.tacz.server.ServerButtonJams.receive(p,m);c.get().setPacketHandled(true);}).add();
        CompatNetwork.CHANNEL.messageBuilder(Progress.class,23,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeVarInt(m.slot());b.writeUtf(m.key(),512);b.writeVarInt(m.ticks());b.writeBoolean(m.active());})
            .decoder(b->new Progress(b.readVarInt(),b.readUtf(512),b.readVarInt(),b.readBoolean()))
            .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.visorcompat.tacz.client.ButtonJamClient.receive(m));c.get().setPacketHandled(true);}).add();
    }
    public static void hold(boolean held,int slot,String key){CompatNetwork.CHANNEL.sendToServer(new Hold(held,slot,key));}
    public static void progress(ServerPlayer p,int slot,String key,int ticks,boolean active){CompatNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Progress(slot,key,ticks,active));}
}
