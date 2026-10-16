package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import static dev.visorcompat.tacz.HolsterAssignments.Kind.*;
import static org.junit.jupiter.api.Assertions.*;

class HolsterAssignmentsTest {
    private final HolsterAssignments.Kind[] hotbar={NONE,LONG_GUN,HANDGUN,NONE,HANDGUN,LONG_GUN,HANDGUN,LONG_GUN,NONE};
    @Test void ascendingFillsRightLeftChestBack(){
        assertArrayEquals(new int[]{2,4,1,5},HolsterAssignments.resolve(hotbar,false,null));
    }
    @Test void descendingFillsRightLeftChestBack(){
        assertArrayEquals(new int[]{6,4,7,5},HolsterAssignments.resolve(hotbar,true,null));
    }
    @Test void manualKeepsExplicitMountsRegardlessOfOrder(){
        assertArrayEquals(new int[]{4,2,5,1},HolsterAssignments.resolve(hotbar,true,new int[]{4,2,5,1}));
    }
    @Test void manualWrongClassEmptyAndOutOfBoundsStayEmpty(){
        assertArrayEquals(new int[]{-1,-1,-1,-1},HolsterAssignments.resolve(hotbar,false,new int[]{1,0,2,9}));
    }
    @Test void duplicateValidBindingFirstMountWins(){
        assertArrayEquals(new int[]{2,-1,1,-1},HolsterAssignments.resolve(hotbar,false,new int[]{2,2,1,1}));
    }
    @Test void rejectedWrongClassDoesNotConsumeSlot(){
        assertArrayEquals(new int[]{-1,2,1,-1},HolsterAssignments.resolve(hotbar,false,new int[]{1,2,1,-1}));
    }
    @Test void negativeManualBindingsDisableMountsWithoutAutoFallback(){
        assertArrayEquals(new int[]{-1,4,-1,-1},HolsterAssignments.resolve(hotbar,false,new int[]{-1,4,-2,-99}));
    }
    @Test void emptyAndMissingEntriesRemainEmpty(){
        assertArrayEquals(new int[]{-1,-1,-1,-1},HolsterAssignments.resolve(new HolsterAssignments.Kind[]{NONE,null,NONE},false,null));
        assertArrayEquals(new int[]{-1,-1,-1,-1},HolsterAssignments.resolve(new HolsterAssignments.Kind[0],true,null));
        assertArrayEquals(new int[]{-1,-1,-1,-1},HolsterAssignments.resolve(null,false,null));
        assertArrayEquals(new int[]{2,-1,-1,-1},HolsterAssignments.resolve(hotbar,false,new int[]{2}));
    }
    @Test void scarceClassesLeaveOtherMountsEmpty(){
        assertArrayEquals(new int[]{1,-1,0,-1},HolsterAssignments.resolve(new HolsterAssignments.Kind[]{LONG_GUN,HANDGUN},false,null));
    }
    @Test void resolverDoesNotMutateInputs(){
        var before=hotbar.clone();int[] manual={2,4,1,5};var manualBefore=manual.clone();
        HolsterAssignments.resolve(hotbar,false,manual);
        assertArrayEquals(before,hotbar);assertArrayEquals(manualBefore,manual);
    }
    @Test void pinnedRightLeavesEarlierHandgunForLeft(){
        assertArrayEquals(new int[]{4,2,1,5},HolsterAssignments.resolvePinned(hotbar,false,null,new int[]{4,-1,-1,-1}));
    }
    @Test void twoPinsReserveSlotsBeforeDescendingFill(){
        assertArrayEquals(new int[]{2,6,1,7},HolsterAssignments.resolvePinned(hotbar,true,null,new int[]{2,-1,1,-1}));
    }
    @Test void manualChangesCannotMoveHeldMountAndCannotReuseItsSlot(){
        assertArrayEquals(new int[]{4,-1,5,1},HolsterAssignments.resolvePinned(hotbar,false,new int[]{2,4,5,1},new int[]{4,-1,-1,-1}));
    }
    @Test void duplicatePinsFirstValidMountWinsAndAutoFillsVacancy(){
        assertArrayEquals(new int[]{4,2,5,1},HolsterAssignments.resolvePinned(hotbar,false,null,new int[]{4,4,5,5}));
    }
    @Test void invalidPinsDoNotReserveSlots(){
        assertArrayEquals(new int[]{2,4,1,5},HolsterAssignments.resolvePinned(hotbar,false,null,new int[]{1,99,2,-2}));
    }
    @Test void manualDisabledMountRetainsValidHeldPin(){
        assertArrayEquals(new int[]{4,-1,-1,-1},HolsterAssignments.resolvePinned(hotbar,false,new int[]{-1,-1,-1,-1},new int[]{4}));
    }
}
