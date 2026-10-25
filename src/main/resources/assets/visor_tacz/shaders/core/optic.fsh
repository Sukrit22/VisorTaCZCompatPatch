#version 150
uniform sampler2D Sampler0;
uniform vec4 Viewport;
uniform vec2 AimUV;
uniform float Zoom;
uniform float Scope;
uniform float EyeValid;
out vec4 fragColor;
void main() {
    vec2 uv = (gl_FragCoord.xy - Viewport.xy) / Viewport.zw;
    vec2 pixels = (uv - AimUV) * Viewport.zw;
    if (Zoom <= 1.0001) {
        // An unmagnified aperture is transparent. Replaying the pre-gun scene
        // here erases the barrel/muzzle and any other gun seen through the lens.
        float coverage;
        if (Scope < 0.5) {
            coverage = 1.0 - smoothstep(1.0, 2.1, length(pixels));
        } else {
            coverage = min(abs(pixels.x), abs(pixels.y)) < 0.8
                && max(abs(pixels.x), abs(pixels.y)) < 12.0 ? 1.0 : 0.0;
        }
        if (coverage <= 0.0) discard;
        fragColor = vec4(1.0, 0.07, 0.025, coverage);
        return;
    }
    vec2 sampleUV = AimUV + (uv - AimUV) / max(Zoom, 1.0);
    if (EyeValid < 0.5 || any(lessThan(sampleUV, vec2(0.0))) || any(greaterThan(sampleUV, vec2(1.0)))) {
        fragColor = vec4(0.015, 0.015, 0.015, 1.0);
        return;
    }
    vec3 scene = texture(Sampler0, sampleUV).rgb;
    bool crosshair = min(abs(pixels.x), abs(pixels.y)) < 0.8 && max(abs(pixels.x), abs(pixels.y)) < 12.0;
    fragColor = vec4(crosshair ? vec3(0.95, 0.08, 0.03) : scene, 1.0);
}
