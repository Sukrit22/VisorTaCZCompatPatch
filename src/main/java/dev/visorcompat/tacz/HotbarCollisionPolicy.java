package dev.visorcompat.tacz;

/** Resolves rejection without displacing the hand that already owns a gun. */
public final class HotbarCollisionPolicy {
    private HotbarCollisionPolicy() {}
    public static boolean protects(boolean gun, boolean held, boolean lockSelected) {
        return gun && (held || lockSelected);
    }
    public static int previousSelection(boolean selectingMain, int previous, int ownerSlot) {
        if (previous >= 0 && previous < 9 && previous != ownerSlot) return previous;
        // An offhand can be empty. Minecraft's selected main slot must remain valid.
        return selectingMain ? (ownerSlot + 1) % 9 : -1;
    }
}
