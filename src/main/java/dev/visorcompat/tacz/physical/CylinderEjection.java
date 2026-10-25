package dev.visorcompat.tacz.physical;
import org.joml.Vector3fc;
/** Room-scale metres. Catching is decided once, before the native ammo count is cleared. */
public final class CylinderEjection {
    /** Physical preserves live ammunition automatically; Advanced requires a free catching hand. */
    public static boolean recoverLive(boolean advanced,boolean emptyHand,boolean inCatchArea){
        return !advanced || emptyHand&&inCatchArea;
    }
    public static boolean canCatch(Vector3fc cylinder,Vector3fc hand,float scale){
        if(!Float.isFinite(scale)||scale<=0)return false;
        float down=(cylinder.y()-hand.y())/scale,dx=(cylinder.x()-hand.x())/scale,dz=(cylinder.z()-hand.z())/scale;
        return Float.isFinite(down+dx+dz)&&down>=.08f&&down<=.45f&&dx*dx+dz*dz<=.12f*.12f;
    }
    /** The server-selected catch plane ends only the visual, after it falls to the hand. */
    public static boolean reachedCatch(float originY,float velocityY,float scale,float seconds,boolean gentle,float catchY){
        if(!Float.isFinite(originY+velocityY+scale+seconds+catchY)||scale<=0||seconds<0)return false;
        float y=originY+velocityY*seconds-drop(seconds,gentle)*scale;
        return y<=catchY&&velocityY-(drop(seconds+.001f,gentle)-drop(seconds,gentle))*scale/.001f<0;
    }
    public static float pullSpeed(float delta,float seconds){return !Float.isFinite(delta+seconds)||seconds<=0||seconds>.2f?0:Math.max(0,Math.min(6,delta/seconds));}
    /** Gravity eases in for 180 ms, then reaches normal gravitational acceleration. */
    public static float drop(float seconds,boolean gentle){
        float t=Math.max(0,seconds),r=.18f,g=9.81f;
        if(!gentle)return .5f*g*t*t;
        if(t<r)return g*t*t*t/(6*r);
        float after=t-r;return g*r*r/6+g*r*.5f*after+.5f*g*after*after;
    }
}
