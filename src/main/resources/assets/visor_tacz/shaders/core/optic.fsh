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
    if (Scope < 0.5) {
        float coverage = 1.0 - smoothstep(1.0, 2.1, length(pixels));
        vec3 scene = texture(Sampler0, uv).rgb;
        fragColor = vec4(mix(scene, vec3(1.0, 0.07, 0.025), coverage), 1.0);
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
