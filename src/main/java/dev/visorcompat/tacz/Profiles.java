package dev.visorcompat.tacz;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IGun;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

public final class Profiles {

    private Profiles() {}

    // Derived from the 1.1.8-hotfix default pack's thirdperson_hand and muzzle_flash nodes.
    private static final Map<String, WeaponProfile> PROFILES = Map.ofEntries(
        Map.entry(
            "tacz:glock_17",
            new WeaponProfile(.65f, 0, 4.225f, -.35f, .00625f, 5.35f, -7.05f, 0)
        ),
        Map.entry(
            "tacz:m4a1",
            new WeaponProfile(.65f, 0, 6.875f, -2.65f, 0, 9.55f, -26.25f, .30f)
        ),
        Map.entry(
            "tacz:m870",
            new WeaponProfile(
                .6f,
                0,
                6.825f,
                5.425f,
                .0125f,
                9.125f,
                -23.5f,
                .32f,
                WeaponProfile.Mechanism.PUMP
            )
        ),
        Map.entry(
            "tacz:m700",
            new WeaponProfile(
                .6f,
                .125f,
                4.05f,
                7.175f,
                0,
                7.25f,
                -25,
                .30f,
                WeaponProfile.Mechanism.BOLT
            )
        ),
        Map.entry(
            "tacz:hk_mp5a5",
            new WeaponProfile(
                .6f,
                0,
                7.925f,
                5.65f,
                0,
                10.875f,
                -12.975f,
                .25f,
                WeaponProfile.Mechanism.SMG
            )
        ),
        Map.entry(
            "tacz:m1911",
            new WeaponProfile(
                .6f,
                0,
                4.075f,
                -.15f,
                0,
                5.475f,
                -7.875f,
                0,
                WeaponProfile.Mechanism.M1911
            )
        ),
        Map.entry("tacz:p320", new WeaponProfile(0.6f, 0f, 8f, 1.75f, 0f, 9.5f, -4.4375f, 0f, WeaponProfile.Mechanism.P320)),
        Map.entry("tacz:m9a4", new WeaponProfile(0.6f, 0f, 8f, 1.75f, 0f, 9.5625f, -4.95312f, 0f, WeaponProfile.Mechanism.M9A4)),
        Map.entry("tacz:deagle", new WeaponProfile(0.6f, 0f, 4.05f, 5.325f, 0f, 5.875f, -3.2f, 0f, WeaponProfile.Mechanism.DEAGLE)),
        Map.entry("tacz:deagle_golden", new WeaponProfile(0.6f, 0f, 4.05f, 5.325f, 0f, 5.875f, -3.2f, 0f, WeaponProfile.Mechanism.DEAGLE_GOLDEN)),
        Map.entry("tacz:timeless50", new WeaponProfile(0.6f, 0f, 8f, 1.75f, 0f, 9.25f, -5.6875f, 0f, WeaponProfile.Mechanism.TIMELESS50)),
        Map.entry("tacz:b93r", new WeaponProfile(0.6f, 0f, 8f, 1.75f, 0f, 9.65625f, -6.125f, 0f, WeaponProfile.Mechanism.B93R)),
        Map.entry("tacz:cz75", new WeaponProfile(0.6f, 0f, 4.75f, 0f, 0f, 6.2f, -7.35f, 0f, WeaponProfile.Mechanism.CZ75)),
        Map.entry("tacz:hk_mk23", new WeaponProfile(0.6f, 0f, 5.25f, 5.5f, 0f, 6.9375f, -2.35937f, 0f, WeaponProfile.Mechanism.HK_MK23)),
        Map.entry("tacz:rhino357", new WeaponProfile(0.6f, 0f, 8.0125f, 2.8f, 0f, 9.02589f, -5.5625f, 0f, WeaponProfile.Mechanism.RHINO357)),
        Map.entry("tacz:taurus500", new WeaponProfile(0.6f, 0f, 8.1f, 5.5875f, 0f, 10.75f, -9.53125f, 0f, WeaponProfile.Mechanism.TAURUS500)),
        Map.entry("tacz:taurus943", new WeaponProfile(0.6f, 0f, 4.225f, 1.00938f, 0f, 5.9875f, -5.3125f, 0f, WeaponProfile.Mechanism.TAURUS943)),
        Map.entry("tacz:lonetrail", new WeaponProfile(0.6f, 0f, 6.63125f, 2.74375f, 0.00863f, 8.11642f, -12.24776f, 0f, WeaponProfile.Mechanism.LONETRAIL))
    );

    public static WeaponProfile byId(String id) {
        return PROFILES.get(id);
    }

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
            : PROFILES.get(gun.getGunId(stack).toString());
    }
}
