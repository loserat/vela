#!/usr/bin/env python3
"""Loopback-only UI preview with synthetic data. No TV/router/light connections."""
from http.server import ThreadingHTTPServer,BaseHTTPRequestHandler
from pathlib import Path
import json,time,argparse
parser=argparse.ArgumentParser();parser.add_argument('--port',type=int,default=9040);args=parser.parse_args()
page=Path(__file__).resolve().parents[1]/'app/assets/settings/index.html'
state={'version':'0.7.0','display':{'camera':True,'clock':True,'date':True},'cameras':[{'id':'fixture-1','name':'Eingang','available':True,'host':'Lokale Quelle','status':'ready'},{'id':'fixture-2','name':'Garten','available':False,'status':'missing'}],'defaultCameraId':'fixture-1'}
hue={'configured':True,'connected':True,'host':'10.0.0.2','roomId':'1','roomName':'TV-Zimmer','rooms':[{'id':'1','name':'TV-Zimmer'},{'id':'2','name':'Küche'}]}
class Handler(BaseHTTPRequestHandler):
 def log_message(self,*args):pass
 def send(self,data,status=200):
  body=data if isinstance(data,bytes) else json.dumps(data).encode();self.send_response(status);self.send_header('Content-Type','text/html; charset=utf-8' if isinstance(data,bytes) else 'application/json');self.send_header('Cache-Control','no-store');self.end_headers();self.wfile.write(body)
 def do_GET(self):
  if self.path=='/':return self.send(page.read_bytes())
  if self.path=='/api/state':return self.send(state)
  if self.path=='/api/hue/state':return self.send(hue)
  if self.path=='/api/inputs/state':return self.send({'inputs':[{'name':'Apple TV','port':'HDMI 4','status':'Verbunden'},{'name':'HDMI 1','port':'HDMI 1','status':'Standby'}]})
  if self.path=='/api/network/state':return self.send({'source':'android','connected':True,'transport':'LAN','internet':'validated','address':'10.0.0.20','checkedAt':int(time.time()*1000)})
  self.send({'message':'Not found'},404)
 def do_POST(self):
  data=json.loads(self.rfile.read(int(self.headers.get('Content-Length','0'))) or b'{}')
  if self.path=='/api/display':state['display']=data
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
