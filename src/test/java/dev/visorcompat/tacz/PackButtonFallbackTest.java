package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PackButtonFallbackTest {
    @Test void validUnknownPackGunGetsOnlyButtonCapabilities() {
        var p=PackButtonFallback.resolve(null,true,true);
        assertNotNull(p);assertTrue(p.buttonOnly());assertFalse(p.physical());
        assertFalse(p.manualAction());assertFalse(p.cylinder());assertFalse(p.bolt());
        assertTrue(PoseMath.finite(p.grip()));assertTrue(PoseMath.finite(p.muzzleOffset()));
        assertTrue(PackButtonFallback.generic(p));
        assertEquals(-.20f,DescriptorProfiles.zone(p,"support").z());
        assertNull(DescriptorProfiles.zone(p,"rack"));
        assertNull(DescriptorProfiles.zone(p,"magazine"));
        assertSame(p,PackButtonFallback.resolve(null,false,true));
        assertTrue(CalibrationLayout.pages("pack:gun|pack:display",p).stream()
            .noneMatch(page->java.util.Set.of("magazine","rack","release","pouch","selector").contains(page.id())));
    }

    @Test void invalidIdsDoNotGainButtonSupport() {
        assertNull(PackButtonFallback.resolve(null,true,false));
        assertNull(PackButtonFallback.resolve(Profiles.byId("tacz:m4a1"),true,false));
    }

    @Test void customDisplayDoesNotInheritPhysicalBones() {
        var bundled=Profiles.byId("tacz:m4a1");
        assertTrue(bundled.physical());
        assertSame(bundled,PackButtonFallback.resolve(bundled,true,true));
        var custom=PackButtonFallback.resolve(bundled,false,true);
        assertTrue(custom.buttonOnly());assertTrue(PackButtonFallback.generic(custom));
        assertNull(DescriptorProfiles.details(custom));
    }

    @Test void commonPistolTypeKeepsCompactHoldingClassification() {
        var pistol=PackButtonFallback.resolve(null,true,true,"pistol");
        assertEquals(0,pistol.supportDistance());assertTrue(pistol.buttonOnly());
        assertTrue(PackButtonFallback.generic(pistol));
        assertSame(pistol,PackButtonFallback.resolve(null,false,true,"PISTOL"));
        assertEquals(.20f,PackButtonFallback.resolve(null,true,true,"rifle").supportDistance());
        assertEquals(.20f,PackButtonFallback.resolve(null,true,true,null).supportDistance());
    }
}
