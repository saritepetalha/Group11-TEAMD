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
 *  ✔ Enhanced to preserve custom settings and manage difficulty separately
 */
public final class OptionsIO {

    /** Where the JSON files live – change if you prefer another folder. */
    private static final Path CONFIG_PATH =
            Paths.get(getOptionsDirectoryPath(), "options.json");
    
    /** Backup of original default settings - never modified during gameplay */
    private static final Path DEFAULT_CONFIG_PATH =
            Paths.get(getOptionsDirectoryPath(), "default_options.json");
    
    /** User's custom settings - preserved across difficulty changes */
    private static final Path CUSTOM_CONFIG_PATH =
            Paths.get(getOptionsDirectoryPath(), "custom_options.json");

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
        return loadFromPath(CONFIG_PATH, true);
    }

    /**
     * Loads difficulty-specific options or custom options
     * @param filename The filename (e.g., "easy", "normal", "hard", "custom")
     * @return GameOptions or null if file doesn't exist
     */
    public static GameOptions load(String filename) {
        Path path;
        
        // Special handling for "custom" - load from custom_options.json
        if ("custom".equalsIgnoreCase(filename)) {
            path = CUSTOM_CONFIG_PATH;
            System.out.println("OptionsIO.load: Loading custom settings from: " + path.toAbsolutePath());
        } else {
            path = CONFIG_DIR.resolve(filename + ".json");
            System.out.println("OptionsIO.load: Loading difficulty settings from: " + path.toAbsolutePath());
        }
        
        return loadFromPath(path, false);
    }

    /**
     * Loads the original default settings (for reset functionality)
     * @return GameOptions with default settings
     */
    public static GameOptions loadDefaults() {
        System.out.println("OptionsIO.loadDefaults(): Loading default settings from: " + DEFAULT_CONFIG_PATH.toAbsolutePath());
        GameOptions defaults = loadFromPath(DEFAULT_CONFIG_PATH, false);
        
        if (defaults == null) {
            // If default_options.json doesn't exist, create it with hardcoded defaults
            defaults = GameOptions.defaults();
            saveDefaults(defaults);
            System.out.println("OptionsIO.loadDefaults(): Created default_options.json with hardcoded defaults");
        }
        
        return defaults;
    }

    /**
     * Saves user's custom settings (preserves them across difficulty changes)
     * @param opts The custom options to save
     */
    public static void saveCustom(GameOptions opts) {
        System.out.println("OptionsIO.saveCustom(): Saving custom settings to: " + CUSTOM_CONFIG_PATH.toAbsolutePath());
        save(opts, CUSTOM_CONFIG_PATH);
    }

    /**
     * Saves the default settings backup (should only be called once during initialization)
     * @param opts The default options to save
     */
    public static void saveDefaults(GameOptions opts) {
        synchronized (GSON) {
            // Only save defaults if the file doesn't exist to preserve original defaults
            if (Files.notExists(DEFAULT_CONFIG_PATH)) {
                System.out.println("OptionsIO.saveDefaults(): Saving default settings to: " + DEFAULT_CONFIG_PATH.toAbsolutePath());
                save(opts, DEFAULT_CONFIG_PATH);
            } else {
                System.out.println("OptionsIO.saveDefaults(): Default settings file already exists, not overwriting");
            }
        }
    }

    /**
     * Saves options for temporary gameplay use (does NOT overwrite custom settings)
     * This is used during gameplay when a difficulty is selected
     * @param opts The options to use for current game session
     */
    public static void saveForGameplay(GameOptions opts) {
        System.out.println("OptionsIO.saveForGameplay(): Saving temporary gameplay settings to: " + CONFIG_PATH.toAbsolutePath());
        save(opts, CONFIG_PATH);
    }

    /**
     * Restores custom settings to options.json (call after gameplay ends)
     */
    public static void restoreCustomSettings() {
        GameOptions customSettings = load("custom");
        if (customSettings != null) {
            System.out.println("OptionsIO.restoreCustomSettings(): Restoring custom settings to options.json");
            save(customSettings, CONFIG_PATH);
        } else {
            System.out.println("OptionsIO.restoreCustomSettings(): No custom settings found, keeping current options.json");
        }
    }

    /**
     * Internal helper method to load from a specific path
     */
    private static GameOptions loadFromPath(Path path, boolean createDefaultIfMissing) {
        synchronized (GSON) {
            try {
                if (Files.notExists(path) || Files.size(path) == 0) {
                    if (createDefaultIfMissing) {
                        GameOptions defaults = GameOptions.defaults();
                        save(defaults, path);
                        // Also ensure we have a backup of the defaults
                        saveDefaults(defaults);
                        System.out.println("OptionsIO.loadFromPath(): Created new file at: " + path);
                        return defaults;
                    } else {
                        System.out.println("OptionsIO.loadFromPath(): File not found: " + path);
                        return null;
                    }
                }

                try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    GameOptions opts = GSON.fromJson(reader, GameOptions.class);
                    if (opts == null) {
                        if (createDefaultIfMissing) {
                            opts = GameOptions.defaults();
                            save(opts, path);
                            saveDefaults(opts);
                            System.out.println("OptionsIO.loadFromPath(): JSON was empty, used defaults for: " + path);
                        }
                    } else {
                        System.out.println("OptionsIO.loadFromPath(): Successfully loaded: " + path + ". Starting gold: " + opts.getStartingGold());
                    }
                    return opts;
                }
            } catch (IOException | RuntimeException ex) {
                System.out.println("OptionsIO.loadFromPath(): Exception loading: " + path + " - " + ex.getMessage());
                ex.printStackTrace();
                
                if (createDefaultIfMissing) {
                    // Fallback – keep the game runnable even if JSON is corrupt
                    GameOptions defaults = GameOptions.defaults();
                    save(defaults, path);
                    saveDefaults(defaults);
                    return defaults;
                }
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
                System.out.println("OptionsIO.save(): Successfully saved to: " + path.toAbsolutePath());
            } catch (IOException ex) {
                System.err.println("OptionsIO.save(): Failed to save to: " + path.toAbsolutePath());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Quick helper if you want a "Reset to Defaults" button.
     * Resets both options.json and custom_options.json to defaults.
     */
    public static void resetToDefaults() {
        GameOptions defaults = loadDefaults();
        save(defaults, CONFIG_PATH);
        save(defaults, CUSTOM_CONFIG_PATH);
        System.out.println("OptionsIO.resetToDefaults(): Reset both options.json and custom_options.json to defaults");
    }

    /**
     * Initializes the options system - call this once at application startup
     * This ensures all necessary files exist and custom settings are preserved
     */
    public static void initialize() {
        System.out.println("OptionsIO.initialize(): Initializing options system...");
        
        // Load or create default options.json
        GameOptions currentOptions = load();
        
        // Ensure we have a backup of defaults
        saveDefaults(currentOptions);
        
        // If custom_options.json doesn't exist, create it with current settings
        if (Files.notExists(CUSTOM_CONFIG_PATH)) {
            saveCustom(currentOptions);
            System.out.println("OptionsIO.initialize(): Created custom_options.json with current settings");
        }
        
        System.out.println("OptionsIO.initialize(): Options system initialized successfully");
    }
}

