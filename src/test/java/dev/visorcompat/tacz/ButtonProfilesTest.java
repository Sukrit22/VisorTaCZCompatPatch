package dev.visorcompat.tacz;
import dev.visorcompat.tacz.client.CalibrationHints;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ButtonProfilesTest {
    @Test void registryCovers54BaseGunsWithIndependentCapabilities(){
        assertEquals(54,Profiles.ids().size());assertEquals(36,ButtonProfiles.ids().size());
        assertEquals(18,Profiles.ids().stream().filter(id->Profiles.byId(id).physical()).count());
        for(String id:ButtonProfiles.ids()){
            var p=Profiles.byId(id);assertNotNull(p);assertTrue(p.buttonOnly());assertFalse(p.physical());
            assertFalse(p.pump());assertFalse(p.bolt());assertFalse(p.cylinder());
            assertTrue(PoseMath.finite(p.grip()));assertTrue(PoseMath.finite(p.muzzleOffset()));assertTrue(p.muzzleOffset().length()*1.5f<3f);
        }
        assertTrue(Profiles.byId("tacz:kar98").buttonOnly());assertTrue(Profiles.byId("tacz:aa12").buttonOnly());
        assertTrue(Profiles.byId("tacz:m870").physical());assertNull(Profiles.byId("other:unregistered"));
    }
    @Test void buttonCalibrationOmitsNonexistentPhysicalActions(){
        for(String id:ButtonProfiles.ids()){
            var pages=CalibrationLayout.pages(id+"|tacz:default");
            assertTrue(pages.stream().noneMatch(p->java.util.Set.of("magazine","rack","release","pouch","selector").contains(p.id())));
            for(var page:pages){var hint=CalibrationHints.get(id,page.id());assertNotEquals(page.id(),hint.title());assertTrue(hint.text().length()>20);}
            assertTrue(pages.stream().anyMatch(p->p.id().equals("muzzle")));assertTrue(pages.stream().anyMatch(p->p.id().equals("sight")));
        }
    }
    @Test void buttonOnlyDoesNotOverwritePhysicalGeometry(){
        assertEquals(.65f,Profiles.byId("tacz:glock_17").scale());assertEquals(WeaponProfile.Mechanism.M1911,Profiles.byId("tacz:m1911").mechanism());
        assertFalse(ButtonProfiles.ids().contains("tacz:glock_17"));assertFalse(ButtonProfiles.ids().contains("tacz:m4a1"));
    }
}
