package dev.visorcompat.tacz;
import org.joml.Vector3fc;
import java.util.List;

/** Full box dimensions in metres at VR scale 1, independent of model size. */
public record ZoneSizes(Box support,Box magazine,Box rack,Box selector,Box pouch,Box sight,Box port,Box release) {
    public ZoneSizes(Box a,Box b,Box c,Box d,Box e,Box f,Box g){this(a,b,c,d,e,f,g,null);}
    public enum Zone { SUPPORT, MAGAZINE, RACK, SELECTOR, POUCH, SIGHT, PORT, RELEASE }
    public record Box(float width,float height,float depth) {
        public boolean valid(){return bound(width)&&bound(height)&&bound(depth);}
        private static boolean bound(float v){return Float.isFinite(v)&&v>=.01f&&v<=1.5f;}
        public boolean contains(Vector3fc point,Vector3fc center){
            return Math.abs(point.x()-center.x())<=width/2 && Math.abs(point.y()-center.y())<=height/2 && Math.abs(point.z()-center.z())<=depth/2;
        }
    }
    private static Box cube(float size){return new Box(size,size,size);}
    public static final ZoneSizes DEFAULT=new ZoneSizes(null,null,null,null,null,null,null);
    public ZoneSizes {
        support=support==null?cube(.30f):support;magazine=magazine==null?cube(.18f):magazine;
        rack=rack==null?cube(.21f):rack;selector=selector==null?cube(.08f):selector;
        pouch=pouch==null?cube(.50f):pouch;sight=sight==null?new Box(.07f,.07f,.73f):sight;
        port=port==null?cube(.07f):port;release=release==null?cube(.06f):release;
    }
    public List<Box> boxes(){return List.of(support,magazine,rack,selector,pouch,sight,port,release);}
    public Box get(Zone zone){return boxes().get(zone.ordinal());}
    public boolean valid(){return boxes().stream().allMatch(Box::valid);}
    public ZoneSizes resizeMounted(float ratio) {
        if(!Float.isFinite(ratio) || ratio<=0)throw new IllegalArgumentException("Invalid size ratio");
        var result=new Box[8];
        for(var zone:Zone.values()) {
            var b=get(zone);
            result[zone.ordinal()]=zone==Zone.POUCH?b:new Box(clamp(b.width()*ratio),clamp(b.height()*ratio),clamp(b.depth()*ratio));
        }
        return new ZoneSizes(result[0],result[1],result[2],result[3],result[4],result[5],result[6],result[7]);
    }
    private static float clamp(float v){return Math.max(.01f,Math.min(1.5f,v));}
}
