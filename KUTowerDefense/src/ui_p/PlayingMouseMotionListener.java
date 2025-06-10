package ui_p;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class PlayingMouseMotionListener implements MouseMotionListener {
    private PlayingUI playingUI;

    public void setPlayingUI(PlayingUI playingUI) {
        this.playingUI = playingUI;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (playingUI != null) {
            playingUI.mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (playingUI != null) {
            playingUI.mouseMoved(e);
        }
    }
} 