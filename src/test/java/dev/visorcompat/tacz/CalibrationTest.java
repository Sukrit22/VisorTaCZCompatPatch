package dev.visorcompat.tacz;

import com.google.gson.Gson;
import org.joml.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalibrationTest {
    @Test void zeroPreservesPose() {
        var basis=new Quaternionf().rotateY(.5f);
        var origin=new Vector3f(1,2,3);
        assertEquals(origin,Calibration.ZERO.position(origin,basis,2));
        assertTrue(basis.equals(Calibration.ZERO.orientation(basis),1e-6f));
    }
    @Test void translationIsControllerLocalAndWorldScaled() {
        var c=new Calibration(.01f,0,0,0,0,0);
        var basis=new Quaternionf().rotateY((float)(java.lang.Math.PI/2));
        assertTrue(c.position(new Vector3f(1,2,3),basis,2).distance(new Vector3f(1,2,2.98f))<1e-6);
    }
    @Test void rotationMovesMuzzleAndFiringDirectionTogether() {
        var c=new Calibration(0,0,0,0,90,0);
        var rotation=c.orientation(new Quaternionf());
        var direction=rotation.transform(new Vector3f(0,0,-1));
        var muzzle=rotation.transform(new Vector3f(0,0,-.3f));
        assertTrue(direction.distance(new Vector3f(-1,0,0))<1e-6);
        assertTrue(muzzle.distance(new Vector3f(direction).mul(.3f))<1e-6);
    }
    @Test void rejectsNonfiniteAndExcessiveNetworkOffsets() {
        assertFalse(new Calibration(Float.NaN,0,0,0,0,0).valid());
        assertFalse(new Calibration(0,0,0,Float.POSITIVE_INFINITY,0,0).valid());
        assertFalse(new Calibration(.251f,0,0,0,0,0).valid());
        assertFalse(new Calibration(0,0,0,0,181,0).valid());
        assertTrue(new Calibration(.25f,-.25f,0,-180,180,0).valid());
    }
    @Test void jsonRoundTripPreservesAllSixAxes() {
        var c=new Calibration(.01f,-.02f,.03f,12,-34,56);
        var gson=new Gson();
        assertEquals(c,gson.fromJson(gson.toJson(c),Calibration.class));
    }

    @Test void legacyCalibrationDefaultsMuzzleOffsetsToZero() {
        var c=new Gson().fromJson("{\"x\":0.01,\"pitch\":12}",Calibration.class);
        assertEquals(.01f,c.x());
        assertEquals(12,c.pitch());
        assertEquals(new Vector3f(0,0,-.3f),c.muzzleOffset(new Vector3f(0,0,-.3f)));
        assertTrue(c.valid());
    }
    @Test void muzzleOffsetRotatesWithGunWithoutMovingGripOrChangingAim() {
        var c=new Calibration(.01f,.02f,.03f,0,90,0,.01f,.02f,-.05f);
        var gripOnly=new Calibration(.01f,.02f,.03f,0,90,0);
        assertEquals(gripOnly.position(new Vector3f(),new Quaternionf(),1),
            c.position(new Vector3f(),new Quaternionf(),1));
        assertEquals(gripOnly.orientation(new Quaternionf()),c.orientation(new Quaternionf()));
        var local=c.muzzleOffset(new Vector3f(0,0,-.3f));
        var world=c.orientation(new Quaternionf()).transform(local.mul(2));
        assertTrue(world.distance(new Vector3f(-.7f,.04f,-.02f))<1e-6);
        assertEquals(c,new Gson().fromJson(new Gson().toJson(c),Calibration.class));
    }
    @Test void muzzleOffsetsAreBoundedAndFinite() {
        assertFalse(new Calibration(0,0,0,0,0,0,Float.NaN,0,0).valid());
        assertFalse(new Calibration(0,0,0,0,0,0,0,.251f,0).valid());
        assertFalse(new Calibration(0,0,0,0,0,0,0,0,Float.POSITIVE_INFINITY).valid());
    }
}
