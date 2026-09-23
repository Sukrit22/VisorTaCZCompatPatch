package dev.visorcompat.tacz.physical;
/** Hysteresis prevents repeated haptic pulses when hovering at the grip boundary. */
public final class RegripZone {
    public static final float ENTER=.04f,EXIT=.055f;
    private boolean inside;
    public boolean update(boolean ready,float distance){
        if(!ready||!Float.isFinite(distance)||distance>EXIT){inside=false;return false;}
        if(!inside&&distance<=ENTER){inside=true;return true;}
        return false;
    }
    public void reset(){inside=false;}
}
