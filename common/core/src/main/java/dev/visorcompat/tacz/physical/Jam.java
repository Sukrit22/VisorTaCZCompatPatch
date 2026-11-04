package dev.visorcompat.tacz.physical;

/** Pure ammunition ledger. Reserved cartridges are removed from magazine/chamber. */
public record Jam(Kind kind,int remaining) {
    public enum Kind { NONE, STOVEPIPE, DOUBLE_FEED, DUD }
    public record Allocation(Jam jam,int magazine,boolean chamber) {}
    public static Allocation allocate(Kind requested,int magazine,boolean chamber) {
        int available=magazine+(chamber?1:0);
        Kind kind=requested;
        if(kind==Kind.DOUBLE_FEED && available<2)kind=available>0?Kind.DUD:Kind.STOVEPIPE;
        if(kind==Kind.DUD && available<1)kind=Kind.STOVEPIPE;
        int reserve=kind==Kind.DOUBLE_FEED?2:kind==Kind.DUD?1:0;
        boolean nextChamber=chamber;
        int nextMagazine=magazine;
        for(int i=0;i<reserve;i++) {if(nextChamber)nextChamber=false;else nextMagazine--;}
        return new Allocation(new Jam(kind,kind==Kind.STOVEPIPE?1:reserve),nextMagazine,nextChamber);
    }
    public Jam pull(boolean magazineOut) {
        if(kind==Kind.DOUBLE_FEED && !magazineOut)return this;
        return new Jam(kind,Math.max(0,remaining-1));
    }
    public int refundable() {return kind==Kind.DUD || kind==Kind.DOUBLE_FEED?remaining:0;}
}
