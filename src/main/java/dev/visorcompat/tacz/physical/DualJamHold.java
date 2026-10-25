package dev.visorcompat.tacz.physical;

/** One hand's server-owned hold, bound to an exact inventory object until release. */
public final class DualJamHold<T> {
    private T stack;
    private ButtonJamHold timer;
    private boolean finished;
    public void release(){stack=null;timer=null;finished=false;}
    public void input(T current,boolean down,boolean eligible,long tick){
        if(!down){release();return;}
        if(finished)return;
        if(!eligible||(stack!=null&&stack!=current)){timer=null;finished=true;return;}
        if(timer==null){stack=current;timer=new ButtonJamHold(tick);}
        timer.refresh(tick);
    }
    public boolean active(){return timer!=null&&!finished;}
    public boolean valid(T current,boolean eligible,long tick){return active()&&stack==current&&eligible&&timer.alive(tick);}
    public int elapsed(long tick){return active()?timer.elapsed(tick):0;}
    public boolean complete(long tick){return active()&&timer.complete(tick);}
    public void finish(){timer=null;finished=true;}
}
