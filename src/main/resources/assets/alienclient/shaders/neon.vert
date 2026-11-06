#version 120
varying vec3 Normal;
varying vec3 View;
varying vec4 Tint;
varying vec2 Tex;
void main(){vec4 p=gl_ModelViewMatrix*gl_Vertex;View=-p.xyz;Normal=normalize(gl_NormalMatrix*gl_Normal);Tint=gl_Color;Tex=gl_MultiTexCoord0.xy;gl_Position=gl_ProjectionMatrix*p;}
