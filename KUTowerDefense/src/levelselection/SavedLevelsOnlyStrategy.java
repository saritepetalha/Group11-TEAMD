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
            // Check for .save files in the saves/ directory (where PlayingModel saves them)
            File saveFile = new File("saves", levelName + ".save");
            boolean exists = saveFile.exists();

            if (exists) {
                System.out.println("Found save file: " + saveFile.getAbsolutePath());
            } else {
                System.out.println("No save file found for: " + levelName + " at " + saveFile.getAbsolutePath());
            }

            return exists;
        } catch (Exception e) {
            System.err.println("Error checking save file for level: " + levelName + " - " + e.getMessage());
            return false;
        }
    }
}