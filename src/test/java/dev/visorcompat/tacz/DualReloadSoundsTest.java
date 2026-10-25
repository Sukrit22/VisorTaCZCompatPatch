package dev.visorcompat.tacz;
import dev.visorcompat.tacz.client.DualReloadSounds;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class DualReloadSoundsTest {
 @Test void m1014UsesPairedThenSingleShellLoops(){var p=DualReloadSounds.plan(Map.of("reload_intro_empty",1d,"reload_loop",.5d,"reload_loop_2",.8d,"reload_end",.6d),true,false,5);assertEquals(java.util.List.of("reload_intro_empty","reload_loop_2","reload_loop_2","reload_loop","reload_end"),p.stream().map(DualReloadSounds.Segment::name).toList());assertEquals(3.1,p.get(4).second(),.0001);}
 @Test void spasAlternateModeUsesItsOwnClips(){var p=DualReloadSounds.plan(Map.of("reload_empty_intro_semi",1d,"reload_intro_semi",.6d,"reload_loop_semi",.5d,"reload_end_semi",.7d,"reload_loop",9d),true,true,2);assertEquals("reload_empty_intro_semi",p.get(0).name());assertEquals("reload_loop_semi",p.get(1).name());assertEquals(2,p.get(3).second(),.0001);}
 @Test void magazineReloadsDoNotInventTubeSounds(){assertTrue(DualReloadSounds.plan(Map.of("reload_empty",2d),true,false,30).isEmpty());}
 @Test void slotAudioOwnsBoltAndReloadButKeepsNativeShotAndIdleChannels(){
  assertTrue(DualReloadSounds.ownsAnimation("bolt"));assertTrue(DualReloadSounds.ownsAnimation("reload_empty"));
  assertTrue(DualReloadSounds.ownsAnimation("reload_loop_2"));assertFalse(DualReloadSounds.ownsAnimation("shoot"));
  assertFalse(DualReloadSounds.ownsAnimation("static_bolt_caught"));assertFalse(DualReloadSounds.ownsAnimation(null));
 }
}
