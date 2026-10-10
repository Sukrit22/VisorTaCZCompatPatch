package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.CylinderEjection;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;
class CylinderEjectionTest {
    @Test void physicalAlwaysRecoversWhileAdvancedNeedsAnEmptyHandUnderCylinder(){
        assertTrue(CylinderEjection.recoverLive(false,false,false));
        assertTrue(CylinderEjection.recoverLive(false,true,false));
        assertFalse(CylinderEjection.recoverLive(true,true,false));
        assertFalse(CylinderEjection.recoverLive(true,false,true));
        assertTrue(CylinderEjection.recoverLive(true,true,true));
    }
    @Test void catchRequiresHandBelowAndNearCylinder(){var c=new Vector3f();assertTrue(CylinderEjection.canCatch(c,new Vector3f(.05f,-.25f,0),1));assertFalse(CylinderEjection.canCatch(c,new Vector3f(0,-.01f,0),1));assertFalse(CylinderEjection.canCatch(c,new Vector3f(0,-.5f,0),1));assertFalse(CylinderEjection.canCatch(c,new Vector3f(.2f,-.25f,0),1));assertFalse(CylinderEjection.canCatch(c,new Vector3f(0,.2f,0),1));}
    @Test void catchUsesRoomMetresAndRejectsInvalidTracking(){assertTrue(CylinderEjection.canCatch(new Vector3f(),new Vector3f(.1f,-.5f,0),2));assertFalse(CylinderEjection.canCatch(new Vector3f(),new Vector3f(Float.NaN),1));assertFalse(CylinderEjection.canCatch(new Vector3f(),new Vector3f(),0));}
    @Test void fasterPullEjectsFasterAndTrackingSpikesAreCapped(){assertEquals(1,CylinderEjection.pullSpeed(.05f,.05f),1e-5);assertEquals(2,CylinderEjection.pullSpeed(.1f,.05f),1e-5);assertEquals(6,CylinderEjection.pullSpeed(10,.05f));assertEquals(0,CylinderEjection.pullSpeed(.1f,1));assertEquals(0,CylinderEjection.pullSpeed(-.1f,.05f));}
    @Test void gravityStartsGentlerThenAcceleratesContinuously(){float a=CylinderEjection.drop(.05f,true),b=CylinderEjection.drop(.1f,true);assertTrue(a<CylinderEjection.drop(.05f,false));assertTrue(b>2*a);assertEquals(CylinderEjection.drop(.18f-1e-5f,true),CylinderEjection.drop(.18f+1e-5f,true),.0001);assertTrue(CylinderEjection.drop(.3f,true)>b);}
}
