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
     * Gets the appropriate options directory path based on project structure
     */
    private static String getOptionsDirectoryPath() {
        // Try multiple possible paths in order of preference
        String[] possiblePaths = {
                "KUTowerDefense/resources/Options",    // Correct location with difficulty files - try FIRST
                "src/main/resources/Options",          // Standard Maven structure from project root
                "demo/src/main/resources/Options",     // If running from parent directory
                "main/resources/Options",              // If running from src directory
                "resources/Options"                    // If running from src/main directory (wrong location)
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                try {
                    String canonicalPath = dir.getCanonicalPath();
                    System.out.println("OptionsIO: Using options directory: " + canonicalPath);
                    return canonicalPath;
                } catch (Exception e) {
                    String absolutePath = dir.getAbsolutePath();
                    System.out.println("OptionsIO: Using options directory (fallback): " + absolutePath);
                    return absolutePath;
                }
            }
        }

        // If none found, default to Maven structure and create it
        String defaultPath = "src/main/resources/Options";
        File defaultDir = new File(defaultPath);
        try {
            defaultDir.mkdirs();
            String canonicalPath = defaultDir.getCanonicalPath();
            System.out.println("OptionsIO: Created and using options directory: " + canonicalPath);
            return canonicalPath;
        } catch (Exception e) {
            String absolutePath = defaultDir.getAbsolutePath();
            System.out.println("OptionsIO: Created and using options directory (fallback): " + absolutePath);
            return absolutePath;
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
        System.out.println("OptionsIO.load(): Loading from default path: " + CONFIG_PATH.toAbsolutePath());
        synchronized (GSON) {        // Gson is not 100 % threadsafe
            try {
                if (Files.notExists(CONFIG_PATH) || Files.size(CONFIG_PATH) == 0) {
                    GameOptions defaults = GameOptions.defaults();
                    save(defaults);  // creates folders + file
                    System.out.println("OptionsIO.load(): Created new default options.json");
                    return defaults;
                }

                try (var reader = Files.newBufferedReader(CONFIG_PATH,
                        StandardCharsets.UTF_8)) {
                    GameOptions opts = GSON.fromJson(reader, GameOptions.class);
                    if (opts == null) {
                        // JSON was empty or only whitespace
                        opts = GameOptions.defaults();
                        save(opts);
                        System.out.println("OptionsIO.load(): JSON was empty, used defaults");
                    } else {
                        System.out.println("OptionsIO.load(): Successfully loaded options.json. Starting gold: " + opts.getStartingGold());
                    }
                    return opts;
                }
            } catch (IOException | RuntimeException ex) {
                System.out.println("OptionsIO.load(): Exception loading options.json: " + ex.getMessage());
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
        System.out.println("OptionsIO.load: Trying to load from: " + path.toAbsolutePath());
        System.out.println("OptionsIO.load: File exists: " + Files.exists(path));
        synchronized (GSON) {
            try {
                if (Files.notExists(path) || Files.size(path) == 0) {
                    System.out.println("OptionsIO.load: File not found or empty: " + path);
                    return null;
                }
                try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    GameOptions options = GSON.fromJson(reader, GameOptions.class);
                    System.out.println("OptionsIO.load: Successfully loaded " + filename + ". Starting gold: " + (options != null ? options.getStartingGold() : "NULL"));
                    return options;
                }
            } catch (IOException | RuntimeException ex) {
                System.out.println("OptionsIO.load: Exception loading " + filename + ": " + ex.getMessage());
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

