package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JamTest {
    @Test void reservationsConserveAmmunitionForEveryInitialLoad() {
        for(var kind:Jam.Kind.values())for(int mag=0;mag<=40;mag++)for(boolean chamber:new boolean[]{false,true}) {
            var a=Jam.allocate(kind,mag,chamber);
            assertEquals(mag+(chamber?1:0),a.magazine()+(a.chamber()?1:0)+a.jam().refundable());
            assertTrue(a.magazine()>=0);
        }
    }
    @Test void doubleFeedRequiresMagazineRemoval() {
        var a=Jam.allocate(Jam.Kind.DOUBLE_FEED,16,true);
        assertEquals(15,a.magazine());assertFalse(a.chamber());
        assertEquals(a.jam(),a.jam().pull(false));
        assertEquals(1,a.jam().pull(true).remaining());
        assertEquals(0,a.jam().pull(true).pull(true).remaining());
    }
    @Test void extraPullsNeverCreateAdditionalObstructions() {
        var jam=new Jam(Jam.Kind.DOUBLE_FEED,2);
        for(int i=0;i<20;i++)jam=jam.pull(true);
        assertEquals(0,jam.remaining());assertEquals(0,jam.refundable());
    }
    @Test void insufficientAmmoDowngradesWithoutInventingCartridges() {
        assertEquals(Jam.Kind.DUD,Jam.allocate(Jam.Kind.DOUBLE_FEED,0,true).jam().kind());
        assertEquals(Jam.Kind.STOVEPIPE,Jam.allocate(Jam.Kind.DOUBLE_FEED,0,false).jam().kind());
    }
    @Test void interruptionRefundsOnlyRemainingLiveObstructions() {
        var jam=Jam.allocate(Jam.Kind.DOUBLE_FEED,10,true).jam().pull(true);
        assertEquals(1,jam.refundable());
        assertEquals(0,new Jam(Jam.Kind.STOVEPIPE,1).refundable());
    }
    @Test void dudConsumesOneRealRoundAndStovepipeIsCosmetic() {
        var dud=Jam.allocate(Jam.Kind.DUD,16,true);
        assertEquals(16,dud.magazine());assertFalse(dud.chamber());assertEquals(1,dud.jam().refundable());
        var stove=Jam.allocate(Jam.Kind.STOVEPIPE,16,true);
        assertEquals(16,stove.magazine());assertTrue(stove.chamber());
    }
    @Test void magazineStaysAbsentDuringRackingAndShotsAreBlocked() {
        assertTrue(Handling.magazineOut(Handling.Phase.RACKING_EMPTY));
        assertFalse(Handling.fireable(Handling.Phase.RACKING_EMPTY));
        assertFalse(Handling.fireable(Handling.Phase.PLUCKING));
    }
}
