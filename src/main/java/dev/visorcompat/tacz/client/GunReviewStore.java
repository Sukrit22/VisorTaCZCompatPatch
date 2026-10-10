package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.GunReviewCatalog;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import net.minecraftforge.fml.loading.FMLPaths;

public final class GunReviewStore {
    public static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("visor_tacz/gun-reviews.toml");
    private static GunReviewCatalog catalog;
    private static boolean failed;
    public static GunReviewCatalog get(){
        if(catalog==null){catalog=new GunReviewCatalog(Map.of(),Map.of());try{reload();}catch(IOException e){com.mojang.logging.LogUtils.getLogger().error("Cannot load gun review metadata: {}",e.getMessage());}}
        return catalog;
    }
    public static boolean failed(){get();return failed;}
    public static void reload() throws IOException {
        try{
            if(!Files.exists(FILE)){
                Files.createDirectories(FILE.getParent());
                try(var in=GunReviewStore.class.getResourceAsStream("/gun-reviews.toml")){
                    if(in==null)throw new IOException("Missing bundled gun-reviews.toml");
                    Files.copy(in,FILE);
                }
            }
            if(Files.size(FILE)>1_048_576)throw new IOException("Review file exceeds 1 MiB");
            var next=GunReviewCatalog.parse(Files.readString(FILE));catalog=next;failed=false;
        }catch(Exception e){failed=true;throw new IOException("Cannot read "+FILE+": "+e.getMessage(),e);}
    }
}
