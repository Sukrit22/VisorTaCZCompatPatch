package dev.visorcompat.tacz;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoseMathTest {
    @Test void minecraftAnglesMatchControllerAxes() {
        assertEquals(180, Math.abs(PoseMath.yaw(new Vector3f(0, 0, -1))), .001);
        assertEquals(-90, PoseMath.yaw(new Vector3f(1, 0, 0)), .001);
        assertEquals(-90, PoseMath.pitch(new Vector3f(0, 1, 0)), .001);
        assertEquals(90, PoseMath.pitch(new Vector3f(0, -1, 0)), .001);
    }
    @Test void unsupportedOrDistantHandDoesNotSteerGun() {
        var q = new Quaternionf().rotateY(.4f);
        assertEquals(q, PoseMath.supportedRotation(q, new Vector3f(), new Vector3f(2, 0, 0), .3f, 1));
        assertEquals(q, PoseMath.supportedRotation(q, new Vector3f(), new Vector3f(0, 0, -.3f), 0, 1));
    }
    @Test void nearbyForegripSteersBothModelAndShotDirection() {
        var target = new Vector3f(.04f, 0, -.3f);
        var q = PoseMath.supportedRotation(new Quaternionf(), new Vector3f(), target, .3f, 1);
        assertTrue(q.transform(new Vector3f(0, 0, -1)).distance(target.normalize()) < 1e-5);
    }
    @Test void muzzleUsesSameScaleAndCoordinateConversionAsModel() {
        var profile = new WeaponProfile(.65f, 0, 4.225f, -.35f, .00625f, 5.35f, -7.05f, 0);
        var muzzle = profile.muzzleOffset();
        assertEquals(-6.7 * .65 / 16, muzzle.z, 1e-6);
        assertTrue(muzzle.y > 0);
        assertTrue(muzzle.x < 0);
    }
    @Test void rejectsNonFiniteTrackingCoordinates() {
        assertFalse(PoseMath.finite(new Vector3f(Float.NaN, 0, 0)));
        assertFalse(PoseMath.finite(new Vector3f(0, Float.POSITIVE_INFINITY, 0)));
    }
}
