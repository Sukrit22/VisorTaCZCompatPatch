package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DualPhysicalMagazineTest {
 @Test void removingMagazinePreservesOneChamberedShot(){var ledger=new DualPhysicalMagazine();var removed=ledger.remove(new Chamber(16,true));assertEquals(new Chamber(0,true),removed);assertEquals(16,ledger.reserved());assertEquals(17,removed.rounds()+ledger.reserved());assertTrue(DualPhysicalMagazine.canFire(false,false,false,removed));assertFalse(DualPhysicalMagazine.canFire(false,false,false,new Chamber(0,false)));}
 @Test void disconnectRestoresExactlyOnceEvenAfterChamberFired(){var ledger=new DualPhysicalMagazine();ledger.remove(new Chamber(16,true));var restored=ledger.restore(new Chamber(0,false));assertEquals(new Chamber(16,false),restored);assertEquals(restored,ledger.restore(restored));assertFalse(ledger.removed());}
 @Test void repeatedRemovalCannotOverwriteReservedAmmo(){var ledger=new DualPhysicalMagazine();var a=ledger.remove(new Chamber(10,true));assertEquals(a,ledger.remove(a));assertEquals(10,ledger.reserved());}
 @Test void twoGunsKeepIndependentReservations(){var a=new DualPhysicalMagazine();var b=new DualPhysicalMagazine();var left=a.remove(new Chamber(7,true));var right=b.remove(new Chamber(3,false));assertEquals(new Chamber(3,false),b.restore(right));assertEquals(7,a.reserved());assertEquals(new Chamber(7,true),a.restore(left));}
 @Test void savedReservationSurvivesSessionRecreation(){var first=new DualPhysicalMagazine();var removed=first.remove(new Chamber(12,true));var recovered=new DualPhysicalMagazine(first.reserved());assertEquals(new Chamber(12,true),recovered.restore(removed));}
 @Test void helperHandRequiresOtherGunStowed(){assertTrue(DualPhysicalMagazine.free(true,false,true));assertFalse(DualPhysicalMagazine.free(true,true,true));assertFalse(DualPhysicalMagazine.free(false,false,true));assertFalse(DualPhysicalMagazine.free(true,false,false));}
 @Test void milestoneAndFiringGatesAreExplicit(){assertTrue(DualPhysicalMagazine.supported("tacz:glock_17"));assertFalse(DualPhysicalMagazine.supported("tacz:cz75"));var chamber=new Chamber(5,true);assertFalse(DualPhysicalMagazine.canFire(true,false,false,chamber));assertFalse(DualPhysicalMagazine.canFire(false,true,false,chamber));assertFalse(DualPhysicalMagazine.canFire(false,false,true,chamber));}
}
