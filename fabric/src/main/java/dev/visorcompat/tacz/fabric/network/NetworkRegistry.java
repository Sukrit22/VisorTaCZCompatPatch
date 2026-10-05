package dev.visorcompat.tacz.fabric.network;
import net.minecraft.resources.ResourceLocation;
public final class NetworkRegistry {public static SimpleChannel newSimpleChannel(ResourceLocation id,java.util.function.Supplier<String> protocol,java.util.function.Predicate<String> client,java.util.function.Predicate<String> server){return new SimpleChannel(id,protocol.get());}}
