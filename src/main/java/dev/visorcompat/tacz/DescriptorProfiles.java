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
    public record Data(String id,WeaponProfile profile,String magazine,String rack,String bolt,Map<String,Vector3f> zones,PistolProfiles.Data pistol,Visual visual,List<String> manualCycleModes,float boltRatio,Map<String,String> sounds) {}
    public record Visual(String shotNode,float shotTravel,String cylinder,String hammer,float hammerDegrees,List<String> rounds,List<String> heads,String loader,List<String> hidden,List<ShotPart> shotFollowers,List<String> shotModes) {}
    public record ShotPart(String node,float travel) {}
    private static volatile Map<String,Data> loaded;
    private static final Map<WeaponProfile,Data> DETAILS=Collections.synchronizedMap(new IdentityHashMap<>());
    private static String digest="";
    public static Data details(WeaponProfile p){return DETAILS.get(p);}
    public static Vector3f zone(WeaponProfile p,String name){var d=details(p);return d==null?PackButtonFallback.zone(p,name):!d.zones().containsKey(name)?null:new Vector3f(d.zones().get(name));}
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
            String mag=g.getOrElse("parts.magazine.node",""),rack=g.getOrElse("parts.action.node",""),bolt=g.getOrElse("parts.bolt.node","");
            boolean detachable=Set.of("closed_bolt_magazine","locking_charging_handle","lifted_bolt").contains(template);
            if(detachable&&mag.isEmpty()||mechanism!=WeaponProfile.Mechanism.BUTTON&&rack.isEmpty())throw new IllegalArgumentException("Missing physical part: "+id);
            if(!mag.matches("[a-zA-Z0-9_.-]*")||!rack.matches("[a-zA-Z0-9_.-]*")||!bolt.matches("[a-zA-Z0-9_.-]*")||!bolt.isEmpty()&&rack.equals(bolt))throw new IllegalArgumentException("Invalid or duplicate action/bolt nodes");
            // This phase implements the existing template joint only. Reject unimplemented motion rather than ignoring it.
            if(!required(g,"joints.action.kind").equals("template")||!required(g,"joints.action.frame").equals("gun"))throw new IllegalArgumentException("Only tested template joints are supported");
            Config joints=g.get("joints");if(!joints.valueMap().keySet().equals(Set.of("action")))throw new IllegalArgumentException("Only the template action joint is supported");
            Config action=g.get("joints.action");if(!Set.of("kind","frame","bolt_ratio").containsAll(action.valueMap().keySet()))throw new IllegalArgumentException("Custom joint axes/limits are not implemented");
            var zones=new LinkedHashMap<String,Vector3f>();for(String name:List.of("magazine","rack","support","release","selector","port")){
                Config zone=g.get("zones."+name);if(zone==null||!zone.valueMap().keySet().equals(Set.of("position")))throw new IllegalArgumentException("Zones accept position only; sizes stay in calibration JSON");
                zones.put(name,vector(g,"zones."+name+".position",2));
            }
            for(String unsupported:List.of("bindings","poses","transitions"))if(g.contains(unsupported))throw new IllegalArgumentException("Custom "+unsupported+" not implemented; use the mechanism template");
            PistolProfiles.Data pistol=null;
            if(g.contains("pistol"))pistol=new PistolProfiles.Data(id.substring(id.indexOf(':')+1),rack,g.getOrElse("pistol.cylinder",false),g.getOrElse("pistol.selector",false),required(g,"pistol.ammo"),number(g,"pistol.open_degrees"),vector(g,"pistol.shell_pixels",256),new Vector3f(zones.get("magazine")),new Vector3f(zones.get("rack")),required(g,"pistol.sound_folder"),g.getOrElse("pistol.mag_out",""),g.getOrElse("pistol.mag_in",""),g.getOrElse("pistol.action_back",""),g.getOrElse("pistol.action_close",""));
            Visual visual=new Visual(g.getOrElse("visual.shot_node",""),g.contains("visual.shot_travel")?number(g,"visual.shot_travel"):0,g.getOrElse("visual.cylinder",""),g.getOrElse("visual.hammer",""),g.contains("visual.hammer_degrees")?number(g,"visual.hammer_degrees"):0,nodes(g,"visual.rounds"),nodes(g,"visual.heads"),g.getOrElse("visual.loader",""),nodes(g,"visual.hidden"),shotParts(g),nodes(g,"visual.shot_modes"));
            if(!visual.shotNode().matches("[a-zA-Z0-9_.-]*")||visual.shotNode().isEmpty()&&visual.shotTravel()!=0||Math.abs(visual.shotTravel())>.3f||Math.abs(visual.hammerDegrees())>180||visual.rounds().size()>64||(!visual.heads().isEmpty()&&visual.heads().size()!=visual.rounds().size()))throw new IllegalArgumentException("Invalid cosmetic motion: "+id);
            float boltRatio=g.contains("joints.action.bolt_ratio")?number(g,"joints.action.bolt_ratio"):1;
            if(Math.abs(boltRatio)>4)throw new IllegalArgumentException("Invalid bolt ratio: "+id);
            for(String mode:visual.shotModes())if(!Set.of("SEMI","BURST","AUTO").contains(mode))throw new IllegalArgumentException("Invalid shot mode: "+id);
            boolean cylinder=Set.of(WeaponProfile.Mechanism.RHINO357,WeaponProfile.Mechanism.TAURUS500,WeaponProfile.Mechanism.TAURUS943,WeaponProfile.Mechanism.LONETRAIL,WeaponProfile.Mechanism.DB_SHORT,WeaponProfile.Mechanism.DB_LONG).contains(mechanism);
            if(cylinder&&(pistol==null||!pistol.cylinder())||!cylinder&&pistol!=null&&pistol.cylinder())throw new IllegalArgumentException("Cylinder metadata/template mismatch: "+id);
            if(pistol!=null&&(Math.abs(pistol.openDegrees())>180||!pistol.ammo().matches("[a-z0-9_.-]+:[a-z0-9_./-]+")))throw new IllegalArgumentException("Invalid pistol metadata: "+id);
            for(String mode:nodes(g,"manual_cycle_modes"))if(!Set.of("SEMI","BURST","AUTO").contains(mode)||!template.equals("closed_bolt_magazine"))throw new IllegalArgumentException("Unsupported manual cycle mode: "+id);
            if(result.put(id,new Data(id,profile,mag,rack,bolt.isEmpty()?null:bolt,Map.copyOf(zones),pistol,visual,nodes(g,"manual_cycle_modes"),boltRatio,sounds(g)))!=null)throw new IllegalArgumentException("Duplicate gun: "+id);
        }
        return Map.copyOf(result);
    }
    public static String template(WeaponProfile.Mechanism m){return switch(m){case PUMP,SPAS12,TUBE_AUTO->"pump";case BOLT->"lifted_bolt";case SMG->"locking_charging_handle";case RHINO357,TAURUS500,TAURUS943->"cylinder";case LONETRAIL,DB_SHORT,DB_LONG->"breech";case BUTTON->"button";default->"closed_bolt_magazine";};}
    /** Null preserves legacy sound selection; an empty configured clip intentionally stays silent. */
    public static String sound(WeaponProfile profile,int cue){
        var d=profile==null?null:details(profile);if(d==null||d.sounds().isEmpty())return null;
        String key=switch(cue){case 0->"mag_out";case 1->"mag_in";case 4->"eject";case 5,6->"action_close";default->"action_back";};
        return d.sounds().getOrDefault(key,"");
    }
    private static Map<String,String> sounds(Config gun){
        Config config=gun.get("sounds");if(config==null)return Map.of();var out=new HashMap<String,String>();
        for(var entry:config.valueMap().entrySet()){
            if(!Set.of("mag_out","mag_in","action_back","action_close","eject").contains(entry.getKey())||!(entry.getValue() instanceof String value)||!value.isEmpty()&&!value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Invalid handling sound");
            out.put(entry.getKey(),(String)entry.getValue());
        }return Map.copyOf(out);
    }
    private static List<ShotPart> shotParts(Config c){
        List<Config> values=c.getOrElse("visual.shot_followers",List.of());var result=new ArrayList<ShotPart>();var seen=new HashSet<String>();
        seen.add(c.getOrElse("visual.shot_node",""));
        for(Config part:values){String node=required(part,"node");float travel=number(part,"travel");
            if(!part.valueMap().keySet().equals(Set.of("node","travel"))||!node.matches("[a-zA-Z0-9_.-]+")||!seen.add(node)||Math.abs(travel)>.3f||result.size()>=8)throw new IllegalArgumentException("Invalid shot follower");
            result.add(new ShotPart(node,travel));
        }return List.copyOf(result);
    }
    private static List<String> nodes(Config c,String key){List<?> values=c.getOrElse(key,List.of());var result=new ArrayList<String>();for(Object v:values){if(!(v instanceof String n)||!n.matches("[a-zA-Z0-9_.-]+"))throw new IllegalArgumentException("Invalid node list: "+key);result.add(n);}return List.copyOf(result);}
    private static String required(Config c,String key){Object v=c.get(key);if(!(v instanceof String s)||s.isBlank())throw new IllegalArgumentException("Missing "+key);return s;}
    private static float number(Config c,String key){Object o=c.get(key);if(!(o instanceof Number n)||!Float.isFinite(n.floatValue()))throw new IllegalArgumentException("Invalid "+key);return n.floatValue();}
    private static Vector3f vector(Config c,String key,float max){List<?> a=c.get(key);if(a==null||a.size()!=3)throw new IllegalArgumentException("Expected XYZ: "+key);float[] f=new float[3];for(int i=0;i<3;i++){if(!(a.get(i) instanceof Number n)||!Float.isFinite(n.floatValue())||Math.abs(n.floatValue())>max)throw new IllegalArgumentException("Out of range: "+key);f[i]=n.floatValue();}return new Vector3f(f);}
}
