package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class HolsterFilesTest {
    @TempDir Path dir;
    @Test void roundTripKeepsExplicitPerGunOffsets()throws Exception{var path=dir.resolve("holsters.json");var c=new HolsterCalibration(.27f,.88f,-.2f,-55,10,5);HolsterFiles.write(path,Map.of("tacz:glock_17|tacz:default",c));assertEquals(c,HolsterFiles.read(path).get("tacz:glock_17|tacz:default"));}
    @Test void invalidReloadDoesNotRewriteFile()throws Exception{var path=dir.resolve("holsters.json");for(String s:new String[]{"{","null","{\"gun\":null}","{\"gun\":{\"y\":-3}}"}){Files.writeString(path,s);assertThrows(IOException.class,()->HolsterFiles.read(path));assertEquals(s,Files.readString(path));}}
    @Test void invalidSavePreservesPreviousData()throws Exception{var path=dir.resolve("holsters.json");HolsterFiles.write(path,Map.of("gun",HolsterCalibration.defaults(true)));String old=Files.readString(path);assertThrows(IOException.class,()->HolsterFiles.write(path,Map.of("gun",new HolsterCalibration(0,-1,0,0,0,0))));assertEquals(old,Files.readString(path));}
}
