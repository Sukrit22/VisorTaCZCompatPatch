package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PumpTest {
    private final WeaponProfile pump=new WeaponProfile(.6f,0,6.825f,5.425f,0,9,-23,.32f,WeaponProfile.Mechanism.PUMP);
    @Test void partialStrokeDoesNotOpenOrFeed(){
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(false,.04f,0,0));
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(true,.04f,0,0));
    }
    @Test void fullCycleRequiresRearThenForward(){
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(false,0,0,0));
        assertEquals(PumpCycle.Transition.OPEN,PumpCycle.transition(false,.08f,0,0));
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(true,.08f,0,0));
        assertEquals(PumpCycle.Transition.CLOSE,PumpCycle.transition(true,0,0,0));
    }
    @Test void sidewaysOrInvalidTrackingCannotCycle(){
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(false,.10f,.20f,0));
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(true,0,0,.20f));
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(false,Float.NaN,0,0));
        assertEquals(PumpCycle.Transition.NONE,PumpCycle.transition(true,0,Float.NaN,0));
    }
    @Test void heldRearPositionDoesNotRepeatedlyEject(){
        boolean open=false;int ejections=0;
        for(int i=0;i<100;i++)if(PumpCycle.transition(open,.09f,0,0)==PumpCycle.Transition.OPEN){open=true;ejections++;}
        assertEquals(1,ejections);
    }
    @Test void interruptedOpenStrokeCanResumeForward(){
        assertEquals(PumpCycle.Transition.CLOSE,PumpCycle.transition(true,.01f,0,0));
        assertEquals(new Chamber(4,true),PumpCycle.close(5,false));
    }
    @Test void emptyTubeCannotInventShellAndLoadedChamberCannotDoubleFeed(){
        assertEquals(new Chamber(0,false),PumpCycle.close(0,false));
        assertEquals(new Chamber(5,true),PumpCycle.close(5,true));
    }
    @Test void insertionRequiresExactlyOneConsumedShellAndSpace(){
        assertFalse(PumpCycle.canInsert(5,5,1));assertFalse(PumpCycle.canInsert(4,5,0));
        assertTrue(PumpCycle.canInsert(4,5,1));assertFalse(PumpCycle.canInsert(4,5,2));
    }
    @Test void pumpUsesPouchAndForeEndNotDetachableMagazine(){
        assertEquals(Handling.Target.POUCH,Handling.target(Handling.Phase.READY,new Vector3f(1),true,pump,Calibration.ZERO));
        assertEquals(Handling.Target.RACK,Handling.target(Handling.Phase.PUMP_OPEN,Handling.rack(pump).add(0,0,.08f),false,pump,Calibration.ZERO));
        assertEquals(Handling.Target.NONE,Handling.target(Handling.Phase.READY,Handling.magazine(pump),false,pump,Calibration.ZERO));
    }
}
