package dev.visorcompat.tacz;
import dev.visorcompat.tacz.server.DualCharge;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DualChargeTest {
 private DualCharge.Settings auto(float max){return new DualCharge.Settings(DualCharge.Mode.AUTO,.1f,.1f,max,max,max);}
 @Test void nativeRevolverDelaysAreRespected(){for(float max:new float[]{.3f,.5f}){var charge=new DualCharge();var cfg=auto(max);int ticks=Math.round(max/.1f);for(int t=1;t<ticks;t++)assertFalse(charge.tick(cfg,true,true,true,true));assertTrue(charge.tick(cfg,true,true,true,true));charge.fired(cfg);assertEquals(0,charge.progress(),.0001);assertFalse(charge.tick(cfg,false,true,true,true));}}
 @Test void handsChargeIndependently(){var a=new DualCharge();var b=new DualCharge();var cfg=auto(.3f);a.tick(cfg,true,true,true,true);a.tick(cfg,true,true,true,true);assertFalse(b.tick(cfg,true,true,true,true));assertTrue(a.tick(cfg,true,true,true,true));}
 @Test void autoReleaseAndTrackingResetCannotFire(){var h=new DualCharge();var cfg=auto(.3f);h.tick(cfg,true,true,true,true);assertFalse(h.tick(cfg,false,false,true,true));assertEquals(0,h.progress(),.0001);h.tick(cfg,true,true,true,true);h.reset();assertEquals(0,h.progress());}
 @Test void cooldownCannotAccumulateWhenDisallowed(){var h=new DualCharge();assertFalse(h.tick(auto(.3f),true,true,false,false));assertEquals(0,h.progress());}
 @Test void holdFiresOnReleaseAndDelayContinuesAfterRelease(){var cfg=new DualCharge.Settings(DualCharge.Mode.HOLD,.2f,.1f,1,1,.4f);var h=new DualCharge();assertFalse(h.tick(cfg,true,true,true,true));assertFalse(h.tick(cfg,true,true,true,true));assertTrue(h.tick(cfg,false,false,true,true));h.fired(cfg);assertFalse(h.tick(cfg,false,false,true,true));var d=new DualCharge();cfg=new DualCharge.Settings(DualCharge.Mode.DELAY,.2f,.1f,0,.4f,.4f);assertFalse(d.tick(cfg,true,true,true,true));assertTrue(d.tick(cfg,false,false,true,true));d.fired(cfg);assertEquals(0,d.progress());}
}
