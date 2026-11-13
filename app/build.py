#!/usr/bin/env python3
"""Build the TV dashboard with the installed Android SDK, JDK and pinned LibVLC AAR."""
from pathlib import Path
import os, shutil, subprocess, sys, zipfile, xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parent
default_sdk=Path.home()/('Library/Android/sdk' if sys.platform=='darwin' else 'Android/Sdk')
SDK=Path(os.environ.get('ANDROID_HOME',os.environ.get('ANDROID_SDK_ROOT',str(default_sdk))))
TOOLS=SDK/'build-tools/36.1.0'
ANDROID=SDK/'platforms/android-34/android.jar'
java_home=os.environ.get('JAVA_HOME')
if not java_home and sys.platform=='darwin':
    result=subprocess.run(['/usr/libexec/java_home','-v','21'],capture_output=True,text=True)
    if result.returncode==0:java_home=result.stdout.strip()
if not java_home and shutil.which('javac'):java_home=str(Path(shutil.which('javac')).resolve().parent.parent)
if not java_home:raise SystemExit('JDK 21 is required. Set JAVA_HOME to its installation directory.')
JAVA=Path(java_home)
BUILD=ROOT/'build'
VLC=ROOT/'deps/vlc'
OUT=ROOT.parent/'dist'
ENV=dict(os.environ,JAVA_HOME=str(JAVA))
def run(*args):
    subprocess.run([str(x) for x in args],check=True,env=ENV,cwd=ROOT)
for d in ['gen','classes','dex']:
    shutil.rmtree(BUILD/d,ignore_errors=True)
    (BUILD/d).mkdir(parents=True,exist_ok=True)
OUT.mkdir(parents=True,exist_ok=True)
for needed in [TOOLS/'aapt',TOOLS/'d8',TOOLS/'apksigner',ANDROID,JAVA/'bin/javac',VLC/'classes.jar']:
    if not needed.exists():raise SystemExit('Missing build dependency: '+str(needed))
run(TOOLS/'aapt','package','-f','--auto-add-overlay','--extra-packages','org.videolan','-M',ROOT/'AndroidManifest.xml','-S',ROOT/'res','-S',VLC/'res','-A',ROOT/'assets','-I',ANDROID,'-J',BUILD/'gen','-F',BUILD/'resources.ap_')
sources=list((ROOT/'src').rglob('*.java'))+list((BUILD/'gen').rglob('*.java'))
run(JAVA/'bin/javac','--release','8','-encoding','UTF-8','-classpath',str(ANDROID)+os.pathsep+str(VLC/'classes.jar'),'-d',BUILD/'classes',*sources)
run(TOOLS/'d8','--min-api','26','--lib',ANDROID,'--output',BUILD/'dex',VLC/'classes.jar',*list((BUILD/'classes').rglob('*.class')))
unsigned=BUILD/'unsigned.apk'
shutil.copyfile(BUILD/'resources.ap_',unsigned)
with zipfile.ZipFile(unsigned,'a',zipfile.ZIP_DEFLATED) as z:
    z.write(BUILD/'dex/classes.dex','classes.dex')
    for native in (VLC/'jni/armeabi-v7a').glob('*.so'):
        z.write(native,'lib/armeabi-v7a/'+native.name)
run(TOOLS/'zipalign','-f','4',unsigned,BUILD/'aligned.apk')
key=Path(os.environ.get('NICKOS_KEYSTORE',str(ROOT/'local-test-signing.p12')))
ENV['NICKOS_SIGN_PASSWORD']=os.environ.get('NICKOS_SIGN_PASSWORD','nickos-local-test')
if not key.exists():
    run(JAVA/'bin/keytool','-genkeypair','-keystore',key,'-storetype','PKCS12','-storepass:env','NICKOS_SIGN_PASSWORD','-keypass:env','NICKOS_SIGN_PASSWORD','-alias','leisetv','-keyalg','RSA','-keysize','2048','-validity','3650','-dname','CN=TV Dashboard Local Test')
    key.chmod(0o600)
version=ET.parse(ROOT/'AndroidManifest.xml').getroot().get('{http://schemas.android.com/apk/res/android}versionName')
apk=OUT/('TV-Dashboard-'+version+'.apk')
run(TOOLS/'apksigner','sign','--ks',key,'--ks-key-alias','leisetv','--ks-pass','env:NICKOS_SIGN_PASSWORD','--out',apk,BUILD/'aligned.apk')
run(TOOLS/'apksigner','verify','--verbose',apk)
print(str(apk)+' ('+str(apk.stat().st_size)+' bytes)')
