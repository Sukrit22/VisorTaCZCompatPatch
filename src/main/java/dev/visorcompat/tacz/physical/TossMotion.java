package dev.visorcompat.tacz.physical;
import org.joml.Vector3f;
import org.joml.Vector3fc;
public final class TossMotion {
    public static Vector3f advance(Vector3fc position,Vector3fc velocity,float gravity,float dt){return new Vector3f(position).fma(dt,velocity).add(0,-.5f*gravity*dt*dt,0);}
    public static boolean nearSegment(Vector3fc from,Vector3fc to,Vector3fc hand,float radius){
        var delta=new Vector3f(to).sub(from);float length=delta.lengthSquared();
        float t=length<1e-8f?0:Math.max(0,Math.min(1,new Vector3f(hand).sub(from).dot(delta)/length));
        return new Vector3f(from).fma(t,delta).distanceSquared(hand)<=radius*radius;
    }
}
