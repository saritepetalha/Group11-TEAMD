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
            // Try multiple possible paths in order of preference
            String[] possiblePaths = {
                    "src/main/resources/Saves",          // Standard Maven structure from project root
                    "demo/src/main/resources/Saves",     // If running from parent directory
                    "main/resources/Saves",              // If running from src directory
                    "resources/Saves",                   // If running from src/main directory
                    "KUTowerDefense/resources/Saves"     // Legacy structure
            };

            for (String path : possiblePaths) {
                File savesDir = new File(path);
                if (savesDir.exists() && savesDir.isDirectory()) {
                    File saveFile = new File(savesDir, levelName + ".json");
                    if (saveFile.exists()) {
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error checking save file for level: " + levelName + " - " + e.getMessage());
            return false;
        }
    }
}