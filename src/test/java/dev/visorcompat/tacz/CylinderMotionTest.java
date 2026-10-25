package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.CylinderMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CylinderMotionTest {
    @Test void closeRequiresDistanceEffortThenStop(){
        var m=new CylinderMotion();assertEquals(0,m.update(0,.05f,true));
        assertEquals(0,m.update(.04f,.05f,true));assertEquals(0,m.update(.085f,.05f,true));
        assertEquals(-1,m.update(.085f,.05f,true));
    }
    @Test void slowDriftAndShortMovementDoNotClose(){
        var m=new CylinderMotion();for(int i=0;i<20;i++)assertEquals(0,m.update(i*.005f,.05f,true));
        assertEquals(0,m.update(.095f,.05f,true));assertEquals(0,m.update(.095f,.05f,true));
    }
    @Test void repeatedSmallShakesNeverAccumulateIntoClose(){
        var m=new CylinderMotion();m.update(0,.05f,true);
        for(int i=0;i<100;i++){
            assertEquals(0,m.update(.025f,.05f,true));
            assertEquals(0,m.update(0,.05f,true));
        }
    }
    @Test void slowMovementThenSmallFlickDoesNotUseOldDistance(){
        var m=new CylinderMotion();m.update(0,.05f,true);
        for(int i=1;i<=20;i++)assertEquals(0,m.update(i*.005f,.05f,true));
        assertEquals(0,m.update(.125f,.05f,true));assertEquals(0,m.update(.125f,.05f,true));
    }
    @Test void expiredStrokeCannotCloseLater(){
        var m=new CylinderMotion();m.update(0,.05f,true);m.update(.04f,.05f,true);
        for(int i=1;i<=10;i++)assertEquals(0,m.update(.04f+i*.006f,.05f,true));
        assertEquals(0,m.update(.10f,.05f,true));
    }
    @Test void openingNeedsUseThenTwoCentimetresLeft(){
        var m=new CylinderMotion();assertEquals(0,m.update(0,.05f,false));assertEquals(0,m.update(-.03f,.05f,false));
        m.armOpen();assertEquals(0,m.update(0,.05f,false));assertEquals(0,m.update(-.01f,.05f,false));assertEquals(1,m.update(-.025f,.05f,false));
        assertEquals(0,m.update(-.06f,.05f,false));
    }
    @Test void trackingGapCancelsUseArm(){var m=new CylinderMotion();m.armOpen();m.update(0,.05f,false);assertEquals(0,m.update(-1,1,false));assertEquals(0,m.update(-1.1f,.05f,false));}
    @Test void completedStrokeSurvivesEndpointJitterAndClosesOnDeceleration(){
        var m=new CylinderMotion();m.update(0,.05f,true);m.update(.04f,.05f,true);m.update(.085f,.05f,true);
        assertEquals(-1,m.update(.083f,.05f,true));
        m.reset();m.update(0,.05f,true);m.update(.04f,.05f,true);m.update(.08f,.05f,true);
        assertEquals(-1,m.update(.095f,.05f,true));
    }
    @Test void smoothDecelerationBeforeEightCentimetresCanFinishStroke(){
        var m=new CylinderMotion();m.update(0,.05f,true);m.update(.04f,.05f,true);
        assertEquals(0,m.update(.055f,.05f,true));assertEquals(0,m.update(.07f,.05f,true));
        assertEquals(-1,m.update(.085f,.05f,true));
    }
    @Test void contextualStrokeMatchesDirectionalGuide(){
        assertTrue(dev.visorcompat.tacz.physical.CylinderCycle.stroke(.08f,0,0,true));
        assertFalse(dev.visorcompat.tacz.physical.CylinderCycle.stroke(-.08f,0,0,true));
        assertFalse(dev.visorcompat.tacz.physical.CylinderCycle.stroke(.079f,0,0,true));
        assertTrue(dev.visorcompat.tacz.physical.CylinderCycle.stroke(-.02f,0,0,false));
        assertFalse(dev.visorcompat.tacz.physical.CylinderCycle.stroke(.02f,0,0,false));
        assertFalse(dev.visorcompat.tacz.physical.CylinderCycle.stroke(.08f,.13f,0,true));
    }
}
