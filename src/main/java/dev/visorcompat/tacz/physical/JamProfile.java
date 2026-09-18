package dev.visorcompat.tacz.physical;
import dev.visorcompat.tacz.*;
import org.joml.Vector3f;

/** Initial Glock/M4 geometry only; deliberately separate from jam state rules. */
public record JamProfile(Vector3f port,float frozenTravel) {
    public static JamProfile of(WeaponProfile profile,Calibration calibration) {
        // Default-pack shell pivots, converted using the same grip/model transform.
        Vector3f shell=profile.pump()?new Vector3f(0,9,.7f):profile.supportDistance()==0?new Vector3f(.01875f,5.375f,1.35f):new Vector3f(.2f,9.75f,-6.025f);
        return new JamProfile(shell.mul(profile.scale()/16f).sub(profile.grip()).add(calibration.interactions().port().vector()),.025f);
    }
}
