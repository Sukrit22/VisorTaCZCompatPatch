package dev.visorcompat.tacz;
import com.google.gson.Gson;
import dev.visorcompat.tacz.physical.Handling;
import org.joml.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InteractionTest {
    @Test void previousInteractionJsonDefaultsNewPortWithoutLosingRack(){
        var offsets=new Gson().fromJson("{\"rack\":{\"x\":0.03,\"y\":0,\"z\":0}}",InteractionOffsets.class);
        assertEquals(.03f,offsets.rack().x());assertEquals(InteractionOffsets.Point.ZERO,offsets.port());
    }
    @Test void portCalibrationMovesOnlyThePort(){
        var offsets=new InteractionOffsets(null,null,null,null,null,null,new InteractionOffsets.Point(.02f,-.01f,.04f));
        var c=new Calibration(0,0,0,0,0,0,0,0,0,offsets);
        var base=dev.visorcompat.tacz.physical.JamProfile.of(rifle,Calibration.ZERO).port();
        var moved=dev.visorcompat.tacz.physical.JamProfile.of(rifle,c).port();
        assertTrue(moved.sub(base).distance(new Vector3f(.02f,-.01f,.04f))<1e-6);
        assertEquals(Handling.rack(rifle,Calibration.ZERO),Handling.rack(rifle,c));
        assertEquals(c,new Gson().fromJson(new Gson().toJson(c),Calibration.class));
    }
    private final WeaponProfile rifle=Profiles.byId("tacz:m4a1");
    @Test void oldCalibrationMigratesWithoutLosingGrip(){
        var c=new Gson().fromJson("{\"x\":0.01,\"pitch\":5,\"muzzleZ\":-0.02}",Calibration.class);
        assertEquals(.01f,c.x());assertEquals(5,c.pitch());assertEquals(-.02f,c.muzzleZ());assertEquals(InteractionOffsets.ZERO,c.interactions());assertTrue(c.valid());
    }
    @Test void offsetsRoundTripAndRejectNonfinite(){
        var c=new Calibration(0,0,0,0,0,0,0,0,0,new InteractionOffsets(new InteractionOffsets.Point(.02f,.07f,0),null,null,null,null,null));
        assertEquals(c,new Gson().fromJson(new Gson().toJson(c),Calibration.class));
        assertFalse(new InteractionOffsets.Point(Float.NaN,0,0).valid());assertFalse(new InteractionOffsets.Point(.501f,0,0).valid());
    }
    @Test void raisedForegripDoesNotPitchBarrelWhenHandsMatchCalibratedPoints(){
        var support=new Vector3f(0,.08f,-.3f);var rotation=new Quaternionf().rotateY(.6f);
        var main=new Vector3f(2,3,4);var off=rotation.transform(new Vector3f(support).mul(2)).add(main);
        var result=PoseMath.supportedRotation(rotation,main,off,support,2,true);
        assertTrue(result.transform(new Vector3f(0,0,-1)).distance(rotation.transform(new Vector3f(0,0,-1)))<1e-5);
    }
    @Test void pouchOnlyClaimsTheReplacementStage(){
        for(var phase:Handling.Phase.values())assertEquals(phase==Handling.Phase.NO_MAG?Handling.Target.POUCH:Handling.Target.NONE,
            Handling.target(phase,new Vector3f(1,1,1),true,rifle,Calibration.ZERO));
    }
    @Test void outsideZonesPassThroughAndCalibrationMovesGrabZone(){
        var c=new Calibration(0,0,0,0,0,0,0,0,0,new InteractionOffsets(null,new InteractionOffsets.Point(.3f,0,0),null,null,null,null));
        assertEquals(Handling.Target.MAGAZINE,Handling.target(Handling.Phase.READY,Handling.magazine(rifle,c),false,rifle,c));
        assertEquals(Handling.Target.NONE,Handling.target(Handling.Phase.READY,Handling.magazine(rifle),false,rifle,c));
    }
    @Test void pouchOffsetsFollowHeadYawAndScale(){
        var c=new Calibration(0,0,0,0,0,0,0,0,0,new InteractionOffsets(null,null,null,null,new InteractionOffsets.Point(.2f,.1f,0),null));
        var forward=new Vector3f(1,0,0);var base=Handling.pouch(new Vector3f(),forward,2);
        assertTrue(Handling.pouch(new Vector3f(),forward,2,c).sub(base).distance(new Vector3f(0,.2f,.4f))<1e-5);
    }
}
