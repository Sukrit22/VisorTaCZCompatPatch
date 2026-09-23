package dev.visorcompat.tacz.physical;

import org.joml.*;
import dev.visorcompat.tacz.*;

/** Shared gesture geometry, in calibrated gun-local metres. */
public final class Handling {
    private Handling() {}
    public enum Phase { READY, REMOVING, OLD_MAG, NO_MAG, NEW_MAG, LOADING, NEED_RACK, RACKING, SUPPORT, RACKING_EMPTY, PLUCKING, PUMP_HOLD, PUMP_OPEN, SHELL, CYLINDER_OPEN, CYLINDER_HOLD, LOADER }
    public enum Target { NONE, POUCH, RACK, MAGAZINE, SUPPORT, SELECTOR, CASING, RELEASE }
    public static Target target(Phase phase,Vector3fc local,boolean inPouch,WeaponProfile p,Calibration c) {
        return target(phase,local,inPouch,p,c,false);
    }
    public static Target target(Phase phase,Vector3fc local,boolean inPouch,WeaponProfile p,Calibration c,boolean boltOpen) {
        if(p.cylinder()) {
            if(phase!=Phase.READY&&phase!=Phase.CYLINDER_OPEN)return Target.NONE;
            if(phase==Phase.CYLINDER_OPEN&&inPouch)return Target.POUCH;
            var rack=rack(p,c);var port=JamProfile.of(p,c).port();
            if(phase==Phase.CYLINDER_OPEN&&inside(local,port,c,ZoneSizes.Zone.PORT)
                && (!inside(local,rack,c,ZoneSizes.Zone.RACK)||local.distance(port)<local.distance(rack)))return Target.CASING;
            return inside(local,rack,c,ZoneSizes.Zone.RACK)?Target.RACK:Target.NONE;
        }
        if(p.pump()) {
            if(phase!=Phase.READY && phase!=Phase.NEED_RACK && phase!=Phase.PUMP_OPEN)return Target.NONE;
            if(inPouch)return Target.POUCH;
            var point=rack(p,c);if(phase==Phase.PUMP_OPEN)point.add(0,0,PumpCycle.TRAVEL);
            return inside(local,point,c,ZoneSizes.Zone.RACK)?Target.RACK:Target.NONE;
        }
        if(phase==Phase.NO_MAG && inPouch)return Target.POUCH;
        if(phase==Phase.NO_MAG && inside(local,rack(p,c,boltOpen),c,ZoneSizes.Zone.RACK))return Target.RACK;
        if(phase!=Phase.READY && phase!=Phase.NEED_RACK)return Target.NONE;
        var support=support(p,c);
        var select=selector(p,c);
        if(PistolProfiles.get(p)!=null&&p.selector()&&inside(local,select,c,ZoneSizes.Zone.SELECTOR)
            &&local.distance(select)<local.distance(support)&&local.distance(select)<local.distance(rack(p,c,boltOpen))&&local.distance(select)<local.distance(magazine(p,c)))return Target.SELECTOR;
        if(inside(local,support,c,ZoneSizes.Zone.SUPPORT) && local.distance(support)<local.distance(rack(p,c,boltOpen)) && local.distance(support)<local.distance(magazine(p,c)))return Target.SUPPORT;
        if(inside(local,rack(p,c,boltOpen),c,ZoneSizes.Zone.RACK))return Target.RACK;
        if(inside(local,magazine(p,c),c,ZoneSizes.Zone.MAGAZINE))return Target.MAGAZINE;
        if((phase==Phase.READY || phase==Phase.NEED_RACK) && inside(local,support(p,c),c,ZoneSizes.Zone.SUPPORT))return Target.SUPPORT;
        if(phase==Phase.READY && p.selector() && inside(local,selector(p,c),c,ZoneSizes.Zone.SELECTOR))return Target.SELECTOR;
        return Target.NONE;
    }
    public static Target target(Phase phase,Vector3fc local,boolean pouch,WeaponProfile p,Calibration c,boolean open,boolean locked) {
        if(locked && !p.manualAction() && (phase==Phase.READY || phase==Phase.NEED_RACK || phase==Phase.NO_MAG) && inside(local,release(p,c),c,ZoneSizes.Zone.RELEASE))return Target.RELEASE;
        return target(phase,local,pouch,p,c,open);
    }
    public static Target target(Phase phase,Vector3fc local,boolean pouch,WeaponProfile p,Calibration c,boolean open,boolean locked,boolean lifted){
        if(p.bolt() && lifted && (phase==Phase.READY||phase==Phase.NEED_RACK||phase==Phase.NO_MAG)){
            if(inside(local,rack(p,c,open).add(0,.035f,0),c,ZoneSizes.Zone.RACK))return Target.RACK;
            var result=target(phase,local,pouch,p,c,open,locked);return result==Target.RACK?Target.NONE:result;
        }
        return target(phase,local,pouch,p,c,open,locked);
    }
    /** Extra acquisition room only for the free hand working an anchored sniper bolt. */
    public static boolean boltGrab(Vector3fc local,WeaponProfile profile,Calibration c,boolean open,boolean lifted){
        if(!profile.bolt())return false;
        var center=rack(profile,c,open);if(lifted)center.add(0,.035f,0);
        var box=c.zones().rack();return new ZoneSizes.Box(box.width()+.10f,box.height()+.10f,box.depth()+.10f).contains(local,center);
    }
    public static boolean racking(Phase phase) {return phase==Phase.RACKING || phase==Phase.RACKING_EMPTY;}
    public static boolean fireable(Phase phase) { return phase == Phase.PUMP_HOLD || phase == Phase.READY || phase == Phase.SUPPORT || phase == Phase.OLD_MAG
            || phase == Phase.NO_MAG || phase == Phase.NEW_MAG; }
    public static boolean magazineOut(Phase phase) {
        return phase == Phase.OLD_MAG || phase == Phase.NO_MAG || phase == Phase.NEW_MAG || phase == Phase.RACKING_EMPTY;
    }
    private static Vector3f point(WeaponProfile p,String name){var v=DescriptorProfiles.zone(p,name);return v==null?new Vector3f():v;}
    public static Vector3f magazine(WeaponProfile p){return point(p,"magazine");}
    public static Vector3f rack(WeaponProfile p){return point(p,"rack");}
    public static Vector3f release(WeaponProfile p,Calibration c){return point(p,"release").add(c.interactions().release().vector()).mul(c.gunScale());}
    public static Vector3f selector(WeaponProfile p){return point(p,"selector");}
    public static Vector3f magazine(WeaponProfile p,Calibration c){return magazine(p).add(c.interactions().magazine().vector()).mul(c.gunScale());}
    public static Vector3f rack(WeaponProfile p,Calibration c){return rack(p).add(c.interactions().rack().vector()).mul(c.gunScale());}
    public static Vector3f rack(WeaponProfile p,Calibration c,boolean open){return rack(p,c).add(0,0,p.bolt()&&open?PumpCycle.TRAVEL:0);}
    public static Vector3f selector(WeaponProfile p,Calibration c){return selector(p).add(c.interactions().selector().vector()).mul(c.gunScale());}
    public static Vector3f support(WeaponProfile p,Calibration c){if(p.pump())return rack(p,c);return point(p,"support").add(c.interactions().support().vector()).mul(c.gunScale());}
    public static Vector3f pouch(Vector3fc head,Vector3fc forward,float scale,Calibration c) {
        Vector3f flat=new Vector3f(forward.x(),0,forward.z());
        if(flat.lengthSquared()<.001f)flat.set(0,0,-1);else flat.normalize();
        var offset=c.interactions().pouch();
        return pouch(head,forward,scale).add(new Vector3f(-flat.z,0,flat.x).mul(offset.x()*scale))
            .add(0,offset.y()*scale,0).sub(flat.mul(offset.z()*scale));
    }
    public static Vector3f local(GunPose gun, Vector3fc world) {
        return new Quaternionf(gun.rotation()).conjugate().transform(new Vector3f(world).sub(gun.hand())).div(gun.worldScale());
    }
    public static boolean inside(Vector3fc point,Vector3fc center,Calibration c,ZoneSizes.Zone zone) {
        return c.zones().get(zone).contains(point,center);
    }
    /** Acquire at the calibrated box; retain with 12 cm of extra room on each side. */
    public static boolean supportRetained(Vector3fc point,Vector3fc center,Calibration c) {
        var box=c.zones().support();
        return new ZoneSizes.Box(box.width()+.24f,box.height()+.24f,box.depth()+.24f).contains(point,center);
    }
    public static boolean inPouch(Vector3fc off,Vector3fc head,Vector3fc forward,float scale,Calibration c) {
        return c.zones().pouch().contains(pouchLocal(off,head,forward,scale,c),new Vector3f());
    }
    public static Vector3f pouchLocal(Vector3fc off,Vector3fc head,Vector3fc forward,float scale,Calibration c) {
        var flat=new Vector3f(forward.x(),0,forward.z());if(flat.lengthSquared()<.001f)flat.set(0,0,-1);else flat.normalize();
        var d=new Vector3f(off).sub(pouch(head,forward,scale,c)).div(scale);
        return new Vector3f(d.dot(new Vector3f(-flat.z,0,flat.x)),d.y,-d.dot(flat));
    }
    public static boolean near(Vector3fc a, Vector3fc b, float radius) {
        return PoseMath.finite(new Vector3f(a)) && a.distance(b) <= radius;
    }
    public static boolean magazinePulled(Vector3fc start, Vector3fc now) {
        return start.y()-now.y() >= .065f && java.lang.Math.abs(start.x()-now.x()) < .16f
            && java.lang.Math.abs(start.z()-now.z()) < .16f;
    }
    public static boolean racked(Vector3fc start, Vector3fc now) {
        return now.z()-start.z() >= .055f && java.lang.Math.abs(start.x()-now.x()) < .12f
            && java.lang.Math.abs(start.y()-now.y()) < .12f;
    }
    public static Vector3f pouch(Vector3fc head, Vector3fc forward, float scale) {
        Vector3f flat=new Vector3f(forward.x(),0,forward.z());
        if(flat.lengthSquared()<.001f) flat.set(0,0,-1); else flat.normalize();
        return new Vector3f(head).add(flat.mul(.18f*scale)).add(0,-.65f*scale,0);
    }
}
