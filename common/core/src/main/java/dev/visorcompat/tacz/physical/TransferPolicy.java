package dev.visorcompat.tacz.physical;
/** A transfer may begin on demand; finishing an existing transfer is never gated. */
public final class TransferPolicy {
    public static boolean mayStart(boolean anytime,boolean spent,boolean open,boolean lifted,boolean chamberLoaded,boolean jammed){
        return anytime || spent || open || lifted || !chamberLoaded || jammed;
    }
}
