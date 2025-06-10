package ui_p;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class PlayingMouseWheelListener implements MouseWheelListener {
    private PlayingUI playingUI;

    public void setPlayingUI(PlayingUI playingUI) {
        this.playingUI = playingUI;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (playingUI != null) {
            playingUI.mouseWheelMoved(e);
        }
    }
} 