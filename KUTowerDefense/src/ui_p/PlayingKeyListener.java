package ui_p;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PlayingKeyListener implements KeyListener {
    private PlayingUI playingUI;

    public void setPlayingUI(PlayingUI playingUI) {
        this.playingUI = playingUI;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (playingUI != null) {
            playingUI.keyTyped(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (playingUI != null) {
            playingUI.keyPressed(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (playingUI != null) {
            playingUI.keyReleased(e);
        }
    }
} 