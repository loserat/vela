package de.leisetv;

import android.content.*;
import android.media.tv.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

/** HDMI discovery and explicit passthrough intents; no device IDs/ports are hard-coded. */
final class InputCatalog {
    static final class Entry {
        final String id,name,port;final int state;
        Entry(String id,String name,String port,int state){this.id=id;this.name=name;this.port=port;this.state=state;}
        String status(){return state==TvInputManager.INPUT_STATE_CONNECTED?"Verbunden":state==TvInputManager.INPUT_STATE_CONNECTED_STANDBY?"Standby":"Nicht verbunden";}
    }
    private static final class Hdmi {
        final int port;final String name;final boolean cec;
        Hdmi(int port,String name,boolean cec){this.port=port;this.name=name;this.cec=cec;}
    }
    /** Android 8 exposes this read-only SystemApi metadata on the provided input object.
     * Newer firmware may restrict it; then ordinary labels/parent relationships remain available. */
    private static Hdmi hdmi(TvInputInfo info){
        try{Object value=info.getClass().getMethod("getHdmiDeviceInfo").invoke(info);if(value==null)return null;Class<?> type=value.getClass();return new Hdmi((Integer)type.getMethod("getPortId").invoke(value),String.valueOf(type.getMethod("getDisplayName").invoke(value)),(Boolean)type.getMethod("isCecDevice").invoke(value));}
        catch(ReflectiveOperationException|RuntimeException ignored){return null;}
    }
    private static String base(Context c,TvInputInfo info){CharSequence label=info.loadLabel(c);return label==null||label.length()==0?"HDMI":label.toString();}
    private static int number(String name){Matcher m=Pattern.compile("HDMI\\s*([0-9]+)",Pattern.CASE_INSENSITIVE).matcher(name);if(!m.find())return -1;try{return Integer.parseInt(m.group(1));}catch(NumberFormatException e){return -1;}}
    static List<Entry> read(Context context){
        List<Entry> result=new ArrayList<>();TvInputManager manager=(TvInputManager)context.getSystemService(Context.TV_INPUT_SERVICE);if(manager==null)return result;
        try{
            List<TvInputInfo> all=manager.getTvInputList();Map<Integer,String> devices=new HashMap<>();Map<String,TvInputInfo> map=new HashMap<>();Map<String,Hdmi> metadata=new HashMap<>();
            for(TvInputInfo info:all){map.put(info.getId(),info);if(info.getType()!=TvInputInfo.TYPE_HDMI)continue;Hdmi h=hdmi(info);metadata.put(info.getId(),h);if(h!=null&&h.cec&&h.port>0&&!h.name.isEmpty())devices.put(h.port,h.name);}
            // Public parent labels are a fallback when legacy HDMI metadata is unavailable.
            for(TvInputInfo info:all){if(info.getType()!=TvInputInfo.TYPE_HDMI||metadata.get(info.getId())!=null)continue;TvInputInfo parent=map.get(info.getParentId());if(parent!=null){int port=number(base(context,parent));if(port>0&&!devices.containsKey(port))devices.put(port,base(context,info));}}
            // One row per physical input: CEC aliases annotate the port instead of replacing it.
            for(TvInputInfo info:all){Hdmi h=metadata.get(info.getId());if(!info.isPassthroughInput()||info.getType()!=TvInputInfo.TYPE_HDMI||info.isHidden(context)||(h!=null&&h.cec)||info.getParentId()!=null)continue;
                String original=base(context,info);int n=number(original);if(n<1&&h!=null)n=h.port;String port=n>0?"HDMI "+n:"HDMI";String name=devices.get(n);CharSequence alias=info.loadCustomLabel(context);
                if(name==null){name=n>0?port:original;if(alias!=null&&alias.length()>0&&!alias.toString().equals(name))port=port+" · "+alias;}
                result.add(new Entry(info.getId(),name,port,manager.getInputState(info.getId())));
            }
            // Some platforms expose only a CEC child, without a selectable physical parent.
            if(result.isEmpty())for(TvInputInfo info:all){if(!info.isPassthroughInput()||info.getType()!=TvInputInfo.TYPE_HDMI||info.isHidden(context))continue;Hdmi h=metadata.get(info.getId());String port=h!=null&&h.port>0?"HDMI "+h.port:"HDMI";result.add(new Entry(info.getId(),h!=null&&h.cec?h.name:base(context,info),port,manager.getInputState(info.getId())));}
            result.sort(Comparator.comparingInt((Entry e)->e.state==TvInputManager.INPUT_STATE_CONNECTED?0:e.state==TvInputManager.INPUT_STATE_CONNECTED_STANDBY?1:2).thenComparing(e->e.port).thenComparing(e->e.name));
        }catch(RuntimeException ignored){}
        return result;
    }
    static JSONArray view(Context context)throws JSONException{JSONArray result=new JSONArray();for(Entry e:read(context))result.put(new JSONObject().put("id",e.id).put("name",e.name).put("port",e.port).put("state",e.state).put("status",e.status()));return result;}
    static Intent intent(Context context,String id){TvInputManager manager=(TvInputManager)context.getSystemService(Context.TV_INPUT_SERVICE);TvInputInfo current=manager==null?null:manager.getTvInputInfo(id);if(current==null||!current.isPassthroughInput()||current.getType()!=TvInputInfo.TYPE_HDMI)throw new IllegalArgumentException("HDMI input no longer present");return new Intent(Intent.ACTION_VIEW).setDataAndType(TvContract.buildChannelUriForPassthroughInput(id),TvContract.Channels.CONTENT_ITEM_TYPE);}
}
