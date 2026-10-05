package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;
public final class PhysicsStore {
    public static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("visor_tacz-physics.json");
    private static Map<String,PhysicsCalibration> saved;
    private static String error;
    public static String previewKey;
    public static PhysicsCalibration preview;
    public static void reload() throws IOException {
        if(preview!=null)throw new IOException("Save or cancel the physics preview before reloading");
        try{var next=PhysicsFiles.read(FILE);saved=next;error=null;}
        catch(IOException ex){error="Cannot load physics: "+ex.getMessage();throw new IOException(error,ex);}
    }
    private static void load(){if(saved!=null)return;saved=new HashMap<>();try{reload();}catch(IOException ex){com.mojang.logging.LogUtils.getLogger().error("Cannot load {}",FILE,ex);}}
    public static PhysicsCalibration get(String key,WeaponProfile profile){load();return key.equals(previewKey)&&preview!=null?preview:saved.getOrDefault(key,PhysicsCalibration.defaults(profile));}
    public static PhysicsCalibration saved(String key,WeaponProfile profile){load();return saved.getOrDefault(key,PhysicsCalibration.defaults(profile));}
    public static void save(String key,PhysicsCalibration value)throws IOException{
        load();if(error!=null)throw new IOException(error);if(!value.valid())throw new IOException("Physics values outside limits");
        var next=new TreeMap<>(saved);next.put(key,value);PhysicsFiles.write(FILE,next);saved=next;
    }
}
