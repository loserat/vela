#!/usr/bin/env python3
"""Emulator-only fixed-color slider regression against the loopback JointSpace fixture."""
from pathlib import Path
import subprocess,xml.etree.ElementTree as ET,re,json,urllib.request
import argparse,os,shutil
parser=argparse.ArgumentParser();parser.add_argument('--serial',required=True);parser.add_argument('--output',type=Path,default=Path('test-results'));args=parser.parse_args();args.output.mkdir(parents=True,exist_ok=True)
adb=[os.environ.get('ADB') or shutil.which('adb') or str(Path.home()/'Library/Android/sdk/platform-tools/adb'),'-s',args.serial]
tests=[]
assert subprocess.check_output(adb+['shell','getprop','ro.kernel.qemu'],text=True).strip()=='1','Disposable emulator only'
def call(*args):return subprocess.check_output(adb+list(args),timeout=20)
def key(value):call('shell','input','keyevent',str(value))
def ui():
    call('shell','uiautomator','dump','/sdcard/atpd-051-check.xml');return ET.fromstring(call('exec-out','cat','/sdcard/atpd-051-check.xml'))
def desc(n):return n.get('content-desc','') or n.get('text','')
def find(prefix):return next(n for n in ui().iter('node') if desc(n).startswith(prefix))
def tap(prefix):
    n=find(prefix);a,b,c,d=map(int,re.findall(r'\d+',n.get('bounds')));call('shell','input','tap',str((a+c)//2),str((b+d)//2))
def state():return json.load(urllib.request.urlopen('http://127.0.0.1:8895/test/state',timeout=2))
def check(name,value):
    tests.append({'name':name,'passed':bool(value)});print(('PASS ' if value else 'FAIL ')+name,flush=True);assert value,name
def fail(value):urllib.request.urlopen(urllib.request.Request('http://127.0.0.1:8895/test/failure',data=json.dumps({'fail':value}).encode(),headers={'Content-Type':'application/json'}),timeout=2).close()
def color():return state()['config']['colorSettings']['color']
def open_light():
    call('shell','am','start','-W','-a','android.intent.action.MAIN','-c','android.intent.category.HOME','-n','de.leisetv/.MainActivity');ui();key(21);key(22);key(23);ui()
try:
    open_light();tap('Blau');find('Blau, ausgewählt')
    call('shell','am','force-stop','de.leisetv');key(21);open_light();find('Blau, ausgewählt');key(22);key(22);key(22)
    check('Slider receives remote focus',find('Helligkeit').get('focused')=='true')
    before=state();read_before=len(before.get('reads',[]));writes_before=len(before['writes'])
    urllib.request.urlopen(urllib.request.Request('http://127.0.0.1:8895/test/delay',data=b'{"seconds":1.6}',headers={'Content-Type':'application/json'}),timeout=4).close()
    import io
    from PIL import Image,ImageChops
    baseline_image=Image.open(io.BytesIO(call('exec-out','screencap','-p'))).convert('RGB')
    key(22)
    pending_image=Image.open(io.BytesIO(call('exec-out','screencap','-p'))).convert('RGB')
    regions=[(0,0,1920,654),(0,816,1920,1080),(0,654,1224,816),(1824,654,1920,816)]
    check('Pixels outside slider remain unchanged during input',all(ImageChops.difference(baseline_image.crop(box),pending_image.crop(box)).getbbox() is None for box in regions))
    middle=ui()
    check('Slider remains focused during delayed request',any(desc(n).startswith('Helligkeit') and n.get('focused')=='true' and n.get('enabled')=='true' for n in middle.iter('node')))
    check('Other color controls stay enabled',any(desc(n).startswith('Rosa') and n.get('enabled')=='true' for n in middle.iter('node')))
    check('Header stays stable, no full-page busy cycle',any(desc(n).startswith('Ambilight, Eingeschaltet, Blau') for n in middle.iter('node')) and not any('Wird übernommen' in desc(n) for n in middle.iter('node')))
    call('shell','input','keyevent','--longpress','22');find('Helligkeit')
    after=state();check('Color bytes preserved during repeated brightness changes',after['config']['colorSettings']['color']['hue']==170 and after['config']['colorSettings']['color']['saturation']==255)
    newwrites=after['writes'][writes_before:];check('Brightness writes are bounded',1<=len(newwrites)<=2)
    newreads=after['reads'][read_before:];check('Brightness only refreshes color configuration',newreads and all(p=='/6/ambilight/currentconfiguration' for p in newreads))
    urllib.request.urlopen(urllib.request.Request('http://127.0.0.1:8895/test/delay',data=b'{"seconds":0}',headers={'Content-Type':'application/json'}),timeout=4).close()
    find('Helligkeit');before=state();call('shell','input','swipe','1280','742','1740','742','600');find('Helligkeit');after=state()
    check('Touch drag sends one final update',len(after['writes'])-len(before['writes'])==1)
    check('Brightness stays within byte bounds',0<=after['config']['colorSettings']['color']['brightness']<=255)
finally:(args.output/'ambilight-verification.json').write_text(json.dumps({'target':'isolated emulator, delayed mock responses','tests':tests,'mockState':state()},indent=2,ensure_ascii=False))
