#version 120
uniform sampler2D Scene;uniform vec2 Size;uniform vec3 Accent;uniform float Strength;
varying vec3 normal;
void main(){vec3 n=normalize(normal);vec2 uv=gl_FragCoord.xy/Size;float fresnel=pow(1.-abs(n.z),2.);
vec3 through=texture2D(Scene,clamp(uv+n.xy*.009*Strength,vec2(.001),vec2(.999))).rgb;
vec3 col=mix(through,vec3(.92,.95,1.),.14+fresnel*.38)+Accent*fresnel*.1;
gl_FragColor=vec4(col,.72+fresnel*.22);}
