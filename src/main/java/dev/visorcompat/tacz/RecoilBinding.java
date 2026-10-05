package dev.visorcompat.tacz;
/** Authoritative recoil stream identity. Client stack instances change on ammo/NBT sync. */
public record RecoilBinding(String key,int slot,long generation) {
    public boolean matches(String key,int slot,long generation){return this.key.equals(key)&&this.slot==slot&&this.generation==generation;}
}
