package dev.visorcompat.tacz.physical;

import org.joml.*;
import dev.visorcompat.tacz.*;

/** Shared gesture geometry, in calibrated gun-local metres. */
public final class Handling {
    private Handling() {}
    public enum Phase { READY, REMOVING, OLD_MAG, NO_MAG, NEW_MAG, LOADING, NEED_RACK, RACKING, SUPPORT, RACKING_EMPTY, PLUCKING, PUMP_HOLD, PUMP_OPEN, SHELL }
    public enum Target { NONE, POUCH, RACK, MAGAZINE, SUPPORT, SELECTOR, CASING }
    public static Target target(Phase phase,Vector3fc local,boolean inPouch,WeaponProfile p,Calibration c) {
        if(p.pump()) {
            if(phase!=Phase.READY && phase!=Phase.NEED_RACK && phase!=Phase.PUMP_OPEN)return Target.NONE;
            if(inPouch)return Target.POUCH;
            var point=rack(p,c);if(phase==Phase.PUMP_OPEN)point.add(0,0,PumpCycle.TRAVEL);
            return near(local,point,.12f)?Target.RACK:Target.NONE;
        }
        if(phase==Phase.NO_MAG && inPouch)return Target.POUCH;
        if(phase==Phase.NO_MAG && near(local,rack(p,c),.105f))return Target.RACK;
        if(phase!=Phase.READY && phase!=Phase.NEED_RACK)return Target.NONE;
        if(near(local,rack(p,c),.105f))return Target.RACK;
        if(near(local,magazine(p,c),.105f))return Target.MAGAZINE;
        if(phase==Phase.READY && p.supportDistance()>0 && near(local,support(p,c),.15f))return Target.SUPPORT;
        if(phase==Phase.READY && near(local,selector(p,c),.04f))return Target.SELECTOR;
        return Target.NONE;
    }
    public static boolean racking(Phase phase) {return phase==Phase.RACKING || phase==Phase.RACKING_EMPTY;}
    public static boolean fireable(Phase phase) { return phase == Phase.PUMP_HOLD || phase == Phase.READY || phase == Phase.SUPPORT || phase == Phase.OLD_MAG
            || phase == Phase.NO_MAG || phase == Phase.NEW_MAG; }
    public static boolean magazineOut(Phase phase) {
        return phase == Phase.OLD_MAG || phase == Phase.NO_MAG || phase == Phase.NEW_MAG || phase == Phase.RACKING_EMPTY;
    }
    public static Vector3f magazine(WeaponProfile p) {
        if(p.pump())return new Vector3f(0,.015f,-.20f);
        return p.supportDistance() == 0 ? new Vector3f(-.00025f,-.116f,.108f) : new Vector3f(0,-.076f,-.136f);
    }
    public static Vector3f rack(WeaponProfile p) {
        if(p.pump())return new Vector3f(0,.02f,-p.supportDistance());
        return p.supportDistance() == 0 ? new Vector3f(0,.15f,.01f) : new Vector3f(0,.105f,.07f);
    }
    public static Vector3f selector(WeaponProfile p) { return new Vector3f(.04f,.03f,0); }
    public static Vector3f magazine(WeaponProfile p,Calibration c){return magazine(p).add(c.interactions().magazine().vector());}
    public static Vector3f rack(WeaponProfile p,Calibration c){return rack(p).add(c.interactions().rack().vector());}
    public static Vector3f selector(WeaponProfile p,Calibration c){return selector(p).add(c.interactions().selector().vector());}
    public static Vector3f support(WeaponProfile p,Calibration c){return new Vector3f(0,p.pump()?.02f:0,-p.supportDistance()).add(c.interactions().support().vector());}
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
