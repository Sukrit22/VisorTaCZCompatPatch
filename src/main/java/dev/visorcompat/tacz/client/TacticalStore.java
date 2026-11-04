package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import net.minecraftforge.fml.loading.FMLPaths;

public final class TacticalStore {
    public static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("visor_tacz-tactical.json");
    private static Map<String,TacticalCalibration> saved;
    private static String error;
    private TacticalStore(){}
    public static void reload()throws IOException{
        try{saved=TacticalFiles.read(FILE);error=null;}catch(IOException ex){error=ex.getMessage();throw ex;}
    }
    private static void load(){if(saved!=null)return;saved=new HashMap<>();try{reload();}catch(IOException ex){com.mojang.logging.LogUtils.getLogger().error("Cannot load {}",FILE,ex);}}
    public static String error(){load();return error;}
    public static TacticalCalibration get(String gunId){return get(gunId,gunId+"|tacz:default");}
    public static TacticalCalibration get(String gunId,String originalKey){
        load();var existing=saved.get(gunId);if(existing!=null)return existing;
        var d=TacticalCalibration.DEFAULT;return new TacticalCalibration(MagazineGripStore.savedValue(originalKey),d.pinchGrip(),d.magwellEntry(),d.pitchTolerance(),d.yawTolerance(),d.rollTolerance(),d.entryRadius(),d.seatedDepth(),d.seatedTolerance());
    }
    public static void save(String gunId,TacticalCalibration value)throws IOException{
        load();if(error!=null)throw new IOException(error);var next=new TreeMap<>(saved);next.put(gunId,value);TacticalFiles.write(FILE,next);saved=next;
    }
}


