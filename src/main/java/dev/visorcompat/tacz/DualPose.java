package dev.visorcompat.tacz;
import org.joml.*;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
/** Shared controller transform for each independently owned weapon. */
public final class DualPose {
    private DualPose(){}
    public static GunPose resolve(VRPlayerPose pose,WeaponProfile profile,Calibration c,boolean offhand){
        if(pose==null||profile==null||c==null||!c.valid())return null;
        float scale=pose.getWorldScale();if(!Float.isFinite(scale)||scale<.25f||scale>4)return null;
        var hand=offhand?pose.getOffhand():pose.getMainHand();
        var raw=new Quaternionf(hand.getRotation().getNormalizedRotation(new Quaternionf()));
        var offset=c.translation();var adjustment=c.rotation();
        if(offhand){offset.x=-offset.x;adjustment.set(adjustment.x,-adjustment.y,-adjustment.z,adjustment.w);}
        var pos=new Quaternionf(raw).transform(offset.mul(scale)).add(hand.getPosition());
        var q=raw.mul(adjustment).normalize();var muzzle=c.muzzleOffset(profile.muzzleOffset());if(offhand)muzzle.x=-muzzle.x;
        muzzle=q.transform(muzzle.mul(scale)).add(pos);
        var dir=q.transform(new Vector3f(0,0,-1));
        return PoseMath.finite(pos)&&PoseMath.finite(muzzle)&&PoseMath.finite(dir)?new GunPose(pos,q,muzzle,dir,scale):null;
    }
}
