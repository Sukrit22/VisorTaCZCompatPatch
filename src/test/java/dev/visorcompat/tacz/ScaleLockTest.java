package dev.visorcompat.tacz;
import com.google.gson.Gson;
import dev.visorcompat.tacz.physical.Handling;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScaleLockTest {
    @Test void oldProfileDefaultsLockedWithoutChangingItsExistingBoxes(){
        var c=new Gson().fromJson("{\"gunScale\":0.8,\"zones\":{\"rack\":{\"width\":0.2,\"height\":0.3,\"depth\":0.4}}}",Calibration.class);
        assertTrue(c.scaleLock());assertEquals(.8f,c.gunScale());assertEquals(.2f,c.zones().rack().width());
    }
    @Test void explicitUnlockPersistsInJson(){
        var gson=new Gson();var c=new Calibration(0,0,0,0,0,0,0,0,0,InteractionOffsets.ZERO,.8f,ZoneSizes.DEFAULT,false);
        assertEquals(c,gson.fromJson(gson.toJson(c),Calibration.class));assertFalse(c.scaleLock());
    }
    @Test void linkedResizeKeepsPouchAndResizesMountedAxes(){
        var zones=ZoneSizes.DEFAULT.resizeMounted(.8f);
        assertEquals(ZoneSizes.DEFAULT.pouch(),zones.pouch());
        assertEquals(ZoneSizes.DEFAULT.rack().width()*.8f,zones.rack().width(),1e-6);
        assertEquals(ZoneSizes.DEFAULT.sight().depth()*.8f,zones.sight().depth(),1e-6);
        assertTrue(zones.valid());
    }
    @Test void linkedDimensionsRespectBounds(){
        assertTrue(ZoneSizes.DEFAULT.resizeMounted(100).valid());assertTrue(ZoneSizes.DEFAULT.resizeMounted(.001f).valid());
        assertThrows(IllegalArgumentException.class,()->ZoneSizes.DEFAULT.resizeMounted(Float.NaN));
    }
    @Test void openPumpAllowsPouchForSidePortAndRearGrip(){
        var p=new WeaponProfile(.6f,0,6.825f,5.425f,.0125f,9.125f,-23.5f,.32f,WeaponProfile.Mechanism.PUMP);
        assertEquals(Handling.Target.POUCH,Handling.target(Handling.Phase.READY,new Vector3f(10),true,p,Calibration.ZERO));
        assertEquals(Handling.Target.POUCH,Handling.target(Handling.Phase.PUMP_OPEN,new Vector3f(10),true,p,Calibration.ZERO));
        assertEquals(Handling.Target.RACK,Handling.target(Handling.Phase.PUMP_OPEN,Handling.rack(p,Calibration.ZERO).add(0,0,.08f),false,p,Calibration.ZERO));
    }
}
