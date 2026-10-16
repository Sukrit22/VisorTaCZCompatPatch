package dev.visorcompat.tacz;
/** Native action transitions: 0 silent, 1 reload, 2 complete, 3 cancel, 4 bolt. */
public final class DualActionFeedback {
 private DualActionFeedback(){}
 public static int cue(int previous,int action,boolean finishing,boolean held){
  if(action==previous)return 0;
  if(action==1)return 1;
  if(action==2)return 4;
  return held&&(previous==2||finishing)?2:3;
 }
}
