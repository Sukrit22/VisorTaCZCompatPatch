package dev.visorcompat.tacz;

import com.google.gson.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Versioned independent file. Unknown versions and invalid values never become writable defaults. */
public final class TacticalFiles {
    public static final int SCHEMA_VERSION=1;
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private TacticalFiles(){}
    public static Map<String,TacticalCalibration> read(Path file)throws IOException{
        if(!Files.exists(file))return new HashMap<>();
        try{
            var root=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            int version=0;
            if(root.has("schemaVersion")){var schema=root.get("schemaVersion");if(!schema.isJsonPrimitive()||!schema.getAsJsonPrimitive().isNumber()||schema.getAsDouble()!=schema.getAsInt())throw new IOException("Invalid tactical schema version");version=schema.getAsInt();}
            if(version<0||version>SCHEMA_VERSION)throw new IOException("Unsupported tactical schema version: "+version);
            var entries=root.has("guns")?root.getAsJsonObject("guns"):root;
            Map<String,TacticalCalibration> result=new HashMap<>();
            for(var e:entries.entrySet()){
                if(e.getKey().equals("schemaVersion"))continue;
                var merged=GSON.toJsonTree(TacticalCalibration.DEFAULT).getAsJsonObject();
                var supplied=e.getValue().getAsJsonObject();
                for(var field:supplied.entrySet()){
                    if(field.getValue().isJsonObject()&&merged.has(field.getKey())&&merged.get(field.getKey()).isJsonObject()){
                        var pose=merged.getAsJsonObject(field.getKey());
                        for(var axis:field.getValue().getAsJsonObject().entrySet())pose.add(axis.getKey(),axis.getValue());
                    }else merged.add(field.getKey(),field.getValue());
                }
                result.put(e.getKey(),GSON.fromJson(merged,TacticalCalibration.class));
            }
            validate(result);return result;
        }catch(RuntimeException ex){throw new IOException("Invalid tactical calibration JSON",ex);}
    }
    public static void write(Path file,Map<String,TacticalCalibration> data)throws IOException{
        validate(data);
        // Re-read before replacing: even direct callers cannot overwrite an unknown future schema.
        if(Files.exists(file))read(file);
        var root=new JsonObject();root.addProperty("schemaVersion",SCHEMA_VERSION);root.add("guns",GSON.toJsonTree(new TreeMap<>(data)));
        Files.createDirectories(file.toAbsolutePath().getParent());var tmp=file.resolveSibling(file.getFileName()+".tmp");
        Files.writeString(tmp,GSON.toJson(root));Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);
    }
    private static void validate(Map<String,TacticalCalibration> data)throws IOException{
        if(data==null||data.entrySet().stream().anyMatch(e->e.getKey()==null||e.getKey().isBlank()||e.getValue()==null||!e.getValue().valid()))throw new IOException("Tactical values outside limits");
    }
}
