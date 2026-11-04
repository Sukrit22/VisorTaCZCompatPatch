package dev.visorcompat.tacz.physical;

import dev.visorcompat.tacz.TacticalCalibration;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Hand-driven insertion rail; only a fresh aligned approach can acquire it. */
public final class MagazineGuide {
    private MagazineGuide(){}
    public record Result(boolean engaged, boolean seated, float depth, Vector3f position, Quaternionf rotation){}
    public static Result solve(Vector3f position,Quaternionf rotation,Vector3f entry,Quaternionf rail,
                               TacticalCalibration c,boolean engaged){
        var axis=new Quaternionf(rail).transform(new Vector3f(0,1,0));
        var delta=new Vector3f(position).sub(entry);float depth=delta.dot(axis);
        float lateral=new Vector3f(delta).sub(new Vector3f(axis).mul(depth)).length();
        var angles=new Quaternionf(rail).conjugate().mul(rotation).getEulerAnglesYXZ(new Vector3f()).mul(180f/(float)Math.PI);
        boolean finite=Float.isFinite(depth+lateral+angles.x+angles.y+angles.z);
        boolean aligned=Math.abs(angles.x)<=c.pitchTolerance()&&Math.abs(angles.y)<=c.yawTolerance()&&Math.abs(angles.z)<=c.rollTolerance();
        boolean hold=finite&&(engaged?lateral<=c.entryRadius()*2&&depth>=-.025f&&depth<=c.seatedDepth()+.1f
                :aligned&&delta.length()<=c.entryRadius()&&depth<=Math.min(.025f,c.seatedDepth()*.4f));
        float travel=Math.max(0,Math.min(c.seatedDepth(),depth));
        return new Result(hold,hold&&depth>=c.seatedDepth()-Math.min(c.seatedTolerance(),c.seatedDepth()*.25f),travel,
                hold?new Vector3f(entry).add(axis.mul(travel)):new Vector3f(position),hold?new Quaternionf(rail):new Quaternionf(rotation));
    }
}
