package ui_p;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PlayingMouseListener implements MouseListener {
    private PlayingUI playingUI;

    public void setPlayingUI(PlayingUI playingUI) {
        this.playingUI = playingUI;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (playingUI != null) {
            playingUI.mouseClicked(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (playingUI != null) {
            playingUI.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (playingUI != null) {
            playingUI.mouseReleased(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (playingUI != null) {
            playingUI.mouseEntered(e);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (playingUI != null) {
            playingUI.mouseExited(e);
        }
    }
} 