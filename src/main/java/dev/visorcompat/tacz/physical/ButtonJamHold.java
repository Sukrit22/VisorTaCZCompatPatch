package dev.visorcompat.tacz.physical;

/** Server-tick hold timer. Heartbeats keep a lost release from completing a jam clear. */
public final class ButtonJamHold {
    public static final int DURATION=40, LEASE=8;
    private final long started;
    private long refreshed;
    public ButtonJamHold(long tick){started=refreshed=tick;}
    public void refresh(long tick){refreshed=tick;}
    public boolean alive(long tick){return tick-refreshed<=LEASE;}
    public int elapsed(long tick){return (int)Math.max(0,Math.min(DURATION,tick-started));}
    public boolean complete(long tick){return alive(tick)&&elapsed(tick)>=DURATION;}
}
