package dev.visorcompat.tacz;
import org.joml.*;

public final class OpticMath {
    private OpticMath() {}
    public static int scopeView(int zoomNumber,int[] views){return views==null||views.length==0?0:java.lang.Math.max(0,views[java.lang.Math.floorMod(zoomNumber,views.length)]-1);}
    /** Keep desktop ADS zoom separate from nonmagnifying sight apertures. */
    public static float lensZoom(float selectedZoom,boolean scopeModel,boolean sightModel,boolean scopeOcular) {
        return lensZoom(selectedZoom,scopeModel,sightModel,scopeOcular,null);
    }
    public static float lensZoom(float selectedZoom,boolean scopeModel,boolean sightModel,boolean scopeOcular,Float opticalOverride) {
        // A combined optic can have a separate, unmagnified backup sight aperture.
        if(scopeModel && sightModel && !scopeOcular)return 1;
        if(opticalOverride!=null && Float.isFinite(opticalOverride) && opticalOverride>=1 && opticalOverride<=16)return opticalOverride;
        // TaCZ red-dot ADS zoom values also describe desktop camera zoom, not optical power.
        if(!scopeModel)return 1;
        return Float.isFinite(selectedZoom)?java.lang.Math.max(1,java.lang.Math.min(16,selectedZoom)):1;
    }
    public static boolean aligned(Vector3fc eye,Vector3fc look,Vector3fc sight,boolean held) {
        return aligned(eye,look,sight,held,ZoneSizes.DEFAULT.sight());
    }
    public static boolean aligned(Vector3fc eye,Vector3fc look,Vector3fc sight,boolean held,ZoneSizes.Box box) {
        if(!PoseMath.finite(new Vector3f(eye)) || !PoseMath.finite(new Vector3f(look)))return false;
        float behind=eye.z()-sight.z();
        float dx=eye.x()-sight.x(),dy=eye.y()-sight.y();
        float extra=held?.02f:0;
        return behind>.015f && behind<(.015f+box.depth()+(held?.1f:0)) && java.lang.Math.abs(dx)<box.width()/2+extra && java.lang.Math.abs(dy)<box.height()/2+extra
            && look.z()<-(held?.9063f:.9659f);
    }
    public static Vector2f project(Vector3fc point,Matrix4fc projection) {
        Vector4f clip=new Vector4f(point,1).mul(projection);
        if(!Float.isFinite(clip.x) || !Float.isFinite(clip.y) || !Float.isFinite(clip.w) || clip.w<=.00001f)return null;
        return new Vector2f(clip.x/clip.w*.5f+.5f,clip.y/clip.w*.5f+.5f);
    }
}
