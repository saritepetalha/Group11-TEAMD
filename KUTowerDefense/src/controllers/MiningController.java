package controllers;

import models.PlayingModel;
import managers.StoneMiningManager;
import objects.Tile;
import constants.GameDimensions;

/**
 * GRASP Controller Pattern implementation for Mining interactions
 * Handles all user interactions related to stone mining
 */
public class MiningController {
    private PlayingModel model;

    public MiningController(PlayingModel model) {
        this.model = model;
    }

    /**
     * Handles mouse clicks for stone mining interactions
     */
    public boolean handleMouseClick(int x, int y) {
        // Block mining interactions when game is paused
        if (model.isGamePaused()) return false;

        if (model.getStoneMiningManager() == null) {
            return false;
        }

        // Check if mine button was clicked
        if (model.getStoneMiningManager().getMineButton() != null &&
                model.getStoneMiningManager().getMineButton().getBounds().contains(x, y)) {
            // Mine button click is handled by the button itself
            return true;
        }

        // Check if stone tile was clicked
        return handleStoneClick(x, y);
    }

    /**
     * Handles mouse pressed events for mining
     */
    public boolean handleMousePressed(int x, int y) {
        // Block mining interactions when game is paused
        if (model.isGamePaused()) return false;

        if (model.getStoneMiningManager() == null) {
            return false;
        }

        // Handle mine button press
        if (model.getStoneMiningManager().getMineButton() != null &&
                model.getStoneMiningManager().getMineButton().getBounds().contains(x, y)) {
            model.getStoneMiningManager().mousePressed(x, y);
            return true;
        }

        // Handle stone tile press for mining
        boolean stoneHandled = handleStoneClick(x, y);
        if (!stoneHandled) {
            // Clear mining button if clicked elsewhere
            model.getStoneMiningManager().clearMiningButton();
        }

        // Handle general mouse pressed for stone mining manager
        model.getStoneMiningManager().mousePressed(x, y);
        return stoneHandled;
    }

    /**
     * Handles mouse released events for mining
     */
    public boolean handleMouseReleased(int x, int y) {
        // Block mining interactions when game is paused
        if (model.isGamePaused()) return false;

        if (model.getStoneMiningManager() == null) {
            return false;
        }

        model.getStoneMiningManager().mouseReleased(x, y);
        return true;
    }

    /**
     * Handles mouse moved events for mining
     */
    public boolean handleMouseMoved(int x, int y) {
        // Allow mouse moved for mining UI elements even when paused (for tooltips)
        if (model.getStoneMiningManager() == null) {
            return false;
        }

        model.getStoneMiningManager().mouseMoved(x, y);
        return true;
    }

    /**
     * Handles clicking on stone tiles for mining
     */
    private boolean handleStoneClick(int x, int y) {
        int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;

        if (tileX >= 0 && tileX < model.getLevel()[0].length &&
                tileY >= 0 && tileY < model.getLevel().length) {
            int tileId = model.getLevel()[tileY][tileX];

            // Check if it's a stone tile (19 or 23 are stone tile IDs)
            if (tileId == 19 || tileId == 23) {
                model.getStoneMiningManager().handleStoneClick(new Tile(tileX, tileY, tileId));
                return true;
            }
        }

        return false;
    }

    /**
     * Updates the mining system
     */
    public void update() {
        if (model.getStoneMiningManager() != null) {
            model.getStoneMiningManager().update();
        }
    }

    /**
     * Checks if mining is currently in progress
     */
    public boolean isMiningInProgress() {
        if (model.getStoneMiningManager() == null) {
            return false;
        }
        return model.getStoneMiningManager().isMiningInProgress();
    }

    /**
     * Checks if there's an active mining button
     */
    public boolean hasActiveMiningButton() {
        if (model.getStoneMiningManager() == null) {
            return false;
        }
        return model.getStoneMiningManager().getMineButton() != null;
    }

    /**
     * Clears any active mining operations
     */
    public void clearMiningOperations() {
        if (model.getStoneMiningManager() != null) {
            model.getStoneMiningManager().clearMiningButton();
        }
    }
}