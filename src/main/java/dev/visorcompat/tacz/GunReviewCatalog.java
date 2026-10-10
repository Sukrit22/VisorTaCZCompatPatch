package dev.visorcompat.tacz;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.StringReader;
import java.util.*;

/** Human verification metadata. Never enables mechanics or changes calibration values. */
public record GunReviewCatalog(Map<String,String> packs,Map<String,Entry> guns) {
    public record Entry(String pack,String name,boolean calibrated,String notes) {}
    public static GunReviewCatalog parse(String text){
        Config root=new TomlParser().parse(new StringReader(text));
        if(!Integer.valueOf(1).equals(root.get("schema")))throw new IllegalArgumentException("Expected review schema 1");
        var packs=new LinkedHashMap<String,String>();
        for(Config p:GunReviewCatalog.<Config>list(root,"packs")){
            String id=required(p,"id");
            if(packs.putIfAbsent(id,required(p,"name"))!=null)throw new IllegalArgumentException("Duplicate pack: "+id);
        }
        var guns=new LinkedHashMap<String,Entry>();
        for(Config g:GunReviewCatalog.<Config>list(root,"guns")){
            String id=required(g,"id"),display=g.getOrElse("display","tacz:default"),pack=required(g,"pack");
            if(!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||!display.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Invalid gun/display ID: "+id);
            if(!packs.containsKey(pack))throw new IllegalArgumentException("Unknown pack: "+pack);
            Object calibrated=g.getOrElse("calibrated",false);
            if(!(calibrated instanceof Boolean))throw new IllegalArgumentException("calibrated must be true/false: "+id);
            var entry=new Entry(pack,g.getOrElse("name",""),(Boolean)calibrated,g.getOrElse("notes",""));
            if(guns.putIfAbsent(id+"|"+display,entry)!=null)throw new IllegalArgumentException("Duplicate gun/display: "+id+"|"+display);
        }
        return new GunReviewCatalog(Map.copyOf(packs),Map.copyOf(guns));
    }
    private static <T> List<T> list(Config c,String key){return c.getOrElse(key,List.of());}
    private static String required(Config c,String key){Object v=c.get(key);if(!(v instanceof String s)||s.isBlank())throw new IllegalArgumentException("Missing "+key);return s;}
}
