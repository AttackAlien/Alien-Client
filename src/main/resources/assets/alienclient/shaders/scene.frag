#version 120
uniform sampler2D Scene,Depth;
uniform mat4 InvProjection,InvView;
uniform vec3 Camera,Accent;
uniform float Time,Strength,Radius,WaveLife,ChromaRadius,ChromaMemory;
uniform int Mode,Sky,WaveCount,PathCount;
uniform vec4 Waves[4],Path[32];
varying vec2 uv;
float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1)),f.x),f.y);}
float fbm(vec2 p){float v=0.,a=.5;for(int i=0;i<4;i++){v+=noise(p)*a;p=p*2.03+3.1;a*=.5;}return v;}
vec3 skyColor(vec3 dir){float elev=clamp(dir.y*.6+.4,0.,1.);vec2 p=vec2(atan(dir.x,dir.z),asin(clamp(dir.y,-1.,1.)));
float clouds=fbm(p*vec2(4.,5.)+vec2(Time*.025,Time*.009));float stars=step(.9993,hash(floor(p*500.)))*smoothstep(.0,.3,dir.y);
vec3 c=vec3(0.);
if(Sky==1||Sky==2){c=mix(vec3(.19,.035,.34),vec3(.018,.012,.065),elev);c+=vec3(.31,.08,.53)*pow(clouds,2.)+vec3(.17,.08,.29)*noise(p*18.+Time*.01);c+=stars*.8;
if(Sky==2){vec2 b=p-vec2(.25,.55);b.x=mod(b.x+3.14159,6.28318)-3.14159;float r=length(b),disk=length(vec2(b.x,b.y*3.4));
float glow=exp(-abs(disk-.18)*70.)+exp(-abs(r-.083)*100.);c+=vec3(.7,.38,1.)*glow*1.7;c*=smoothstep(.068,.083,r);}}
else if(Sky==3){c=mix(vec3(.025,.16,.22),vec3(.005,.015,.05),elev);float ribbon=exp(-abs(p.y-(.45+.12*sin(p.x*3.+Time*.08)))*24.);c+=vec3(.12,.7,.53)*ribbon*clouds*1.7;c+=stars;}
else if(Sky==4){c=mix(vec3(.76,.85,.95),vec3(.30,.47,.72),elev);c=mix(c,vec3(.96,.97,1.),smoothstep(.4,.68,clouds)*.85);float snow=pow(hash(floor((p+vec2(Time*.013,Time*.028))*100.)),40.);c+=snow*.5;}
else if(Sky==5){float fire=fbm(vec2(p.x*5.,p.y*4.-Time*.15));c=mix(vec3(.09,.01,.025),vec3(.95,.18,.018),pow(1.-elev,.8));c+=vec3(1.,.32,.025)*pow(fire,3.)*(1.-elev)*1.8;}
else if(Sky==6){c=mix(vec3(1.,.49,.38),vec3(.17,.12,.44),elev);c+=vec3(.42,.12,.12)*clouds*.4;}
else if(Sky==7){c=mix(vec3(.10,.64,.71),vec3(.015,.06,.24),elev);c+=vec3(.08,.36,.42)*pow(sin(p.x*5.+clouds*8.+Time*.09)*.5+.5,7.)*.4;}
else {c=mix(vec3(.18,.20,.29),vec3(.025,.035,.075),elev);c+=clouds*.17;float flash=pow(max(0.,sin(Time*.7)),80.)*step(.55,noise(vec2(floor(Time*.2),4.)));c+=flash*vec3(.6,.65,.85);}
return clamp(c,0.,1.);}
void main(){vec3 base=texture2D(Scene,uv).rgb;float d=texture2D(Depth,uv).r;
vec4 eye=InvProjection*vec4(uv*2.-1.,d*2.-1.,1.);eye/=eye.w;vec4 local=InvView*eye;vec3 world=local.xyz+Camera;
if(d>.99999){vec3 dir=normalize((InvView*vec4(normalize(eye.xyz),0.)).xyz);
if(Sky>0)base=mix(base,skyColor(dir),Strength);
if(Mode==1){vec2 cell=fract(vec2(atan(dir.x,dir.z),asin(clamp(dir.y,-1.,1.)))*3.);vec2 edge=min(cell,1.-cell);float g=1.-smoothstep(.012,.025,min(edge.x,edge.y));base=mix(base,base*.65+Accent*g*.6,Strength);}
if(Mode==4){float grey=dot(base,vec3(.2126,.7152,.0722));base=mix(base,vec3(grey)*vec3(.86,.79,1.),Strength);}gl_FragColor=vec4(base,1.);return;}
vec3 normal=normalize(cross(dFdx(world),dFdy(world)));
if(Mode==1){float lum=dot(base,vec3(.2126,.7152,.0722));vec3 f=fract(world);vec3 edge=min(f,1.-f);float grid=abs(normal.y)>.6?min(edge.x,edge.z):abs(normal.x)>.6?min(edge.y,edge.z):min(edge.x,edge.y);float line=1.-smoothstep(.018,.045,grid);
float circuit=1.-smoothstep(.013,.03,abs(fract(world.x*.35+world.z*.21)-.5));vec3 cyber=vec3(.015,.014,.035)+lum*.055+Accent*(line*.74+circuit*.12);base=mix(base,cyber,Strength);}
if(Mode==4){float keep=0.;for(int i=0;i<32;i++){if(i>=PathCount)break;float age=Path[i].w;float r=ChromaRadius*(1.-smoothstep(ChromaMemory*.65,ChromaMemory,age));keep=max(keep,1.-smoothstep(r*.7,r,length(world-Path[i].xyz)));}
float grey=dot(base,vec3(.2126,.7152,.0722));vec3 muted=vec3(grey)*vec3(.86,.79,1.);base=mix(base,mix(muted,base,keep),Strength);}
if(abs(normal.y)>.45)for(int i=0;i<4;i++){if(i>=WaveCount)break;float age=Waves[i].w,t=age/WaveLife;if(t<0.||t>1.)continue;
float r=Radius*t,dist=length(world.xz-Waves[i].xz);float ring=exp(-pow((dist-r)/.13,2.));float glow=exp(-pow((dist-r)/.38,2.))*.35;
float plane=1.-smoothstep(.3,.8,abs(world.y-Waves[i].y));base+=(Accent*(ring+glow)+vec3(.3)*ring)*(1.-t)*plane*Strength;}
gl_FragColor=vec4(clamp(base,0.,1.),1.);}
