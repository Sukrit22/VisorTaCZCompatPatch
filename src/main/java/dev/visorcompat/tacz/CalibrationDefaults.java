package dev.visorcompat.tacz;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Explicit offsets, not baked into model pivots. User profiles override these. */
public final class CalibrationDefaults {
    private CalibrationDefaults() {}
    public static Map<String,Calibration> load() {
        try(var stream=CalibrationDefaults.class.getResourceAsStream("/calibration-defaults.json")) {
            if(stream==null)throw new IllegalStateException("Missing bundled calibration");
            Map<String,Calibration> values=new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),
                new TypeToken<Map<String,Calibration>>(){}.getType());
            if(values==null || values.values().stream().anyMatch(v->v==null || !v.valid()))
                throw new IllegalStateException("Invalid bundled calibration");
            return Map.copyOf(values);
        } catch(java.io.IOException e) {throw new IllegalStateException("Cannot read bundled calibration",e);}
    }
}
