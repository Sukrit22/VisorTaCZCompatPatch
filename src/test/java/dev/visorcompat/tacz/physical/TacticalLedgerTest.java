package dev.visorcompat.tacz.physical;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TacticalLedgerTest {
    @Test void alternativeRetainsOldPinchedMagazineAfterNormalReplacementInserted(){
        var ledger=new TacticalLedger(-1,-1,false);ledger.take(false,17);assertTrue(ledger.remove(true,9));
        assertEquals(17,ledger.insert(false));assertEquals(-1,ledger.rounds(false));assertEquals(9,ledger.rounds(true));
        assertFalse(ledger.out());assertEquals(9,ledger.release(true));assertEquals(0,ledger.release(true));
    }
    @Test void tacticalSwapRetainsOldRoundsUntilPouch(){
        var ledger=new TacticalLedger(-1,-1,false);
        assertTrue(ledger.take(true,17));assertTrue(ledger.remove(9));
        assertEquals(17,ledger.insert(true));assertFalse(ledger.out());
        assertEquals(9,ledger.rounds(false));assertEquals(9,ledger.release(false));
        assertEquals(0,ledger.release(false));assertEquals(-1,ledger.insert(true));
    }
    @Test void canReinsertOldRatherThanNewAndRecoverNewOnce(){
        var ledger=new TacticalLedger(-1,-1,false);ledger.take(true,17);ledger.remove(9);
        assertEquals(9,ledger.insert(false));assertEquals(17,ledger.release(true));assertEquals(0,ledger.release(true));
    }
    @Test void pregrabDoesNotRemoveInstalledMagazineAndCannotOverwriteHeldAmmo(){
        var ledger=new TacticalLedger(-1,-1,false);assertTrue(ledger.take(false,17));
        assertFalse(ledger.out());assertFalse(ledger.take(false,30));assertFalse(ledger.remove(9));
        ledger.eject();assertTrue(ledger.out());assertEquals(17,ledger.insert(false));
    }
    @Test void emptyMagazineCanBeRetainedAndInserted(){
        var ledger=new TacticalLedger(-1,-1,false);assertTrue(ledger.remove(0));assertEquals(0,ledger.insert(false));assertFalse(ledger.out());
    }
    @Test void serializedInterruptionRetainsExactlyBothMagazines(){
        var before=new TacticalLedger(-1,-1,false);before.take(true,17);before.remove(9);
        var restored=new TacticalLedger(before.rounds(false),before.rounds(true),before.out());
        assertEquals(26,restored.release(false)+restored.release(true));assertEquals(0,restored.release(false)+restored.release(true));assertTrue(restored.out());
    }
    @Test void invalidCountsRejected(){assertThrows(IllegalArgumentException.class,()->new TacticalLedger(-2,0,false));assertThrows(IllegalArgumentException.class,()->new TacticalLedger(4097,0,false));}
}
