package dev.visorcompat.tacz.neoforge.network;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.*;
import java.util.function.*;
/** Retains shared typed messages while using NeoForge's registry-aware payload transport. */
public final class SimpleChannel {
 private static final List<SimpleChannel> CHANNELS=new ArrayList<>();
 public final ResourceLocation id;private final String protocol;private final Map<Integer,Entry<?>> ids=new HashMap<>();private final Map<Class<?>,Entry<?>> types=new HashMap<>();
 private record Entry<T>(int id,Class<T> type,NetworkDirection direction,BiConsumer<T,FriendlyByteBuf> encode,Function<FriendlyByteBuf,T> decode,BiConsumer<T,Supplier<Context>> handler){}
 public record Context(IPayloadContext context){public ServerPlayer getSender(){return context.player() instanceof ServerPlayer p?p:null;}public void setPacketHandled(boolean handled){}}
 public record Payload(CustomPacketPayload.Type<Payload> type,Object message,SimpleChannel channel,NetworkDirection direction) implements CustomPacketPayload {}
 SimpleChannel(ResourceLocation id,String protocol){this.id=id;this.protocol=protocol;CHANNELS.add(this);}
 public static void registerPayloads(RegisterPayloadHandlersEvent event){for(var channel:CHANNELS)channel.register(event);}
 private void register(RegisterPayloadHandlersEvent event){
  for(var direction:NetworkDirection.values()){
   var type=new CustomPacketPayload.Type<Payload>(ResourceLocation.fromNamespaceAndPath(id.getNamespace(),id.getPath()+ (direction==NetworkDirection.PLAY_TO_SERVER?"_server":"_client")));
   StreamCodec<RegistryFriendlyByteBuf,Payload> codec=StreamCodec.of((b,p)->encode(b,p.message,direction),(b)->new Payload(type,decode(b,direction),this,direction));
   var registrar=event.registrar(protocol);
   if(direction==NetworkDirection.PLAY_TO_SERVER)registrar.playToServer(type,codec,(p,c)->handle(p,c));
   else registrar.playToClient(type,codec,(p,c)->handle(p,c));
  }
 }
 @SuppressWarnings("unchecked") private void handle(Payload p,IPayloadContext c){Entry<Object> e=(Entry<Object>)types.get(p.message.getClass());c.enqueueWork(()->e.handler.accept(p.message,()->new Context(c)));}
 public <T> Builder<T> messageBuilder(Class<T> type,int id,NetworkDirection direction){return new Builder<>(type,id,direction);}
 public final class Builder<T>{final Class<T> type;final int id;final NetworkDirection direction;BiConsumer<T,FriendlyByteBuf> encoder;Function<FriendlyByteBuf,T> decoder;BiConsumer<T,Supplier<Context>> handler;
  Builder(Class<T> t,int i,NetworkDirection d){type=t;id=i;direction=d;}public Builder<T> encoder(BiConsumer<T,FriendlyByteBuf> e){encoder=e;return this;}public Builder<T> decoder(Function<FriendlyByteBuf,T> d){decoder=d;return this;}public Builder<T> consumerMainThread(BiConsumer<T,Supplier<Context>> h){handler=h;return this;}
  public void add(){var e=new Entry<>(id,type,direction,Objects.requireNonNull(encoder),Objects.requireNonNull(decoder),Objects.requireNonNull(handler));if(ids.putIfAbsent(id,e)!=null||types.putIfAbsent(type,e)!=null)throw new IllegalStateException("Duplicate packet");}
 }
 private Object decode(RegistryFriendlyByteBuf b,NetworkDirection direction){var e=ids.get(b.readVarInt());if(e==null||e.direction!=direction)throw new IllegalArgumentException("Wrong packet direction");Object message=e.decode.apply(b);if(b.readableBytes()!=0)throw new IllegalArgumentException("Trailing packet data");return message;}
 @SuppressWarnings("unchecked") private void encode(RegistryFriendlyByteBuf b,Object message,NetworkDirection direction){var e=(Entry<Object>)types.get(message.getClass());if(e==null||e.direction!=direction)throw new IllegalArgumentException("Wrong packet direction");b.writeVarInt(e.id);e.encode.accept(message,b);}
 private Payload payload(Object message,NetworkDirection direction){return new Payload(new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(id.getNamespace(),id.getPath()+(direction==NetworkDirection.PLAY_TO_SERVER?"_server":"_client"))),message,this,direction);}
 public void send(PacketDistributor.Target target,Object message){var p=payload(message,NetworkDirection.PLAY_TO_CLIENT);switch(target.kind()){case 0->net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((ServerPlayer)target.entity(),p);case 1->net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(target.entity(),p);case 2->net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(target.entity(),p);default->throw new IllegalArgumentException();}}
 public void sendToServer(Object message){net.neoforged.neoforge.network.PacketDistributor.sendToServer(payload(message,NetworkDirection.PLAY_TO_SERVER));}
}
