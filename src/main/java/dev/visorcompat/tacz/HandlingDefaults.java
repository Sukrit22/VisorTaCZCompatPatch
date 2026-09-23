package dev.visorcompat.tacz;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
/** Bundled presets are fallbacks; explicit user calibration always wins. */
public final class HandlingDefaults {
    private static <T> Map<String,T> load(String name,java.lang.reflect.Type type,java.util.function.Predicate<T> valid){
        try(var stream=HandlingDefaults.class.getResourceAsStream("/"+name+"-defaults.json")){
            if(stream==null)throw new IllegalStateException("Missing bundled "+name);
            Map<String,T> values=new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),type);
            if(values==null||values.values().stream().anyMatch(v->v==null||!valid.test(v)))throw new IllegalStateException("Invalid bundled "+name);
            return Map.copyOf(values);
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
    }
    public static Map<String,HolsterCalibration> holsters(){return load("holster",new TypeToken<Map<String,HolsterCalibration>>(){}.getType(),HolsterCalibration::valid);}
    public static Map<String,InspectionCalibration> inspections(){return load("inspection",new TypeToken<Map<String,InspectionCalibration>>(){}.getType(),InspectionCalibration::valid);}
}
