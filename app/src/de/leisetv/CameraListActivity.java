package de.leisetv;

import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import java.util.*;
import org.json.*;

/** Six cameras per page; keeps the last selected source when returning from playback. */
public final class CameraListActivity extends TvScreen {
    private static final int PER_PAGE=6;private int page;private String selected="",preferred="",model="";private JSONArray cameras;
    @Override public void onCreate(Bundle state){super.onCreate(state);}
    @Override protected void onResume(){super.onResume();JSONObject library;try{library=CameraLibrary.read(this);cameras=library.getJSONArray("cameras");preferred=library.optString("defaultCameraId");}catch(Exception ignored){library=new JSONObject();cameras=new JSONArray();}String current=library.toString();if(root!=null&&model.equals(current))return;model=current;if(selected.isEmpty())selected=preferred;for(int i=0;i<cameras.length();i++)if(selected.equals(cameras.optJSONObject(i).optString("id")))page=i/PER_PAGE;show();}
    private void show(){
        page=Math.max(0,Math.min(page,Math.max(0,(cameras.length()-1)/PER_PAGE)));stage(UiText.t("Kameras"),cameras.length()+UiText.t(" Quellen · ")+(cameras.length()>PER_PAGE?UiText.t("Seite ")+(page+1)+UiText.t(" von ")+((cameras.length()+5)/6):UiText.t("Deine Kameraübersicht")));
        TvButton settings=button(UiText.t("Einrichten"),1600,206,224,72,()->startActivity(new Intent(this,SettingsActivity.class)));
        if(cameras.length()==0){TvButton empty=button(UiText.t("Noch keine Kameras"),96,310,1728,360,()->startActivity(new Intent(this,SettingsActivity.class)));empty.icon="camera";empty.detail=UiText.t("In den Web-Einstellungen hinzufügen");empty.setNextFocusDownId(back.getId());back.setNextFocusUpId(empty.getId());TvStyle.focus(empty);return;}
        List<Choice> items=new ArrayList<>();Choice target=null;
        for(int i=page*PER_PAGE;i<Math.min(cameras.length(),(page+1)*PER_PAGE);i++){JSONObject c=cameras.optJSONObject(i);Choice tile=new Choice(c,preferred.equals(c.optString("id")));int pos=i%PER_PAGE;place(tile,96+(pos%3)*584,310+(pos/3)*268,560,244);items.add(tile);if(selected.equals(c.optString("id")))target=tile;}
        TvButton prev=null,next=null;
        if(cameras.length()>PER_PAGE){prev=button(UiText.t("← Vorherige"),1224,914,282,64,()->{if(page>0){page--;show();}});next=button(UiText.t("Weitere →"),1524,914,300,64,()->{if((page+1)*PER_PAGE<cameras.length()){page++;show();}});prev.setEnabled(page>0);next.setEnabled((page+1)*PER_PAGE<cameras.length());}
        for(int i=0;i<items.size();i++){Choice t=items.get(i);t.setNextFocusLeftId(items.get(i%3==0?i:i-1).getId());t.setNextFocusRightId(items.get(i%3==2||i==items.size()-1?i:i+1).getId());t.setNextFocusUpId(i<3?settings.getId():items.get(i-3).getId());t.setNextFocusDownId(i+3<items.size()?items.get(i+3).getId():next!=null&&next.isEnabled()?next.getId():prev!=null&&prev.isEnabled()?prev.getId():back.getId());}
        back.setNextFocusUpId(items.get(Math.max(0,items.size()-3)).getId());settings.setNextFocusDownId(items.get(Math.min(2,items.size()-1)).getId());settings.setNextFocusLeftId(items.get(0).getId());
        if(prev!=null){prev.setNextFocusLeftId(back.getId());prev.setNextFocusRightId(next.getId());next.setNextFocusLeftId(prev.isEnabled()?prev.getId():back.getId());prev.setNextFocusUpId(items.get(items.size()-1).getId());next.setNextFocusUpId(items.get(items.size()-1).getId());back.setNextFocusRightId(prev.isEnabled()?prev.getId():next.getId());}
        TvStyle.focus(target!=null?target:items.get(0));
    }
    private final class Choice extends View {
        final JSONObject camera;final boolean preferred;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Choice(JSONObject camera,boolean preferred){super(CameraListActivity.this);this.camera=camera;this.preferred=preferred;setFocusable(true);setClickable(true);setDefaultFocusHighlightEnabled(false);String status=status();setContentDescription(camera.optString("name")+", "+status+(preferred?UiText.t(", Startkamera"):""));setOnFocusChangeListener((v,f)->{invalidate();if(f)selected=camera.optString("id");});setOnClickListener(v->{if(!CameraLibrary.ready(camera)){Toast.makeText(CameraListActivity.this,UiText.t("Stream in den Web-Einstellungen ergänzen"),Toast.LENGTH_SHORT).show();return;}startActivity(new Intent(CameraListActivity.this,CameraActivity.class).putExtra("cameraId",camera.optString("id")));});}
        String status(){return CameraLibrary.ready(camera)?UiText.t("Stream eingerichtet"):"offline".equals(camera.optString("status"))?UiText.t("Als offline hinterlegt"):UiText.t("Stream fehlt");}
        @Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/560f,getHeight()/244f);TvStyle.card(c,p,4,4,556,240,isFocused(),24,TvStyle.accent("camera"));TvStyle.icon(c,p,"camera",32,29,48,TvStyle.accent("camera"));TvStyle.arrow(c,p,514,52,isFocused());
            if(preferred){p.setColor(0xff314B43);c.drawRoundRect(350,31,472,63,8,8,p);TvStyle.text(c,p,UiText.t("Startkamera"),362,53,17,TvStyle.MINT,true);}
            p.setTextSize(34);p.setTypeface(TvStyle.NORMAL);TvStyle.text(c,p,TvStyle.fit(p,camera.optString("name"),488),32,166,34,TvStyle.INK,false);p.setColor(CameraLibrary.ready(camera)?TvStyle.MINT:0xffDDBA81);c.drawCircle(38,205,4,p);TvStyle.text(c,p,status(),52,212,21,TvStyle.MUTED,false);c.restore();}
    }
}
