#!/usr/bin/env python3
"""Fetch the pinned public VLC SDK; contains no runtime/user configuration."""
from pathlib import Path
import subprocess,hashlib,zipfile
ROOT=Path(__file__).resolve().parent
deps=ROOT/'deps';deps.mkdir(exist_ok=True)
aar=deps/'libvlc-all-3.7.7.aar'
url='https://repo.maven.apache.org/maven2/org/videolan/android/libvlc-all/3.7.7/libvlc-all-3.7.7.aar'
expected='b48dab96e0e90e34cce3861963500c144a5aadb1b249d501d6a4b104d849e61b'
if not aar.exists():subprocess.run(['curl','--fail','--location','--output',str(aar),url],check=True)
if hashlib.sha256(aar.read_bytes()).hexdigest()!=expected:raise SystemExit('VLC archive checksum mismatch')
with zipfile.ZipFile(aar) as z:
    for name in z.namelist():
        if name in ['classes.jar','AndroidManifest.xml','R.txt'] or name.startswith(('res/','jni/armeabi-v7a/')):
            z.extract(name,deps/'vlc')
print('Pinned VLC SDK 3.7.7 ready (armeabi-v7a).')
