#version 120
varying vec2 Tex;
void main(){Tex=gl_MultiTexCoord0.xy;gl_Position=gl_Vertex;}
