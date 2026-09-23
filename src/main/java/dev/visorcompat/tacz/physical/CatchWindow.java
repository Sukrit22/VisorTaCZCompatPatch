package dev.visorcompat.tacz.physical;
/** One bounded arbitration window. Expiry hands ownership to Visor permanently for this press. */
public final class CatchWindow {
    public enum Result { WAIT, CATCH, HOTBAR, CANCEL }
    private long started;
    private boolean pending;
    public void start(long now){started=now;pending=true;}
    public boolean pending(){return pending;}
    public void cancel(){pending=false;}
    public Result poll(long now,int milliseconds,boolean eligible,boolean near){
        if(!pending)return Result.CANCEL;
        if(!eligible){pending=false;return Result.CANCEL;}
        if(now-started>=milliseconds*1_000_000L){pending=false;return Result.HOTBAR;}
        if(near){pending=false;return Result.CATCH;}
        return Result.WAIT;
    }
}
