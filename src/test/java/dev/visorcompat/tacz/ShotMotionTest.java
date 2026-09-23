package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.ShotMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ShotMotionTest {
    @Test void slideCompletesWithinOneShotWithoutMovingReceiver(){assertEquals(0,ShotMotion.cycle(0));assertEquals(1,ShotMotion.cycle(.025f),1e-6);assertTrue(ShotMotion.cycle(.08f)>0);assertEquals(0,ShotMotion.cycle(.12f));assertEquals(0,ShotMotion.cycle(1));}
    @Test void invalidClockSamplesCannotProduceTransforms(){assertEquals(0,ShotMotion.cycle(-1));assertEquals(0,ShotMotion.cycle(Float.NaN));assertEquals(0,ShotMotion.cycle(Float.POSITIVE_INFINITY));}
}
