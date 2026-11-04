package dev.visorcompat.tacz.physical;

import dev.visorcompat.tacz.*;
import net.minecraft.world.item.ItemStack;
import org.joml.*;

/** Shared server/render transforms, in calibrated gun-local metres. */
public final class TacticalPose {
    private TacticalPose(){}
    public record Pose(Vector3f position,Quaternionf rotation){}
    public static Quaternionf rotation(InspectionCalibration c){float r=(float)java.lang.Math.PI/180;return new Quaternionf().rotateY(c.yaw()*r).rotateX(c.pitch()*r).rotateZ(c.roll()*r);}
    public static Pose magazine(ItemStack stack,boolean pinch,TacticalCalibration c,GunPose gun,Vector3f hand,Quaternionf handRotation){
        var grip=pinch?c.pinchGrip():c.normalGrip();
        var offset=TacticalState.captured(stack,pinch)?TacticalState.position(stack,pinch):new Vector3f(grip.x(),grip.y(),grip.z());
        var q=TacticalState.captured(stack,pinch)?TacticalState.rotation(stack,pinch):rotation(grip);
        var world=new Quaternionf(handRotation).transform(offset.mul(gun.worldScale())).add(hand);
        return new Pose(Handling.local(gun,world),new Quaternionf(gun.rotation()).conjugate().mul(handRotation).mul(q));
    }
    public static Vector3f entry(WeaponProfile profile,Calibration calibration,TacticalCalibration c){var v=c.magwellEntry();return Handling.magazine(profile,calibration).add(v.x(),v.y(),v.z());}
}
