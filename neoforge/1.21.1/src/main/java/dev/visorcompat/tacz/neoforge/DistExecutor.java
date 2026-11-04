package dev.visorcompat.tacz.neoforge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import java.util.function.Supplier;
public final class DistExecutor {
 public static void safeRunWhenOn(Dist dist,Supplier<Runnable> action){if(FMLEnvironment.dist==dist)action.get().run();}
 public static void unsafeRunWhenOn(Dist dist,Supplier<Runnable> action){if(FMLEnvironment.dist==dist)action.get().run();}
}
