package dev.visorcompat.tacz.physical;

import dev.visorcompat.tacz.TacticalCalibration;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MagazineGuideTest {
    private MagazineGuide.Result solve(Vector3f p,Quaternionf q,boolean held){return MagazineGuide.solve(p,q,new Vector3f(),new Quaternionf(),TacticalCalibration.DEFAULT,held);}
    @Test void alignedEntryAcquiresButDoesNotFinish(){var r=solve(new Vector3f(.02f,0,0),new Quaternionf(),false);assertTrue(r.engaged());assertFalse(r.seated());assertEquals(0,r.position().x);}
    @Test void reachingSeatWithoutEnteringCannotTeleportInsert(){assertFalse(solve(new Vector3f(0,.06f,0),new Quaternionf(),false).engaged());}
    @Test void capturedRailRejectsSidewaysShakeButTracksDepth(){var r=solve(new Vector3f(.03f,.06f,.02f),new Quaternionf().rotateZ(1),true);assertTrue(r.seated());assertEquals(new Vector3f(0,.06f,0),r.position());assertEquals(new Quaternionf(),r.rotation());}
    @Test void pullingBackAndMovingFarAwayDisengage(){assertFalse(solve(new Vector3f(0,-.03f,0),new Quaternionf(),true).engaged());assertFalse(solve(new Vector3f(.2f,0,0),new Quaternionf(),true).engaged());}
    @Test void angularMisalignmentAndInvalidSamplesCannotAcquire(){assertFalse(solve(new Vector3f(),new Quaternionf().rotateZ(1.5f),false).engaged());assertFalse(solve(new Vector3f(Float.NaN,0,0),new Quaternionf(),true).engaged());}
    @Test void rotatedRailSupportsCalibratedInsertionDirection(){var q=new Quaternionf().rotateZ((float)Math.PI/2);var r=MagazineGuide.solve(new Vector3f(-.06f,0,0),q,new Vector3f(),q,TacticalCalibration.DEFAULT,true);assertTrue(r.seated());assertEquals(-.06f,r.position().x,.00001f);}
}
