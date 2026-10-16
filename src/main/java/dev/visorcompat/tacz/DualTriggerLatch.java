package dev.visorcompat.tacz;
/** Requires a fresh release after acquisition, interruption, or a completed semi-auto shot. */
public final class DualTriggerLatch {
 private boolean armed,pending;
 public void input(boolean held,boolean down){if(!held){reset();return;}if(!down){armed=true;pending=false;}else if(armed)pending=true;}
 public boolean pending(){return pending;}
 public void completed(boolean automatic){if(!automatic)reset();}
 public void reset(){armed=false;pending=false;}
}
