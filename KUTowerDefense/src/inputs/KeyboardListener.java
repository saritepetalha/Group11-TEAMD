package inputs;

import java.awt.event.KeyListener;

import main.Game;
import main.GameStates;
import ui_p.DeadTree;

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
            GameStates.gameState = GameStates.MENU;
            game.getIntro().stopMusic();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_A) {
            GameStates.gameState = GameStates.MENU;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            GameStates.gameState = GameStates.PLAYING;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            GameStates.gameState = GameStates.OPTIONS;
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
                }
            }

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

}
