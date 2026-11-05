package dev.visorcompat.tacz.physical;

/** Raw room metres: deliberately contains no head/player/world position or world yaw. */
public record RoomFlickSample(float mx,float my,float mz,float ox,float oy,float oz,float rx,float rz) {
    public boolean valid(){
        return Float.isFinite(mx)&&Float.isFinite(my)&&Float.isFinite(mz)
            &&Float.isFinite(ox)&&Float.isFinite(oy)&&Float.isFinite(oz)
            &&Float.isFinite(rx)&&Float.isFinite(rz)&&rx*rx+rz*rz>.9f&&rx*rx+rz*rz<1.1f
            &&Math.max(Math.max(Math.abs(mx),Math.abs(my)),Math.abs(mz))<100
            &&Math.max(Math.max(Math.abs(ox),Math.abs(oy)),Math.abs(oz))<100;
    }
    public float lateral(boolean offhand,float axisX,float axisZ){
        return (offhand?ox:mx)*axisX+(offhand?oz:mz)*axisZ;
    }
    public float distanceSquared(RoomFlickSample other,boolean offhand){
        float x=offhand?ox-other.ox:mx-other.mx,y=offhand?oy-other.oy:my-other.my,z=offhand?oz-other.oz:mz-other.mz;
        return x*x+y*y+z*z;
    }
}
