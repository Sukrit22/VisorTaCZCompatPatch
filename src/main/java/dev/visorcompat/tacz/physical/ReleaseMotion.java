package dev.visorcompat.tacz.physical;
import org.joml.*;
/** Velocity at a release point includes angular motion around the grip. */
public record ReleaseMotion(Vector3f velocity,Vector3f spin) {
    public static ReleaseMotion between(Vector3fc before,Quaternionfc oldRotation,Vector3fc now,Quaternionfc rotation,Vector3fc offset,float dt,float scale){
        if(dt<=0||dt>.15f)return new ReleaseMotion(new Vector3f(),new Vector3f());
        var linear=new Vector3f(now).sub(before).div(dt);
        var delta=new Quaternionf(rotation).mul(new Quaternionf(oldRotation).conjugate()).normalize();
        if(delta.w<0)delta.set(-delta.x,-delta.y,-delta.z,-delta.w);
        float angle=2*(float)java.lang.Math.acos(java.lang.Math.max(-1,java.lang.Math.min(1,delta.w)));
        var spin=new Vector3f(delta.x,delta.y,delta.z);
        if(spin.lengthSquared()>.000001f)spin.normalize(java.lang.Math.min(20,angle/dt));else spin.zero();
        var velocity=linear.add(new Vector3f(spin).cross(new Vector3f(offset)));
        if(velocity.length()>6*scale)velocity.normalize(6*scale);
        return new ReleaseMotion(velocity,spin);
    }
}
