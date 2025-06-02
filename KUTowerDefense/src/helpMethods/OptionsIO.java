package helpMethods;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import config.GameOptions;

/**
 * Loads / saves {@link GameOptions} as JSON.
 *
 *  ✔ Works on Java 11
 *  ✔ Pretty-prints JSON for humans
 *  ✔ Creates a default file on first launch
 *  ✔ Thread-safe (single Gson instance, sync around disk I/O)
 */
public final class OptionsIO {

    /** Where the JSON file lives – change if you prefer another folder. */
    private static final Path CONFIG_PATH =
            Paths.get(getOptionsDirectoryPath(), "options.json");

    private static final Path CONFIG_DIR  = Paths.get(getOptionsDirectoryPath());

    /** Re-use one configured Gson instance for the entire app. */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()   // keeps '<', '>' readable
            .create();

    /** Utility class – no instances allowed. */
    private OptionsIO() { }

    /**
     * Detects if we're running in a Maven project structure
     */
    private static boolean isMavenProject() {
        File pomFile = new File("pom.xml");
        return pomFile.exists();
    }

    /**
     * Gets the appropriate options directory path based on project structure
     */
    private static String getOptionsDirectoryPath() {
        if (isMavenProject()) {
            return "src/main/resources/Options";
        } else {
            return "resources/Options";
        }
    }

    // ---------------------------------------------------------------------
    //  PUBLIC API
    // ---------------------------------------------------------------------

    /**
     * Loads options from disk.  If the file doesn't exist or is empty/corrupt, a new one is
     * created with {@link GameOptions#defaults()}.
     *
     * @return never {@code null}
     */
    public static GameOptions load() {
        synchronized (GSON) {        // Gson is not 100 % threadsafe
            try {
                if (Files.notExists(CONFIG_PATH) || Files.size(CONFIG_PATH) == 0) {
                    GameOptions defaults = GameOptions.defaults();
                    save(defaults);  // creates folders + file
                    return defaults;
                }

                try (var reader = Files.newBufferedReader(CONFIG_PATH,
                        StandardCharsets.UTF_8)) {
                    GameOptions opts = GSON.fromJson(reader, GameOptions.class);
                    if (opts == null) {
                        // JSON was empty or only whitespace
                        opts = GameOptions.defaults();
                        save(opts);
                    }
                    return opts;
                }
            } catch (IOException | RuntimeException ex) {
                ex.printStackTrace();
                // Fallback – keep the game runnable even if JSON is corrupt
                GameOptions defaults = GameOptions.defaults();
                save(defaults);
                return defaults;
            }
        }
    }

    public static GameOptions load(String filename) {
        Path path = CONFIG_DIR.resolve(filename + ".json");
        synchronized (GSON) {
            try {
                if (Files.notExists(path) || Files.size(path) == 0) {
                    return null;
                }
                try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    return GSON.fromJson(reader, GameOptions.class);
                }
            } catch (IOException | RuntimeException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    /** write to the *default* options.json */
    public static void save(GameOptions opts) {
        save(opts, CONFIG_PATH);
    }

    /** write to a *custom* path (e.g. level1_slot0_options.json) */
    public static void save(GameOptions opts, String filename) {
        save(opts, Paths.get(getOptionsDirectoryPath(), filename + ".json"));
    }

    /** internal helper */
    private static void save(GameOptions opts, Path path) {
        synchronized (GSON) {
            try {
                Files.createDirectories(path.getParent());
                try (var writer = Files.newBufferedWriter(path,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE)) {
                    GSON.toJson(opts, writer);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Quick helper if you want a "Reset to Defaults" button.
     * Completely overwrites the existing JSON file.
     */
    public static void resetToDefaults() {
        save(GameOptions.defaults());
    }
}

