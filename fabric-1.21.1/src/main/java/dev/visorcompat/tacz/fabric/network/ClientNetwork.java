package dev.visorcompat.tacz.fabric.network;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
public final class ClientNetwork {
 public static void register(SimpleChannel channel){ClientPlayNetworking.registerGlobalReceiver(ChannelPayload.TYPE,(payload,context)->{var buf=payload.buffer();try{Runnable work=channel.decode(buf,NetworkDirection.PLAY_TO_CLIENT,new SimpleChannel.Context(null));context.client().execute(work);}catch(RuntimeException ex){context.client().getConnection().getConnection().disconnect(Component.literal("Visor TaCZ: incompatible or invalid server packet"));}finally{buf.release();}});}
 public static void send(SimpleChannel c,Object message){if(ClientPlayNetworking.canSend(ChannelPayload.TYPE))ClientPlayNetworking.send(ChannelPayload.copy(c.encode(message,NetworkDirection.PLAY_TO_SERVER,net.minecraft.client.Minecraft.getInstance().getConnection().registryAccess())));}
}

