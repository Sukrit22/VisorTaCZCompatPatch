package dev.visorcompat.tacz.physical;

/** Two detached magazines, independent of chamber/native magazine ammunition. -1 means absent. */
public final class TacticalLedger {
    private int grab=-1,pinch=-1;private boolean out;
    public TacticalLedger(int grab,int pinch,boolean out){if(grab< -1||pinch< -1||grab>4096||pinch>4096)throw new IllegalArgumentException("Magazine count");this.grab=grab;this.pinch=pinch;this.out=out;}
    public int rounds(boolean pinched){return pinched?pinch:grab;}
    public boolean out(){return out;}
    public boolean take(boolean pinched,int rounds){if(rounds<0||rounds>4096||rounds(pinched)>=0)return false;if(pinched)pinch=rounds;else grab=rounds;return true;}
    public boolean remove(int rounds){return remove(false,rounds);}
    public boolean remove(boolean pinch,int rounds){if(out||!take(pinch,rounds))return false;out=true;return true;}
    public void eject(){out=true;}
    public int release(boolean pinched){int n=rounds(pinched);if(pinched)pinch=-1;else grab=-1;return Math.max(0,n);}
    public int insert(boolean pinched){if(!out||rounds(pinched)<0)return -1;int n=release(pinched);out=false;return n;}
}
