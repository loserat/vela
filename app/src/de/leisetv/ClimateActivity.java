package de.leisetv;

import android.app.Activity;
import android.os.*;
import android.view.*;
import java.util.concurrent.*;
import org.json.*;

/** Actual received values only; historical curves begin with account connection. */
public final class ClimateActivity extends Activity {
    private final Handler handler=new Handler();
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private ClimateChartsView chart;
    private boolean active;
    private final Runnable update=new Runnable(){
        @Override public void run(){
            if(!active)return;
            worker.execute(()->{try{JSONObject view=ClimateRepository.view(ClimateActivity.this);handler.post(()->{if(active){chart.data=view;chart.invalidate();}});}catch(Exception ignored){}});
            handler.postDelayed(this,30000);
        }
    };
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);chart=new ClimateChartsView(this);setContentView(chart);}
    @Override protected void onResume(){super.onResume();active=true;handler.removeCallbacks(update);update.run();}
    @Override protected void onPause(){active=false;handler.removeCallbacks(update);super.onPause();}
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
    @Override public boolean onKeyDown(int code,KeyEvent event){
        if(code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT){
            if(event.getRepeatCount()==0){chart.hours=code==KeyEvent.KEYCODE_DPAD_LEFT?(chart.hours==24?6:1):(chart.hours==1?6:24);chart.invalidate();}return true;
        }return super.onKeyDown(code,event);
    }
}
