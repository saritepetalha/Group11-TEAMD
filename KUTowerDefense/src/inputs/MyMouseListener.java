package inputs;

import main.Game;
import main.GameStates;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseEvent;

public class MyMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Game game;

    public MyMouseListener(Game game) {
        this.game = game;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int scaledX = game.getGameScreen().getScaledMouseX(e.getX());
        int scaledY = game.getGameScreen().getScaledMouseY(e.getY());

        switch (GameStates.gameState) {
            case MENU:
                game.getMenu().mouseDragged(scaledX, scaledY);
                break;
            case PLAYING:
                game.getPlaying().mouseDragged(scaledX, scaledY);
                break;
            case OPTIONS:
                game.getOptions().mouseDragged(scaledX, scaledY);
                break;
            case EDIT:
                game.getMapEditing().mouseDragged(scaledX, scaledY);
                break;
            case GAME_OVER:
                game.getGameOverScene().mouseDragged(scaledX, scaledY);
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int scaledX = game.getGameScreen().getScaledMouseX(e.getX());
        int scaledY = game.getGameScreen().getScaledMouseY(e.getY());

        switch (GameStates.gameState) {
            case MENU:
                game.getMenu().mouseMoved(scaledX, scaledY);
                break;
            case PLAYING:
                game.getPlaying().mouseMoved(scaledX, scaledY);
                break;
            case OPTIONS:
                game.getOptions().mouseMoved(scaledX, scaledY);
                break;
            case EDIT:
                game.getMapEditing().mouseMoved(scaledX, scaledY);
                break;
            case GAME_OVER:
                game.getGameOverScene().mouseMoved(scaledX, scaledY);
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int scaledX = game.getGameScreen().getScaledMouseX(e.getX());
        int scaledY = game.getGameScreen().getScaledMouseY(e.getY());

        switch (GameStates.gameState) {
            case MENU:
                game.getMenu().mouseClicked(scaledX, scaledY);
                break;
            case PLAYING:
                game.getPlaying().mouseClicked(scaledX, scaledY);
                break;
            case OPTIONS:
                game.getOptions().mouseClicked(scaledX, scaledY);
                break;
            case EDIT:
                game.getMapEditing().mouseClicked(scaledX, scaledY);
                break;
            case GAME_OVER:
                game.getGameOverScene().mouseClicked(scaledX, scaledY);
                break;
            default:
                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int scaledX = game.getGameScreen().getScaledMouseX(e.getX());
        int scaledY = game.getGameScreen().getScaledMouseY(e.getY());

        switch (GameStates.gameState) {
            case MENU:
                game.getMenu().mousePressed(scaledX, scaledY);
                break;
            case PLAYING:
                game.getPlaying().mousePressed(scaledX, scaledY);
                break;
            case OPTIONS:
                game.getOptions().mousePressed(scaledX, scaledY);
                break;
            case EDIT:
                game.getMapEditing().mousePressed(scaledX, scaledY);
                break;
            case GAME_OVER:
                game.getGameOverScene().mousePressed(scaledX, scaledY);
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int scaledX = game.getGameScreen().getScaledMouseX(e.getX());
        int scaledY = game.getGameScreen().getScaledMouseY(e.getY());

        switch (GameStates.gameState) {
            case MENU:
                game.getMenu().mouseReleased(scaledX, scaledY);
                break;
            case PLAYING:
                game.getPlaying().mouseReleased(scaledX, scaledY);
                break;
            case OPTIONS:
                game.getOptions().mouseReleased(scaledX, scaledY);
                break;
            case EDIT:
                game.getMapEditing().mouseReleased(scaledX, scaledY);
                break;
            case GAME_OVER:
                game.getGameOverScene().mouseReleased(scaledX, scaledY);
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int scaledX = game.getGameScreen().getScaledMouseX(e.getX());
        int scaledY = game.getGameScreen().getScaledMouseY(e.getY());

        switch (GameStates.gameState) {
            case PLAYING:
                game.getPlaying().mouseWheelMoved(e);
                break;
            case EDIT:
                game.getMapEditing().mouseWheelMoved(e);
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

}
