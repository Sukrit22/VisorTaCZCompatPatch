package dev.visorcompat.tacz.fabric.network;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
public final class ClientNetwork {
 public static void register(SimpleChannel channel){ClientPlayNetworking.registerGlobalReceiver(channel.id,(client,handler,buf,sender)->{try{Runnable work=channel.decode(buf,NetworkDirection.PLAY_TO_CLIENT,new SimpleChannel.Context(null));client.execute(work);}catch(RuntimeException ex){client.execute(()->handler.getConnection().disconnect(Component.literal("Visor TaCZ: incompatible or invalid server packet")));}});}
 public static void send(SimpleChannel c,Object message){if(ClientPlayNetworking.canSend(c.id))ClientPlayNetworking.send(c.id,c.encode(message,NetworkDirection.PLAY_TO_SERVER));}
}
