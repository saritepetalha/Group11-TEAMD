package controllers;

import constants.GameDimensions;
import models.MapModel;
import models.PathValidator;
import models.PlacementResult;
import objects.Tile;
import strategies.TilePlacementStrategy;
import strategies.TilePlacementStrategyFactory;
import ui_p.EditTiles;
import views.MapView;

/**
 * MapController - Coordinates user interactions between Model and View
 * Responsibilities:
 * - Handle mouse events
 * - Coordinate tile placement
 * - Manage editing modes
 * - Strategy pattern for different placement behaviors
 */
public class MapController {

    private MapModel mapModel;
    private MapView mapView;
    private EditTiles editTiles;

    // Current state
    private Tile selectedTile;
    private String currentMode = "Draw";

    // Mouse position tracking
    private int mouseX, mouseY;

    // Strategy pattern for tile placement
    private TilePlacementStrategyFactory strategyFactory;

    // Road building state
    private boolean isFirstTile = true;
    private int prevDx = 0;
    private int prevDy = 0;
    private int lastTileX = -1, lastTileY = -1;
    private int prevDraggedTileX = -1, prevDraggedTileY = -1;

    public MapController(MapModel mapModel, MapView mapView, EditTiles editTiles) {
        this.mapModel = mapModel;
        this.mapView = mapView;
        this.editTiles = editTiles;
        this.strategyFactory = new TilePlacementStrategyFactory();
    }

    // Mouse event handlers
    public void handleMouseClicked(int x, int y) {
        if (x >= GameDimensions.GAME_WIDTH) {
            editTiles.mouseClicked(x, y);
        } else {
            if (currentMode.equals("Erase")) {
                handleEraseTile(x, y);
            } else {
                handlePlaceTile(x, y);
            }
        }
    }

    public void handleMouseMoved(int x, int y) {
        if (x >= GameDimensions.GAME_WIDTH) {
            editTiles.mouseMoved(x, y);
            mapView.setDrawSelected(false);
        } else {
            editTiles.mouseReleased(x, y);

            // Update mouse position for tile preview
            mouseX = (x / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
            mouseY = (y / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;

            mapView.updateMousePosition(x, y);
            mapView.setDrawSelected(true);
        }
    }

    public void handleMousePressed(int x, int y) {
        if (x >= GameDimensions.GAME_WIDTH) {
            editTiles.mousePressed(x, y);
        }
        // Reset road building state
        isFirstTile = true;
        prevDx = 0;
        prevDy = 0;
    }

    public void handleMouseReleased(int x, int y) {
        editTiles.mouseReleased(x, y);
    }

    public void handleMouseDragged(int x, int y) {
        if (x < GameDimensions.GAME_WIDTH && y < GameDimensions.GAME_HEIGHT) {
            handleDragPlacement(x, y);
        }
    }

    // Tile placement logic
    private void handlePlaceTile(int x, int y) {
        if (selectedTile == null) return;

        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;

        PlacementResult result = mapModel.placeTile(tileX, tileY, selectedTile);

        if (!result.isSuccess()) {
            mapView.showPopupMessage(result.getErrorMessage(), x, y);
        }
    }

    private void handleEraseTile(int x, int y) {
        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;

        mapModel.eraseTile(tileX, tileY);
    }

    private void handleDragPlacement(int x, int y) {
        if (selectedTile == null) return;

        // Prevent dragging for pathpoints
        if (selectedTile.getId() == -1 || selectedTile.getId() == -2) {
            return;
        }

        TilePlacementStrategy strategy = strategyFactory.getStrategy(selectedTile, currentMode);
        strategy.executePlacement(x, y, mapModel, this);
    }

    // Mode management
    public void setMode(String mode) {
        this.currentMode = mode;

        // Handle special modes
        switch (mode) {
            case "Fill":
                if (selectedTile != null) {
                    mapModel.fillAllTiles(selectedTile);
                }
                break;
            case "Trash":
                mapModel.resetAllTiles();
                break;
        }
    }

    public void setSelectedTile(Tile tile) {
        this.selectedTile = tile;
        mapView.setSelectedTile(tile);
    }

    public Tile getSelectedTile() {
        return selectedTile;
    }

    // Road building state getters/setters (for strategy pattern)
    public boolean isFirstTile() { return isFirstTile; }
    public void setFirstTile(boolean firstTile) { isFirstTile = firstTile; }

    public int getPrevDx() { return prevDx; }
    public void setPrevDx(int prevDx) { this.prevDx = prevDx; }

    public int getPrevDy() { return prevDy; }
    public void setPrevDy(int prevDy) { this.prevDy = prevDy; }

    public int getLastTileX() { return lastTileX; }
    public void setLastTileX(int lastTileX) { this.lastTileX = lastTileX; }

    public int getLastTileY() { return lastTileY; }
    public void setLastTileY(int lastTileY) { this.lastTileY = lastTileY; }

    public int getPrevDraggedTileX() { return prevDraggedTileX; }
    public void setPrevDraggedTileX(int prevDraggedTileX) { this.prevDraggedTileX = prevDraggedTileX; }

    public int getPrevDraggedTileY() { return prevDraggedTileY; }
    public void setPrevDraggedTileY(int prevDraggedTileY) { this.prevDraggedTileY = prevDraggedTileY; }

    // Save/Load operations
    /**
     * Attempts to save the level with validation
     * @param filename The filename to save to
     * @return ValidationResult indicating success or failure with error message
     */
    public PathValidator.ValidationResult saveLevel(String filename) {
        // Validate before saving
        PathValidator.ValidationResult validationResult = mapModel.validateBeforeSave();

        if (validationResult.isValid()) {
            mapModel.saveLevel(filename);
        }

        return validationResult;
    }

    // Getters for external access
    public MapModel getMapModel() { return mapModel; }

    // Mouse position getters for backwards compatibility
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
} 