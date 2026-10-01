package dev.visorcompat.tacz;
import java.util.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Quaternionf;
/** Pure shared recoil timeline; server and each client own separate instances. Angles are degrees. */
public final class RecoilMotion {
    public static final int SAMPLES=33;
    public record Curve(float ticks,float[] pitch,float[] yaw) {
        public Curve { pitch=pitch.clone();yaw=yaw.clone(); }
        public boolean valid(){if(!Float.isFinite(ticks)||ticks<=0||ticks>40||pitch.length!=SAMPLES||yaw.length!=SAMPLES)return false;for(float v:pitch)if(!Float.isFinite(v))return false;for(float v:yaw)if(!Float.isFinite(v))return false;return true;}
        private float sample(float[] a,double age){if(age<=0||age>=ticks)return 0;double at=age/ticks*(SAMPLES-1);int i=(int)at;return a[i]+(a[i+1]-a[i])*(float)(at-i);}
    }
    private record Impulse(double start,Curve curve){}
    private final List<Impulse> impulses=new ArrayList<>();
    public void add(double start,Curve curve){if(!curve.valid())return;impulses.removeIf(i->start-i.start>=i.curve.ticks);if(impulses.size()>=16)impulses.remove(0);impulses.add(new Impulse(start,curve));}
    public Vector2f angles(double time){float p=0,y=0;for(var i:impulses){p+=i.curve.sample(i.curve.pitch,time-i.start);y+=i.curve.sample(i.curve.yaw,time-i.start);}return new Vector2f(Math.max(-2,Math.min(8,p)),Math.max(-4,Math.min(4,y)));}
    public GunPose apply(GunPose base,double time){if(base==null)return null;var a=angles(time);var local=new Quaternionf().rotateY((float)Math.toRadians(a.y)).rotateX((float)Math.toRadians(a.x));var q=new Quaternionf(base.rotation()).mul(local);var offset=new Quaternionf(base.rotation()).conjugate().transform(new Vector3f(base.muzzle()).sub(base.hand()));return new GunPose(base.hand(),q,q.transform(offset).add(base.hand()),q.transform(new Vector3f(0,0,-1)),base.worldScale());}
}
