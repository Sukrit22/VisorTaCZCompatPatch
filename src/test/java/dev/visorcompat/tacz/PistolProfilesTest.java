package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PistolProfilesTest {
    @Test void conventionalPistolsUseTouchSupportAndIndependentGeometry(){
        for(String name:new String[]{"m1911","p320","m9a4","deagle","deagle_golden","timeless50","b93r","cz75","hk_mk23"}){
            var p=Profiles.byId("tacz:"+name);assertNotNull(p);assertEquals(0,p.supportDistance());assertFalse(p.manualAction());assertFalse(p.smg());
            var d=PistolProfiles.get(p);assertEquals(name,d.id());assertNotNull(p.rackNode());assertNull(p.boltNode());
            assertTrue(PoseMath.finite(JamProfile.of(p,Calibration.ZERO).port()));
            assertTrue(CalibrationLayout.pages("tacz:"+name).stream().anyMatch(x->x.id().equals("release")));
        }
        assertEquals("upper",Profiles.byId("tacz:m9a4").rackNode());assertEquals("upper",Profiles.byId("tacz:b93r").rackNode());
        assertEquals("slide2",Profiles.byId("tacz:deagle").rackNode());
        assertEquals("slide",Profiles.byId("tacz:glock_17").rackNode());
        assertEquals(.65f,Profiles.byId("tacz:glock_17").scale());
    }
    @Test void selectorsDoNotTurnPistolsIntoRifles(){
        for(String name:new String[]{"b93r","hk_mk23"}){var p=Profiles.byId("tacz:"+name);assertTrue(p.selector());assertEquals(0,p.supportDistance());assertEquals(Handling.Target.SELECTOR,Handling.target(Handling.Phase.READY,Handling.selector(p),false,p,Calibration.ZERO));}
        assertFalse(Profiles.byId("tacz:cz75").selector());assertFalse(Profiles.byId("tacz:m1911").selector());
    }
    @Test void cylindersNeverInheritMagazineRemovalOrSlideRelease(){
        for(String name:new String[]{"rhino357","taurus500","taurus943","lonetrail"}){
            var p=Profiles.byId("tacz:"+name);assertTrue(p.cylinder());assertTrue(p.manualAction());assertFalse(p.selector());
            assertTrue(CalibrationLayout.pages("tacz:"+name).stream().noneMatch(x->x.id().equals("release")));
            assertNotEquals(Handling.Target.POUCH,Handling.target(Handling.Phase.READY,Handling.magazine(p),true,p,Calibration.ZERO));
            assertNotEquals(Handling.Target.MAGAZINE,Handling.target(Handling.Phase.READY,Handling.magazine(p),true,p,Calibration.ZERO));
            assertEquals(Handling.Target.POUCH,Handling.target(Handling.Phase.CYLINDER_OPEN,Handling.magazine(p),true,p,Calibration.ZERO));
            assertEquals(Handling.Target.RACK,Handling.target(Handling.Phase.READY,Handling.rack(p),false,p,Calibration.ZERO));
            assertEquals(Handling.Target.CASING,Handling.target(Handling.Phase.CYLINDER_OPEN,JamProfile.of(p,Calibration.ZERO).port(),false,p,Calibration.ZERO));
            assertFalse(Handling.fireable(Handling.Phase.CYLINDER_OPEN));assertFalse(Handling.fireable(Handling.Phase.CYLINDER_HOLD));
        }
    }
    @Test void sharedMetadataIsNotChangedByCalibrationMath(){
        var p=Profiles.byId("tacz:m1911");var saved=new org.joml.Vector3f(PistolProfiles.get(p).shell());
        JamProfile.of(p,Calibration.ZERO);Handling.magazine(p).add(1,1,1);Handling.rack(p).add(1,1,1);
        assertEquals(saved,PistolProfiles.get(p).shell());assertEquals(0f,PistolProfiles.get(p).magazine().x);
    }
    @Test void cylinderCannotLoadClosedFullOrOverSpent(){
        assertFalse(CylinderCycle.canInsert(false,0,0,6));assertFalse(CylinderCycle.canInsert(true,2,4,6));
        assertFalse(CylinderCycle.canInsert(true,6,0,6));assertTrue(CylinderCycle.canInsert(true,2,3,6));
        assertEquals(4,CylinderCycle.spent(99,2,6));assertEquals(0,CylinderCycle.spent(-2,2,6));
        assertFalse(CylinderCycle.canInsert(true,1,0,1));assertTrue(CylinderCycle.canInsert(true,0,0,1));
    }
    @Test void cylinderRoundLedgerThroughFireEjectAndReload(){
        int live=6,spent=0,inventory=10,discarded=0;
        live-=2;spent+=2;
        assertFalse(CylinderCycle.canInsert(true,live,spent,6));
        discarded+=live;live=0;spent=0;
        for(int i=0;i<8;i++)if(CylinderCycle.canInsert(true,live,spent,6)&&inventory>0){inventory--;live++;}
        assertEquals(6,live);assertEquals(4,inventory);assertEquals(16,live+inventory+discarded+2);
        // Saved live count remains native; discarding/recreating gesture state cannot add rounds.
        assertEquals(0,CylinderCycle.spent(spent,live,6));
    }
    @Test void openGestureRejectsPartialInvalidAndDistantMotion(){
        assertFalse(CylinderCycle.toggle(.02f,0,0));assertTrue(CylinderCycle.toggle(.06f,0,0));assertTrue(CylinderCycle.toggle(-.06f,0,0));
        assertFalse(CylinderCycle.toggle(.06f,.2f,0));assertFalse(CylinderCycle.toggle(Float.NaN,0,0));assertFalse(CylinderCycle.toggle(.6f,0,0));
    }
}
