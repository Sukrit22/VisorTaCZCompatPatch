package dev.visorcompat.tacz.physical;
/** Pure pump stroke/feeding rules; distance is gun-local, independent of frame rate. */
public final class PumpCycle {
    public enum Transition { NONE, OPEN, CLOSE }
    public static Transition transition(boolean open,float travel,float side,float vertical){
        return open?(forward(travel,side,vertical)?Transition.CLOSE:Transition.NONE):(rear(travel,side,vertical)?Transition.OPEN:Transition.NONE);
    }
    public static final float TRAVEL=.08f, CLOSED=.018f;
    public static boolean rear(float travel,float side,float vertical){return Float.isFinite(travel)&&Float.isFinite(side)&&Float.isFinite(vertical)&&travel>=TRAVEL&&Math.abs(side)<.13f&&Math.abs(vertical)<.13f;}
    public static boolean forward(float travel,float side,float vertical){return Float.isFinite(travel)&&Float.isFinite(side)&&Float.isFinite(vertical)&&travel<=CLOSED&&travel>=-.04f&&Math.abs(side)<.13f&&Math.abs(vertical)<.13f;}
    public static Chamber close(int tube,boolean chamber){return new Chamber(tube,chamber).feed();}
    public static boolean canInsert(int tube,int capacity,int consumed){return tube>=0&&tube<capacity&&consumed==1;}
}
