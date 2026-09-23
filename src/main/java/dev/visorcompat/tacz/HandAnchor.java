package dev.visorcompat.tacz;
import org.joml.*;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
/** Relative transform for support transfer or a pose-preserving main-hand re-grip. */
public record HandAnchor(Quaternionf rotation,Vector3f offset,boolean mainHand,Vector3f support) {
    public HandAnchor(Quaternionf rotation,Vector3f offset,boolean mainHand){this(rotation,offset,mainHand,null);}
    public HandAnchor(Quaternionf rotation,Vector3f offset){this(rotation,offset,false);}
    public static HandAnchor capture(GunPose gun,VRPlayerPose pose){return capture(gun,pose,false);}
    public static HandAnchor captureMain(GunPose gun,VRPlayerPose pose){return capture(gun,pose,true);}
    private static HandAnchor capture(GunPose gun,VRPlayerPose pose,boolean main){
        var controller=main?pose.getMainHand():pose.getOffhand();
        var inverse=controller.getRotation().getNormalizedRotation(new Quaternionf()).conjugate();
        return new HandAnchor(new Quaternionf(inverse).mul(gun.rotation()),inverse.transform(new Vector3f(gun.hand()).sub(controller.getPosition()).div(gun.worldScale())),main,main?new Quaternionf(gun.rotation()).conjugate().transform(new Vector3f(pose.getOffhand().getPosition()).sub(gun.hand()).div(gun.worldScale())):null);
    }
    public GunPose resolve(VRPlayerPose pose,WeaponProfile profile,Calibration c){
        float scale=pose.getWorldScale();if(!Float.isFinite(scale)||scale<.25f||scale>4)return null;
        var controller=mainHand?pose.getMainHand():pose.getOffhand();
        var basis=controller.getRotation().getNormalizedRotation(new Quaternionf());
        var hand=new Vector3f(controller.getPosition()).add(new Quaternionf(basis).transform(new Vector3f(offset).mul(scale)));
        var rot=basis.mul(rotation).normalize();var direction=rot.transform(new Vector3f(0,0,-1));
        var muzzle=new Quaternionf(rot).transform(c.muzzleOffset(profile.muzzleOffset()).mul(scale)).add(hand);
        return PoseMath.finite(hand)&&PoseMath.finite(muzzle)&&PoseMath.finite(direction)?new GunPose(hand,rot,muzzle,direction,scale):null;
    }
    public static void write(net.minecraft.network.FriendlyByteBuf b,HandAnchor a){
        b.writeBoolean(a!=null);if(a!=null){b.writeFloat(a.rotation.x);b.writeFloat(a.rotation.y);b.writeFloat(a.rotation.z);b.writeFloat(a.rotation.w);b.writeFloat(a.offset.x);b.writeFloat(a.offset.y);b.writeFloat(a.offset.z);b.writeBoolean(a.mainHand);b.writeBoolean(a.support!=null);if(a.support!=null)b.writeVector3f(a.support);}
    }
    public static HandAnchor read(net.minecraft.network.FriendlyByteBuf b){return b.readBoolean()?new HandAnchor(new Quaternionf(b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat()),new Vector3f(b.readFloat(),b.readFloat(),b.readFloat()),b.readBoolean(),b.readBoolean()?b.readVector3f():null):null;}
}
