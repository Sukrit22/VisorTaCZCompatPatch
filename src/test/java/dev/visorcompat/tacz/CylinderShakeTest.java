package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CylinderShakeTest {
    @Test void tiltOrSlowSingleStrokeDoesNotDumpRounds(){var s=new CylinderShake();for(int i=0;i<20;i++)assertFalse(s.update(.002f*i,.05f,true));}
    @Test void deliberateUpAndBackTriggersOnce(){var s=new CylinderShake();assertFalse(s.update(0,.05f,true));assertFalse(s.update(.05f,.05f,true));assertTrue(s.update(.02f,.05f,true));assertFalse(s.update(.02f,.05f,true));}
    @Test void eitherStrokeDirectionWorks(){var s=new CylinderShake();s.update(0,.05f,true);s.update(-.05f,.05f,true);assertTrue(s.update(-.02f,.05f,true));}
    @Test void lostTrackingOrLoweringGunDisarms(){var s=new CylinderShake();s.update(0,.05f,true);s.update(.05f,.05f,true);assertFalse(s.update(.02f,.2f,true));assertFalse(s.update(0,.05f,true));s.update(.05f,.05f,true);assertFalse(s.update(.02f,.05f,false));assertFalse(s.update(0,.05f,true));assertFalse(s.update(Float.NaN,.05f,true));}
    @Test void loaderFillsOnlyVacantChambersAndSingleRoundStaysSingle(){assertEquals(4,CylinderCycle.loadCount(true,1,1,6,true));assertEquals(1,CylinderCycle.loadCount(true,1,1,6,false));assertEquals(0,CylinderCycle.loadCount(false,0,0,6,true));assertEquals(0,CylinderCycle.loadCount(true,2,4,6,true));}
}
