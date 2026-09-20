package dev.visorcompat.tacz;
import java.util.ArrayList;
import java.util.List;
import static dev.visorcompat.tacz.ZoneSizes.Zone.*;

/** Capabilities are code-owned; hint text cannot turn on unsupported actions. */
public final class CalibrationLayout {
    public record Page(String id,int offset,int count,ZoneSizes.Zone zone) {}
    public static List<Page> pages(String key) {
        String gun=key.split("\\|",2)[0];var profile=Profiles.byId(gun);var pages=new ArrayList<Page>();
        pages.add(new Page("grip",0,3,null));pages.add(new Page("rotation",3,3,null));
        pages.add(new Page("scale",30,1,null));pages.add(new Page("muzzle",6,3,null));
        if(profile!=null && profile.supportDistance()>0 && !profile.pump())pages.add(new Page("support",9,3,SUPPORT));
        pages.add(new Page("magazine",12,3,MAGAZINE));pages.add(new Page("rack",15,3,RACK));
        if(profile!=null && profile.selector())pages.add(new Page("selector",18,3,SELECTOR));
        pages.add(new Page("pouch",21,3,POUCH));pages.add(new Page("sight",24,3,SIGHT));
        // Manual-action ports are visual only: it has no casing-pluck interaction.
        pages.add(new Page("port",27,3,profile!=null && profile.manualAction()?null:PORT));
        return List.copyOf(pages);
    }
}
