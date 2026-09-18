package dev.visorcompat.tacz;
import org.joml.*;

public final class OpticMath {
    private OpticMath() {}
    public static boolean aligned(Vector3fc eye,Vector3fc look,Vector3fc sight,boolean held) {
        if(!PoseMath.finite(new Vector3f(eye)) || !PoseMath.finite(new Vector3f(look)))return false;
        float behind=eye.z()-sight.z();
        float dx=eye.x()-sight.x(),dy=eye.y()-sight.y();
        float radius=held?.055f:.035f;
        return behind>.015f && behind<(held?.85f:.75f) && dx*dx+dy*dy<radius*radius
            && look.z()<-(held?.9063f:.9659f);
    }
    public static Vector2f project(Vector3fc point,Matrix4fc projection) {
        Vector4f clip=new Vector4f(point,1).mul(projection);
        if(!Float.isFinite(clip.x) || !Float.isFinite(clip.y) || !Float.isFinite(clip.w) || clip.w<=.00001f)return null;
        return new Vector2f(clip.x/clip.w*.5f+.5f,clip.y/clip.w*.5f+.5f);
    }
}
