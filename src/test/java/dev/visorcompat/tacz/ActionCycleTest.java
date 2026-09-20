package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static dev.visorcompat.tacz.physical.ActionCycle.Bolt.*;
class ActionCycleTest {
    @Test void rifleRequiresLiftBeforeRearwardAndForwardBeforeLower(){
        assertEquals(LOWERED,ActionCycle.bolt(LOWERED,.09f,0,0));
        assertEquals(LIFTED,ActionCycle.bolt(LOWERED,0,.04f,0));
        assertEquals(OPEN,ActionCycle.bolt(LIFTED,.09f,.04f,0));
        assertEquals(OPEN,ActionCycle.bolt(OPEN,.09f,0,0));
        assertEquals(OPEN,ActionCycle.bolt(OPEN,0,0,0));
        assertEquals(LIFTED,ActionCycle.bolt(OPEN,0,.04f,0));
        assertEquals(LOWERED,ActionCycle.bolt(LIFTED,0,0,0));
    }
    @Test void invalidAndPartialStrokesPreserveSavedState(){
        for(var state:ActionCycle.Bolt.values()){
            assertEquals(state,ActionCycle.bolt(state,Float.NaN,0,0));
            assertEquals(state,ActionCycle.bolt(state,.08f,.04f,.3f));
        }
        assertEquals(LIFTED,ActionCycle.bolt(LIFTED,.04f,.04f,0));
        assertEquals(OPEN,ActionCycle.bolt(OPEN,.04f,.04f,0));
    }
    @Test void releasingBoltCannotCreateOrDuplicateAmmo(){
        var start=new Chamber(30,false);
        var loaded=ActionCycle.release(start,true,true,false);
        assertEquals(new Chamber(29,true),loaded);assertEquals(start.rounds(),loaded.rounds());
        assertEquals(loaded,ActionCycle.release(loaded,true,true,false));
        assertEquals(start,ActionCycle.release(start,false,true,false));
        assertEquals(start,ActionCycle.release(start,true,false,false));
        assertEquals(start,ActionCycle.release(start,true,true,true));
        assertEquals(new Chamber(0,false),ActionCycle.release(new Chamber(0,false),true,true,false));
    }
    @Test void mp5NeedsRaisedRearwardHandleAndDownwardSlap(){
        assertFalse(ActionCycle.latch(.06f,0,0));assertFalse(ActionCycle.latch(0,.04f,0));
        assertTrue(ActionCycle.latch(.06f,.04f,0));assertFalse(ActionCycle.latch(.06f,.04f,.2f));
        assertFalse(ActionCycle.slap(.10f,.10f));assertFalse(ActionCycle.slap(.10f,.14f));assertTrue(ActionCycle.slap(.14f,.10f));
    }
    @Test void portLoadRequiresOpenEmptyChamberAndExactlyOneShell(){
        assertTrue(ActionCycle.portLoad(true,false,1));
        assertFalse(ActionCycle.portLoad(false,false,1));assertFalse(ActionCycle.portLoad(true,true,1));
        assertFalse(ActionCycle.portLoad(true,false,0));assertFalse(ActionCycle.portLoad(true,false,2));
        // Side-loaded shell is native chamber ammunition, so closing cannot take a second shell from tube.
        assertEquals(new Chamber(5,true),PumpCycle.close(5,true));
        assertEquals(new Chamber(4,true),PumpCycle.close(5,false));
    }
    @Test void releaseBoxOnlyInterceptsWhenActionIsLocked(){
        var profile=Profiles.byId("tacz:m4a1");var c=Calibration.ZERO;var pos=Handling.release(profile,c);
        assertEquals(Handling.Target.RELEASE,Handling.target(Handling.Phase.NEED_RACK,pos,false,profile,c,false,true));
        assertNotEquals(Handling.Target.RELEASE,Handling.target(Handling.Phase.NEED_RACK,pos,false,profile,c,false,false));
        assertNotEquals(Handling.Target.RELEASE,Handling.target(Handling.Phase.LOADING,pos,false,profile,c,false,true));
    }
    @Test void liftedBoltPickupMatchesGuideAndNoLongerAcceptsOldPointWithSmallZone(){
        var p=Profiles.byId("tacz:m700");var c=new Calibration(0,0,0,0,0,0,0,0,0,InteractionOffsets.ZERO,1f,new ZoneSizes(null,null,new ZoneSizes.Box(.02f,.02f,.02f),null,null,null,null));
        var closed=Handling.rack(p,c,true);var lifted=new Vector3f(closed).add(0,.035f,0);
        assertEquals(Handling.Target.RACK,Handling.target(Handling.Phase.NEED_RACK,lifted,false,p,c,true,false,true));
        assertNotEquals(Handling.Target.RACK,Handling.target(Handling.Phase.NEED_RACK,closed,false,p,c,true,false,true));
    }
    @Test void newReleaseDefaultsDoNotChangeOldOffsets(){
        var c=new com.google.gson.Gson().fromJson("{\"y\":-0.01,\"z\":0.03,\"interactions\":{\"rack\":{\"y\":0.1}}}",Calibration.class);
        assertTrue(c.valid());assertEquals(-.01f,c.y());assertEquals(.03f,c.z());assertEquals(.1f,c.interactions().rack().y());
        assertEquals(InteractionOffsets.Point.ZERO,c.interactions().release());assertEquals(.06f,c.zones().release().width());
    }
}
