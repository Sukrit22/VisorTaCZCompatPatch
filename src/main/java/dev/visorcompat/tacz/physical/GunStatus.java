package dev.visorcompat.tacz.physical;

/** Status priority shared by every panel; not a replacement for TaCZ fire checks. */
public final class GunStatus {
    private GunStatus() {}
    public static String describe(boolean jam,boolean hot,boolean reload,boolean physical,
                                  Handling.Phase phase,boolean chamber,int magazine) {
        if(jam)return "JAMMED";
        if(hot)return "OVERHEATED";
        if(reload || physical && phase==Handling.Phase.LOADING)return "RELOADING";
        if(physical && Handling.racking(phase))return "ACTION OPEN";
        if(physical && (phase==Handling.Phase.OLD_MAG || phase==Handling.Phase.NO_MAG || phase==Handling.Phase.NEW_MAG))
            return chamber?"NO MAG | CHAMBER LOADED":"NO MAGAZINE";
        if(!chamber && magazine<=0)return "EMPTY";
        if(physical && !chamber)return "EMPTY CHAMBER";
        return "";
    }
}
