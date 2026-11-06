#version 120
uniform vec3 GlowColor;
uniform float Intensity;
uniform int Mask;
uniform int Textured;
uniform sampler2D Fabric;
varying vec3 Normal;
varying vec3 View;
varying vec4 Tint;
varying vec2 Tex;
void main(){
    vec4 fabric=Textured!=0?texture2D(Fabric,Tex):vec4(1.0);
    vec3 n=normalize(Normal),v=normalize(View);if(!gl_FrontFacing)n=-n;
    float rim=pow(1.0-abs(dot(n,v)),3.1);
    vec3 base=pow(clamp(Tint.rgb*fabric.rgb*1.25,0.0,1.0),vec3(.83));
    float key=max(0.0,dot(n,normalize(vec3(-0.5,0.8,0.7))));
    float bright=max(base.r,max(base.g,base.b));
    float accent=Textured!=0?smoothstep(0.45,0.9,bright):smoothstep(0.56,0.95,bright);
    vec3 emission=GlowColor*(rim*.75+accent*(Textured!=0?.14:.035))*Intensity;
    vec3 lit=mix(base*(.94+key*.12),mix(base,GlowColor,.78),min(.70,rim*Intensity*.70))+GlowColor*rim*Intensity*.10;
    float alpha=Tint.a*fabric.a;
    if(Mask!=0)gl_FragColor=vec4(emission,alpha);
    else gl_FragColor=vec4(lit,alpha);
}
