package dev.visorcompat.tacz;

import java.util.Arrays;

/** Resolves inventory slots without changing inventory or depending on a game client. */
public final class HolsterAssignments {
    public enum Mount { RIGHT, LEFT, CHEST, BACK }
    public enum Kind { NONE, HANDGUN, LONG_GUN }
    private HolsterAssignments(){}

    /** Result order is RIGHT, LEFT, CHEST, BACK; -1 denotes an empty mount. */
    public static int[] resolve(Kind[] hotbar,boolean descending,int[] manualBindings){
        return resolvePinned(hotbar,descending,manualBindings,null);
    }
    /** Held slots retain their mount before any unreserved slots are assigned. */
    public static int[] resolvePinned(Kind[] hotbar,boolean descending,int[] manualBindings,int[] pinned){
        int[] result=new int[Mount.values().length];Arrays.fill(result,-1);
        if(hotbar==null)return result;
        boolean[] used=new boolean[hotbar.length];
        if(pinned!=null){
            for(int mount=0;mount<result.length&&mount<pinned.length;mount++){
                int slot=pinned[mount];
                if(slot>=0&&slot<hotbar.length&&!used[slot]&&hotbar[slot]==kind(mount)){
                    result[mount]=slot;used[slot]=true;
                }
            }
        }
        if(manualBindings!=null){
            for(int mount=0;mount<result.length&&mount<manualBindings.length;mount++){
                if(result[mount]>=0)continue;
                int slot=manualBindings[mount];
                if(slot>=0&&slot<hotbar.length&&!used[slot]&&hotbar[slot]==kind(mount)){
                    result[mount]=slot;used[slot]=true;
                }
            }
            return result;
        }
        for(int step=0;step<hotbar.length;step++){
            int slot=descending?hotbar.length-1-step:step;
            if(used[slot])continue;
            for(int mount=0;mount<result.length;mount++){
                if(result[mount]<0&&hotbar[slot]==kind(mount)){
                    result[mount]=slot;used[slot]=true;break;
                }
            }
        }
        return result;
    }
    private static Kind kind(int mount){return mount<2?Kind.HANDGUN:Kind.LONG_GUN;}
}
