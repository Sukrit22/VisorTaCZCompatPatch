package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.ButtonJamHold;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ButtonJamHoldTest {
    @Test void requiresFullServerDuration(){var h=new ButtonJamHold(100);for(int t=100;t<140;t+=4)h.refresh(t);assertFalse(h.complete(139));h.refresh(140);assertTrue(h.complete(140));assertEquals(40,h.elapsed(200));}
    @Test void lostReleaseExpiresBeforeCompletion(){var h=new ButtonJamHold(0);assertTrue(h.alive(8));assertFalse(h.alive(9));assertFalse(h.complete(40));}
    @Test void aNewHoldRestartsProgress(){var first=new ButtonJamHold(0);first.refresh(20);var restarted=new ButtonJamHold(20);assertEquals(0,restarted.elapsed(20));assertFalse(restarted.complete(40));}
}
