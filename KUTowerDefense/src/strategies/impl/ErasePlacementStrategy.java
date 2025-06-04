package strategies.impl;

import constants.GameDimensions;
import controllers.MapController;
import models.MapModel;
import strategies.TilePlacementStrategy;

/**
 * Strategy for erasing tiles
 */
public class ErasePlacementStrategy implements TilePlacementStrategy {
    
    @Override
    public void executePlacement(int x, int y, MapModel mapModel, MapController controller) {
        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;
        
        // Skip if same tile as last erasure
        if (controller.getLastTileX() == tileX && controller.getLastTileY() == tileY) {
            return;
        }
        
        mapModel.eraseTile(tileX, tileY);
        
        controller.setLastTileX(tileX);
        controller.setLastTileY(tileY);
    }
} 