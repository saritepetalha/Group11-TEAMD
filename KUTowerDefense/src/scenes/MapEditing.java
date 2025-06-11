package scenes;

import constants.GameDimensions;
import controllers.MapController;
import helpMethods.LoadSave;
import helpMethods.LevelBuilder;
import main.Game;
import managers.TileManager;
import models.MapModel;
import models.PathValidator;
import objects.Tile;
import objects.Tower;
import ui_p.EditTiles;
import views.MapView;

import javax.swing.*;
import java.awt.*;

/**
 * MapEditing - Now powered by MVC architecture internally
 * Maintains 100% API compatibility while using clean MVC components under the hood
 *
 * REFACTORED: This class now delegates to MVC components instead of handling everything directly
 *
 * STATE MANAGEMENT:
 * - When created from Main Menu: Creates fresh empty grass map (createNewMap = true)
 * - When created from Level Selection: Loads existing level data (createNewMap = false)
 * - Game.handleMapEditingStateChange() manages this automatically
 */
public class MapEditing extends GameScene implements SceneMethods {

    // MVC Components (internal - hidden from external API)
    private MapModel mapModel;
    private MapView mapView;
    private MapController mapController;

    // UI Components
    private EditTiles editTiles;

    // Game references
    private final Window owner;

    // Legacy state variables (for API compatibility)
    private Tower selectedTower;

    // Track how this editing session was initiated
    private boolean isNewMapMode = false;

    public MapEditing(Game game, Window owner) {
        this(game, owner, true); // Default to new map mode for backwards compatibility
    }

    public MapEditing(Game game, Window owner, boolean createNewMap) {
        super(game);
        this.owner = owner;
        this.isNewMapMode = createNewMap;

        // Initialize MVC architecture internally
        initializeMVC();
        createDefaultLevel();
        loadDefaultLevel();
    }

    /**
     * Initialize the MVC components internally
     */
    private void initializeMVC() {
        // Initialize model based on the mode
        if (isNewMapMode) {
            // Create fresh grass map (9x16 default game size)
            mapModel = new MapModel(16, 9);
            System.out.println("Created new empty map for editing (9x16)");
        } else {
            // Load existing level data for editing
            int[][] level = LevelBuilder.getLevelData();
            mapModel = new MapModel(level);
            System.out.println("Loaded existing map for editing");
        }

        // Initialize view
        mapView = new MapView(mapModel);

        // Initialize UI
        editTiles = new EditTiles(
                GameDimensions.GAME_WIDTH,
                0,
                4 * GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.GAME_HEIGHT,
                this,
                game,
                owner
        );

        // Initialize controller
        mapController = new MapController(mapModel, mapView, editTiles);

        // Connect EditTiles to controller
        editTiles.setMapController(mapController);
    }

    private void createDefaultLevel() {
        // This is now handled by MapModel initialization
    }

    private void loadDefaultLevel() {
        int[][] lvl = LoadSave.getLevelData("defaultleveltest1");
        if (lvl != null) {
            System.out.println(java.util.Arrays.deepToString(lvl));
        }
    }

    @Override
    public void render(Graphics g) {
        setCustomCursor();

        // Delegate rendering to MVC view
        mapView.render(g);
        editTiles.draw(g);
    }

    // Legacy API methods - delegate to MVC components
    public void modifyTile(int x, int y) {
        // Convert to tile coordinates and delegate to model
        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;

        if (mapController.getSelectedTile() != null) {
            var result = mapModel.placeTile(tileX, tileY, mapController.getSelectedTile());
            if (!result.isSuccess()) {
                mapView.showPopupMessage(result.getErrorMessage(), x, y);
            }
        }
    }

    public void eraseTile(int x, int y) {
        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;
        mapModel.eraseTile(tileX, tileY);
    }

    public void fillAllTiles() {
        if (mapController.getSelectedTile() != null) {
            mapModel.fillAllTiles(mapController.getSelectedTile());
        }
    }

    public void resetAllTiles() {
        mapModel.resetAllTiles();
    }

    public void saveLevel(String filename) {
        // Use validation like the MVC controller
        PathValidator.ValidationResult validationResult = mapModel.validateBeforeSave();

        if (!validationResult.isValid()) {
            // Show validation error popup
            JOptionPane.showMessageDialog(owner, validationResult.getErrorMessage(), "Map Save Error", JOptionPane.WARNING_MESSAGE);
        } else {
            // Save the map if validation passes
            mapModel.saveLevel(filename);
            // Show success message
            JOptionPane.showMessageDialog(owner, "Map saved successfully!", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void setDrawSelected(boolean drawSelected) {
        mapView.setDrawSelected(drawSelected);
    }

    public TileManager getTileManager() {
        return mapModel.getTileManager();
    }

    public void setSelectedTile(Tile selectedTile) {
        mapController.setSelectedTile(selectedTile);
    }

    // Mouse event handlers - delegate to controller
    @Override
    public void mouseClicked(int x, int y) {
        mapController.handleMouseClicked(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        mapController.handleMouseMoved(x, y);
    }

    @Override
    public void mousePressed(int x, int y) {
        mapController.handleMousePressed(x, y);
    }

    @Override
    public void mouseReleased(int x, int y) {
        mapController.handleMouseReleased(x, y);
    }

    @Override
    public void mouseDragged(int x, int y) {
        mapController.handleMouseDragged(x, y);
    }

    // Level management methods
    public void setLevel(int[][] level) {
        mapModel.setLevel(level);
    }

    public void setOverlayData(int[][] overlayData) {
        mapModel.setOverlayData(overlayData);
    }

    public void setCurrentLevelName(String name) {
        mapModel.setCurrentLevelName(name);
    }

    public String getCurrentLevelName() {
        return mapModel.getCurrentLevelName();
    }

    // Legacy getters/setters for backwards compatibility
    public Tower getSelectedTower() {
        return selectedTower;
    }

    public void setSelectedTower(Tower selectedTower) {
        this.selectedTower = selectedTower;
    }

    // Expose MVC components for advanced usage (optional)
    public MapModel getMapModel() {
        return mapModel;
    }

    public MapView getMapView() {
        return mapView;
    }

    public MapController getMapController() {
        return mapController;
    }
}
