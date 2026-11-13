package de.leisetv;

import android.content.Context;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import org.json.*;
import java.io.IOException;

/** Authenticated diagnostics whitelist. No serial, MAC, accounts, provider keys, streams or SSID. */
final class DeviceInfo {
    static JSONObject view(Context context)throws Exception{
        android.content.pm.PackageInfo app=context.getPackageManager().getPackageInfo(context.getPackageName(),0);
        DisplayMetrics metrics=new DisplayMetrics();WindowManager windows=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);if(windows!=null)windows.getDefaultDisplay().getRealMetrics(metrics);
        StatFs storage=new StatFs(context.getFilesDir().getAbsolutePath());
        return new JSONObject().put("manufacturer",Build.MANUFACTURER).put("platformModel",Build.MODEL).put("productModel",AdminServer.preferences(context).getString("device_product_model", "")).put("productModelSource",AdminServer.preferences(context).contains("device_product_model")?"stored":"unavailable")
            .put("androidVersion",Build.VERSION.RELEASE).put("apiLevel",Build.VERSION.SDK_INT).put("securityPatch",Build.VERSION.SECURITY_PATCH).put("hardware",Build.HARDWARE)
            .put("abis",new JSONArray(java.util.Arrays.asList(Build.SUPPORTED_ABIS))).put("appVersion",app.versionName).put("appVersionCode",app.versionCode)
            .put("width",metrics.widthPixels).put("height",metrics.heightPixels).put("densityDpi",metrics.densityDpi).put("uptimeMs",SystemClock.elapsedRealtime())
            .put("storageFreeBytes",storage.getAvailableBytes()).put("storageTotalBytes",storage.getTotalBytes()).put("checkedAt",System.currentTimeMillis());
    }
    static JSONObject saveModel(Context context,JSONObject input)throws Exception{
        String model=input.getString("model").trim();if(model.length()>80||model.chars().anyMatch(value->Character.isISOControl(value)))throw new IOException("Invalid product model");
        android.content.SharedPreferences.Editor edit=AdminServer.preferences(context).edit();if(model.isEmpty())edit.remove("device_product_model");else edit.putString("device_product_model",model);edit.commit();return view(context);
    }
    static JSONObject diagnostic(Context context)throws Exception{
        JSONObject info=view(context);info.remove("productModel");info.remove("productModelSource");JSONObject network=NetworkStatus.view(context);
        return new JSONObject().put("format","tv-dashboard-diagnostics-v1").put("device",info)
            .put("network",new JSONObject().put("source","android").put("transport",network.optString("transport")).put("connected",network.optBoolean("connected")).put("internet",network.optString("internet")));
    }
}
