package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class DescriptorProfilesTest {
    private String bundled()throws Exception{try(var in=getClass().getResourceAsStream("/gun-profiles.toml")){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
    @Test void everyExistingPhysicalProfileMigratesWithoutGeometryChanges()throws Exception{
        var data=DescriptorProfiles.parse(bundled());assertEquals(54,data.size());int migrated=0;
        for(String id:Profiles.ids()){
            var p=Profiles.byId(id);if(!p.physical())continue;migrated++;
            var d=data.get(id);assertNotNull(d,id);assertEquals(p,d.profile(),id);assertEquals(p.rackNode(),d.rack(),id);assertEquals(p.boltNode(),d.bolt(),id);
            assertTrue(Handling.magazine(p).distance(d.zones().get("magazine"))<1e-6,id);
            assertTrue(Handling.rack(p).distance(d.zones().get("rack"))<1e-6,id);
            assertTrue(Handling.support(p,Calibration.ZERO).distance(d.zones().get("support"))<1e-6,id);
            assertTrue(Handling.release(p,Calibration.ZERO).distance(d.zones().get("release"))<1e-6,id);
            assertTrue(JamProfile.of(p,Calibration.ZERO).port().distance(d.zones().get("port"))<1e-6,id);
        }
        assertEquals(20,migrated);
    }
    @Test void newScarsUseOneActionBoneAndNativeMagazineTemplate()throws Exception{
        var d=DescriptorProfiles.parse(bundled());for(String id:new String[]{"tacz:scar_l","tacz:scar_h"}){
            assertTrue(Profiles.byId(id).physical());assertTrue(d.get(id).profile().physical());
            assertEquals("bolt",d.get(id).rack());assertNull(d.get(id).bolt());assertEquals("magazine",d.get(id).magazine());
        }
    }
    @Test void rejectUnsupportedMotionAndMalformedGeometryBeforeApplying()throws Exception{
        var text=bundled();assertThrows(IllegalArgumentException.class,()->DescriptorProfiles.parse(text.replace("kind = \"template\"","kind = \"arbitrary\"")));
        assertThrows(IllegalArgumentException.class,()->DescriptorProfiles.parse(text.replaceFirst("support_distance = 0.0","support_distance = -1.0")));
        assertThrows(IllegalArgumentException.class,()->DescriptorProfiles.parse(text.replace("ammo_adapter = \"native_rounds\"","ammo_adapter = \"free_ammo\"")));
    }
    @Test void cosmeticPartsAndManualCyclingAreConfiguredInToml()throws Exception{
        var d=DescriptorProfiles.parse(bundled());assertEquals(8,d.get("tacz:taurus943").visual().rounds().size());assertEquals("hammer",d.get("tacz:taurus943").visual().hammer());
        assertEquals("speed_loader",d.get("tacz:rhino357").visual().loader());assertEquals("loader",d.get("tacz:taurus500").visual().loader());
        assertEquals(java.util.List.of("SEMI"),d.get("tacz:hk_mk23").manualCycleModes());assertTrue(d.get("tacz:glock_17").manualCycleModes().isEmpty());
        assertEquals("m4a1_bolt",d.get("tacz:m4a1").visual().shotNode());
    }
    @Test void rejectUnsafeVisualTravelAndUnimplementedFireMode()throws Exception{
        var text=bundled();assertThrows(IllegalArgumentException.class,()->DescriptorProfiles.parse(text.replace("shot_travel = 0.035","shot_travel = 1.0")));
        assertThrows(IllegalArgumentException.class,()->DescriptorProfiles.parse(text.replace("manual_cycle_modes = [\"SEMI\"]","manual_cycle_modes = [\"INVALID\"]")));
    }
    @Test void gunIdLookupDoesNotRequireBindingAStack(){
        var first=Profiles.byId("tacz:scar_l");assertSame(first,Profiles.byId("tacz:scar_l"));assertTrue(first.physical());assertNotNull(DescriptorProfiles.details(first));
        assertEquals(.65f,Profiles.byId("tacz:glock_17").scale());assertEquals(4.225f,Profiles.byId("tacz:glock_17").gripY());
        assertEquals(-26.25f,Profiles.byId("tacz:m4a1").muzzleZ());
    }
}
