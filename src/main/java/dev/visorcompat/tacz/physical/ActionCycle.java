package dev.visorcompat.tacz.physical;
/** Position gestures measured from a fixed gun-local action anchor, in metres. */
public final class ActionCycle {
    public enum Bolt { LOWERED, LIFTED, OPEN }
    public static Bolt bolt(Bolt state,float back,float up,float side){
        if(!Float.isFinite(back+up+side)||Math.abs(side)>.13f)return state;
        return switch(state){
            case LOWERED -> up>=.035f && Math.abs(back)<.04f?Bolt.LIFTED:state;
            case LIFTED -> back>=.08f && up>=.02f?Bolt.OPEN:
                back>=-.06f && back<=.035f && up<=.02f && up>=-.10f?Bolt.LOWERED:state;
            case OPEN -> back>=-.06f && back<=.025f && up>=-.10f && up<=.14f?Bolt.LIFTED:state;
        };
    }
    public static PumpCycle.Transition rack(boolean rearReached,float back,float side,float up){
        if(!Float.isFinite(back+side+up)||Math.abs(side)>.12f||Math.abs(up)>.12f)return PumpCycle.Transition.NONE;
        if(!rearReached&&back>=.055f)return PumpCycle.Transition.OPEN;
        if(rearReached&&back<=.018f&&back>=-.04f)return PumpCycle.Transition.CLOSE;
        return PumpCycle.Transition.NONE;
    }
    public static boolean latch(float back,float up,float side){return Float.isFinite(back+up+side)&&back>=.055f&&up>=.035f&&Math.abs(side)<.12f;}
    public static boolean slap(float previousY,float nowY){return Float.isFinite(previousY+nowY)&&previousY-nowY>=.025f;}
    public static boolean releaseSweep(org.joml.Vector3fc from,org.joml.Vector3fc to,org.joml.Vector3fc center,dev.visorcompat.tacz.ZoneSizes.Box box){
        if(!dev.visorcompat.tacz.PoseMath.finite(from)||!dev.visorcompat.tacz.PoseMath.finite(to)||from.distance(to)>.5f)return false;
        boolean forward=from.z()-to.z()>=.015f || from.z()>=center.z()&&to.z()<center.z()&&from.z()-to.z()>.001f;
        boolean down=from.y()-to.y()>=.015f || from.y()>=center.y()&&to.y()<center.y()&&from.y()-to.y()>.001f;
        if(!forward&&!down)return false;
        float low=0,high=1;
        float[] a={from.x()-center.x(),from.y()-center.y(),from.z()-center.z()};
        float[] d={to.x()-from.x(),to.y()-from.y(),to.z()-from.z()};
        float[] half={box.width()/2,box.height()/2,box.depth()/2};
        for(int i=0;i<3;i++){
            if(Math.abs(d[i])<1e-6f){if(Math.abs(a[i])>half[i])return false;}
            else {float x=(-half[i]-a[i])/d[i],y=(half[i]-a[i])/d[i];low=Math.max(low,Math.min(x,y));high=Math.min(high,Math.max(x,y));if(low>high)return false;}
        }
        return true;
    }
    public static boolean boltSlap(org.joml.Vector3fc from,org.joml.Vector3fc to,org.joml.Vector3fc rack,dev.visorcompat.tacz.ZoneSizes.Box box){
        return from.y()-to.y()>.004f && releaseSweep(from,to,new org.joml.Vector3f(rack).add(0,.035f,0),box);
    }
    public static boolean regrip(boolean actionBlocked,boolean held,float distance){
        return !actionBlocked&&!held&&Float.isFinite(distance)&&distance<=.085f;
    }
    public static Chamber release(Chamber chamber,boolean latched,boolean magazine,boolean jammed){return latched&&magazine&&!jammed?chamber.feed():chamber;}
    public static boolean portLoad(boolean open,boolean loaded,int consumed){return open&&!loaded&&consumed==1;}
}
