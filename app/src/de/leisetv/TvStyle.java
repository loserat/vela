package de.leisetv;

import android.graphics.*;

/** Static drawing helpers shared by the TV screens; no animation loop. */
final class TvStyle {
    static final int BG=0xff101722, INK=0xffF5F3EF, MUTED=0xffA1AEAE, MINT=0xffCBF0D9;
    static final Typeface NORMAL=Typeface.create("sans-serif",0), MEDIUM=Typeface.create("sans-serif-medium",0), LIGHT=Typeface.create("sans-serif-light",0);
    private static Bitmap backdrop;
    static void text(Canvas c,Paint p,String s,float x,float y,float size,int color,boolean bold){
        p.setShader(null);p.setAlpha(255);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTypeface(bold?MEDIUM:NORMAL);c.drawText(s,x,y,p);
    }
    static String fit(Paint p,String value,float width){return android.text.TextUtils.ellipsize(value,new android.text.TextPaint(p),width,android.text.TextUtils.TruncateAt.END).toString();}
    static void description(android.view.View v,String value){if(!android.text.TextUtils.equals(v.getContentDescription(),value))v.setContentDescription(value);}
    static void focus(android.view.View v){if(v!=null&&v.isEnabled()&&!v.requestFocus())v.requestFocusFromTouch();}
    static synchronized void background(Canvas c,Paint p,float w,float h){
        if(backdrop==null){
            backdrop=Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);backdrop.setDensity(Bitmap.DENSITY_NONE);Canvas b=new Canvas(backdrop);Paint brush=new Paint(Paint.ANTI_ALIAS_FLAG);
            brush.setShader(new LinearGradient(0,0,1920,1080,new int[]{0xff192B36,BG,0xff1B1E2B},null,Shader.TileMode.CLAMP));b.drawRect(0,0,1920,1080,brush);
            brush.setShader(new RadialGradient(300,0,1200,0xff28514C,0x0028514C,Shader.TileMode.CLAMP));b.drawRect(0,0,1920,1080,brush);
            brush.setShader(new RadialGradient(1770,80,1100,0x655E4D91,0x005E4D91,Shader.TileMode.CLAMP));b.drawRect(0,0,1920,1080,brush);
            brush.setShader(new RadialGradient(1250,1080,1000,0x354E3975,0x004E3975,Shader.TileMode.CLAMP));b.drawRect(0,0,1920,1080,brush);
        }
        p.setShader(null);p.setAlpha(255);p.setStyle(Paint.Style.FILL);c.drawBitmap(backdrop,null,new RectF(0,0,w,h),p);
    }
    static int accent(String type){if("camera".equals(type))return 0xff8BDCC7;if("ambilight".equals(type)||"color".equals(type))return 0xffBEAFF3;if("hue".equals(type))return 0xffF1C48B;if("devices".equals(type)||"web".equals(type)||"clock".equals(type))return 0xff98C8EE;if("tv".equals(type))return 0xffE9ABAE;if("apps".equals(type))return 0xffC3B6ED;return 0xffA7D6CF;}
    static int blend(int base,int tint,float amount){return Color.rgb(Math.round(Color.red(base)*(1-amount)+Color.red(tint)*amount),Math.round(Color.green(base)*(1-amount)+Color.green(tint)*amount),Math.round(Color.blue(base)*(1-amount)+Color.blue(tint)*amount));}
    static void card(Canvas c,Paint p,float l,float t,float r,float b,boolean focus,float radius){card(c,p,l,t,r,b,focus,radius,MINT);}
    static void card(Canvas c,Paint p,float l,float t,float r,float b,boolean focus,float radius,int accent){
        p.setShader(null);p.setAlpha(255);p.setStyle(Paint.Style.FILL);p.setColor(blend(0xff15212C,accent,focus?.19f:.085f));c.drawRoundRect(l,t,r,b,radius,radius,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(focus?3:1);p.setColor(focus?MINT:blend(0xff34414F,accent,.25f));c.drawRoundRect(l,t,r,b,radius,radius,p);p.setStyle(Paint.Style.FILL);
    }
    static void icon(Canvas c,Paint p,String type,float x,float y,float size,int color){
        c.save();c.translate(x,y);c.scale(size/64f,size/64f);p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3.2f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
        if("camera".equals(type)){c.drawRoundRect(5,14,46,49,8,8,p);c.drawCircle(25,31,9,p);Path v=new Path();v.moveTo(46,24);v.lineTo(59,17);v.lineTo(59,46);v.lineTo(46,39);c.drawPath(v,p);}
        else if("tv".equals(type)){c.drawRoundRect(5,9,59,46,7,7,p);c.drawLine(24,56,40,56,p);c.drawLine(32,46,32,56,p);}
        else if("ambilight".equals(type)){c.drawRoundRect(12,17,52,47,6,6,p);c.drawLine(4,22,4,42,p);c.drawLine(60,22,60,42,p);c.drawLine(19,8,45,8,p);}
        else if("hue".equals(type)){c.drawCircle(32,24,17,p);c.drawLine(22,38,24,48,p);c.drawLine(42,38,40,48,p);c.drawLine(24,48,40,48,p);c.drawLine(26,55,38,55,p);}
        else if("devices".equals(type)){c.drawRoundRect(9,18,55,48,9,9,p);c.drawCircle(43,33,2,p);c.drawLine(20,55,44,55,p);}
        else if("apps".equals(type)){for(int i=0;i<4;i++)c.drawRoundRect(10+(i%2)*25,10+(i/2)*25,29+(i%2)*25,29+(i/2)*25,4,4,p);}
        else if("web".equals(type)){c.drawCircle(32,32,24,p);c.drawOval(20,8,44,56,p);c.drawLine(8,32,56,32,p);}
        else if("clock".equals(type)){c.drawCircle(32,32,24,p);c.drawLine(32,16,32,33,p);c.drawLine(32,33,44,39,p);}
        else if("color".equals(type)){c.drawCircle(22,25,14,p);c.drawCircle(42,25,14,p);c.drawCircle(32,43,14,p);}
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
