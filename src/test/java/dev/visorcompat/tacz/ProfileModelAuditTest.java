package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProfileModelAuditTest {
    private String bundled() throws Exception {
        try(var in=getClass().getResourceAsStream("/gun-profiles.toml")) {
            return new String(in.readAllBytes(),StandardCharsets.UTF_8);
        }
    }
    @Test void aa12UsesTopHandleAndIndependentReverseIndicator() throws Exception {
        var d=DescriptorProfiles.parse(bundled()).get("tacz:aa12");
        assertEquals("charge_handle",d.rack()); assertEquals("bolt",d.bolt());
        assertEquals(-.5707143f,d.boltRatio(),1e-6);
        assertEquals(-.1284375f,d.visual().shotTravel(),1e-6);
        assertEquals(List.of(new DescriptorProfiles.ShotPart("charge_handle",.01875f)),d.visual().shotFollowers());
        assertTrue(d.zones().get("rack").y>.19f);
        assertTrue(d.zones().get("rack").z<-.29f);
    }
    @Test void tubeShotAnimationOnlyCyclesSelfLoadingSpasMode() throws Exception {
        var data=DescriptorProfiles.parse(bundled());
        assertEquals(List.of("BURST"),data.get("tacz:spas_12").visual().shotModes());
        assertEquals(.1875f,data.get("tacz:m1014").visual().shotTravel());
        assertEquals("bullet_and_bolt",data.get("tacz:m700").rack());
    }
    @Test void newMechanismsAreExplicitAndDoNotPretendToSupportLeverOrBeltReloads() {
        for(String id:List.of("ak12","ak74m","hcar","hk_mp7a1","mcx_spear_lt","mk18","qr_24","scar_mk20","svd","urgi","tec_9"))
            assertEquals(WeaponProfile.Mechanism.MAGAZINE,Profiles.byId("tacz:"+id).mechanism(),id);
        assertEquals(WeaponProfile.Mechanism.PUMP,Profiles.byId("tacz:m1897").mechanism());
        for(String id:List.of("hm50b","m1866","mrad","pkm"))assertTrue(Profiles.byId("tacz:"+id).buttonOnly(),id);
    }
    @Test void absentNewFieldsKeepLegacyTemplatesAndMalformedFollowersFail() throws Exception {
        var text=bundled();var data=DescriptorProfiles.parse(text);
        assertEquals(1f,data.get("tacz:glock_17").boltRatio());
        assertTrue(data.get("tacz:glock_17").visual().shotFollowers().isEmpty());
        assertThrows(IllegalArgumentException.class,()->DescriptorProfiles.parse(text.replace("bolt_ratio = -0.5707143","bolt_ratio = 8")));
        assertThrows(IllegalArgumentException.class,()->DescriptorProfiles.parse(text.replace("{node = \"charge_handle\", travel = 0.01875}","{node = \"bolt\", travel = 0.01875}")));
        assertThrows(IllegalArgumentException.class,()->DescriptorProfiles.parse(text.replace("shot_modes = [\"BURST\"]","shot_modes = [\"INVALID\"]")));
    }
    @Test void soundMappingSupportsSharedNativeFoldersAndDeliberatelySilentCues() {
        assertEquals("tacz:ak47/ak47_reload_magin",DescriptorProfiles.sound(Profiles.byId("tacz:ak74m"),1));
        assertEquals("",DescriptorProfiles.sound(Profiles.byId("tacz:hk_mp7a1"),2));
        assertEquals("tacz:m1897/m1897_bolt_in",DescriptorProfiles.sound(Profiles.byId("tacz:m1897"),4));
        assertNull(DescriptorProfiles.sound(Profiles.byId("tacz:glock_17"),1));
    }
    @Test void shotgunActionHintsDescribeTheActualMovingPart() {
        assertTrue(dev.visorcompat.tacz.client.CalibrationHints.get("tacz:aa12","rack").text().contains("above the receiver"));
        assertTrue(dev.visorcompat.tacz.client.CalibrationHints.get("tacz:m1014","rack").text().contains("fore-end stays fixed"));
        assertTrue(dev.visorcompat.tacz.client.CalibrationHints.get("tacz:spas_12","rack").text().contains("independent grab zone"));
    }
}
