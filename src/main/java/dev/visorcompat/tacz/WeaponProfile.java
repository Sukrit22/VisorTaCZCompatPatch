package dev.visorcompat.tacz;

import org.joml.Vector3f;

/** Coordinates are original TaCZ geometry pixels, before Bedrock conversion. */
public record WeaponProfile(float scale, float gripX, float gripY, float gripZ,
                            float muzzleX, float muzzleY, float muzzleZ,
                            float supportDistance, Mechanism mechanism) {
    public enum Mechanism { MAGAZINE, PUMP }
    public WeaponProfile(float scale,float gripX,float gripY,float gripZ,float muzzleX,float muzzleY,float muzzleZ,float supportDistance) {
        this(scale,gripX,gripY,gripZ,muzzleX,muzzleY,muzzleZ,supportDistance,Mechanism.MAGAZINE);
    }
    public boolean pump(){return mechanism==Mechanism.PUMP;}
    public Vector3f grip() { return convert(gripX, gripY, gripZ); }
    public Vector3f muzzleOffset() {
        return convert(muzzleX - gripX, muzzleY - gripY, muzzleZ - gripZ);
    }
    private Vector3f convert(float x, float y, float z) {
        return new Vector3f(-x, y, z).mul(scale / 16f);
    }
}
