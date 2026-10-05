package dev.visorcompat.tacz.physical;
import org.joml.Vector3f;
import org.joml.Quaternionf;
/** A one-dimensional sliding constraint, then ballistic flight. No inventory or mesh collision. */
public final class MagazineMotion {
    private final Vector3f localAxis;private final float clearance,friction,scale;
    private final Vector3f origin,position,velocity=new Vector3f(),spin=new Vector3f();
    private final Quaternionf rotation;private float distance;private boolean free;
    public MagazineMotion(Vector3f origin,Quaternionf rotation,Vector3f axis,float clearance,float friction,float scale){this.origin=new Vector3f(origin);this.position=new Vector3f(origin);this.rotation=new Quaternionf(rotation);this.localAxis=new Vector3f(axis).normalize();this.clearance=clearance;this.friction=friction;this.scale=scale;}
    public boolean free(){return free;}public float distance(){return distance;}public Vector3f position(){return new Vector3f(position);}public Vector3f velocity(){return new Vector3f(velocity);}public Quaternionf rotation(){return new Quaternionf(rotation);}
    public void detach(){free=true;}
    public void advance(Vector3f nextOrigin,Quaternionf nextRotation,float seconds){
        if(!Float.isFinite(seconds)||seconds<=0)return;
        if(seconds>.15f){velocity.zero();spin.zero();if(!free){origin.set(nextOrigin);rotation.set(nextRotation);position.set(nextOrigin).add(new Quaternionf(rotation).transform(new Vector3f(localAxis)).mul(distance));}return;}
        var startOrigin=new Vector3f(origin);var startRotation=new Quaternionf(rotation);
        int steps=Math.max(1,(int)Math.ceil(seconds*120));float dt=seconds/steps;
        for(int i=1;i<=steps;i++){
            if(free){velocity.y-=9.81f*scale*dt;position.fma(dt,velocity);rotation.premul(new Quaternionf().rotationXYZ(spin.x*dt,spin.y*dt,spin.z*dt)).normalize();continue;}
            float t=(float)i/steps;var o=new Vector3f(startOrigin).lerp(nextOrigin,t);var q=new Quaternionf(startRotation).slerp(nextRotation,t);
            var axis=new Quaternionf(q).transform(new Vector3f(localAxis));var previous=new Vector3f(position);
            velocity.y-=9.81f*scale*dt;
            float proposed=new Vector3f(position).fma(dt,velocity).sub(o).dot(axis);
            float delta=proposed-distance;float drag=Math.min(Math.abs(delta),friction*scale*dt*dt);
            proposed-=Math.copySign(drag,delta);distance=Math.max(0,proposed);
            position.set(o).fma(distance,axis);
            velocity.set(position).sub(previous).div(dt);if(velocity.length()>6*scale)velocity.normalize(6*scale);
            var angular=ReleaseMotion.between(origin,rotation,o,q,new Vector3f(),dt,scale);spin.set(angular.spin());
            origin.set(o);rotation.set(q);
            if(distance>=clearance)free=true;
        }
    }
}
