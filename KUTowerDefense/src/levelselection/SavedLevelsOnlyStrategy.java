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
            // Get the saves directory path using the same method as GameStateManager
            File projectRoot = findProjectRoot();

            // Check if we have a demo subdirectory structure
            File demoDir = new File(projectRoot, "demo");
            File savesDir;
            if (demoDir.exists() && new File(demoDir, "pom.xml").exists()) {
                savesDir = new File(demoDir, "resources/Saves");
            } else {
                savesDir = new File(projectRoot, "resources/Saves");
            }

            File saveFile = new File(savesDir, levelName + ".json");
            return saveFile.exists();
        } catch (Exception e) {
            System.err.println("Error checking save file for level: " + levelName + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Finds the project root directory by looking for key indicators
     */
    private File findProjectRoot() {
        File currentDir = new File(System.getProperty("user.dir"));
        File checkDir = currentDir;

        // Look for project root indicators going up the directory tree
        for (int i = 0; i < 5; i++) { // Limit search to 5 levels up
            // Check for Maven project root indicators
            if (new File(checkDir, "pom.xml").exists() ||
                    new File(checkDir, "demo/pom.xml").exists()) {
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
}