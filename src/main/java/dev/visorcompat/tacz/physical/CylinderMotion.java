package dev.visorcompat.tacz.physical;
/** A deliberate lateral stroke followed by deceleration, measured in head-relative metres. */
public final class CylinderMotion {
    public static final float OPEN_DISTANCE=.02f,CLOSE_DISTANCE=.08f;
    private float origin,last,elapsed,distance,peakSpeed;private boolean started,armed,moving;
    public void reset(){started=false;armed=false;moving=false;elapsed=distance=peakSpeed=0;}
    public void armOpen(){reset();armed=true;}
    public int update(float position,float dt,boolean open){
        if(!Float.isFinite(position)||!Float.isFinite(dt)||dt<=0||dt>.15f){reset();return 0;}
        if(!open&&!armed){reset();return 0;}
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
        if(distance>=CLOSE_DISTANCE&&speed<=Math.max(.12f,peakSpeed*.45f)){reset();return -1;}
        if(speed<.12f){moving=false;elapsed=distance=0;}
        return 0;
    }
}
