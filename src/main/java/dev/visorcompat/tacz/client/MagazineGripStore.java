package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.InspectionCalibration;
import dev.visorcompat.tacz.MagazineGripFiles;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;

/** Saved grip of a detached magazine; completely independent of gun calibration. */
public final class MagazineGripStore {
    public static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("visor_tacz-magazine-grips.json");
    private static Map<String,InspectionCalibration> saved;
    private static String error;
    static String previewKey;
    static InspectionCalibration preview;
    private MagazineGripStore() {}
    public static void reload()throws IOException{
        if(preview!=null)throw new IOException("Close the magazine grip preview before reloading");
        try{saved=MagazineGripFiles.read(FILE);error=null;}
        catch(IOException ex){error="Cannot load magazine grips: "+ex.getMessage();throw ex;}
    }
    private static void load(){
        if(saved!=null)return;saved=new HashMap<>();
        try{reload();}catch(IOException ex){com.mojang.logging.LogUtils.getLogger().error("Cannot load {}",FILE,ex);}
    }
    public static String error(){load();return error;}
    public static InspectionCalibration get(String key){load();return key.equals(previewKey)&&preview!=null?preview:saved.getOrDefault(key,MagazineGripFiles.DEFAULT);}
    public static void save(String key,InspectionCalibration value)throws IOException{
        load();if(error!=null)throw new IOException(error);
        if(value==null||!value.valid())throw new IOException("Magazine grip values outside limits");
        var next=new TreeMap<>(saved);next.put(key,value);MagazineGripFiles.write(FILE,next);saved=next;
    }
}
