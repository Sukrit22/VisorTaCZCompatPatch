package dev.visorcompat.tacz;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IGun;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

public final class Profiles {

    private Profiles() {}

    public static java.util.Set<String> ids(){return DescriptorProfiles.all().keySet();}
    public static WeaponProfile byId(String id){return DescriptorProfiles.get(id);}

    public static boolean physical(ItemStack stack){var p=get(stack);return p!=null&&p.physical();}

    public static boolean cylinder(ItemStack stack){var p=get(stack);return p!=null&&p.cylinder();}

    public static boolean bolt(ItemStack stack) {
        var p = get(stack);
        return p != null && p.bolt();
    }

    public static boolean manualAction(ItemStack stack) {
        var p = get(stack);
        return p != null && p.manualAction();
    }

    public static boolean pump(ItemStack stack) {
        var p = get(stack);
        return p != null && p.pump();
    }

    public static String key(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun == null
            ? ""
            : gun.getGunId(stack) + "|" + gun.getGunDisplayId(stack);
    }

    public static WeaponProfile get(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun == null ||
            !DefaultAssets.DEFAULT_GUN_DISPLAY_ID.equals(
                gun.getGunDisplayId(stack)
            )
            ? null
            : byId(gun.getGunId(stack).toString());
    }
}
