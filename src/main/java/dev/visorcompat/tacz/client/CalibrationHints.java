package dev.visorcompat.tacz.client;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Bundled author-editable prose, loaded once at startup. */
public final class CalibrationHints {
    public record Hint(String title,String text,int color) {}
    private static Config config;
    public static Hint get(String key,String action) {
        if(config==null) {
            try(var stream=CalibrationHints.class.getResourceAsStream("/calibration-hints.toml")) {
                if(stream==null)throw new IllegalStateException("Missing calibration hints");
                config=new TomlParser().parse(new InputStreamReader(stream,StandardCharsets.UTF_8));
            } catch(Exception e) {throw new IllegalStateException("Invalid bundled calibration hints",e);}
        }
        String gun=key.split("\\|",2)[0];
        String title=config.getOrElse(List.of(gun,action,"title"),action);
        String text=config.getOrElse(List.of(gun,action,"hint"),"Adjust the preview, then Save & close.");
        String color=config.getOrElse(List.of(gun,action,"color"),"#FFFFFF");
        return new Hint(title,text,Integer.parseInt(color.replace("#",""),16));
    }
}
