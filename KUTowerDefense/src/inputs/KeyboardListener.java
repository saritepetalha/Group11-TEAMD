package inputs;

import java.awt.event.KeyListener;

import main.GameStates;

import java.awt.event.KeyEvent;

import static main.GameStates.*;

public class KeyboardListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {}
    private scenes.Playing playing;

    public KeyboardListener(scenes.Playing playing) {
        this.playing = playing;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A) {
            GameStates.gameState = GameStates.MENU;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            GameStates.gameState = GameStates.PLAYING;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            GameStates.gameState = GameStates.OPTIONS;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.out.println("ESC key pressed - clearing tile selection");

            // If in playing state, clear the tile selection
            if (GameStates.gameState == GameStates.PLAYING) {
                playing.setDrawSelected(false);
                playing.setSelectedTile(null);
            }

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

}
