package de.leisetv;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
public final class SettingsActivity extends Activity {
    @Override public void onCreate(Bundle state){
        super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER_VERTICAL);root.setPadding(96,60,96,60);root.setBackgroundColor(0xff10191C);
        label(root,"Einstellungen",38,0xffF2F3E9,0);
        label(root,"Am Handy oder Mac öffnen",20,0xff97AAA9,24);
        label(root,"https://"+AdminServer.localAddress()+":8766",27,0xffC7E9CB,14);
        label(root,"PIN  "+AdminServer.pin(this),30,0xffF2F3E9,32);
        label(root,"Zurück",16,0xff97AAA9,48);setContentView(root);
    }
    private void label(LinearLayout root,String value,int size,int color,int top){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,top,0,0);root.addView(t);
    }
}
