package dev.visorcompat.tacz.physical;
import dev.visorcompat.tacz.*;
import org.joml.Vector3f;
/** Ejection geometry comes exclusively from the selected TOML profile. */
public record JamProfile(Vector3f port,float frozenTravel){
    public static JamProfile of(WeaponProfile p,Calibration c){var v=DescriptorProfiles.zone(p,"port");return new JamProfile((v==null?new Vector3f():v).add(c.interactions().port().vector()).mul(c.gunScale()),.025f);}
}
