package dev.visorcompat.tacz;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;

/** Explicit optical power; never inferred from attachment names or desktop ADS zoom. */
public final class OpticZoomOverrides {
    private OpticZoomOverrides(){}
    public static Map<String,Float> parse(String text){
        Config root=new TomlParser().parse(new StringReader(text));
        Object section=root.get("magnification");
        if(section==null)return Map.of();
        if(!(section instanceof Config values))throw new IllegalArgumentException("magnification must be a table");
        Map<String,Float> result=new TreeMap<>();
        for(var entry:values.entrySet()){
            String id=entry.getKey();Object value=entry.getValue();
            if(!id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+") || !(value instanceof Number number))
                throw new IllegalArgumentException("Each magnification entry needs a quoted attachment ID and numeric power");
            float zoom=number.floatValue();
            if(!Float.isFinite(zoom)||zoom<1||zoom>16)throw new IllegalArgumentException("Optical power must be between 1 and 16");
            result.put(id,zoom);
        }
        return Map.copyOf(result);
    }
}
