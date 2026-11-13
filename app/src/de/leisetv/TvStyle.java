package de.leisetv;

import android.graphics.*;

/** Static drawing helpers shared by the TV screens; no animation loop. */
final class TvStyle {
    static final int BG=0xff0B1418, INK=0xffF0F5F1, MUTED=0xff8DA6A5, MINT=0xffC9F1D1;
    static void text(Canvas c,Paint p,String s,float x,float y,float size,int color,boolean bold){
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);
        p.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",0));c.drawText(s,x,y,p);
    }
    static void background(Canvas c,Paint p,float w,float h){
        p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,0,w,h,new int[]{0xff12252B,BG,0xff0D181B},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);
        p.setShader(new RadialGradient(w*.22f,0,w*.65f,0xff1B3839,0x001B3839,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
    }
    static void card(Canvas c,Paint p,float l,float t,float r,float b,boolean focus,float radius){
        p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(l,t,r,b,focus?0xff25433F:0xff18292E,focus?0xff172F30:0xff122126,Shader.TileMode.CLAMP));
        c.drawRoundRect(l,t,r,b,radius,radius,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(focus?3:1);
        p.setColor(focus?MINT:0xff304347);c.drawRoundRect(l,t,r,b,radius,radius,p);p.setStyle(Paint.Style.FILL);
    }
    static void icon(Canvas c,Paint p,String type,float x,float y,float size,int color){
        c.save();c.translate(x,y);c.scale(size/64f,size/64f);p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3.2f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
        if("camera".equals(type)){c.drawRoundRect(5,14,46,49,8,8,p);c.drawCircle(25,31,9,p);Path v=new Path();v.moveTo(46,24);v.lineTo(59,17);v.lineTo(59,46);v.lineTo(46,39);c.drawPath(v,p);}
        else if("tv".equals(type)){c.drawRoundRect(5,9,59,46,7,7,p);c.drawLine(24,56,40,56,p);c.drawLine(32,46,32,56,p);}
        else if("ambilight".equals(type)){c.drawRoundRect(12,17,52,47,6,6,p);c.drawLine(4,22,4,42,p);c.drawLine(60,22,60,42,p);c.drawLine(19,8,45,8,p);}
        else if("climate".equals(type)){c.drawRoundRect(22,6,37,43,8,8,p);c.drawCircle(30,47,12,p);c.drawLine(30,22,30,48,p);c.drawLine(47,18,58,18,p);c.drawLine(47,29,55,29,p);}
        else{c.drawCircle(32,32,11,p);for(int i=0;i<8;i++){c.save();c.rotate(i*45,32,32);c.drawLine(32,5,32,12,p);c.restore();}c.drawCircle(32,32,22,p);}
        p.setStyle(Paint.Style.FILL);c.restore();
    }
    static void cameraArt(Canvas c,Paint p,float x,float y,boolean focus){
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);p.setColor(focus?0xff476D63:0xff2B4548);
        c.drawCircle(x,y,148,p);c.drawCircle(x,y,112,p);c.drawCircle(x,y,69,p);
        for(int i=0;i<6;i++){double a=Math.toRadians(i*60);c.drawLine(x+(float)Math.cos(a)*69,y+(float)Math.sin(a)*69,x+(float)Math.cos(a+.7)*112,y+(float)Math.sin(a+.7)*112,p);}
        p.setStyle(Paint.Style.FILL);p.setColor(focus?0xff3B5B50:0xff20373C);c.drawCircle(x,y,38,p);
    }
    static void arrow(Canvas c,Paint p,float x,float y,boolean focus){
        p.setStyle(Paint.Style.FILL);p.setColor(focus?MINT:0xff263E42);c.drawCircle(x,y,21,p);
        p.setStyle(Paint.Style.STROKE);p.setColor(focus?BG:MUTED);p.setStrokeWidth(2.5f);c.drawLine(x-6,y,x+6,y,p);c.drawLine(x+1,y-5,x+6,y,p);c.drawLine(x+1,y+5,x+6,y,p);p.setStyle(Paint.Style.FILL);
    }
}
