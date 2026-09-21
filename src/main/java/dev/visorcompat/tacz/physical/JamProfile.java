package dev.visorcompat.tacz.physical;

import dev.visorcompat.tacz.*;
import org.joml.Vector3f;

/** Default-pack ejection-port geometry; deliberately separate from jam state rules. */
public record JamProfile(Vector3f port, float frozenTravel) {
    public static JamProfile of(
        WeaponProfile profile,
        Calibration calibration
    ) {
        // Default-pack shell pivots, converted using the same grip/model transform.
        Vector3f buttonShell=profile.buttonOnly()?ButtonProfiles.shell(profile):null;
        Vector3f shell = buttonShell!=null?buttonShell:PistolProfiles.get(profile)!=null ? new Vector3f(PistolProfiles.get(profile).shell()) : profile.m1911()
            ? new Vector3f(0, 5.55f, .625f)
            : profile.bolt()
              ? new Vector3f(.375f, 7.4375f, 5.975f)
              : profile.smg()
                ? new Vector3f(.2f, 11.35f, 1.975f)
                : profile.pump()
                  ? new Vector3f(0, 9, .7f)
                  : profile.supportDistance() == 0
                    ? new Vector3f(.01875f, 5.375f, 1.35f)
                    : new Vector3f(.2f, 9.75f, -6.025f);
        return new JamProfile(
            shell
                .mul(profile.scale() / 16f)
                .sub(profile.grip())
                .add(calibration.interactions().port().vector())
                .mul(calibration.gunScale()),
            .025f
        );
    }
}
