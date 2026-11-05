#!/usr/bin/env python3
"""Convert the credited Quaternius glTF rig into bounded, shared animation data.

Requires Python 3 + NumPy. Geometry, skin weights and all 14 authored clips come
from the original GLB; no reconstruction or procedural replacement of the mesh.
The Java client samples baked joint matrices and skins vertices at render time.
"""
from pathlib import Path
import argparse, gzip, hashlib, json, math, struct
import numpy as np


def rigid(doc, accessor, raw, output, yaw):
    """Faithful static mesh import for the credited Sahur model, without invented rig clips."""
    assert not doc.get("skins") and not doc.get("animations") and len(doc["nodes"])==2
    node=next(n for n in doc["nodes"] if n.get("mesh")==0)
    x,y,z,w=node.get("rotation",[0,0,0,1]);scale=np.array(node.get("scale",[1,1,1]))
    rotation=np.array([[1-2*(y*y+z*z),2*(x*y-z*w),2*(x*z+y*w)],
                       [2*(x*y+z*w),1-2*(x*x+z*z),2*(y*z-x*w)],
                       [2*(x*z-y*w),2*(y*z+x*w),1-2*(x*x+y*y)]])
    a=math.radians(yaw);turn=np.array([[math.cos(a),0,math.sin(a)],[0,1,0],[-math.sin(a),0,math.cos(a)]])
    transform=turn@rotation@np.diag(scale);normal=np.linalg.inv(transform).T
    positions=[];normals=[];materials=[];indices=[];offset=0
    for primitive in doc["meshes"][0]["primitives"]:
        assert primitive.get("mode",4)==4
        p=accessor(primitive["attributes"]["POSITION"])@transform.T
        n=accessor(primitive["attributes"]["NORMAL"])@normal.T
        n/=np.linalg.norm(n,axis=1,keepdims=True)
        positions.append(p);normals.append(n);materials.extend([primitive["material"]]*len(p))
        tri=accessor(primitive["indices"]).flatten();assert len(tri)%3==0 and tri.max()<len(p)
        indices.extend((tri+offset).tolist());offset+=len(p)
    positions=np.concatenate(positions);normals=np.concatenate(normals)
    low,high=positions.min(axis=0),positions.max(axis=0);factor=.72/(high[1]-low[1])
    positions=(positions-(low+high)/2)*factor+np.array([0,.14,0])
    colors=[]
    for m in doc["materials"]:
        linear=np.array(m["pbrMetallicRoughness"]["baseColorFactor"][:3]);srgb=np.where(linear<=.0031308,linear*12.92,1.055*np.power(linear,1/2.4)-.055)
        rgb=np.rint(np.clip(srgb,0,1)*255).astype(int);colors.append((int(rgb[0])<<16)|(int(rgb[1])<<8)|int(rgb[2]))
    data=bytearray(struct.pack(">6I",0x414c5231,1,len(positions),len(indices),len(colors),0))
    for color in colors:data.extend(struct.pack(">I",color))
    for p,n,m in zip(positions,normals,materials):data.extend(np.asarray(np.r_[p,n],dtype=">f4").tobytes());data.append(m)
    data.extend(np.asarray(indices,dtype=">i4").tobytes());output.parent.mkdir(parents=True,exist_ok=True)
    output.write_bytes(gzip.compress(bytes(data),compresslevel=9,mtime=0))
    report=dict(author="CalinnHotCake",license="CC-BY-3.0",model="Tung Tung Tung Sahur",sourceUrl="https://poly.pizza/m/Q6SvoDhhwA",
                sourceSha256=hashlib.sha256(raw).hexdigest(),outputSha256=hashlib.sha256(output.read_bytes()).hexdigest(),
                vertices=len(positions),triangles=len(indices)//3,materials=[f"#{c:06X}" for c in colors],
                authoredAnimations=0,joints=0,normalizationScale=factor,orientationYaw=yaw,compressedBytes=output.stat().st_size)
    output.with_suffix(".json").write_text(json.dumps(report,indent=2)+"\n");print(json.dumps(report,indent=2))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--rigid", action="store_true")
    parser.add_argument("--yaw", type=float, default=0)
    args = parser.parse_args()
    raw = args.source.read_bytes()
    assert raw[:4] == b"glTF" and struct.unpack_from("<II", raw, 4) == (2, len(raw))
    size, kind = struct.unpack_from("<II", raw, 12)
    assert kind == 0x4e4f534a
    doc = json.loads(raw[20:20+size])
    length, kind = struct.unpack_from("<II", raw, 20+size)
    assert kind == 0x004e4942
    binary = raw[28+size:28+size+length]
    assert not doc.get("images") and len(doc["meshes"]) == 1
    types = {5121: "u1", 5123: "<u2", 5125: "<u4", 5126: "<f4"}
    widths = {"SCALAR": 1, "VEC2": 2, "VEC3": 3, "VEC4": 4, "MAT4": 16}

    def accessor(index):
        a = doc["accessors"][index]
        assert not a.get("sparse") and not a.get("normalized")
        v = doc["bufferViews"][a["bufferView"]]
        assert v.get("buffer", 0) == 0
        dtype = np.dtype(types[a["componentType"]]); width = widths[a["type"]]
        offset = v.get("byteOffset", 0) + a.get("byteOffset", 0)
        stride = v.get("byteStride", width*dtype.itemsize)
        assert offset+(a["count"]-1)*stride+width*dtype.itemsize <= len(binary)
        return np.ndarray((a["count"], width), dtype=dtype, buffer=binary,
                          offset=offset, strides=(stride, dtype.itemsize)).copy()

    if args.rigid:
        rigid(doc,accessor,raw,args.output,args.yaw)
        return
    assert len(doc["skins"])==1
    nodes = doc["nodes"]; parents = [-1]*len(nodes)
    for i, n in enumerate(nodes):
        assert "matrix" not in n
        for child in n.get("children", []):
            assert parents[child] == -1
            parents[child] = i
    assert all(parent < i for i, parent in enumerate(parents))
    translations = np.array([n.get("translation", [0, 0, 0]) for n in nodes], dtype=float)
    rotations = np.array([n.get("rotation", [0, 0, 0, 1]) for n in nodes], dtype=float)
    scales = np.array([n.get("scale", [1, 1, 1]) for n in nodes], dtype=float)
    skin = doc["skins"][0]; joints = skin["joints"]
    inverse = accessor(skin["inverseBindMatrices"]).reshape(-1, 4, 4).transpose(0, 2, 1)
    positions=[]; normals=[]; weights=[]; ids=[]; materials=[]; indices=[]; offset=0
    for primitive in doc["meshes"][0]["primitives"]:
        assert primitive.get("mode", 4) == 4
        a = primitive["attributes"]
        p, n, w, j = [accessor(a[k]) for k in ("POSITION", "NORMAL", "WEIGHTS_0", "JOINTS_0")]
        assert np.allclose(w.sum(axis=1), 1, atol=1e-5) and np.all(j < len(joints))
        w = w/w.sum(axis=1, keepdims=True)
        positions.append(p); normals.append(n); weights.append(w); ids.append(j)
        materials.extend([primitive["material"]]*len(p))
        tri = accessor(primitive["indices"]).flatten()
        assert len(tri)%3 == 0 and tri.max() < len(p)
        indices.extend((tri+offset).tolist()); offset += len(p)
    positions, normals, weights, ids = [np.concatenate(a) for a in (positions, normals, weights, ids)]

    def trs(t, q, s):
        q = q/np.linalg.norm(q); x, y, z, w = q
        m = np.array([[1-2*(y*y+z*z), 2*(x*y-z*w), 2*(x*z+y*w), t[0]],
                      [2*(x*y+z*w), 1-2*(x*x+z*z), 2*(y*z-x*w), t[1]],
                      [2*(x*z-y*w), 2*(y*z+x*w), 1-2*(x*x+y*y), t[2]],
                      [0, 0, 0, 1]], dtype=float)
        m[:3, :3] *= s
        return m

    def interpolate(times, values, time, quaternion):
        right = min(len(times)-1, int(np.searchsorted(times, time, side="right")))
        left = max(0, right-1); span = times[right]-times[left]
        f = np.clip((time-times[left])/span, 0, 1) if span > 0 else 0
        a, b = values[left], values[right]
        if quaternion:
            dot = float(np.dot(a, b))
            if dot < 0: b, dot = -b, -dot
            if dot < .9995:
                angle = math.acos(np.clip(dot, -1, 1))
                return (a*math.sin((1-f)*angle)+b*math.sin(f*angle))/math.sin(angle)
            q = a+(b-a)*f
            return q/np.linalg.norm(q)
        return a+(b-a)*f

    clips=[]
    for animation in doc["animations"]:
        channels=[]; duration=0
        for channel in animation["channels"]:
            sample=animation["samplers"][channel["sampler"]]
            assert sample.get("interpolation", "LINEAR") == "LINEAR"
            times=accessor(sample["input"]).flatten(); values=accessor(sample["output"])
            assert np.all(np.diff(times)>0) and len(times)==len(values)
            channels.append((channel["target"]["node"], channel["target"]["path"], times, values))
            duration=max(duration, float(times[-1]))
        frames=[]; count=max(2, round(duration*24)+1)
        for frame in range(count):
            time=duration*frame/(count-1)
            t, q, s = translations.copy(), rotations.copy(), scales.copy()
            for node, path, times, values in channels:
                target={"translation": t, "rotation": q, "scale": s}[path]
                target[node]=interpolate(times, values, time, path=="rotation")
            world=[]
            for i in range(len(nodes)):
                m=trs(t[i], q[i], s[i]); world.append(world[parents[i]]@m if parents[i]>=0 else m)
            frames.append(np.array([world[node]@inverse[j] for j, node in enumerate(joints)]))
        clips.append((animation["name"].split("Alien_")[-1], duration, np.array(frames)))
    assert len(clips)==14 and len(joints)==45 and len(positions)<=12000

    idle=next(c[2][0] for c in clips if c[0]=="Idle")
    homogeneous=np.column_stack((positions, np.ones(len(positions))))
    base=np.sum(np.einsum("nvij,nj->nvi", idle[ids], homogeneous)*weights[:,:,None],axis=1)[:,:3]
    low, high = base.min(axis=0), base.max(axis=0)
    center=(low+high)/2; factor=.72/(high[1]-low[1])
    normalize=np.eye(4); normalize[:3,:3]*=factor
    normalize[:3,3]=-center*factor+np.array([0,.14,0])
    output=bytearray(struct.pack(">6I", 0x414c4e32, 1, len(positions), len(indices), len(joints), len(clips)))
    def floats(array):output.extend(np.asarray(array, dtype=">f4").tobytes())
    def string(value):
        encoded=value.encode("utf-8");output.extend(struct.pack(">H",len(encoded)));output.extend(encoded)
    for i in range(len(positions)):
        floats(positions[i]);floats(normals[i]);output.extend(np.asarray(ids[i], dtype=np.uint8).tobytes());floats(weights[i]);output.append(materials[i])
    output.extend(np.asarray(indices, dtype=">i4").tobytes())
    metadata=[]
    for name, duration, frames in clips:
        string(name);output.extend(struct.pack(">fI",duration,len(frames)))
        world=np.einsum("ij,fnjk->fnik",normalize,frames)
        normal=np.linalg.inv(world[:,:,:3,:3]).transpose(0,1,3,2)
        packed=np.concatenate((world[:,:,:3,:].reshape(len(frames),len(joints),12),normal.reshape(len(frames),len(joints),9)),axis=2)
        assert np.isfinite(packed).all();floats(packed)
        metadata.append(dict(name=name,duration=duration,frames=len(frames)))
    args.output.parent.mkdir(parents=True,exist_ok=True)
    args.output.write_bytes(gzip.compress(bytes(output),compresslevel=9,mtime=0))
    report=dict(author="Quaternius",license="CC0-1.0",model="Alien (Quaternius)",
                sourceUrl="https://poly.pizza/m/HYUUkdugoP",sourceSha256=hashlib.sha256(raw).hexdigest(),
                outputSha256=hashlib.sha256(args.output.read_bytes()).hexdigest(),vertices=len(positions),
                triangles=len(indices)//3,joints=len(joints),clips=metadata,normalizationScale=factor,
                normalizationCenter=center.tolist(),packedBytes=len(output),compressedBytes=args.output.stat().st_size)
    args.output.with_suffix(".json").write_text(json.dumps(report,indent=2)+"\n")
    print(json.dumps(report,indent=2))


if __name__=="__main__":main()
