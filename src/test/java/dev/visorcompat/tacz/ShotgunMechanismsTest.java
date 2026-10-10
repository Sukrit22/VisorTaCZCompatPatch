package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class ShotgunMechanismsTest {
    @Test void spasNativeBurstIsSelfLoadingWhileSemiRequiresPump(){
        assertTrue(TubeCycle.manual(WeaponProfile.Mechanism.SPAS12,"SEMI"));
        assertFalse(TubeCycle.manual(WeaponProfile.Mechanism.SPAS12,"BURST"));
        assertFalse(TubeCycle.manual(WeaponProfile.Mechanism.TUBE_AUTO,"SEMI"));
        assertTrue(TubeCycle.manual(WeaponProfile.Mechanism.PUMP,"SEMI"));
    }
    @Test void openBoltNeverInventsAChamberOrConsumesAMagazineRoundOnRack(){
        var nativeMagazine=new Chamber(8,false);
        assertEquals(nativeMagazine,NativeAction.close(nativeMagazine,true,true,false));
        assertTrue(NativeAction.ready(true,8,false));assertFalse(NativeAction.ready(true,0,true));
        assertEquals(new Chamber(7,true),NativeAction.close(nativeMagazine,false,true,false));
        assertEquals(nativeMagazine,NativeAction.close(nativeMagazine,false,false,false));
        assertEquals(nativeMagazine,NativeAction.close(nativeMagazine,false,true,true));
    }
    @Test void shotgunProfilesUseActualModelActionsAndKeepTubeModesDistinct()throws Exception{
        String text;try(var in=getClass().getResourceAsStream("/gun-profiles.toml")){text=new String(in.readAllBytes(),StandardCharsets.UTF_8);}
        var guns=DescriptorProfiles.parse(text);
        var spas=guns.get("tacz:spas_12");assertTrue(spas.profile().pump());assertTrue(spas.profile().selector());assertEquals("pump",spas.rack());assertEquals("bolt2",spas.bolt());
        var benelli=guns.get("tacz:m1014");assertEquals(WeaponProfile.Mechanism.TUBE_AUTO,benelli.profile().mechanism());assertEquals("Bolt",benelli.rack());assertFalse(benelli.profile().selector());
        var aa=guns.get("tacz:aa12");assertEquals(WeaponProfile.Mechanism.MAGAZINE,aa.profile().mechanism());assertEquals("bolt",aa.rack());assertEquals("magazine",aa.magazine());
    }
}
