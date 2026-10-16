package dev.visorcompat.tacz.fabric;
public final class DistExecutor {public static void unsafeRunWhenOn(Dist dist,java.util.function.Supplier<Runnable> code){if((net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()==net.fabricmc.api.EnvType.CLIENT)==(dist==Dist.CLIENT))code.get().run();}}
