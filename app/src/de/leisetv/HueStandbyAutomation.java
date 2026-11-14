package de.leisetv;

import android.content.*;
import android.os.PowerManager;
import android.os.SystemClock;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.*;

/** One bounded, room-scoped off command per observed interactive-to-sleep transition. */
final class HueStandbyAutomation implements AutoCloseable {
    private final Context context;
    private final PowerManager power;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private volatile long generation;
    private volatile boolean closed;
    private boolean interactive;
    private final BroadcastReceiver receiver=new BroadcastReceiver(){
        @Override public void onReceive(Context c,Intent intent){
            if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){interactive=true;generation++;}
            else if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())&&interactive){interactive=false;sleep(++generation);}
        }
    };
    HueStandbyAutomation(Context context){
        this.context=context.getApplicationContext();power=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
        interactive=power!=null&&power.isInteractive();
        IntentFilter filter=new IntentFilter(Intent.ACTION_SCREEN_ON);filter.addAction(Intent.ACTION_SCREEN_OFF);
        // These are protected system broadcasts; ordinary apps cannot spoof them.
        this.context.registerReceiver(receiver,filter);
    }
    static JSONObject view(Context c)throws Exception{
        SharedPreferences prefs=AdminServer.preferences(c);JSONObject hue=HueController.setup(c);
        return new JSONObject().put("enabled",prefs.getBoolean("hue_standby_off",false))
            .put("ready",hue.optBoolean("connected")&&hue.optString("roomId").matches("[1-9][0-9]{0,4}"))
            .put("lastAttemptAt",prefs.getLong("hue_standby_at",0)).put("lastResult",prefs.getString("hue_standby_result","none"));
    }
    static JSONObject save(Context c,JSONObject input)throws Exception{
        if(!(input.opt("enabled") instanceof Boolean))throw new IOException("Boolean required");
        boolean enabled=input.getBoolean("enabled");
        if(enabled&&!view(c).optBoolean("ready"))throw new IOException("Select a paired Hue room first");
        AdminServer.preferences(c).edit().putBoolean("hue_standby_off",enabled).commit();return view(c);
    }
    private boolean enabled(){return IntegrationSettings.hue(context)&&AdminServer.preferences(context).getBoolean("hue_standby_off",false);}
    private void sleep(long token){
        if(!enabled()||power==null||closed)return;
        final JSONObject target=HueController.standbyTarget(context);
        final long at=System.currentTimeMillis(),deadline=SystemClock.elapsedRealtime()+1500;
        final PowerManager.WakeLock lock=power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TV-Dashboard:HueStandby");
        try{
            lock.acquire(9000); // CPU only, never wakes the display; always time limited.
            worker.execute(()->{
                String result="skipped";
                try{
                    boolean sent=HueController.standbyOff(context,target,deadline,()->!closed&&token==generation&&enabled()&&!power.isInteractive());
                    if(sent)result="sent";
                }catch(Exception error){result="failed";android.util.Log.w("TV-Dashboard","hue-standby:not-confirmed");}
                finally{
                    AdminServer.preferences(context).edit().putLong("hue_standby_at",at).putString("hue_standby_result",result).apply();
                    if(lock.isHeld())lock.release();
                }
            });
        }catch(RuntimeException error){if(lock.isHeld())lock.release();}
    }
    @Override public void close(){closed=true;generation++;context.unregisterReceiver(receiver);worker.shutdown();}
}
