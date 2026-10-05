package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class PhysicsFilesTest {
 @TempDir Path dir;
 @Test void roundtripAndInvalidSavePreservesFile()throws Exception{var p=PhysicsCalibration.defaults(Profiles.byId("tacz:glock_17"));var f=dir.resolve("physics.json");PhysicsFiles.write(f,Map.of("tacz:glock_17|tacz:default",p));assertEquals(p,PhysicsFiles.read(f).values().iterator().next());String before=Files.readString(f);assertThrows(java.io.IOException.class,()->PhysicsFiles.write(f,Map.of("bad",new PhysicsCalibration(0,0,0,0,0,-1,.1f,.1f,.1f,0,.055f))));assertEquals(before,Files.readString(f));}
 @Test void invalidFileRejected()throws Exception{var f=dir.resolve("bad.json");Files.writeString(f,"{\"gun\":{}}");assertThrows(java.io.IOException.class,()->PhysicsFiles.read(f));}
}
