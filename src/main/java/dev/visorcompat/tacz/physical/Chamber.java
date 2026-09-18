package dev.visorcompat.tacz.physical;

/** Ammo-only transitions; no inventory access or client-side mutation. */
public record Chamber(int magazine, boolean loaded) {
    public Chamber { if(magazine<0) throw new IllegalArgumentException("Negative magazine ammo"); }
    public Chamber unchamber() { return new Chamber(magazine+(loaded?1:0),false); }
    public Chamber eject() { return new Chamber(magazine,false); }
    public Chamber feed() { return loaded?this:magazine>0?new Chamber(magazine-1,true):new Chamber(0,false); }
    public Chamber rack() { return eject().feed(); }
    public int rounds() { return magazine+(loaded?1:0); }
}
