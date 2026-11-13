#!/usr/bin/env python3
"""Repackage unchanged app classes with a pinned VLC ABI for disposable-emulator tests."""
from pathlib import Path
import argparse,os,subprocess,tempfile,zipfile,xml.etree.ElementTree as ET,sys
root=Path(__file__).resolve().parents[1];version=ET.parse(root/'app/AndroidManifest.xml').getroot().get('{http://schemas.android.com/apk/res/android}versionName')
parser=argparse.ArgumentParser();parser.add_argument('--apk',type=Path,default=root/'dist'/('TV-Dashboard-'+version+'.apk'));parser.add_argument('--abi',default='arm64-v8a',choices=['arm64-v8a','x86_64']);parser.add_argument('--output',type=Path,default=root/'test-results/emulator.apk');args=parser.parse_args();args.output.parent.mkdir(parents=True,exist_ok=True)
sdk=Path(os.environ.get('ANDROID_HOME',os.environ.get('ANDROID_SDK_ROOT',str(Path.home()/('Library/Android/sdk' if sys.platform=='darwin' else 'Android/Sdk')))));tools=sdk/'build-tools/36.1.0';env=dict(os.environ,NICKOS_SIGN_PASSWORD=os.environ.get('NICKOS_SIGN_PASSWORD','nickos-local-test'))
with tempfile.TemporaryDirectory(prefix='atpd-abi-') as tmp:
 unsigned=Path(tmp)/'unsigned.apk';aligned=Path(tmp)/'aligned.apk';count=0
 with zipfile.ZipFile(args.apk) as original,zipfile.ZipFile(unsigned,'w',zipfile.ZIP_DEFLATED) as out:
  for entry in original.infolist():
   if not entry.filename.startswith(('lib/','META-INF/')):out.writestr(entry,original.read(entry.filename))
  with zipfile.ZipFile(root/'app/deps/libvlc-all-3.7.7.aar') as vlc:
   for name in vlc.namelist():
    if name.startswith('jni/'+args.abi+'/') and name.endswith('.so'):out.writestr('lib/'+name[4:],vlc.read(name));count+=1
 assert count,'Requested ABI missing from pinned AAR'
 subprocess.run([str(tools/'zipalign'),'-f','4',str(unsigned),str(aligned)],check=True)
 subprocess.run([str(tools/'apksigner'),'sign','--ks',os.environ.get('NICKOS_KEYSTORE',str(root/'app/local-test-signing.p12')),'--ks-key-alias','leisetv','--ks-pass','env:NICKOS_SIGN_PASSWORD','--out',str(args.output),str(aligned)],env=env,check=True)
 subprocess.run([str(tools/'apksigner'),'verify',str(args.output)],check=True)
print('Test-only emulator package:',args.output,'; application classes unchanged.')
