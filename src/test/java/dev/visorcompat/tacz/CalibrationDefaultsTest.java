package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Personal fits may change independently; the distribution must remain valid and complete. */
class CalibrationDefaultsTest {
    @Test void shippedProfilesAreValidAndCoverEveryBundledGun() {
        var profiles=CalibrationDefaults.load();
        for(String id:Profiles.ids())assertNotNull(profiles.get(id+"|tacz:default"),id);
        profiles.forEach((key,value)->{assertFalse(key.isBlank());assertNotNull(value,key);assertTrue(value.valid(),key);});
    }
    @Test void handlingPresetsAreValidAndCoverSupportedGrips(){
        var inspections=HandlingDefaults.inspections();var holsters=HandlingDefaults.holsters();
        for(String id:Profiles.ids()){
            String key=id+"|tacz:default";assertNotNull(holsters.get(key),key);
            var profile=Profiles.byId(id);
            if(profile.physical()&&profile.supportDistance()==0&&!profile.manualAction())assertNotNull(inspections.get(key),key);
        }
        inspections.forEach((key,value)->{assertFalse(key.isBlank());assertNotNull(value,key);assertTrue(value.valid(),key);});
        holsters.forEach((key,value)->{assertFalse(key.isBlank());assertNotNull(value,key);assertTrue(value.valid(),key);});
    }
}
