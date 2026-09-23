package dev.visorcompat.tacz;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.player.VRPlayerPose;

public record GunPose(Vector3f hand, Quaternionf rotation, Vector3f muzzle, Vector3f direction,
                      float worldScale) {
    public static GunPose resolve(VRPlayerPose pose, WeaponProfile profile, boolean emptyOffhand) {
        return resolve(pose, profile, emptyOffhand, Calibration.ZERO);
    }
    public static GunPose resolve(VRPlayerPose pose, WeaponProfile profile, boolean emptyOffhand, Calibration calibration) {
        return resolve(pose,profile,emptyOffhand,calibration,null);
    }
    public static GunPose resolve(VRPlayerPose pose,WeaponProfile profile,boolean emptyOffhand,Calibration calibration,HandAnchor anchor) {
        if (pose == null || profile == null || calibration == null || !calibration.valid()) return null;
        if(anchor!=null){
            var base=anchor.resolve(pose,profile,calibration);
            if(base==null||!anchor.mainHand()||!emptyOffhand)return base;
            var support=anchor.support()!=null?anchor.support():dev.visorcompat.tacz.physical.Handling.support(profile,calibration);
            var q=PoseMath.supportedRotation(base.rotation(),base.hand(),pose.getOffhand().getPosition(),support,base.worldScale(),profile.supportDistance()>0,retention(calibration.zones().support()));
            return new GunPose(base.hand(),q,new Quaternionf(q).transform(calibration.muzzleOffset(profile.muzzleOffset()).mul(base.worldScale())).add(base.hand()),new Quaternionf(q).transform(new Vector3f(0,0,-1)),base.worldScale());
        }
        float scale = pose.getWorldScale();
        var main = pose.getMainHand();
        Vector3f hand = new Vector3f(main.getPosition());
        if (!Float.isFinite(scale) || scale < .25f || scale > 4f || !PoseMath.finite(hand)) return null;
        Quaternionf rotation = main.getRotation().getNormalizedRotation(new Quaternionf());
        if (!Float.isFinite(rotation.x + rotation.y + rotation.z + rotation.w)) return null;
        rotation.normalize();
        hand = calibration.position(hand, rotation, scale);
        rotation = calibration.orientation(rotation);
        if (emptyOffhand) rotation = PoseMath.supportedRotation(rotation, hand,pose.getOffhand().getPosition(),
            dev.visorcompat.tacz.physical.Handling.support(profile,calibration),scale,profile.supportDistance()>0,
            retention(profile.pump()?calibration.zones().rack():calibration.zones().support()));
        Vector3f direction = rotation.transform(new Vector3f(0, 0, -1)).normalize();
        Vector3f muzzle = rotation.transform(calibration.muzzleOffset(profile.muzzleOffset()).mul(scale)).add(hand);
        return PoseMath.finite(muzzle) && PoseMath.finite(direction)
                ? new GunPose(hand, rotation, muzzle, direction, scale) : null;
    }
    private static ZoneSizes.Box retention(ZoneSizes.Box box){return new ZoneSizes.Box(box.width()+.24f,box.height()+.24f,box.depth()+.24f);}
}
