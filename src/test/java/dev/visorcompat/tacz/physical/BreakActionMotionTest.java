package dev.visorcompat.tacz.physical;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BreakActionMotionTest {
    @Test void requiresFreshArmedDirectionalFlick(){
        var motion=new BreakActionMotion();assertEquals(0,motion.update(-.8f,.05f,false));
        motion.arm(0);assertEquals(0,motion.update(.5f,.05f,false));assertEquals(1,motion.update(-.4f,.05f,false));
        assertEquals(0,motion.update(-.8f,.05f,false));
        motion.arm(-.5f);assertEquals(-1,motion.update(0,.05f,true));
    }
    @Test void expiredOrStalePosesCannotOpen(){
        var motion=new BreakActionMotion();motion.arm(0);assertEquals(0,motion.update(-1,.2f,false));
        motion.arm(0);for(int i=0;i<25;i++)assertEquals(0,motion.update(0,.05f,false));assertEquals(0,motion.update(-.7f,.05f,false));
    }
    @Test void angularWrapCannotProduceFalseFullRotation(){
        assertFalse(BreakActionMotion.rotated(3.12f,-3.12f,false));
        assertTrue(BreakActionMotion.rotated(0,-.5f,false));assertFalse(BreakActionMotion.rotated(0,.5f,false));
        assertTrue(BreakActionMotion.rotated(0,.5f,true));
    }
}
