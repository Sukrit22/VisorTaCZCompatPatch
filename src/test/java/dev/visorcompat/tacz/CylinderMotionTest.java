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
    @Test void openingCanBeConfiguredWithoutUse(){
        var m=new CylinderMotion();assertEquals(0,m.update(0,.05f,false,false));
        assertEquals(1,m.update(-.025f,.05f,false,false));
    }
    @Test void closingCanRequireUseAndConsumesThePress(){
        var m=new CylinderMotion();m.update(0,.05f,true,true);m.update(.04f,.05f,true,true);
        m.update(.085f,.05f,true,true);assertEquals(0,m.update(.085f,.05f,true,true));
        m.armOpen();m.update(0,.05f,true,true);m.update(.04f,.05f,true,true);m.update(.085f,.05f,true,true);
        assertEquals(-1,m.update(.085f,.05f,true,true));
        assertEquals(0,m.update(.2f,.05f,true,true));
    }
    @Test void closingUsePermissionExpiresAndTrackingLossCancelsIt(){
        var m=new CylinderMotion();m.armOpen();for(int i=0;i<26;i++)assertEquals(0,m.update(0,.05f,true,true));
        m.update(.04f,.05f,true,true);m.update(.085f,.05f,true,true);assertEquals(0,m.update(.085f,.05f,true,true));
        m.armOpen();m.update(0,.05f,true,true);m.update(.04f,.05f,true,true);m.update(.085f,.5f,true,true);
        assertEquals(0,m.update(.085f,.05f,true,true));
    }
    @Test void slowEightCentimetreSettleDoesNotCloseWithoutASnap(){
        // Reaches the full 8 cm and comes to rest, but the stroke never reached a real
        // flick speed: peak 0.4 m/s < CLOSE_FLICK_SPEED. This is the "move a little and
        // it closes" false positive the gate eliminates.
        var m=new CylinderMotion();m.update(0,.05f,true);
        m.update(.02f,.05f,true);m.update(.04f,.05f,true);
        m.update(.06f,.05f,true);m.update(.08f,.05f,true);
        assertEquals(0,m.update(.08f,.05f,true));
        assertEquals(0,m.update(.08f,.05f,true));
    }
    @Test void realFlickStillClosesAfterDeceleration(){
        var m=new CylinderMotion();m.update(0,.05f,true);
        m.update(.04f,.05f,true);m.update(.085f,.05f,true);
        assertEquals(-1,m.update(.085f,.05f,true));
    }

    @Test void cooldownDiscardsStrokeThatStartedBeforeExpiry(){
        var m=new CylinderMotion();
        m.update(0,.05f,true,false,true);
        m.update(.04f,.05f,true,false,true); // returning hand at 350 ms
        assertEquals(0,m.update(.085f,.05f,true,false,false)); // 400 ms: new baseline
        assertEquals(0,m.update(.085f,.05f,true,false,false));
        m.update(.125f,.05f,true,false,false);m.update(.17f,.05f,true,false,false);
        assertEquals(-1,m.update(.17f,.05f,true,false,false)); // deliberate new flick
    }
    @Test void blockedMotionCannotKeepUsePermission(){
        var m=new CylinderMotion();m.armOpen();
        m.update(0,.05f,true,true,true);
        m.update(.04f,.05f,true,true,false);m.update(.085f,.05f,true,true,false);
        assertEquals(0,m.update(.085f,.05f,true,true,false));
    }
    @Test void headAndWorldChangesCannotMoveAStationaryRoomHand(){
        var m=new CylinderMotion();
        // Server uses the fixed room axis captured at arm/start, never the changing HMD axis.
        for(float yaw:new float[]{0,.4f,.8f,1.2f,1.6f}){
            var sample=new dev.visorcompat.tacz.physical.RoomFlickSample(.25f,1,.4f,-.25f,1,.4f,(float)Math.cos(yaw),(float)Math.sin(yaw));
            assertTrue(sample.valid());
            assertEquals(0,m.update(sample.lateral(false,1,0),.05f,true,false));
        }
    }
    @Test void roomHandFlickWorksForEitherHoldingHand(){
        for(boolean off:new boolean[]{false,true}){
            var m=new CylinderMotion();int action=0;
            for(float x:new float[]{0,.04f,.085f,.085f}){
                var sample=new dev.visorcompat.tacz.physical.RoomFlickSample(off?0:x,1,0,off?x:0,1,0,1,0);
                action=m.update(sample.lateral(off,1,0),.05f,true,false);
            }
            assertEquals(-1,action);
        }
    }
    @Test void malformedRoomSamplesAreRejected(){
        assertFalse(new dev.visorcompat.tacz.physical.RoomFlickSample(Float.NaN,0,0,0,0,0,1,0).valid());
        assertFalse(new dev.visorcompat.tacz.physical.RoomFlickSample(0,0,0,0,0,0,0,0).valid());
        assertFalse(new dev.visorcompat.tacz.physical.RoomFlickSample(1000,0,0,0,0,0,1,0).valid());
    }
}
