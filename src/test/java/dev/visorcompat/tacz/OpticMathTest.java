package dev.visorcompat.tacz;

import org.joml.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpticMathTest {
    private final Vector3f sight=new Vector3f(0,.04f,-.15f);
    private final Vector3f forward=new Vector3f(0,0,-1);
    @Test void acceptsEyeBehindAlignedSight() {
        assertTrue(OpticMath.aligned(new Vector3f(0,.04f,.15f),forward,sight,false));
    }
    @Test void hysteresisAvoidsRepeatedAdsAtEdge() {
        var eye=new Vector3f(.045f,.04f,.15f);
        assertFalse(OpticMath.aligned(eye,forward,sight,false));
        assertTrue(OpticMath.aligned(eye,forward,sight,true));
        assertFalse(OpticMath.aligned(new Vector3f(.06f,.04f,.15f),forward,sight,true));
    }
    @Test void rejectsWrongSideTooFarAndHeadTurnedAway() {
        assertFalse(OpticMath.aligned(new Vector3f(0,.04f,-.2f),forward,sight,false));
        assertFalse(OpticMath.aligned(new Vector3f(0,.04f,1),forward,sight,true));
        assertFalse(OpticMath.aligned(new Vector3f(0,.04f,.15f),new Vector3f(1,0,0),sight,true));
        assertFalse(OpticMath.aligned(new Vector3f(Float.NaN,0,0),forward,sight,true));
    }
    @Test void projectsPerEyeAimWithAsymmetricProjection() {
        var projection=new Matrix4f().frustum(-.08f,.12f,-.1f,.1f,.1f,100);
        var uv=OpticMath.project(new Vector3f(0,0,-10),projection);
        assertNotNull(uv);assertEquals(.4f,uv.x,1e-5);assertEquals(.5f,uv.y,1e-5);
        assertNull(OpticMath.project(new Vector3f(0,0,10),projection));
        assertNull(OpticMath.project(new Vector3f(Float.NaN,0,-10),projection));
    }
}
