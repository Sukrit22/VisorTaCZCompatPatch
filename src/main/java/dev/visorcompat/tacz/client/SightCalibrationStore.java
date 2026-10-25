package dev.visorcompat.tacz.client;

import com.google.gson.*;
import dev.visorcompat.tacz.SightCalibration;
import dev.visorcompat.tacz.SightCalibrationFiles;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

/** Separate optional attachment adjustments leave existing gun calibration files intact. */
public final class SightCalibrationStore {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    public static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("visor_tacz-sight-calibration.json");
    private static Map<String,SightCalibration> saved;
    private static String error;
    static String previewKey;
    static SightCalibration preview;
    private static void load(){
        if(saved!=null)return;saved=new TreeMap<>();
        if(!Files.exists(FILE))return;
        try {
            saved=SightCalibrationFiles.read(FILE);
        }catch(Exception failure){error="Cannot read sight calibration file; preserve and repair it before saving.";com.mojang.logging.LogUtils.getLogger().error(error,failure);}
    }
    public static String error(){load();return error;}
    public static void reload()throws IOException{
        if(preview!=null)throw new IOException("Save or cancel sight calibration edits before reloading");
        var next=SightCalibrationFiles.read(FILE);saved=next;error=null;
    }
    public static SightCalibration get(String key){load();return saved.getOrDefault(key,SightCalibration.ZERO);}
    public static SightCalibration render(String key){return key.equals(previewKey)&&preview!=null?preview:get(key);}
    public static void save(String key,SightCalibration value)throws IOException{
        load();if(error!=null)throw new IOException(error);if(!value.valid())throw new IOException("Sight offset exceeds 2 metres or box dimensions are invalid");
        var next=new TreeMap<>(saved);next.put(key,value);Files.createDirectories(FILE.getParent());
        Path temp=FILE.resolveSibling(FILE.getFileName()+".tmp");Files.writeString(temp,JSON.toJson(next));
        Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);saved=next;
    }
}
