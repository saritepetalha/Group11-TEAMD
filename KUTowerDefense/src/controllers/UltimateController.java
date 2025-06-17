package controllers;

import models.PlayingModel;

/**
 * GRASP Controller Pattern implementation for Ultimate abilities
 * Handles all user interactions related to ultimate abilities
 */
public class UltimateController {
    private PlayingModel model;

    public UltimateController(PlayingModel model) {
        this.model = model;
    }

    /**
     * Handles mouse clicks for ultimate ability usage and targeting
     */
    public boolean handleMouseClick(int x, int y) {
        // Block ultimate abilities when game is paused
        if (model.isGamePaused()) return false;

        if (model.getUltiManager() == null) {
            return false;
        }

        // Handle gold factory placement
        if (model.getUltiManager().isGoldFactorySelected()) {
            return handleGoldFactoryPlacement(x, y);
        }

        return false;
    }

    /**
     * Handles mouse pressed events for ultimate targeting
     */
    public boolean handleMousePressed(int x, int y) {
        // Block ultimate abilities when game is paused
        if (model.isGamePaused()) return false;

        if (model.getUltiManager() == null) {
            return false;
        }

        // Handle lightning targeting
        if (model.getUltiManager().isWaitingForLightningTarget()) {
            // Only trigger lightning if we have enough gold
            if (model.getUltiManager().isWaitingForLightningTarget()) {
                // Don't handle mouse press here — just acknowledge targeting mode
                return false;
            }
        }

        return false;
    }

    /**
     * Handles right mouse clicks for canceling ultimate abilities
     */
    public boolean handleRightMouseClick(int x, int y) {
        // Block ultimate abilities when game is paused
        if (model.isGamePaused()) return false;

        if (model.getUltiManager() == null) {
            return false;
        }

        boolean cancelled = false;

        // Cancel lightning targeting mode
        if (model.getUltiManager().isWaitingForLightningTarget()) {
            model.getUltiManager().setWaitingForLightningTarget(false);
            System.out.println("⚡ Lightning targeting cancelled by right-click!");
            cancelled = true;
        }

        // Cancel gold factory placement mode
        if (model.getUltiManager().isGoldFactorySelected()) {
            model.getUltiManager().deselectGoldFactory();
            System.out.println("🏭 Gold Factory placement cancelled by right-click!");
            cancelled = true;
        }

        return cancelled;
    }


    /**
     * Handles gold factory placement at specified coordinates
     */
    private boolean handleGoldFactoryPlacement(int x, int y) {
        if (model.getUltiManager() == null || model.getPlayerManager() == null) {
            return false;
        }
        // Pixel coordinates are already provided, pass them directly to tryPlaceGoldFactory
        // The UltiManager will handle the coordinate conversion internally
        boolean success = model.getUltiManager().tryPlaceGoldFactory(x, y);
        if (success) {
            System.out.println("🏭 Gold Factory placed successfully!");
            return true;
        }

        return false;
    }


    /**
     * Gets available controller instances for UI integration
     */
    public static UltimateController getInstance(models.PlayingModel model) {
        return new UltimateController(model);
    }
}