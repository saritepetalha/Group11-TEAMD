package helpMethods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.GameOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

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
            Paths.get("resources", "Options", "options.json");

    /** Re-use one configured Gson instance for the entire app. */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()   // keeps '<', '>' readable
            .create();

    /** Utility class – no instances allowed. */
    private OptionsIO() { }

    // ---------------------------------------------------------------------
    //  PUBLIC API
    // ---------------------------------------------------------------------

    /**
     * Loads options from disk.  If the file doesn’t exist, a new one is
     * created with {@link GameOptions#defaults()}.
     *
     * @return never {@code null}
     */
    public static GameOptions load() {
        synchronized (GSON) {        // Gson is not 100 % threadsafe
            try {
                if (Files.notExists(CONFIG_PATH)) {
                    GameOptions defaults = GameOptions.defaults();
                    save(defaults);  // creates folders + file
                    return defaults;
                }

                try (var reader = Files.newBufferedReader(CONFIG_PATH,
                        StandardCharsets.UTF_8)) {
                    return GSON.fromJson(reader, GameOptions.class);
                }
            } catch (IOException | RuntimeException ex) {
                ex.printStackTrace();
                // Fallback – keep the game runnable even if JSON is corrupt
                return GameOptions.defaults();
            }
        }
    }

    /**
     * Serialises the given options back to <kbd>options.json</kbd>.
     * Call it when the player hits “Save” in your Options Scene.
     */
    public static void save(GameOptions opts) {
        synchronized (GSON) {
            try {
                Files.createDirectories(CONFIG_PATH.getParent());

                try (var writer = Files.newBufferedWriter(CONFIG_PATH,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE)) {
                    GSON.toJson(opts, writer);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                // Decide if you want to show an in-game popup; here we just log.
            }
        }
    }

    /**
     * Quick helper if you want a “Reset to Defaults” button.
     * Completely overwrites the existing JSON file.
     */
    public static void resetToDefaults() {
        save(GameOptions.defaults());
    }
}
