package dev.visorcompat.tacz;
import org.joml.*;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
/** Relative rigid transform captured when handing the gun to the support controller. */
public record HandAnchor(Quaternionf rotation,Vector3f offset) {
    public static HandAnchor capture(GunPose gun,VRPlayerPose pose){
        var inverse=pose.getOffhand().getRotation().getNormalizedRotation(new Quaternionf()).conjugate();
        return new HandAnchor(new Quaternionf(inverse).mul(gun.rotation()),inverse.transform(new Vector3f(gun.hand()).sub(pose.getOffhand().getPosition()).div(gun.worldScale())));
    }
    public GunPose resolve(VRPlayerPose pose,WeaponProfile profile,Calibration c){
        float scale=pose.getWorldScale();if(!Float.isFinite(scale)||scale<.25f||scale>4)return null;
        var basis=pose.getOffhand().getRotation().getNormalizedRotation(new Quaternionf());
        var hand=new Vector3f(pose.getOffhand().getPosition()).add(new Quaternionf(basis).transform(new Vector3f(offset).mul(scale)));
        var rot=basis.mul(rotation).normalize();var direction=rot.transform(new Vector3f(0,0,-1));
        var muzzle=new Quaternionf(rot).transform(c.muzzleOffset(profile.muzzleOffset()).mul(scale)).add(hand);
        return PoseMath.finite(hand)&&PoseMath.finite(muzzle)&&PoseMath.finite(direction)?new GunPose(hand,rot,muzzle,direction,scale):null;
    }
    public static void write(net.minecraft.network.FriendlyByteBuf b,HandAnchor a){
        b.writeBoolean(a!=null);if(a!=null){b.writeFloat(a.rotation.x);b.writeFloat(a.rotation.y);b.writeFloat(a.rotation.z);b.writeFloat(a.rotation.w);b.writeFloat(a.offset.x);b.writeFloat(a.offset.y);b.writeFloat(a.offset.z);}
    }
    public static HandAnchor read(net.minecraft.network.FriendlyByteBuf b){return b.readBoolean()?new HandAnchor(new Quaternionf(b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat()),new Vector3f(b.readFloat(),b.readFloat(),b.readFloat())):null;}
}
