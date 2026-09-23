package dev.visorcompat.tacz;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
/** Validate before adopting a file; preserve the original when parsing fails. */
public final class HolsterFiles {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    public static Map<String,HolsterCalibration> read(Path file)throws IOException{
        try{
            Map<String,HolsterCalibration> next=Files.exists(file)?GSON.fromJson(Files.readString(file),new TypeToken<Map<String,HolsterCalibration>>(){}.getType()):new HashMap<>();
            validate(next);return new HashMap<>(next);
        }catch(RuntimeException ex){throw new IOException("Invalid holster JSON",ex);}
    }
    public static void write(Path file,Map<String,HolsterCalibration> data)throws IOException{
        validate(data);Files.createDirectories(file.toAbsolutePath().getParent());var temp=file.resolveSibling(file.getFileName()+".tmp");
        Files.writeString(temp,GSON.toJson(new TreeMap<>(data)));Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);
    }
    private static void validate(Map<String,HolsterCalibration> values)throws IOException{
        if(values==null||values.entrySet().stream().anyMatch(e->e.getKey()==null||e.getKey().isBlank()||e.getValue()==null||!e.getValue().valid()))throw new IOException("Invalid holster calibration");
    }
}
