package managers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GameStateManager {
    private static final String SAVE_DIR = getSavesDirectoryPath();
    private static final String DEFAULT_SAVE_FILE = "defaultlevel.json";
    private final Gson gson;

    /**
     * Gets the appropriate saves directory path based on project structure
     */
    private static String getSavesDirectoryPath() {
        // Print current working directory for debugging
        String currentDir = System.getProperty("user.dir");
        System.out.println("🔍 GameStateManager: Current working directory: " + currentDir);

        // Try multiple possible paths in order of preference
        String[] possiblePaths = {
                "resources/Saves",                   // Top-level resources directory (PREFERRED)
                "./resources/Saves",                 // Explicit relative path to top-level resources
                currentDir + "/resources/Saves",     // Absolute path to top-level resources
                "src/main/resources/Saves",          // Standard Maven structure from project root
                "demo/src/main/resources/Saves",     // If running from parent directory
                "main/resources/Saves",              // If running from src directory
                "KUTowerDefense/resources/Saves",    // Legacy structure
                // Additional paths for IntelliJ
                "./src/main/resources/Saves",        // Explicit relative path
                "../src/main/resources/Saves",       // If running from target directory
                "../../src/main/resources/Saves",    // If running from deeper nested directory
                // Absolute path construction
                currentDir + "/src/main/resources/Saves",
                currentDir + "/demo/src/main/resources/Saves"
        };

        System.out.println("🔍 GameStateManager: Searching for Saves directory...");
        for (String path : possiblePaths) {
            File dir = new File(path);
            System.out.println("🔍 Checking path: " + path + " - exists: " + dir.exists() + ", isDirectory: " + dir.isDirectory());
            if (dir.exists() && dir.isDirectory()) {
                try {
                    String canonicalPath = dir.getCanonicalPath();
                    System.out.println("✅ GameStateManager: Found saves directory at: " + canonicalPath);
                    return canonicalPath;
                } catch (Exception e) {
                    String absolutePath = dir.getAbsolutePath();
                    System.out.println("✅ GameStateManager: Found saves directory at: " + absolutePath);
                    return absolutePath;
                }
            }
        }

        // If none found, default to top-level resources structure and create it
        String defaultPath = "resources/Saves";
        File defaultDir = new File(defaultPath);
        try {
            defaultDir.mkdirs();
            String createdPath = defaultDir.getCanonicalPath();
            System.out.println("✅ GameStateManager: Created saves directory at: " + createdPath);
            return createdPath;
        } catch (Exception e) {
            String createdPath = defaultDir.getAbsolutePath();
            System.out.println("✅ GameStateManager: Created saves directory at: " + createdPath);
            return createdPath;
        }
    }

    public GameStateManager() {
        System.out.println("🔍 GameStateManager: Initializing with save directory: " + SAVE_DIR);

        // Create saves directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            System.out.println("✅ GameStateManager: Saves directory ready: " + SAVE_DIR);
        } catch (IOException e) {
            System.err.println("❌ GameStateManager: Failed to create saves directory: " + e.getMessage());
        }

        // Initialize Gson with pretty printing
        gson = new GsonBuilder().setPrettyPrinting().create();
    }


    public void saveGameState(GameStateMemento memento, String saveFileName) {
        try {
            String json = gson.toJson(memento);
            File saveFile = new File(SAVE_DIR, saveFileName + ".json");
            System.out.println("🔍 GameStateManager: Attempting to save to: " + saveFile.getAbsolutePath());
            Files.write(saveFile.toPath(), json.getBytes());
            System.out.println("✅ GameStateManager: Game state saved to: " + saveFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ GameStateManager: Error saving game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteSaveFile(String saveFileName) {
        try {
            File saveFile = new File(SAVE_DIR, saveFileName + ".json");
            if (saveFile.exists()) {
                Files.delete(saveFile.toPath());
                System.out.println("✅ GameStateManager: Save file deleted: " + saveFile.getAbsolutePath());
            } else {
                System.out.println("🔍 GameStateManager: Save file not found for deletion: " + saveFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ GameStateManager: Error deleting save file: " + e.getMessage());
        }
    }


    public GameStateMemento loadGameState(String saveFileName) {
        // Ensure the filename has .json extension
        if (!saveFileName.toLowerCase().endsWith(".json")) {
            saveFileName += ".json";
        }

        Path savePath = Paths.get(SAVE_DIR, saveFileName);
        System.out.println("🔍 GameStateManager: Attempting to load from: " + savePath.toAbsolutePath());

        if (!Files.exists(savePath)) {
            System.out.println("❌ GameStateManager: No save file found at " + savePath.toAbsolutePath());
            return null;
        }

        try (FileReader reader = new FileReader(savePath.toFile())) {
            GameStateMemento memento = gson.fromJson(reader, GameStateMemento.class);
            System.out.println("✅ GameStateManager: Game state loaded from " + savePath.toAbsolutePath());
            return memento;
        } catch (IOException e) {
            System.err.println("❌ GameStateManager: Failed to load game state: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public boolean saveFileExists(String saveFileName) {
        // Ensure the filename has .json extension
        if (!saveFileName.toLowerCase().endsWith(".json")) {
            saveFileName += ".json";
        }
        Path savePath = Paths.get(SAVE_DIR, saveFileName);
        boolean exists = Files.exists(savePath);
        System.out.println("🔍 GameStateManager: Checking if save file exists: " + savePath.toAbsolutePath() + " - " + exists);
        return exists;
    }
}