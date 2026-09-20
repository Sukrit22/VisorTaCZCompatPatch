package dev.visorcompat.tacz.physical;

/** Pure manual-bolt ammunition transitions; no live rounds are hidden in addon tags. */
public final class BoltCycle {
    public record Result(boolean open,boolean spent,Chamber chamber,int effect) {}
    public static Result advance(boolean open,boolean spent,Chamber chamber,PumpCycle.Transition transition,
                                 boolean magazinePresent,boolean canFeed) {
        if(transition==PumpCycle.Transition.OPEN && !open)
            return new Result(true,false,new Chamber(chamber.magazine(),false),spent?4:chamber.loaded()?3:2);
        if(transition==PumpCycle.Transition.CLOSE && open)
            return new Result(false,false,magazinePresent&&canFeed?chamber.feed():chamber,5);
        return new Result(open,spent,chamber,-1);
    }
}
