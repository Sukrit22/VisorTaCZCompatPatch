package dev.visorcompat.tacz;
import java.util.Map;
import org.joml.Vector3f;
import dev.visorcompat.tacz.WeaponProfile.Mechanism;
/** Pinned default-pack parts and sound assets. Interaction points are calibratable starting estimates. */
public final class PistolProfiles {
    public record Data(String id,String rack,boolean cylinder,boolean selector,String ammo,float openDegrees,
                       Vector3f shell,Vector3f magazine,Vector3f rackPoint,String folder,
                       String magOut,String magIn,String actionBack,String actionClose) {
        public String sound(int kind){return folder+"/"+switch(kind){case 0->magOut;case 1->magIn;case 5,6->actionClose;default->actionBack;};}
    }
    private static final Map<Mechanism,Data> DATA=Map.ofEntries(
        Map.entry(Mechanism.M1911, new Data("m1911", "slide", false, false, "tacz:45acp", 0f,
            new Vector3f(0f, 5.55f, 0.625f), new Vector3f(0.0f, -0.0780049f, 0.0655987f), new Vector3f(0.0f, 0.0553125f, 0.0640625f),
            "m1911", "m1911_reload_magout", "m1911_reload_magin", "m1911_inspect_slide_pull", "m1911_reload_empty_chamber")),
        Map.entry(Mechanism.P320, new Data("p320", "slide", false, false, "tacz:45acp", 0f,
            new Vector3f(0f, 9.625f, 2f), new Vector3f(0.0f, 0.0069619f, 0.0312581f), new Vector3f(0.0f, 0.0609375f, 0.044375f),
            "p320", "p320_reload_magout", "p320_reload_magin", "p320_inspect_slidepull", "p320_inspect_sliderelease")),
        Map.entry(Mechanism.M9A4, new Data("m9a4", "upper", false, false, "tacz:9mm", 0f,
            new Vector3f(0.0625f, 9.5625f, 2.79688f), new Vector3f(0.0f, -0.15f, 0.084375f), new Vector3f(0.0023437f, 0.0585938f, 0.074258f),
            "m9a4", "m9a4_reload_magout", "m9a4_reload_magin", "m9a4_inspect_slide", "m9a4_inspect_slide")),
        Map.entry(Mechanism.DEAGLE, new Data("deagle", "slide2", false, false, "tacz:50ae", 0f,
            new Vector3f(0f, 5.95f, 5.775f), new Vector3f(0.0f, -0.058125f, 0.0440625f), new Vector3f(0.0f, 0.07125f, 0.051875f),
            "deagle", "deagle_reload_magout", "deagle_reload_magin", "deagle_inspect_slideback", "deagle_inspect_slideclose")),
        Map.entry(Mechanism.DEAGLE_GOLDEN, new Data("deagle_golden", "slide2", false, false, "tacz:357mag", 0f,
            new Vector3f(0f, 5.95f, 5.775f), new Vector3f(0.0f, -0.058125f, 0.0440625f), new Vector3f(0.0f, 0.07125f, 0.051875f),
            "deagle", "deagle_reload_magout", "deagle_reload_magin", "deagle_inspect_slideback", "deagle_inspect_slideclose")),
        Map.entry(Mechanism.TIMELESS50, new Data("timeless50", "slide", false, false, "tacz:50ae", 0f,
            new Vector3f(0f, 9.375f, 2.25f), new Vector3f(0.0f, -0.0751924f, 0.0505987f), new Vector3f(0.0f, 0.0515625f, 0.05375f),
            "timeless50", "timeless50_reload_magout", "timeless50_reload_magin", "timeless50_inspect_slidepull", "timeless50_inspect_slidepull")),
        Map.entry(Mechanism.B93R, new Data("b93r", "upper", false, true, "tacz:9mm", 0f,
            new Vector3f(0f, 9.6875f, 2.975f), new Vector3f(0.0f, -0.253125f, 0.121875f), new Vector3f(0.0f, 0.0632812f, 0.0809375f),
            "b93r", "b93r_reload_magout", "b93r_reload_magin", "b93r_inspect_slide_back", "b93r_inspect_slide_release")),
        Map.entry(Mechanism.CZ75, new Data("cz75", "slide", false, false, "tacz:9mm", 0f,
            new Vector3f(0.1f, 6.275f, 0.7f), new Vector3f(-0.0046875f, -0.118125f, 0.1021875f), new Vector3f(0.00375f, 0.0571875f, 0.06125f),
            "cz75", "cz75_reload_magout", "cz75_reload_magin", "cz75_inspect_slidepull", "cz75_inspect_sliderelease")),
        Map.entry(Mechanism.HK_MK23, new Data("hk_mk23", "slide", false, true, "tacz:45acp", 0f,
            new Vector3f(0.25f, 6.71875f, 6.28125f), new Vector3f(0.0f, -0.046875f, 0.05625f), new Vector3f(0.009375f, 0.0550781f, 0.0642969f),
            "m1911", "m1911_reload_magout", "m1911_reload_magin", "m1911_inspect_slide_pull", "m1911_reload_empty_chamber")),
        Map.entry(Mechanism.RHINO357, new Data("rhino357", "cylinder_crane", true, false, "tacz:357mag", 88f,
            new Vector3f(1.6166667f, 8.9875f, 1.0625f), new Vector3f(0.060625f, 0.0365625f, -0.0651562f), new Vector3f(-0.009375f, 0.0065625f, -0.0651562f),
            "rhino357", "rhino_reload_eject", "rhino_reload_bulletin", "rhino_reload_cylinder_turn", "rhino_reload_cylinder_close")),
        Map.entry(Mechanism.TAURUS500, new Data("taurus500", "cranecylinder", true, false, "tacz:500mag", 107f,
            new Vector3f(1.7416667f, 9.3f, 1.0f), new Vector3f(0.0653125f, 0.045f, -0.1720312f), new Vector3f(-0.0046875f, 0.015f, -0.1720312f),
            "taurus500", "taurus500_reload_bullet_eject", "taurus500_reload_bullet_in", "taurus500_reload_cylinder_turn", "taurus500_reload_cylinder_off")),
        Map.entry(Mechanism.TAURUS943, new Data("taurus943", "cylinder_group", true, false, "tacz:22wmr", 95.5f,
            new Vector3f(1.7104167f, 5.14375f, -2.0625f), new Vector3f(0.0641406f, 0.0344531f, -0.1151955f), new Vector3f(-0.0058594f, 0.0044531f, -0.1151955f),
            "taurus943", "943_reload_bullet_eject", "943_reload_bullet_insert_1", "943_reload_cylinder_turn", "943_reload_cylinder_close")),
        Map.entry(Mechanism.LONETRAIL, new Data("lonetrail", "breach2", true, false, "tacz:30_06", -105f,
            new Vector3f(1.8666667f, 8.425f, 9.0f), new Vector3f(0.07f, 0.0672656f, 0.2346094f), new Vector3f(0.0f, 0.0372656f, 0.2346094f),
            "lonetrail", "breach_reload_eject", "breach_reload_insert", "breach_reload_gate_on", "breach_reload_gate_close"))
    );
    public static Data get(WeaponProfile p){return p==null?null:DATA.get(p.mechanism());}
}
