package dev.visorcompat.tacz;

import org.joml.Vector3f;

/** Gun-local offsets, except pouch which uses head-yaw-local right/up/back. */
public record InteractionOffsets(Point support,Point magazine,Point rack,Point selector,Point pouch,Point sight,Point port) {
    public record Point(float x,float y,float z) {
        public static final Point ZERO=new Point(0,0,0);
        public Vector3f vector(){return new Vector3f(x,y,z);}
        public boolean valid(){return Float.isFinite(x)&&Float.isFinite(y)&&Float.isFinite(z)
            && Math.abs(x)<=.5f && Math.abs(y)<=.5f && Math.abs(z)<=.5f;}
    }
    public static final InteractionOffsets ZERO=new InteractionOffsets(null,null,null,null,null,null,null);
    public InteractionOffsets(Point support,Point magazine,Point rack,Point selector,Point pouch,Point sight) {this(support,magazine,rack,selector,pouch,sight,null);}
    public InteractionOffsets {
        support=support==null?Point.ZERO:support;magazine=magazine==null?Point.ZERO:magazine;
        rack=rack==null?Point.ZERO:rack;selector=selector==null?Point.ZERO:selector;
        pouch=pouch==null?Point.ZERO:pouch;sight=sight==null?Point.ZERO:sight;port=port==null?Point.ZERO:port;
    }
    public Point[] points(){return new Point[]{support,magazine,rack,selector,pouch,sight,port};}
    public boolean valid(){for(var p:points())if(!p.valid())return false;return true;}
}
