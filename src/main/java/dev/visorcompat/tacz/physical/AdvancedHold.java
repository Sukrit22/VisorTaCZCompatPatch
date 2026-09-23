package dev.visorcompat.tacz.physical;

/** Transient ownership only. Never owns an ItemStack or modifies ammunition. */
public final class AdvancedHold {
    public enum State { HOLSTERED, MAIN, SUPPORT, RELEASED, INSPECT }
    private State state=State.HOLSTERED;
    private long releasedAt;
    private boolean triggerArmed, useArmed=true;
    public State state(){return state;}
    public boolean held(){return state==State.MAIN || state==State.SUPPORT || state==State.INSPECT;}
    public boolean canFire(){return state==State.MAIN && triggerArmed;}
    public void grip(boolean main){state=main?State.MAIN:State.SUPPORT;triggerArmed=false;}
    public void release(long now,boolean inHolster){state=inHolster?State.HOLSTERED:State.RELEASED;releasedAt=now;triggerArmed=false;}
    public boolean inspect(long now,int windowMs,boolean pistol){
        if(!pistol || !useArmed || state!=State.RELEASED || now-releasedAt<0 || now-releasedAt>windowMs*1_000_000L)return false;
        state=State.INSPECT;useArmed=false;triggerArmed=false;return true;
    }
    public void useReleased(){useArmed=true;}
    public boolean useArmed(){return useArmed;}
    public void useConsumed(){useArmed=false;}
    public void triggerReleased(){triggerArmed=true;}
    public void holster(){state=State.HOLSTERED;triggerArmed=false;useArmed=true;}
    public boolean recover(long now,int windowMs,boolean toss,float y,float floor,float height,float distance){
        if(state!=State.RELEASED || now-releasedAt<windowMs*1_000_000L)return false;
        return !toss || y<=floor+height*.25f || distance>3f || now-releasedAt>3_000_000_000L;
    }
}
