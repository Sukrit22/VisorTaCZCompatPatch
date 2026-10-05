package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RecoilBindingTest {
    @Test void nativeInventorySyncPreservesAuthoritativeStream(){var b=new RecoilBinding(new String("tacz:glock_17"),0,10);assertTrue(b.matches(new String("tacz:glock_17"),0,10));}
    @Test void sameGunTypeDoesNotCarryKickAcrossSlots(){var b=new RecoilBinding("tacz:glock_17",0,10);assertFalse(b.matches("tacz:glock_17",1,10));assertFalse(b.matches("tacz:m4a1",0,10));}
    @Test void replacingGunOrReenteringSlotStartsNewServerStream(){var b=new RecoilBinding("tacz:glock_17",0,10);assertFalse(b.matches("tacz:glock_17",0,11));}
}
