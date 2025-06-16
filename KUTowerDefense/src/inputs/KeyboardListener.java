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
                mapEditing.setDrawSelected(false);
                mapEditing.setSelectedTile(null);
                hasSelectionToClear = true;
                System.out.println("ESC: Cleared edit tile selection");
            }

            // If in playing state, handle selections and fullscreen
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

                // Only handle fullscreen in playing mode
                // If currently in fullscreen, prioritize exiting fullscreen over clearing selections
                if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
                    System.out.println("ESC: Exiting fullscreen mode (playing mode only)");
                    game.getFullscreenManager().exitFullscreen();
                }
                // If no selection to clear and not in fullscreen, enter fullscreen
                else if (!hasSelectionToClear && game.getFullscreenManager() != null) {
                    System.out.println("ESC: Entering fullscreen mode (playing mode only)");
                    game.getFullscreenManager().enterFullscreen();
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_F11) {
            // F11 toggles fullscreen, but only in playing mode
            if (GameStates.gameState == GameStates.PLAYING && game.getFullscreenManager() != null) {
                System.out.println("F11: Toggling fullscreen mode (playing mode only)");
                game.getFullscreenManager().toggleFullscreen();
            } else if (GameStates.gameState != GameStates.PLAYING) {
                System.out.println("F11: Fullscreen is only available in playing mode");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

}
