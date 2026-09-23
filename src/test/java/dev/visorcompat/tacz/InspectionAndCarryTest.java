package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.PropGrip;
import org.joml.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class InspectionAndCarryTest {
    @TempDir Path directory;
    @Test void inspectionRoundTripPreservesSignedOffsets()throws Exception{
        var file=directory.resolve("inspection.json");var value=new InspectionCalibration(-.01f,.03f,-.08f,125,-30,45);
        InspectionFiles.write(file,Map.of("tacz:glock_17|tacz:default",value));
        assertEquals(value,InspectionFiles.read(file).get("tacz:glock_17|tacz:default"));
    }
    @Test void invalidInspectionDoesNotOverwriteSavedData()throws Exception{
        var file=directory.resolve("inspection.json");InspectionFiles.write(file,Map.of("gun",InspectionCalibration.defaults(true)));
        String saved=Files.readString(file);
        assertThrows(java.io.IOException.class,()->InspectionFiles.write(file,Map.of("gun",new InspectionCalibration(0,0,0,181,0,0))));
        assertEquals(saved,Files.readString(file));
    }
    @Test void contactCaptureDoesNotSnapGunToController(){
        var gun=new Vector3f(2,3,4);var hand=new Vector3f(2,3,3.5f);var q=new Quaternionf().rotateY(.7f);
        var capture=PropGrip.capture(gun,new Quaternionf(),hand,q,1);
        assertTrue(capture.position(hand,q,1).distance(gun)<1e-5);
        assertTrue(capture.position(new Vector3f(hand).add(1,0,0),q,1).distance(new Vector3f(gun).add(1,0,0))<1e-5);
        assertEquals(1,java.lang.Math.abs(capture.orientation(q).w),1e-5);
    }
    @Test void headRelativeHolsterFollowsCrouchWithoutChangingOffsets(){
        var c=HolsterCalibration.defaults(true);
        var standing=c.fromHead(new Vector3f(0,65.62f,0),0,1,1);
        var crouching=c.fromHead(new Vector3f(0,65.12f,0),0,1,1);
        assertEquals(64.94f,standing.y,1e-5);
        assertEquals(.5f,standing.y-crouching.y,1e-5);
        assertEquals(standing.x,crouching.x);assertEquals(standing.z,crouching.z);
    }
    @Test void headRelativeHolsterAppliesWorldScaleOnce(){
        var c=HolsterCalibration.defaults(true);
        assertEquals(101.88f,c.fromHead(new Vector3f(0,103.24f,0),0,2,1).y,1e-5);
    }
}
