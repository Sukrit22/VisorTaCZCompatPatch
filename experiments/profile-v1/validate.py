"""Offline schema experiment, Python 3.11+. No Minecraft/pack installation or gameplay changes."""
import math
import re
import sys
import tomllib
from pathlib import Path

class ProfileError(ValueError):
    pass

def require(ok, message):
    if not ok:
        raise ProfileError(message)

def vector(v, length=3):
    return isinstance(v,list) and len(v)==length and all(type(x) in (int,float) and math.isfinite(x) for x in v)

def validate(doc):
    require(doc.get('schema')==1,'unsupported schema')
    require(isinstance(doc.get('guns'),list) and doc['guns'],'guns must be a nonempty array')
    seen=set()
    for gun in doc['guns']:
        key=(gun.get('id'),gun.get('display'))
        require(all(isinstance(x,str) and re.fullmatch(r'[a-z0-9_.-]+:[a-z0-9_./-]+',x) for x in key),'invalid gun/display ID')
        require(key not in seen,'duplicate gun/display');seen.add(key)
        require(gun.get('mechanism')=='closed_bolt_magazine','prototype only validates closed_bolt_magazine')
        require(gun.get('ammo_adapter')=='native_rounds','prototype does not support custom ammo adapters')
        require(gun.get('model')=='tacz_resolved_display','use resolved TaCZ display')
        parts=gun.get('parts',{});joints=gun.get('joints',{});zones=gun.get('zones',{})
        require('receiver' in parts and 'magazine' in parts,'missing mechanism parts')
        for name,part in parts.items():
            require(part.get('source') in ('gun_model','native_ammo_shell'),'unknown part source: '+name)
            if part['source']=='gun_model':require(isinstance(part.get('node'),str) and bool(part['node']),'missing node: '+name)
        driven=set()
        for name,joint in joints.items():
            require(joint.get('part') in parts,'unknown joint part: '+name)
            require(joint['part'] not in driven,'multiple joints need explicit ordered composition');driven.add(joint['part'])
            require(joint.get('kind') in ('prismatic','revolute'),'unknown joint type')
            require(joint.get('frame')=='gun','prototype requires explicit gun frame')
            require(vector(joint.get('axis')) and abs(sum(x*x for x in joint['axis'])-1)<1e-5,'joint axis must be unit length')
            require(vector(joint.get('limits'),2) and joint['limits'][0]<joint['limits'][1],'invalid limits')
            require(joint.get('return_mode') in ('spring','hold'),'unknown return mode')
            if joint['kind']=='revolute':require(vector(joint.get('pivot')),'revolute joint needs explicit pivot')
        for name,zone in zones.items():
            require(zone.get('frame')=='gun','prototype requires gun-space zones')
            require(vector(zone.get('position')),'invalid zone position')
            require(vector(zone.get('size')) and all(.01<=x<=1.5 for x in zone['size']),'invalid zone size')
            require(vector(zone.get('color')) and all(type(x)==int and 0<=x<=255 for x in zone['color']),'invalid color')
            require(zone.get('part') in parts,'unknown zone part')
            if 'follow_joint' in zone:require(zone['follow_joint'] in joints,'unknown following joint')
        for name,binding in gun.get('bindings',{}).items():
            require(binding.get('zone') in zones,'unknown binding zone')
            if binding.get('gesture')=='grab_joint':
                require(binding.get('joint') in joints,'unknown binding joint')
                require(binding.get('rear_command')=='action.rear' and binding.get('forward_command')=='action.forward','unknown endpoint command')
            elif binding.get('gesture')=='magazine_transfer':
                require(binding.get('part')=='magazine' and binding.get('command')=='magazine.transfer','invalid magazine binding')
            else:raise ProfileError('unknown gesture')
        for pose in gun.get('poses',[]):
            require(pose.get('state')=='action.locked_open','unknown persistent state')
            require(pose.get('joint') in joints and pose.get('at') in ('min','max'),'invalid state pose')
    return doc

if __name__=='__main__':
    for file in sys.argv[1:]:
        with Path(file).open('rb') as stream:validate(tomllib.load(stream))
        print('Schema OK:',file)
