package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GunReviewCatalogTest {
    @Test void bundledChecklistCoversEveryBundledGunWithoutAssumingVerification() throws Exception {
        try(var reviews=getClass().getResourceAsStream("/gun-reviews.toml");var profiles=getClass().getResourceAsStream("/gun-profiles.toml")){
            var catalog=GunReviewCatalog.parse(new String(reviews.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));
            var guns=DescriptorProfiles.parse(new String(profiles.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));
            assertEquals(guns.size(),catalog.guns().size());
            for(String id:guns.keySet())assertFalse(catalog.guns().get(id+"|tacz:default").calibrated());
        }
    }
    private static final String PACK="schema=1\n[[packs]]\nid='custom'\nname='Custom pack'\n";
    private static final String GUN="[[guns]]\nid='pack:pistol'\npack='custom'\ncalibrated=true\n";
    @Test void exactDisplaySeparatesReviewEvenWithinSamePack(){
        var c=GunReviewCatalog.parse(PACK+GUN+GUN.replace("calibrated=true","display='pack:skin'\ncalibrated=false"));
        assertTrue(c.guns().get("pack:pistol|tacz:default").calibrated());
        assertFalse(c.guns().get("pack:pistol|pack:skin").calibrated());
        assertNull(c.guns().get("pack:other|tacz:default"));
    }
    @Test void rejectsAmbiguousOrMistypedMetadata(){
        assertThrows(IllegalArgumentException.class,()->GunReviewCatalog.parse(PACK+GUN+GUN));
        assertThrows(IllegalArgumentException.class,()->GunReviewCatalog.parse(PACK+GUN.replace("pack='custom'","pack='missing'")));
        assertThrows(IllegalArgumentException.class,()->GunReviewCatalog.parse(PACK+GUN.replace("calibrated=true","calibrated='yes'")));
    }
}
