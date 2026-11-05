package dev.visorcompat.tacz.physical;
/** A deliberate lateral stroke followed by deceleration, measured in room-scale metres. */
public final class CylinderMotion {
    public static final float OPEN_DISTANCE=.02f,CLOSE_DISTANCE=.08f;
    /** A close stroke must reach this wrist speed (room-scale m/s) to count as a real flick.
        Kept below the 0.8 m/s "fast" stroke the tests exercise so a genuine flick isn't
        rejected by float rounding at the boundary, yet still way above idle repositioning
        (~0.3-0.5 m/s) which is the "moved a little and it closed" false positive. */
    public static final float CLOSE_FLICK_SPEED=.7f;
    private float origin,last,elapsed,distance,peakSpeed,armedElapsed;private boolean started,armed,moving;
    public void reset(){started=false;armed=false;moving=false;elapsed=distance=peakSpeed=armedElapsed=0;}
    public void armOpen(){reset();armed=true;}
    public int update(float position,float dt,boolean open){return update(position,dt,open,!open);}
    public int update(float position,float dt,boolean open,boolean requireUse,boolean blocked){
        if(blocked){reset();return 0;}
        return update(position,dt,open,requireUse);
    }
    public int update(float position,float dt,boolean open,boolean requireUse){
        if(!Float.isFinite(position)||!Float.isFinite(dt)||dt<=0||dt>.15f){reset();return 0;}
        if(requireUse){
            if(!armed){reset();return 0;}
            armedElapsed+=dt;if(armedElapsed>1.2f){reset();return 0;}
        }
        if(!started){origin=last=position;started=true;return 0;}
        float delta=position-last,speed=delta/dt;last=position;
        if(!open){
            elapsed+=dt;
            if(elapsed>1.2f){reset();return 0;}
            if(position-origin<=-OPEN_DISTANCE){reset();return 1;}
            return 0;
        }
        // Idle movement never accumulates a minimum/maximum over seconds. Start a fresh
        // short stroke only with outward effort, then require its full eight centimetres.
        if(!moving){
            if(speed>=.30f){moving=true;origin=position-delta;elapsed=dt;distance=delta;peakSpeed=speed;}
            return 0;
        }
        elapsed+=dt;distance=Math.max(distance,position-origin);peakSpeed=Math.max(peakSpeed,speed);
        if(elapsed>.45f){moving=false;elapsed=distance=0;return 0;}
        // Tracking seldom reports a perfectly stopped hand. Keep the completed travel
        // through endpoint jitter, and accept a clear slowdown or reversal immediately.
        // A close only counts when the stroke was a real flick: peak wrist speed must be a
        // deliberate snap, not 8 cm of drift that merely happens to come to rest (which is
        // why a slow "move a little" after flicking the cylinder open would close it).
        if(distance>=CLOSE_DISTANCE&&peakSpeed>=CLOSE_FLICK_SPEED&&speed<=Math.max(.12f,peakSpeed*.45f)){reset();return -1;}
        if(speed<.12f){moving=false;elapsed=distance=0;}
        return 0;
    }
}
