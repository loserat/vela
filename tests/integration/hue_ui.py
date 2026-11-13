#!/usr/bin/env python3
"""Emulator-only UI regression suite; never use against a real Hue Bridge."""
from pathlib import Path
import subprocess,time,xml.etree.ElementTree as ET,re,json,urllib.request,ssl,io
from PIL import Image,ImageChops
import argparse,os,shutil,urllib.parse
parser=argparse.ArgumentParser();parser.add_argument('--serial',required=True);parser.add_argument('--fixture',default='https://127.0.0.1:8443');parser.add_argument('--output',type=Path,default=Path('test-results'));args=parser.parse_args()
assert urllib.parse.urlparse(args.fixture).hostname in ['127.0.0.1','localhost'],'Loopback fixture only'
adb=os.environ.get('ADB') or shutil.which('adb') or str(Path.home()/'Library/Android/sdk/platform-tools/adb');A=[adb,'-s',args.serial];tests=[];args.output.mkdir(parents=True,exist_ok=True)
def call(*a):return subprocess.check_output(A+list(a),timeout=25)
def shell(*a):return call('shell',*a)
assert shell('getprop','ro.kernel.qemu').strip()==b'1'
def key(n):shell('input','keyevent',str(n))
def ui():shell('uiautomator','dump','/sdcard/hue-061-check.xml');return ET.fromstring(call('exec-out','cat','/sdcard/hue-061-check.xml'))
def desc(n):return n.get('content-desc') or n.get('text','')
def find(prefix,t=None):return next(n for n in (t if t is not None else ui()).iter('node') if desc(n).startswith(prefix))
def tap(prefix):
 n=find(prefix);a,b,c,d=map(int,re.findall(r'\d+',n.get('bounds')));shell('input','tap',str((a+c)//2),str((b+d)//2))
def fixture(body=None):
 req=urllib.request.Request(args.fixture+'/' +('test/state' if body is None else 'test/control'),data=None if body is None else json.dumps(body).encode(),headers={} if body is None else {'Content-Type':'application/json'})
 with urllib.request.urlopen(req,context=ssl._create_unverified_context(),timeout=5) as r:return json.load(r)
def check(name,value):tests.append({'name':name,'passed':bool(value)});print(('PASS ' if value else 'FAIL ')+name,flush=True);assert value,name
def open_hue():
 shell('am','start','-W','--activity-clear-top','-n','de.leisetv/.MainActivity','-a','android.intent.action.MAIN','-c','android.intent.category.HOME');ui();key(21);key(22);key(22);key(23)
 deadline=time.monotonic()+10
 while time.monotonic()<deadline:
  t=ui()
  if any(', ausgewählt' in desc(n) and n.get('focused')=='true' for n in t.iter('node')):return
 raise AssertionError('Hue did not finish state read and restore remote focus')
try:
 fixture({'delay':0,'failure':False,'scenesEnabled':False,'action':{'on':True,'bri':191,'hue':59110,'sat':165,'colormode':'hs'}});before=fixture();open_hue();t=ui();check('Normal Home remote route opens Hue without light writes',find('Hue, TV-Zimmer',t) is not None and fixture()['writes']==before['writes'])
 names=['Warmweiß','Neutralweiß','Kaltweiß','Kerze','Rot','Orange','Amber','Gelb','Limette','Grün','Minze','Türkis','Cyan','Blau','Violett','Rosa']
 check('All sixteen colors and slider visible without scrolling',all(find(n,t) is not None for n in names) and find('Helligkeit',t) is not None and not any(n.get('class')=='android.widget.ScrollView' for n in t.iter('node')))
 check('Setup forms absent from daily view',not any(n.get('class') in ['android.widget.EditText','android.widget.Spinner'] for n in t.iter('node')))
 check('No empty scene picker',not any(desc(n)=='Szenen' for n in t.iter('node')))
 check('Preset selection is separate from remote focus',find('Rosa, ausgewählt',t).get('focused')=='true')
 tap('Raum & Bridge');t=ui();check('Setup isolated in dialog',any(n.get('class')=='android.widget.EditText' for n in t.iter('node')) and any(desc(n)=='Diesen Raum verwenden' for n in t.iter('node')));key(4);check('Closing setup sends no light or registration commands',fixture()['writes']==before['writes'] and fixture()['registrations']==before['registrations'])
 tap('Blau');t=ui();s=fixture();check('Blue command preserves room scope and correct Hue scale',s['action']['hue']==43690 and s['action']['sat']==254 and s['action']['on'] and s['outsideWrites']==0);check('Acknowledged selection displayed',find('Blau, ausgewählt',t) is not None)
 # Restore remote focus using normal Home navigation, then move from Blue across the grid to the slider.
 open_hue();key(22);key(22);key(22);check('Remote reaches brightness slider',find('Helligkeit').get('focused')=='true')
 fixture({'delay':1.2,'staleReads':True});before=fixture();baseline=Image.open(io.BytesIO(call('exec-out','screencap','-p'))).convert('RGB');key(21);pending=Image.open(io.BytesIO(call('exec-out','screencap','-p'))).convert('RGB')
 regions=[(0,0,1920,654),(0,816,1920,1080),(0,654,1224,816),(1824,654,1920,816)]
 check('Brightness input leaves other pixels unchanged',all(ImageChops.difference(baseline.crop(b),pending.crop(b)).getbbox() is None for b in regions));t=ui();check('Slider retains focus and enabled colors',find('Helligkeit',t).get('focused')=='true' and find('Rosa',t).get('enabled')=='true');s=fixture();check('Brightness does not fetch groups lights or scenes',len(s['reads'])==len(before['reads']));check('Acknowledged target does not snap to stale bridge reads',find('Helligkeit').get('content-desc','').startswith('Helligkeit 70 Prozent'));check('Brightness preserves hue and saturation',s['action']['hue']==43690 and s['action']['sat']==254 and 1<=s['action']['bri']<=254)
 fixture({'delay':0,'staleReads':False});before=fixture();shell('input','swipe','1280','742','1740','742','600');ui();check('Touch dragging sends one final brightness command',len(fixture()['writes'])==len(before['writes'])+1)
 fixture({'failure':True});key(21);t=ui();check('Brightness failure keeps slider focus and stable enabled controls',find('Helligkeit',t).get('focused')=='true' and find('Blau',t).get('enabled')=='true');check('Brightness failure reports unconfirmed change',any('Helligkeit nicht bestätigt' in desc(n) for n in t.iter('node')));fixture({'failure':False});key(21);t=ui();check('Brightness retries without a page reload',not any('Helligkeit nicht bestätigt' in desc(n) for n in t.iter('node')))
 fixture({'action':{'colormode':'ct','ct':450,'hue':43690,'sat':254}});tap('Aktualisieren');t=ui();check('CT state selects white instead of stale blue hue',find('Warmweiß, ausgewählt',t) is not None and not any(desc(n)=='Blau, ausgewählt' for n in t.iter('node')))
 fixture({'action':{'colormode':'xy','hue':0,'sat':254}});tap('Aktualisieren');t=ui();check('XY mode does not falsely select stale hue',not any(', ausgewählt' in desc(n) for n in t.iter('node')))
 fixture({'scenesEnabled':True});tap('Aktualisieren');tap('Szenen');t=ui();check('Only selected room scenes are listed',any(desc(n)=='Lesen' for n in t.iter('node')) and not any('Andere Szene' in desc(n) for n in t.iter('node')));tap('Lesen');ui();check('Selected scene applies only to chosen room',fixture()['action'].get('scene')=='scene1' and fixture()['outsideWrites']==0)
 fixture({'failure':True});tap('Aktualisieren');t=ui();check('Refresh failure retains last confirmed view and reports error',find('Blau',t).get('enabled')=='true' and any('Keine Verbindung' in desc(n) for n in t.iter('node')));fixture({'failure':False});tap('Aktualisieren');check('Refresh restores controls',find('Blau').get('enabled')=='true')
 # Return fixture to presentation data without issuing any light command.
 fixture({'scenesEnabled':False,'action':{'on':True,'bri':191,'hue':59110,'sat':165,'colormode':'hs'}});tap('Aktualisieren');t=ui();check('No project branding on page',not any(any(b in desc(n) for b in ['ATPD','NickOS','VELA']) for n in t.iter('node')));(args.output/'hue.png').write_bytes(call('exec-out','screencap','-p'))
finally:(args.output/'hue-verification.json').write_text(json.dumps({'target':'isolated Android14 emulator and local HTTPS Hue fixture; no real light commands','tests':tests},indent=2))
