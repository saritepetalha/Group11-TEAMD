package levelselection;

import java.util.ArrayList;

/**
 * Strategy interface for different level selection behaviors
 */
public interface LevelSelectionStrategy {
    /**
     * Gets the list of levels to display based on the strategy
     * @return List of level names that should be shown
     */
    ArrayList<String> getLevelsToShow();

    /**
     * Gets the title to display for this selection mode
     * @return Title string for the UI
     */
    String getSelectionTitle();

    /**
     * Gets the description to display for this selection mode
     * @return Description string for the UI
     */
    String getSelectionDescription();
}
