#!/usr/bin/env python3
"""Native inventory/navigation checks against the loopback fixture, disposable emulator only."""
from pathlib import Path
import argparse,subprocess,os,shutil,time,json,re,ssl,urllib.request,xml.etree.ElementTree as ET
p=argparse.ArgumentParser();p.add_argument('--serial',required=True);p.add_argument('--output',type=Path,default=Path('test-results'));args=p.parse_args();args.output.mkdir(parents=True,exist_ok=True)
A=[os.environ.get('ADB') or shutil.which('adb') or str(Path.home()/'Library/Android/sdk/platform-tools/adb'),'-s',args.serial];checks=[]
def call(*a):return subprocess.check_output(A+list(a),timeout=30)
def shell(*a):return call('shell',*a)
assert shell('getprop','ro.kernel.qemu').strip()==b'1','Disposable emulator only'
def ui():shell('uiautomator','dump','/sdcard/unifi-check.xml');return ET.fromstring(call('exec-out','cat','/sdcard/unifi-check.xml'))
def desc(n):return n.get('content-desc') or n.get('text','')
def find(prefix,tree=None):return next(n for n in (tree if tree is not None else ui()).iter('node') if desc(n).startswith(prefix))
def tap(prefix):
 n=find(prefix);a,b,c,d=map(int,re.findall(r'\d+',n.get('bounds')));shell('input','tap',str((a+c)//2),str((b+d)//2));return ui()
def check(name,value):checks.append({'name':name,'passed':bool(value)});print(('PASS ' if value else 'FAIL ')+name,flush=True);assert value,name
def control(fail):
 context=ssl._create_unverified_context();request=urllib.request.Request('https://127.0.0.1:8444/test/control',data=json.dumps({'failure':fail}).encode(),headers={'Content-Type':'application/json'});urllib.request.urlopen(request,context=context,timeout=5).close()
def wait(predicate):
 deadline=time.monotonic()+20
 while time.monotonic()<deadline:
  tree=ui()
  if predicate(tree):return tree
 raise AssertionError('Native inventory did not reach expected state')
try:
 shell('am','start','-W','--activity-clear-top','-n','de.leisetv/.MainActivity','-a','android.intent.action.MAIN','-c','android.intent.category.HOME');tree=ui();badge=find('Netzwerkstatus,',tree);a,b,c,d=map(int,re.findall(r'\d+',badge.get('bounds')));check('Home network indicator is compact and remains selectable',c-a==440 and d-b==64 and badge.get('clickable')=='true');(args.output/'home.png').write_bytes(call('exec-out','screencap','-p'))
 tap('Netzwerkstatus,');tap('UniFi-Geräte');tree=wait(lambda t:any(desc(n).startswith('AP Wohnzimmer,') for n in t.iter('node')));check('Native local inventory displays fixture devices',find('AP Wohnzimmer,',tree) is not None);controls=[n for n in tree.iter('node') if n.get('clickable')=='true'];check('Inventory controls fit TV display',all(all(0<=v<=(1920 if i%2==0 else 1080) for i,v in enumerate(map(int,re.findall(r'\d+',n.get('bounds'))))) for n in controls));check('Unknown device state stays unknown',desc(find('Anderes Gerät,',tree)).endswith('Unbekannt'));(args.output/'unifi.png').write_bytes(call('exec-out','screencap','-p'))
 tree=tap('Weitere →');check('Pagination shows the seventh device with reported Offline state',desc(find('Switch,',tree)).endswith('Offline') and find('Weitere →',tree).get('enabled')=='false');tap('← Vorherige')
 control(True);tap('Aktualisieren');tree=wait(lambda t:any('veraltet, zuletzt' in desc(n) for n in t.iter('node')));check('Provider failure marks retained device statuses stale',any('veraltet, zuletzt' in desc(n) for n in tree.iter('node')))
 control(False);tap('Aktualisieren');tree=wait(lambda t:any(desc(n).startswith('AP Wohnzimmer,') and 'veraltet' not in desc(n) for n in t.iter('node')));check('Manual refresh recovers current statuses',find('AP Wohnzimmer,',tree) is not None)
 shell('input','keyevent','4');tree=ui();check('Back returns to TV network details',find('Netzwerk-Einstellungen',tree) is not None);shell('input','keyevent','4');tree=ui();check('Back returns to Home',find('Kameras',tree) is not None)
 with urllib.request.urlopen('https://127.0.0.1:8444/test/state',context=ssl._create_unverified_context(),timeout=5) as r:state=json.load(r)
 check('Production inventory sends no upstream writes',state['writes']==0 and any('offset=3' in path for path in state['reads']))
finally:
 control(False);(args.output/'unifi-ui-verification.json').write_text(json.dumps({'target':'disposable emulator; synthetic inventory and loopback HTTPS only','tests':checks},indent=2))
