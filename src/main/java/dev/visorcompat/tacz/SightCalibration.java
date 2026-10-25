package dev.visorcompat.tacz;

import org.joml.Vector3f;

/** Extra gun-local sight adjustment; null box inherits the gun baseline dimensions. */
public record SightCalibration(float x,float y,float z,ZoneSizes.Box box) {
    public static final SightCalibration ZERO=new SightCalibration(0,0,0,null);
    public boolean valid(){return Float.isFinite(x)&&Float.isFinite(y)&&Float.isFinite(z)
        &&Math.abs(x)<=2&&Math.abs(y)<=2&&Math.abs(z)<=2&&(box==null||box.valid());}
    public Vector3f apply(Vector3f modelSight,Calibration gun){
        return new Vector3f(modelSight).add(gun.interactions().sight().vector()).add(x,y,z).mul(gun.gunScale());
    }
    public ZoneSizes.Box dimensions(Calibration gun){return box==null?gun.zones().sight():box;}
}
