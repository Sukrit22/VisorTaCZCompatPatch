package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.DoubleTrigger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DoubleTriggerTest {
    @Test void partialThenFullFiresOnlyRemainingShell() {
        var t=new DoubleTrigger();t.sample(0);t.sample(.33f);assertTrue(t.pending());
        t.fired();assertFalse(t.pending());t.sample(.90f);assertTrue(t.pending());
        t.fired();assertFalse(t.pending());t.sample(1);assertFalse(t.pending());
    }
    @Test void fullPullRequestsTwoAndReleaseRearms() {
        var t=new DoubleTrigger();t.sample(0);t.sample(1);t.fired();assertTrue(t.pending());
        t.fired();assertFalse(t.pending());t.sample(.10f);t.sample(1);assertFalse(t.pending());
        t.sample(.09f);t.sample(.33f);assertTrue(t.pending());
    }
    @Test void invalidationRequiresFreshReleaseAndClearsQueuedShot() {
        var t=new DoubleTrigger();t.sample(0);t.sample(1);t.fired();t.reset();
        t.sample(1);assertFalse(t.pending());t.sample(0);t.sample(1);assertTrue(t.pending());
        t.sample(Float.NaN);assertFalse(t.pending());
    }
}
