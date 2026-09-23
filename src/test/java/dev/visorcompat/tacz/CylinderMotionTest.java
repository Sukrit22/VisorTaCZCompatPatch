package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.CylinderMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CylinderMotionTest {
    @Test void closeRequiresDistanceEffortThenStop(){
        var m=new CylinderMotion();assertEquals(0,m.update(0,.05f,true));
        assertEquals(0,m.update(.04f,.05f,true));assertEquals(0,m.update(.085f,.05f,true));
        assertEquals(0,m.update(.085f,.05f,true));assertEquals(-1,m.update(.085f,.05f,true));
    }
    @Test void slowDriftAndShortMovementDoNotClose(){
        var m=new CylinderMotion();for(int i=0;i<20;i++)assertEquals(0,m.update(i*.005f,.05f,true));
        assertEquals(0,m.update(.095f,.05f,true));assertEquals(0,m.update(.095f,.05f,true));
    }
    @Test void openingNeedsUseThenTwoCentimetresLeft(){
        var m=new CylinderMotion();assertEquals(0,m.update(0,.05f,false));assertEquals(0,m.update(-.03f,.05f,false));
        m.armOpen();assertEquals(0,m.update(0,.05f,false));assertEquals(0,m.update(-.01f,.05f,false));assertEquals(1,m.update(-.025f,.05f,false));
        assertEquals(0,m.update(-.06f,.05f,false));
    }
    @Test void trackingGapCancelsUseArm(){var m=new CylinderMotion();m.armOpen();m.update(0,.05f,false);assertEquals(0,m.update(-1,1,false));assertEquals(0,m.update(-1.1f,.05f,false));}
}
