#!/usr/bin/env python3
"""Install a same-key test probe into an explicitly selected disposable AOSP emulator."""
from pathlib import Path
import argparse,subprocess,os,shutil,tempfile,zipfile,sys,json,re
parser=argparse.ArgumentParser();parser.add_argument('--serial',required=True);parser.add_argument('--cameras',type=int,default=8);parser.add_argument('--task',default='prepare',choices=['prepare','cameras','hide-camera','show-camera','devices','unifi']);parser.add_argument('--report',type=Path);args=parser.parse_args()
root=Path(__file__).resolve().parents[2];sdk=Path(os.environ.get('ANDROID_HOME',os.environ.get('ANDROID_SDK_ROOT',str(Path.home()/('Library/Android/sdk' if sys.platform=='darwin' else 'Android/Sdk')))));adb=[str(sdk/'platform-tools/adb'),'-s',args.serial]
assert subprocess.check_output(adb+['shell','getprop','ro.kernel.qemu'],text=True).strip()=='1','Refusing non-emulator device'
java_home=os.environ.get('JAVA_HOME')
if not java_home and sys.platform=='darwin':java_home=subprocess.check_output(['/usr/libexec/java_home','-v','21'],text=True).strip()
java=Path(java_home) if java_home else Path(shutil.which('javac')).resolve().parent.parent
env=dict(os.environ,JAVA_HOME=str(java),NICKOS_SIGN_PASSWORD=os.environ.get('NICKOS_SIGN_PASSWORD','nickos-local-test'));tools=sdk/'build-tools/36.1.0';jar=sdk/'platforms/android-34/android.jar'
def run(*values):return subprocess.run(list(map(str,values)),check=True,env=env,stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True)
with tempfile.TemporaryDirectory(prefix='atpd-probe-') as tmp:
 t=Path(tmp);(t/'classes').mkdir();(t/'dex').mkdir();manifest=t/'AndroidManifest.xml';manifest.write_text('<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="fixture.atpd"><uses-sdk android:minSdkVersion="26" android:targetSdkVersion="28"/><application android:label="Disposable test fixture"/><instrumentation android:name="fixture.FixtureProbe" android:targetPackage="de.leisetv"/></manifest>')
 run(java/'bin/javac','--release','8','-classpath',jar,'-d',t/'classes',root/'tests/integration/FixtureProbe.java');run(tools/'d8','--min-api','26','--lib',jar,'--output',t/'dex',*list((t/'classes').rglob('*.class')));run(tools/'aapt','package','-f','-M',manifest,'-I',jar,'-F',t/'probe.apk')
 with zipfile.ZipFile(t/'probe.apk','a') as archive:archive.write(t/'dex/classes.dex','classes.dex')
 run(tools/'apksigner','sign','--ks',Path(os.environ.get('NICKOS_KEYSTORE',str(root/'app/local-test-signing.p12'))),'--ks-key-alias','leisetv','--ks-pass','env:NICKOS_SIGN_PASSWORD',t/'probe.apk');run(*adb,'install','-r',t/'probe.apk')
 output=run(*adb,'shell','am','instrument','-w','-e','task',args.task,'-e','cameras',str(args.cameras),'fixture.atpd/fixture.FixtureProbe').stdout
 if args.report:
  match=re.search(r'INSTRUMENTATION_RESULT: checks=(.*)',output);args.report.parent.mkdir(parents=True,exist_ok=True);args.report.write_text(json.dumps({'target':'disposable emulator and loopback UniFi HTTPS fixture','tests':json.loads(match.group(1)) if match else []},indent=2))
 assert 'fixture=ready' in output and 'INSTRUMENTATION_CODE: -1' in output,output
print('Disposable fixture prepared:',args.task,'; no real camera streams.')
