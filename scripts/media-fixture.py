#!/usr/bin/env python3
"""TEST ONLY. Synthetic metadata for the real Forge graphical runner, never a Spotify session."""
import argparse, hashlib, json, os, pathlib, struct, time, zlib
p=argparse.ArgumentParser();p.add_argument('--media-bridge',action='store_true');p.add_argument('--output',type=pathlib.Path,required=True);p.add_argument('--parent',type=int,required=True);p.add_argument('--source',required=True);p.add_argument('--art',required=True);a=p.parse_args()
control=pathlib.Path(os.environ['ALIEN_MEDIA_FIXTURE_CONTROL']);a.output.mkdir(parents=True,exist_ok=True)
previous=None;start=time.monotonic()
def png(version):
    def chunk(tag,data):return struct.pack('>I',len(data))+tag+data+struct.pack('>I',zlib.crc32(tag+data)&0xffffffff)
    pixels=b''.join(b'\0'+bytes(v for x in range(96) for v in ((x*2+version*20)%256,(y*2+40)%256,180)) for y in range(96))
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',96,96,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(pixels))+chunk(b'IEND',b'')
try:
    while pathlib.Path('/proc',str(a.parent)).exists():
        try: data=json.loads(control.read_text())
        except (FileNotFoundError,ValueError):time.sleep(.1);continue
        if data!=previous: previous=data;start=time.monotonic()
        if data.get('disconnect'):time.sleep(.1);continue
        state={'schema':1,'status':data.get('status','playing'),'title':data.get('title','MÍDIA DE TESTE — fonte sintética'),'artist':'Diagnóstico gráfico / não é Spotify','album':'Forge test fixture','source':'Diagnostic fixture','capturedAt':int(time.time()*1000),'positionMs':data.get('positionMs',3000),'durationMs':data.get('durationMs',120000),'rate':1,'artwork':''}
        if state['status']=='playing' and state['positionMs'] is not None:state['positionMs']+=int((time.monotonic()-start)*1000)
        if a.source=='Spotify': state['status']='idle'
        if state['status'] in ('playing','paused') and a.art=='true' and data.get('cover',True):
            cover=png(data.get('version',1));state['artwork']=hashlib.sha256(cover).hexdigest();tmp=a.output/'.cover-fixture';tmp.write_bytes(cover);tmp.replace(a.output/'cover.png')
        tmp=a.output/'.state-fixture';tmp.write_text(json.dumps(state),encoding='utf-8');tmp.replace(a.output/'now-playing.json');time.sleep(.2)
finally:
    for f in a.output.iterdir():
        if f.name in ('now-playing.json','cover.png','.cover-fixture','.state-fixture'):f.unlink(missing_ok=True)
    try:a.output.rmdir()
    except OSError:pass
