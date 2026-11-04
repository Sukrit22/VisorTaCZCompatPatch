package dev.visorcompat.tacz.physical;
/** Require a 4 cm stroke and 2 cm reversal while tilted up, with fresh tracking. */
public final class CylinderShake {
    private boolean started;private float start,peak,elapsed;private int direction;
    public void reset(){started=false;direction=0;elapsed=0;}
    public boolean update(float position,float dt,boolean eligible){
        if(!eligible||!Float.isFinite(position+dt)||dt<=0||dt>.15f){reset();return false;}
        if(!started){start=peak=position;started=true;return false;}
        elapsed+=dt;if(elapsed>.65f){reset();return false;}
        if(direction==0){if(Math.abs(position-start)>=.04f){direction=position>start?1:-1;peak=position;}return false;}
        if((position-peak)*direction>0)peak=position;
        if((peak-position)*direction>=.02f){reset();return true;}return false;
    }
}
