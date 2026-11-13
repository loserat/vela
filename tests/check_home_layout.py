#!/usr/bin/env python3
"""Verify every six-tile permutation without an Android SDK or a TV."""
from pathlib import Path
import os,shutil,subprocess,tempfile,sys
root=Path(__file__).resolve().parent.parent
java_home=os.environ.get('JAVA_HOME')
if not java_home and sys.platform=='darwin':java_home=subprocess.check_output(['/usr/libexec/java_home','-v','21'],text=True).strip()
java=Path(java_home)/'bin' if java_home else Path(shutil.which('javac')).resolve().parent
with tempfile.TemporaryDirectory(prefix='atpd-home-layout-') as output:
 subprocess.run([str(java/'javac'),'-encoding','UTF-8','-d',output,str(root/'app/src/de/leisetv/HomeLayout.java'),str(root/'tests/java/de/leisetv/HomeLayoutChecks.java')],check=True)
 subprocess.run([str(java/'java'),'-cp',output,'de.leisetv.HomeLayoutChecks'],check=True)
