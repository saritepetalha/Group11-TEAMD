package strategies.impl;

import constants.GameDimensions;
import controllers.MapController;
import models.MapModel;
import strategies.TilePlacementStrategy;

/**
 * Strategy for placing castles (2x2 tiles)
 */
public class CastlePlacementStrategy implements TilePlacementStrategy {
    
    @Override
    public void executePlacement(int x, int y, MapModel mapModel, MapController controller) {
        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;
        
        // Skip if same tile as last placement
        if (controller.getLastTileX() == tileX && controller.getLastTileY() == tileY) {
            return;
        }
        
        // Check if we can place a 2x2 castle
        if (tileY + 1 < mapModel.getHeight() && tileX + 1 < mapModel.getWidth()) {
            if (controller.getSelectedTile() != null) {
                mapModel.placeTile(tileX, tileY, controller.getSelectedTile());
                
                controller.setLastTileX(tileX);
                controller.setLastTileY(tileY);
                controller.setPrevDraggedTileX(tileX);
                controller.setPrevDraggedTileY(tileY);
            }
        }
    }
} 