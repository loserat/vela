#!/usr/bin/env python3
"""Regression: overlapping a full color command must not leave a phantom slider value."""
from pathlib import Path
exec((Path(__file__).with_name('hue_ui.py')).read_text().split('try:\n fixture(')[0])
try:
 fixture({'delay':0,'failure':False,'staleReads':False,'scenesEnabled':False,'action':{'on':True,'bri':191,'hue':59110,'sat':165,'colormode':'hs'}});open_hue();key(22);t=ui();assert find('Helligkeit',t).get('focused')=='true';baseline=Image.open(io.BytesIO(call('exec-out','screencap','-p'))).convert('RGB')
 # Go from the slider through Refresh/Setup and the left grid column to Blue.
 key(19);key(21);key(20);key(20);key(20);key(20);key(22);t=ui();assert find('Blau',t).get('focused')=='true'
 fixture({'delay':2.5});before=fixture();key(23);key(22);key(22);key(22);key(21);time.sleep(3);t=ui();after=Image.open(io.BytesIO(call('exec-out','screencap','-p'))).convert('RGB')
 check('Color command plus early slider input leaves confirmed slider pixels unchanged',ImageChops.difference(baseline.crop((1224,654,1824,816)),after.crop((1224,654,1824,816))).getbbox() is None and find('Helligkeit',t).get('focused')=='true')
 writes=fixture()['writes'][len(before['writes']):];check('Busy slider input sends no unintended brightness command',len(writes)==1 and not any('bri' in item for item in writes))
finally:
 fixture({'delay':0});(args.output/'hue-busy-verification.json').write_text(json.dumps({'target':'isolated emulator and delayed HTTPS fixture','tests':tests},indent=2))
