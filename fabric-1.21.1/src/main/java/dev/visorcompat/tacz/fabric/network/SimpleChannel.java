package dev.visorcompat.tacz.fabric.network;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.*;
import java.util.function.*;
/** Fabric transport for shared typed messages. Protocol and direction are checked before dispatch. */
public final class SimpleChannel {
 public final ResourceLocation id;private final String protocol;private final Map<Integer,Entry<?>> ids=new HashMap<>();private final Map<Class<?>,Entry<?>> types=new HashMap<>();
 private record Entry<T>(int id,Class<T> type,NetworkDirection direction,BiConsumer<T,FriendlyByteBuf> encode,Function<FriendlyByteBuf,T> decode,BiConsumer<T,Supplier<Context>> handler){}
 public record Context(ServerPlayer sender){public ServerPlayer getSender(){return sender;}public void setPacketHandled(boolean handled){}}
 SimpleChannel(ResourceLocation id,String protocol){this.id=id;this.protocol=protocol;
    net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(ChannelPayload.TYPE,ChannelPayload.CODEC);
  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(ChannelPayload.TYPE,ChannelPayload.CODEC);
  ServerPlayNetworking.registerGlobalReceiver(ChannelPayload.TYPE,(payload,context)->{
   var buf=payload.buffer();
   try{Runnable work=decode(buf,NetworkDirection.PLAY_TO_SERVER,new Context(context.player()));context.server().execute(work);}catch(RuntimeException ex){context.player().connection.disconnect(Component.literal("Visor TaCZ: incompatible or invalid packet (protocol "+protocol+")"));}finally{buf.release();}
  });
 }public <T> Builder<T> messageBuilder(Class<T> type,int id,NetworkDirection direction){return new Builder<>(type,id,direction);}
 public final class Builder<T>{final Class<T> type;final int id;final NetworkDirection direction;BiConsumer<T,FriendlyByteBuf> encoder;Function<FriendlyByteBuf,T> decoder;BiConsumer<T,Supplier<Context>> handler;
  Builder(Class<T> t,int i,NetworkDirection d){type=t;id=i;direction=d;}public Builder<T> encoder(BiConsumer<T,FriendlyByteBuf> e){encoder=e;return this;}public Builder<T> decoder(Function<FriendlyByteBuf,T> d){decoder=d;return this;}public Builder<T> consumerMainThread(BiConsumer<T,Supplier<Context>> h){handler=h;return this;}
  public void add(){var entry=new Entry<>(id,type,direction,Objects.requireNonNull(encoder),Objects.requireNonNull(decoder),Objects.requireNonNull(handler));if(ids.putIfAbsent(id,entry)!=null||types.putIfAbsent(type,entry)!=null)throw new IllegalStateException("Duplicate packet");}
 }
 private <T> Runnable decoded(Entry<T> e,FriendlyByteBuf b,Context c){T m=e.decode.apply(b);if(b.readableBytes()!=0)throw new IllegalArgumentException("Trailing packet data");return ()->e.handler.accept(m,()->c);}
 public Runnable decode(FriendlyByteBuf b,NetworkDirection side,Context context){if(!protocol.equals(b.readUtf(32)))throw new IllegalArgumentException("Protocol mismatch");var e=ids.get(b.readVarInt());if(e==null||e.direction!=side)throw new IllegalArgumentException("Wrong packet direction");return decoded(e,b,context);}
 @SuppressWarnings("unchecked") public <T> FriendlyByteBuf encode(T message,NetworkDirection side,net.minecraft.core.RegistryAccess registries){var entry=(Entry<T>)types.get(message.getClass());if(entry==null||entry.direction!=side)throw new IllegalArgumentException("Unknown packet");var b=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),registries);b.writeUtf(protocol,32);b.writeVarInt(entry.id);entry.encode.accept(message,b);return b;}
  public void send(PacketDistributor.Target target,Object message){for(var p:target.players())if(ServerPlayNetworking.canSend(p,ChannelPayload.TYPE))ServerPlayNetworking.send(p,ChannelPayload.copy(encode(message,NetworkDirection.PLAY_TO_CLIENT,p.registryAccess())));}
public void sendToServer(Object message){ClientNetwork.send(this,message);}
}



