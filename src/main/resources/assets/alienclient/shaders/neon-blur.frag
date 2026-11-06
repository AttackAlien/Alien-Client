#version 120
uniform sampler2D Image;
uniform vec2 Direction;
varying vec2 Tex;
void main(){
 vec3 c=texture2D(Image,Tex).rgb*.227027;
 c+=(texture2D(Image,Tex+Direction*1.384615).rgb+texture2D(Image,Tex-Direction*1.384615).rgb)*.316216;
 c+=(texture2D(Image,Tex+Direction*3.230769).rgb+texture2D(Image,Tex-Direction*3.230769).rgb)*.070270;
 gl_FragColor=vec4(c,1.0);
}
