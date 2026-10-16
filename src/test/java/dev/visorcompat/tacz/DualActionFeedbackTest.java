package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DualActionFeedbackTest {
 @Test void reloadStartsOnceAndContinuedSnapshotsAreSilent(){assertEquals(1,DualActionFeedback.cue(0,1,false,true));assertEquals(0,DualActionFeedback.cue(1,1,true,true));}
 @Test void onlyFinishedHeldReloadConfirmsCompletion(){assertEquals(2,DualActionFeedback.cue(1,0,true,true));assertEquals(3,DualActionFeedback.cue(1,0,false,true));}
 @Test void HolsteringDuringFinishingCancelsInsteadOfReportingSuccess(){assertEquals(3,DualActionFeedback.cue(1,0,true,false));}
 @Test void boltHasDistinctStartAndCompletion(){assertEquals(4,DualActionFeedback.cue(0,2,false,true));assertEquals(2,DualActionFeedback.cue(2,0,false,true));assertEquals(3,DualActionFeedback.cue(2,0,false,false));}
}
