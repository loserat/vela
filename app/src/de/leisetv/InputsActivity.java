package de.leisetv;

import android.content.*;
import android.media.tv.*;
import android.os.*;
import android.widget.Toast;
import java.util.*;

/** Entry only reads sources. Only an explicit card click launches the selected TV input. */
public final class InputsActivity extends TvScreen {
    private final Handler handler=new Handler();private TvInputManager manager;private boolean active;private int page;private String focusId="",model="";private List<InputCatalog.Entry> entries=new ArrayList<>();
    private final Runnable refresh=()->{if(active){load();}};
    private final TvInputManager.TvInputCallback callback=new TvInputManager.TvInputCallback(){public void onInputAdded(String id){schedule();}public void onInputRemoved(String id){schedule();}public void onInputStateChanged(String id,int state){schedule();}};
    private void schedule(){handler.removeCallbacks(refresh);handler.postDelayed(refresh,250);}
    @Override public void onCreate(Bundle state){super.onCreate(state);manager=(TvInputManager)getSystemService(TV_INPUT_SERVICE);}
    @Override protected void onResume(){super.onResume();active=true;if(manager!=null)manager.registerCallback(callback,handler);load();}
    @Override protected void onPause(){active=false;handler.removeCallbacks(refresh);if(manager!=null)manager.unregisterCallback(callback);super.onPause();}
    private void load(){entries=InputCatalog.read(this);org.json.JSONArray values=new org.json.JSONArray();for(InputCatalog.Entry e:entries)values.put(new org.json.JSONArray().put(e.id).put(e.name).put(e.port).put(e.state));String current=values.toString()+AdminServer.preferences(this).getString("last_input","");if(root!=null&&current.equals(model))return;model=current;render();}
    private void render(){
        page=Math.max(0,Math.min(page,Math.max(0,(entries.size()-1)/6)));stage(UiText.t("Geräte"),UiText.t("HDMI-Eingänge direkt auswählen"));
        if(entries.isEmpty()){TvButton empty=button(UiText.t("Keine TV-Eingänge gemeldet"),96,310,1728,360,()->{});empty.icon="tv";empty.detail=UiText.t("Dieser Android-Dienst stellt gerade keine HDMI-Eingänge bereit.");empty.setNextFocusDownId(back.getId());back.setNextFocusUpId(empty.getId());TvStyle.focus(back);return;}
        String last=AdminServer.preferences(this).getString("last_input","");List<TvButton> tiles=new ArrayList<>();TvButton target=null;int cols=entries.size()<=4?2:3,width=cols==2?852:560;
        for(int i=page*6;i<Math.min(entries.size(),(page+1)*6);i++){InputCatalog.Entry entry=entries.get(i);int pos=i%6;TvButton card=button(entry.name,96+(pos%cols)*(width+24),310+(pos/cols)*268,width,244,()->select(entry));card.icon="devices";card.detail=(entry.name.equals(entry.port)?"":entry.port+" · ")+entry.status()+(entry.id.equals(last)?UiText.t(" · Zuletzt gewählt"):"");card.selected(entry.id.equals(last));card.setContentDescription(entry.name+", "+card.detail+(entry.id.equals(last)?UiText.t(", zuletzt gewählt"):""));card.setOnFocusChangeListener((v,f)->{card.invalidate();if(f)focusId=entry.id;});tiles.add(card);if(entry.id.equals(focusId))target=card;}
        TvButton next=null;if(entries.size()>6)next=button(UiText.t("Weitere Eingänge →"),1424,914,400,64,()->{page=(page+1)%((entries.size()+5)/6);focusId="";render();});
        for(int i=0;i<tiles.size();i++){TvButton tile=tiles.get(i);tile.setNextFocusLeftId(tiles.get(i%cols==0?i:i-1).getId());tile.setNextFocusRightId(tiles.get(i%cols==cols-1||i==tiles.size()-1?i:i+1).getId());tile.setNextFocusUpId(tiles.get(i<cols?i:i-cols).getId());tile.setNextFocusDownId(i+cols<tiles.size()?tiles.get(i+cols).getId():next!=null?next.getId():back.getId());}
        back.setNextFocusUpId(tiles.get(0).getId());if(next!=null){next.setNextFocusUpId(tiles.get(tiles.size()-1).getId());next.setNextFocusLeftId(back.getId());back.setNextFocusRightId(next.getId());}TvStyle.focus(target!=null?target:tiles.get(0));
    }
    private void select(InputCatalog.Entry entry){
        try{Intent intent=InputCatalog.intent(this,entry.id);android.content.pm.ResolveInfo target=getPackageManager().resolveActivity(intent,0);if(target==null||target.activityInfo==null||getPackageName().equals(target.activityInfo.packageName))throw new ActivityNotFoundException();startActivity(intent);AdminServer.preferences(this).edit().putString("last_input",entry.id).apply();}
        catch(RuntimeException e){Toast.makeText(this,UiText.t("Eingang gerade nicht verfügbar. Bitte TV-Verbindung prüfen."),Toast.LENGTH_LONG).show();schedule();}
    }
}
