package dev.visorcompat.tacz.physical;
/** Render-time recoil of moving parts only; never feeds ammunition. */
public final class ShotMotion {
    public static float cycle(float seconds){if(!Float.isFinite(seconds)||seconds<0||seconds>=.12f)return 0;return seconds<.025f?seconds/.025f:1-(seconds-.025f)/.095f;}
}
