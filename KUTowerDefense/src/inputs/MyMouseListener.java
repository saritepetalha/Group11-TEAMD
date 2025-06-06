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
        if (GameStates.gameState == GameStates.PLAYING) {
            if (game.getPlaying() != null) {                // ← ADD
                game.getPlaying().mouseDragged(e.getX(), e.getY());
            }
        }
        else if (GameStates.gameState == GameStates.MENU) {
            game.getMenu().mouseDragged(e.getX(), e.getY());
        }
        else if (GameStates.gameState == GameStates.OPTIONS) {
            game.getOptions().mouseDragged(e.getX(), e.getY());
        }
        else if (GameStates.gameState == GameStates.EDIT) {
            game.getMapEditing().mouseDragged(e.getX(), e.getY());
        }
        else if (GameStates.gameState == GameStates.GAME_OVER) {
            if (game.getGameOverScene() != null) {
                game.getGameOverScene().mouseDragged(e.getX(), e.getY());
            }
        }
        else if (GameStates.gameState == GameStates.STATISTICS) {
            if (game.getStatisticsScene() != null) {
                game.getStatisticsScene().mouseDragged(e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (GameStates.gameState == GameStates.PLAYING) {
            if (game.getPlaying() != null) {
                game.getPlaying().mouseMoved(e.getX(), e.getY());
            }
        }
        else if (GameStates.gameState == GameStates.MENU) {
            game.getMenu().mouseMoved(e.getX(), e.getY());
        }
        else if (GameStates.gameState == GameStates.OPTIONS) {
            game.getOptions().mouseMoved(e.getX(), e.getY());
        }
        else if (GameStates.gameState == GameStates.EDIT) {
            game.getMapEditing().mouseMoved(e.getX(), e.getY());
        }
        else if (GameStates.gameState == GameStates.GAME_OVER) {
            if (game.getGameOverScene() != null) {
                game.getGameOverScene().mouseMoved(e.getX(), e.getY());
            }
        }
        else if (GameStates.gameState == GameStates.STATISTICS) {
            if (game.getStatisticsScene() != null) {
                game.getStatisticsScene().mouseMoved(e.getX(), e.getY());
            }
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying() != null) {
                    game.getPlaying().mouseClicked(e.getX(), e.getY());
                }
            }
            else if (GameStates.gameState == GameStates.MENU) {
                game.getMenu().mouseClicked(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.OPTIONS) {
                game.getOptions().mouseClicked(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.EDIT) {
                game.getMapEditing().mouseClicked(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.GAME_OVER) {
                if (game.getGameOverScene() != null) {
                    game.getGameOverScene().mouseClicked(e.getX(), e.getY());
                }
            }
            else if (GameStates.gameState == GameStates.STATISTICS) {
                game.getStatisticsScene().mouseClicked(e.getX(), e.getY());
            }

        }
        else if (e.getButton() == MouseEvent.BUTTON3) {
            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying() != null) {
                    game.getPlaying().rightMouseClicked(e.getX(), e.getY());
                }
            }
            System.out.println("Right mouse button was clicked");
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Only handle left-click presses to avoid triggering actions on right-click
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying() != null) {
                    game.getPlaying().mousePressed(e.getX(), e.getY());
                }
            }
            else if (GameStates.gameState == GameStates.MENU) {
                game.getMenu().mousePressed(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.OPTIONS) {
                game.getOptions().mousePressed(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.EDIT) {
                game.getMapEditing().mousePressed(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.GAME_OVER) {
                if (game.getGameOverScene() != null) {
                    game.getGameOverScene().mousePressed(e.getX(), e.getY());
                }
            }
            else if (GameStates.gameState == GameStates.STATISTICS) {
                game.getStatisticsScene().mousePressed(e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Only handle left-click releases to be consistent with mousePressed
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying() != null) {
                    game.getPlaying().mouseReleased(e.getX(), e.getY());
                }
            }
            else if (GameStates.gameState == GameStates.MENU) {
                game.getMenu().mouseReleased(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.OPTIONS) {
                game.getOptions().mouseReleased(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.EDIT) {
                game.getMapEditing().mouseReleased(e.getX(), e.getY());
            }
            else if (GameStates.gameState == GameStates.STATISTICS) {
                game.getStatisticsScene().mouseReleased(e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Forward mouse wheel events to the appropriate scene
        if (GameStates.gameState == GameStates.PLAYING) {
            if (game.getPlaying() != null) {
                game.getPlaying().mouseWheelMoved(e);
            }
        }
        else if (GameStates.gameState == GameStates.MENU) {
            // Add mouseWheelMoved method to Menu class if needed
        }
        else if (GameStates.gameState == GameStates.OPTIONS) {
            // Add mouseWheelMoved method to Options class if needed
        }
        else if (GameStates.gameState == GameStates.EDIT) {
            // Add mouseWheelMoved method to MapEditing class if needed
        }
        else if (GameStates.gameState == GameStates.STATISTICS) {
            game.getStatisticsScene().mouseWheelMoved(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

}
