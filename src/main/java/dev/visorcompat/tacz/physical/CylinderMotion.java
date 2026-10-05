package dev.visorcompat.tacz.physical;
/** A deliberate lateral stroke followed by a stop, measured in head-relative metres. */
public final class CylinderMotion {
    private float origin,last,elapsed,distance;private boolean started,armed,moving;
    public void reset(){started=false;armed=false;moving=false;elapsed=distance=0;}
    public void armOpen(){reset();armed=true;}
    public int update(float position,float dt,boolean open){
        if(!Float.isFinite(position)||!Float.isFinite(dt)||dt<=0||dt>.15f){reset();return 0;}
        if(!open&&!armed){reset();return 0;}
        if(!started){origin=last=position;started=true;return 0;}
        float delta=position-last,speed=delta/dt;last=position;
        if(!open){
            elapsed+=dt;
            if(elapsed>1.2f){reset();return 0;}
            if(position-origin<=-.02f){reset();return 1;}
            return 0;
        }
        // Idle movement never accumulates a minimum/maximum over seconds. Start a fresh
        // short stroke only with outward effort, then require its full eight centimetres.
        if(!moving){
            if(speed>=.30f){moving=true;origin=position-delta;elapsed=dt;distance=delta;}
            return 0;
        }
        elapsed+=dt;distance=position-origin;
        if(elapsed>.45f||delta<-.012f){moving=false;elapsed=distance=0;return 0;}
        // One fresh stopped pose is sufficient: extra dwell added perceptible server delay.
        if(Math.abs(speed)<.12f){
            boolean close=distance>=.08f;
            moving=false;elapsed=distance=0;
            if(close){reset();return -1;}
        }
        return 0;
    }
}
