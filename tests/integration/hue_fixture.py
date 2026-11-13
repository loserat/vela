#!/usr/bin/env python3
"""Loopback-only Hue HTTPS fixture. Never points at a real bridge or lamps."""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import argparse,json,ssl,subprocess,tempfile,time,threading
parser=argparse.ArgumentParser();parser.add_argument('--port',type=int,default=8443);parser.add_argument('--cert');parser.add_argument('--key');args=parser.parse_args()
if not args.cert or not args.key:
    directory=Path(tempfile.mkdtemp(prefix='atpd-fixture-'));args.cert=str(directory/'cert.pem');args.key=str(directory/'key.pem')
    subprocess.run(['openssl','req','-x509','-newkey','rsa:2048','-nodes','-keyout',args.key,'-out',args.cert,'-days','2','-subj','/CN=0011223344556677'],check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
application_key='mock_hue_application_key_123456' # Deliberately synthetic, accepted only by this fixture.
state={'paired':False,'link':False,'registrations':0,'writes':[],'reads':[],'outsideWrites':0,'delay':0,'failure':False,'scenesEnabled':False,'staleReads':False,'action':{'on':True,'bri':191,'hue':59110,'sat':165,'colormode':'hs'}}
lock=threading.Lock()
class Handler(BaseHTTPRequestHandler):
    def reply(self,value,code=200):
        body=json.dumps(value).encode();self.send_response(code);self.send_header('Content-Type','application/json');self.send_header('Content-Length',str(len(body)));self.end_headers()
        try:self.wfile.write(body)
        except (BrokenPipeError,ConnectionResetError,ssl.SSLError):pass
    def body(self):return json.loads(self.rfile.read(int(self.headers.get('Content-Length','0'))) or b'{}')
    def do_GET(self):
        if self.path=='/test/state':
            with lock:self.reply(state)
            return
        if self.path=='/api/config':self.reply({'name':'Test Hue Bridge','bridgeid':'0011223344556677','modelid':'BSB002'});return
        state['reads'].append(self.path)
        if state['failure']:self.reply({},503);return
        prefix='/api/'+application_key
        if not self.path.startswith(prefix):self.reply([{'error':{'type':1}}]);return
        path=self.path[len(prefix):];action=dict(state['action'])
        if state['staleReads']:action['bri']=50
        if path=='/groups':self.reply({'1':{'name':'TV-Zimmer','type':'Room','lights':['1','2'],'state':{'any_on':action['on'],'all_on':action['on']},'action':action},'2':{'name':'Anderer Raum','type':'Room','lights':['3'],'state':{'any_on':False,'all_on':False},'action':{'on':False,'bri':120}}})
        elif path=='/lights':self.reply({i:{'state':{'on':True,'bri':127,'hue':0,'sat':254,'ct':300}} for i in ['1','2','3']})
        elif path=='/scenes':self.reply({'scene1':{'name':'Lesen','group':'1'},'scene2':{'name':'Andere Szene','group':'2'}} if state['scenesEnabled'] else {})
        else:self.reply({},404)
    def do_POST(self):
        data=self.body()
        if self.path=='/test/control':
            with lock:
                for name in ['delay','failure','scenesEnabled','staleReads']:
                    if name in data:state[name]=data[name]
                if 'action' in data:state['action'].update(data['action'])
            self.reply({});return
        if self.path=='/test/link':state['link']=True;self.reply({});return
        if self.path=='/api':
            if not state['link']:self.reply([{'error':{'type':101}}]);return
            state['paired']=True;state['link']=False;state['registrations']+=1;self.reply([{'success':{'username':application_key}}]);return
        self.reply({},404)
    def do_PUT(self):
        data=self.body();time.sleep(float(state['delay']))
        if state['failure']:self.reply({},503);return
        if self.path=='/api/'+application_key+'/groups/1/action':
            if 'hue' in data or 'sat' in data:data['colormode']='hs'
            if 'ct' in data:data['colormode']='ct'
            with lock:state['action'].update(data);state['writes'].append(data)
            self.reply([{'success':{'/groups/1/action':True}}]);return
        state['outsideWrites']+=1;self.reply([{'error':{'type':7}}])
    def log_message(self,*args):pass
server=ThreadingHTTPServer(('127.0.0.1',args.port),Handler);context=ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER);context.load_cert_chain(args.cert,args.key);server.socket=context.wrap_socket(server.socket,server_side=True);print('Loopback Hue fixture listening on port',args.port,flush=True);server.serve_forever()
