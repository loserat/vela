package de.leisetv;
import java.util.*;
public final class HomeLayoutChecks {
    private static int configurations;
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void verify(List<String> order){
        for(boolean camera:new boolean[]{true,false})for(boolean hue:new boolean[]{true,false})for(boolean ard:new boolean[]{true,false})for(boolean zdf:new boolean[]{true,false})for(String mode:new String[]{"focus","grid"}){
            List<HomeLayout.Slot> slots=HomeLayout.positions(order,camera,hue,ard,zdf,mode);List<String> expected=HomeLayout.normalizedOrder(order);if(!camera)expected.remove("cameras");if(!hue)expected.remove("hue");if(!ard)expected.remove("ard");if(!zdf)expected.remove("zdf");
            require(slots.size()==expected.size(),"Visible tile count");
            for(int i=0;i<slots.size();i++){
                HomeLayout.Slot slot=slots.get(i);require(slot.id.equals(expected.get(i)),"Stored order is respected");
                require(slot.x>=96&&slot.y>=300&&slot.x+slot.w<=1824&&slot.y+slot.h<=856,"Tile inside TV content region");
                for(int j=i+1;j<slots.size();j++){HomeLayout.Slot other=slots.get(j);require(slot.x+slot.w<=other.x||other.x+other.w<=slot.x||slot.y+slot.h<=other.y||other.y+other.h<=slot.y,"No overlapping tiles");}
            }
            Set<Integer> reached=new HashSet<>();ArrayDeque<Integer> todo=new ArrayDeque<>();todo.add(0);
            while(!todo.isEmpty()){int at=todo.remove();if(!reached.add(at))continue;for(int direction=0;direction<4;direction++){int next=HomeLayout.neighbor(slots,at,direction);require(next>=0&&next<slots.size(),"Every neighbor exists");todo.add(next);}}
            require(reached.size()==slots.size(),"D-pad reaches every visible destination");configurations++;
        }
    }
    private static void permutations(List<String> order,int start){if(start==order.size()){verify(order);return;}for(int i=start;i<order.size();i++){Collections.swap(order,i,start);permutations(order,start+1);Collections.swap(order,i,start);}}
    public static void main(String[] args){
        List<String> defaults=Arrays.asList(HomeLayout.DEFAULT_ORDER);
        require(HomeLayout.validOrder(defaults),"Default order valid");
        require(!HomeLayout.validOrder(Arrays.asList("cameras","cameras","hue","tv","inputs","system")),"Duplicate rejected");
        require(!HomeLayout.validOrder(Arrays.asList("unknown","ambilight","hue","tv","inputs","system")),"Unknown rejected");
        require(!HomeLayout.validOrder(defaults.subList(0,5)),"Missing destination rejected");
        List<HomeLayout.Slot> standard=HomeLayout.positions(defaults,true,"focus");require(standard.get(0).x==96&&standard.get(0).w==560&&standard.get(0).h==392&&standard.get(3).y==724&&standard.get(3).h==132,"Original layout preserved");
        List<HomeLayout.Slot> hidden=HomeLayout.positions(defaults,false,"focus");require(hidden.get(0).w==852&&hidden.get(2).id.equals("tv")&&hidden.get(2).h==132,"Hidden camera leaves two large and three small tiles");
        List<String> moved=new ArrayList<>(defaults);Collections.swap(moved,0,5);require(HomeLayout.positions(moved,true,"focus").get(5).id.equals("cameras")&&HomeLayout.positions(moved,true,"focus").get(5).h==132,"Camera can move to small row");
        boolean invalid=false;try{HomeLayout.positions(defaults,true,"unknown");}catch(IllegalArgumentException e){invalid=true;}require(invalid,"Unknown geometry rejected");
        List<String> legacy=new ArrayList<>(Arrays.asList(HomeLayout.LEGACY_ORDER));Collections.reverse(legacy);require(HomeLayout.validOrder(legacy)&&HomeLayout.normalizedOrder(legacy).subList(0,6).equals(legacy),"Legacy custom order is preserved before appending shortcuts");
        permutations(new ArrayList<>(defaults),0);require(configurations==1290240,"All eight-item permutations, visibility states and modes tested");
        System.out.println("Home layout checks passed: 1290240 configurations; bounds, overlap, order and D-pad reachability verified.");
    }
}
