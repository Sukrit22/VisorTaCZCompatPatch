package dev.visorcompat.tacz;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

/** Optional attachment map; read and validate a complete replacement before adopting it. */
public final class SightCalibrationFiles {
    private SightCalibrationFiles(){}
    public static Map<String,SightCalibration> read(Path file)throws IOException{
        if(Files.notExists(file))return new TreeMap<>();
        try(var reader=Files.newBufferedReader(file)){
            Map<String,SightCalibration> values=new Gson().fromJson(reader,new TypeToken<Map<String,SightCalibration>>(){}.getType());
            if(values==null)throw new IOException("Expected a sight calibration JSON object");
            for(var e:values.entrySet())if(e.getKey()==null||e.getKey().isBlank()||e.getValue()==null||!e.getValue().valid())
                throw new IOException("Invalid sight calibration for "+e.getKey());
            return new TreeMap<>(values);
        }catch(RuntimeException e){throw new IOException("Invalid sight calibration JSON",e);}
    }
}
