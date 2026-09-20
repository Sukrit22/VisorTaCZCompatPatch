package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.TransferPolicy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TransferPolicyTest {
    @Test void readyRifleOnlyTransfersInAnytimeMode(){
        assertFalse(TransferPolicy.mayStart(false,false,false,false,true,false));
        assertTrue(TransferPolicy.mayStart(true,false,false,false,true,false));
    }
    @Test void everyBoltHandlingStateAllowsRestrictedTransfer(){
        assertTrue(TransferPolicy.mayStart(false,true,false,false,true,false));
        assertTrue(TransferPolicy.mayStart(false,false,true,false,true,false));
        assertTrue(TransferPolicy.mayStart(false,false,false,true,true,false));
        assertTrue(TransferPolicy.mayStart(false,false,false,false,false,false));
        assertTrue(TransferPolicy.mayStart(false,false,false,false,true,true));
    }
}
