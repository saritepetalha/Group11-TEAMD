package strategies.impl;

import constants.GameDimensions;
import controllers.MapController;
import models.MapModel;
import objects.Tile;
import strategies.TilePlacementStrategy;

/**
 * Strategy for placing roads with intelligent curve and crossing detection
 */
public class RoadPlacementStrategy implements TilePlacementStrategy {
    
    @Override
    public void executePlacement(int x, int y, MapModel mapModel, MapController controller) {
        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;
        
        // Skip if same tile as last placement
        if (controller.getLastTileX() == tileX && controller.getLastTileY() == tileY) {
            return;
        }
        
        // Calculate movement direction
        int dx = tileX - controller.getPrevDraggedTileX();
        int dy = tileY - controller.getPrevDraggedTileY();
        
        // Only process if we've actually moved
        if (dx != 0 || dy != 0) {
            // Normalize movement to single unit (prevents diagonal placement)
            if (Math.abs(dx) > Math.abs(dy)) {
                dx = dx > 0 ? 1 : -1;
                dy = 0;
            } else {
                dx = 0;
                dy = dy > 0 ? 1 : -1;
            }
            
            // Determine what type of road tile to place
            Tile tileToPlace = determineRoadTile(mapModel, tileX, tileY, dx, dy, controller);
            
            if (tileToPlace != null) {
                mapModel.placeTile(tileX, tileY, tileToPlace);
                
                // Update controller state
                controller.setPrevDx(dx);
                controller.setPrevDy(dy);
                controller.setFirstTile(false);
            }
        }
        
        controller.setLastTileX(tileX);
        controller.setLastTileY(tileY);
        controller.setPrevDraggedTileX(tileX);
        controller.setPrevDraggedTileY(tileY);
    }
    
    private Tile determineRoadTile(MapModel mapModel, int tileX, int tileY, int dx, int dy, MapController controller) {
        // Check if we're crossing an existing road
        Tile existingTile = mapModel.getTileManager().getTile(mapModel.getTileAt(tileX, tileY));
        boolean isRoadTile = existingTile != null && existingTile.getName().contains("Road");
        
        if (isRoadTile) {
            return handleRoadCrossing(existingTile, dx, dy, mapModel);
        } else if (controller.isFirstTile()) {
            return getBasicRoadTile(dx, dy, mapModel);
        } else if (dx != controller.getPrevDx() || dy != controller.getPrevDy()) {
            // Direction changed - place a curve
            Tile curvedTile = getCurvedRoadTile(controller.getPrevDx(), controller.getPrevDy(), dx, dy, mapModel);
            
            // Update previous tile if it was a straight road
            if (controller.getPrevDraggedTileX() >= 0 && controller.getPrevDraggedTileY() >= 0) {
                Tile prevTile = mapModel.getTileManager().getTile(
                    mapModel.getTileAt(controller.getPrevDraggedTileX(), controller.getPrevDraggedTileY())
                );
                if (prevTile != null && prevTile.getName().contains("FlatRoad")) {
                    mapModel.placeTile(controller.getPrevDraggedTileX(), controller.getPrevDraggedTileY(), curvedTile);
                    return getBasicRoadTile(dx, dy, mapModel);
                }
            }
            
            return curvedTile;
        } else {
            // Continuing in same direction
            return getBasicRoadTile(dx, dy, mapModel);
        }
    }
    
    private Tile handleRoadCrossing(Tile existingTile, int dx, int dy, MapModel mapModel) {
        String tileName = existingTile.getName();
        
        // Already a four-way crossing
        if (tileName.contains("FourWay")) {
            return mapModel.getTileManager().RoadFourWay;
        }
        
        // Check for horizontal crossing vertical
        if ((tileName.contains("FlatRoadHorizontal") && dy != 0) ||
            (tileName.contains("FlatRoadVertical") && dx != 0)) {
            return mapModel.getTileManager().RoadFourWay;
        }
        
        // For curved roads, create four-way intersection
        if (tileName.contains("CurvedRoad")) {
            return mapModel.getTileManager().RoadFourWay;
        }
        
        return getBasicRoadTile(dx, dy, mapModel);
    }
    
    private Tile getBasicRoadTile(int dx, int dy, MapModel mapModel) {
        if (dx != 0) {
            return mapModel.getTileManager().FlatRoadHorizontal;
        } else {
            return mapModel.getTileManager().FlatRoadVertical;
        }
    }
    
    private Tile getCurvedRoadTile(int oldDx, int oldDy, int newDx, int newDy, MapModel mapModel) {
        // Right to Down or Up to Left
        if ((oldDx == 1 && oldDy == 0 && newDx == 0 && newDy == 1) ||
            (oldDx == 0 && oldDy == -1 && newDx == -1 && newDy == 0)) {
            return mapModel.getTileManager().CurvedRoadRightDown;
        }
        // Right to Up or Down to Left  
        else if ((oldDx == 1 && oldDy == 0 && newDx == 0 && newDy == -1) ||
                 (oldDx == 0 && oldDy == 1 && newDx == -1 && newDy == 0)) {
            return mapModel.getTileManager().CurvedRoadRightUp;
        }
        // Left to Down or Up to Right
        else if ((oldDx == -1 && oldDy == 0 && newDx == 0 && newDy == 1) ||
                 (oldDx == 0 && oldDy == -1 && newDx == 1 && newDy == 0)) {
            return mapModel.getTileManager().CurvedRoadLeftDown;
        }
        // Left to Up or Down to Right
        else if ((oldDx == -1 && oldDy == 0 && newDx == 0 && newDy == -1) ||
                 (oldDx == 0 && oldDy == 1 && newDx == 1 && newDy == 0)) {
            return mapModel.getTileManager().CurvedRoadLeftUp;
        }
        
        // Fallback to straight road
        return getBasicRoadTile(newDx, newDy, mapModel);
    }
} 