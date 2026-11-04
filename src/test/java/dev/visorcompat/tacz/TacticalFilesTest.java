package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class TacticalFilesTest {
    @TempDir Path dir;
    @Test void defaultsSeparatePinchAndSeatAtOriginalMagwell(){
        var d=TacticalCalibration.DEFAULT;
        assertEquals(.045f,d.pinchGrip().x());
        assertEquals(0,d.magwellEntry().y()+d.seatedDepth(),.000001f);
        assertTrue(d.valid());
    }
    @Test void roundTripAndMissingFieldsDefault()throws Exception{
        var file=dir.resolve("tactical.json");TacticalFiles.write(file,Map.of("tacz:gun",TacticalCalibration.DEFAULT));
        assertEquals(TacticalCalibration.DEFAULT,TacticalFiles.read(file).get("tacz:gun"));
        Files.writeString(file,"{\"guns\":{\"tacz:gun\":{\"pinchGrip\":{\"x\":0.02},\"yawTolerance\":20}}}");
        var c=TacticalFiles.read(file).get("tacz:gun");assertEquals(.02f,c.pinchGrip().x());assertEquals(30,c.pinchGrip().yaw());assertEquals(20,c.yawTolerance());assertEquals(TacticalCalibration.DEFAULT.normalGrip(),c.normalGrip());
    }
    @Test void rejectsFutureSchemaWithoutOverwriting()throws Exception{
        var file=dir.resolve("future.json");String future="{\"schemaVersion\":2,\"guns\":{}}";Files.writeString(file,future);
        assertThrows(IOException.class,()->TacticalFiles.read(file));assertThrows(IOException.class,()->TacticalFiles.write(file,Map.of()));assertEquals(future,Files.readString(file));
    }
    @Test void alternativeControlsRoundTripAndDefaultOff()throws Exception{
        var file=dir.resolve("controls.json");var d=TacticalCalibration.DEFAULT;
        var c=new TacticalCalibration(d.normalGrip(),d.pinchGrip(),d.magwellEntry(),d.pitchTolerance(),d.yawTolerance(),d.rollTolerance(),d.entryRadius(),d.seatedDepth(),d.seatedTolerance(),true);
        TacticalFiles.write(file,Map.of("tacz:glock_17",c));assertTrue(TacticalFiles.read(file).get("tacz:glock_17").alternativeControls());
        Files.writeString(file,"{\"guns\":{\"tacz:glock_17\":{}}}");assertFalse(TacticalFiles.read(file).get("tacz:glock_17").alternativeControls());
    }
    @Test void rejectsInvalidFiniteRangesAndPreservesFile()throws Exception{
        var file=dir.resolve("invalid.json");TacticalFiles.write(file,Map.of());String original=Files.readString(file);var d=TacticalCalibration.DEFAULT;
        var invalid=new TacticalCalibration(d.normalGrip(),d.pinchGrip(),d.magwellEntry(),Float.NaN,35,35,.08f,.06f,.015f);
        assertFalse(invalid.valid());assertThrows(IOException.class,()->TacticalFiles.write(file,Map.of("gun",invalid)));assertEquals(original,Files.readString(file));
        Files.writeString(file,"{\"guns\":{\"gun\":{\"entryRadius\":0}}}");assertThrows(IOException.class,()->TacticalFiles.read(file));
    }
}
