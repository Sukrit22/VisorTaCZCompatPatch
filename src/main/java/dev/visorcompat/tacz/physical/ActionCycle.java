package dev.visorcompat.tacz.physical;
/** Position gestures measured from a fixed gun-local action anchor, in metres. */
public final class ActionCycle {
    public enum Bolt { LOWERED, LIFTED, OPEN }
    public static Bolt bolt(Bolt state,float back,float up,float side){
        if(!Float.isFinite(back+up+side)||Math.abs(side)>.13f)return state;
        return switch(state){
            case LOWERED -> up>=.035f && Math.abs(back)<.04f?Bolt.LIFTED:state;
            case LIFTED -> back>=.08f && up>=.02f?Bolt.OPEN:
                Math.abs(back)<.025f && up<=.012f && up>=-.04f?Bolt.LOWERED:state;
            case OPEN -> Math.abs(back)<.018f && up>=.02f?Bolt.LIFTED:state;
        };
    }
    public static boolean latch(float back,float up,float side){return Float.isFinite(back+up+side)&&back>=.055f&&up>=.035f&&Math.abs(side)<.12f;}
    public static boolean slap(float previousY,float nowY){return Float.isFinite(previousY+nowY)&&previousY-nowY>=.025f;}
    public static Chamber release(Chamber chamber,boolean latched,boolean magazine,boolean jammed){return latched&&magazine&&!jammed?chamber.feed():chamber;}
    public static boolean portLoad(boolean open,boolean loaded,int consumed){return open&&!loaded&&consumed==1;}
}
