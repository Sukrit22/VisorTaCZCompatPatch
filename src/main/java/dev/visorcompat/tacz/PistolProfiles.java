package dev.visorcompat.tacz;
import java.util.Map;
import org.joml.Vector3f;
import dev.visorcompat.tacz.WeaponProfile.Mechanism;
/** Pinned default-pack parts and sound assets. Interaction points are calibratable starting estimates. */
public final class PistolProfiles {
    public record Data(String id,String rack,boolean cylinder,boolean selector,String ammo,float openDegrees,
                       Vector3f shell,Vector3f magazine,Vector3f rackPoint,String folder,
                       String magOut,String magIn,String actionBack,String actionClose) {
        public String sound(int kind){return folder+"/"+switch(kind){case 0->magOut;case 1->magIn;case 5,6->actionClose;default->actionBack;};}
    }
    public static Data get(WeaponProfile p){var d=p==null?null:DescriptorProfiles.details(p);return d==null?null:d.pistol();}
}
