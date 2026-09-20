package dev.visorcompat.tacz;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Shared, Minecraft-independent geometry for rendering and authoritative shots. */
public final class PoseMath {
    private PoseMath() {}
    public static boolean finite(Vector3fc v) {
        return Float.isFinite(v.x()) && Float.isFinite(v.y()) && Float.isFinite(v.z());
    }
    public static float pitch(Vector3fc direction) {
        return (float) Math.toDegrees(-Math.asin(Math.max(-1, Math.min(1, direction.y()))));
    }
    public static float yaw(Vector3fc direction) {
        return (float) Math.toDegrees(Math.atan2(-direction.x(), direction.z()));
    }
    public static Quaternionf supportedRotation(Quaternionf main, Vector3fc hand, Vector3fc offhand,
                                               float distance, float scale) {
        return supportedRotation(main,hand,offhand,new Vector3f(0,0,-distance),scale,distance>0);
    }
    public static Quaternionf supportedRotation(Quaternionf main,Vector3fc hand,Vector3fc offhand,
                                                Vector3fc support,float scale,boolean enabled) {
        return supportedRotation(main,hand,offhand,support,scale,enabled,null);
    }
    public static Quaternionf supportedRotation(Quaternionf main,Vector3fc hand,Vector3fc offhand,
                                                Vector3fc support,float scale,boolean enabled,ZoneSizes.Box zone) {
        Quaternionf result = new Quaternionf(main);
        if (!enabled || !finite(offhand) || !finite(support) || support.lengthSquared()<.0025f) return result;
        Vector3f local=new Vector3f(support);
        Vector3f expected=result.transform(new Vector3f(local)).mul(scale).add(hand);
        Vector3f forward=result.transform(local.normalize());
        Vector3f delta = new Vector3f(offhand).sub(hand);
        // Snap only near the foregrip, with a meaningful hand separation and modest angle.
        var handLocal=new Quaternionf(main).conjugate().transform(new Vector3f(offhand).sub(hand)).div(scale);
        if ((zone==null?expected.distance(offhand) > .12f * scale:!zone.contains(handLocal,support)) || delta.length() < .15f * scale) return result;
        delta.normalize();
        if (forward.dot(delta) < .8f) return result;
        return new Quaternionf().rotationTo(forward, delta).mul(result).normalize();
    }
}
