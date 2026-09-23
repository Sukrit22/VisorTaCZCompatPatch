package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.RegripZone;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RegripZoneTest {
    @Test void entryBuzzesOnceAndBoundaryJitterDoesNotRepeat(){
        var zone=new RegripZone();
        assertFalse(zone.update(true,.20f));assertTrue(zone.update(true,.03f));
        assertFalse(zone.update(true,.045f));assertFalse(zone.update(true,.03f));
        assertFalse(zone.update(true,.06f));assertTrue(zone.update(true,.03f));
    }
    @Test void blockedActionAndLostTrackingNeverBuzz(){
        var zone=new RegripZone();assertFalse(zone.update(false,.02f));
        assertFalse(zone.update(true,Float.NaN));assertTrue(zone.update(true,.02f));
        zone.reset();assertTrue(zone.update(true,.02f));
    }
}
