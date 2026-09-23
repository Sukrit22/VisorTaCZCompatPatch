package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.CylinderCycle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CylinderGestureTest {
    @Test void muzzleMustPointUpAndCountsStayBounded(){
        assertTrue(CylinderCycle.fallsOut(.8f));assertFalse(CylinderCycle.fallsOut(.5f));
        assertFalse(CylinderCycle.fallsOut(Float.NaN));
        assertEquals(3,CylinderCycle.spent(5,3,6));assertEquals(0,CylinderCycle.spent(-1,3,6));
    }
}
