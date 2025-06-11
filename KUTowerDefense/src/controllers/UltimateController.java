package controllers;

import models.PlayingModel;
import managers.UltiManager;
import managers.PlayerManager;
import java.awt.event.MouseWheelEvent;
import constants.GameDimensions;

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
        if (model.getUltiManager() == null) {
            return false;
        }

        // Handle lightning targeting
        if (model.getUltiManager().isWaitingForLightningTarget()) {
            model.getUltiManager().triggerLightningAt(x, y);
            return true;
        }

        return false;
    }

    /**
     * Handles right mouse clicks for canceling ultimate abilities
     */
    public boolean handleRightMouseClick(int x, int y) {
        if (model.getUltiManager() == null) {
            return false;
        }

        boolean cancelled = false;

        // Cancel lightning targeting
        if (model.getUltiManager().isWaitingForLightningTarget()) {
            model.getUltiManager().setWaitingForLightningTarget(false);
            System.out.println("⚡ Lightning strike targeting cancelled by right-click!");
            cancelled = true;
        }

        // Cancel gold factory placement
        if (model.getUltiManager().isGoldFactorySelected()) {
            model.getUltiManager().deselectGoldFactory();
            System.out.println("🏭 Gold factory placement cancelled by right-click!");
            cancelled = true;
        }

        return cancelled;
    }

    /**
     * Triggers earthquake ultimate ability
     */
    public boolean triggerEarthquake() {
        if (model.getUltiManager() == null || model.getPlayerManager() == null) {
            return false;
        }

        if (model.getUltiManager().canUseEarthquake()) {
            if (model.getPlayerManager().getGold() >= 50) {
                model.getUltiManager().triggerEarthquake();
                return true;
            } else {
                System.out.println("Not enough gold for Earthquake!");
                return false;
            }
        } else {
            System.out.println("Earthquake is on cooldown!");
            return false;
        }
    }

    /**
     * Initiates lightning strike targeting mode
     */
    public boolean initiateLightningStrike() {
        if (model.getUltiManager() == null || model.getPlayerManager() == null) {
            return false;
        }

        if (model.getUltiManager().isWaitingForLightningTarget()) {
            model.getUltiManager().setWaitingForLightningTarget(false);
            return true;
        } else if (model.getUltiManager().canUseLightning()) {
            if (model.getPlayerManager().getGold() >= 75) {
                model.getUltiManager().setWaitingForLightningTarget(true);
                return true;
            } else {
                System.out.println("Not enough gold for Lightning Strike!");
                return false;
            }
        } else {
            System.out.println("Lightning Strike is on cooldown!");
            return false;
        }
    }

    /**
     * Triggers freeze ultimate ability
     */
    public boolean triggerFreeze() {
        if (model.getUltiManager() == null || model.getPlayerManager() == null) {
            return false;
        }

        if (model.getUltiManager().canUseFreeze()) {
            if (model.getPlayerManager().getGold() >= 60) {
                model.getUltiManager().triggerFreeze();
                return true;
            } else {
                System.out.println("Not enough gold for Freeze!");
                return false;
            }
        } else {
            System.out.println("Freeze is on cooldown!");
            return false;
        }
    }

    /**
     * Selects or deselects gold factory for placement
     */
    public boolean selectGoldFactory() {
        if (model.getUltiManager() == null || model.getPlayerManager() == null) {
            return false;
        }

        // If already selected, deselect it
        if (model.getUltiManager().isGoldFactorySelected()) {
            model.getUltiManager().deselectGoldFactory();
            return true;
        }

        // Check if there's already an active factory
        if (model.getUltiManager().hasActiveGoldFactory()) {
            System.out.println("Only one Gold Factory allowed at a time!");
            return false;
        }

        if (model.getUltiManager().canUseGoldFactory()) {
            if (model.getPlayerManager().getGold() >= 100) {
                model.getUltiManager().selectGoldFactory();
                return true;
            } else {
                System.out.println("Not enough gold for Gold Factory!");
                return false;
            }
        } else {
            System.out.println("Gold Factory is on cooldown!");
            return false;
        }
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
     * Checks if any ultimate ability is currently waiting for user input
     */
    public boolean isWaitingForInput() {
        if (model.getUltiManager() == null) {
            return false;
        }

        return model.getUltiManager().isWaitingForLightningTarget() ||
                model.getUltiManager().isGoldFactorySelected();
    }

    /**
     * Gets the type of input currently being waited for
     */
    public String getWaitingInputType() {
        if (model.getUltiManager() == null) {
            return "NONE";
        }

        if (model.getUltiManager().isWaitingForLightningTarget()) {
            return "LIGHTNING_TARGET";
        } else if (model.getUltiManager().isGoldFactorySelected()) {
            return "GOLD_FACTORY_PLACEMENT";
        }

        return "NONE";
    }

    /**
     * Handles ultimate button clicks from UI
     */
    public boolean handleEarthquakeButton() {
        return triggerEarthquake();
    }

    public boolean handleLightningButton() {
        return initiateLightningStrike();
    }

    public boolean handleFreezeButton() {
        return triggerFreeze();
    }

    public boolean handleGoldFactoryButton() {
        return selectGoldFactory();
    }

    /**
     * Gets available controller instances for UI integration
     */
    public static UltimateController getInstance(models.PlayingModel model) {
        return new UltimateController(model);
    }
}