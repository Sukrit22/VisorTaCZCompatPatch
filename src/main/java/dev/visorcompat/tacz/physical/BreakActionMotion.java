package dev.visorcompat.tacz.physical;

/** Deliberate angular strokes. An opening flick expires; stale poses never complete it. */
public final class BreakActionMotion {
    private float origin,elapsed;private boolean armed;
    public void reset(){armed=false;elapsed=0;}
    public void arm(float pitch){reset();if(Float.isFinite(pitch)){origin=pitch;armed=true;}}
    public int update(float pitch,float dt,boolean open){
        if(!Float.isFinite(pitch)||!Float.isFinite(dt)||dt<=0||dt>.15f){reset();return 0;}
        if(!armed)return 0;
        elapsed+=dt;if(elapsed>1.2f){reset();return 0;}
        float delta=angle(pitch-origin);
        if((!open&&delta<-.35f)||(open&&delta>.35f)){reset();return open?-1:1;}
        return 0;
    }
    public static float angle(float radians){return (float)Math.atan2(Math.sin(radians),Math.cos(radians));}
    public static boolean rotated(float start,float now,boolean open){float delta=angle(now-start);return Float.isFinite(delta)&&(open?delta>.40f:delta<-.40f);}
}
