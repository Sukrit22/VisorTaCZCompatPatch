package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalibrationDefaultsTest {
    @Test void shippedProfilesAreValidAndKeepTheirVisibleOffsets() {
        var profiles=CalibrationDefaults.load();
        assertEquals(54,profiles.size());
        assertEquals(-.06999999f,profiles.get("tacz:m700|tacz:default").z());
        assertEquals(.055000003f,profiles.get("tacz:deagle|tacz:default").y());
        assertTrue(profiles.values().stream().allMatch(Calibration::valid));
        assertEquals(.059999995f,profiles.get("tacz:glock_17|tacz:default").y());
        assertEquals(-0.24000004f,profiles.get("tacz:m4a1|tacz:default").interactions().sight().z());
        assertEquals(13.0f,profiles.get("tacz:m870|tacz:default").pitch());
        assertEquals(-0.26000002f,profiles.get("tacz:m870|tacz:default").interactions().rack().z());
    }
    @Test void currentUserScaleAndHandlingPresets(){
        var p=CalibrationDefaults.load();assertEquals(.6499999f,p.get("tacz:glock_17|tacz:default").gunScale());
        var i=HandlingDefaults.inspections();var h=HandlingDefaults.holsters();
        assertEquals(10,i.size());assertEquals(54,h.size());
        assertEquals(-.109999985f,i.get("tacz:glock_17|tacz:default").y());
        assertEquals(-65f,i.get("tacz:deagle_golden|tacz:default").pitch());
        assertEquals(i.get("tacz:glock_17|tacz:default"),i.get("tacz:m1911|tacz:default"));
        assertEquals(h.get("tacz:glock_17|tacz:default"),h.get("tacz:taurus943|tacz:default"));
        assertEquals(h.get("tacz:m4a1|tacz:default"),h.get("tacz:m700|tacz:default"));
        assertEquals(.9200007f,h.get("tacz:glock_17|tacz:default").y());
    }
}
