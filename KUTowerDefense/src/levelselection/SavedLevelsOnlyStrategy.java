package levelselection;

import java.io.File;
import java.util.ArrayList;

import helpMethods.LoadSave;

/**
 * Strategy that shows only levels that have saved game states
 */
public class SavedLevelsOnlyStrategy implements LevelSelectionStrategy {

    @Override
    public ArrayList<String> getLevelsToShow() {
        ArrayList<String> allLevels = LoadSave.getSavedLevels();
        ArrayList<String> levelsWithSaves = new ArrayList<>();

        // Check which levels have corresponding save files
        for (String levelName : allLevels) {
            if (hasSaveFile(levelName)) {
                levelsWithSaves.add(levelName);
            }
        }

        return levelsWithSaves;
    }

    @Override
    public String getSelectionTitle() {
        return "Load Game";
    }

    @Override
    public String getSelectionDescription() {
        return "Choose a saved game to continue";
    }

    /**
     * Checks if a level has a corresponding save file
     * @param levelName Name of the level to check
     * @return true if save file exists, false otherwise
     */
    private boolean hasSaveFile(String levelName) {
        try {
            // Use the same path detection logic as GameStateManager
            String currentDir = System.getProperty("user.dir");

            // Check for JSON save files (prioritizing top-level resources directory)
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

            for (String path : possiblePaths) {
                File savesDir = new File(path);
                if (savesDir.exists() && savesDir.isDirectory()) {
                    File saveFile = new File(savesDir, levelName + ".json");
                    if (saveFile.exists()) {
                        System.out.println("✅ SavedLevelsOnlyStrategy: Found save file: " + saveFile.getAbsolutePath());
                        return true;
                    }
                }
            }

            System.out.println("❌ SavedLevelsOnlyStrategy: No save file found for: " + levelName);
            return false;
        } catch (Exception e) {
            System.err.println("❌ SavedLevelsOnlyStrategy: Error checking save file for level: " + levelName + " - " + e.getMessage());
            return false;
        }
    }
}