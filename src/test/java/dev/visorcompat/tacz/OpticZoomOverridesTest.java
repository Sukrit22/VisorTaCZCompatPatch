package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpticZoomOverridesTest {
    @Test void emptyTablePreservesNativeRules(){assertTrue(OpticZoomOverrides.parse("[magnification]\n").isEmpty());}
    @Test void quotedAttachmentIdsKeepDotsAndPaths(){
        var entries=OpticZoomOverrides.parse("[magnification]\n\"pack.id:optic/2x\"=2.0\n\"tacz:scope_lpvo_1_6\"=1\n");
        assertEquals(2f,entries.get("pack.id:optic/2x"));assertEquals(1f,entries.get("tacz:scope_lpvo_1_6"));
    }
    @Test void rejectsInvalidNumbersAndIds(){
        for(String entry:new String[]{"\"pack:optic\"=0","\"pack:optic\"=17","\"pack:optic\"=nan","\"pack:optic\"=\"2\"","\"Bad:optic\"=2"})
            assertThrows(IllegalArgumentException.class,()->OpticZoomOverrides.parse("[magnification]\n"+entry));
    }
}
