package dev.visorcompat.tacz;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/** Independent magazine-grip file, using the existing validated six-axis codec. */
public final class MagazineGripFiles {
    private MagazineGripFiles() {}
    public static final InspectionCalibration DEFAULT=new InspectionCalibration(0,0,0,0,30,0);
    public static Map<String,InspectionCalibration> read(Path file)throws IOException{return InspectionFiles.read(file);}
    public static void write(Path file,Map<String,InspectionCalibration> values)throws IOException{InspectionFiles.write(file,values);}
}
