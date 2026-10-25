package dev.visorcompat.tacz;
import org.joml.*;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
/** Shared controller transform for each independently owned weapon. */
public final class DualPose {
    private DualPose(){}
    public static ZoneSizes.Box supportRetention(Calibration c){var box=c.zones().support();return new ZoneSizes.Box(box.width()+.24f,box.height()+.24f,box.depth()+.24f);}
    public static boolean supporting(VRPlayerPose pose,WeaponProfile profile,Calibration c,boolean offhand){
        var raw=resolve(pose,profile,c,offhand,false);if(raw==null)return false;
        var other=offhand?pose.getMainHand():pose.getOffhand();
        return PoseMath.supporting(raw.rotation(),raw.hand(),other.getPosition(),dev.visorcompat.tacz.physical.Handling.support(profile,c),pose.getWorldScale(),profile.supportDistance()>0,supportRetention(c));
    }
    public static GunPose resolve(VRPlayerPose pose,WeaponProfile profile,Calibration c,boolean offhand){
        return resolve(pose,profile,c,offhand,false);
    }
    public static GunPose resolve(VRPlayerPose pose,WeaponProfile profile,Calibration c,boolean offhand,boolean otherAvailable){
        if(pose==null||profile==null||c==null||!c.valid())return null;
        float scale=pose.getWorldScale();if(!Float.isFinite(scale)||scale<.25f||scale>4)return null;
        var hand=offhand?pose.getOffhand():pose.getMainHand();
        var raw=new Quaternionf(hand.getRotation().getNormalizedRotation(new Quaternionf()));
        var offset=c.translation();var adjustment=c.rotation();
        if(offhand){offset.x=-offset.x;adjustment.set(adjustment.x,-adjustment.y,-adjustment.z,adjustment.w);}
        var pos=new Quaternionf(raw).transform(offset.mul(scale)).add(hand.getPosition());
        var q=raw.mul(adjustment).normalize();
        if(otherAvailable&&profile.supportDistance()>0){
            var support=dev.visorcompat.tacz.physical.Handling.support(profile,c);
            var other=offhand?pose.getMainHand():pose.getOffhand();
            q=PoseMath.supportedRotation(q,pos,other.getPosition(),support,scale,true,
                supportRetention(c));
        }
        var muzzle=c.muzzleOffset(profile.muzzleOffset());
        muzzle=q.transform(muzzle.mul(scale)).add(pos);
        var dir=q.transform(new Vector3f(0,0,-1));
        return PoseMath.finite(pos)&&PoseMath.finite(muzzle)&&PoseMath.finite(dir)?new GunPose(pos,q,muzzle,dir,scale):null;
    }
}
