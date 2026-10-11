package dev.visorcompat.tacz;

/** Conservative, common-side geometry for valid pack guns without a matching descriptor.
 * These are calibration starting points, not measured pack model coordinates. */
public final class PackButtonFallback {
    private PackButtonFallback() {}

    private static final WeaponProfile GENERIC = new WeaponProfile(
        .65f, 0, 8, 0, 0, 9, -12, .20f, WeaponProfile.Mechanism.BUTTON);
    private static final WeaponProfile PISTOL = new WeaponProfile(
        .65f, 0, 8, 0, 0, 9, -7, 0, WeaponProfile.Mechanism.BUTTON);

    public static WeaponProfile resolve(WeaponProfile bundled, boolean defaultDisplay, boolean installed) {
        return resolve(bundled, defaultDisplay, installed, "");
    }

    public static WeaponProfile resolve(WeaponProfile bundled, boolean defaultDisplay, boolean installed, String type) {
        if (!installed) return null;
        if (defaultDisplay && bundled != null) return bundled;
        return "pistol".equalsIgnoreCase(type) ? PISTOL : GENERIC;
    }

    public static boolean generic(WeaponProfile profile) { return profile == GENERIC || profile == PISTOL; }

    public static org.joml.Vector3f zone(WeaponProfile profile, String name) {
        if (!generic(profile)) return null;
        return switch (name) {
            case "support" -> new org.joml.Vector3f(0, -.015f, -profile.supportDistance());
            default -> null;
        };
    }
}
