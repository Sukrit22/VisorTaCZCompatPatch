package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.DualJamHold;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DualJamHoldTest {
 @Test void independentHandsAndFullDuration(){var a=new Object();var b=new Object();var main=new DualJamHold<Object>();var off=new DualJamHold<Object>();for(int t=0;t<=40;t++){main.input(a,true,true,t);off.input(b,true,true,t);if(t<40)assertFalse(main.complete(t));}assertTrue(main.complete(40));main.finish();assertFalse(main.active());assertTrue(off.complete(40));}
 @Test void replacementCannotInheritProgress(){var h=new DualJamHold<Object>();var a=new Object();h.input(a,true,true,0);h.input(new Object(),true,true,39);assertFalse(h.active());h.input(a,true,true,40);assertFalse(h.active());h.input(a,false,true,41);h.input(a,true,true,42);assertFalse(h.complete(42));}
 @Test void releaseAndLostHeartbeatAbort(){var h=new DualJamHold<Object>();var a=new Object();h.input(a,true,true,0);assertFalse(h.valid(a,true,9));h.input(a,false,true,10);assertFalse(h.active());}
 @Test void completedHoldCannotRestartUntilRelease(){var h=new DualJamHold<Object>();var a=new Object();h.input(a,true,true,0);h.finish();h.input(a,true,true,100);assertFalse(h.active());h.input(a,false,true,101);h.input(a,true,true,102);assertTrue(h.active());}
 @Test void invalidPoseOrDifferentStackRejectsSession(){var h=new DualJamHold<Object>();var a=new Object();h.input(a,true,true,0);assertFalse(h.valid(a,false,1));assertFalse(h.valid(new Object(),true,1));}
}
