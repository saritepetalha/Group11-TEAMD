package inputs;

import java.awt.event.KeyListener;

import main.Game;
import main.GameStates;

import java.awt.event.KeyEvent;

import scenes.MapEditing;

public class KeyboardListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {}
    private MapEditing mapEditing;
    private Game game;

    public KeyboardListener(MapEditing mapEditing, Game game) {
        this.mapEditing = mapEditing;
        this.game = game;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Handle F11 key for fullscreen toggle
        if (e.getKeyCode() == KeyEvent.VK_F11) {
            game.toggleFullscreen();
            return;
        }

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
            System.out.println("ESC key pressed - clearing tile selection");

            // If in edit state, clear the tile selection
            if (GameStates.gameState == GameStates.EDIT) {
                mapEditing.setDrawSelected(false);
                mapEditing.setSelectedTile(null);
            }

            // If in playing state, clear the tile selection
            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying().getSelectedDeadTree() != null) {
                    game.getPlaying().getSelectedDeadTree().setShowChoices(false);
                    game.getPlaying().setSelectedDeadTree(null);
                }
                if (game.getPlaying().getDisplayedTower() != null) {
                    game.getPlaying().setDisplayedTower(null);
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

}
