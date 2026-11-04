#version 120
uniform sampler2D Scene,Depth;
uniform mat4 InvProjection,InvView;
uniform vec3 Camera,Accent;
uniform float Time,Strength,Radius,WaveLife,ChromaRadius,ChromaMemory;
uniform int Mode,Sky,WaveCount,PathCount,Mist,Bokeh,AfterHand,Palette,Preview;
uniform int SkyPolish;uniform float CloudCover,CloudSoftness,HorizonHaze,StarDensity,VisualHour;
uniform float SkySpeed,SkyLiveliness,SkyBreathing,MistDensity,BokehStrength;uniform vec2 Size;
uniform vec4 Waves[4],Path[32];
varying vec2 uv;
float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1)),f.x),f.y);}
float fbm(vec2 p){float v=0.,a=.5;for(int i=0;i<4;i++){v+=noise(p)*a;p=p*2.03+3.1;a*=.5;}return v;}
float softNoise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*f*(f*(f*6.-15.)+10.);return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1)),f.x),f.y);}
float softFbm(vec2 p){float v=0.,a=.5;for(int i=0;i<4;i++){v+=softNoise(p)*a;p=p*2.03+3.1;a*=.5;}return v;}
float softStars(vec3 dir,float T){
 vec3 n=abs(dir);vec2 p;float face;
 if(n.z>=n.x&&n.z>=n.y){p=dir.xy/n.z;face=dir.z>0.?0.:1.;}
 else if(n.x>=n.y){p=dir.zy/n.x;face=dir.x>0.?2.:3.;}else{p=dir.xz/n.y;face=dir.y>0.?4.:5.;}
 vec2 q=(p*.5+.5)*100.,cell=floor(q),local=fract(q),seedCell=cell+face*vec2(131.,73.);
 float seed=hash(seedCell),gate=step(1.-StarDensity*.035,seed);vec2 center=.18+.64*vec2(hash(seedCell+17.3),hash(seedCell+42.9));
 float r=mix(.025,.09,hash(seedCell+51.7)),dist=length(local-center),aa=max(.014,max(fwidth(q.x),fwidth(q.y))*.65);
 float core=1.-smoothstep(max(.0,r-aa),r+aa,dist),glow=exp(-dist*dist/max(.0001,r*r*6.))*.15;
 float border=smoothstep(0.,.04,min(min(p.x+1.,1.-p.x),min(p.y+1.,1.-p.y)));
 return (core+glow)*gate*border*(.87+.13*sin(T*.45+seed*35.))*smoothstep(-.04,.15,dir.y);
}
vec3 atmosphere(vec3 dir,float T){
 float angle=(VisualHour-6.)*.2617994,sunHeight=sin(angle),day=smoothstep(-.14,.12,sunHeight),twilight=exp(-abs(sunHeight)*8.);
 float up=pow(clamp(dir.y,0.,1.),.48);vec3 zenith=mix(vec3(.008,.016,.045),vec3(.12,.38,.73),day),horizon=mix(vec3(.035,.06,.12),vec3(.65,.81,.95),day);
 horizon=mix(horizon,vec3(.92,.44,.24),twilight*.65);vec3 c=mix(horizon,zenith,up);
 vec3 sunDir=normalize(vec3(cos(angle),sunHeight,.08*cos(angle)));float sunDot=max(0.,dot(dir,sunDir));
 c+=vec3(1.,.79,.5)*pow(sunDot,18.)*(.11+twilight*.3)*day;
 vec2 anchor=Preview==1?vec2(0.):Camera.xz*.0015;vec2 q=anchor+dir.xz/max(.16,dir.y)*1.5;
 float broad=softFbm(q+vec2(T*.018,T*.007)),detail=softFbm(q*2.1+vec2(-T*.012,T*.01)+7.1);
 float mass=broad*.75+detail*.25,threshold=mix(.8,.27,CloudCover),soft=mix(.035,.15,CloudSoftness);
 float cloud=smoothstep(threshold-soft,threshold+soft,mass)*smoothstep(.12,.35,dir.y)*smoothstep(0.,.02,CloudCover);
 vec3 lit=mix(vec3(.12,.16,.25),vec3(.98,.985,1.),day),shadow=mix(vec3(.055,.075,.13),vec3(.55,.65,.78),day);
 vec3 cloudColor=mix(shadow,lit,smoothstep(threshold-soft,threshold+.18,mass));cloudColor=mix(cloudColor,vec3(.96,.65,.46),twilight*day*.38);
 c=mix(c,cloudColor,cloud*.93);c+=softStars(dir,T)*(1.-day)*(1.-cloud*.96);
 float haze=exp(-abs(dir.y)*9.)*HorizonHaze*.2;c=mix(c,horizon,haze);
 return clamp(c+vec3((hash(gl_FragCoord.xy)-.5)/255.),0.,1.);
}
vec3 skyColor(vec3 dir){float T=Time*SkySpeed;if(Sky==16)return atmosphere(dir,T);float elev=SkyPolish==1?smoothstep(-.12,.9,dir.y):clamp(dir.y*.6+.4,0.,1.);vec2 p=vec2(atan(dir.x,dir.z),asin(clamp(dir.y,-1.,1.)));
float clouds=SkyPolish==1?softFbm(dir.xz*vec2(3.,4.)/max(.3,dir.y+.6)+vec2(T*.018,T*.007)):fbm(p*vec2(4.,5.)+vec2(T*.025,T*.009));float stars=SkyPolish==1?softStars(dir,T):step(.9993,hash(floor(p*500.)))*smoothstep(.0,.3,dir.y);
vec3 c=vec3(0.);
if(Sky==1||Sky==2||Sky==9){c=mix(vec3(.19,.035,.34),vec3(.018,.012,.065),elev);c+=vec3(.31,.08,.53)*pow(clouds,2.)+vec3(.17,.08,.29)*noise(p*18.+T*.01);c+=stars*.8;
if(Sky==2){vec2 b=p-vec2(.25,.55);b.x=mod(b.x+3.14159,6.28318)-3.14159;float r=length(b),disk=length(vec2(b.x,b.y*3.4));
float glow=exp(-abs(disk-.18)*70.)+exp(-abs(r-.083)*100.);c+=vec3(.7,.38,1.)*glow*1.7;c*=smoothstep(.068,.083,r);}}
else if(Sky==3){c=mix(vec3(.025,.16,.22),vec3(.005,.015,.05),elev);float ribbon=exp(-abs(p.y-(.45+.12*sin(p.x*3.+T*.08)))*24.);c+=vec3(.12,.7,.53)*ribbon*clouds*1.7;c+=stars;}
else if(Sky==4){c=mix(vec3(.76,.85,.95),vec3(.30,.47,.72),elev);c=mix(c,vec3(.96,.97,1.),smoothstep(.4,.68,clouds)*.85);float snow=pow(hash(floor((p+vec2(T*.013,T*.028))*100.)),40.);c+=snow*.5;}
else if(Sky==5){float fire=fbm(vec2(p.x*5.,p.y*4.-T*.15));c=mix(vec3(.09,.01,.025),vec3(.95,.18,.018),pow(1.-elev,.8));c+=vec3(1.,.32,.025)*pow(fire,3.)*(1.-elev)*1.8;}
else if(Sky==6){c=mix(vec3(1.,.49,.38),vec3(.17,.12,.44),elev);c+=vec3(.42,.12,.12)*clouds*.4;}
else if(Sky==7){c=mix(vec3(.10,.64,.71),vec3(.015,.06,.24),elev);c+=vec3(.08,.36,.42)*pow(sin(p.x*5.+clouds*8.+T*.09)*.5+.5,7.)*.4;}
else if(Sky==10){c=vec3(.018,.03,.065);float threads=0.;for(int i=0;i<4;i++){float a=float(i);float line=p.y-sin(p.x*(1.5+a*.3)+T*.08+a)*(.2+a*.025)-.5;threads+=exp(-abs(line)*100.);}c+=vec3(.48,.8,.9)*threads;}
else if(Sky==11){c=mix(vec3(.1,.08,.22),vec3(.008,.009,.03),elev);c+=vec3(.17,.11,.32)*pow(clouds,2.);c+=stars*(SkyPolish==1?1.:(.5+.5*sin(T+p.x*5.)));}
else if(Sky==12){float bands=pow(.5+.5*sin(p.y*16.+sin(p.x*4.+T*.12)*2.),8.);c=vec3(.055,.012,.12)+vec3(.47,.12,.78)*bands*clouds;}
else if(Sky==13){float bands=pow(.5+.5*sin(p.x*10.+sin(p.y*6.+T*.09)*3.),10.);c=vec3(.025,.06,.10)+vec3(.32,.77,.82)*bands*.7;}
else if(Sky==14){c=mix(vec3(.53,.61,.7),vec3(.16,.22,.32),elev)+clouds*.12;vec2 r=vec2(p.x*100.,p.y*15.+T*2.);float streak=step(.97,hash(floor(r)))*(1.-fract(r.y));c+=vec3(.45,.65,.8)*streak;}
else if(Sky==15){c=mix(vec3(.87,.91,.97),vec3(.64,.72,.84),elev);vec2 snow=p*50.+vec2(T*.22,T*.4);float flakes=step(.975,hash(floor(snow)))*exp(-length(fract(snow)-.5)*8.);c+=flakes*.6;}
else {c=mix(vec3(.18,.20,.29),vec3(.025,.035,.075),elev);c+=clouds*.17;float flash=pow(max(0.,sin(T*.7)),80.)*step(.55,noise(vec2(floor(T*.2),4.)));c+=flash*vec3(.6,.65,.85);}
vec3 tint=Palette==1?vec3(1.3,.64,1.1):Palette==2?vec3(.6,1.2,1.4):Palette==3?vec3(1.5,.7,.35):vec3(1.);
c*=tint*(SkyPolish==1?(.97+.03*sin(T*.25*SkyBreathing)):(.85+.15*sin(T*.7*SkyBreathing)))*(.65+SkyLiveliness*.58);return clamp(c+(SkyPolish==1?vec3((hash(gl_FragCoord.xy)-.5)/255.):vec3(0.)),0.,1.);}
void main(){if(Preview==1){gl_FragColor=vec4(skyColor(normalize(vec3(uv*vec2(4.,2.)-vec2(2.,.2),1.))),1.);return;}vec3 base=texture2D(Scene,uv).rgb;float d=texture2D(Depth,uv).r;
if(AfterHand==1){vec2 px=1.5/Size;vec3 neighbors=(texture2D(Scene,uv+vec2(px.x,0)).rgb+texture2D(Scene,uv-vec2(px.x,0)).rgb+texture2D(Scene,uv+vec2(0,px.y)).rgb+texture2D(Scene,uv-vec2(0,px.y)).rgb)*.25;float edge=length(base-neighbors);gl_FragColor=vec4(clamp(base+Accent*edge*Strength*2.,0.,1.),1.);return;}
if(Bokeh==1){float focus=texture2D(Depth,vec2(.5)).r;float linear=1./max(.00002,1.-d),focal=1./max(.00002,1.-focus);float blur=clamp(abs(linear-focal)/max(60.,focal),0.,1.)*BokehStrength*5.;vec3 sum=base;float weight=1.;for(int i=0;i<8;i++){float a=float(i)*.785398;vec2 offset=vec2(cos(a),sin(a))*blur/Size;float nd=texture2D(Depth,uv+offset).r;float w=1.-smoothstep(.0002,.003,abs(nd-d));sum+=texture2D(Scene,uv+offset).rgb*w;weight+=w;}base=sum/weight;}
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
if(Mist==1){float fog=(1.-exp(-length(local.xyz)*.018*MistDensity))*(.5+.5*exp(-abs(world.y-Camera.y)*.12));vec3 mist=mix(vec3(.52,.53,.65),Accent,.18);base=mix(base,mist,clamp(fog,0.,.8));}
if(abs(normal.y)>.45)for(int i=0;i<4;i++){if(i>=WaveCount)break;float age=Waves[i].w,t=age/WaveLife;if(t<0.||t>1.)continue;
float r=Radius*t,dist=length(world.xz-Waves[i].xz);float ring=exp(-pow((dist-r)/.13,2.));float glow=exp(-pow((dist-r)/.38,2.))*.35;
float plane=1.-smoothstep(.3,.8,abs(world.y-Waves[i].y));base+=(Accent*(ring+glow)+vec3(.3)*ring)*(1.-t)*plane*Strength;}
gl_FragColor=vec4(clamp(base,0.,1.),1.);}
