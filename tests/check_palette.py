#!/usr/bin/env python3
"""Run color conversion regressions without an Android device or SDK."""
from pathlib import Path
import os
import shutil
import subprocess
import sys
import tempfile

root = Path(__file__).resolve().parent.parent
java_home = os.environ.get('JAVA_HOME')
if not java_home and sys.platform == 'darwin':
    result = subprocess.run(['/usr/libexec/java_home', '-v', '21'], capture_output=True, text=True)
    if result.returncode == 0:
        java_home = result.stdout.strip()
if not java_home and shutil.which('javac'):
    java_home = str(Path(shutil.which('javac')).resolve().parent.parent)
if not java_home:
    raise SystemExit('Set JAVA_HOME to a JDK installation.')
java = Path(java_home) / 'bin'
with tempfile.TemporaryDirectory(prefix='atpd-palette-') as output:
    subprocess.run([str(java / 'javac'), '-encoding', 'UTF-8', '-d', output,
                    str(root / 'app/src/de/leisetv/AmbilightPalette.java'),
                    str(root / 'tests/java/de/leisetv/PaletteChecks.java')], check=True)
    subprocess.run([str(java / 'java'), '-cp', output, 'de.leisetv.PaletteChecks'], check=True)
