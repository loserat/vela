package de.leisetv;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;

/** Common static TV canvas, spacing, footer and remote-friendly buttons. */
abstract class TvScreen extends Activity {
    FrameLayout root;float scale,ox,oy;private View backdrop;private String title,subtitle="";TvButton back;
    int px(int value){return Math.round(value*scale);}
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);android.util.DisplayMetrics dm=new android.util.DisplayMetrics();getWindowManager().getDefaultDisplay().getMetrics(dm);scale=Math.min(dm.widthPixels/1920f,dm.heightPixels/1080f);ox=(dm.widthPixels-1920*scale)/2;oy=(dm.heightPixels-1080*scale)/2;}
    void stage(String heading,String detail){
        title=heading;subtitle=detail;root=new FrameLayout(this);root.setBackgroundColor(TvStyle.BG);
        backdrop=new View(this){final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);@Override protected void onDraw(Canvas c){c.save();c.translate(ox,oy);c.scale(scale,scale);TvStyle.background(c,p,1920,1080);TvStyle.text(c,p,title,96,121,58,TvStyle.INK,false);TvStyle.text(c,p,subtitle,99,165,25,TvStyle.MUTED,false);p.setColor(0xff34464B);c.drawRect(96,892,1824,893,p);TvStyle.text(c,p,"OK auswählen   ·   Pfeiltasten bewegen",308,955,21,TvStyle.MUTED,false);c.restore();}};
        backdrop.setContentDescription(title+", "+detail);backdrop.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);root.addView(backdrop,new FrameLayout.LayoutParams(-1,-1));setContentView(root);back=button("←  Zurück",96,914,176,64,()->finish());
    }
    void subtitle(String text){subtitle=text;TvStyle.description(backdrop,title+", "+text);backdrop.invalidate();}
    void place(View v,int x,int y,int w,int h){if(v instanceof TvButton){((TvButton)v).w=w;((TvButton)v).h=h;}v.setId(View.generateViewId());FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(px(w),px(h));lp.leftMargin=Math.round(ox+x*scale);lp.topMargin=Math.round(oy+y*scale);root.addView(v,lp);}
    TvButton button(String name,int x,int y,int w,int h,Runnable action){TvButton b=new TvButton(this,name,action);place(b,x,y,w,h);return b;}
}
