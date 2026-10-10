package dev.visorcompat.tacz.physical;
import dev.visorcompat.tacz.WeaponProfile;
/** SPAS uses TaCZ's BURST enum for self-loading and SEMI for pump action. */
public final class TubeCycle {
    private TubeCycle() {}
    public static boolean manual(WeaponProfile.Mechanism mechanism,String nativeMode){
        return mechanism==WeaponProfile.Mechanism.PUMP || mechanism==WeaponProfile.Mechanism.SPAS12 && !"BURST".equals(nativeMode);
    }
}
