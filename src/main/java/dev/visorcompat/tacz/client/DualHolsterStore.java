package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;
/** Separate dual mounts, preserving single-gun calibration and two copies of the same gun. */
public final class DualHolsterStore {
 public static final Path FILE=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("visor_tacz-dual-holsters.json");
 private static Map<String,HolsterCalibration> saved;
 private static IOException failure;
 static String previewKey;static HolsterCalibration preview;
 public static String key(String gun,boolean pistol,boolean off){return gun+"#dual:"+(off?"off:":"main:")+(pistol?"pistol":"long");}
 private static void load(){if(saved!=null)return;saved=new HashMap<>();try{saved=HolsterFiles.read(FILE);}catch(IOException e){failure=e;com.mojang.logging.LogUtils.getLogger().error("Cannot load dual holsters",e);}}
 public static HolsterCalibration defaults(String gun,boolean pistol,boolean off){
  if(!pistol&&off)return new HolsterCalibration(.22f,1.38f,.24f,0,-90,-45);
  return HolsterStore.get(gun,pistol);
 }
 public static HolsterCalibration get(String gun,boolean pistol,boolean off){load();String key=key(gun,pistol,off);return key.equals(previewKey)&&preview!=null?preview:saved.getOrDefault(key,defaults(gun,pistol,off));}
 public static void save(String gun,boolean pistol,boolean off,HolsterCalibration value)throws IOException{load();if(failure!=null)throw new IOException("Fix invalid dual holster file before saving",failure);var next=new TreeMap<>(saved);next.put(key(gun,pistol,off),value);HolsterFiles.write(FILE,next);saved=next;}
}
