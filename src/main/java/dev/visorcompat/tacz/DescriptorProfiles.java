package dev.visorcompat.tacz;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import org.joml.Vector3f;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
/** First executable subset: tested mechanism templates, semantic parts and gun-frame zones. */
public final class DescriptorProfiles {
    public record Data(String id,WeaponProfile profile,String magazine,String rack,String bolt,Map<String,Vector3f> zones,PistolProfiles.Data pistol,Visual visual,List<String> manualCycleModes) {}
    public record Visual(String shotNode,float shotTravel,String cylinder,String hammer,float hammerDegrees,List<String> rounds,List<String> heads,String loader,List<String> hidden) {}
    private static volatile Map<String,Data> loaded;
    private static final Map<WeaponProfile,Data> DETAILS=Collections.synchronizedMap(new IdentityHashMap<>());
    private static String digest="";
    public static Data details(WeaponProfile p){return DETAILS.get(p);}
    public static Vector3f zone(WeaponProfile p,String name){var d=details(p);return d==null||!d.zones().containsKey(name)?null:new Vector3f(d.zones().get(name));}
    public static WeaponProfile get(String id){var d=all().get(id);return d==null?null:d.profile();}
    public static String digest(){all();return digest;}
    public static Map<String,Data> all(){if(loaded==null)load();return loaded;}
    private static synchronized void load(){
        if(loaded!=null)return;
        try{
            var values=new TreeMap<String,Data>();var source=new StringBuilder();
            try(var in=DescriptorProfiles.class.getResourceAsStream("/gun-profiles.toml")){
                if(in==null)throw new IOException("Missing gun-profiles.toml");String text=new String(in.readAllBytes(),StandardCharsets.UTF_8);values.putAll(parse(text));source.append(text);
            }
            Path config=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
            Path dir=config==null?null:config.resolve("visor_tacz/profiles");
            if(dir!=null&&Files.isDirectory(dir))try(var files=Files.list(dir)){
                for(var path:files.filter(p->p.getFileName().toString().endsWith(".toml")).sorted().toList()){
                    if(Files.size(path)>262144)throw new IOException("Profile exceeds 256 KiB: "+path.getFileName());
                    String text=Files.readString(path);values.putAll(parse(text));source.append("\n").append(path.getFileName()).append("\n").append(text);
                }
            }
            digest=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(source.toString().getBytes(StandardCharsets.UTF_8)));
            for(var d:values.values())DETAILS.put(d.profile(),d);loaded=Map.copyOf(values);
        }catch(Exception e){com.mojang.logging.LogUtils.getLogger().error("TaCZ VR descriptor profiles disabled: {}",e.getMessage());loaded=Map.of();digest="invalid";}
    }
    public static Map<String,Data> parse(String text){
        Config root=new TomlParser().parse(new StringReader(text));
        if(((Number)root.getOrElse("schema",0)).intValue()!=1)throw new IllegalArgumentException("Expected schema 1");
        List<Config> guns=root.get("guns");if(guns==null||guns.size()>128)throw new IllegalArgumentException("Expected at most 128 guns");
        var result=new LinkedHashMap<String,Data>();
        for(Config g:guns){
            String id=required(g,"id");if(!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Invalid gun ID");
            if(!required(g,"display").equals("tacz:default")||!required(g,"model").equals("tacz_resolved_display")||!required(g,"ammo_adapter").equals("native_rounds"))throw new IllegalArgumentException("Unsupported display/model/ammo adapter: "+id);
            var mechanism=WeaponProfile.Mechanism.valueOf(required(g,"variant"));
            String template=required(g,"mechanism");
            if(!template.equals(template(mechanism)))throw new IllegalArgumentException("Mechanism/variant mismatch: "+id);
            var grip=vector(g,"geometry.grip_pixels",128);var muzzle=vector(g,"geometry.muzzle_pixels",256);
            float scale=number(g,"geometry.scale"),support=number(g,"geometry.support_distance");if(scale<=0||scale>2||support<0||support>2)throw new IllegalArgumentException("Geometry out of range");
            var profile=new WeaponProfile(scale,grip.x,grip.y,grip.z,muzzle.x,muzzle.y,muzzle.z,support,mechanism);
            String mag=required(g,"parts.magazine.node"),rack=required(g,"parts.action.node"),bolt=g.getOrElse("parts.bolt.node","");
            if(!mag.matches("[a-zA-Z0-9_.-]+")||!rack.matches("[a-zA-Z0-9_.-]+")||!bolt.matches("[a-zA-Z0-9_.-]*")||rack.equals(bolt))throw new IllegalArgumentException("Invalid or duplicate action/bolt nodes");
            // This phase implements the existing template joint only. Reject unimplemented motion rather than ignoring it.
            if(!required(g,"joints.action.kind").equals("template")||!required(g,"joints.action.frame").equals("gun"))throw new IllegalArgumentException("Only tested template joints are supported");
            Config joints=g.get("joints");if(!joints.valueMap().keySet().equals(Set.of("action")))throw new IllegalArgumentException("Only the template action joint is supported");
            Config action=g.get("joints.action");if(!action.valueMap().keySet().equals(Set.of("kind","frame")))throw new IllegalArgumentException("Custom joint axes/limits are not implemented");
            var zones=new LinkedHashMap<String,Vector3f>();for(String name:List.of("magazine","rack","support","release","selector","port")){
                Config zone=g.get("zones."+name);if(zone==null||!zone.valueMap().keySet().equals(Set.of("position")))throw new IllegalArgumentException("Zones accept position only; sizes stay in calibration JSON");
                zones.put(name,vector(g,"zones."+name+".position",2));
            }
            for(String unsupported:List.of("bindings","poses","transitions"))if(g.contains(unsupported))throw new IllegalArgumentException("Custom "+unsupported+" not implemented; use the mechanism template");
            PistolProfiles.Data pistol=null;
            if(g.contains("pistol"))pistol=new PistolProfiles.Data(id.substring(id.indexOf(':')+1),rack,g.getOrElse("pistol.cylinder",false),g.getOrElse("pistol.selector",false),required(g,"pistol.ammo"),number(g,"pistol.open_degrees"),vector(g,"pistol.shell_pixels",256),new Vector3f(zones.get("magazine")),new Vector3f(zones.get("rack")),required(g,"pistol.sound_folder"),required(g,"pistol.mag_out"),required(g,"pistol.mag_in"),required(g,"pistol.action_back"),required(g,"pistol.action_close"));
            Visual visual=new Visual(g.getOrElse("visual.shot_node",""),g.contains("visual.shot_travel")?number(g,"visual.shot_travel"):0,g.getOrElse("visual.cylinder",""),g.getOrElse("visual.hammer",""),g.contains("visual.hammer_degrees")?number(g,"visual.hammer_degrees"):0,nodes(g,"visual.rounds"),nodes(g,"visual.heads"),g.getOrElse("visual.loader",""),nodes(g,"visual.hidden"));
            if(visual.shotTravel()<0||visual.shotTravel()>.15f||Math.abs(visual.hammerDegrees())>180||visual.rounds().size()>64||(!visual.heads().isEmpty()&&visual.heads().size()!=visual.rounds().size()))throw new IllegalArgumentException("Invalid cosmetic motion: "+id);
            boolean cylinder=Set.of(WeaponProfile.Mechanism.RHINO357,WeaponProfile.Mechanism.TAURUS500,WeaponProfile.Mechanism.TAURUS943,WeaponProfile.Mechanism.LONETRAIL).contains(mechanism);
            if(cylinder&&(pistol==null||!pistol.cylinder())||!cylinder&&pistol!=null&&pistol.cylinder())throw new IllegalArgumentException("Cylinder metadata/template mismatch: "+id);
            if(pistol!=null&&(Math.abs(pistol.openDegrees())>180||!pistol.ammo().matches("[a-z0-9_.-]+:[a-z0-9_./-]+")))throw new IllegalArgumentException("Invalid pistol metadata: "+id);
            for(String mode:nodes(g,"manual_cycle_modes"))if(!Set.of("SEMI","BURST","AUTO").contains(mode)||!template.equals("closed_bolt_magazine"))throw new IllegalArgumentException("Unsupported manual cycle mode: "+id);
            if(result.put(id,new Data(id,profile,mag,rack,bolt.isEmpty()?null:bolt,Map.copyOf(zones),pistol,visual,nodes(g,"manual_cycle_modes")))!=null)throw new IllegalArgumentException("Duplicate gun: "+id);
        }
        return Map.copyOf(result);
    }
    public static String template(WeaponProfile.Mechanism m){return switch(m){case PUMP->"pump";case BOLT->"lifted_bolt";case SMG->"locking_charging_handle";case RHINO357,TAURUS500,TAURUS943->"cylinder";case LONETRAIL->"breech";case BUTTON->"button";default->"closed_bolt_magazine";};}
    private static List<String> nodes(Config c,String key){List<?> values=c.getOrElse(key,List.of());var result=new ArrayList<String>();for(Object v:values){if(!(v instanceof String n)||!n.matches("[a-zA-Z0-9_.-]+"))throw new IllegalArgumentException("Invalid node list: "+key);result.add(n);}return List.copyOf(result);}
    private static String required(Config c,String key){Object v=c.get(key);if(!(v instanceof String s)||s.isBlank())throw new IllegalArgumentException("Missing "+key);return s;}
    private static float number(Config c,String key){Object o=c.get(key);if(!(o instanceof Number n)||!Float.isFinite(n.floatValue()))throw new IllegalArgumentException("Invalid "+key);return n.floatValue();}
    private static Vector3f vector(Config c,String key,float max){List<?> a=c.get(key);if(a==null||a.size()!=3)throw new IllegalArgumentException("Expected XYZ: "+key);float[] f=new float[3];for(int i=0;i<3;i++){if(!(a.get(i) instanceof Number n)||!Float.isFinite(n.floatValue())||Math.abs(n.floatValue())>max)throw new IllegalArgumentException("Out of range: "+key);f[i]=n.floatValue();}return new Vector3f(f);}
}
