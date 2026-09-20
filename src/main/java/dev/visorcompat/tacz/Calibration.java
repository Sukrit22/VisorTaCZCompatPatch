package dev.visorcompat.tacz;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Translation in metres at world scale 1; intrinsic XYZ rotation in degrees. */
public record Calibration(float x, float y, float z, float pitch, float yaw, float roll,
                          float muzzleX, float muzzleY, float muzzleZ, InteractionOffsets interactions,
                          Float gunScale,ZoneSizes zones, Boolean scaleLock) {
    public Calibration {
        scaleLock=scaleLock==null?true:scaleLock;
        interactions=interactions==null?InteractionOffsets.ZERO:interactions;
        gunScale=gunScale==null?1f:gunScale;zones=zones==null?ZoneSizes.DEFAULT:zones;
    }
    public Calibration(float x,float y,float z,float pitch,float yaw,float roll,float mx,float my,float mz,InteractionOffsets interactions,Float scale,ZoneSizes zones) {
        this(x,y,z,pitch,yaw,roll,mx,my,mz,interactions,scale,zones,true);
    }
    public Calibration(float x,float y,float z,float pitch,float yaw,float roll,float mx,float my,float mz,InteractionOffsets interactions) {
        this(x,y,z,pitch,yaw,roll,mx,my,mz,interactions,1f,ZoneSizes.DEFAULT);
    }
    public Calibration(float x,float y,float z,float pitch,float yaw,float roll,float mx,float my,float mz) {
        this(x,y,z,pitch,yaw,roll,mx,my,mz,InteractionOffsets.ZERO);
    }
    public Calibration(float x, float y, float z, float pitch, float yaw, float roll) {
        this(x,y,z,pitch,yaw,roll,0,0,0);
    }
    public static final Calibration ZERO = new Calibration(0,0,0,0,0,0);
    public boolean valid() {
        return bounded(x,.25f) && bounded(y,.25f) && bounded(z,.25f)
            && bounded(pitch,180) && bounded(yaw,180) && bounded(roll,180)
            && bounded(muzzleX,.25f) && bounded(muzzleY,.25f) && bounded(muzzleZ,.25f) && interactions.valid()
            && Float.isFinite(gunScale) && gunScale>=.5f && gunScale<=1.5f && zones.valid();
    }
    private static boolean bounded(float value, float limit) {
        return Float.isFinite(value) && Math.abs(value) <= limit;
    }
    public Vector3f position(Vector3f origin, Quaternionf basis, float scale) {
        return new Vector3f(origin).add(basis.transform(translation().mul(scale)));
    }
    public Quaternionf orientation(Quaternionf basis) {
        return new Quaternionf(basis).mul(rotation()).normalize();
    }
    public Vector3f muzzleOffset(Vector3f base) {
        return new Vector3f(base).add(muzzleX,muzzleY,muzzleZ).mul(gunScale);
    }
    public Vector3f translation() { return new Vector3f(x,y,z); }
    public Quaternionf rotation() {
        float r = (float) (Math.PI / 180);
        return new Quaternionf().rotationXYZ(pitch*r, yaw*r, roll*r);
    }
}
