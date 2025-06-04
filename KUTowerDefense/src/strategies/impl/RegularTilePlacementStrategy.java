package strategies.impl;

import constants.GameDimensions;
import controllers.MapController;
import models.MapModel;
import strategies.TilePlacementStrategy;

/**
 * Strategy for placing regular tiles (grass, water, etc.)
 */
public class RegularTilePlacementStrategy implements TilePlacementStrategy {
    
    @Override
    public void executePlacement(int x, int y, MapModel mapModel, MapController controller) {
        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;
        
        // Skip if same tile as last placement
        if (controller.getLastTileX() == tileX && controller.getLastTileY() == tileY) {
            return;
        }
        
        if (controller.getSelectedTile() != null) {
            mapModel.placeTile(tileX, tileY, controller.getSelectedTile());
            
            controller.setLastTileX(tileX);
            controller.setLastTileY(tileY);
        }
    }
} 