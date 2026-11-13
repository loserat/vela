#!/usr/bin/env python3
"""Navigation/gallery smoke suite. Requires an explicitly selected disposable emulator."""
from pathlib import Path
import argparse,os,shutil,subprocess,time,re,json,xml.etree.ElementTree as ET
parser=argparse.ArgumentParser();parser.add_argument('--serial',required=True);parser.add_argument('--output',type=Path,default=Path('test-results'));args=parser.parse_args();args.output.mkdir(parents=True,exist_ok=True)
A=[os.environ.get('ADB') or shutil.which('adb') or str(Path.home()/'Library/Android/sdk/platform-tools/adb'),'-s',args.serial];checks=[]
def call(*a):return subprocess.check_output(A+list(a),timeout=30)
def shell(*a):return call('shell',*a)
assert shell('getprop','ro.kernel.qemu').strip()==b'1','Disposable emulator only'
def ui():shell('uiautomator','dump','/sdcard/atpd-gui-check.xml');return ET.fromstring(call('exec-out','cat','/sdcard/atpd-gui-check.xml'))
def desc(n):return n.get('content-desc') or n.get('text','')
def find(prefix,t=None):return next(n for n in (t if t is not None else ui()).iter('node') if desc(n).startswith(prefix))
def tap(prefix):
 n=find(prefix);a,b,c,d=map(int,re.findall(r'\d+',n.get('bounds')));assert c>a and d>b;shell('input','tap',str((a+c)//2),str((b+d)//2));return ui()
def key(n):shell('input','keyevent',str(n))
def check(n,v):checks.append({'name':n,'passed':bool(v)});print(('PASS ' if v else 'FAIL ')+n,flush=True);assert v,n
def home():shell('am','start','-W','--activity-clear-top','-n','de.leisetv/.MainActivity','-a','android.intent.action.MAIN','-c','android.intent.category.HOME');return ui()
def screenshot(name):
 t=ui();clickable=[n for n in t.iter('node') if n.get('clickable')=='true'];check(name+' controls fit landscape display',all(all(0<=v<=(1920 if i%2==0 else 1080) for i,v in enumerate(map(int,re.findall(r'\d+',n.get('bounds',''))))) for n in clickable));(args.output/(name+'.png')).write_bytes(call('exec-out','screencap','-p'));return t
try:
 t=home();check('Home exposes cameras light controls TV devices and system',all(find(s,t) is not None for s in ['Kameras','Ambilight','Hue','Fernsehen','Geräte','System']));screenshot('home')
 tap('System');t=screenshot('system');check('System exposes six functions',all(find(s,t) is not None for s in ['Web-Einstellungen','Bildschirmschoner','Apps','Fernsehen','TV-Einstellungen','Android-Startseite']))
 tap('Web-Einstellungen');t=ui();check('Web access screen exposes local address and access code',any(desc(n).startswith('https://') and 'Zugangscode' in desc(n) for n in t.iter('node')));key(4);ui()
 tap('Apps');t=screenshot('apps');check('Apps page has a reachable Back control',find('← Zurück',t) is not None);key(4);t=ui();check('Back from Apps returns to System',find('Bildschirmschoner',t) is not None);key(4);ui()
 tap('Kameras');t=screenshot('cameras');check('Camera page shows six of eight fixture sources',all(find(s,t) is not None for s in ['Eingang','Garten','Garage','Terrasse','Werkstatt','Hof']) and not any(desc(n).startswith('Flur') for n in t.iter('node')))
 tap('Weitere →');t=ui();check('Next camera page shows remaining two sources',find('Flur',t) is not None and find('Gästehaus',t) is not None and not any(desc(n).startswith('Eingang') for n in t.iter('node')));check('Last camera page disables Next',find('Weitere →',t).get('enabled')=='false')
 tap('Einrichten');ui();key(4);t=ui();check('Camera page survives settings return',find('Flur',t) is not None and not any(desc(n).startswith('Eingang') for n in t.iter('node')));tap('← Vorherige');check('Previous camera page restores first six sources',find('Eingang') is not None);key(4);ui()
 tap('Geräte');t=ui();check('Devices entry stays on selector without switching',find('Geräte,',t) is not None);check('No-input emulator has an honest empty state',find('Keine TV-Eingänge gemeldet',t) is not None);key(4);ui()
 tap('System');tap('Bildschirmschoner');t=ui();check('Screensaver styles and preview are accessible',all(find(s,t) is not None for s in ['Uhr','Farbfläche','Vorschau starten','Aktivierung & Wartezeit']))
 tap('Farbfläche');t=ui();check('Color screensaver selected and date disabled',', ausgewählt' in desc(find('Farbfläche',t)) and find('Datum',t).get('enabled')=='false');tap('Uhr');t=screenshot('screensaver');check('Clock screensaver restores date control',find('Datum',t).get('enabled')=='true')
 tap('Vorschau starten');t=ui();check('Full-screen screensaver preview opens',find('Bildschirmschoner-Vorschau',t) is not None and not any(desc(n).startswith('Vorschau starten') for n in t.iter('node')));key(4);t=ui();check('Back leaves preview on screensaver settings',find('Vorschau starten',t) is not None);home()
 tap('Ambilight');t=ui();check('Ambilight fixed-color controls remain available',find('Rosa',t).get('enabled')=='true');tap('Rosa');screenshot('ambilight');key(4);ui()
 tap('Hue');deadline=time.monotonic()+10
 while time.monotonic()<deadline:
  t=ui()
  if any(desc(n).startswith('Hue, TV-Zimmer') for n in t.iter('node')):break
 check('Hue retains fixture bridge and room',find('Hue, TV-Zimmer',t) is not None);screenshot('hue')
 check('Gallery contains no project branding in native UI',not any(any(brand in desc(n) for brand in ['ATPD','NickOS','VELA']) for n in t.iter('node')))
finally:(args.output/'gui-verification.json').write_text(json.dumps({'target':'isolated emulator with synthetic cameras and light fixtures; no real video or HDMI switching','tests':checks},indent=2))
