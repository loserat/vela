#!/usr/bin/env python3
"""Exercise production Ambilight state and HTTP commands against a loopback-only fixture."""
from pathlib import Path
import hashlib
import os
import shutil
import subprocess
import sys
import tempfile

root = Path(__file__).resolve().parent.parent
dependency = root / 'test-results/json-20240303.jar'
digest = '3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed'
if not dependency.exists():
    dependency.parent.mkdir(exist_ok=True)
    subprocess.run(['curl', '--fail', '--location', '--max-time', '30', '--output', str(dependency),
                    'https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar'], check=True)
if hashlib.sha256(dependency.read_bytes()).hexdigest() != digest:
    raise SystemExit('JSON-java test dependency checksum mismatch')
java_home = os.environ.get('JAVA_HOME')
if not java_home and sys.platform == 'darwin':
    java_home = subprocess.check_output(['/usr/libexec/java_home', '-v', '21'], text=True).strip()
if not java_home:
    java_home = str(Path(shutil.which('javac')).resolve().parent.parent)
java = Path(java_home) / 'bin'
with tempfile.TemporaryDirectory(prefix='atpd-ambilight-') as output:
    subprocess.run([str(java / 'javac'), '-encoding', 'UTF-8', '-classpath', str(dependency), '-d', output,
                    str(root / 'app/src/de/leisetv/AmbilightController.java'),
                    str(root / 'app/src/de/leisetv/AmbilightPalette.java'),
                    str(root / 'tests/java/de/leisetv/AmbilightChecks.java')], check=True)
    subprocess.run([str(java / 'java'), '-cp', output + os.pathsep + str(dependency),
                    'de.leisetv.AmbilightChecks'], check=True)
