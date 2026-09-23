package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.AdvancedHold;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AdvancedHoldTest {
    @Test void freshPressRequiredAfterCatch(){var h=new AdvancedHold();h.grip(true);assertFalse(h.canFire());h.triggerReleased();assertTrue(h.canFire());h.release(0,false);assertFalse(h.canFire());h.grip(false);h.triggerReleased();assertFalse(h.canFire());}
    @Test void inspectWindowAndUseLatch(){var h=new AdvancedHold();h.grip(true);h.release(0,false);assertTrue(h.inspect(99_000_000,100,true));h.grip(true);assertFalse(h.useArmed());h.useReleased();assertTrue(h.useArmed());}
    @Test void lateInspectionAndLongGunsRejected(){var h=new AdvancedHold();h.release(0,false);assertFalse(h.inspect(101_000_000,100,true));assertFalse(h.inspect(20_000_000,100,false));}
    @Test void quarterHeightAndGrace(){var h=new AdvancedHold();h.release(0,false);assertFalse(h.recover(50_000_000,100,true,0,0,2,0));assertTrue(h.recover(200_000_000,100,true,.5f,0,2,0));assertFalse(h.recover(200_000_000,100,true,.6f,0,2,0));}
    @Test void recoverAutoTimeoutAndDistance(){var h=new AdvancedHold();h.release(0,false);assertTrue(h.recover(200_000_000,100,false,2,0,2,0));assertTrue(h.recover(4_000_000_000L,100,true,2,0,2,0));assertTrue(h.recover(200_000_000,100,true,2,0,2,4));}
    @Test void holsterNeverInspectsOrFires(){var h=new AdvancedHold();h.release(0,true);assertFalse(h.inspect(0,250,true));assertFalse(h.canFire());}
    @Test void useMustBeReleasedAfterInspectionRegrip(){var h=new AdvancedHold();h.release(0,false);assertTrue(h.inspect(0,250,true));h.grip(true);h.triggerReleased();assertTrue(h.canFire());assertFalse(h.useArmed());h.useReleased();assertTrue(h.useArmed());h.useConsumed();assertFalse(h.useArmed());}
    @Test void holdDoesNotRecoverBelowKnee(){var h=new AdvancedHold();h.grip(true);assertFalse(h.recover(5_000_000_000L,50,false,0,0,2,10));h.grip(false);assertFalse(h.recover(5_000_000_000L,50,false,0,0,2,10));}
    @Test void inspectCannotRepeatAfterReleaseWithoutUseRelease(){var h=new AdvancedHold();h.release(0,false);assertTrue(h.inspect(0,250,true));h.release(1,false);assertFalse(h.inspect(1,250,true));h.useReleased();assertTrue(h.inspect(1,250,true));}
}
