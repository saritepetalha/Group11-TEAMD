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
     * Finds the project root directory by looking for key indicators
     */
    private static File findProjectRoot() {
        File currentDir = new File(System.getProperty("user.dir"));
        File checkDir = currentDir;

        // Look for project root indicators going up the directory tree
        for (int i = 0; i < 5; i++) { // Limit search to 5 levels up
            // Check for Maven project root indicators
            if (new File(checkDir, "pom.xml").exists() ||
                    new File(checkDir, "demo/pom.xml").exists() ||
                    (new File(checkDir, "src/main/resources").exists() && new File(checkDir, "pom.xml").exists())) {
                return checkDir;
            }

            // Check if we're inside a demo directory structure
            if (checkDir.getName().equals("demo") && new File(checkDir, "pom.xml").exists()) {
                return checkDir;
            }

            File parent = checkDir.getParentFile();
            if (parent == null) break;
            checkDir = parent;
        }

        // If no clear project root found, return current directory
        return currentDir;
    }

    /**
     * Gets the appropriate saves directory path based on project structure
     */
    private static String getSavesDirectoryPath() {
        File projectRoot = findProjectRoot();

        // Check if we have a demo subdirectory structure
        File demoDir = new File(projectRoot, "demo");
        if (demoDir.exists() && new File(demoDir, "pom.xml").exists()) {
            File defaultPath = new File(demoDir, "src/main/resources/Saves");
            try {
                return defaultPath.getCanonicalPath();
            } catch (Exception e) {
                return defaultPath.getAbsolutePath();
            }
        } else {
            File defaultPath = new File(projectRoot, "src/main/resources/Saves");
            try {
                return defaultPath.getCanonicalPath();
            } catch (Exception e) {
                return defaultPath.getAbsolutePath();
            }
        }
    }

    public GameStateManager() {
        // Create saves directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create saves directory: " + e.getMessage());
        }

        // Initialize Gson with pretty printing
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveGameState(GameStateMemento memento) {
        saveGameState(memento, DEFAULT_SAVE_FILE);
    }

    public void saveGameState(GameStateMemento memento, String saveFileName) {
        try {
            String json = gson.toJson(memento);
            File saveFile = new File(SAVE_DIR, saveFileName + ".json");
            Files.write(saveFile.toPath(), json.getBytes());
            System.out.println("Game state saved to: " + saveFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving game state: " + e.getMessage());
        }
    }

    public void deleteSaveFile(String saveFileName) {
        try {
            File saveFile = new File(SAVE_DIR, saveFileName + ".json");
            if (saveFile.exists()) {
                Files.delete(saveFile.toPath());
                System.out.println("Save file deleted: " + saveFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error deleting save file: " + e.getMessage());
        }
    }

    public GameStateMemento loadGameState() {
        return loadGameState(DEFAULT_SAVE_FILE);
    }

    public GameStateMemento loadGameState(String saveFileName) {
        // Ensure the filename has .json extension
        if (!saveFileName.toLowerCase().endsWith(".json")) {
            saveFileName += ".json";
        }

        Path savePath = Paths.get(SAVE_DIR, saveFileName);
        if (!Files.exists(savePath)) {
            System.out.println("No save file found at " + savePath);
            return null;
        }

        try (FileReader reader = new FileReader(savePath.toFile())) {
            GameStateMemento memento = gson.fromJson(reader, GameStateMemento.class);
            System.out.println("Game state loaded from " + savePath);
            return memento;
        } catch (IOException e) {
            System.err.println("Failed to load game state: " + e.getMessage());
            return null;
        }
    }

    public boolean saveFileExists() {
        return saveFileExists(DEFAULT_SAVE_FILE);
    }

    public boolean saveFileExists(String saveFileName) {
        // Ensure the filename has .json extension
        if (!saveFileName.toLowerCase().endsWith(".json")) {
            saveFileName += ".json";
        }
        return Files.exists(Paths.get(SAVE_DIR, saveFileName));
    }
}