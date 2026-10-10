package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MagazineGripFilesTest {
    @TempDir Path dir;
    @Test void retainsSixAxesAndDoesNotTouchGunOrInspectionFiles()throws Exception{
        var gun=dir.resolve("visor_tacz-calibration.json");var inspection=dir.resolve("visor_tacz-inspections.json");
        Files.writeString(gun,"existing gun calibration");Files.writeString(inspection,"existing inspection calibration");
        var file=dir.resolve("visor_tacz-magazine-grips.json");
        var c=new InspectionCalibration(.013f,-.024f,.035f,-11,42,17);
        MagazineGripFiles.write(file,Map.of("tacz:ak47|tacz:default",c));
        assertEquals(c,MagazineGripFiles.read(file).get("tacz:ak47|tacz:default"));
        assertEquals("existing gun calibration",Files.readString(gun));
        assertEquals("existing inspection calibration",Files.readString(inspection));
    }
    @Test void invalidSaveAndMalformedReadPreserveExistingFile()throws Exception{
        var file=dir.resolve("grips.json");MagazineGripFiles.write(file,Map.of("gun",MagazineGripFiles.DEFAULT));String original=Files.readString(file);
        assertThrows(IOException.class,()->MagazineGripFiles.write(file,Map.of("gun",new InspectionCalibration(.51f,0,0,0,0,0))));
        assertEquals(original,Files.readString(file));
        Files.writeString(file,"{bad");assertThrows(IOException.class,()->MagazineGripFiles.read(file));assertEquals("{bad",Files.readString(file));
    }
    @Test void defaultMatchesExistingThirtyDegreeGrip(){assertEquals(new InspectionCalibration(0,0,0,0,30,0),MagazineGripFiles.DEFAULT);assertTrue(MagazineGripFiles.DEFAULT.valid());}
}
