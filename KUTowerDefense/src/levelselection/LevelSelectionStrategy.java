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

}
