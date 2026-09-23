package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.HolsterCalibration;
import dev.visorcompat.tacz.HolsterFiles;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
public final class HolsterStore {
    public static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("visor_tacz-holsters.json");
    private static Map<String,HolsterCalibration> saved;
    private static String error;
    private static final Map<String,HolsterCalibration> DEFAULTS=dev.visorcompat.tacz.HandlingDefaults.holsters();
    static String previewKey;
    static HolsterCalibration preview;
    public static void reload() throws IOException {
        if(preview!=null)throw new IOException("Save or cancel the holster preview before reloading");
        try{
            saved=HolsterFiles.read(FILE);error=null;
        }catch(Exception ex){error="Cannot load holsters: "+ex.getMessage();throw new IOException(error,ex);}
    }
    private static void load(){if(saved!=null)return;saved=new HashMap<>();try{reload();}catch(IOException ex){com.mojang.logging.LogUtils.getLogger().error("Cannot load {}",FILE,ex);}}
    public static HolsterCalibration get(String key,boolean pistol){load();return key.equals(previewKey)&&preview!=null?preview:saved.getOrDefault(key,DEFAULTS.getOrDefault(key,HolsterCalibration.defaults(pistol)));}
    public static void save(String key,HolsterCalibration value)throws IOException{
        load();if(error!=null)throw new IOException(error);if(!value.valid())throw new IOException("Holster values outside limits");
        var next=new TreeMap<>(saved);next.put(key,value);HolsterFiles.write(FILE,next);saved=next;
    }
}
