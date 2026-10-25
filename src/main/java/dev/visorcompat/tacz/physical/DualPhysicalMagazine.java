package dev.visorcompat.tacz.physical;

/** Magazine reservation owns only the removed rounds; never another weapon's inventory. */
public final class DualPhysicalMagazine {
    private int reserved=-1;
    public DualPhysicalMagazine(){}
    public DualPhysicalMagazine(int saved){reserved=Math.max(-1,saved);}
    public static boolean supported(String id){return "tacz:glock_17".equals(id);}
    public boolean removed(){return reserved>=0;}
    public int reserved(){return reserved;}
    public Chamber remove(Chamber chamber){if(removed())return chamber;reserved=chamber.magazine();return new Chamber(0,chamber.loaded());}
    public Chamber restore(Chamber chamber){if(!removed())return chamber;var restored=new Chamber(chamber.magazine()+reserved,chamber.loaded());reserved=-1;return restored;}
    public static boolean free(boolean gunHeld,boolean otherHeld,boolean otherAllowed){return gunHeld&&!otherHeld&&otherAllowed;}
    public static boolean canFire(boolean manipulating,boolean locked,boolean jammed,Chamber chamber){return !manipulating&&!locked&&!jammed&&chamber.loaded();}
}
