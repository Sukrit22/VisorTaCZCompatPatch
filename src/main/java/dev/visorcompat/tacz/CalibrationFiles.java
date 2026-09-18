package dev.visorcompat.tacz;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Parse and validate the entire replacement before the caller adopts it. */
public final class CalibrationFiles {
    private CalibrationFiles() {}
    public static Map<String,Calibration> read(Path file) throws IOException {
        var next=new HashMap<>(CalibrationDefaults.load());
        if(Files.notExists(file))return next;
        try(var reader=Files.newBufferedReader(file)) {
            Map<String,Calibration> values=new Gson().fromJson(reader,new TypeToken<Map<String,Calibration>>(){}.getType());
            if(values==null)throw new IOException("Expected a JSON object, not an empty file or null");
            for(var entry:values.entrySet()) {
                if(entry.getKey()==null || entry.getKey().isBlank() || entry.getValue()==null || !entry.getValue().valid())
                    throw new IOException("Invalid calibration profile: "+entry.getKey());
            }
            next.putAll(values);
            return next;
        } catch(RuntimeException e) {throw new IOException("Invalid calibration JSON",e);}
    }
}
