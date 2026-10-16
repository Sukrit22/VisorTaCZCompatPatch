package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DualTriggerLatchTest {
 @Test void heldTriggerCannotFireOnDrawOrAfterFocusRecovery(){var t=new DualTriggerLatch();t.input(true,true);assertFalse(t.pending());t.input(true,false);t.input(true,true);assertTrue(t.pending());t.reset();t.input(true,true);assertFalse(t.pending());}
 @Test void semiAutoRequiresReleaseEvenWithRepeatedPackets(){var t=new DualTriggerLatch();t.input(true,false);t.input(true,true);t.completed(false);for(int i=0;i<10;i++)t.input(true,true);assertFalse(t.pending());t.input(true,false);t.input(true,true);assertTrue(t.pending());}
 @Test void handsAreIndependentAndHolsteringClearsOne(){var a=new DualTriggerLatch();var b=new DualTriggerLatch();a.input(true,false);b.input(true,false);a.input(true,true);b.input(true,true);a.input(false,true);assertFalse(a.pending());assertTrue(b.pending());b.completed(true);assertTrue(b.pending());b.input(true,false);assertFalse(b.pending());}
}
