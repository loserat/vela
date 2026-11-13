#!/usr/bin/env python3
"""Emulator-only dream/prefs test. Original Android activation values are restored."""
from pathlib import Path
import subprocess,time,json,xml.etree.ElementTree as ET,re
import argparse,os,shutil
parser=argparse.ArgumentParser();parser.add_argument('--serial',required=True);parser.add_argument('--output',type=Path,default=Path('test-results'));args=parser.parse_args();args.output.mkdir(parents=True,exist_ok=True)
A=[os.environ.get('ADB') or shutil.which('adb') or str(Path.home()/'Library/Android/sdk/platform-tools/adb'),'-s',args.serial]
def shell(*args):return subprocess.check_output(A+['shell',*args],text=True).strip()
assert shell('getprop','ro.kernel.qemu')=='1'
checks=[]
def check(name,condition):checks.append({'name':name,'passed':bool(condition)});print(('PASS ' if condition else 'FAIL ')+name,flush=True);assert condition,name
keys=['screensaver_components','screensaver_enabled','screensaver_activate_on_sleep','screensaver_activate_on_dock'];before={k:shell('settings','get','secure',k) for k in keys}
def tree():
 shell('uiautomator','dump','/sdcard/atpd-ui.xml');return ET.fromstring(subprocess.check_output(A+['exec-out','cat','/sdcard/atpd-ui.xml']))
def tap(label):
 n=next(n for n in tree().iter('node') if (n.get('content-desc') or n.get('text','')).split(',')[0]==label);v=list(map(int,re.findall(r'\d+',n.get('bounds'))));shell('input','tap',str((v[0]+v[2])//2),str((v[1]+v[3])//2));time.sleep(.25)
def prefs():
 t=ET.fromstring(subprocess.check_output(A+['exec-out','cat','/data/user/0/de.leisetv/shared_prefs/nickos.xml']));return {n.get('name'):n.text if n.tag=='string' else n.get('value') for n in t}
def foreground():return '\n'.join(x for x in shell('dumpsys','activity','activities').splitlines() if 'mResumedActivity' in x or 'topResumedActivity' in x)
try:
 shell('am','start','-W','--activity-clear-top','-n','de.leisetv/.ScreensaverSettingsActivity');time.sleep(.4)

 if prefs().get('saver_date')=='false':tap('Datum aus')
 if prefs().get('saver_dim')=='false':tap('Normale Darstellung')
 check('Settings leaves Android activation unchanged',before=={k:shell('settings','get','secure',k) for k in keys})
 tap('Farbfläche');check('Color style persists',prefs().get('saver_style')=='color')
 tap('Uhr');tap('Datum an');check('Date toggle persists',prefs().get('saver_style')=='clock' and prefs().get('saver_date')=='false')
 tap('Dunkle Darstellung');check('Dim toggle persists',prefs().get('saver_dim')=='false')
 shell('am','start','-W','--activity-clear-top','-n','de.leisetv/.MainActivity');shell('am','start','-W','--activity-clear-top','-n','de.leisetv/.ScreensaverSettingsActivity');time.sleep(.3)
 labels=[n.get('content-desc') or n.get('text','') for n in tree().iter('node')];check('Options restored on reopen','Datum aus' in labels and 'Normale Darstellung' in labels)
 tap('Vorschau starten');time.sleep(.4);check('Full screen preview opens','ScreensaverPreviewActivity' in foreground());shell('input','keyevent','4');time.sleep(.4);check('Back returns to settings','ScreensaverSettingsActivity' in foreground())
 check('Preview does not change Android activation',before=={k:shell('settings','get','secure',k) for k in keys})
 tap('Aktivierung & Wartezeit');time.sleep(.5);check('System screensaver settings opens','de.leisetv/.ScreensaverSettingsActivity' not in foreground() and 'settings' in foreground().lower())
 shell('settings','put','secure','screensaver_components','de.leisetv/.DashboardDreamService');shell('settings','put','secure','screensaver_enabled','1');time.sleep(.8);shell('cmd','dreams','start-dreaming');time.sleep(1.5)
 dump=shell('dumpsys','dreams');check('Android actually starts registered dream','de.leisetv/de.leisetv.DashboardDreamService' in dump and ('mIsDreaming=true' in dump or 'mCurrentDream=' in dump))
 shell('cmd','dreams','stop-dreaming');shell('am','start','-W','--activity-clear-top','-n','de.leisetv/.ScreensaverSettingsActivity');time.sleep(.2);tap('Datum aus');tap('Normale Darstellung')
finally:
 shell('cmd','dreams','stop-dreaming')
 for k,v in before.items():shell('settings','delete','secure',k) if v=='null' else shell('settings','put','secure',k,v)
 check('Emulator system activation restored',before=={k:shell('settings','get','secure',k) for k in keys})
 (args.output/'screensaver-verification.json').write_text(json.dumps({'target':'isolated Android emulator; no real TV activation changed','tests':checks},indent=2))
