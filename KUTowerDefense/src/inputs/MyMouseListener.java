package inputs;

import main.Game;
import main.GameStates;
import constants.GameDimensions;
import java.awt.Point;

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

    /**
     * Scales mouse coordinates for fullscreen mode using the same logic as rendering
     */
    private Point getScaledMouseCoordinates(int x, int y) {
        if (game.getFullscreenManager() == null || !game.getFullscreenManager().isFullscreen()) {
            return new Point(x, y);
        }

        // Only apply coordinate scaling for custom-rendered scenes
        boolean usesCustomRendering = (GameStates.gameState != GameStates.OPTIONS &&
                GameStates.gameState != GameStates.LOAD_GAME &&
                GameStates.gameState != GameStates.NEW_GAME_LEVEL_SELECT &&
                GameStates.gameState != GameStates.SKILL_SELECTION);

        if (!usesCustomRendering) {
            // For JPanel-based scenes, coordinates are handled by the wrapper
            return new Point(x, y);
        }

        // Calculate the same scaling as done in GameScreen.paintComponent
        int baseWidth, baseHeight;
        if (GameStates.gameState == GameStates.PLAYING || GameStates.gameState == GameStates.GAME_OVER) {
            // For playing state, use actual level dimensions if available
            if (game.getPlaying() != null && game.getPlaying().getController() != null
                    && game.getPlaying().getController().getModel() != null
                    && game.getPlaying().getController().getModel().getLevel() != null) {

                int[][] level = game.getPlaying().getController().getModel().getLevel();
                int levelCols = level[0].length;
                int levelRows = level.length;
                baseWidth = levelCols * GameDimensions.TILE_DISPLAY_SIZE;
                baseHeight = levelRows * GameDimensions.TILE_DISPLAY_SIZE;
            } else {
                // Fallback to default dimensions
                baseWidth = GameDimensions.GAME_WIDTH;
                baseHeight = GameDimensions.GAME_HEIGHT;
            }
        } else if (GameStates.gameState == GameStates.EDIT) {
            baseWidth = GameDimensions.TOTAL_GAME_WIDTH;
            baseHeight = GameDimensions.GAME_HEIGHT;
        } else {
            baseWidth = GameDimensions.MAIN_MENU_SCREEN_WIDTH;
            baseHeight = GameDimensions.MAIN_MENU_SCREEN_HEIGHT;
        }

        // Get GameScreen dimensions
        int screenWidth = game.getGameScreen().getWidth();
        int screenHeight = game.getGameScreen().getHeight();

        // Calculate scaling using the same method as GameScreen
        double scaleX = (double) screenWidth / baseWidth;
        double scaleY = (double) screenHeight / baseHeight;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) (baseWidth * scale);
        int scaledHeight = (int) (baseHeight * scale);
        int offsetX = (screenWidth - scaledWidth) / 2;
        int offsetY = (screenHeight - scaledHeight) / 2;

        // Convert screen coordinates to game coordinates
        int gameX = (int) ((x - offsetX) / scale);
        int gameY = (int) ((y - offsetY) / scale);

        return new Point(gameX, gameY);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point scaledCoords = getScaledMouseCoordinates(e.getX(), e.getY());

        if (GameStates.gameState == GameStates.PLAYING) {
            if (game.getPlaying() != null) {                // ← ADD
                game.getPlaying().mouseDragged(scaledCoords.x, scaledCoords.y);
            }
        }
        else if (GameStates.gameState == GameStates.MENU) {
            game.getMenu().mouseDragged(scaledCoords.x, scaledCoords.y);
        }
        else if (GameStates.gameState == GameStates.OPTIONS) {
            game.getOptions().mouseDragged(scaledCoords.x, scaledCoords.y);
        }
        else if (GameStates.gameState == GameStates.EDIT) {
            game.getMapEditing().mouseDragged(scaledCoords.x, scaledCoords.y);
        }
        else if (GameStates.gameState == GameStates.GAME_OVER) {
            if (game.getGameOverScene() != null) {
                game.getGameOverScene().mouseDragged(scaledCoords.x, scaledCoords.y);
            }
        }
        else if (GameStates.gameState == GameStates.STATISTICS) {
            if (game.getStatisticsScene() != null) {
                game.getStatisticsScene().mouseDragged(scaledCoords.x, scaledCoords.y);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point scaledCoords = getScaledMouseCoordinates(e.getX(), e.getY());

        if (GameStates.gameState == GameStates.PLAYING) {
            if (game.getPlaying() != null) {
                game.getPlaying().mouseMoved(scaledCoords.x, scaledCoords.y);
            }
        }
        else if (GameStates.gameState == GameStates.MENU) {
            game.getMenu().mouseMoved(scaledCoords.x, scaledCoords.y);
        }
        else if (GameStates.gameState == GameStates.OPTIONS) {
            game.getOptions().mouseMoved(scaledCoords.x, scaledCoords.y);
        }
        else if (GameStates.gameState == GameStates.EDIT) {
            game.getMapEditing().mouseMoved(scaledCoords.x, scaledCoords.y);
        }
        else if (GameStates.gameState == GameStates.GAME_OVER) {
            if (game.getGameOverScene() != null) {
                game.getGameOverScene().mouseMoved(scaledCoords.x, scaledCoords.y);
            }
        }
        else if (GameStates.gameState == GameStates.STATISTICS) {
            if (game.getStatisticsScene() != null) {
                game.getStatisticsScene().mouseMoved(scaledCoords.x, scaledCoords.y);
            }
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point scaledCoords = getScaledMouseCoordinates(e.getX(), e.getY());

        if (e.getButton() == MouseEvent.BUTTON1) {
            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying() != null) {
                    game.getPlaying().mouseClicked(scaledCoords.x, scaledCoords.y);
                }
            }
            else if (GameStates.gameState == GameStates.MENU) {
                game.getMenu().mouseClicked(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.OPTIONS) {
                game.getOptions().mouseClicked(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.EDIT) {
                game.getMapEditing().mouseClicked(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.GAME_OVER) {
                if (game.getGameOverScene() != null) {
                    game.getGameOverScene().mouseClicked(scaledCoords.x, scaledCoords.y);
                }
            }
            else if (GameStates.gameState == GameStates.STATISTICS) {
                game.getStatisticsScene().mouseClicked(scaledCoords.x, scaledCoords.y);
            }

        }
        else if (e.getButton() == MouseEvent.BUTTON3) {
            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying() != null) {
                    game.getPlaying().rightMouseClicked(scaledCoords.x, scaledCoords.y);
                }
            }
            System.out.println("Right mouse button was clicked");
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Only handle left-click presses to avoid triggering actions on right-click
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point scaledCoords = getScaledMouseCoordinates(e.getX(), e.getY());

            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying() != null) {
                    game.getPlaying().mousePressed(scaledCoords.x, scaledCoords.y);
                }
            }
            else if (GameStates.gameState == GameStates.MENU) {
                game.getMenu().mousePressed(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.OPTIONS) {
                game.getOptions().mousePressed(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.EDIT) {
                game.getMapEditing().mousePressed(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.GAME_OVER) {
                if (game.getGameOverScene() != null) {
                    game.getGameOverScene().mousePressed(scaledCoords.x, scaledCoords.y);
                }
            }
            else if (GameStates.gameState == GameStates.STATISTICS) {
                game.getStatisticsScene().mousePressed(scaledCoords.x, scaledCoords.y);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Only handle left-click releases to be consistent with mousePressed
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point scaledCoords = getScaledMouseCoordinates(e.getX(), e.getY());

            if (GameStates.gameState == GameStates.PLAYING) {
                if (game.getPlaying() != null) {
                    game.getPlaying().mouseReleased(scaledCoords.x, scaledCoords.y);
                }
            }
            else if (GameStates.gameState == GameStates.MENU) {
                game.getMenu().mouseReleased(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.OPTIONS) {
                game.getOptions().mouseReleased(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.EDIT) {
                game.getMapEditing().mouseReleased(scaledCoords.x, scaledCoords.y);
            }
            else if (GameStates.gameState == GameStates.STATISTICS) {
                game.getStatisticsScene().mouseReleased(scaledCoords.x, scaledCoords.y);
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
