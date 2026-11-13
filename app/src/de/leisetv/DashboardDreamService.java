package de.leisetv;

import android.service.dreams.DreamService;

public final class DashboardDreamService extends DreamService {
    private ScreensaverView view;
    @Override public void onAttachedToWindow(){
        super.onAttachedToWindow();setInteractive(false);setFullscreen(true);setScreenBright(!AdminServer.preferences(this).getBoolean("saver_dim",true));view=new ScreensaverView(this,null,true);setContentView(view);
    }
    @Override public void onDreamingStarted(){super.onDreamingStarted();if(view!=null)view.startClock();}
    @Override public void onDreamingStopped(){if(view!=null)view.stopClock();super.onDreamingStopped();}
}
