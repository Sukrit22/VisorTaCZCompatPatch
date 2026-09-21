package dev.visorcompat.tacz;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.joml.Vector3f;
/** Geometry only: these entries never opt into physical ammo/state handling. */
public final class ButtonProfiles {
    public record Entry(WeaponProfile profile,float[] shell,String category,String muzzleSource) {}
    private static final Map<String,Entry> DATA=load();
    private static Map<String,Entry> load(){
        try(var stream=ButtonProfiles.class.getResourceAsStream("/button-profiles.json")){
            if(stream==null)throw new IllegalStateException("Missing button profiles");
            Map<String,Entry> data=new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),new TypeToken<Map<String,Entry>>(){}.getType());
            if(data==null||data.values().stream().anyMatch(e->e==null||e.profile()==null||!e.profile().buttonOnly()||!PoseMath.finite(e.profile().grip())||!PoseMath.finite(e.profile().muzzleOffset())||e.profile().scale()<=0))throw new IllegalStateException("Invalid button profile");
            return Map.copyOf(data);
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot load button profiles",e);}
    }
    public static WeaponProfile get(String id){var entry=DATA.get(id);return entry==null?null:entry.profile();}
    public static java.util.Set<String> ids(){return DATA.keySet();}
    public static Vector3f shell(WeaponProfile p){
        for(var e:DATA.values())if(e.profile().equals(p)){
            var s=e.shell();return s==null?null:new Vector3f(-s[0],s[1],s[2]);
        }
        return null;
    }
}
