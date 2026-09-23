package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.CatchWindow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CatchWindowTest {
    @Test void approachingGunWinsBeforeDeadline(){var w=new CatchWindow();w.start(0);assertEquals(CatchWindow.Result.WAIT,w.poll(100_000_000,250,true,false));assertEquals(CatchWindow.Result.CATCH,w.poll(200_000_000,250,true,true));assertFalse(w.pending());}
    @Test void expiryCannotStealLaterHotbarPress(){var w=new CatchWindow();w.start(0);assertEquals(CatchWindow.Result.HOTBAR,w.poll(250_000_000,250,true,true));assertEquals(CatchWindow.Result.CANCEL,w.poll(260_000_000,250,true,true));}
    @Test void invalidContextCancelsWithoutCatch(){var w=new CatchWindow();w.start(0);assertEquals(CatchWindow.Result.CANCEL,w.poll(1,250,false,true));}
    @Test void releaseCancellationDoesNotReopenRadial(){var w=new CatchWindow();w.start(0);w.cancel();assertEquals(CatchWindow.Result.CANCEL,w.poll(300_000_000,250,true,false));}
}
