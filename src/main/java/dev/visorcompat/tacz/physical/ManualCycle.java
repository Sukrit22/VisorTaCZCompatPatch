package dev.visorcompat.tacz.physical;
import dev.visorcompat.tacz.*;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.item.ItemStack;
/** Native ammo stays untouched; this latch only requires a physical stroke after firing. */
public final class ManualCycle {
    public static final String SPENT="visor_tacz_manual_cycle_spent";
    public static boolean enabled(ItemStack stack){var p=Profiles.get(stack);var d=p==null?null:DescriptorProfiles.details(p);var gun=IGun.getIGunOrNull(stack);return d!=null&&gun!=null&&d.manualCycleModes().contains(gun.getFireMode(stack).name());}
    public static boolean spent(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(SPENT);}
}
