package de.leisetv;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** No default Android ripple/state animator: focus and selection are drawn explicitly. */
final class TvButton extends View {
    final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);int w,h;String label,detail="",icon="";boolean selected;
    TvButton(Context context,String label,Runnable action){super(context);this.label=label;setFocusable(true);setClickable(true);setDefaultFocusHighlightEnabled(false);setContentDescription(label);setOnFocusChangeListener((v,f)->invalidate());setOnClickListener(v->action.run());}
    void label(String value){if(!label.equals(value)){label=value;invalidate();}describe();}
    void selected(boolean value){if(selected!=value){selected=value;invalidate();}describe();}
    void describe(){TvStyle.description(this,label+(selected?UiText.t(", ausgewählt"):""));}
    @Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/(float)w,getHeight()/(float)h);int accent=icon.isEmpty()?TvStyle.MINT:TvStyle.accent(icon);TvStyle.card(c,p,4,4,w-4,h-4,isFocused(),h>180?26:18,accent);
        if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(TvStyle.MINT);c.drawRoundRect(11,11,w-11,h-11,h>180?21:13, h>180?21:13,p);p.setStyle(Paint.Style.FILL);}
        if(!icon.isEmpty())TvStyle.icon(c,p,icon,30,30,h>180?52:32,accent);
        if(h>180){p.setTextSize(36);p.setTypeface(TvStyle.NORMAL);String title=TvStyle.fit(p,label,w-64);TvStyle.text(c,p,title,32,h-72,36,isEnabled()?TvStyle.INK:TvStyle.MUTED,false);p.setTextSize(22);String sub=TvStyle.fit(p,detail,w-68);TvStyle.text(c,p,sub,34,h-34,22,TvStyle.MUTED,false);}
        else{p.setTextSize(25);p.setTypeface(TvStyle.MEDIUM);String text=TvStyle.fit(p,label,w-48);TvStyle.text(c,p,text,(w-p.measureText(text))/2,h/2f+9,25,isEnabled()?TvStyle.INK:TvStyle.MUTED,true);}c.restore();
    }
}
