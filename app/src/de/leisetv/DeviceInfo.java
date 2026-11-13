package de.leisetv;

import android.content.Context;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import org.json.*;

/** Authenticated diagnostics whitelist. No serial, MAC, accounts, provider keys, streams or SSID. */
final class DeviceInfo {
    static JSONObject view(Context context)throws Exception{
        android.content.pm.PackageInfo app=context.getPackageManager().getPackageInfo(context.getPackageName(),0);
        DisplayMetrics metrics=new DisplayMetrics();WindowManager windows=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);if(windows!=null)windows.getDefaultDisplay().getRealMetrics(metrics);
        StatFs storage=new StatFs(context.getFilesDir().getAbsolutePath());
        return new JSONObject().put("manufacturer",Build.MANUFACTURER).put("platformModel",Build.MODEL)
            .put("androidVersion",Build.VERSION.RELEASE).put("apiLevel",Build.VERSION.SDK_INT).put("securityPatch",Build.VERSION.SECURITY_PATCH).put("hardware",Build.HARDWARE)
            .put("abis",new JSONArray(java.util.Arrays.asList(Build.SUPPORTED_ABIS))).put("appVersion",app.versionName).put("appVersionCode",app.versionCode)
            .put("width",metrics.widthPixels).put("height",metrics.heightPixels).put("densityDpi",metrics.densityDpi).put("uptimeMs",SystemClock.elapsedRealtime())
            .put("storageFreeBytes",storage.getAvailableBytes()).put("storageTotalBytes",storage.getTotalBytes()).put("checkedAt",System.currentTimeMillis());
    }
    static JSONObject diagnostic(Context context)throws Exception{
        JSONObject info=view(context);JSONObject network=NetworkStatus.view(context);
        return new JSONObject().put("format","tv-dashboard-diagnostics-v1").put("device",info)
            .put("network",new JSONObject().put("source","android").put("transport",network.optString("transport")).put("connected",network.optBoolean("connected")).put("internet",network.optString("internet")));
    }
}
