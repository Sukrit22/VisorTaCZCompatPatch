package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.OpticZoomOverrides;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.*;
import java.util.Map;

/** Optional, live-reloaded optical overrides. An unreadable file falls back to native rules. */
final class OpticZoomStore {
    private static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("visor_tacz-optics.toml");
    private static Map<String,Float> values=Map.of();
    private static long nextCheck,modified=Long.MIN_VALUE,size=-1;
    static Float get(String attachmentId){
        long now=System.nanoTime();
        if(now>=nextCheck){
            nextCheck=now+1_000_000_000L;
            try {
                if(!Files.exists(FILE)){
                    Files.createDirectories(FILE.getParent());
                    Files.writeString(FILE,"# Explicit optical magnification by attachment ID. Empty uses native rules.\n[magnification]\n# \"your_pack:your_optic\" = 2.0\n",StandardOpenOption.CREATE_NEW);
                }
                long stamp=Files.getLastModifiedTime(FILE).toMillis(),length=Files.size(FILE);
                if(stamp!=modified||length!=size){
                    modified=stamp;size=length;
                    try {values=OpticZoomOverrides.parse(Files.readString(FILE));}
                    catch(Exception error){values=Map.of();com.mojang.logging.LogUtils.getLogger().warn("Cannot read optic overrides {}; using native optic rules",FILE,error);}
                }
            } catch(Exception error){
                if(modified!=Long.MAX_VALUE){com.mojang.logging.LogUtils.getLogger().warn("Cannot access optic overrides {}; using native optic rules",FILE,error);modified=Long.MAX_VALUE;}
                values=Map.of();
            }
        }
        return values.get(attachmentId);
    }
}
