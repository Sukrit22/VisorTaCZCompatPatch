package dev.visorcompat.tacz.client;
import java.util.*;
/** Read-only animation selection for native tube reloads, including two-shell loops. */
public final class DualReloadSounds {
    public record Segment(String name,double second) {}
    private DualReloadSounds(){}
    public static boolean ownsAnimation(String name){return name!=null&&(name.equals("bolt")||name.startsWith("bolt_")||name.startsWith("reload_"));}
    public static List<Segment> plan(Map<String,Double> lengths,boolean empty,boolean alternate,int rounds){
        String suffix=alternate?"_semi":"";
        String intro=choose(lengths,empty?new String[]{"reload_intro_empty"+suffix,"reload_empty_intro"+suffix,"reload_intro_empty","reload_empty_intro"}:new String[]{"reload_intro"+suffix,"reload_intro"});
        String loop=choose(lengths,new String[]{"reload_loop"+suffix,"reload_loop"});
        String pair=choose(lengths,new String[]{"reload_loop_2"+suffix,"reload_loop_2"});
        String end=choose(lengths,new String[]{"reload_end"+suffix,"reload_end"});
        if(intro==null||loop==null)return List.of();
        var result=new ArrayList<Segment>();double time=0;
        result.add(new Segment(intro,time));time+=lengths.get(intro);
        int remaining=Math.max(0,Math.min(256,rounds));
        while(remaining>0){boolean two=remaining>1&&pair!=null;String name=two?pair:loop;result.add(new Segment(name,time));time+=lengths.get(name);remaining-=two?2:1;}
        if(end!=null)result.add(new Segment(end,time));
        return result;
    }
    private static String choose(Map<String,Double> lengths,String[] names){for(String n:names)if(lengths.containsKey(n)&&Double.isFinite(lengths.get(n))&&lengths.get(n)>0)return n;return null;}
}
