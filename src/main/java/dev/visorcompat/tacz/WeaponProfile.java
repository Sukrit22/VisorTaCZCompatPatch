package dev.visorcompat.tacz;

import org.joml.Vector3f;

/** Coordinates are original TaCZ geometry pixels, before Bedrock conversion. */
public record WeaponProfile(
    float scale,
    float gripX,
    float gripY,
    float gripZ,
    float muzzleX,
    float muzzleY,
    float muzzleZ,
    float supportDistance,
    Mechanism mechanism
) {
    public enum Mechanism {
        BUTTON,
        MAGAZINE,
        PUMP,
        BOLT,
        SMG,
        M1911, P320, M9A4, DEAGLE, DEAGLE_GOLDEN, TIMELESS50, B93R, CZ75, HK_MK23, RHINO357, TAURUS500, TAURUS943, LONETRAIL,
    }

    public WeaponProfile(
        float scale,
        float gripX,
        float gripY,
        float gripZ,
        float muzzleX,
        float muzzleY,
        float muzzleZ,
        float supportDistance
    ) {
        this(
            scale,
            gripX,
            gripY,
            gripZ,
            muzzleX,
            muzzleY,
            muzzleZ,
            supportDistance,
            Mechanism.MAGAZINE
        );
    }

    public boolean buttonOnly(){return mechanism==Mechanism.BUTTON;}
    public boolean physical(){return !buttonOnly();}

    public boolean bolt() {
        return mechanism == Mechanism.BOLT;
    }

    public boolean smg() {
        return mechanism == Mechanism.SMG;
    }

    public boolean manualAction() {
        return pump() || bolt() || cylinder();
    }

    public boolean selector() {
        return (supportDistance > 0 || PistolProfiles.get(this)!=null && PistolProfiles.get(this).selector()) && !manualAction();
    }

    public boolean cylinder(){var d=PistolProfiles.get(this);return d!=null&&d.cylinder();}

    public String magazineNode(){var d=DescriptorProfiles.details(this);return d==null?"magazine":d.magazine();}
    public String rackNode() {
        var descriptor=DescriptorProfiles.details(this);return descriptor==null?"slide":descriptor.rack();
    }

    public String boltNode() {
        var descriptor=DescriptorProfiles.details(this);return descriptor==null?null:descriptor.bolt();
    }

    public boolean pump() {
        return mechanism == Mechanism.PUMP;
    }

    public boolean m1911() {
        return mechanism == Mechanism.M1911;
    }

    public Vector3f grip() {
        return convert(gripX, gripY, gripZ);
    }

    public Vector3f muzzleOffset() {
        return convert(muzzleX - gripX, muzzleY - gripY, muzzleZ - gripZ);
    }

    private Vector3f convert(float x, float y, float z) {
        return new Vector3f(-x, y, z).mul(scale / 16f);
    }
}
