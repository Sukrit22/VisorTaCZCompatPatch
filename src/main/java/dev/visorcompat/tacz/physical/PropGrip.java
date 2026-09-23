package dev.visorcompat.tacz.physical;
import org.joml.*;
/** Preserve the contact transform when carrying a prop away from its trigger grip. */
public record PropGrip(Vector3f offset,Quaternionf rotation){
    public static PropGrip capture(Vector3fc gun,Quaternionfc gunRotation,Vector3fc hand,Quaternionfc handRotation,float scale){
        var inverse=new Quaternionf(handRotation).conjugate();
        return new PropGrip(new Quaternionf(inverse).transform(new Vector3f(gun).sub(hand).div(scale)),inverse.mul(gunRotation));
    }
    public Vector3f position(Vector3fc hand,Quaternionfc handRotation,float scale){return new Quaternionf(handRotation).transform(new Vector3f(offset).mul(scale)).add(hand);}
    public Quaternionf orientation(Quaternionfc handRotation){return new Quaternionf(handRotation).mul(rotation).normalize();}
}
