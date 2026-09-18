package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GunStatusTest {
    @Test void insertedMagazineIsNotLoadedChamber() {
        assertEquals("EMPTY CHAMBER",GunStatus.describe(false,false,false,true,Handling.Phase.NEED_RACK,false,17));
        assertEquals("EMPTY",GunStatus.describe(false,false,false,true,Handling.Phase.NEED_RACK,false,0));
    }
    @Test void removedMagazineDoesNotImplyUnloadedGun() {
        assertEquals("NO MAG | CHAMBER LOADED",GunStatus.describe(false,false,false,true,Handling.Phase.NO_MAG,true,0));
        assertEquals("NO MAGAZINE",GunStatus.describe(false,false,false,true,Handling.Phase.NO_MAG,false,0));
    }
    @Test void jamAndHeatRemainVisibleDuringManipulation() {
        assertEquals("JAMMED",GunStatus.describe(true,true,true,true,Handling.Phase.RACKING,false,17));
        assertEquals("OVERHEATED",GunStatus.describe(false,true,false,true,Handling.Phase.NEED_RACK,false,17));
    }
    @Test void normalButtonChamberingIsNotMislabelled() {
        assertEquals("",GunStatus.describe(false,false,false,false,Handling.Phase.READY,false,17));
    }
    @Test void unfinishedActionsHaveDistinctLabels() {
        assertEquals("RELOADING",GunStatus.describe(false,false,true,true,Handling.Phase.LOADING,false,17));
        assertEquals("ACTION OPEN",GunStatus.describe(false,false,false,true,Handling.Phase.RACKING,false,17));
    }
}
