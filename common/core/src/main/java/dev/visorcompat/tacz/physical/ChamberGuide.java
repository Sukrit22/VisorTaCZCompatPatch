package dev.visorcompat.tacz.physical;

/** Cosmetic equilibrium along the action axis. No cartridge ledger or time integration. */
public final class ChamberGuide {
    private ChamberGuide() {}
    /** Rearward travel in metres, bounded by the opening and a short chamber guide. */
    public static float displacement(float opening, float rearwardGravity) {
        if (!Float.isFinite(opening) || !Float.isFinite(rearwardGravity)) return 0;
        float bound=Math.min(.012f,Math.max(0,opening));
        return bound*Math.max(0,Math.min(1,rearwardGravity));
    }
}
