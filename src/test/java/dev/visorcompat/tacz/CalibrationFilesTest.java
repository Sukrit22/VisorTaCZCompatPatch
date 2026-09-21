package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class CalibrationFilesTest {
    @TempDir Path dir;
    @Test void reloadReadsChangesAndKeepsExplicitZerosInsteadOfAddingDefaults() throws Exception {
        var file=dir.resolve("calibration.json");
        Files.writeString(file,"{\"tacz:m870|tacz:default\":{\"y\":0.03}}");
        var first=CalibrationFiles.read(file);
        assertEquals(.03f,first.get("tacz:m870|tacz:default").y());
        assertEquals(0f,first.get("tacz:m870|tacz:default").pitch());
        Files.writeString(file,"{\"tacz:m870|tacz:default\":{\"y\":-0.01}}");
        assertEquals(-.01f,CalibrationFiles.read(file).get("tacz:m870|tacz:default").y());
        assertEquals(.03f,first.get("tacz:m870|tacz:default").y());
    }
    @Test void malformedOrInvalidReplacementFailsAndCanBeRetried() throws Exception {
        var file=dir.resolve("calibration.json");
        for(var text:new String[]{"{", "null", "", "{\"gun\":null}", "{\"gun\":{\"y\":900}}"}) {
            Files.writeString(file,text);assertThrows(IOException.class,()->CalibrationFiles.read(file));
            assertEquals(text,Files.readString(file));
        }
        Files.writeString(file,"{}");assertEquals(CalibrationDefaults.load(),CalibrationFiles.read(file));
    }
    @Test void removedFileOrProfileRestoresBundledFallback() throws Exception {
        var file=dir.resolve("calibration.json");
        assertEquals(CalibrationDefaults.load(),CalibrationFiles.read(file));
        Files.writeString(file,"{\"custom:gun|custom:display\":{\"z\":0.01}}");
        assertEquals(CalibrationDefaults.load().size()+1,CalibrationFiles.read(file).size());
        Files.writeString(file,"{}");assertEquals(CalibrationDefaults.load().size(),CalibrationFiles.read(file).size());
    }
}
