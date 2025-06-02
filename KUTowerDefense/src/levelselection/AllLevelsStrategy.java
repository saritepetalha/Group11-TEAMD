package levelselection;

import java.util.ArrayList;

import helpMethods.LoadSave;

/**
 * Strategy that shows all available levels for new game selection
 */
public class AllLevelsStrategy implements LevelSelectionStrategy {

    @Override
    public ArrayList<String> getLevelsToShow() {
        return LoadSave.getSavedLevels();
    }

    @Override
    public String getSelectionTitle() {
        return "Select Level";
    }

    @Override
    public String getSelectionDescription() {
        return "Choose a level to start playing";
    }
}