package dev.visorcompat.tacz;
import org.joml.*;
/** Inspection-only offsets relative to the normal calibrated controller grip. */
public record InspectionCalibration(float x,float y,float z,float pitch,float yaw,float roll){
    public static InspectionCalibration defaults(boolean ignored){return new InspectionCalibration(0,0,0,0,0,0);}
    public boolean valid(){return Float.isFinite(x+y+z+pitch+yaw+roll)&&java.lang.Math.abs(x)<=.5f&&java.lang.Math.abs(y)<=.5f&&java.lang.Math.abs(z)<=.5f&&java.lang.Math.abs(pitch)<=180&&java.lang.Math.abs(yaw)<=180&&java.lang.Math.abs(roll)<=180;}
    public Vector3f position(GunPose grip){return new Quaternionf(grip.rotation()).transform(new Vector3f(x,y,z).mul(grip.worldScale())).add(grip.hand());}
    public Quaternionf rotation(GunPose grip){float r=(float)java.lang.Math.PI/180;return new Quaternionf(grip.rotation()).rotateY(yaw*r).rotateX(pitch*r).rotateZ(roll*r);}
}
