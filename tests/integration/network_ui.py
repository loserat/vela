#!/usr/bin/env python3
"""Passive connectivity page smoke checks on an explicitly selected disposable emulator."""
from pathlib import Path
import argparse,os,shutil,subprocess,json,re,xml.etree.ElementTree as ET
p=argparse.ArgumentParser();p.add_argument('--serial',required=True);p.add_argument('--output',type=Path,default=Path('test-results'));args=p.parse_args();args.output.mkdir(parents=True,exist_ok=True)
A=[os.environ.get('ADB') or shutil.which('adb') or str(Path.home()/'Library/Android/sdk/platform-tools/adb'),'-s',args.serial];checks=[]
def call(*a):return subprocess.check_output(A+list(a),timeout=30)
def shell(*a):return call('shell',*a)
assert shell('getprop','ro.kernel.qemu').strip()==b'1','Disposable emulator only'
def ui():shell('uiautomator','dump','/sdcard/network-check.xml');return ET.fromstring(call('exec-out','cat','/sdcard/network-check.xml'))
def desc(n):return n.get('content-desc') or n.get('text','')
def check(name,value):checks.append({'name':name,'passed':bool(value)});print(('PASS ' if value else 'FAIL ')+name,flush=True);assert value,name
try:
 shell('am','start','-W','--activity-clear-top','-n','de.leisetv/.MainActivity','-a','android.intent.action.MAIN','-c','android.intent.category.HOME');tree=ui();target=next(n for n in tree.iter('node') if 'Internet' in desc(n) and n.get('clickable')=='true');check('Home exposes a selectable passive network summary',target is not None)
 a,b,c,d=map(int,re.findall(r'\d+',target.get('bounds')));shell('input','tap',str((a+c)//2),str((b+d)//2));tree=ui();text=[desc(n) for n in tree.iter('node')];check('Network page identifies Android as source and limits UniFi claim',any(t.startswith('Netzwerk:') and 'Quelle Android, keine UniFi-Gesamtprüfung' in t for t in text))
 controls=[n for n in tree.iter('node') if n.get('clickable')=='true'];check('Network page controls fit landscape display',all(all(0<=v<=(1920 if i%2==0 else 1080) for i,v in enumerate(map(int,re.findall(r'\d+',n.get('bounds'))))) for n in controls))
 reload=next(n for n in controls if desc(n)=='Aktualisieren');check('Refresh is initially focused and settings remain explicit',reload.get('focused')=='true' and any(t=='Netzwerk-Einstellungen' for t in text))
 shell('input','keyevent','23');tree=ui();check('Passive refresh retains focus without launching settings',any(desc(n)=='Aktualisieren' and n.get('focused')=='true' for n in tree.iter('node')))
 (args.output/'network.png').write_bytes(call('exec-out','screencap','-p'));shell('input','keyevent','4');tree=ui();check('Back returns to dashboard Home',any(desc(n).startswith('Kameras') for n in tree.iter('node')))
finally:(args.output/'network-verification.json').write_text(json.dumps({'target':'disposable emulator Android connectivity, no router/cloud/probe requests or network setting writes','tests':checks},indent=2))
