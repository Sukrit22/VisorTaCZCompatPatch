package dev.visorcompat.tacz.physical;

import dev.visorcompat.tacz.Profiles;
import dev.visorcompat.tacz.WeaponProfile;
import net.minecraft.world.item.ItemStack;

/** Foregrip model state only: ammunition remains in TaCZ's native inventory/reload system. */
public final class CzMagazine {
    public static final String ATTACHED="visor_tacz_cz_foregrip",FLIPPED="visor_tacz_mag_flipped";
    public static boolean applies(ItemStack stack){var p=Profiles.get(stack);return p!=null&&p.mechanism()==WeaponProfile.Mechanism.CZ75;}
    public static boolean attached(ItemStack stack){return applies(stack)&&stack.hasTag()&&stack.getTag().getBoolean(ATTACHED);}
    public static boolean flipped(ItemStack stack){return applies(stack)&&stack.hasTag()&&stack.getTag().getBoolean(FLIPPED);}
    public static boolean heldPhase(Handling.Phase phase){return phase==Handling.Phase.OLD_MAG||phase==Handling.Phase.NEW_MAG;}
}
