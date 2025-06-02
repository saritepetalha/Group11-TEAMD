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
     * Detects if we're running in a Maven project structure
     */
    private static boolean isMavenProject() {
        // Check if we're in a Maven project by looking for pom.xml in the expected location
        File pomFile = new File("demo/pom.xml");
        return pomFile.exists();
    }

    /**
     * Gets the appropriate saves directory path based on project structure
     */
    private static String getSavesDirectoryPath() {
        if (isMavenProject()) {
            return "demo/src/main/resources/Saves";
        } else {
            return "resources/Saves";
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