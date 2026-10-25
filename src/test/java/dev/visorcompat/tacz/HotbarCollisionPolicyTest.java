package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HotbarCollisionPolicyTest {
    @Test void onlyMainReservesWhenDualIsDisabled(){
        assertTrue(HotbarCollisionPolicy.protects(true,false,true,false,true,false));
        assertFalse(HotbarCollisionPolicy.protects(true,false,true,false,false,false));
        assertTrue(HotbarCollisionPolicy.protects(true,false,true,true,false,false));
    }
    @Test void holsterDrawCanTakeSelectedButNeverHeldGun(){
        for(boolean main:new boolean[]{true,false}){
            assertFalse(HotbarCollisionPolicy.protects(true,false,true,true,main,true));
            assertTrue(HotbarCollisionPolicy.protects(true,true,true,true,main,true));
        }
    }
    @Test void selectedLockCanBeDisabledButHeldGunCannotBeStolen(){
        assertTrue(HotbarCollisionPolicy.protects(true,false,true));
        assertFalse(HotbarCollisionPolicy.protects(true,false,false));
        assertTrue(HotbarCollisionPolicy.protects(true,true,false));
    }
    @Test void ordinaryItemsKeepNativeCollisionBehavior(){
        assertFalse(HotbarCollisionPolicy.protects(false,true,true));
        assertFalse(HotbarCollisionPolicy.protects(false,false,true));
    }
    @Test void mainSelectingOffhandGunRestoresMainAndKeepsOwnerDistinct(){
        assertEquals(2,HotbarCollisionPolicy.previousSelection(true,2,7));
    }
    @Test void offhandSelectingMainGunRestoresOffhandAndKeepsOwnerDistinct(){
        assertEquals(7,HotbarCollisionPolicy.previousSelection(false,7,2));
        assertEquals(-1,HotbarCollisionPolicy.previousSelection(false,-1,2));
    }
    @Test void recoveryNeverSelectsOwnerOrInvalidMainSlot(){
        for(int owner=0;owner<9;owner++)for(int previous=-2;previous<10;previous++){
            int main=HotbarCollisionPolicy.previousSelection(true,previous,owner);
            int off=HotbarCollisionPolicy.previousSelection(false,previous,owner);
            assertTrue(main>=0&&main<9);assertNotEquals(owner,main);
            assertTrue(off==-1||(off>=0&&off<9));assertNotEquals(owner,off);
        }
    }
}
