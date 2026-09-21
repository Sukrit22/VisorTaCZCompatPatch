package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalibrationDefaultsTest {
    @Test void shippedProfilesAreValidAndKeepTheirVisibleOffsets() {
        var profiles=CalibrationDefaults.load();
        assertEquals(7,profiles.size());
        assertEquals(-.06999999f,profiles.get("tacz:m700|tacz:default").z());
        assertEquals(.055000003f,profiles.get("tacz:deagle|tacz:default").y());
        assertTrue(profiles.values().stream().allMatch(Calibration::valid));
        assertEquals(.07999999f,profiles.get("tacz:glock_17|tacz:default").y());
        assertEquals(-0.24000004f,profiles.get("tacz:m4a1|tacz:default").interactions().sight().z());
        assertEquals(13.0f,profiles.get("tacz:m870|tacz:default").pitch());
        assertEquals(-0.26000002f,profiles.get("tacz:m870|tacz:default").interactions().rack().z());
    }
}
