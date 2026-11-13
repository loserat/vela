package de.leisetv;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;

public final class ScreensaverPreviewActivity extends Activity {
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);ScreensaverView view=new ScreensaverView(this,null,true);view.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_UP)finish();return true;});setContentView(view);}
    @Override public boolean onKeyDown(int key,KeyEvent event){if(key==KeyEvent.KEYCODE_BACK||key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT||key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN){finish();return true;}return super.onKeyDown(key,event);}
}
