package dev.visorcompat.tacz;

import dev.visorcompat.tacz.physical.*;
import org.joml.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandlingTest {
    @Test void magazineRequiresDownwardTravelNotSidewaysOrUp() {
        var start=new Vector3f(0,0,0);
        assertTrue(Handling.magazinePulled(start,new Vector3f(0,-.07f,0)));
        assertFalse(Handling.magazinePulled(start,new Vector3f(0,.07f,0)));
        assertFalse(Handling.magazinePulled(start,new Vector3f(.2f,-.07f,0)));
        assertFalse(Handling.magazinePulled(start,new Vector3f(0,-.02f,0)));
    }
    @Test void chargingRequiresBackwardTravelNotControllerJitter() {
        var start=new Vector3f(0,0,0);
        assertTrue(Handling.racked(start,new Vector3f(0,0,.06f)));
        assertFalse(Handling.racked(start,new Vector3f(0,0,-.06f)));
        assertFalse(Handling.racked(start,new Vector3f(0,0,.01f)));
        assertFalse(Handling.racked(start,new Vector3f(.2f,0,.06f)));
    }
    @Test void manipulationPhasesBlockShots() {
        for(var phase:Handling.Phase.values()) assertEquals(
            phase==Handling.Phase.PUMP_HOLD || phase==Handling.Phase.READY || phase==Handling.Phase.SUPPORT || phase==Handling.Phase.OLD_MAG
            || phase==Handling.Phase.NO_MAG || phase==Handling.Phase.NEW_MAG,Handling.fireable(phase));
    }
    @Test void gunLocalGesturesRespectRotationAndWorldScale() {
        var rotation=new Quaternionf().rotateY(.7f);
        var origin=new Vector3f(3,4,5);
        var local=new Vector3f(.1f,-.1f,.2f);
        var world=rotation.transform(new Vector3f(local).mul(2)).add(origin);
        var gun=new GunPose(origin,rotation,new Vector3f(),new Vector3f(),2);
        assertTrue(Handling.local(gun,world).distance(local)<1e-6);
    }
    @Test void pouchFollowsHeadAndScalesWithoutNaNWhenLookingStraightUp() {
        var pouch=Handling.pouch(new Vector3f(1,2,3),new Vector3f(0,1,0),2);
        assertTrue(pouch.distance(new Vector3f(1,.7f,2.64f))<1e-6);
    }
    @Test void emptyReloadUnchamberThenRackConservesEveryRound() {
        for(int n=0;n<=100;n++) {
            var loaded=new Chamber(n,true);
            assertEquals(loaded.rounds(),loaded.unchamber().rounds());
            assertEquals(loaded.rounds(),loaded.unchamber().rack().rounds());
        }
    }
    @Test void rackingLoadedChamberEjectsExactlyOneAndNeverCreatesAmmo() {
        for(int n=0;n<=100;n++) {
            var loaded=new Chamber(n,true);
            assertEquals(loaded.rounds()-1,loaded.rack().rounds());
            var empty=new Chamber(n,false);
            assertEquals(empty.rounds(),empty.rack().rounds());
        }
    }
    @Test void invalidPositionsCannotEnterAnInteractionZone() {
        assertFalse(Handling.near(new Vector3f(Float.NaN,0,0),new Vector3f(),.1f));
        assertFalse(Handling.racked(new Vector3f(),new Vector3f(Float.NaN,0,.07f)));
    }
    @Test void fullPullEjectsBeforeReturnAndDoesNotFeedYet() {
        var start=new Chamber(16,true);
        var open=start.eject();
        assertEquals(16,open.magazine());assertFalse(open.loaded());
        assertEquals(start.rounds()-1,open.rounds());
        var closed=open.feed();assertEquals(15,closed.magazine());assertTrue(closed.loaded());
        assertEquals(open.rounds(),closed.rounds());
    }
    @Test void holdingOpenDoesNotEjectAdditionalRounds() {
        var chamber=new Chamber(16,true).eject();
        for(int i=0;i<100;i++)chamber=chamber.eject();
        assertEquals(new Chamber(16,false),chamber);
    }
    @Test void interruptedPullLeavesEjectedRoundGoneButMagazineUntouched() {
        var open=new Chamber(9,true).eject();
        assertEquals(9,open.rounds());assertEquals(9,open.magazine());assertFalse(open.loaded());
        assertEquals(9,open.eject().feed().rounds());
    }
    @Test void emptyPullCannotCreateAVisibleLiveRoundOrAmmunition() {
        var open=new Chamber(0,false).eject();assertFalse(open.loaded());assertEquals(0,open.feed().rounds());
        var unchambered=new Chamber(17,false).eject();assertEquals(17,unchambered.rounds());assertEquals(17,unchambered.feed().rounds());
    }
}
