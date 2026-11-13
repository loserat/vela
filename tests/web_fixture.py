#!/usr/bin/env python3
"""Loopback-only UI preview with synthetic data. No TV/router/light connections."""
from http.server import ThreadingHTTPServer,BaseHTTPRequestHandler
from pathlib import Path
import json,time,argparse
parser=argparse.ArgumentParser();parser.add_argument('--port',type=int,default=9040);args=parser.parse_args()
page=Path(__file__).resolve().parents[1]/'app/assets/settings/index.html'
state={'version':'0.8.1','device':{'manufacturer':'Philips','model':'TEST','displayName':'Philips Test TV'},'display':{'camera':True,'clock':True,'date':True,'homeTitle':'Zuhause'},'cameras':[{'id':'fixture-1','name':'Eingang','available':True,'host':'Lokale Quelle','status':'ready'},{'id':'fixture-2','name':'Garten','available':False,'status':'missing'}],'defaultCameraId':'fixture-1'}
saver={'style':'clock','date':True,'dim':True}
hue={'configured':True,'connected':True,'host':'10.0.0.2','roomId':'1','roomName':'TV-Zimmer','rooms':[{'id':'1','name':'TV-Zimmer'},{'id':'2','name':'Küche'}]}
class Handler(BaseHTTPRequestHandler):
 def log_message(self,*args):pass
 def send(self,data,status=200):
  body=data if isinstance(data,bytes) else json.dumps(data).encode();self.send_response(status);self.send_header('Content-Type','text/html; charset=utf-8' if isinstance(data,bytes) else 'application/json');self.send_header('Cache-Control','no-store');self.end_headers();self.wfile.write(body)
 def do_GET(self):
  if self.path=='/':return self.send(page.read_bytes())
  if self.path=='/api/state':return self.send(state)
  if self.path=='/api/screensaver/state':return self.send(saver)
  if self.path=='/api/device/state':return self.send({'manufacturer':'Philips','platformModel':'TEST','androidVersion':'8.0.0','apiLevel':26,'securityPatch':'2019-01-01','hardware':'Test hardware','abis':['armeabi-v7a'],'appVersion':'0.8.1','appVersionCode':13,'width':1920,'height':1080,'densityDpi':320,'uptimeMs':7260000,'storageFreeBytes':3000000000,'storageTotalBytes':8000000000,'checkedAt':int(time.time()*1000)})
  if self.path=='/api/device/diagnostic':return self.send({'format':'tv-dashboard-diagnostics-v1','device':{'manufacturer':'Philips','platformModel':'TEST','appVersion':'0.8.1'},'network':{'transport':'WLAN','internet':'validated'}})
  if self.path=='/api/unifi/state':return self.send({'configured':True,'host':'10.0.0.1','siteId':'fixture-site','ready':True,'checkedAt':int(time.time()*1000),'sites':[{'id':'fixture-site','name':'Zuhause'}],'devices':[{'name':'Wohnzimmer AP','model':'U7 Test','state':'ONLINE'},{'name':'Gateway','model':'Test Gateway','state':'ONLINE'},{'name':'Switch','model':'Test Switch','state':'OFFLINE'}]})
  if self.path=='/api/hue/state':return self.send(hue)
  if self.path=='/api/inputs/state':return self.send({'inputs':[{'name':'Apple TV','port':'HDMI 4','status':'Verbunden'},{'name':'HDMI 1','port':'HDMI 1','status':'Standby'}]})
  if self.path=='/api/network/state':return self.send({'source':'android','connected':True,'transport':'LAN','internet':'validated','address':'10.0.0.20','ssid':'Zuhause','checkedAt':int(time.time()*1000)})
  self.send({'message':'Not found'},404)
 def do_POST(self):
  data=json.loads(self.rfile.read(int(self.headers.get('Content-Length','0'))) or b'{}')
  if self.path=='/api/display':
   state['display']={key:data[key] for key in ['camera','clock','date','homeTitle']};state['device']['displayName']=data.get('deviceName') or 'Philips Test TV'
  elif self.path=='/api/screensaver/configure':saver.update(data);return self.send(saver)
  elif self.path=='/api/hue/room':hue.update(roomId=data['id'],roomName=next(r['name'] for r in hue['rooms'] if r['id']==data['id']));return self.send(hue)
  elif self.path=='/api/hue/configure':hue.update(host=data['host']);return self.send(hue)
  elif self.path=='/api/hue/pair':return self.send(hue)
  elif self.path=='/api/camera/delete':state['cameras']=[c for c in state['cameras'] if c['id']!=data['id']]
  elif self.path=='/api/camera':
   old=next((c for c in state['cameras'] if c['id']==data['id']),None)
   if old:old['name']=data['name']
   else:state['cameras'].append({'id':'fixture-new','name':data['name'],'available':bool(data['url']),'status':'missing'})
   if data.get('setDefault'):state['defaultCameraId']=data['id'] or 'fixture-new'
  self.send({'ok':True})
print(f'Synthetic preview: http://127.0.0.1:{args.port}',flush=True)
ThreadingHTTPServer(('127.0.0.1',args.port),Handler).serve_forever()
