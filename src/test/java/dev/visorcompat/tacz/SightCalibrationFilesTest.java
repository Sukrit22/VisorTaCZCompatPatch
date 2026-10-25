package dev.visorcompat.tacz;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class SightCalibrationFilesTest {
    @TempDir Path directory;
    @Test void absentOptionalFileUsesBaselineOnly()throws Exception{assertTrue(SightCalibrationFiles.read(directory.resolve("absent.json")).isEmpty());}
    @Test void attachmentAndViewKeysKeepIndependentOffsetsAndOptionalDimensions()throws Exception{
        Path file=directory.resolve("sights.json");
        Files.writeString(file,"{\"gun|scope|view=0\":{\"x\":0,\"y\":0,\"z\":-0.7},\"gun|scope|view=1\":{\"x\":0.01,\"y\":0,\"z\":-0.2,\"box\":{\"width\":0.1,\"height\":0.12,\"depth\":0.4}}}");
        var map=SightCalibrationFiles.read(file);
        assertEquals(-.7f,map.get("gun|scope|view=0").z());assertNull(map.get("gun|scope|view=0").box());
        assertEquals(.4f,map.get("gun|scope|view=1").box().depth());
    }
    @Test void unreadableFileIsRejectedAndPreserved()throws Exception{
        Path file=directory.resolve("sights.json");String invalid="{\"gun|scope\":{\"z\":-2.1}}";Files.writeString(file,invalid);
        assertThrows(java.io.IOException.class,()->SightCalibrationFiles.read(file));assertEquals(invalid,Files.readString(file));
    }
}
