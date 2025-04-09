package inputs;

import java.awt.event.KeyListener;

import main.GameStates;

import java.awt.event.KeyEvent;

import static main.GameStates.*;

public class KeyboardListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A) {
            GameStates.gameState = GameStates.MENU;
        }
        else if (e.getKeyCode() == KeyEvent.VK_S) {
            GameStates.gameState = GameStates.PLAYING;
        }
        else if (e.getKeyCode() == KeyEvent.VK_D) {
            GameStates.gameState = GameStates.OPTIONS;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

}
