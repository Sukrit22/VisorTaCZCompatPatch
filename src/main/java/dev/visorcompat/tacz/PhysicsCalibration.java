package dev.visorcompat.tacz;
import org.joml.Vector3f;
import org.joml.Quaternionf;
/** Magazine geometry in unscaled gun-local metres. Independent of interaction zones. */
public record PhysicsCalibration(float offsetX,float offsetY,float offsetZ,float pitch,float roll,float travel,
                                 float width,float height,float depth,float friction,float slideTravel) {
    public static PhysicsCalibration defaults(WeaponProfile profile){
        var d=DescriptorProfiles.details(profile);float stroke=d==null?.055f:d.visual().shotTravel();
        return new PhysicsCalibration(0,0,0,0,0,profile.supportDistance()==0&&!profile.manualAction()?.10f:.08f,
            .035f,.14f,.065f,.25f,stroke>0?stroke:.055f);
    }
    public boolean valid(){return range(offsetX,-1,1)&&range(offsetY,-1,1)&&range(offsetZ,-1,1)
        &&range(pitch,-180,180)&&range(roll,-180,180)&&range(travel,.005f,.5f)
        &&range(width,.005f,.5f)&&range(height,.005f,.5f)&&range(depth,.005f,.5f)
        &&range(friction,0,20)&&range(slideTravel,.005f,.15f);}
    private static boolean range(float n,float min,float max){return Float.isFinite(n)&&n>=min&&n<=max;}
    public Vector3f origin(WeaponProfile profile){return dev.visorcompat.tacz.physical.Handling.magazine(profile).add(offsetX,offsetY,offsetZ);}
    public Vector3f exitDirection(){float r=(float)Math.PI/180;return new Quaternionf().rotateX(pitch*r).rotateZ(roll*r).transform(new Vector3f(0,-1,0));}
}
