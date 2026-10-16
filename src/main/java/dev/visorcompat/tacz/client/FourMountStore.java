package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;
/** New mount identities; old calibration remains a read-only fallback. */
public final class FourMountStore {
 public static final Path FILE=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("visor_tacz-four-holsters.json");
 private static Map<String,HolsterCalibration> saved;private static IOException failure;
 static String previewKey;static HolsterCalibration preview;
 public static String key(String gun,int mount){return gun+"#mount:"+HolsterAssignments.Mount.values()[mount].name().toLowerCase(java.util.Locale.ROOT);}
 private static void load(){if(saved!=null)return;saved=new HashMap<>();try{saved=HolsterFiles.read(FILE);}catch(IOException e){failure=e;com.mojang.logging.LogUtils.getLogger().error("Cannot load four holsters",e);}}
 public static HolsterCalibration defaults(String gun,int mount){return DualHolsterStore.get(gun,mount<2,mount==1||mount==3);}
 public static HolsterCalibration get(String gun,int mount){load();String k=key(gun,mount);return k.equals(previewKey)&&preview!=null?preview:saved.getOrDefault(k,defaults(gun,mount));}
 public static void save(String gun,int mount,HolsterCalibration value)throws IOException{load();if(failure!=null)throw new IOException("Fix invalid four-holster file before saving",failure);var next=new TreeMap<>(saved);next.put(key(gun,mount),value);HolsterFiles.write(FILE,next);saved=next;}
}
