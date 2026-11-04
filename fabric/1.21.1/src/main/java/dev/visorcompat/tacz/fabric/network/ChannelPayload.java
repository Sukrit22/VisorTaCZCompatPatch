package dev.visorcompat.tacz.fabric.network;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Length-delimited shared protocol carried by the 1.21 typed payload API. */
public record ChannelPayload(byte[] bytes,net.minecraft.core.RegistryAccess registries) implements CustomPacketPayload {
 public static final Type<ChannelPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("visor_tacz","main"));
 public static final StreamCodec<RegistryFriendlyByteBuf,ChannelPayload> CODEC=StreamCodec.of((buf,value)->buf.writeByteArray(value.bytes),buf->new ChannelPayload(buf.readByteArray(1048576),buf.registryAccess()));
 @Override public Type<ChannelPayload> type(){return TYPE;}
 public FriendlyByteBuf buffer(){return new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes),registries);}
 public static ChannelPayload copy(FriendlyByteBuf buffer){try{byte[] bytes=new byte[buffer.readableBytes()];buffer.readBytes(bytes);return new ChannelPayload(bytes,((RegistryFriendlyByteBuf)buffer).registryAccess());}finally{buffer.release();}}
}


