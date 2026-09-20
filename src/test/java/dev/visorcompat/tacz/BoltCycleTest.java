package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoltCycleTest {
    @Test void newProfilesExposeCorrectMechanismsAndParts(){
        var rifle=Profiles.byId("tacz:m700");var smg=Profiles.byId("tacz:hk_mp5a5");
        assertTrue(rifle.bolt());assertTrue(rifle.manualAction());assertFalse(rifle.selector());assertEquals("bolt",rifle.rackNode());
        assertTrue(smg.smg());assertFalse(smg.manualAction());assertTrue(smg.selector());assertEquals("charge_handle",smg.rackNode());
        assertEquals(.6f,rifle.scale());assertEquals(.6f,smg.scale());
    }
    @Test void muzzleUsesEachModelsGripAndMuzzlePivots(){
        var rifle=Profiles.byId("tacz:m700");
        assertEquals((7.25f-4.05f)*.6f/16,rifle.muzzleOffset().y,1e-6);
        assertEquals((-25-7.175f)*.6f/16,rifle.muzzleOffset().z,1e-6);
        assertEquals((-12.975f-5.65f)*.6f/16,Profiles.byId("tacz:hk_mp5a5").muzzleOffset().z,1e-6);
    }
    @Test void spentCaseEjectsOnceAndForwardFeedsExactlyOne(){
        var rear=BoltCycle.advance(false,true,new Chamber(4,false),PumpCycle.Transition.OPEN,true,true);
        assertEquals(4,rear.effect());assertEquals(4,rear.chamber().magazine());assertTrue(rear.open());assertFalse(rear.spent());
        var held=BoltCycle.advance(rear.open(),rear.spent(),rear.chamber(),PumpCycle.Transition.OPEN,true,true);
        assertEquals(-1,held.effect());assertEquals(rear.chamber(),held.chamber());
        var closed=BoltCycle.advance(true,false,held.chamber(),PumpCycle.Transition.CLOSE,true,true);
        assertFalse(closed.open());assertTrue(closed.chamber().loaded());assertEquals(3,closed.chamber().magazine());
        assertEquals(-1,BoltCycle.advance(false,false,closed.chamber(),PumpCycle.Transition.CLOSE,true,true).effect());
    }
    @Test void liveEjectionDiscardsOnlyChamberedRound(){
        var result=BoltCycle.advance(false,false,new Chamber(4,true),PumpCycle.Transition.OPEN,true,true);
        assertEquals(3,result.effect());assertFalse(result.chamber().loaded());assertEquals(4,result.chamber().magazine());
    }
    @Test void interruptedOrShortStrokeDoesNotFeed(){
        var transition=PumpCycle.transition(false,.04f,0,0);
        var partial=BoltCycle.advance(false,true,new Chamber(4,false),transition,true,true);
        assertFalse(partial.open());assertTrue(partial.spent());assertEquals(-1,partial.effect());
        var canceled=BoltCycle.advance(true,false,new Chamber(4,false),PumpCycle.Transition.NONE,true,true);
        assertTrue(canceled.open());assertFalse(canceled.chamber().loaded());assertEquals(4,canceled.chamber().magazine());
    }
    @Test void noMagazineOrJamPreventsFeeding(){
        for(boolean magazine:new boolean[]{false,true}) {
            var result=BoltCycle.advance(true,false,new Chamber(4,false),PumpCycle.Transition.CLOSE,magazine,false);
            assertFalse(result.chamber().loaded());assertEquals(4,result.chamber().magazine());
        }
        assertFalse(BoltCycle.advance(true,false,new Chamber(4,false),PumpCycle.Transition.CLOSE,false,true).chamber().loaded());
        assertFalse(BoltCycle.advance(true,false,new Chamber(0,false),PumpCycle.Transition.CLOSE,true,true).chamber().loaded());
    }
    @Test void openHandleTargetMovesBackButMagazineTargetStays(){
        var rifle=Profiles.byId("tacz:m700");var c=Calibration.ZERO;
        assertEquals(.08f,Handling.rack(rifle,c,true).z-Handling.rack(rifle,c,false).z,1e-6);
        assertEquals(Handling.Target.RACK,Handling.target(Handling.Phase.NEED_RACK,Handling.rack(rifle,c,true),false,rifle,c,true));
        assertEquals(Handling.Target.MAGAZINE,Handling.target(Handling.Phase.NEED_RACK,Handling.magazine(rifle,c),false,rifle,c,true));
    }
    @Test void rifleOmitsSelectorAndCasingGrabButKeepsSupport(){
        var pages=CalibrationLayout.pages("tacz:m700|tacz:default");
        assertTrue(pages.stream().anyMatch(p->p.id().equals("support")));assertFalse(pages.stream().anyMatch(p->p.id().equals("selector")));
        assertNull(pages.stream().filter(p->p.id().equals("port")).findFirst().orElseThrow().zone());
        assertTrue(CalibrationLayout.pages("tacz:hk_mp5a5|tacz:default").stream().anyMatch(p->p.id().equals("selector")));
    }
}
