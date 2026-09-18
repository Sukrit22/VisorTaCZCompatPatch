package dev.visorcompat.tacz;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.DefaultAssets;
import net.minecraft.world.item.ItemStack;
import java.util.Map;

public final class Profiles {
    private Profiles() {}
    // Derived from the 1.1.8-hotfix default pack's thirdperson_hand and muzzle_flash nodes.
    private static final Map<String, WeaponProfile> PROFILES = Map.of(
        "tacz:glock_17", new WeaponProfile(.65f, 0, 4.225f, -.35f, .00625f, 5.35f, -7.05f, 0),
        "tacz:m4a1", new WeaponProfile(.65f, 0, 6.875f, -2.65f, 0, 9.55f, -26.25f, .30f),
        "tacz:m870", new WeaponProfile(.6f,0,6.825f,5.425f,.0125f,9.125f,-23.5f,.32f,WeaponProfile.Mechanism.PUMP)
    );
    public static boolean pump(ItemStack stack){var p=get(stack);return p!=null && p.pump();}
    public static String key(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun == null ? "" : gun.getGunId(stack) + "|" + gun.getGunDisplayId(stack);
    }
    public static WeaponProfile get(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun == null || !DefaultAssets.DEFAULT_GUN_DISPLAY_ID.equals(gun.getGunDisplayId(stack))
                ? null : PROFILES.get(gun.getGunId(stack).toString());
    }
}
