package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.InspectionCalibration;
import dev.visorcompat.tacz.InspectionFiles;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
public final class InspectionStore {
    public static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("visor_tacz-inspections.json");
    private static Map<String,InspectionCalibration> saved;
    private static String error;
    private static final Map<String,InspectionCalibration> DEFAULTS=dev.visorcompat.tacz.HandlingDefaults.inspections();
    static String previewKey;
    static InspectionCalibration preview;
    public static void reload() throws IOException {
        if(preview!=null)throw new IOException("Save or cancel the inspection preview before reloading");
        try{
            saved=InspectionFiles.read(FILE);error=null;
        }catch(Exception ex){error="Cannot load inspections: "+ex.getMessage();throw new IOException(error,ex);}
    }
    private static void load(){if(saved!=null)return;saved=new HashMap<>();try{reload();}catch(IOException ex){com.mojang.logging.LogUtils.getLogger().error("Cannot load {}",FILE,ex);}}
    public static InspectionCalibration get(String key,boolean pistol){load();return key.equals(previewKey)&&preview!=null?preview:saved.getOrDefault(key,DEFAULTS.getOrDefault(key,InspectionCalibration.defaults(pistol)));}
    public static void save(String key,InspectionCalibration value)throws IOException{
        load();if(error!=null)throw new IOException(error);if(!value.valid())throw new IOException("Inspection values outside limits");
        var next=new TreeMap<>(saved);next.put(key,value);InspectionFiles.write(FILE,next);saved=next;
    }
}
