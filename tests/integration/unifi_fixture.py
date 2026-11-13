#!/usr/bin/env python3
"""Loopback UniFi Network HTTPS fixture, synthetic credentials/data and no real network devices."""
from http.server import ThreadingHTTPServer,BaseHTTPRequestHandler
from pathlib import Path
from urllib.parse import urlsplit,parse_qs
import ssl,json,argparse,tempfile,subprocess
p=argparse.ArgumentParser();p.add_argument('--port',type=int,default=8444);p.add_argument('--cert');p.add_argument('--key');args=p.parse_args()
if not args.cert or not args.key:
 root=Path(tempfile.mkdtemp(prefix='atpd-unifi-fixture-'));args.cert=str(root/'cert.pem');args.key=str(root/'key.pem');subprocess.run(['openssl','req','-x509','-newkey','rsa:2048','-nodes','-keyout',args.key,'-out',args.cert,'-days','2','-subj','/CN=UniFi test fixture'],check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
state={'failure':False,'reads':[],'writes':0}
class Handler(BaseHTTPRequestHandler):
 def log_message(self,*a):pass
 def reply(self,value,code=200):
  body=json.dumps(value).encode();self.send_response(code);self.send_header('Content-Type','application/json');self.send_header('Content-Length',str(len(body)));self.end_headers();self.wfile.write(body)
 def do_GET(self):
  if self.path=='/test/state':return self.reply(state)
  state['reads'].append(self.path)
  if self.headers.get('X-API-Key')!='fixture-unifi-network-key':return self.reply({},401)
  if state['failure']:return self.reply({},503)
  path=urlsplit(self.path);query=parse_qs(path.query)
  if path.path=='/proxy/network/integration/v1/sites':return self.reply({'data':[{'id':'fixture-site','name':'Teststandort'}],'totalCount':1})
  if path.path=='/proxy/network/integration/v1/sites/fixture-site/devices':
   offset=int(query.get('offset',['0'])[0]);devices=[{'name':'AP Wohnzimmer','model':'U7 Test','state':'ONLINE'},{'name':'Gateway','model':'Test Gateway','state':'ONLINE'},{'name':'Switch','model':'USW Test','state':'OFFLINE'},{'name':'Anderes Gerät','model':'Test','state':'FUTURE_STATE'},{'name':'Etage','model':'USW Test','state':'UPDATING'},{'name':'Büro','model':'U6 Test','state':'ONLINE'},{'name':'Garten','model':'U6 Test','state':'ONLINE'}]
   # Deliberately small pages exercise production pagination.
   return self.reply({'data':devices[offset:offset+3],'totalCount':len(devices)})
  return self.reply({},404)
 def do_POST(self):
  body=json.loads(self.rfile.read(int(self.headers.get('Content-Length','0'))) or b'{}')
  if self.path=='/test/control':state.update({key:body[key] for key in ['failure'] if key in body});return self.reply({})
  state['writes']+=1;self.reply({},405)
 def do_PUT(self):state['writes']+=1;self.reply({},405)
 def do_DELETE(self):state['writes']+=1;self.reply({},405)
server=ThreadingHTTPServer(('127.0.0.1',args.port),Handler);context=ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER);context.load_cert_chain(args.cert,args.key);server.socket=context.wrap_socket(server.socket,server_side=True);print('Loopback UniFi fixture listening on',args.port,flush=True);server.serve_forever()
