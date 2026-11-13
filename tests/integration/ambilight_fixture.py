#!/usr/bin/env python3
"""Loopback-only JointSpace fixture for a disposable emulator."""
from http.server import BaseHTTPRequestHandler,ThreadingHTTPServer
import json,time
state={'power':'Off','config':{'styleName':'FOLLOW_VIDEO','isExpert':False,'menuSetting':'NATURAL'},'fail':False,'writes':[]}
state['reads']=[];state['delay']=0
styles={'supportedStyles':[{'styleName':'OFF'},{'styleName':'FOLLOW_VIDEO'},{'styleName':'FOLLOW_AUDIO'},{'styleName':'FOLLOW_COLOR','algorithms':['MANUAL_HUE']}]}
class Handler(BaseHTTPRequestHandler):
    def reply(self,code,value):
        body=json.dumps(value).encode();self.send_response(code);self.send_header('Content-Type','application/json');self.send_header('Content-Length',str(len(body)));self.end_headers();self.wfile.write(body)
    def do_GET(self):
        if self.path=='/test/state':self.reply(200,state);return
        if state['fail']:self.reply(503,{'error':'Fixture unavailable'});return
        state['reads'].append(self.path)
        value={'/6/ambilight/power':{'power':state['power']},'/6/ambilight/currentconfiguration':state['config'],'/6/ambilight/supportedstyles':styles}.get(self.path)
        self.reply(404,{} ) if value is None else self.reply(200,value)
    def do_POST(self):
        data=json.loads(self.rfile.read(int(self.headers.get('Content-Length','0'))))
        if self.path=='/test/delay':state['delay']=data['seconds'];self.reply(200,{});return
        if self.path=='/test/failure':state['fail']=data['fail'];self.reply(200,{});return
        if state['fail']:self.reply(503,{});return
        if state['delay']:time.sleep(state['delay'])
        if self.path=='/6/ambilight/power' and data.get('power') in ['On','Off']:state['power']=data['power']
        elif self.path=='/6/ambilight/currentconfiguration':
            if not isinstance(data.get('isExpert'),bool):self.reply(422,{});return
            if data.get('styleName')=='FOLLOW_COLOR':
                color=data.get('colorSettings',{}).get('color',{})
                if not (0<=color.get('hue',-1)<=255 and 0<=color.get('saturation',-1)<=255 and 0<=color.get('brightness',-1)<=255):self.reply(422,{});return
            state['config']=data
        else:self.reply(404,{});return
        state['writes'].append({'path':self.path,'body':data});self.reply(200,{})
    def log_message(self,*args):pass
ThreadingHTTPServer(('127.0.0.1',8895),Handler).serve_forever()
