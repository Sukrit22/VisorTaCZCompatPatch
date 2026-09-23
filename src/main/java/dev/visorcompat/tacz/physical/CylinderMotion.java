package dev.visorcompat.tacz.physical;
/** Lateral displacement in head-relative metres; rotation alone never closes a cylinder. */
public final class CylinderMotion {
    private float origin,last,elapsed,peak,stopped;private boolean started,armed;
    public void reset(){started=false;armed=false;elapsed=peak=stopped=0;}
    public void armOpen(){reset();armed=true;}
    public int update(float position,float dt,boolean open){
        if(!Float.isFinite(position+dt)||dt<=0||dt>.15f){reset();return 0;}
        if(!open&&!armed){started=false;return 0;}
        if(!started){origin=last=position;started=true;return 0;}
        elapsed+=dt;float speed=(position-last)/dt;last=position;
        if(elapsed>1.2f){reset();return 0;}
        if(!open){if(position-origin<=-.02f){reset();return 1;}return 0;}
        if(position<origin){origin=position;elapsed=peak=stopped=0;}
        peak=Math.max(peak,speed);
        if(position-origin>=.08f&&peak>=.25f&&Math.abs(speed)<.10f)stopped+=dt;else stopped=0;
        if(stopped>=.075f){reset();return -1;}return 0;
    }
}
