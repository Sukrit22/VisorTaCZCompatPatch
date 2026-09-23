package dev.visorcompat.tacz.physical;

/** A receiver constrained to one rail, not a general rigid-body simulation. */
public record InertialPump(float travel,float speed) {
    public InertialPump step(float handVelocityChange,float gravityAlongRail,float dt){
        if(!Float.isFinite(handVelocityChange+gravityAlongRail+dt)||dt<=0||dt>.1f)return this;
        float v=Math.max(-3,Math.min(3,speed+handVelocityChange-gravityAlongRail*dt));
        v*=Math.exp(-5*dt);
        float next=travel+v*dt;
        if(next<=0)return new InertialPump(0,0);
        if(next>=PumpCycle.TRAVEL)return new InertialPump(PumpCycle.TRAVEL,0);
        return new InertialPump(next,v);
    }
}
