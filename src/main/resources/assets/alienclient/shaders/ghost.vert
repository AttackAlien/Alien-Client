#version 120
varying vec3 Normal;
varying vec3 View;
varying vec3 Position;
varying vec4 Tint;
varying float Height;
void main() {
    vec4 eye = gl_ModelViewMatrix * gl_Vertex;
    View = -eye.xyz;
    Normal = normalize(gl_NormalMatrix * gl_Normal);
    Position = gl_Vertex.xyz;
    Height = gl_MultiTexCoord0.x;
    Tint = gl_Color;
    gl_Position = gl_ProjectionMatrix * eye;
}
