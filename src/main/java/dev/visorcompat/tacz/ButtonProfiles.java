package dev.visorcompat.tacz;
import org.joml.Vector3f;
/** Derived view of the single TOML catalog. */
public final class ButtonProfiles {
    public static WeaponProfile get(String id){var p=Profiles.byId(id);return p!=null&&p.buttonOnly()?p:null;}
    public static java.util.Set<String> ids(){return Profiles.ids().stream().filter(id->Profiles.byId(id).buttonOnly()).collect(java.util.stream.Collectors.toUnmodifiableSet());}
    public static Vector3f shell(WeaponProfile p){var point=DescriptorProfiles.zone(p,"port");return point==null?null:point.add(p.grip()).mul(16f/p.scale());}
}
