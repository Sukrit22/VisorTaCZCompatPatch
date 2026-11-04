package dev.visorcompat.tacz.platform;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
/** Loader-owned path lookup; no Minecraft classes are needed here. */
public final class PlatformPaths {
    private PlatformPaths() {}
    public static Path configDirectory(){return FabricLoader.getInstance().getConfigDir();}
}
