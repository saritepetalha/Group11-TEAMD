package controllers;

import models.PlayingModel;
import managers.TreeInteractionManager;
import ui_p.DeadTree;
import ui_p.LiveTree;

/**
 * GRASP Controller Pattern implementation for Tree interactions
 * Handles all user interactions related to live and dead trees
 */
public class TreeController {
    private PlayingModel model;

    public TreeController(PlayingModel model) {
        this.model = model;
    }

    /**
     * Handles mouse clicks for tree interactions
     */
    public boolean handleMouseClick(int x, int y) {
        // Block tree interactions when game is paused
        if (model.isGamePaused()) return false;

        if (model.getTreeInteractionManager() == null) {
            return false;
        }

        boolean handled = false;

        // Handle dead tree interactions first
        if (model.getDeadTrees() != null) {
            handled = handleDeadTreeClick(x, y);
        }

        // Handle live tree interactions if dead trees weren't handled
        if (!handled && model.getLiveTrees() != null) {
            handled = handleLiveTreeClick(x, y);
        }

        return handled;
    }

    /**
     * Handles mouse pressed events for tree interactions
     */
    public boolean handleMousePressed(int x, int y) {
        // Block tree interactions when game is paused
        if (model.isGamePaused()) return false;

        if (model.getTreeInteractionManager() == null) {
            return false;
        }

        // Handle tree interactions
        model.getTreeInteractionManager().handleDeadTreeInteraction(x, y);
        model.getTreeInteractionManager().handleLiveTreeInteraction(x, y);

        return true;
    }

    /**
     * Handles dead tree interactions specifically
     */
    private boolean handleDeadTreeClick(int x, int y) {
        model.getTreeInteractionManager().handleDeadTreeInteraction(x, y);
        return true;
    }

    /**
     * Handles live tree interactions specifically
     */
    private boolean handleLiveTreeClick(int x, int y) {
        model.getTreeInteractionManager().handleLiveTreeInteraction(x, y);
        return true;
    }

    /**
     * Clears all tree selections
     */
    public void clearTreeSelections() {
        // Clear dead tree selection
        if (model.getSelectedDeadTree() != null) {
            model.getSelectedDeadTree().setShowChoices(false);
            model.setSelectedDeadTree(null);
        }

        // Clear ALL dead tree choices
        if (model.getDeadTrees() != null) {
            for (DeadTree deadTree : model.getDeadTrees()) {
                deadTree.setShowChoices(false);
            }
        }

        // Clear ALL live tree choices
        if (model.getLiveTrees() != null) {
            for (LiveTree liveTree : model.getLiveTrees()) {
                liveTree.setShowChoices(false);
            }
        }
    }

    /**
     * Checks if any tree has active choices displayed
     */
    public boolean hasActiveTreeChoices() {
        // Check dead trees
        if (model.getDeadTrees() != null) {
            for (DeadTree deadTree : model.getDeadTrees()) {
                if (deadTree.isShowChoices()) {
                    return true;
                }
            }
        }

        // Check live trees
        if (model.getLiveTrees() != null) {
            for (LiveTree liveTree : model.getLiveTrees()) {
                if (liveTree.isShowChoices()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the currently selected dead tree
     */
    public DeadTree getSelectedDeadTree() {
        return model.getSelectedDeadTree();
    }

    /**
     * Checks if a specific tree is clicked
     */
    public boolean isTreeClicked(int x, int y) {
        // Check dead trees
        if (model.getDeadTrees() != null) {
            for (DeadTree tree : model.getDeadTrees()) {
                if (tree.isClicked(x, y)) {
                    return true;
                }
            }
        }

        // Check live trees
        if (model.getLiveTrees() != null) {
            for (LiveTree tree : model.getLiveTrees()) {
                if (tree.isClicked(x, y)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handles mouse movement for tree tooltips
     */
    public void handleMouseMoved(int x, int y) {
        // Allow mouse moved for tree tooltips even when paused
        // Handle tooltips for live trees
        if (model.getLiveTrees() != null) {
            for (LiveTree liveTree : model.getLiveTrees()) {
                liveTree.handleMouseHover(x, y, model);
            }
        }

        // Note: Dead tree tooltips are handled within the tree objects themselves
    }

    /**
     * Sets a dead tree as selected
     */
    public void selectDeadTree(DeadTree tree) {
        // Clear other selections first
        clearTreeSelections();

        // Set the new selection
        if (tree != null) {
            tree.setShowChoices(true);
            model.setSelectedDeadTree(tree);
        }
    }

    /**
     * Sets a live tree to show choices
     */
    public void selectLiveTree(LiveTree tree) {
        // Clear other selections first
        clearTreeSelections();

        // Set the new selection
        if (tree != null) {
            tree.setShowChoices(true);
        }
    }
}