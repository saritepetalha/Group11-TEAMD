package inputs;

import java.awt.event.KeyListener;

import main.Game;
import main.GameStates;

import java.awt.event.KeyEvent;


public class KeyboardListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {}
    private scenes.MapEditing mapEditing;
    private Game game;

    public KeyboardListener(scenes.MapEditing mapEditing, Game game) {
        this.mapEditing = mapEditing;
        this.game = game;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // if in intro, any key skips to menu
        if (GameStates.gameState == GameStates.INTRO) {
            game.getIntro().stopMusic();
            game.changeGameState(GameStates.MENU);
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_A) {
            game.changeGameState(GameStates.MENU);
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            game.changeGameState(GameStates.PLAYING);
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            game.changeGameState(GameStates.OPTIONS);
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.out.println("ESC key pressed");

            // Check if we can clear selections first
            boolean hasSelectionToClear = false;

            // If in edit state, clear the tile selection
            if (GameStates.gameState == GameStates.EDIT) {
                // Check if there's a selected tile through the controller
                boolean hasSelectedTile = false;
                if (mapEditing != null && mapEditing.getMapController() != null) {
                    hasSelectedTile = mapEditing.getMapController().getSelectedTile() != null;
                }

                if (hasSelectedTile) {
                    mapEditing.setDrawSelected(false);
                    mapEditing.setSelectedTile(null);
                    hasSelectionToClear = true;
                    System.out.println("ESC: Cleared edit tile selection");
                }
            }

            // If in playing state, clear the tile selection
            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying().getSelectedDeadTree() != null) {
                    game.getPlaying().getSelectedDeadTree().setShowChoices(false);
                    game.getPlaying().setSelectedDeadTree(null);
                    hasSelectionToClear = true;
                    System.out.println("ESC: Cleared dead tree selection");
                }
                if (game.getPlaying().getDisplayedTower() != null) {
                    game.getPlaying().setDisplayedTower(null);
                    hasSelectionToClear = true;
                    System.out.println("ESC: Cleared tower selection");
                }
            }

            // If in statistics scene, always toggle fullscreen
            if (GameStates.gameState == GameStates.STATISTICS) {
                if (game.getFullscreenManager() != null) {
                    System.out.println("ESC: Toggling fullscreen mode from statistics");
                    game.getFullscreenManager().toggleFullscreen();
                }
                return;
            }

            // If no selection to clear, toggle fullscreen
            if (!hasSelectionToClear && game.getFullscreenManager() != null) {
                System.out.println("ESC: Toggling fullscreen mode");
                game.getFullscreenManager().toggleFullscreen();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_F11) {
            // F11 always toggles fullscreen regardless of selections
            if (game.getFullscreenManager() != null) {
                System.out.println("F11: Toggling fullscreen mode");
                game.getFullscreenManager().toggleFullscreen();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

}
