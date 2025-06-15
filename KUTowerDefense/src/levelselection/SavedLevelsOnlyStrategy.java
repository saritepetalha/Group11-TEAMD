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
        ArrayList<String> levelsWithSaves = new ArrayList<>();

        // Find all save files directly from the saves directory
        try {
            String currentDir = System.getProperty("user.dir");

            // Check for save files in all possible paths
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
                    File[] saveFiles = savesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
                    if (saveFiles != null) {
                        for (File saveFile : saveFiles) {
                            String fileName = saveFile.getName();
                            // Remove .json extension
                            String saveFileName = fileName.substring(0, fileName.length() - 5);

                            // Only add if not already in the list
                            if (!levelsWithSaves.contains(saveFileName)) {
                                levelsWithSaves.add(saveFileName);
                                System.out.println("✅ SavedLevelsOnlyStrategy: Found save file: " + saveFileName);
                            }
                        }
                    }
                    break; // Found a valid saves directory, use it
                }
            }
        } catch (Exception e) {
            System.err.println("❌ SavedLevelsOnlyStrategy: Error scanning save files: " + e.getMessage());
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
     * Gets a user-friendly display name for a save file
     * For saves like "level1_saveno_2", returns "level1 (Save 2)"
     * For regular saves like "level1", returns "level1"
     * @param saveFileName The save file name (without .json extension)
     * @return User-friendly display name
     */
    public static String getDisplayName(String saveFileName) {
        if (saveFileName.contains("_saveno_")) {
            String[] parts = saveFileName.split("_saveno_");
            if (parts.length == 2) {
                String levelName = parts[0];
                String saveNumber = parts[1];
                return levelName + " (Save " + saveNumber + ")";
            }
        }
        return saveFileName;
    }

    /**
     * Gets the base level name from a save file name
     * For saves like "level1_saveno_2", returns "level1"
     * For regular saves like "level1", returns "level1"
     * @param saveFileName The save file name (without .json extension)
     * @return Base level name
     */
    public static String getBaseLevelName(String saveFileName) {
        if (saveFileName.contains("_saveno_")) {
            String[] parts = saveFileName.split("_saveno_");
            if (parts.length >= 1) {
                return parts[0];
            }
        }
        return saveFileName;
    }
}