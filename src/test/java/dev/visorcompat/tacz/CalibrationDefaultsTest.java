package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalibrationDefaultsTest {
    @Test void shippedProfilesAreValidAndKeepTheirVisibleOffsets() {
        var profiles=CalibrationDefaults.load();
        assertEquals(3,profiles.size());
        assertTrue(profiles.values().stream().allMatch(Calibration::valid));
        assertEquals(.07999999f,profiles.get("tacz:glock_17|tacz:default").y());
        assertEquals(-.24000004f,profiles.get("tacz:m4a1|tacz:default").interactions().sight().z());
        assertEquals(13f,profiles.get("tacz:m870|tacz:default").pitch());
        assertEquals(-.28f,profiles.get("tacz:m870|tacz:default").interactions().rack().z());
    }
}
