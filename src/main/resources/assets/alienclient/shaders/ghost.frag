#version 120
uniform float Time;
uniform int Mask;
uniform float Cut;
uniform float Opacity;
uniform float Glow;
varying vec3 Normal;
varying vec3 View;
varying vec3 Position;
varying vec4 Tint;
varying float Height;
void main() {
    vec3 n = normalize(Normal);
    vec3 v = normalize(View);
    if (!gl_FrontFacing) n = -n;
    vec3 key = normalize(vec3(-0.6, 0.8, 0.75));
    float diffuse = max(0.0, dot(n, key));
    float rim = pow(1.0 - abs(dot(n,v)), 2.6);
    float sheen = pow(max(0.0, dot(reflect(-key,n),v)), 26.0);
    float ripple = sin(Position.x*31.0+sin(Position.z*19.0-Time*0.8)*1.8+Position.y*16.0-Time*1.4);
    float field = Height + ripple*0.014 + sin(Position.z*43.0+Position.y*19.0+Time)*0.009;
    float mask = smoothstep(Cut-0.025, Cut+0.085, field);
    float edge = exp(-abs(field-Cut-0.01)*70.0)*step(0.015,Cut);
    vec3 pearl = mix(Tint.rgb,vec3(0.88,0.93,1.0),0.12);
    vec3 color = pearl*(0.84+diffuse*0.35) + Tint.rgb*rim*(0.26+Glow*0.55) + vec3(1.0)*sheen*0.45;
    color += mix(Tint.rgb,vec3(0.84,1.0,0.98),0.50)*edge*(0.35+Glow*0.60);
    float alpha = Opacity*mask*(0.64+rim*0.36);
    if(Mask!=0)color=Tint.rgb*(rim*.85+edge*.9)*Glow;
    gl_FragColor = vec4(color,alpha);
}
