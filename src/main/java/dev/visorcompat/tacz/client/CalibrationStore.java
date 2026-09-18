package dev.visorcompat.tacz.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.visorcompat.tacz.Calibration;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

public final class CalibrationStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("visor_tacz-calibration.json");
    private static Map<String, Calibration> saved;
    private static String loadError;
    public static String error() { load(); return loadError; }
    static String previewKey;
    static Calibration preview;
    private static void load() {
        if (saved != null) return;
        saved = new HashMap<>(dev.visorcompat.tacz.CalibrationDefaults.load());
        try {
            saved=dev.visorcompat.tacz.CalibrationFiles.read(FILE);
        } catch (Exception e) {
            // Preserve unreadable data; never silently replace the user's file.
            loadError = "Cannot read calibration file; fix it, then use Reload calibration.";
            com.mojang.logging.LogUtils.getLogger().error("Cannot read calibration file {}", FILE, e);
        }
    }
    public static void reload() throws IOException {
        if(preview!=null)throw new IOException("Save or cancel the calibration preview before reloading");
        try {
            var next=dev.visorcompat.tacz.CalibrationFiles.read(FILE);
            saved=next;loadError=null;
        } catch(IOException e) {
            loadError="Cannot reload calibration: "+e.getMessage()+". Fix the file and reload again.";
            throw new IOException(loadError,e);
        }
    }
    public static Calibration get(String key) { load(); return saved.getOrDefault(key, Calibration.ZERO); }
    public static Calibration render(String key) {
        return key.equals(previewKey) && preview != null ? preview : get(key);
    }
    public static void save(String key, Calibration value) throws IOException {
        if (!value.valid()) throw new IOException("Calibration is outside allowed limits");
        load();
        if (loadError != null) throw new IOException(loadError);
        var next = new TreeMap<>(saved);
        next.put(key,value);
        Files.createDirectories(FILE.getParent());
        Path temp = FILE.resolveSibling(FILE.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(next));
        Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
        saved = next;
    }
}
