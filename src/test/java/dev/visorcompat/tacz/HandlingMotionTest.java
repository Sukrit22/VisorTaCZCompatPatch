package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.joml.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandlingMotionTest {
    @Test void stationaryMagazineFallsWithoutInventedSidewaysKick(){
        var m=ReleaseMotion.between(new Vector3f(),new Quaternionf(),new Vector3f(),new Quaternionf(),new Vector3f(0,-.1f,0),.05f,1);
        assertEquals(0,m.velocity().length(),1e-6);
        assertTrue(TossMotion.advance(new Vector3f(),m.velocity(),9.81f,.1f).y<0);
    }
    @Test void wristRotationAddsTangentialVelocity(){
        var q=new Quaternionf().rotateZ(.1f);var offset=q.transform(new Vector3f(0,-.1f,0));
        var m=ReleaseMotion.between(new Vector3f(),new Quaternionf(),new Vector3f(),q,offset,.05f,1);
        assertTrue(m.velocity().x>.19f);assertEquals(0,m.velocity().dot(offset),1e-5);
    }
    @Test void trackingJumpHasBoundedMagazineSpeed(){
        var m=ReleaseMotion.between(new Vector3f(),new Quaternionf(),new Vector3f(100),new Quaternionf(),new Vector3f(),.05f,1);
        assertEquals(6,m.velocity().length(),1e-5);
    }
    @Test void supportRetentionDoesNotEnlargeAcquisition(){
        var point=new Vector3f(.22f,0,0);var center=new Vector3f();
        assertFalse(Handling.inside(point,center,Calibration.ZERO,ZoneSizes.Zone.SUPPORT));
        assertTrue(Handling.supportRetained(point,center,Calibration.ZERO));
        assertFalse(Handling.supportRetained(new Vector3f(.5f,0,0),center,Calibration.ZERO));
    }
    @Test void inertiaNeedsBackAndForwardAndCannotOverrunStops(){
        var closed=new InertialPump(0,0);
        assertEquals(closed,closed.step(0,0,.05f));
        var open=closed.step(3,0,.05f);
        assertEquals(PumpCycle.TRAVEL,open.travel(),1e-6);
        assertEquals(PumpCycle.Transition.OPEN,PumpCycle.transition(false,open.travel(),0,0));
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(true,open.travel(),0,0));
        var forward=open.step(-3,0,.05f);
        assertEquals(0,forward.travel(),1e-6);
        assertEquals(PumpCycle.Transition.CLOSE,PumpCycle.transition(true,forward.travel(),0,0));
    }
    @Test void invalidInertiaSampleCannotMoveAction(){
        var state=new InertialPump(.04f,0);
        assertEquals(state,state.step(Float.NaN,0,.05f));
        assertEquals(state,state.step(3,0,1));
    }
}
