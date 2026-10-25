package dev.visorcompat.tacz.server;

/** Server-tick charge accumulator; every physical hand owns a separate instance. */
public final class DualCharge {
    public enum Mode { AUTO, HOLD, DELAY }
    public record Settings(Mode mode,float increase,float decrease,float onFire,float maximum,float threshold) {}
    private float progress;
    public float progress(){return progress;}
    public void reset(){progress=0;}
    public boolean tick(Settings s,boolean requested,boolean down,boolean canCharge,boolean canShoot){
        if(s.mode()==Mode.HOLD){
            if(requested&&down&&canCharge)progress=Math.min(s.maximum(),progress+s.increase());
            else if(!down&&canShoot&&progress>=s.threshold())return true;
            else progress=Math.max(0,progress-s.decrease());
            return false;
        }
        boolean charging=s.mode()==Mode.DELAY?(requested||progress>0):requested&&down;
        if(charging&&canCharge){progress=Math.min(s.maximum(),progress+s.increase());return canShoot&&progress>=s.maximum();}
        progress=Math.max(0,progress-s.decrease());return false;
    }
    public void fired(Settings s){progress=s.mode()==Mode.DELAY?0:Math.max(0,progress-s.onFire());}
}
