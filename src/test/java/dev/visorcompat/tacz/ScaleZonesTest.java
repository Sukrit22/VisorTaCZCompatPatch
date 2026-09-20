package dev.visorcompat.tacz;
import com.google.gson.Gson;
import dev.visorcompat.tacz.physical.Handling;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScaleZonesTest {
    private final WeaponProfile pump=new WeaponProfile(.6f,0,6.825f,5.425f,.0125f,9.125f,-23.5f,.32f,WeaponProfile.Mechanism.PUMP);
    private Calibration calibration(float scale){return new Calibration(0,.017f,0,13,0,0,0,0,0,InteractionOffsets.ZERO,scale,ZoneSizes.DEFAULT);}
    @Test void oldJsonKeepsOffsetsAndDefaultsToFullSize(){
        var c=new Gson().fromJson("{\"y\":0.08,\"interactions\":{\"rack\":{\"z\":-0.28}}}",Calibration.class);
        assertTrue(c.valid());assertEquals(1f,c.gunScale());assertEquals(.08f,c.y());assertEquals(-.28f,c.interactions().rack().z());
        assertEquals(ZoneSizes.DEFAULT,c.zones());
    }
    @Test void sizeMovesMuzzleAndMountedPointsButNotGripOrPouch(){
        var full=calibration(1);var small=calibration(.75f);
        assertEquals(full.translation(),small.translation());
        assertTrue(full.muzzleOffset(pump.muzzleOffset()).mul(.75f).distance(small.muzzleOffset(pump.muzzleOffset()))<1e-6);
        assertTrue(Handling.rack(pump,full).mul(.75f).distance(Handling.rack(pump,small))<1e-6);
        assertEquals(Handling.pouch(new Vector3f(),new Vector3f(0,0,-1),1,full),Handling.pouch(new Vector3f(),new Vector3f(0,0,-1),1,small));
    }
    @Test void invalidSizesAreRejectedRatherThanNormalized(){
        assertFalse(calibration(0).valid());assertFalse(calibration(Float.NaN).valid());assertFalse(calibration(1.51f).valid());
        assertTrue(calibration(.5f).valid());assertTrue(calibration(1.5f).valid());
        assertFalse(new ZoneSizes.Box(0,.1f,.1f).valid());assertFalse(new ZoneSizes.Box(.1f,Float.POSITIVE_INFINITY,.1f).valid());
    }
    @Test void boxDimensionsAreIndependentAndInclusive(){
        var box=new ZoneSizes.Box(.1f,.2f,.4f);var center=new Vector3f();
        assertTrue(box.contains(new Vector3f(.05f,.1f,.2f),center));
        assertFalse(box.contains(new Vector3f(.051f,0,0),center));
        assertFalse(box.contains(new Vector3f(0,.101f,0),center));
        assertFalse(box.contains(new Vector3f(0,0,.201f),center));
        assertFalse(box.contains(new Vector3f(Float.NaN,0,0),center));
    }
    @Test void calibratedPumpAndSupportUseExactlyTheSamePoint(){
        var c=CalibrationDefaults.load().get("tacz:m870|tacz:default");
        var point=Handling.rack(pump,c);assertEquals(point,Handling.support(pump,c));
        var off=new Vector3f(point).add(.03f,0,0);
        var rotation=PoseMath.supportedRotation(new Quaternionf(),new Vector3f(),off,point,1,true,c.zones().rack());
        assertTrue(rotation.transform(new Vector3f(point).normalize()).distance(off.normalize())<1e-5);
    }
    @Test void pouchCoordinatesFollowHeadYawAndWorldScale(){
        var c=Calibration.ZERO;var head=new Vector3f(1,2,3);var forward=new Vector3f(1,0,0);
        var center=Handling.pouch(head,forward,2,c);
        assertTrue(Handling.inPouch(new Vector3f(center).add(0,0,.49f),head,forward,2,c));
        assertFalse(Handling.inPouch(new Vector3f(center).add(0,0,.51f),head,forward,2,c));
    }
    @Test void sightDepthAndWidthAffectAdsRatherThanHandGrab(){
        var box=new ZoneSizes.Box(.04f,.06f,.2f);var look=new Vector3f(0,0,-1);var sight=new Vector3f();
        assertTrue(OpticMath.aligned(new Vector3f(.015f,0,.1f),look,sight,false,box));
        assertFalse(OpticMath.aligned(new Vector3f(.025f,0,.1f),look,sight,false,box));
        assertFalse(OpticMath.aligned(new Vector3f(0,0,.3f),look,sight,false,box));
    }
}
