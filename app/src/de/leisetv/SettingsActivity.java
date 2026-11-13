package de.leisetv;

import android.graphics.*;
import android.os.Bundle;
import android.view.View;

/** Local credentials are displayed only here, never in the regular dashboard. */
public final class SettingsActivity extends TvScreen {
    @Override public void onCreate(Bundle saved){super.onCreate(saved);stage("Web-Einstellungen","Am Handy oder Computer öffnen");
        View info=new View(this){final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);@Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/1728f,getHeight()/460f);TvStyle.card(c,p,4,4,1724,456,false,28);TvStyle.icon(c,p,"web",48,44,62,TvStyle.MINT);TvStyle.text(c,p,"Lokale Adresse",50,168,24,TvStyle.MUTED,false);TvStyle.text(c,p,"https://"+AdminServer.localAddress()+":8766",48,230,46,TvStyle.INK,false);p.setColor(0xff34464B);c.drawRect(1040,60,1041,400,p);TvStyle.text(c,p,"Zugangscode",1114,168,24,TvStyle.MUTED,false);TvStyle.text(c,p,AdminServer.pin(SettingsActivity.this),1110,243,64,TvStyle.MINT,true);TvStyle.text(c,p,"Zertifikat beim ersten Öffnen bestätigen.",50,363,24,TvStyle.MUTED,false);c.restore();}};
        info.setContentDescription("https://"+AdminServer.localAddress()+":8766, Zugangscode "+AdminServer.pin(this));place(info,96,300,1728,460);TvStyle.focus(back);
    }
}
