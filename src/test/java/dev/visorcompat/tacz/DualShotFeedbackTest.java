package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.DualShotFeedback;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DualShotFeedbackTest {
 @Test void confirmedShotsAnimateOnlyTheirOwnSlot(){var fx=new DualShotFeedback();fx.fired(2,"glock",1_000_000_000L,false);assertEquals(1,fx.cycle(2,"glock",1_030_000_000L));assertEquals(0,fx.cycle(3,"glock",1_030_000_000L));assertTrue(fx.flash(2,"glock",1_030_000_000L));assertFalse(fx.flash(3,"glock",1_030_000_000L));}
 @Test void laterOtherHandShotDoesNotRestartFirstHand(){var fx=new DualShotFeedback();fx.fired(2,"glock",1_000_000_000L,false);fx.fired(3,"glock",1_080_000_000L,false);assertFalse(fx.flash(2,"glock",1_100_000_000L));assertTrue(fx.flash(3,"glock",1_100_000_000L));assertTrue(fx.cycle(2,"glock",1_100_000_000L)<fx.cycle(3,"glock",1_100_000_000L));}
 @Test void suppressorHidesFlashWhileKeepingMovingParts(){var fx=new DualShotFeedback();fx.fired(0,"silenced",0,true);assertFalse(fx.flash(0,"silenced",30_000_000L));assertEquals(1,fx.cycle(0,"silenced",30_000_000L));}
 @Test void replacementResetAndOldClocksCannotLeak(){var fx=new DualShotFeedback();fx.fired(0,"a",0,false);assertFalse(fx.flash(0,"b",30_000_000L));assertEquals(0,fx.cycle(0,"b",30_000_000L));assertEquals(0,fx.cycle(0,"a",150_000_000L));fx.clear(0);assertFalse(fx.flash(0,"a",30_000_000L));}
 @Test void bothEyesSampleTheSameShotWithoutConsumingIt(){var fx=new DualShotFeedback();fx.fired(0,"glock",0,false);float left=fx.cycle(0,"glock",30_000_000L);assertEquals(left,fx.cycle(0,"glock",30_000_000L));assertTrue(fx.flash(0,"glock",30_000_000L));assertTrue(fx.flash(0,"glock",30_000_000L));}
 @Test void metadataFreeGlockStillResolvesNativeManipulationSounds(){assertEquals("glock_17/glock17_inspect_rackon",DualShotFeedback.glockSound(2));assertEquals(DualShotFeedback.glockSound(2),DualShotFeedback.glockSound(3));assertEquals("glock_17/glock17_inspect_rackhit",DualShotFeedback.glockSound(5));assertEquals("glock_17/glock17_reload_magin",DualShotFeedback.glockSound(1));assertEquals("",DualShotFeedback.glockSound(-1));}
}
