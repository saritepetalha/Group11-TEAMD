package strategies;

import controllers.MapController;
import models.MapModel;

/**
 * Strategy interface for different tile placement behaviors
 * Allows different placement logic for roads, castles, regular tiles, etc.
 */
public interface TilePlacementStrategy {
    void executePlacement(int x, int y, MapModel mapModel, MapController controller);
} 