package dev.visorcompat.tacz.physical;
/** Native ammo is authoritative; spent cases only occupy experimental loading slots. */
public final class CylinderCycle {
    public static boolean fallsOut(float upward){return Float.isFinite(upward)&&upward>.65f;}
    public static boolean toggle(float side,float up,float back){return Float.isFinite(side+up+back)&&Math.abs(side)>=.05f&&Math.abs(side)<=.30f&&Math.abs(up)<.12f&&Math.abs(back)<.12f;}
    public static boolean canInsert(boolean open,int live,int spent,int capacity){return open&&live>=0&&spent>=0&&live+spent<capacity;}
    public static int loadCount(boolean open,int live,int spent,int capacity,boolean loader){return !canInsert(open,live,spent,capacity)?0:loader?Math.min(64,capacity-live-spent):1;}
    public static int spent(int saved,int live,int capacity){return Math.max(0,Math.min(saved,Math.max(0,capacity-live)));}
}
