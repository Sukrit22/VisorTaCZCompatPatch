package dev.visorcompat.tacz.physical;

/** Two detents, sharing a maximum of two native shots per trigger pull. */
public final class DoubleTrigger {
    private boolean armed;
    private int requested, fired;
    public void reset() { armed=false; requested=0; fired=0; }
    public void sample(float value) {
        if (!Float.isFinite(value)) { reset(); return; }
        if (value < .10f) { armed=true; requested=0; fired=0; return; }
        if (!armed) return;
        if (value >= .90f) requested=2;
        else if (value >= .33f) requested=Math.max(requested,1);
    }
    public boolean pending() { return armed && requested>fired; }
    public void fired() { if(pending()) fired++; }
}
