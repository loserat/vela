#!/usr/bin/env python3
"""Check publication rules with Git itself; never create or read local secrets."""
from pathlib import Path
import os
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def git(*args, data=None):
    return subprocess.run(
        ['git', '-c', 'core.ignorecase=false', '-c', f'core.excludesFile={os.devnull}', '-C', str(ROOT), *args], input=data,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False,
    )


def ignored(paths):
    result = git('check-ignore', '--no-index', '-z', '--stdin',
                 data=('\0'.join(paths) + '\0').encode())
    assert result.returncode in (0, 1), result.stderr.decode()
    return set(result.stdout.decode().strip('\0').split('\0')) - {''}


cases = {
    'app/build/classes/Example.class': True,
    'app/deps/libvlc.aar': True,
    'dist/TV-Dashboard.apk': True,
    'scratch/TV-Dashboard.apk': True,
    'scratch/bundle.aab': True,
    'scratch/bundle.apks': True,
    'scratch/package.apk.idsig': True,
    'app/local-test-signing.p12': True,
    'scratch/signing.pfx': True,
    'scratch/signing.jks': True,
    'scratch/signing.keystore': True,
    'scratch/identity.pem': True,
    'scratch/identity.key': True,
    'scratch/identity.p8': True,
    'scratch/identity.pk8': True,
    '.env': True,
    '.env.local': True,
    'scratch/.env.production': True,
    '.env.example': False,
    'scratch/.env.example': False,
    'scratch/session.cookies': True,
    'scratch/camera.enc': True,
    'scratch/cache.db': True,
    'scratch/cache.db-wal': True,
    'scratch/device.log': True,
    'leise-tv-camera-config.json': True,
    'scratch/leise-tv-camera-config.json': True,
    'hue-bridge-local.json': True,
    'scratch/hue-bridge-local.json': True,
    'work/session.json': True,
    'outputs/diagnostic.json': True,
    'test-results/report.json': True,
    'scratch/device-private.json': True,
    'scratch/__pycache__/module.pyc': True,
    '.DS_Store': True,
    'docs/images/unreviewed.png': True,
    'docs/images/unreviewed.PNG': True,
    'docs/images/unreviewed.JPG': True,
    'docs/images/unreviewed.JpEg': True,
    'app/assets/unreviewed.png': True,
    'README.md': False,
    'docs/ROADMAP.md': False,
    'app/AndroidManifest.xml': False,
    'app/assets/i18n/en.json': False,
    'app/src/de/leisetv/CameraConfig.java': False,
    'tests/check_gitignore.py': False,
}

# The image allowlist stays explicit: every exception must be a tracked image.
allowlist = [line[1:] for line in (ROOT / '.gitignore').read_text().splitlines()
             if line.startswith('!/docs/images/')]
tracked_result = git('ls-files', '-z')
assert tracked_result.returncode == 0, tracked_result.stderr.decode()
tracked = tracked_result.stdout.decode().strip('\0').split('\0')
assert allowlist and len(allowlist) == len(set(allowlist))
for path in allowlist:
    assert not any(char in path for char in '*?['), 'Image exceptions must be exact paths'
    assert path.lstrip('/') in tracked, f'Untracked image exception: {path}'
    cases[path.lstrip('/')] = False

actual = ignored(cases)
for path, should_ignore in cases.items():
    assert (path in actual) == should_ignore, f'Incorrect publication rule: {path}'

# Ignore rules do not protect already tracked files, so inspect the index too.
hidden_tracked = ignored(tracked)
assert not hidden_tracked, f'Tracked files match ignore rules: {sorted(hidden_tracked)}'
tracked_images = {p for p in tracked if Path(p).suffix.lower() in {'.png', '.jpg', '.jpeg'}}
assert tracked_images == {p.lstrip('/') for p in allowlist}, 'Image allowlist/index mismatch'

key_block = re.compile(rb'-----BEGIN (?:RSA |DSA |EC |OPENSSH |ENCRYPTED )?PRIVATE KEY-----')
for path in tracked:
    # Read only version-controlled content, never ignored signing/config files.
    content = git('show', ':' + path)
    assert content.returncode == 0, content.stderr.decode()
    assert not key_block.search(content.stdout), f'Private-key block in tracked file: {path}'

print(f'{len(cases)} publication-rule cases passed; {len(allowlist)} reviewed images preserved.')
print(f'{len(tracked)} index entries checked: no ignored tracked files or private-key blocks.')
