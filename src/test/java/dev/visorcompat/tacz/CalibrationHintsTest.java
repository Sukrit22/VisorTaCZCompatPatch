package dev.visorcompat.tacz;
import dev.visorcompat.tacz.client.CalibrationHints;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalibrationHintsTest {
    @Test void everySupportedPageHasBundledGunSpecificText(){
        for(String gun:new String[]{"tacz:glock_17","tacz:m4a1","tacz:m870","tacz:m700","tacz:hk_mp5a5","tacz:m1911","tacz:p320","tacz:m9a4","tacz:deagle","tacz:deagle_golden","tacz:timeless50","tacz:b93r","tacz:cz75","tacz:hk_mk23","tacz:rhino357","tacz:taurus500","tacz:taurus943","tacz:lonetrail"})
            for(var page:CalibrationLayout.pages(gun+"|tacz:default")){
                var hint=CalibrationHints.get(gun,page.id());
                assertNotEquals(page.id(),hint.title());assertTrue(hint.text().length()>20);
                assertTrue(hint.color()>=0 && hint.color()<=0xFFFFFF);
            }
    }
    @Test void unsupportedActionsAreNotOffered(){
        var pump=CalibrationLayout.pages("tacz:m870|tacz:default");
        assertTrue(pump.stream().noneMatch(p->p.id().equals("support")||p.id().equals("selector")));
        assertEquals(ZoneSizes.Zone.PORT,pump.stream().filter(p->p.id().equals("port")).findFirst().orElseThrow().zone());
        assertTrue(CalibrationLayout.pages("tacz:m4a1|tacz:default").stream().anyMatch(p->p.id().equals("support")));
    }
}
