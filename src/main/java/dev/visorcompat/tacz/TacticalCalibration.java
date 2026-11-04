package dev.visorcompat.tacz;

/** Per-gun tactical poses in metres/degrees; tolerances describe a neutral pose range. */
public record TacticalCalibration(InspectionCalibration normalGrip, InspectionCalibration pinchGrip,
        InspectionCalibration magwellEntry, float pitchTolerance, float yawTolerance, float rollTolerance,
        float entryRadius, float seatedDepth, float seatedTolerance, boolean alternativeControls) {
    public TacticalCalibration(InspectionCalibration normalGrip,InspectionCalibration pinchGrip,InspectionCalibration magwellEntry,float pitchTolerance,float yawTolerance,float rollTolerance,float entryRadius,float seatedDepth,float seatedTolerance){this(normalGrip,pinchGrip,magwellEntry,pitchTolerance,yawTolerance,rollTolerance,entryRadius,seatedDepth,seatedTolerance,false);}
    public static final TacticalCalibration DEFAULT=new TacticalCalibration(MagazineGripFiles.DEFAULT,
            new InspectionCalibration(.045f,0,0,0,30,0),new InspectionCalibration(0,-.06f,0,0,0,0),35,35,35,.08f,.06f,.015f);
    public boolean valid(){return normalGrip!=null&&normalGrip.valid()&&pinchGrip!=null&&pinchGrip.valid()
            &&magwellEntry!=null&&magwellEntry.valid()&&range(pitchTolerance,0,180)&&range(yawTolerance,0,180)
            &&range(rollTolerance,0,180)&&range(entryRadius,.005f,.5f)&&range(seatedDepth,0,.5f)&&range(seatedTolerance,.001f,.2f);}
    private static boolean range(float n,float min,float max){return Float.isFinite(n)&&n>=min&&n<=max;}
}


