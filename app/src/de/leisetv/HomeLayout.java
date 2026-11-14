package de.leisetv;

import java.util.*;

/** Pure geometry for eight stored Home destinations, with deterministic D-pad neighbors. */
final class HomeLayout {
    static final String[] LEGACY_ORDER={"cameras","ambilight","hue","tv","inputs","system"};
    // Shared project baseline: the primary TV layout. Saved orders remain authoritative.
    static final String[] DEFAULT_ORDER={"ambilight","cameras","hue","tv","inputs","system","ard","zdf"};
    static boolean validOrder(List<String> order){return (order.size()==DEFAULT_ORDER.length&&new HashSet<>(order).equals(new HashSet<>(Arrays.asList(DEFAULT_ORDER))))||(order.size()==LEGACY_ORDER.length&&new HashSet<>(order).equals(new HashSet<>(Arrays.asList(LEGACY_ORDER))));}
    static List<String> normalizedOrder(List<String> order){if(!validOrder(order))throw new IllegalArgumentException("Invalid Home order");List<String> result=new ArrayList<>(order);for(String id:DEFAULT_ORDER)if(!result.contains(id))result.add(id);return result;}
    static final class Slot {
        final String id;final int x,y,w,h,row;
        Slot(String id,int x,int y,int w,int h,int row){this.id=id;this.x=x;this.y=y;this.w=w;this.h=h;this.row=row;}
        int center(){return x+w/2;}
    }
    static List<Slot> positions(List<String> order,boolean camera,String layout){
        return positions(order,camera,true,layout);
    }
    static List<Slot> positions(List<String> order,boolean camera,boolean hue,String layout){
        return positions(order,camera,hue,false,false,layout);
    }
    static List<Slot> positions(List<String> order,boolean camera,boolean hue,boolean ard,boolean zdf,String layout){
        if(!validOrder(order)||!("focus".equals(layout)||"grid".equals(layout)))throw new IllegalArgumentException("Invalid Home layout");
        List<String> visible=normalizedOrder(order);if(!camera)visible.remove("cameras");if(!hue)visible.remove("hue");if(!ard)visible.remove("ard");if(!zdf)visible.remove("zdf");
        int top=visible.size()>6?(visible.size()+1)/2:visible.size()==4?2:"focus".equals(layout)?visible.size()-3:3;List<Slot> slots=new ArrayList<>();
        for(int i=0;i<visible.size();i++){
            int row=i<top?0:1,count=row==0?top:visible.size()-top,col=row==0?i:i-top;
            int w=(1728-(count-1)*24)/count,y=row==0?300:"focus".equals(layout)?724:584;
            int h="grid".equals(layout)?260:row==0?392:132;
            slots.add(new Slot(visible.get(i),96+col*(w+24),y,w,h,row));
        }
        return slots;
    }
    // 0 left, 1 right, 2 up, 3 down. Edges stay put; Main connects the lower row to its network badge.
    static int neighbor(List<Slot> slots,int index,int direction){
        Slot from=slots.get(index);int best=index,distance=Integer.MAX_VALUE;
        for(int i=0;i<slots.size();i++){
            Slot to=slots.get(i);boolean valid=direction==0?to.row==from.row&&to.x<from.x:direction==1?to.row==from.row&&to.x>from.x:direction==2?to.row<from.row:direction==3&&to.row>from.row;
            if(valid){int d=Math.abs(to.center()-from.center());if(d<distance){best=i;distance=d;}}
        }
        return best;
    }
}
