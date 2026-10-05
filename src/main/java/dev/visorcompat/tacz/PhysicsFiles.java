package dev.visorcompat.tacz;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
/** Validate fully before adoption; never overwrite an invalid source file. */
public final class PhysicsFiles {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    public static Map<String,PhysicsCalibration> read(Path file)throws IOException{
        try{Map<String,PhysicsCalibration> next=Files.exists(file)?GSON.fromJson(Files.readString(file),new TypeToken<Map<String,PhysicsCalibration>>(){}.getType()):new HashMap<>();
            validate(next);return new HashMap<>(next);
        }catch(RuntimeException ex){throw new IOException("Invalid physics JSON",ex);}
    }
    public static void write(Path file,Map<String,PhysicsCalibration> data)throws IOException{
        validate(data);Path target=file.toAbsolutePath();Files.createDirectories(target.getParent());
        Path temp=Files.createTempFile(target.getParent(),target.getFileName().toString(),".tmp");
        try{Files.writeString(temp,GSON.toJson(new TreeMap<>(data)));
            try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException ex){throw new IOException("Filesystem does not support atomic physics saves",ex);}
        }finally{Files.deleteIfExists(temp);}
    }
    private static void validate(Map<String,PhysicsCalibration> values)throws IOException{
        if(values==null||values.entrySet().stream().anyMatch(e->e.getKey()==null||e.getKey().isBlank()||e.getValue()==null||!e.getValue().valid()))throw new IOException("Invalid physics calibration");
    }
}
